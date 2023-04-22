-- Kita Pairojtanachai, Siddarth Malladi

INSERT INTO topping (ToppingName, ToppingPrice, ToppingCost, ToppingInventory, ToppingAmtSmall, ToppingAmtMedium, ToppingAmtLarge, ToppingAmtXLarge)
VALUES 
	('Pepperoni', 1.25, 0.2, 100, 2, 2.75, 3.5, 4.5), 
	('Sausage', 1.25, 0.15, 100, 2.5, 3, 3.5, 4.25),
	('Ham', 1.5, 0.15, 78, 2, 2.5, 3.25, 4),
	('Chicken', 1.75, 0.25, 56, 1.5, 2, 2.25, 3),
	('Green Pepper', 0.5, 0.02, 79, 1, 1.5, 2, 2.5),
	('Onion', 0.5, 0.02, 85, 1, 1.5, 2, 2.5),
	('Roma Tomato', 0.75, 0.03, 86, 2, 3, 3.5, 4.5),
	('Mushrooms', 0.75, 0.1, 52, 1.5, 2, 2.5, 3),
	('Black Olives', 0.6, 0.1, 39, 0.75, 1, 1.5, 2),
	('Pineapple', 1, 0.25, 15, 1, 1.25, 1.75, 2),
	('Jalapenos', 0.5, 0.05, 64, 0.5, 0.75, 1.25, 1.75),
	('Banana Peppers', 0.5, 0.05, 36, 0.6, 1, 1.3, 1.75),
	('Regular Cheese', 1.5, 0.12, 250, 2, 3.5, 5, 7),
	('Four Cheese Blend', 2, 0.15, 150, 2, 3.5, 5, 7),
	('Feta Cheese', 2, 0.18, 75, 1.75, 3, 4, 5.5),
	('Goat Cheese', 2, 0.2, 54, 1.6, 2.75, 4, 5.5),
	('Bacon', 1.5, 0.25, 89, 1, 1.5, 2, 3);

INSERT INTO discount (DiscountName, DiscountAmount, DiscountIsPercent)
VALUES
	('Employee', 15, True),
	('Lunch Special Medium', 1.00, False),
	('Lunch Special Large', 1.00, False),
	('Specialty Pizza', 1.50, False),
	('Gameday Special', 20, True);
	
INSERT INTO size_crust (SizeCrustSize, SizeCrustCrust, SizeCrustPrice, SizeCrustCost)
VALUES
	('small', 'Thin', 3, 0.5),
	('small', 'Original', 3, 0.75),
	('small', 'Pan', 3.5, 1),
	('small', 'Gluten-Free', 4, 2),
	('medium', 'Thin', 5, 1),
	('medium', 'Original', 5, 1.5),
	('medium', 'Pan', 6, 2.25),
	('medium', 'Gluten-Free', 6.25, 3),
	('large', 'Thin', 8, 1.25),
	('large', 'Original', 8, 2),
	('large', 'Pan', 9, 3),
	('large', 'Gluten-Free', 9.5, 4),
	('x-large', 'Thin', 10, 2),
	('x-large', 'Original', 10, 3),
	('x-large', 'Pan', 11.5, 4.5),
	('x-large', 'Gluten-Free', 12.5, 6);

INSERT INTO customer (CustomerFirstName, CustomerLastName, CustomerPhoneNum)
VALUES
	('Anonymous', 'Anonymous', ''),
	('Ellis', 'Beck', '864-254-5861'),
	('Kurt', 'McKinney', '864-474-9953'),
	('Calvin', 'Sanders', '864-232-8944'),
	('Lance', 'Benton', '864-878-5679');
	
