package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Scanner;

/**
 * ShieldingIndividualClient implemented
 */
public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

    /**
     * Create class for storing food boxes
     */
    static final class MessagingFoodBox {
        ArrayList<MessagingContents> contents; // transient ???
        String delivered_by;
        String diet;
        int id;
        String name;
    }

    /**
     * Create class for storing contents (items in food box or order)
     */
    static final class MessagingContents {
        int id;
        String name;
        int quantity;
    }

    /**
     * Create class for storing order information
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
     */
    static final class MessagingCateringCompanies {
        String name;
        String postCode;
    }

    /**
     * Create class for storing shielding individuals information
     */
    static final class MessagingIndividual {
        String postCode;
        String name;
        String surname;
        String phoneNumber;
    }

    /**
     * Private variables created:
     * endpoint
     * is individual registered
     * individuals chi
     * individuals information
     * id of selected food box by individual
     * list of food boxes, and their details, with specific dietary requirement available
     * list of catering companies and their details
     * list of individuals orders and their details
     */
    private String endpoint;
    private boolean registered;
    private String CHI;
    private MessagingIndividual individualInformation = new MessagingIndividual();
    private int selectedFoodBoxId;
    private ArrayList<MessagingFoodBox> foodBoxOptions =  new ArrayList<>();
    private ArrayList<MessagingCateringCompanies> cateringCompanies =  new ArrayList<>();
    private ArrayList<MessagingOrders> orders =  new ArrayList<>();;

    /**
     * Class constructor
     *
     * @param endpoint the server endpoint to be used
     */
    public ShieldingIndividualClientImp(String endpoint) { this.endpoint = endpoint; }

    /**
     * Registers/checks registered status of shielding individual
     * (also gathers individuals details and stores them locally if newly registered individual)
     *
     * @param CHI CHI number of the shielding individual
     * @return true if individual now is/was registered and false otherwise
     */
    @Override
    public boolean registerShieldingIndividual(String CHI) {

        //check if shielding individual already registered
        if (registered) { return true; }

        // construct endpoint request
        String request = "/registerShieldingIndividual?CHI=" + CHI;

        try {
            //perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            this.individualInformation.postCode = "EH1234";

            // store information locally if newly registered individual
            if (!response.equals("already registered")) {
                response = response.replace("[", "");
                response = response.replace("]", "");
                String[] information = response.split(",");
                individualInformation.postCode = information[0];
                individualInformation.name = information[1];
                individualInformation.surname = information[2];
                individualInformation.phoneNumber = information[3];
            }
            this.CHI = CHI;
            registered = true;
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Lists all food boxes available for dietary requirement provided
     * (also gathers and stores locally all food boxes returned and their details)
     *
     * @param dietaryPreference the dietary preference
     * @return list of ids of all food boxes matching dietary requirement
     */
    @Override
    public Collection<String> showFoodBoxes(String dietaryPreference) {

        // construct endpoint request
        String request =  "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;

        // setup response recipient
        List<MessagingFoodBox> responseBoxes;

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

        // ensure user registered
        if (!registered) {
            System.out.println("Must be registered to place order");
            return false;
        }

        // ask individual if they are ordering from a supermarket or a catering company
        String orderOption = "";
        while (!(orderOption.equals("supermarket") | orderOption.equals("catering company"))) {
            Scanner selectOrderOption = new Scanner(System.in);
            System.out.println("Please input if you would like to order from 'supermarket' or 'catering company'");
            orderOption = selectOrderOption.nextLine();
        }

        if (orderOption.equals("supermarket")) {

            // if user selects supermarket to order from return ???

            return false;

        } else {

            // if user selects catering company to order from build order

            // user asked to select dietary preference
            String dietaryPreference = "";
            while (!(dietaryPreference.equals("none") | dietaryPreference.equals("pollotarian") | dietaryPreference.equals("vegan"))) {
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

        // previous orders for client not on system

        // if client has made no orders no orders can be edited
        if (getOrderNumbers() == null) {
            System.out.println("Sorry you have placed no orders");
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

                    // if successfully edited return true and otherwise throw an error
                    if (response.equals("true")) {
                        System.out.println("Order " + orderNumber + " successfully edited");
                        return true;
                    } else {
                        System.err.println(response);
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
     * Cancels order
     *
     * @param orderNumber the order number
     * @return true if order has been cancelled and false if not
     */
    @Override
    public boolean cancelOrder(int orderNumber) {

        // previous orders for client not on system

        // if client has made no orders no orders can be edited
        if (getOrderNumbers() == null) {
            System.out.println("Sorry you have placed no orders");
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

                    // if order successfully cancelled return true and otherwise throw an error
                    if (response.equals("true")) {
                        System.out.println("Order " + orderNumber + " successfully cancelled");
                        return true;
                    } else {
                        System.err.println(response);
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
                    return true;
                case "1":
                     orders.get(orderPlace).status = "packed";
                     return true;
                case "2":
                    orders.get(orderPlace).status = "dispatched";
                    return true;
                case "3":
                    orders.get(orderPlace).status = "delivered";
                    return true;
                case "4":
                    orders.get(orderPlace).status = "cancelled";
                    return true;
                case "-1":
                    System.err.println("Order not found");
                default:
                    System.err.println(response);
            }

            //print order status
            System.out.println("Order status of order number " + orderNumber + " is: " + response);

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

            // unmarshal response
            response = response.replace("[", "");
            response = response.replace("]", "");
            String[] companies = response.split("\"");

            if (companies.length == 0) {
                System.out.println("No catering companies on system");
                return null;
            }

            for (String company : companies) {
                String[] companyInformation = company.split(",");

                MessagingCateringCompanies current = new MessagingCateringCompanies();
                current.name = companyInformation[0];
                current.postCode = companyInformation[1];

                companyNames.add(current.name);
                cateringCompanies.add(current);
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

        if (!(postCode1.startsWith("EH") & postCode2.startsWith("EH"))) {
            System.out.println("Invalid postCodes");
            return(-1);
        }

        // construct endpoint request
        String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2;

        try {
            //perform request
            String response = ClientIO.doGETRequest(endpoint + request);

            // return float if provided and 0 otherwise
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
        for (MessagingFoodBox foodBox : foodBoxOptions) {
            if (foodBox.id == selectedFoodBoxId) {
                for (MessagingContents content : foodBox.contents) {
                    if (content.id == itemId) {
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
        for (MessagingOrders order : orders) {
            if (order.id == orderNumber) {
                for (MessagingContents content : order.contents) {
                    if (content.id == itemId) {
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

}
