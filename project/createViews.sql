-- Kita Pairojtanachai, Siddarth Malladi

DROP VIEW IF EXISTS ToppingPopularity;
CREATE VIEW ToppingPopularity AS
	SELECT ToppingName AS Topping, SUM(IF(PizzaTopIsExtra, 2, 1)) AS ToppingCount
	FROM topping
		LEFT JOIN pizza_topping
			ON ToppingID = PizzaTopToppingID
	GROUP BY ToppingName
	ORDER BY ToppingCount DESC;
SELECT * FROM ToppingPopularity;
	
	
DROP VIEW IF EXISTS ProfitByPizza;
CREATE VIEW ProfitByPizza AS
	SELECT 
		PizzaSize AS 'Pizza Size', 
		PizzaCrust AS 'Pizza Crust', 
		FORMAT(SUM(PizzaPrice - PizzaCost), 2) AS Profit,
		DATE_FORMAT(MAX(CustOrderOrderedAt), '%M-%e-%Y') AS LastOrderDate
	FROM pizza
		LEFT JOIN customer_order
			ON PizzaCustOrderID = CustOrderID
		GROUP BY PizzaSize, PizzaCrust
		ORDER BY SUM(PizzaPrice - PizzaCost) DESC;
SELECT * FROM ProfitByPizza;

	
DROP VIEW IF EXISTS ProfitByOrderType;
CREATE VIEW ProfitByOrderType AS
	(SELECT
		CASE
			WHEN CustOrderIsPickup THEN 'pickup'
			WHEN CustOrderIsDelivery THEN 'delivery'
			WHEN CustOrderIsDineIn THEN 'dinein'
		END AS CustomerType,
		DATE_FORMAT(CustOrderOrderedAt, '%Y-%M') AS OrderMonth,
		SUM(CustOrderPrice) AS TotalOrderPrice,
		SUM(CustOrderCost) AS TotalOrderCost,
		SUM(CustOrderPrice - CustOrderCost) AS Profit
	FROM customer_order
	GROUP BY CustOrderIsPickup, CustOrderIsDelivery, CustOrderIsDineIn, OrderMonth)
	UNION
	(SELECT 
		'' AS CustomerType,
		'Grand Total' AS OrderMonth,
		SUM(CustOrderPrice) AS TotalOrderPrice,
		SUM(CustOrderCost) AS TotalOrderCost,
		SUM(CustOrderPrice - CustOrderCost) AS Profit
	FROM customer_order)
	ORDER BY OrderMonth;
SELECT * FROM ProfitByOrderType;
		
		