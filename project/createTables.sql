-- Kita Pairojtanachai, Siddarth Malladi

CREATE TABLE size_crust (
	SizeCrustSize VARCHAR(32) NOT NULL,
	SizeCrustCrust VARCHAR(32) NOT NULL,
	SizeCrustPrice DECIMAL(8,2) NOT NULL,
	SizeCrustCost DECIMAL(8,2) NOT NULL,
	PRIMARY KEY (SizeCrustSize, SizeCrustCrust) 
);

CREATE TABLE topping (
	ToppingID INT NOT NULL AUTO_INCREMENT,
	ToppingName VARCHAR(128) NOT NULL,
	ToppingPrice DECIMAL(8,2) NOT NULL,
	ToppingCost DECIMAL(8,2) NOT NULL,
	ToppingInventory DECIMAL(8,2) NOT NULL,
	ToppingMinInventory DECIMAL(8,2),
	ToppingAmtSmall DECIMAL(8,2) NOT NULL,
	ToppingAmtMedium DECIMAL(8,2) NOT NULL,
	ToppingAmtLarge DECIMAL(8,2) NOT NULL,
	ToppingAmtXLarge DECIMAL(8,2) NOT NULL,
	PRIMARY KEY (ToppingID)
);

CREATE TABLE customer (
	CustomerID INT NOT NULL AUTO_INCREMENT,
	CustomerFirstName VARCHAR(64) NOT NULL,
	CustomerLastName VARCHAR(64) NOT NULL,
	CustomerPhoneNum VARCHAR(15) NOT NULL,
	PRIMARY KEY (CustomerID)
);

CREATE TABLE customer_order (
	CustOrderID INT NOT NULL AUTO_INCREMENT,
	CustOrderCustID INT NOT NULL,
	CustOrderOrderedAt TIMESTAMP NOT NULL DEFAULT NOW(),
	CustOrderCost DECIMAL(8,2) NOT NULL,
	CustOrderPrice DECIMAL(8,2) NOT NULL,
	CustOrderIsComplete BOOL NOT NULL DEFAULT FALSE,
	CustOrderIsPickup BOOL NOT NULL,
	CustOrderIsDelivery BOOL NOT NULL,
	CustOrderIsDineIn BOOL NOT NULL,
	PRIMARY KEY (CustOrderID),
	FOREIGN KEY (CustOrderCustID) REFERENCES customer (CustomerID)
);

CREATE TABLE pizza (
	PizzaID INT NOT NULL AUTO_INCREMENT,
	PizzaCustOrderID INT NOT NULL,
	PizzaSize VARCHAR(32) NOT NULL,
	PizzaCrust VARCHAR(32) NOT NULL,
	PizzaIsDone BOOL NOT NULL DEFAULT FALSE,
	PizzaPrice DECIMAL(6,2) NOT NULL,
	PizzaCost DECIMAL(6,2) NOT NULL,
	PRIMARY KEY (PizzaID),
	FOREIGN KEY (PizzaCustOrderID) REFERENCES customer_order (CustOrderID),
	FOREIGN KEY (PizzaSize, PizzaCrust) REFERENCES size_crust (SizeCrustSize, SizeCrustCrust)
);

CREATE TABLE pizza_topping (
	PizzaTopPizzaID INT NOT NULL,
	PizzaTopToppingID INT NOT NULL,
	PizzaTopIsExtra BOOL NOT NULL DEFAULT FALSE,
	PRIMARY KEY (PizzaTopPizzaID, PizzaTopToppingID),
	FOREIGN KEY (PizzaTopPizzaID) REFERENCES pizza (PizzaID),
	FOREIGN KEY (PizzaTopToppingID) REFERENCES topping (ToppingID)
);

CREATE TABLE discount (
	DiscountID INT NOT NULL AUTO_INCREMENT,
	DiscountName VARCHAR(128) NOT NULL,
	DiscountAmount DECIMAL(6,2) NOT NULL,
	DiscountIsPercent BOOL NOT NULL DEFAULT FALSE,
	PRIMARY KEY (DiscountID)
);

CREATE TABLE discount_order (
	DiscOrderDiscountID INT NOT NULL,
	DiscOrderCustOrderID INT NOT NULL,
	PRIMARY KEY (DiscOrderDiscountID, DiscOrderCustOrderID),
	FOREIGN KEY (DiscOrderDiscountID) REFERENCES discount (DiscountID),
	FOREIGN KEY (DiscOrderCustOrderID) REFERENCES customer_order (CustOrderID)
);

CREATE TABLE discount_pizza (
	DiscPizzaDiscountID INT NOT NULL,
	DiscPizzaPizzaID INT NOT NULL,
	PRIMARY KEY (DiscPizzaDiscountID, DiscPizzaPizzaID),
	FOREIGN KEY (DiscPizzaDiscountID) REFERENCES discount (DiscountID),
	FOREIGN KEY (DiscPizzaPizzaID) REFERENCES pizza (PizzaID)
);

CREATE TABLE pickup (
	PickupCustOrderID INT NOT NULL,
	PickupIsPickedUp BOOL NOT NULL DEFAULT FALSE,
	PRIMARY KEY (PickupCustOrderID),
	FOREIGN KEY (PickupCustOrderID) REFERENCES customer_order (CustOrderID) ON DELETE CASCADE
);

CREATE TABLE delivery (
	DeliveryCustOrderID INT NOT NULL,
	DeliveryFirstName VARCHAR(64) NOT NULL,
	DeliveryLastName VARCHAR(64) NOT NULL,
	DeliveryAddress VARCHAR(512) NOT NULL,
	PRIMARY KEY (DeliveryCustOrderID),
	FOREIGN KEY (DeliveryCustOrderID) REFERENCES customer_order (CustOrderID) ON DELETE CASCADE
);

CREATE TABLE dine_in (
	DineInCustOrderID INT NOT NULL,
	DineInTableNum INT NOT NULL,
	PRIMARY KEY (DineInCustOrderID),
	FOREIGN KEY (DineInCustOrderID) REFERENCES customer_order (CustOrderID) ON DELETE CASCADE
);
