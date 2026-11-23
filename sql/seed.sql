USE banktx;

INSERT INTO customer (customer_id, name, contact) VALUES
 ('CUST1','Asha Rao','asha@mail.com'),
 ('CUST2','Ravi Kumar','ravi@mail.com'),
 ('CUST3','Meera Nair','meera@mail.com');

INSERT INTO account (account_id, customer_id, balance, status) VALUES
 ('ACC1001','CUST1',50000.00,'ACTIVE'),
 ('ACC1002','CUST1',20000.00,'ACTIVE'),
 ('ACC2004','CUST2',10000.00,'ACTIVE'),
 ('ACC2005','CUST2',0.00,'INACTIVE'),
 ('ACC3001','CUST3',75000.00,'ACTIVE'),
 ('ACC3002','CUST3',10000.00,'ACTIVE');