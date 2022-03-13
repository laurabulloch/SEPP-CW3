/**
 * Class for handling actions and storing information regarding shielding individuals
 */
package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.LocalDateTime;

/**
 * ShieldingIndividualClient implemented
 */
public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

    /**
     * Create object for storing food boxes
     * (food box contents, who delivers them, food box dietary requirement, food box id, and food box name)
     */
    static final class MessagingFoodBox {
        ArrayList<MessagingContents> contents;
        String delivered_by;
        String diet;
        int id;
        String name;
    }

    /**
     * Create object for storing contents - items in food box or order
     * (item's ID, item's name, and item's quantity)
     */
    static final class MessagingContents {
        int id;
        String name;
        int quantity;
    }

    /**
     * Create object for storing order information
     * (order ID, contents of order, CHI of individual who placed order, ID of catering company who deliver order,
     * the date it was ordered, the date it was packed, the date it was dispatched, the date it was delivered, and
     * its current status)
     */
    static final class MessagingOrders {
        int id;
        ArrayList<MessagingContents> contents;
        String chi;
        String cateringId;
        LocalDateTime ordered;
        LocalDateTime packed;
        LocalDateTime dispatched;
        LocalDateTime delivered;
        String status;
    }

    /**
     * Create class for storing catering companies information
     * (catering company name and catering company postcode)
     */
    static final class MessagingCateringCompanies {
        String name;
        String postCode;
    }

    /**
     * Create class for storing shielding individuals information
     * (individual's postcode, individual's name, individual's surname and individual's phone number)
     */
    static final class MessagingIndividual {
        String postCode;
        String name;
        String surname;
        String phoneNumber;
    }

    /**
     * Private variables created:
     *
     * The server endpoint in use
     * Whether the individual is registered
     * The CHI of the individual
     * The individual's personal information
     * The ID of food box selected by the individual
     * The list of food boxes, and their details, available with specific dietary requirement
     * The list of available catering companies and their details
     * The list of the individual's orders and the order's details
     */
    private String endpoint;
    private boolean registered;
    private String CHI;
    private MessagingIndividual individualInformation = new MessagingIndividual();
    private int selectedFoodBoxId;
    private ArrayList<MessagingFoodBox> foodBoxOptions =  new ArrayList<>();
    private ArrayList<MessagingCateringCompanies> cateringCompanies =  new ArrayList<>();
    private ArrayList<MessagingOrders> orders =  new ArrayList<>();

    /**
     * Class constructor
     *
     * @param endpoint the server endpoint to be used
     */
    public ShieldingIndividualClientImp(String endpoint) { this.endpoint = endpoint; }

    /**
     * Checks if individual is registered and if not, registers them
     *
     * @param CHI CHI number of the shielding individual
     * @return false if registering individual fails or true if individual is now/was previously registered
     */
    @Override
    public boolean registerShieldingIndividual(String CHI) {

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
        String request = "/registerShieldingIndividual?CHI=" + CHI;

        try {
            //perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            // return true and store information locally if newly registered individual
            if (!response.equalsIgnoreCase("already registered")) {
                if (correctNewRegisterFormat(response)) {
                    response = response.replace("[", "");
                    response = response.replace("]", "");
                    String[] information = response.split(",");
                    individualInformation.postCode = information[0];
                    individualInformation.name = information[1];
                    individualInformation.surname = information[2];
                    individualInformation.phoneNumber = information[3];
                    this.CHI = CHI;
                    registered = true;
                    System.out.println("New registration successful");
                    return true;
                } else {
                    return false;
                }
            }

            // return true if response is "already registered"
            assert response.equalsIgnoreCase("already registered");
            System.out.println(response);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Lists all food boxes available for dietary requirement provided
     *
     * @param dietaryPreference the dietary preference
     * @return list of ids of all food boxes matching dietary requirement
     */
    @Override
    public Collection<String> showFoodBoxes(String dietaryPreference) {

        // ensure appropriate dietary preference
        if (!dietaryPreference.equals("none") && !dietaryPreference.equals("pollotarian") &&
                !dietaryPreference.equals("vegan")) {
            System.out.println("Sorry dietary preference must be none, pollotarian, or vegan, please try again");
            return null;
        }
        assert (dietaryPreference.equals("none") || dietaryPreference.equals("pollotarian") ||
                dietaryPreference.equals("vegan"));

        // construct endpoint request
        String request =  "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;

        // setup response recipient
        List<MessagingFoodBox> responseBoxes;

        // create list to store ids to be returned
        ArrayList<String> boxIds = new ArrayList<>();

        try {
            // perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            // unmarshal response
            Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
            responseBoxes = new Gson().fromJson(response, listType);

            // gather required fields
            for (MessagingFoodBox responseBox : responseBoxes) {
                boxIds.add(Integer.toString(responseBox.id));
                foodBoxOptions.add(responseBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return boxIds;
    }

    // **UPDATE2** REMOVED PARAMETER

    /**
     * Places order based on shielding individuals preferences
     *
     * @return true if the order has been placed and false if it has not
     */
    @Override
    public boolean placeOrder() {

        // check individual registered
        if (!isRegistered()) {
            System.out.println("You must be registered to place an order");
            return false;
        }

        // user asked to select dietary preference
        String dietaryPreference = "";
        while (!(dietaryPreference.equals("none") | dietaryPreference.equals("pollotarian") |
                dietaryPreference.equals("vegan"))) {
            Scanner selectDietaryPreference = new Scanner(System.in);
            System.out.println("Please input your dietary preference from the options 'none', 'pollotarian' or 'vegan'");
            dietaryPreference = selectDietaryPreference.nextLine();
        }

        // find if any food boxes available for users dietary requirements and select one if available
        showFoodBoxes(dietaryPreference);
        if (foodBoxOptions == null) {
            System.out.println("Sorry no food boxes available for your dietary preference");
            return false;
        } else if (getFoodBoxNumber() == 1) {
            pickFoodBox(foodBoxOptions.get(0).id);
        } else {
            System.out.println("Multiple food boxes are available for your dietary preference, please input one of " +
                    "the food box IDs from those displayed below to select that food box");
            ArrayList<Integer> boxIds = new ArrayList<>();
            for (MessagingFoodBox foodBox : foodBoxOptions) {
                boxIds.add(foodBox.id);
                System.out.println(foodBox);
            }
            while (!(boxIds.contains(selectedFoodBoxId))) {
                Scanner selectFoodBox = new Scanner(System.in);
                System.out.println("Please input your chosen food box id");
                pickFoodBox(Integer.parseInt(selectFoodBox.nextLine()));
            }
        }

        // display chosen food box contents and ask user if they would like to edit it
        System.out.println("The items in your food box and their quantities:");
        List<Integer> itemIds = (List<Integer>) getItemIdsForFoodBox(selectedFoodBoxId);
        for (int i=0; i<getItemsNumberForFoodBox(selectedFoodBoxId); i++) {
            System.out.println("Name: " + getItemNameForFoodBox(itemIds.get(i), selectedFoodBoxId));
            System.out.println("Quantity: " + getItemQuantityForFoodBox(itemIds.get(i), selectedFoodBoxId));
            System.out.println();
        }
        String editFoodBox = "";
        while (!(editFoodBox.equals("yes") | editFoodBox.equals("no"))) {
            Scanner edit = new Scanner(System.in);
            System.out.println("Would you like to decrease the quantity of any of the items? Please respond 'yes' or 'no'");
            editFoodBox = edit.nextLine();
        }

        // edit order if required - print each item and ask user if they want to edit its quantity
        if (editFoodBox.equals("yes")) {
            for (int i=0; i<getItemsNumberForFoodBox(selectedFoodBoxId); i++) {
                System.out.println("Name: " + getItemNameForFoodBox(itemIds.get(i), selectedFoodBoxId));
                System.out.println("Quantity: " + getItemQuantityForFoodBox(itemIds.get(i), selectedFoodBoxId));
                int quantity = 0;
                while (!(quantity>0 & quantity<=getItemQuantityForFoodBox(itemIds.get(i), selectedFoodBoxId))) {
                    Scanner changeQuantity = new Scanner(System.in);
                    System.out.println("Enter new quantity if you wish to change decrease the items quantity or " +
                            "input the current quantity if you don't wish to edit the items quantity");
                    quantity = Integer.parseInt(changeQuantity.nextLine());
                }
                changeItemQuantityForPickedFoodBox(itemIds.get(i), quantity);
            }
        }

        // choose catering company to order with
        MessagingCateringCompanies cateringChosen = cateringCompanies.get(0);
        for (MessagingCateringCompanies catering : cateringCompanies) {
            if (getClosestCateringCompany().equals(catering.name)) {
                cateringChosen = catering;
            }
        }

        // construct endpoint request
        String request = "placeOrder?individual id=" + getCHI() + "&catering_business_name=" + cateringChosen.name +
                "&catering_postcode=" + cateringChosen.postCode;

        // create list of contents to add to order
        ArrayList<MessagingContents> selectedContents = new ArrayList<>();
        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == selectedFoodBoxId) {
                selectedContents = foodBox.contents;
            }
        }

        // put contents into string format for request
        String selectedContentsString = "{\"contents\":" + selectedContents + "}";

        try {
            //perform request and store order id returned
            String response = ClientIO.doPOSTRequest(endpoint + request, selectedContentsString);

            // create order from food box chosen
            MessagingOrders order = new MessagingOrders();
            order.cateringId = cateringChosen.name;
            order.chi = getCHI();
            order.contents = selectedContents;
            order.ordered = LocalDateTime.now();
            order.delivered = null;
            order.dispatched = null;
            order.packed = null;
            order.status = "ordered";

            orders.add(order);

            // tell user order id of order placed
            System.out.println("Order has been placed with an order number of: " + Integer.parseInt(response));
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Allows user to edit items in the contents of an order
     *
     * @param orderNumber the order number
     * @return true if the order has been edited and false if not
     */
    @Override
    public boolean editOrder(int orderNumber) {

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return false;
        }
        assert orderNumber > 0;

        // check individual registered
        if (!isRegistered()) {
            System.out.println("You must be registered to edit an order");
            return false;
        }

        // ensure order exists locally
        if (getOrderNumbers().contains(orderNumber)) {

            // ensure order is not packed
            if (getStatusForOrder(orderNumber).equals("none")) {

                // display and allow editing of all items
                List<Integer> itemIds = (List<Integer>) getItemIdsForOrder(orderNumber);
                for (int itemId : itemIds) {
                    int quantity = 0;
                    while (!(quantity > 0 & quantity <= getItemQuantityForOrder(itemId, orderNumber))) {
                        Scanner changeQuantity = new Scanner(System.in);
                        System.out.println("Item name: " + getItemNameForOrder(itemId, orderNumber));
                        System.out.println("Item quantity: " + getItemQuantityForOrder(itemId, orderNumber));
                        System.out.println("If you would like to edit the quantity of this item please input a new decreased " +
                                "quantity, if you would like the quantity to remain the same please input the item's current quantity");
                        quantity = Integer.parseInt(changeQuantity.nextLine());
                    }
                    setItemQuantityForOrder(itemId, orderNumber, quantity);
                }

                // construct endpoint request
                String request = "/editOrder?order_id=" + orderNumber;

                // create list of order contents
                ArrayList<MessagingContents> selectedContents = new ArrayList<>();
                for (MessagingOrders order : orders) {
                    if (order.id == orderNumber) {
                        selectedContents = order.contents;
                    }
                }

                // put contents into string format for request
                String selectedContentsString = "{\"contents\":" + selectedContents + "}";

                try {
                    //perform request
                    String response = ClientIO.doPOSTRequest(endpoint + request, selectedContentsString);

                    // if successfully edited return true and otherwise return false
                    if (response.equalsIgnoreCase("true")) {
                        System.out.println("Order " + orderNumber + " successfully edited");
                        return true;
                    } else {
                        System.out.println(response);
                        return false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Sorry your order has already been packed and therefore cannot be edited");
                return false;
            }
        } else {
            System.out.println("Sorry your order number does not match an order number of any of your placed orders");
            return false;
        }
        return false;
    }

    /**
     * Allows user to cancel order
     *
     * @param orderNumber the order number
     * @return true if order has been cancelled and false if not
     */
    @Override
    public boolean cancelOrder(int orderNumber) {

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return false;
        }
        assert orderNumber > 0;

        // check individual registered
        if (!isRegistered()) {
            System.out.println("You must be registered to cancel an order");
            return false;
        }

        // ensure order exists and is stored locally
        if (getOrderNumbers().contains(orderNumber)) {
            // ensure order is not dispatched
            if (getStatusForOrder(orderNumber).equals("none") | getStatusForOrder(orderNumber).equals("packed")) {
                // construct endpoint request
                String request = "/cancelOrder?order_id=" + orderNumber;

                try{
                    // perform request
                    String response = ClientIO.doGETRequest(endpoint + request);

                    // if order successfully cancelled return true and otherwise return false
                    if (response.equalsIgnoreCase("True")) {
                        System.out.println("Order " + orderNumber + " successfully cancelled");
                        return true;
                    } else {
                        System.out.println(response);
                        return false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            // return true if order already cancelled
            } else if (getStatusForOrder(orderNumber).equals("cancelled")) {
                System.out.println("Order has already been cancelled");
                return true;
            } else {
                System.out.println("Sorry your order has already been dispatched and therefore cannot be edited");
                return false;
            }
        } else {
            System.out.println("Sorry your order number does not match an order number of any of your placed orders");
            return false;
        }
        return false;
    }

    /**
     * Requests order status, prints status and stores it locally
     *
     * @param orderNumber the order number
     * @return true if order status has been updated locally and false if not
     */
    @Override
    public boolean requestOrderStatus(int orderNumber) {

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return false;
        }
        assert orderNumber > 0;

        // construct endpoint request
        String request = "/requestStatus?order_id=" + orderNumber;
        int orderPlace = -1;

        // find information stored for order number provided
        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                orderPlace = orders.indexOf(order);
            }
        }

        // if order information not stored return false
        if (orderPlace == -1) {
            System.out.println("Sorry your order number does not match an order number of any of your placed orders");
            return false;
        }

        try {
            //perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            // return true if valid number returned and false otherwise
            switch (response) {
                case "0":
                    orders.get(orderPlace).status = "none";
                    //print order status
                    System.out.println("Order status of order number " + orderNumber + " is: none");
                    return true;
                case "1":
                     orders.get(orderPlace).status = "packed";
                    //print order status
                    System.out.println("Order status of order number " + orderNumber + " is: packed");
                     return true;
                case "2":
                    orders.get(orderPlace).status = "dispatched";
                    //print order status
                    System.out.println("Order status of order number " + orderNumber + " is: dispatched");
                    return true;
                case "3":
                    orders.get(orderPlace).status = "delivered";
                    //print order status
                    System.out.println("Order status of order number " + orderNumber + " is: delivered");
                    return true;
                case "4":
                    orders.get(orderPlace).status = "cancelled";
                    //print order status
                    System.out.println("Order status of order number " + orderNumber + " is: cancelled");
                    return true;
                case "-1":
                    System.out.println("Order not found");
                    return false;
                default:
                    System.out.println(response);
                    return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // **UPDATE**

    /**
     * Creates a list of all catering company names
     * (as well as stores their information locally)
     *
     * @return list of all catering companies names
     */
    @Override
    public Collection<String> getCateringCompanies() {

        // construct endpoint request
        String request =  "/getCaterers";

        ArrayList<String> companyNames = new ArrayList<>();

        try {
            // perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            if (response.equals("[]")) {
                System.out.println("No companies on system");
                return null;
            }

            // unmarshal response
            response = response.replace("[", "");
            response = response.replace("]", "");
            String[] companies = response.split("\"");

            if (companies.length == 0) {
                System.out.println("No catering companies on system");
                return null;
            }

            int x = 0;
            for (String company : companies) {

                x +=1 ;
                if (x % 2 == 0) {
                    String[] companyInformation = company.split(",");

                    MessagingCateringCompanies current = new MessagingCateringCompanies();
                    current.name = companyInformation[1];
                    current.postCode = companyInformation[1];

                    companyNames.add(current.name);
                    cateringCompanies.add(current);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return companyNames;
    }

    // **UPDATE**
    /**
     * Calculates distance between two postcodes
     *
     * @param postCode1 post code of one location
     * @param postCode2 post code of another location
     * @return distance between provided postcodes or -1 if action failed
     */
    @Override
    public float getDistance(String postCode1, String postCode2) {

        // ensure postcodes are not null
        if (postCode1 == null || postCode2 == null) {
            System.out.println("I'm sorry one of your postcodes is null please try again");
            return -1;
        }
        assert postCode1 != null;
        assert postCode2 != null;

        // ensure postCode1 starts with eh
        if (!postCode1.substring(0, 2).equalsIgnoreCase("eh")) {
            System.out.println("I'm sorry your first postcode is invalid please try again");
            return -1;
        }
        assert postCode1.substring(0,2).equalsIgnoreCase("eh");

        // ensure postCode1 starts is 6 or 7 digits long
        if (postCode1.length() != 6 && postCode1.length() != 7) {
            System.out.println("I'm sorry your first postcode is invalid please try again");
            return -1;
        }
        assert (postCode1.length() == 6 || postCode1.length() == 7);

        // ensure postCode2 starts with eh and is 6 or 7 digits long
        if (!postCode2.substring(0, 2).equalsIgnoreCase("eh")) {
            if (postCode2.length() != 6 && postCode2.length() != 7) {
                System.out.println("I'm sorry your second postcode is invalid please try again");
                return -1;
            }
        }
        assert postCode2.substring(0,2).equalsIgnoreCase("eh");
        assert (postCode2.length() == 6 || postCode2.length() == 7);

        // construct endpoint request
        String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2;

        try {
            //perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            // return float if provided and -1 otherwise
            return Float.parseFloat(response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Returns if the individual using the client is registered with the server
     *
     * @return true if the individual using the client is registered with the server
     */
    @Override
    public boolean isRegistered() { return registered; }

    /**
     * Returns the CHI number of the shielding individual
     *
     * @return CHI number of the shielding individual
     */
    @Override
    public String getCHI() { return CHI; }

    /**
     * Returns the number of available food boxes after querying the server
     *
     * @return number of available food boxes after querying the server
     */
    // I think this may be wrong
    @Override
    public int getFoodBoxNumber() { return foodBoxOptions.size(); }

    /**
     * Returns the dietary preference that this specific food box satisfies
     *
     * @param  foodBoxId the food box id as last returned from the server
     * @return dietary preference
     */
    @Override
    public String getDietaryPreferenceForFoodBox(int foodBoxId) {

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return null;
        }
        assert foodBoxId > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == foodBoxId) {
                return foodBox.diet;
            }
        }
        return null;
    }

    /**
     * Returns the number of items in this specific food box.
     *
     * @param  foodBoxId the food box id as last returned from the server
     * @return number of items in the food box
     */
    @Override
    public int getItemsNumberForFoodBox(int foodBoxId) {

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return -1;
        }
        assert foodBoxId > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == foodBoxId) {
                return foodBox.contents.size();
            }
        }
        return -1;
    }

    /**
     * Returns the collection of item ids of the requested food box
     *
     * @param  foodBoxId the food box id as last returned from the server
     * @return collection of item ids of the requested food box
     */
    @Override
    public Collection<Integer> getItemIdsForFoodBox(int foodBoxId) {

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return null;
        }
        assert foodBoxId > 0;

        ArrayList<Integer> itemIds = new ArrayList<>();
        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == foodBoxId) {
                for (MessagingContents content : foodBox.contents) {
                    itemIds.add(content.id);
                }
                return itemIds;
            }
        }
        return null;
    }

    /**
     * Returns the item name of the item in the requested food box
     *
     * @param  itemId the food box id as last returned from the server
     * @param  foodBoxId the food box id as last returned from the server
     * @return the requested item name
     */
    @Override
    public String getItemNameForFoodBox(int itemId, int foodBoxId) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return null;
        }
        assert itemId > 0;

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return null;
        }
        assert foodBoxId > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == selectedFoodBoxId) {
                for (MessagingContents content : foodBox.contents) {
                    if (content.id == itemId) {
                        return content.name;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the item quantity of the item in the requested food box
     *
     * @param  itemId the food box id as last returned from the server
     * @param  foodBoxId the food box id as last returned from the server
     * @return the requested item quantity
     */
    @Override
    public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return -1;
        }
        assert itemId > 0;

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return -1;
        }
        assert foodBoxId > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == selectedFoodBoxId) {
                for (MessagingContents content : foodBox.contents) {
                    if (content.id == itemId) {
                        return content.quantity;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns true if the requested food box was picked.
     *
     * @param  foodBoxId the food box id as last returned from the server
     * @return true if the requested food box was picked
     */
    @Override
    public boolean pickFoodBox(int foodBoxId) {

        // ensure appropriate food box id
        if (foodBoxId <= 0) {
            System.out.println("Sorry food box id must be greater than 0 please try again");
            return false;
        }
        assert foodBoxId > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == foodBoxId) {
                selectedFoodBoxId = foodBoxId;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the item quantity for the picked food box was changed
     *
     * @param  itemId the food box id as last returned from the server
     * @param  quantity the food box item quantity to be set
     * @return true if the item quantity for the picked food box was changed
     */
    @Override
    public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return false;
        }
        assert itemId > 0;

        // ensure appropriate quantity
        if (quantity <= 0) {
            System.out.println("Sorry quantity must be greater than 0 please try again");
            return false;
        }
        assert quantity > 0;

        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == selectedFoodBoxId) {
                for (MessagingContents content : foodBox.contents) {
                    if (content.id == itemId) {

                        // ensure new quantity is less than existing quantity
                        if (content.quantity <= quantity) {
                            System.out.println("Sorry new quantity must be less than existing quantity");
                            return false;
                        }
                        assert quantity < content.quantity;
                        content.quantity = quantity;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the collection of the order numbers placed.
     *
     * @return collection of the order numbers placed
     */
    @Override
    public Collection<Integer> getOrderNumbers() {

        if (orders.size() == 0) {
            return null;
        }

        ArrayList<Integer> orderNumbers = new ArrayList<>();
        for (MessagingOrders order : orders) {
            orderNumbers.add(order.id);
        }
        return orderNumbers;
    }

    /**
     * Returns the status of the order for the requested number.
     *
     * @param orderNumber the order number
     * @return status of the order for the requested number
     */
    @Override
    public String getStatusForOrder(int orderNumber) {

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return null;
        }
        assert orderNumber > 0;

        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                return order.status;
            }
        }
        return null;
    }

    /**
     * Returns the item ids for the items of the requested order.
     *
     * @param  orderNumber the order number
     * @return item ids for the items of the requested order
     */
    @Override
    public Collection<Integer> getItemIdsForOrder(int orderNumber) {

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return null;
        }
        assert orderNumber > 0;

        ArrayList<Integer> itemIds = new ArrayList<>();
        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                for (MessagingContents content : order.contents) {
                    itemIds.add(content.id);
                }
                return itemIds;
            }
        }
        return null;
    }

    /**
     * Returns the name of the item for the requested order.
     *
     * @param  itemId the food box id as last returned from the server
     * @param  orderNumber the order number
     * @return name of the item for the requested order
     */
    @Override
    public String getItemNameForOrder(int itemId, int orderNumber) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return null;
        }
        assert itemId > 0;

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return null;
        }
        assert orderNumber > 0;

        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                for (MessagingContents content : order.contents) {
                    if (content.id == itemId) {
                        return content.name;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the quantity of the item for the requested order.
     *
     * @param  itemId the food box id as last returned from the server
     * @param  orderNumber the order number
     * @return quantity of the item for the requested order
     */
    @Override
    public int getItemQuantityForOrder(int itemId, int orderNumber) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return -1;
        }
        assert itemId > 0;

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return -1;
        }
        assert orderNumber > 0;

        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                for (MessagingContents content : order.contents) {
                    if (content.id == itemId) {
                        return content.quantity;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns true if quantity of the item for the requested order was changed.
     *
     * @param  itemId the food box id as last returned from the server
     * @param  orderNumber the order number
     * @param  quantity the food box item quantity to be set
     * @return true if quantity of the item for the requested order was changed
     */
    @Override
    public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {

        // ensure appropriate item id
        if (itemId <= 0) {
            System.out.println("Sorry item id must be greater than 0 please try again");
            return false;
        }
        assert itemId > 0;

        // ensure appropriate order number
        if (orderNumber <= 0) {
            System.out.println("Sorry order number must be greater than 0 please try again");
            return false;
        }
        assert orderNumber > 0;

        // ensure appropriate quantity
        if (quantity <= 0) {
            System.out.println("Sorry quantity must be greater than 0 please try again");
            return false;
        }
        assert quantity > 0;

        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                for (MessagingContents content : order.contents) {
                    if (content.id == itemId) {

                        // ensure new quantity is less than existing quantity
                        if (content.quantity <= quantity) {
                            System.out.println("Sorry new quantity must be less than existing quantity");
                            return false;
                        }
                        assert quantity < content.quantity;
                        content.quantity = quantity;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

    // **UPDATE**

    /**
     * Returns closest catering company serving orders based on our location
     *
     * @return business name of catering company
     */
    @Override
    public String getClosestCateringCompany() {

        if (cateringCompanies.size() == 0) {
            return null;
        }

        float smallestDist = getDistance(individualInformation.postCode, cateringCompanies.get(0).postCode);
        String bestCC = cateringCompanies.get(0).name;

        for (MessagingCateringCompanies cateringCompany : cateringCompanies) {
            if (getDistance(individualInformation.postCode, cateringCompany.postCode) < smallestDist) {
                smallestDist = getDistance(individualInformation.postCode, cateringCompany.postCode);
                bestCC = cateringCompany.name;
            }
        }

        return bestCC;
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
            Date date = new SimpleDateFormat("ddMMyy").parse(chiDate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Helper for checking if response for newly registered individual information in correct format
     *
     * @param response the response where the format is being checked
     * @return true if the response is formatted correctly and false otherwise
     */
    public Boolean correctNewRegisterFormat(String response) {

        if (response.startsWith("[") && response.endsWith("]")) {
            try {
                String[] responses = response.split(",");
                return responses.length == 4;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    // Because of our use of scanners within the place order method we could not work out how to place an order in the
    // tests and therefore we used this setter to test the cancel and edit order methods

    /**
     * Sets an order and adds it to the individuals orders
     *
     * @param id order id
     * @param contents order contents
     * @param chi individual chi
     * @param cateringId catering company id
     * @param ordered when order ordered
     * @param packed when order packed
     * @param dispatched when order dispatched
     * @param delivered when order delivered
     * @param status order's current status
     */
    public void setOrder(int id, ArrayList<MessagingContents> contents, String chi, String cateringId,
                            LocalDateTime ordered, LocalDateTime packed, LocalDateTime dispatched,
                            LocalDateTime delivered, String status) {

        MessagingOrders order = new MessagingOrders();
        order.id = id;
        order.contents = contents;
        order.chi = chi;
        order.cateringId = cateringId;
        order.ordered = ordered;
        order.packed = packed;
        order.dispatched = dispatched;
        order.delivered = delivered;
        order.status = status;

        orders.add(order);
    }

    // Because of our use of scanners within the place order method we could not work out how to place an order in the
    // tests and therefore we used this setter to test the cancel and edit order methods

    /**
     * Sets some contents to a particular provided item
     *
     * @param id item id
     * @param name item name
     * @param quantity item quantity
     * @return array list of contents - single item
     */
    @Override
    public ArrayList<MessagingContents> setContents(int id, String name, int quantity) {

        ArrayList<MessagingContents> contents = new ArrayList<MessagingContents>();
        MessagingContents item = new MessagingContents();
        item.id = id;
        item.name = name;
        item.quantity = quantity;

        contents.add(item);

        return contents;
    }

}
