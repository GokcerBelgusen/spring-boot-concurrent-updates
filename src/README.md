#
# Simple Spring Boot Application running Optimistic & Pessimistic Locks on PosgreSQL - having read committed isolation level + row level lock
#

INSERT INTO Customer (id,name, credit_limit, current_credit, version) VALUES (1,'John Doe', 5000.00, 10000.00, 1);
INSERT INTO Customer (id,name, credit_limit, current_credit, version) VALUES (2,'Jane Smith', 7000.00, 10000.00, 1);

psql postgres
CREATE USER admin WITH PASSWORD 'admin';
CREATE DATABASE customerdb OWNER admin;
GRANT ALL PRIVILEGES ON DATABASE customerdb TO admin;
/q