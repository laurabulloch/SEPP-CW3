/**
 *
 */

package shield;

public class SupermarketClientImp implements SupermarketClient {

  /**
   * Private variables created:
   * the server endpoint in use
   * whether the supermarket is registered
   * the name of the supermarket
   * the postcode of the catering company
   */
  private String endpoint;
  private boolean registered;
  private String name;
  private String postcode;
  public SupermarketClientImp(String endpoint) { this.endpoint = endpoint; }

  /**
   * Checks if supermarket registered and if not registers them
   * @param name the name of the supermarket
   * @param postCode the postcode of the supermarket
   * @return False if company unable to be registered or True if supermarket was previously registered or is now registered
   */
  @Override
  public boolean registerSupermarket(String name, String postCode) {

    // check if supermarket registered
    if (registered = true) {
      return true;
    }

    // construct endpoint request
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return True if supermarket already registered or now newly registered or return error
      if (response.equals("registered new") || response.equals("already registered")){
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


  // **UPDATE2** ADDED METHOD
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {

    // construct endpoint request
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postcode;
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return True if order recorded or False if it was unsuccessful
      if (response.equals("True")) {
        return true;
      } else {
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
   * @param orderNumber the order number
   * @param status the order status - packed/dispatched/delivered
   * @return True if status was changed or False if the operation was unsuccessful
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {

    // construct endpoint request
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // return True if status changed or False if it was unsuccessful
      if (response.equals("True")) {
        return true;
      } else {
        return false;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Getter for supermarket registered status
   * @return True if supermarket was/is now registered and False otherwise
   */
  @Override
  public boolean isRegistered() {
    return registered;
  }

  /**
   * Getter for supermarket name
   * @return the supermarket name
   */
  @Override
  public String getName() {
    return name;
  }


  /**
   * Getter for the supermarket postcode
   * @return the supermarket postcode
   */
  @Override
  public String getPostCode() {
    return postcode;
  }
}