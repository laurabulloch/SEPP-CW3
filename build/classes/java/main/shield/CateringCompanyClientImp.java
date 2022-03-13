/**
 *
 */

package shield;

/**
 * CateringCompanyClient implemented
 */
public class CateringCompanyClientImp implements CateringCompanyClient {

  /**
   * Private variables created:
   * the server endpoint in use
   * whether the catering company is registered
   * the name of the catering company
   * the postcode of the catering company
   */
  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;

  /**
   * Class constructor
   * @param endpoint the server endpoint to be used
   */
  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Checks if catering company registered and if not registers them
   * @param name the name of the catering company
   * @param postCode the postcode of the catering company
   * @return False if company unable to be registered or True if catering company was previously registered or is now registered
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {

    // check if catering company registered
    if (registered = true) {
      return true;
    }

    // construct endpoint request
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return True if catering company already registered or now newly registered or return error
      if (response.equals("registered new") || response.equals("already registered")) {
        registered = true;
        this.name = name;
        this.postcode = postCode;
        return true;
      } else {
        System.err.println(response);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Updates the status of a particular order
   * @param orderNumber the order number
   * @param status the order status - packed/dispatched/delivered
   * @return True if status was changed or False if the operation was unsuccessful
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {

    // construct endpoint request
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return True if status changed or False if it was unsuccessful
      return response.equals("True");

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Getter for company registered status
   * @return True if company was/is now registered and False otherwise
   */
  @Override
  public boolean isRegistered() {
    return registered;
  }

  /**
   * Getter for company name
   * @return the companies name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Getter for the company postcode
   * @return the companies postcode
   */
  @Override
  public String getPostCode() {
    return postcode;
  }
}