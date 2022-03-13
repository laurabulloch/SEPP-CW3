/**
 * Class for handling actions and storing information regarding supermarkets
 */

package shield;

// imported class to check date at start of CHI
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SupermarketClient implemented
 */
public class SupermarketClientImp implements SupermarketClient {

  /**
   * Private variables created:
   *
   * The server endpoint in use
   * Whether the supermarket is registered
   * The name of the supermarket
   * The postcode of the supermarket
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
  public SupermarketClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Checks if supermarket registered and if not, registers them
   *
   * @param name the name of the supermarket
   * @param postCode the postcode of the supermarket
   * @return false if registering supermarket fails or true if supermarket is now/was previously registered
   */
  @Override
  public boolean registerSupermarket(String name, String postCode) {

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
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

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


  // **UPDATE2** ADDED METHOD

  /**
   * Records a particular order from a client to a supermarket
   *
   * @param CHI CHI number of the shielding individual associated with the order
   * @param orderNumber the order number
   * @return true if order has been recorded or false if not
   */
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {

    // ensure appropriate order number
    if (orderNumber <= 0) {
      System.out.println("Sorry order number must be greater than 0 please try again");
      return false;
    }
    assert orderNumber > 0;

    if (!isRegistered()) {
      System.out.println("Must be registered to record order");
      return false;
    }

    // ensure appropriate individual chi
    if ((CHI == null) || CHI.length() != 10) {
      System.out.println("Sorry CHI must be 10 digits please try again");
      return false;
    } else if (!isNumeric(CHI)) {
      System.out.println("Sorry CHI must be numerical please try again");
      return false;
    } else if (!startsWithDate(CHI)) {
      System.out.println("Sorry CHI must start with a valid date please try again");
      return false;
    }
    assert CHI != null;
    assert isNumeric(CHI);
    assert startsWithDate(CHI);

    // construct endpoint request
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber +
            "&supermarket_business_name=" + name + "&supermarket_postcode=" + postcode;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return true if order has been successfully recorded and false otherwise
      if (response.equalsIgnoreCase("True")) {
        System.out.println("Order has been successfully recorded");
        return true;
      } else {
        System.out.println("Order failed to be recorded");
        return false;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  // **UPDATE**
  /**
   * Updates the status of a particular order
   *
   * @param orderNumber the order number
   * @param status the order status - packed/dispatched/delivered
   * @return True if status was changed or False if not
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
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

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
   * Getter for supermarket registered status
   *
   * @return true if supermarket is registered and false otherwise
   */
  @Override
  public boolean isRegistered() {
    return registered;
  }

  /**
   * Getter for supermarket name
   *
   * @return the supermarket name
   */
  @Override
  public String getName() {
    return name;
  }


  /**
   * Getter for the supermarket postcode
   *
   * @return the supermarket postcode
   */
  @Override
  public String getPostCode() {
    return postcode;
  }

  /**
   * Helper for checking if a CHI is numeric
   *
   * @param chi the chi that is being checked
   * @return true if the chi is numeric and false otherwise
   */
  public Boolean isNumeric(String chi) {

    // check if CHI is able to be parsed
    try {
      Integer.parseInt(chi);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // NOTE: we wanted to check the chi date was before today's date but we could not work out how
  /**
   * Helper for checking if a CHI begins with a valid date
   *
   * @param chi the chi that is being checked
   * @return true if the chi starts with a date and false otherwise
   */
  public Boolean startsWithDate(String chi) {

    // select date from start of chi
    String chiDate = chi.substring(0,7);

    // check if date is able to be parsed
    try {
      new SimpleDateFormat("ddMMyy").parse(chiDate);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }

}