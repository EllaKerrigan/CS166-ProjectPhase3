/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   // enum that provides label (Customer, Driver, Manager) to current user
   enum UserType {
      CUSTOMER,
      DRIVER,
      MANAGER
   }

   // global reference to the label enum
   private static UserType user_type;

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end PizzaStore

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     PizzaStore.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      PizzaStore esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorisedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }// end switch
            if (authorisedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. View Profile");
                  System.out.println("2. Update Profile");
                  System.out.println("3. View Menu");
                  System.out.println("4. Place Order"); // make sure user specifies which store
                  System.out.println("5. View Full Order ID History");
                  System.out.println("6. View Past 5 Order IDs");
                  System.out.println("7. View Order Information"); // user should specify orderID and then be able to
                                                                   // see detailed information about the order
                  System.out.println("8. View Stores");

                  // **the following functionalities should only be able to be used by drivers &
                  // managers**
                  if (user_type == UserType.DRIVER || user_type == UserType.MANAGER) {
                     System.out.println("9. Update Order Status");
                  }

                  // **the following functionalities should ony be able to be used by managers**
                  if (user_type == UserType.MANAGER) {
                     System.out.println("10. Update Menu");
                     System.out.println("11. Update User");
                  }

                  System.out.println(".........................");
                  System.out.println("20. Log out");
                  switch (readChoice()) {
                     case 1:
                        viewProfile(esql, authorisedUser);
                        break;
                     case 2:
                        updateProfile(esql, authorisedUser);
                        break;
                     case 3:
                        viewMenu(esql);
                        break;
                     case 4:
                        placeOrder(esql);
                        break;
                     case 5:
                        viewAllOrders(esql);
                        break;
                     case 6:
                        viewRecentOrders(esql);
                        break;
                     case 7:
                        viewOrderInfo(esql);
                        break;
                     case 8:
                        viewStores(esql);
                        break;
                     case 9:
                        updateOrderStatus(esql);
                        break;
                     case 10:
                        updateMenu(esql);
                        break;
                     case 11:
                        updateUser(esql);
                        break;

                     case 20:
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * 
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql) {
      try {
         // Prompt user for input
         System.out.print("Enter username: ");
         String username = in.readLine();

         System.out.print("Enter password: ");
         String password = in.readLine();

         System.out.print("Enter role (e.g., Customer, Manager, Driver): ");
         String role = in.readLine();

         System.out.print("Enter phone number: ");
         String phoneNum = in.readLine();

         String query = String.format(
               "INSERT INTO Users (login, password, role, favoriteItems, phoneNum) VALUES ('%s', '%s', '%s', NULL, '%s');",
               username, password, role, phoneNum);

         esql.executeUpdate(query);
         System.out.println("User successfully created!");

      } catch (Exception e) {
         System.err.println("Error creating user: " + e.getMessage());
      }
   }

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null is the user does not exist
    **/

   public static String LogIn(PizzaStore esql) {
      try {
         System.out.print("Enter your username: ");
         String login = in.readLine();

         System.out.print("Enter your password: ");
         String password = in.readLine();

         String query = "SELECT * FROM Users WHERE login = ? AND password = ?";

         Connection conn = esql._connection; // Assuming esql.getConnection() returns a valid Connection object

         PreparedStatement stmt = conn.prepareStatement(query);
         stmt.setString(1, login); // Set the login parameter
         stmt.setString(2, password); // Set the password parameter

         ResultSet rs = stmt.executeQuery();

         // Check if the user exists
         if (rs.next()) {
            String role = rs.getString("role").trim();

            if (role.equals("customer")) {
               user_type = UserType.CUSTOMER;
            } else if (role.equals("driver")) {
               user_type = UserType.DRIVER;
            } else if (role.equals("manager")) {
               user_type = UserType.MANAGER;
            }

            System.out.println("Login successful!\n");
            return login; // Return the username (login) if successful
         } else {
            System.out.println("Invalid username or password.\n");
            return null;
         }

      } catch (SQLException e) {
         System.err.println("SQL error: " + e.getMessage());
         return null;
      } catch (Exception e) {
         System.err.println("Error while logging in: " + e.getMessage());
         return null;
      }
   }

   // Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String username) {
      Connection conn = esql._connection;

      try {
         String query = String.format("SELECT favoriteItems, phoneNum FROM Users WHERE login = '%s'", username);
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();

         while (rs.next()) {
            System.out.println("\nFavorite Item: " + rs.getString("favoriteItems"));
            System.out.println("Phone Number: " + rs.getString("phoneNum") + "\n");
         }
      } catch (SQLException e) {
         System.err.println("Database error when viewing profile: " + e.getMessage());
      }
   }

   public static void updateProfile(PizzaStore esql, String username) {
      int user_choice = 0;
      Connection conn = esql._connection;

      try {
         do {
            System.out.println("\n1. Update Favorite Item");
            System.out.println("2. Update Phone Number");
            System.out.println("3. Update Password");
            System.out.println("4. Go Back");
            System.out.print("Please make your choice: ");

            String inputStr = in.readLine();
            if (!inputStr.isEmpty()) {
               user_choice = Integer.parseInt(inputStr);
            }
            if (user_choice < 1 || user_choice > 4) {
               System.out.println("\nPlease select a valid option.");
            }
         } while (user_choice < 1 || user_choice > 4);

      } catch (IOException e) {
         System.err.println("Error with user input: " + e.getMessage());
      }

      try {
         if (user_choice == 1) {
            System.out.print("Input new favorite item: ");
            String inputStr = in.readLine();

            String query = String.format("SELECT COUNT(*) FROM Items WHERE itemName = '%s'", inputStr);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && !rs.getBoolean(1)) {
               System.out.println("Selected Item does not exist in the menu.\n");
               return;
            }

            query = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s'", inputStr, username);
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            System.out.println("Favorite item updated successfully!\n");

         } else if (user_choice == 2) {
            System.out.print("Input new phone number: ");
            String inputStr = in.readLine();

            String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", inputStr, username);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            System.out.println("Phone number updated successfully!\n");

         } else if (user_choice == 3) {
            System.out.print("Input new password: ");
            String inputStr = in.readLine();

            String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", inputStr, username);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            System.out.println("Password updated successfully!\n");

         } else if (user_choice == 4) {
            return;
         }
      } catch (SQLException e) {
         System.err.println("Database error while editing profile: " + e.getMessage());
      } catch (IOException e) {
         System.err.println("Error with user input: " + e.getMessage());
      }
   }

   public static void viewMenu(PizzaStore esql) {
      try {
         System.out.println("Enter a type (e.g., drinks, sides) to filter by type (or leave blank to skip): ");

         String typeOfItem = in.readLine();

         System.out.println("Enter a maximum price to filter by price (or leave blank to skip): ");
         String priceStr = in.readLine();
         Double price = null;
         if (!priceStr.isEmpty()) {
            price = Double.parseDouble(priceStr);
         }
         System.out.println("Sort by price: ");
         System.out.println("1. Lowest to Highest");
         System.out.println("2. Highest to Lowest");
         System.out.print("Enter your choice (1 or 2): ");
         String sortChoice = in.readLine();
         String sortOrder = "";
         if (sortChoice.equals("1")) {
            sortOrder = "ASC"; // Sort from low to high
         } else if (sortChoice.equals("2")) {
            sortOrder = "DESC"; // Sort from high to low
         }

         String query = "SELECT * FROM items";
         String conditions = "";

         if (!typeOfItem.isEmpty()) {
            conditions += " typeOfItem = '" + typeOfItem + "'";
         }

         if (price != null) {
            if (!conditions.isEmpty()) {
               conditions += " AND";
            }
            conditions += " price <= " + price;
         }

         if (!conditions.isEmpty()) {
            query += " WHERE" + conditions;
         }

         if (!sortOrder.isEmpty()) {
            query += " ORDER BY price " + sortOrder;
         }

         Connection conn = esql._connection;
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();

         System.out.println("\nMenu Items:");
         while (rs.next()) {
            System.out.println("Name: " + rs.getString("itemName"));
            System.out.println("Type: " + rs.getString("typeOfItem"));
            System.out.println("Price: " + rs.getDouble("price"));
            System.out.println("Description: " + rs.getString("description"));
            System.out.println();
         }

      } catch (Exception e) {
         System.err.println("Error while retrieving menu: " + e.getMessage());
      }
   }

   public static void placeOrder(PizzaStore esql) {
   }

   public static void viewAllOrders(PizzaStore esql) {
   }

   public static void viewRecentOrders(PizzaStore esql) {
   }

   public static void viewOrderInfo(PizzaStore esql) {
   }

   public static void viewStores(PizzaStore esql) {
      try {
         String query = "SELECT * FROM Store";
         Connection conn = esql._connection;
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();

         System.out.println("\nStore Info:");
         while (rs.next()) {
            System.out.println("Store ID: " + rs.getInt("storeID"));
            System.out.println("Adress: " + rs.getString("address"));
            System.out.println("City: " + rs.getString("city"));
            System.out.println("State: " + rs.getString("state"));
            System.out.println("Open: " + rs.getString("isOpen"));
            System.out.println("Review Score: " + rs.getString("reviewScore"));
            System.out.println();
         }

      } catch (SQLException e) {
         System.err.println("Database error while retrieving store info: " + e.getMessage());
      }
   }

   public static void updateOrderStatus(PizzaStore esql) {
      Connection conn = esql._connection;
      PreparedStatement stmt;
      ResultSet rs;

      try {
         System.out.print("\nEnter the order number of the order you want to edit: ");
         String order_num = in.readLine();

         String query = String.format("SELECT COUNT(*) FROM FoodOrder WHERE orderID = %s", order_num);
         stmt = conn.prepareStatement(query);
         rs = stmt.executeQuery();

         if (rs.next() && !rs.getBoolean(1)) {
            System.out.println("Selected order does not exists.\n");
            return;
         }

         System.out.print("Enter the new order status: ");
         String order_status = in.readLine();

         if (!order_status.equals("complete") && !order_status.equals("incomplete")) {
            System.out.println("Entered order status is not valid");
            return;
         }

         query = String.format("UPDATE FoodOrder SET orderStatus = '%s' WHERE orderID = '%s'", order_status, order_num);
         stmt = conn.prepareStatement(query);
         stmt.executeUpdate();
         System.out.println("Data updated successfully!\n");

      } catch (SQLException e) {
         System.err.println("Database error while retrieving order info: " + e.getMessage());
      } catch (IOException e) {
         System.err.println("Error with user input: " + e.getMessage());
      }
   }

   public static void updateMenu(PizzaStore esql) {
      Connection conn = esql._connection;
      int user_choice = 0;

      try {
         do {
            System.out.println("\n1. Update Existing Item");
            System.out.println("2. Add New Item");
            System.out.println("3. Go Back");
            System.out.print("Please make your choice: ");

            String inputStr = in.readLine();
            if (!inputStr.isEmpty()) {
               user_choice = Integer.parseInt(inputStr);
            }
            if (user_choice < 1 || user_choice > 3) {
               System.out.println("\nPlease select a valid option.");
            }
         } while (user_choice < 1 || user_choice > 3);

         if (user_choice == 1) {
            System.out.print("\nEnter the item name of the item you want to edit: ");
            String item_name = in.readLine();

            String query = String.format("SELECT COUNT(*) FROM Items WHERE itemName = '%s'", item_name);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && !rs.getBoolean(1)) {
               System.out.println("Selected item does not exist.\n");
               return;
            }

            System.out.print("Enter new item name (leave empty to skip): ");
            String new_item_name = in.readLine();

            System.out.print("Enter new ingredient(s) (leave empty to skip): ");
            String new_ingredients = in.readLine();

            System.out.print("Enter new item type (leave empty to skip) : ");
            String new_item_type = in.readLine();

            System.out.print("Enter new price (leave empty to skip): ");
            String new_price_str = in.readLine();
            Double new_price = null;
            if (!new_price_str.isEmpty()) {
               new_price = Double.parseDouble(new_price_str);
            }

            System.out.print("Enter new description (leave empty ro skip): ");
            String new_description = in.readLine();

            query = "UPDATE Items SET ";

            if (!new_item_name.isEmpty()) {
               query += "itemName = '" + new_item_name + "'";
            } else {
               query += "itemName = '" + item_name + "'";
            }

            if (!new_ingredients.isEmpty()) {
               query += ", ingredients = '" + new_ingredients + "'";
            }

            if (!new_item_type.isEmpty()) {
               query += ", typeOfItem = '" + new_item_type + "'";
            }

            if (new_price != null) {
               query += ", price = '" + new_price + "'";
            }

            if (!new_description.isEmpty()) {
               query += ", description = '" + new_description + "'";
            }

            query += String.format(" WHERE itemName = '%s'", item_name);
            conn = esql._connection;
            stmt = conn.prepareStatement(query);
            stmt.executeQuery();

         } else if (user_choice == 2) {
            System.out.print("\nEnter the item name of the item you want to add: ");
            String item_name = in.readLine();

            String query = String.format("SELECT COUNT(*) FROM Items WHERE itemName = '%s'", item_name);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getBoolean(1)) {
               System.out.println("Selected item already exists.\n");
               return;
            }

            System.out.print("Enter ingredient(s): ");
            String new_ingredients = in.readLine();

            System.out.print("Enter item type: ");
            String new_item_type = in.readLine();

            System.out.print("Enter price: ");
            String new_price_str = in.readLine();
            Double new_price = null;
            if (!new_price_str.isEmpty()) {
               new_price = Double.parseDouble(new_price_str);
            }

            System.out.print("Enter new description: ");
            String new_description = in.readLine();

            if (item_name.isEmpty() || new_ingredients.isEmpty() || new_item_type.isEmpty() || new_price == null
                  || new_description.isEmpty()) {
               System.out.println("Empty values are not allowed");
               return;
            }

            query = String.format(
                  "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %.2f, '%s');",
                  item_name, new_ingredients, new_item_type, new_price, new_description);

            esql.executeUpdate(query);
            System.out.println("Item successfully created!");

         } else if (user_choice == 3) {
            return;
         }

      } catch (SQLException e) {
         System.err.println("Database error while retrieving item info: " + e.getMessage());
      } catch (IOException e) {
         System.err.println("Error with user input: " + e.getMessage());
      }
   }

   public static void updateUser(PizzaStore esql) {
      Connection conn = esql._connection;
      int user_choice = 0;

      try {
         // get login and make sure it exists
         System.out.print("\nEnter the login of the user you want to edit: ");
         String login = in.readLine();

         String query = String.format("SELECT COUNT(*) FROM Users WHERE login = '%s'", login);
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();

         if (rs.next() && !rs.getBoolean(1)) {
            System.out.println("Selected user does not exist.\n");
            return;
         }

         do {
            System.out.println("1. Update Login");
            System.out.println("2. Update Role");
            System.out.println("3. Go Back");
            System.out.print("Please make your choice: ");

            String inputStr = in.readLine();
            if (!inputStr.isEmpty()) {
               user_choice = Integer.parseInt(inputStr);
            }
            if (user_choice < 1 || user_choice > 3) {
               System.out.println("\nPlease select a valid option.");
            }
         } while (user_choice < 1 || user_choice > 3);

         if (user_choice == 1 || user_choice == 2) {
            String prompt = (user_choice == 1) ? "Enter new login for the user: " : "Enter new role for the user: ";
            System.out.print(prompt);
            String inputStr = in.readLine();

            // input validation
            if (user_choice == 2 && !inputStr.equals("customer") && !inputStr.equals("driver")
                  && !inputStr.equals("manager")) {
               System.out.println("Selected role is not valid");
               return;
            } else if (user_choice == 1) {
               query = String.format("SELECT COUNT(*) FROM Users WHERE login = '%s'", inputStr);
               stmt = conn.prepareStatement(query);
               rs = stmt.executeQuery();

               if (rs.next() && rs.getBoolean(1)) {
                  System.out.println("Selected login is already taken.\n");
                  return;
               }
            }

            // update user type ennum if role is changed
            if (user_choice == 2 && inputStr.equals("customer")) {
               user_type = UserType.CUSTOMER;
            } else if (user_choice == 2 && inputStr.equals("driver")) {
               user_type = UserType.DRIVER;
            } else if (user_choice == 2 && inputStr.equals("manager")) {
               user_type = UserType.MANAGER;
            }

            String attribute = (user_choice == 1) ? "login" : "role";
            query = String.format("UPDATE Users SET %s = '%s' WHERE login = '%s'", attribute, inputStr, login);
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            System.out.println("Data updated successfully!\n");

         } else if (user_choice == 3) {
            return;
         }

      } catch (SQLException e) {
         System.err.println("Database error while retrieving user info: " + e.getMessage());
      } catch (IOException e) {
         System.err.println("Error with user input: " + e.getMessage());
      }
   }

}// end PizzaStore
