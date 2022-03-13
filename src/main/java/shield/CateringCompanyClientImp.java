/**
 * Class for handling actions and storing information regarding catering companies
 */

package shield;

/**
 * CateringCompanyClient implemented
 */
public class CateringCompanyClientImp implements CateringCompanyClient {

  /**
   * Private variables created:
   *
   * The server endpoint in use
   * Whether the catering company is registered
   * The name of the catering company
   * The postcode of the catering company
   */
  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  /**
   * Class constructor
   *
   * @param endpoint the server endpoint to be used
   */
  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Checks if catering company is registered and if not, registers them
   *
   * @param name the name of the catering company
   * @param postCode the postcode of the catering company
   * @return false if registering catering company fails or true if catering company is now/was previously registered
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {

    // ensure name and postcode are not null
    if (name == null || postCode == null) {
      System.out.println("I'm sorry one of your inputs is null please try again");
      return false;
    }
    assert name!= null;
    assert postCode != null;

    // ensure postCode starts with eh and is 6 or 7 digits long
    if (!postCode.substring(0, 2).equalsIgnoreCase("eh")) {
      if (postCode.length() != 6 && postCode.length() != 7) {
        System.out.println("I'm sorry your postcode is invalid please try again");
        return false;
      }
    }
    assert postCode.substring(0,2).equalsIgnoreCase("eh");
    assert (postCode.length() == 6 || postCode.length() == 7);

    // construct endpoint request
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return false if response does not return appropriate value
      if (!(response.equals("registered new") || response.equals("already registered"))) {
        System.out.println(response);
        return false;
      }
      assert (response.equals("registered new") || response.equals("already registered"));

      // set local variables if first time registration
      if (response.equals("registered new")) {
        this.name = name;
        this.postcode = postCode;
        registered = true;
      }

      // print result and return true
      System.out.println(response);
      return true;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Updates the status of a particular order
   *
   * @param orderNumber the order number
   * @param status the order status - packed/dispatched/delivered
   * @return true if status was changed or false if not
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {

    // ensure appropriate order number
    if (orderNumber <= 0) {
      System.out.println("Sorry order number must be greater than 0 please try again");
      return false;
    }
    assert orderNumber > 0;

    // ensure status is not null and is appropriate value
    if ((status == null) || (!status.equals("packed") && !status.equals("dispatched") && !status.equals("delivered"))) {
      System.out.println("Sorry status update must be packed, dispatched or delivered please try again");
      return false;
    }
    assert status != null;
    assert (status.equals("packed") || status.equals("dispatched") || status.equals("delivered"));

    // construct endpoint request
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return true if status has been successfully changed and false otherwise
      if (response.equalsIgnoreCase("True")) {
        System.out.println("Status has been successfully changed");
        return true;
      } else {
        System.out.println("Status failed to be changed");
        return false;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Getter for company registered status
   *
   * @return true if company is registered and false otherwise
   */
  @Override
  public boolean isRegistered() {
    return registered;
  }

  /**
   * Getter for company name
   *
   * @return the companies name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Getter for the company postcode
   *
   * @return the companies postcode
   */
  @Override
  public String getPostCode() {
    return postcode;
  }
}