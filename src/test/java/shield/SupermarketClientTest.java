/**
 * Class for supermarket unit tests
 */

package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * tests for SupermarketClientImp
 */
// NOTE: text files must be reset before each running of tests

public class SupermarketClientTest {
  private final static String clientPropsFilename = "client.cfg";
  private String endpoint;

  private Properties clientProps;
  private SupermarketClient client;

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
    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
  }


  /**
   * test to register a supermarket
   */
  @Test
  public void testSupermarketNewRegistration() {

    String name = "Supermarket1";
    String postCode = "EH64TU";

    assertTrue(client.registerSupermarket(name, postCode));
  }

  /**
   * tests register a supermarket multiple times
   */
  @Test
  public void testSupermarketMultipleRegistration() {

    String name = "Supermarket2";
    String postCode = "EH64TU";

    client.registerSupermarket(name, postCode);
    assertTrue(client.registerSupermarket(name, postCode));
  }

  /**
   * test to record a supermarket order
   */
  @Test
  public void testRecordSupermarketOrder() {

    Random rand = new Random();
    int orderNumber = rand.nextInt(10000);
    String chi = "1111111234";
    String name = "Supermarket3";
    String postCode = "EH64TU";

    // register individual and supermarket and then attempt to record an order with them
    try {
      String register_individual = "/registerShieldingIndividual?CHI=" + chi;
      ClientIO.doGETRequest(endpoint + register_individual);
    }
    catch (Exception e){
      e.printStackTrace();
    }
    client.registerSupermarket(name, postCode);
    assertTrue(client.recordSupermarketOrder(chi, orderNumber));
  }

  /**
   * test to record a supermarket order when user not registered
   */
  @Test
  public void testRecordSupermarketOrderNonHTTP() {
    Random rand = new Random();
    int orderNumber = rand.nextInt(10000);
    String chi = "11112345";

    assertFalse(client.recordSupermarketOrder(chi, orderNumber));
  }

  /**
   * test to update an order from a supermarket that does not exist
   */
  @Test
  public void testUpdateSupermarketOrderStatus() {

    Random rand = new Random();
    int orderNumber = rand.nextInt(10000);
    String chi = "1111113456";
    String name = "Supermarket4";
    String postCode = "EH64TU";

    // register individual and supermarket and then record an order and attempt to update its status
    try {
      String register_individual = "/registerShieldingIndividual?CHI=" + chi;
      ClientIO.doGETRequest(endpoint + register_individual);
    }
    catch (Exception e){
      e.printStackTrace();
    }
    client.registerSupermarket(name, postCode);
    client.recordSupermarketOrder(chi, orderNumber);
    assertTrue(client.updateOrderStatus(orderNumber, "dispatched"));
  }

  /**
   * test to update an order from a supermarket when order not recorded
   */
  @Test
  public void testUpdateSupermarketOrderStatusNonHTTP() {

    Random rand = new Random();
    int orderNumber = rand.nextInt(10000);

    assertFalse(client.updateOrderStatus(orderNumber, "dispatched"));
  }

  /**
   * test to check a supermarket is not registered
   */
  @Test
  public void testIsNotRegistered(){ assertFalse(client.isRegistered()); }

  /**
   * test to check a supermarket is registered
   */
  @Test
  public void testIsRegistered(){

    String name = "Supermarket5";
    String postCode = "EH1234";

    client.registerSupermarket(name, postCode);
    assertTrue(client.isRegistered());
  }

  /**
   * test to check a supermarket get name function when no name set
   */
  @Test
  public void testGetNoName(){ assertNull(client.getName()); }

  /**
   * test to check a supermarket get name function when name set
   */
  @Test
  public void testGetName(){

    String name = "Supermarket6";
    String postCode = "EH1234";

    client.registerSupermarket(name, postCode);
    assertEquals(client.getName(), name);
  }

  /**
   * test to check a supermarket get postCode function when no postcode set
   */
  @Test
  public void testGetNoPostCode(){ assertNull(client.getPostCode()); }

  /**
   * test to check a supermarket get postcode function when postcode set
   */
  @Test
  public void testGetPostCode(){

    String name = "Supermarket7";
    String postCode = "EH1234";

    client.registerSupermarket(name, postCode);
    assertEquals(client.getPostCode(), postCode);
  }

}