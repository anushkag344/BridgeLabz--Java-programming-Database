
DROP TABLE IF EXISTS STOCKS;

CREATE TABLE STOCKS (
    shipId INT PRIMARY KEY,
    stockCapacity FLOAT,
    chemical VARCHAR(50),
    destination VARCHAR(50),
    destinationId INT
);

INSERT INTO STOCKS
(shipId, stockCapacity, chemical, destination, destinationId)
VALUES
(11, 5000.0, 'Ethanol', 'Mumbai', 1),
(12, 6000.0, 'Methanol', 'Delhi', 2),
(13, 1000.0, 'XYZ', 'Chennai', 3),
(14, 7000.0, 'Alcohol', 'Kolkata', 4),
(15, 5500.0, 'Benzene', 'Hyderabad', 5),
(16, 1000.0, 'Ethanol', 'Pune', 6),
(17, 2000.0, 'ABC', 'Jhansi', 7),
(18, 1500.0, 'DEF', 'Jaipur', 8),
(19, 180.0, 'Phenol', 'Lucknow', 9),
(20, 2500.0, 'Methanol', 'Bengaluru', 10);


ALTER TABLE STOCKS
ADD COLUMN status VARCHAR(20);

CREATE OR REPLACE FUNCTION hazardous_trigger()
RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.stockCapacity < 1000 THEN
        NEW.status := 'Hazardous';
    ELSE
        NEW.status := 'Non-Hazardous';
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_hazardous
BEFORE INSERT
ON STOCKS
FOR EACH ROW
EXECUTE FUNCTION hazardous_trigger();

UPDATE STOCKS
SET status = CASE
    WHEN stockCapacity > 1000 THEN 'Hazardous'
    ELSE 'Non-Hazardous'
END;

SELECT * FROM STOCKS;