INSERT INTO customer_order (CustOrderCustID, CustOrderOrderedAt, CustOrderCost, CustOrderPrice, CustOrderIsComplete, CustOrderIsPickup, CustOrderIsDelivery, CustOrderIsDineIn)
VALUES
	(1, '2023-03-05 12:03:00', 3.68, 11.50, True, False, False, True),
	(1, '2023-03-03 12:05:00', 4.63, 14.85, True, False, False, True),
	(2, '2023-03-03 21:30:00', 19.80, 64.50, True, True, False, False),
	(2, '2023-03-05 19:11:00', 16.82, 34.90, True, False, True, False),
	(3, '2023-03-02 17:30:00', 7.85, 15.35, True, True, False, False),
	(4, '2023-03-02 18:17:00', 3.20, 13.25, True, False, True, False),
	(5, '2023-03-06 20:32:00', 6.30, 20.40, True, False, True, False);

INSERT INTO dine_in (DineInCustOrderID, DineInTableNum)
VALUES (1, 14), (2, 4);

INSERT INTO delivery (DeliveryCustOrderID, DeliveryFirstName, DeliveryLastName, DeliveryAddress)
VALUES
	(4, 'Ellis', 'Beck', '115 Party Blvd, Anderson, SC, 29621'),
	(6, 'Calvin', 'Sanders', '6745 Wessex St, Anderson, SC, 29621'),
	(7, 'Lance', 'Benton', '8879 Suburban Home, Anderson, SC, 29621');

INSERT INTO pickup VALUES (3, True), (5, True);

INSERT INTO pizza (PizzaCustOrderID, PizzaSize, PizzaCrust, PizzaIsDone, PizzaPrice, PizzaCost)
VALUES
	(1, 'large' ,'Thin', True, 13.50, 3.68),
	(2, 'medium', 'Pan', True, 10.60, 3.23),
	(2, 'small', 'Original', True, 6.75, 1.40),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(3, 'large', 'Original', True, 10.75, 3.30),
	(4, 'x-large', 'Original', True, 14.50, 5.59),
	(4, 'x-large', 'Original', True, 15.50, 5.59),
	(4, 'x-large', 'Original', True, 14.00, 5.68),
	(5, 'x-large', 'Gluten-Free', True, 15.35, 7.85),
	(6, 'large', 'Thin', True, 13.25, 3.20),
	(7, 'large', 'Thin', True, 12.00, 3.75),
	(7, 'large', 'Thin', True, 12.00, 2.55);

INSERT INTO pizza_topping (PizzaTopPizzaID, PizzaTopToppingID, PizzaTopIsExtra)
VALUES
	(1, 13, True),
	(1, 1, False),
	(1, 2, False),
	(2, 15, False),
	(2, 9, False),
	(2, 7, False),
	(2, 8, False),
	(2, 12, False),
	(3, 13, False),
	(3, 4, False),
	(3, 12, False),
	(4, 13, False),
	(4, 1, False),
	(5, 13, False),
	(5, 1, False),
	(6, 13, False),
	(6, 1, False),
	(7, 13, False),
	(7, 1, False),
	(8, 13, False),
	(8, 1, False),
	(9, 13, False),
	(9, 1, False),
	(10, 1, False),
	(10, 2, False),
	(10, 14, False),
	(11, 3, True),
	(11, 10, True),
	(11, 14, False),
	(12, 11, False),
	(12, 17, False),
	(12, 14, False),
	(13, 5, False),
	(13, 6, False),
	(13, 7, False),
	(13, 8, False),
	(13, 9, False),
	(13, 16, False),
	(14, 4, False),
	(14, 5, False),
	(14, 6, False),
	(14, 8, False),
	(14, 14, True),
	(15, 14, True),
	(16, 13, False),
	(16, 1, True);

INSERT INTO discount_order (DiscOrderDiscountID, DiscOrderCustOrderID)
VALUES (3, 1), (2, 2), (4, 2), (5, 4), (1, 7);

INSERT INTO discount_pizza (DiscPizzaDiscountID, DiscPizzaPizzaID)
VALUES (4, 11), (4, 13);


