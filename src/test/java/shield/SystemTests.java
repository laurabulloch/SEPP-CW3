/**
 * Class for handling system tests
 */

package shield;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SystemTests {
    private final static String clientPropsFilename = "client.cfg";
    private String endpoint;

    private Properties clientProps;
    private SupermarketClient supermarketClient;
    private ShieldingIndividualClient shieldingClient;
    private CateringCompanyClient cateringClient;

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

    // NOTE: text files must be reset before the running of each test

    @BeforeEach
    public void setup() {
        clientProps = loadProperties(clientPropsFilename);

        supermarketClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
        shieldingClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        cateringClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
        endpoint = clientProps.getProperty("endpoint");
    }

    /**
     * A test for register supermarket use case - main success scenario
     */
    @Test
    public void testSupermarketNewRegistration() {

        String name = "Supermarket1";
        String postCode = "EH64TU";

        // register new supermarket and ensure has been successful by checking local variables have been set
        assertTrue(supermarketClient.registerSupermarket(name, postCode));
        assertEquals(name, supermarketClient.getName());
        assertEquals(postCode, supermarketClient.getPostCode());
        assertTrue(supermarketClient.isRegistered());
    }

    /**
     * A test for register supermarket use case - scenario where supermarket is already registered
     */
    @Test
    public void testSupermarketReRegistration() {

        String name = "Supermarket2";
        String postCode = "EH64TU";

        // register supermarket and then register it again ensuring no errors are thrown and variables are still set
        supermarketClient.registerSupermarket(name, postCode);
        assertTrue(supermarketClient.registerSupermarket(name, postCode));
        assertEquals(name, supermarketClient.getName());
        assertEquals(postCode, supermarketClient.getPostCode());
        assertTrue(supermarketClient.isRegistered());
    }

    /**
     * A test for register shielding individual use case - main success scenario
     */
    @Test
    public void testShieldingIndividualNewRegistration() {

        String chi = "1111111234";

        // register new individual and ensure has been successful by checking local variables have been set
        assertTrue(shieldingClient.registerShieldingIndividual(chi));
        assertEquals(chi, shieldingClient.getCHI());
        assertTrue(shieldingClient.isRegistered());
    }

    /**
     * A test for register shielding individual use case - where individual is already registered
     */
    @Test
    public void testShieldingIndividualReRegistration() {

        String chi = "1111112345";

        // register individual and then register them again ensuring no errors are thrown and variables are still set
        shieldingClient.registerShieldingIndividual(chi);
        assertTrue(shieldingClient.registerShieldingIndividual(chi));
        assertEquals(chi, shieldingClient.getCHI());
        assertTrue(shieldingClient.isRegistered());
    }

    /**
     * A test register for register catering company use case - main success scenario
     */
    @Test
    public void testCateringCompanyNewRegistration() {

        String name = "Catering1";
        String postCode = "EH56ED";

        // register new catering company and ensure has been successful by checking local variables have been set
        assertTrue(cateringClient.registerCateringCompany(name, postCode));
        assertEquals(name, cateringClient.getName());
        assertEquals(postCode, cateringClient.getPostCode());
        assertTrue(cateringClient.isRegistered());
    }

    /**
     * A test for register catering company use case - where catering company is already registered
     */
    @Test
    public void testCateringCompanyReRegistration() {

        String name = "Catering2";
        String postCode = "EH56ED";

        // register catering company and then register it again ensuring no errors are thrown and variables are still set
        cateringClient.registerCateringCompany(name, postCode);
        assertTrue(cateringClient.registerCateringCompany(name, postCode));
        assertEquals(name, cateringClient.getName());
        assertEquals(postCode, cateringClient.getPostCode());
        assertTrue(cateringClient.isRegistered());
    }


    /**
     * A test for place food box order use case with catering company - main success scenario
     */
    @Test
    public void testPlaceOrderWithCatering() {

        // NOTE: sadly we were unable to implement this test as we could not work out how to set scanners in a test

//        String diet = "none";
//        String editOrder = "no";
//        int boxID = 1;
//        String name = "Catering3";
//        String postCode = "EH56ED";
//        String chi = "1111113456";
//
//        try {
//            String register_catering = "/registerCateringCompany?business_name=" + name +"&postcode=" + postCode;
//            String response_catering = ClientIO.doGETRequest(endpoint + register_catering);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        shieldingClient.registerShieldingIndividual(chi);
//        assertTrue(shieldingClient.placeOrder());
    }

    /**
     * A test for place food box order use case with catering company - scenario where user edits box
     */
    @Test
    public void testPlaceOrderWithCateringEdit() {

        // NOTE: sadly we were unable to implement this test as we could not work out how to set scanners in a test

//        String diet = "none";
//        String editOrder = "no";
//        int boxID = 1;
//        String name = "Catering4";
//        String postCode = "EH56ED";
//        String chi = "1111114567";
//
//        try {
//            String register_catering = "/registerCateringCompany?business_name=" + name +"&postcode=" + postCode;
//            String response_catering = ClientIO.doGETRequest(endpoint + register_catering);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        shieldingClient.registerShieldingIndividual(chi);
//        assertTrue(shieldingClient.placeOrder());
    }

    /**
     * A test for place food box order use case with supermarket - main success scenario
     */
    @Test
    public void testRecordOrderWithSupermarket() {

        Random rand = new Random();
        String chi = "0602119802";
        int orderNumber = rand.nextInt(10000);
        String postcode = "EH82OQ";
        String supermarket = "Supermarket3";

        // register individual and supermarket and then attempt to record an order with them
        try {
            String register_individual = "/registerShieldingIndividual?CHI=" + chi;
            ClientIO.doGETRequest(endpoint + register_individual);
        } catch (Exception e){
            e.printStackTrace();
        }
        supermarketClient.registerSupermarket(supermarket, postcode);
        assertTrue(supermarketClient.recordSupermarketOrder(chi, orderNumber));
    }

    /**
     * A test for edit food box order use case with catering company - main success scenario
     */
    @Test
    public void testEditOrderCatering() {

        // NOTE: sadly we were unable to implement this test as we could not work out how to set scanners in a test

//        String orderNoString = "";
//        String chi = "0602119802";
//        String catering = "Catering5";
//        String postcode = "EH108XY";
//        int orderID = 0;
//        String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";
//
//        shieldingClient.registerShieldingIndividual(chi);
//        try {
//            String request_catComp = "/registerCateringCompany?business_name=" + catering + "&postcode=" + postcode;
//            String response_catComp = ClientIO.doGETRequest(endpoint + request_catComp);
//            String place_order = "/placeOrder?individual_id=" + chi + "&catering_business_name=" + catering + "&catering_postcode=" + postcode;
//            orderID = Integer.parseInt(ClientIO.doPOSTRequest(endpoint + place_order, contents));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        shieldingClient.setOrder(orderID, shieldingClient.setContents(1, "cucumbers", 20), chi,
//                catering, null, null, null, null, "packed");
//        assertTrue(shieldingClient.editOrder(orderID));
    }

    /**
     * A test for cancel food box order use case with supermarket - main success scenario
     */
    @Test
    public void testCancelOrderSupermarket() {

        Random rand = new Random();
        String chi = "0602119802";
        int orderNumber = rand.nextInt(10000);
        String postcode = "EH82OQ";
        String supermarket = "Supermarket4";

        // register individual and supermarket and then attempt to record and then cancel an order with them
        try {
            String register_individual = "/registerShieldingIndividual?CHI" + chi;
            ClientIO.doGETRequest(endpoint + register_individual);
        } catch (Exception e){
            e.printStackTrace();
        }
        supermarketClient.registerSupermarket(supermarket, postcode);
        supermarketClient.recordSupermarketOrder(chi, orderNumber);
        supermarketClient.updateOrderStatus(orderNumber, "cancelled");
    }

    /**
     * A test for cancel food box order use case with catering company - main success scenario
     */
    @Test
    public void testCancelOrderCatering() {

        int orderID = 0;
        String chi = "1111115678";
        String catering = "Catering6";
        String postcode = "EH56ED";
        String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";

        // register an individual and a catering company, place an order and then attempt cancel it
        shieldingClient.registerShieldingIndividual(chi);
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
        shieldingClient.setOrder(orderID, shieldingClient.setContents(1, "cucumbers", 20), chi,
                catering, null, null, null, null, "packed");
        assertTrue(shieldingClient.cancelOrder(orderID));
    }

    /**
     * A test for get food box order status use case with catering company - main success scenario
     */
    @Test
    public void testRequestOrderStatusCatering() {

        String chi = "1111116789";
        String catering = "Catering7";
        String postcode = "EH108XY";
        int orderID = 0;
        String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";

        // register shielding individual and catering company, place an order and then attempt to retrieve its status
        shieldingClient.registerShieldingIndividual(chi);
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
        shieldingClient.setOrder(orderID, shieldingClient.setContents(1, "cucumbers", 20), chi,
                catering, null, null, null, null, "packed");
        assertTrue(shieldingClient.requestOrderStatus(orderID));
    }

    /**
     * A test for update order status use case for catering company - main success scenario
     */
    @Test
    public void testUpdateOrderStatusCatering() {

        String chi = "1111119876";
        String catering = "Catering8";
        String postcode = "EH108XY";
        String status = "dispatched";
        String contents = "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20}]}";
        int orderID = 0;

        // register shielding individual and catering company, place order and then attempt to update its status
        shieldingClient.registerShieldingIndividual(chi);
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
        shieldingClient.setOrder(orderID, shieldingClient.setContents(1, "cucumbers", 20), chi,
                catering, null, null, null, null, "packed");
        assertTrue(cateringClient.updateOrderStatus(orderID, status));
    }

    /**
     * A test for update order status use case for supermarket - main success scenario
     */
    @Test
    public void testUpdateOrderStatusSupermarket() {
        Random rand = new Random();
        String chi = "1111118765";
        String supermarket = "Supermarket5";
        String postcode = "EH52XY";
        String status = "dispatched";
        int orderID = rand.nextInt(10000);

        // register individual and supermarket, record order and then attempt to update its status
        try {
            String register_individual = "/registerShieldingIndividual?CHI=" + chi;
            ClientIO.doGETRequest(endpoint + register_individual);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        supermarketClient.registerSupermarket(supermarket, postcode);
        supermarketClient.recordSupermarketOrder(chi, orderID);
        assertTrue(supermarketClient.updateOrderStatus(orderID, status));
    }
}
