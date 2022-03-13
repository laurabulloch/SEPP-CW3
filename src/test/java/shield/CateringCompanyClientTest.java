/**
 * Class for catering company unit tests
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * tests for CateringCompanyClientImp
 */
// NOTE: text files must be reset before each running of tests

public class CateringCompanyClientTest {
    private final static String clientPropsFilename = "client.cfg";
    private String endpoint;

    private Properties clientProps;
    private CateringCompanyClient client;

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
        client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    }


    /**
     * Tests situation where new company is registered
     */
    @Test
    public void testCateringCompanyNewRegistration() {

        String name = "Catering1";
        String postCode = "EH38HU";

        assertTrue(client.registerCateringCompany(name, postCode));
    }


    /**
     * Tests situation where company has already been registered
     */
    @Test
    public void testCateringCompanySecondRegistration() {

        String name = "Catering2";
        String postCode = "EH38HU";

        client.registerCateringCompany(name, postCode);
        assertTrue(client.registerCateringCompany(name, postCode));
    }
    /**
     * Tests updating status of an order that does not exist
     */
    @Test
    public void testUpdateOrderStatus() {

        String chi = "1111111234";
        String name = "Catering3";
        String postCode = "EH108XY";
        String status = "dispatched";
        String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";
        int orderID = 0;

        // register shielding individual and catering company, place order and then attempt to update its status
        client.registerCateringCompany(name, postCode);
        try {
            String register_individual = "/registerShieldingIndividual?CHI=" + chi;
            ClientIO.doGETRequest(endpoint + register_individual);
            String place_order = "/placeOrder?individual_id=" + chi + "&catering_business_name=" + name + "&catering_postcode=" + postCode;
            orderID = Integer.parseInt(ClientIO.doPOSTRequest(endpoint + place_order, contents));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        assertTrue(client.updateOrderStatus(orderID, status));
    }
    /**
     * Tests updating status of an order when no order exists
     */
    @Test
    public void testUpdateOrderStatusNonHTTP() {
        Random rand = new Random();
        int orderNumber = rand.nextInt(10000);
        String status = "dispatched";

        assertFalse(client.updateOrderStatus(orderNumber, status));
    }

    /**
     * Test for getter for name when name not set
     */
    @Test
    public void testGetNoName() {
        assertNull(client.getName());
    }

    /**
     * Test for getter for name when name is set
     */
    @Test
    public void testGetName() {
        String name = "Catering4";
        String postCode = "EH1234";

        client.registerCateringCompany(name, postCode);
        assertEquals(client.getName(), name);
    }

    /**
     * Test for getter for postcode when postcode not set
     */
    @Test
    public void testGetNoPostCode() {
        assertNull(client.getPostCode());
    }

    /**
     * Test for getter for postcode when postcode is set
     */
    @Test
    public void testGetPostCode() {

        String name = "Catering5";
        String postCode = "EH1234";

        client.registerCateringCompany(name, postCode);
        assertEquals(client.getPostCode(), postCode);
    }

    /**
     * Test for getter for if registered when not registered
     */
    @Test
    public void testNotRegistered() {
        assertFalse(client.isRegistered());
    }

    /**
     * Test for getter for postcode when postcode is set
     */
    @Test
    public void testRegistered() {

        String name = "Catering6";
        String postCode = "EH1234";

        client.registerCateringCompany(name, postCode);
        assertTrue(client.isRegistered());
    }
}