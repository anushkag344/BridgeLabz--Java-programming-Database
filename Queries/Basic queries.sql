--create a table
CREATE TABLE cars(
brand VARCHAR(255),
model VARCHAR(255),
year INT
);
--display car 
SELECT * FROM cars;
--insert into 
INSERT INTO cars(brand,model,year)
VALUES('Ford','Mustang',2004);
--insert multiple rows
INSERT INTO cars(brand,model,year)
VALUES
('Volvo','p1234',2003),
('KIA','MI',2006),
('Toyota','Celica',2007);
--specify columns
SELECT brand,year FROM cars;
--alter table 
ALTER TABLE cars
ADD color VARCHAR(255);
--DISPLAY
SELECT * FROM cars;
--update
UPDATE cars
SET color='red'
where brand='Volvo';
--
Select * from cars;
--alter column
ALTER TABLE cars
ALTER COLUMN year TYPE varchar(4);
--DROP 
ALTER TABLE cars
DROP COLUMN color;
--
SELECT * FROM cars;
--delete 
DELETE FROM cars
WHERE brand='volvo';
--
SELECT * FROM cars;
--delete all records
DELETE FROM cars;
--archieved 
TRUNCATE TABLE cars;
--drop 
DROP TABLE cars;
---operators
--NOT EQUAL <> ,!=
SELECT * FROM cars
WHERE brand <> 'Volvo';

--and
SELECT * FROM cars 
WHERE brand='Volvo' AND year=1968;
--
SELECT * FROM cars
WHERE brand='Volvo' OR year=1968;
----IN 
SELECT * FROM cars
WHERE brand IN('Volvo','Mercedes','Ford');
--
SELECT * FROM cars
WHERE brand IS NULL;

---
SELECT column_name,country FROM customers;
--distinct 
SELECT DISTINCT country FROM customers;
---count(distinct)
SELECT COUNT(DISTINCT country)FROM customers; 
---where clause is used to filter records 
SELECT * FROM customers
WHERE city='London';
---order by 
SELECT * FROM cars
ORDER BY price;
SELECT * FROM cars
ORDER BY price DESC;
-----LIMIT 
SELECT * FROM cars
LIMIT 20;
--offset
SELECT * FROM cars
LIMIT 20 OFFSET 40;
---MIN
SELECT MIN(price)
FROM products;
----lowest price and name the column as lowest_price
SELECT MIN(price) AS lowest_price
FROM products;
---
SELECT COUNT(cutomer_id)
FROM cutomers
WHERE city='LONDON';
---record that contain specific letter 
SELECT * FROM Customers
WHERE customer_name LIKE '%A%';
--not start with M
SELECT * FROM cars
WHERE brand NOT LIKE 'M%';
--starts with M
SELECT * FROM cars
WHERE brand LIKE "M%";
---ends with en
SELECT * FROM customer
WHERE customer_name LIKE '%en';
--- underscore(_)-represent single character 
SELECT * FROM customers
WHERE city LIKE 'L_nd__';
----
SELECT * FROM CUSTOMERS 
WHERE cutomer_id IN(SELECT customer_id FROM orders);













