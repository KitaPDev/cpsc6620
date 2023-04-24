package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.mysql.cj.jdbc.admin.MiniAdmin;
import com.mysql.cj.protocol.Resultset;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery,
	// and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "small";
	public final static String size_m = "medium";
	public final static String size_l = "large";
	public final static String size_xl = "x-large";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";

	private static boolean connect_to_db() throws SQLException, IOException {
		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addOrder(Order o, Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, and pickup tables
		 */

		String sql = "INSERT INTO customer_order (CustOrderCustID, CustOrderOrderedAt, CustOrderCost, CustOrderPrice, CustOrderIsComplete, CustOrderIsPickup, CustOrderIsDelivery, CustOrderIsDineIn) "
				+
				"VALUES (?, NOW(), ?, ?, ?, ?, ?, ?);";
		PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		pstmt.setInt(1, o.getCustID());
		pstmt.setDouble(2, o.getBusPrice());
		pstmt.setDouble(3, o.getCustPrice());
		pstmt.setBoolean(4, o.getIsComplete() == 1 ? true : false);

		Boolean isPickup = false, isDelivery = false, isDineIn = false;
		switch (o.getOrderType()) {
			case DBNinja.dine_in:
				isDineIn = true;
				break;
			case DBNinja.pickup:
				isPickup = true;
				break;
			case DBNinja.delivery:
				isDelivery = true;
				break;
		}

		pstmt.setBoolean(5, isPickup);
		pstmt.setBoolean(6, isDelivery);
		pstmt.setBoolean(7, isDineIn);

		int affectedRows = pstmt.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("Insert failed, no rows affected.");
		}

		ResultSet generatedKeys = pstmt.getGeneratedKeys();
		if (generatedKeys.next()) {
			o.setOrderID(generatedKeys.getInt(1));

			for (Discount d : o.getDiscountList()) {
				DBNinja.useOrderDiscount(o, d, true);
			}

			if (o instanceof DineinOrder) {
				DineinOrder d = (DineinOrder) o;

				sql = "INSERT INTO dine_in (DineInCustOrderID, DineInTableNum) VALUES (?, ?);";
				PreparedStatement ps = conn.prepareStatement(sql);

				ps.setInt(1, o.getOrderID());
				ps.setInt(2, d.getTableNum());

				ps.executeUpdate();

			} else if (o instanceof PickupOrder) {
				PickupOrder p = (PickupOrder) o;

				sql = "INSERT INTO pickup VALUES (?, ?);";
				PreparedStatement ps = conn.prepareStatement(sql);

				ps.setInt(1, o.getOrderID());
				ps.setBoolean(2, p.getIsPickedUp() == 1 ? true : false);

				ps.executeUpdate();

			} else if (o instanceof DeliveryOrder) {
				DeliveryOrder d = (DeliveryOrder) o;

				sql = "INSERT INTO delivery (DeliveryCustOrderID, DeliveryFirstName, DeliveryLastName, DeliveryAddress) VALUES (?, ?, ?, ?);";
				PreparedStatement ps = conn.prepareStatement(sql);

				ps.setInt(1, o.getOrderID());
				ps.setString(2, c.getFName());
				ps.setString(3, c.getLName());
				ps.setString(4, d.getAddress());

				ps.executeUpdate();
			}

			for (Pizza p : o.getPizzaList()) {
				p.setOrderID(o.getOrderID());
				DBNinja.addPizza(p, true);
			}

			closeConnection();
		} else {
			throw new SQLException("Insert failed, no ID obtained.");
		}
	}

	public static void addPizza(Pizza p, boolean keepConnOpen) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind adding pizza discounts to that bridge table and
		 * instance of topping usage to that bridge table if you have't accounted
		 * for that somewhere else.
		 */

		String sql = "INSERT INTO pizza (PizzaCustOrderID, PizzaSize, PizzaCrust, PizzaIsDone, PizzaPrice, PizzaCost) "
				+
				"VALUES (?, ?, ?, False, ?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		pstmt.setInt(1, p.getOrderID());
		pstmt.setString(2, p.getSize());
		pstmt.setString(3, p.getCrustType());
		pstmt.setDouble(4, p.getCustPrice());
		pstmt.setDouble(5, p.getBusPrice());

		int affectedRows = pstmt.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("Insert failed, no rows affected.");
		}

		ResultSet generatedKeys = pstmt.getGeneratedKeys();
		if (generatedKeys.next()) {
			p.setPizzaID(generatedKeys.getInt(1));
		} else {
			throw new SQLException("Insert failed, no ID obtained.");
		}

		for (int i = 0; i < p.getToppings().size(); i++) {
			useTopping(p, p.getToppings().get(i), p.getIsDoubleArray()[i], true);
		}

		for (int i = 0; i < p.getDiscounts().size(); i++) {
			usePizzaDiscount(p, p.getDiscounts().get(i), true);
		}

		if (!keepConnOpen) {
			closeConnection();
		}
	}

	public static int getMaxPizzaID() throws SQLException, IOException {
		connect_to_db();
		/*
		 * A function I needed because I forgot to make my pizzas auto increment in my
		 * DB.
		 * It goes and fetches the largest PizzaID in the pizza table.
		 * You wont need to implement this function if you didn't forget to do that
		 */

		closeConnection();
		return -1;
	}

	// this function will update toppings inventory in SQL and add entities to the
	// Pizzatops table. Pass in the p pizza that is using t topping
	public static void useTopping(Pizza p, Topping t, boolean isDoubled, boolean keepConnOpen)
			throws SQLException, IOException {
		connect_to_db();
		/*
		 * This function should 2 two things.
		 * We need to update the topping inventory every time we use t topping
		 * (accounting for extra toppings as well)
		 * and we need to add that instance of topping usage to the pizza-topping bridge
		 * if we haven't done that elsewhere
		 * Ideally, you should't let toppings go negative. If someone tries to use
		 * toppings that you don't have, just print
		 * that you've run out of that topping.
		 */
		String sql = "INSERT INTO pizza_topping (PizzaTopPizzaID, PizzaTopToppingID, PizzaTopIsExtra) " +
				"VALUES (?, ?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt(1, p.getPizzaID());
		stmt.setInt(2, t.getTopID());
		stmt.setBoolean(3, isDoubled);

		stmt.executeUpdate();

		sql = "UPDATE topping SET ToppingInventory = ToppingInventory - ? WHERE ToppingID = ?;";
		stmt = conn.prepareStatement(sql);

		switch (p.getSize()) {
			case "small":
				stmt.setDouble(1, t.getPerAMT());
				break;
			case "medium":
				stmt.setDouble(1, t.getMedAMT());
				break;
			case "large":
				stmt.setDouble(1, t.getLgAMT());
				break;
			case "x-large":
				stmt.setDouble(1, t.getXLAMT());
				break;
		}

		stmt.setInt(2, t.getTopID());
		stmt.executeUpdate();

		if (!keepConnOpen) {
			closeConnection();
		}
	}

	public static void usePizzaDiscount(Pizza p, Discount d, boolean keepConnOpen) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Helper function I used to update the pizza-discount bridge table.
		 * You might use this, you might not depending on where / how to want to update
		 * this table
		 */
		String sql = "INSERT INTO discount_pizza (DiscPizzaDiscountID, DiscPizzaPizzaID) " +
				"VALUES (?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt(1, d.getDiscountID());
		stmt.setInt(2, p.getPizzaID());
		stmt.executeUpdate();

		if (!keepConnOpen) {
			closeConnection();
		}
	}

	public static void useOrderDiscount(Order o, Discount d, boolean keepConnOpen) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Helper function I used to update the pizza-discount bridge table.
		 * You might use this, you might not depending on where / how to want to update
		 * this table
		 */
		String sql = "INSERT INTO discount_order (DiscOrderDiscountID, DiscOrderCustOrderID) " +
				"VALUES (?, ?);";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setInt(1, d.getDiscountID());
		stmt.setInt(2, o.getOrderID());
		stmt.executeUpdate();

		if (!keepConnOpen) {
			closeConnection();
		}
	}

	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This should add a customer to the database
		 */

		String sql = "INSERT INTO customer (CustomerFirstName, CustomerLastName, CustomerPhoneNum) VALUES (?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, c.getFName());
		stmt.setString(2, c.getLName());
		stmt.setString(3, c.getPhone());

		stmt.executeUpdate();
		closeConnection();
	}

	public static void CompleteOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * add code to mark an order as complete in the DB. You may have a boolean field
		 * for this, or maybe a completed time timestamp. However you have it.
		 */
		String sql = "UPDATE customer_order SET CustOrderIsComplete = True WHERE CustOrderID = " + o.getOrderID() + ";";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);

		sql = "UPDATE pizza SET PizzaIsDone = True WHERE PizzaCustOrderID = " + o.getOrderID() + ";";
		stmt.executeUpdate(sql);

		if (o instanceof PickupOrder) {
			sql = "UPDATE pickup SET PickupIsPickedUp = True WHERE PickupCustOrderID = " + o.getOrderID() + ";";
			stmt.executeUpdate(sql);
		}

		closeConnection();
	}

	public static void AddToInventory(Topping t, double toAdd) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Adds toAdd amount of topping to topping t.
		 */
		String sql = "UPDATE topping SET ToppingInventory = ToppingInventory + ? WHERE ToppingID = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setDouble(1, toAdd);
		stmt.setInt(2, t.getTopID());

		stmt.executeUpdate();
		closeConnection();
	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();

		/*
		 * I used this function to PRINT (not return) the inventory list.
		 * When you print the inventory (either here or somewhere else)
		 * be sure that you print it in a way that is readable.
		 * 
		 * 
		 * 
		 * The topping list should also print in alphabetical order
		 */

		closeConnection();
	}

	public static ArrayList<Topping> getInventory() throws SQLException, IOException {
		connect_to_db();
		/*
		 * This function actually returns the toppings. The toppings
		 * should be returned in alphabetical order if you don't
		 * plan on using a printInventory function
		 */
		ArrayList<Topping> toppings = new ArrayList<Topping>();

		String sql = "SELECT * FROM topping ORDER BY ToppingName;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int topID = rs.getInt("ToppingID");
			String topName = rs.getString("ToppingName");
			double amtSmall = rs.getDouble("ToppingAmtSmall");
			double amtMedium = rs.getDouble("ToppingAmtMedium");
			double amtLarge = rs.getDouble("ToppingAmtLarge");
			double amtXLarge = rs.getDouble("ToppingAmtXLarge");
			double price = rs.getDouble("ToppingPrice");
			double cost = rs.getDouble("ToppingCost");
			double minInv = rs.getDouble("ToppingMinInventory");
			double currInv = rs.getDouble("ToppingInventory");

			toppings.add(new Topping(topID, topName, amtSmall, amtMedium, amtLarge, amtXLarge, price, cost,
					(int) minInv, (int) currInv));
		}

		closeConnection();
		return toppings;
	}

	public static ArrayList<Order> getOrderList() throws SQLException, IOException {
		connect_to_db();
		ArrayList<Order> orders = new ArrayList<Order>();
		/*
		 * This function should return an arraylist of all of the orders.
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Also, like toppings, whenever we print out the orders using menu function 4
		 * and 5
		 * these orders should print in order from newest to oldest.
		 */

		String sql = "SELECT * FROM customer_order "
				+ "LEFT JOIN pickup ON PickupCustOrderID = CustOrderID "
				+ "LEFT JOIN delivery ON DeliveryCustOrderID = CustOrderID "
				+ "LEFT JOIN dine_in ON DineinCustOrderID = CustOrderID "
				+ "ORDER BY CustOrderOrderedAt DESC;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int orderID = rs.getInt("CustOrderID");
			int custID = rs.getInt("CustOrderCustID");
			Date orderedAt = rs.getDate("CustOrderOrderedAt");
			double price = rs.getDouble("CustOrderPrice");
			double cost = rs.getDouble("CustOrderCost");

			if (rs.getBoolean("CustOrderIsPickup")) {
				orders.add(new PickupOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("PickupIsPickedUp") ? 1 : 0, rs.getBoolean("CustOrderIsComplete") ? 1 : 0));

			} else if (rs.getBoolean("CustOrderIsDelivery")) {
				orders.add(new DeliveryOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("CustOrderIsComplete") ? 1 : 0,
						rs.getString("DeliveryAddress")));

			} else {
				orders.add(new DineinOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("CustOrderIsComplete") ? 1 : 0,
						rs.getInt("DineInTableNum")));
			}
		}

		stmt.close();
		closeConnection();
		return orders;
	}

	public static ArrayList<Order> getOrderList(Date fromDate) throws SQLException, IOException {
		connect_to_db();
		ArrayList<Order> orders = new ArrayList<Order>();
		/*
		 * This function should return an arraylist of all of the orders.
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Also, like toppings, whenever we print out the orders using menu function 4
		 * and 5
		 * these orders should print in order from newest to oldest.
		 */

		String sql = "SELECT * FROM customer_order " +
				"LEFT JOIN pickup ON PickupCustOrderID = CustOrderID " +
				"LEFT JOIN delivery ON DeliveryCustOrderID = CustOrderID " +
				"LEFT JOIN dine_in ON DineinCustOrderID = CustOrderID " +
				"WHERE CustOrderOrderedAt >= '" + new SimpleDateFormat("yyyy-MM-dd").format(fromDate) + "' " +
				"ORDER BY CustOrderOrderedAt DESC;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int orderID = rs.getInt("CustOrderID");
			int custID = rs.getInt("CustOrderCustID");
			Date orderedAt = rs.getDate("CustOrderOrderedAt");
			double price = rs.getDouble("CustOrderPrice");
			double cost = rs.getDouble("CustOrderCost");

			if (rs.getBoolean("CustOrderIsPickup")) {
				orders.add(new PickupOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("PickupIsPickedUp") ? 1 : 0, rs.getBoolean("CustOrderIsComplete") ? 1 : 0));

			} else if (rs.getBoolean("CustOrderIsDelivery")) {
				orders.add(new DeliveryOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("CustOrderIsComplete") ? 1 : 0,
						rs.getString("DeliveryAddress")));

			} else {
				orders.add(new DineinOrder(orderID, custID, orderedAt.toString(), price, cost,
						rs.getBoolean("CustOrderIsComplete") ? 1 : 0,
						rs.getInt("DineInTableNum")));
			}
		}

		stmt.close();
		closeConnection();
		return orders;
	}

	public static ArrayList<Order> sortOrders(ArrayList<Order> list) {
		/*
		 * This was a function that I used to sort my arraylist based on date.
		 * You may or may not need this function depending on how you fetch
		 * your orders from the DB in the getCurrentOrders function.
		 */

		closeConnection();
		return null;

	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder) {
		// Helper function I used to help sort my dates. You likely wont need these

		return false;
	}

	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		double bp = 0.0;
		// add code to get the base price (for the customer) for that size and crust
		// pizza Depending on how
		// you store size & crust in your database, you may have to do a conversion
		String sql = "SELECT SizeCrustPrice FROM size_crust WHERE SizeCrustSize = ? AND SizeCrustCrust = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, size);
		stmt.setString(2, crust);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			bp += rs.getDouble("SizeCrustPrice");
		}

		closeConnection();
		return bp;
	}

	public static String getCustomerName(int CustID) throws SQLException, IOException {
		/*
		 * This is a helper function I used to fetch the name of a customer
		 * based on a customer ID. It actually gets called in the Order class
		 * so I'll keep the implementation here. You're welcome to change
		 * how the order print statements work so that you don't need this function.
		 */
		connect_to_db();
		String ret = "";
		String query = "Select CustomerFirstName, CustomerLastName From customer WHERE CustomerID = " + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			ret = rset.getString(1) + " " + rset.getString(2);
		}
		conn.close();
		return ret;
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		double bp = 0.0;
		// add code to get the base cost (for the business) for that size and crust
		// pizza Depending on how
		// you store size and crust in your database, you may have to do a conversion
		String sql = "SELECT SizeCrustCost FROM size_crust WHERE SizeCrustSize = ? AND SizeCrustCrust = ?;";
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, size);
		stmt.setString(2, crust);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			bp += rs.getDouble("SizeCrustCost");
		}

		closeConnection();
		return bp;
	}

	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		connect_to_db();
		// returns a list of all the discounts.
		ArrayList<Discount> discs = new ArrayList<Discount>();
		String sql = "SELECT * FROM discount;";
		Statement stmt = conn.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount = rs.getDouble("DiscountAmount");
			boolean isPercent = rs.getBoolean("DiscountIsPercent");

			discs.add(new Discount(discountID, discountName, amount, isPercent));
		}

		closeConnection();
		return discs;
	}

	public static ArrayList<Discount> getDiscountList(Boolean keepConnOpen) throws SQLException, IOException {
		connect_to_db();
		// returns a list of all the discounts.
		ArrayList<Discount> discs = new ArrayList<Discount>();
		String sql = "SELECT * FROM discount;";
		Statement stmt = conn.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount = rs.getDouble("DiscountAmount");
			boolean isPercent = rs.getBoolean("DiscountIsPercent");

			discs.add(new Discount(discountID, discountName, amount, isPercent));
		}

		if (!keepConnOpen) {
			closeConnection();
		}
		return discs;
	}

	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		ArrayList<Customer> custs = new ArrayList<Customer>();
		/*
		 * return an arrayList of all the customers. These customers should
		 * print in alphabetical order, so account for that as you see fit.
		 */
		Statement stmt = conn.createStatement();
		String sql = "SELECT * FROM customer ORDER BY CustomerFirstName, CustomerLastName;";
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			int custID = rs.getInt("CustomerID");
			String fName = rs.getString("CustomerFirstName");
			String lName = rs.getString("CustomerLastName");
			String phoneNum = rs.getString("CustomerPhoneNum");

			custs.add(new Customer(custID, fName, lName, phoneNum));
		}

		stmt.close();
		closeConnection();
		return custs;
	}

	public static int getNextOrderID() throws SQLException, IOException {
		/*
		 * A helper function I had to use because I forgot to make
		 * my OrderID auto increment...You can remove it if you
		 * did not forget to auto increment your orderID.
		 */

		closeConnection();
		return -1;
	}

	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print (other than that it should
		 * be in alphabetical order by name), just make sure it's readable.
		 */
		String sql = "SELECT * FROM ToppingPopularity;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		System.out.printf("%-25s %-15s\n", "Topping", "ToppingCount");
		while (rs.next()) {
			String topping = rs.getString("Topping");
			int count = rs.getInt("ToppingCount");

			System.out.printf("%-25s %-15s\n", topping, count);
		}

		closeConnection();
	}

	public static void printProfitByPizzaReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print, just make sure it's readable.
		 */
		String sql = "SELECT * FROM ProfitByPizza;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		System.out.printf("%-15s %-20s %-10s %-20s\n", "Pizza Size", "Pizza Crust", "Profit", "LastOrderDate");
		while (rs.next()) {
			String size = rs.getString("Pizza Size");
			String crust = rs.getString("Pizza Crust");
			double profit = rs.getDouble("Profit");
			String date = rs.getString("LastOrderDate");

			System.out.printf("%-15s %-20s %-10s %-20s\n", size, crust, profit, date);
		}

		closeConnection();
	}

	public static void printProfitByOrderType() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print, just make sure it's readable.
		 */
		String sql = "SELECT * FROM ProfitByOrderType;";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		System.out.printf("%-15s %-15s %-20s %-20s %-10s\n", "OrderType", "Order Month", "TotalOrderPrice",
				"TotalOrderCost", "Profit");
		while (rs.next()) {
			if (rs.isLast()) {
				String grandTotal = rs.getString("OrderMonth");
				double price = rs.getDouble("TotalOrderPrice");
				double cost = rs.getDouble("TotalOrderCost");
				double profit = rs.getDouble("Profit");

				System.out.printf("%-15s %-15s %-20s %-20s %-10s\n", "", grandTotal, price, cost, profit);
				break;
			}

			String type = rs.getString("CustomerType");
			String month = rs.getString("OrderMonth");
			double price = rs.getDouble("TotalOrderPrice");
			double cost = rs.getDouble("TotalOrderCost");
			double profit = rs.getDouble("Profit");

			System.out.printf("%-15s %-15s %-20s %-20s %-10s\n", type, month, price, cost, profit);
		}

		closeConnection();
	}

}