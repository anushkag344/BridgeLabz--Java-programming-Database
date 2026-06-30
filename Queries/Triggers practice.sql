---triggers 
CREATE TABLE student(
id INT,
name VARCHAR(50)
);
--create log table 
CREATE TABLE student_log(
message TEXT
);
--function 
CREATE OR REPLACE FUNCTION log_insert()
RETURNS TRIGGER AS $$
BEGIN 
INSERT INTO student_log
VALUES("New student added");
RETURN NEW;
END;
$$ LANGUAGE plpsql;
---create trigger 
CREATE TRIGGER student-insert_trigger
AFTER INSERT 
ON student 
FOR EACH ROW
EXECUTE FUNCTION log_insert();
---test
INSERT INTO student
Values(1,'Anushka');
----
SELECT * FROM student_log;