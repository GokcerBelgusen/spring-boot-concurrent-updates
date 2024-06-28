#
#  Optimistic & Pessimistic Locks example 
#

Spring boot application with postgresql having read committed isolation level + row level lock

# To install and start postgreSQL by homebrew

brew install postgresql
brew services list
brew services start postgresql@14

# To create simple table 

psql postgres
CREATE USER admin WITH PASSWORD 'admin';
CREATE DATABASE customerdb OWNER admin;
GRANT ALL PRIVILEGES ON DATABASE customerdb TO admin;
/q


# To insert simple data 

INSERT INTO Customer (id,name, credit_limit, current_credit, version) VALUES (1,'John Doe', 5000.00, 10000.00, 1);

INSERT INTO Customer (id,name, credit_limit, current_credit, version) VALUES (2,'Jane Smith', 7000.00, 10000.00, 1);


+-------------------+-------------+----------------------+---------------+-------------------+
| Isolation Level   | Dirty Reads | Non-Repeatable Reads | Phantom Reads | Oracle Equivalent |
+-------------------+-------------+----------------------+---------------+-------------------+
| READ COMMITTED    | Prevented   | May Occur            | May Occur     | Default           |
| READ UNCOMMITTED  | May Occur   | May Occur            | May Occur     | Not Supported     |
| REPEATABLE READ   | Prevented   | Prevented            | May Occur     | SERIALIZABLE      |
| SERIALIZABLE      | Prevented   | Prevented            | Prevented     | SERIALIZABLE      |
| NONE              | N/A         | N/A                  | N/A           | Not Supported     |
+-------------------+-------------+----------------------+---------------+-------------------+
