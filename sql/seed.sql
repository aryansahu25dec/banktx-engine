USE banktx;

INSERT INTO customer (customer_id, name, contact) VALUES
 ('CUST01','Aarav Sharma','aarav@mail.com'),
 ('CUST02','Priya Patel','priya@mail.com'),
 ('CUST03','Rohan Verma','rohan@mail.com'),
 ('CUST04','Neha Gupta','neha@mail.com');

INSERT INTO account (account_id, customer_id, balance, status) VALUES
 ('ACC1001','CUST01',50000.00,'ACTIVE'),
 ('ACC2004','CUST02',32500.00,'ACTIVE'),
 ('ACC3009','CUST03',10000.00,'ACTIVE'),
 ('ACC4012','CUST04',15000.00,'INACTIVE');