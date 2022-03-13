/**
 * Class for shielding individual unit tests
 */

package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * tests for ShieldingIndividualClientImp
 */
// NOTE: text files must be reset before each running of tests

public class ShieldingIndividualClientTest {
  private final static String clientPropsFilename = "client.cfg";
  private String endpoint;

  private Properties clientProps;
  private ShieldingIndividualClient client;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);

    endpoint = clientProps.getProperty("endpoint");
    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  /**
   * test to register new shielding individual
   */
  @Test
  public void testShieldingIndividualNewRegistration() {

    String chi = "1111111234";

    assertTrue(client.registerShieldingIndividual(chi));
  }

  /**
   * test register shielding individual multiple times
   */
  @Test
  public void testShieldingIndividualMultipleRegistration() {

    String chi = "1111112345";

    client.registerShieldingIndividual(chi);
    assertTrue(client.registerShieldingIndividual(chi));
  }

  /**
   * test to check food boxes are shown to shielding individual
   */
  @Test
  public void testShowFoodBoxes(){

    String chi = "1111113456";

    // register individual and then attempt to show food boxes
    client.registerShieldingIndividual(chi);
    assertEquals(client.showFoodBoxes("none").size(), 3);
  }

  // NOTE: we could not test this function more thoroughly as we could not work out how to test with scanners
  /**
   * test to check place order function reacts correctly when no individual registered
   */
  @Test
  public void testOrderPlaced(){
    assertFalse(client.placeOrder());
  }

  // NOTE: we could not test this function more thoroughly as we could not work out how to test with scanners
  /**
   * test to check edit order function reacts correctly when no individual registered
   */
  @Test
  public void testOrderEdited(){
    assertFalse(client.editOrder(3));
  }

  /**
   * test to check cancel order function reacts correctly without further input
   */
  @Test
  public void testOrderCancelled(){

    String chi = "1111114567";
    String name = "Catering1";
    String postCode = "EH56ED";
    String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";
    int orderID = 0;

    // register an individual and a catering company, place an order and then attempt cancel it
    client.registerShieldingIndividual(chi);
    try {
      String register_catering = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
      ClientIO.doGETRequest(endpoint + register_catering);
      String place_order = "/placeOrder?individual_id=" + chi + "&catering_business_name=" + name + "&catering_postcode=" + postCode;
      orderID = Integer.parseInt(ClientIO.doPOSTRequest(endpoint + place_order, contents));
    }
    catch (Exception e){
      e.printStackTrace();
    }
    // NOTE: we had to use a setter here as we could not work out how to use scanners in tests
    client.setOrder(orderID, client.setContents(1, "cucumbers", 20), chi,
            name, null, null, null, null, "packed");
    assertTrue(client.cancelOrder(orderID));
  }

  /**
   * test to check cancel order returns false when invalid order id given
   */
  @Test
  public void testOrderCancelledNonHTTP(){

    Random rand = new Random();
    int orderID = rand.nextInt(10000);

    assertFalse(client.cancelOrder(orderID));
  }

  /**
   * test to check a order status reacts correctly when no order matching order id provided
   */
  @Test
  public void testRequestOrderStatus(){

    String chi = "1111115678";
    String catering = "Catering2";
    String postcode = "EH108XY";
    int orderID = 0;
    String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";

    // register shielding individual and catering company, place an order and then attempt to retrieve its status
    client.registerShieldingIndividual(chi);
    try {
      String registerCatering = "/registerCateringCompany?business_name=" + catering + "&postcode=" + postcode;
      ClientIO.doGETRequest(endpoint + registerCatering);
      String place_order = "/placeOrder?individual_id=" + chi + "&catering_business_name=" + catering + "&catering_postcode=" + postcode;
      orderID = Integer.parseInt(ClientIO.doPOSTRequest(endpoint + place_order, contents));
    }
    catch (Exception e){
      e.printStackTrace();
    }
    // NOTE: we had to use a setter here as we could not work out how to use scanners in tests
    client.setOrder(orderID, client.setContents(1, "cucumbers", 20), chi,
            catering, null, null, null, null, "packed");
    assertTrue(client.requestOrderStatus(orderID));
  }

  /**
   * test to check edit order returns false when invalid order id given
   */
  @Test
  public void testRequestOrderStatusNonHTTP(){
    Random rand = new Random();
    int orderID = rand.nextInt(10000);

    assertFalse(client.requestOrderStatus(orderID));
  }

  /**
   * test to check catering companies getter when 2 catering companies on system
   */
  @Test
  public void testGetCateringCompanies(){

    // register 2 new catering companies and ensure list of size 2 returned
    try {
      String registerCatering1 = "/registerCateringCompany?business_name=Catering1&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering1);
      String registerCatering2 = "/registerCateringCompany?business_name=Catering2&postcode=EH108XY";
      ClientIO.doGETRequest(endpoint + registerCatering2);
      String registerCatering3 = "/registerCateringCompany?business_name=Catering3&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering3);
      String registerCatering4 = "/registerCateringCompany?business_name=Catering4&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering4);
      String registerCatering5 = "/registerCateringCompany?business_name=Catering5&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering5);
      String registerCatering6 = "/registerCateringCompany?business_name=Catering6&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering6);
      String registerCatering7 = "/registerCateringCompany?business_name=Catering7&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering7);
      String registerCatering8 = "/registerCateringCompany?business_name=Catering8&postcode=EH56ED";
      ClientIO.doGETRequest(endpoint + registerCatering8);
    }
    catch (Exception e){
      e.printStackTrace();
    }
    assertEquals(client.getCateringCompanies().size(), 8);
  }

  /**
   * test to check distance getter for 2 invalid postcodes
   */
  @Test
  public void testGetDistance(){

    assertEquals(client.getDistance("AB29LU","AB37FT"), -1);
  }

  /**
   * test to check is registered works correctly when individual not registered
   */
  @Test
  public void testIsNotRegistered(){ assertFalse(client.isRegistered()); }

  /**
   * test to check is registered works correctly when individual is registered
   */
  @Test
  public void testIsRegistered(){

    String chi = "1111116789";

    // register individual and check isRegistered is updated
    client.registerShieldingIndividual(chi);
    assertTrue(client.isRegistered());
  }

  /**
   * test to check get chi works correctly when no chi on system
   */
  @Test
  public void testGetNoCHI(){ assertNull(client.getCHI()); }

  /**
   * test to check get chi works correctly when chi on system
   */
  @Test
  public void testGetCHI(){

    String chi = "1111119876";

    // register individual and check chi value is updated
    client.registerShieldingIndividual(chi);
    assertEquals(client.getCHI(), chi);
  }

  /**
   * test to check foodBoxNumber works correctly when no food boxes on system
   */
  @Test
  public void testGetFoodBoxNumber(){ assertEquals(client.getFoodBoxNumber(), 0); }

  /**
   * test to check get dietary preference of food box that does not exist
   */
  @Test
  public void testGetDietaryPreference(){
    Random rand = new Random();
    int foodBoxID = rand.nextInt(10000);

    assertNull(client.getDietaryPreferenceForFoodBox(foodBoxID));
  }

  /**
   * test to check get items number for food box works correctly on food box that does not exist
   */
  @Test
  public void testItemsNumberForFoodBox(){
    Random rand = new Random();
    int foodBoxID = rand.nextInt(10000);

    assertEquals(client.getItemsNumberForFoodBox(foodBoxID), -1);
  }

  /**
   * test to check get item ids for food box works correctly on food box that does not exist
   */
  @Test
  public void testGetItemIdsForFoodBox(){
    Random rand = new Random();
    int foodBoxID = rand.nextInt(10000);

    assertNull(client.getItemIdsForFoodBox(foodBoxID));
  }

  /**
   * test to check get item names for food box works correctly on food box that does not exist
   */
  @Test
  public void testGetItemsNameForFoodBox(){
    Random rand = new Random();
    int itemID = rand.nextInt(10000);
    int foodBoxID = rand.nextInt(10000);

    assertNull(client.getItemNameForFoodBox(itemID, foodBoxID));
  }

  /**
   * test to check get item quantity for food box works correctly on food box that does not exist
   */
  @Test
  public void testGetItemQuantityForFoodBox(){
    Random rand = new Random();
    int itemID = rand.nextInt(10000);
    int foodBoxID = rand.nextInt(10000);

    assertEquals(client.getItemQuantityForFoodBox(itemID, foodBoxID), -1);
  }

  /**
   * test to check pick food box works correctly on food box that does not exist
   */
  @Test
  public void testPickFoodBox(){
    Random rand = new Random();
    int foodBoxID = rand.nextInt(10000);

    assertFalse(client.pickFoodBox(foodBoxID));
  }

  /**
   * test to check change item quantity for picked food box works correctly on item that does not exist
   */
  @Test
  public void testChangeItemQuantityForPickedFoodBox(){
    Random rand = new Random();
    int quantity = rand.nextInt(10000);
    int itemID = rand.nextInt(10000);

    assertFalse(client.changeItemQuantityForPickedFoodBox(itemID, quantity));
  }

  /**
   * test to check get order numbers works correctly when no orders on system
   */
  @Test
  public void testGetOrderNumbers(){ assertNull(client.getOrderNumbers()); }

  /**
   * test to check get status for order works correctly on order that does not exist
   */
  @Test
  public void testGetStatusForOrder(){
    Random rand = new Random();
    int orderNo = rand.nextInt(10000);

    assertNull(client.getStatusForOrder(orderNo));
  }

  /**
   * test to check get item ids for order works correctly on order that does not exist
   */
  @Test
  public void testGetItemIdsForOrder(){
    Random rand = new Random();
    int orderID = rand.nextInt(10000);

    assertNull(client.getItemIdsForOrder(orderID));
  }

  /**
   * test to check get item name for order works correctly on order and item that do not exist
   */
  @Test
  public void testGetItemNameForOrder(){
    Random rand = new Random();
    int itemID = rand.nextInt(10000);
    int orderID = rand.nextInt(10000);

    assertNull(client.getItemNameForOrder(itemID, orderID));
  }

  /**
   * test to check get item quantity for order works correctly on order and item that do not exist
   */
  @Test
  public void testGetItemQuantityForOrder(){
    Random rand = new Random();
    int orderNo = rand.nextInt(10000);
    int itemID = rand.nextInt(10000);

    assertEquals(client.getItemQuantityForOrder(itemID, orderNo), -1);
  }

  /**
   * test to check set item quantity for order works correctly on order and item that do not exist
   */
  @Test
  public void testSetItemQuantityForOrder(){
    Random rand = new Random();
    int orderNo = rand.nextInt(10000);
    int itemID = rand.nextInt(10000);
    int quantity = rand.nextInt(10000);

    assertFalse(client.setItemQuantityForOrder(itemID, orderNo, quantity));
  }

  /**
   * test to check get closest catering company works when no catering companies on system
   */
  @Test
  public void testGetClosestCateringCompany(){ assertNull(client.getClosestCateringCompany()); }

}