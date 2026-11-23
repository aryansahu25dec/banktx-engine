CREATE DATABASE IF NOT EXISTS banktx;
USE banktx;

CREATE TABLE customer (
  customer_id VARCHAR(20) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  contact VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account (
  account_id VARCHAR(20) PRIMARY KEY,
  customer_id VARCHAR(20) NOT NULL,
  balance DECIMAL(15,2) NOT NULL DEFAULT 0,
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT chk_balance CHECK (balance >= 0),
  CONSTRAINT fk_acc_cust FOREIGN KEY (customer_id)
    REFERENCES customer(customer_id),
  INDEX idx_acc_customer (customer_id)
);

CREATE TABLE transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id VARCHAR(30) NOT NULL,
  type ENUM('DEPOSIT','WITHDRAWAL','TRANSFER') NOT NULL,
  source_account VARCHAR(20),
  destination_account VARCHAR(20),
  amount DECIMAL(15,2) NOT NULL,
  status ENUM('SUCCESS','FAILED','REJECTED') NOT NULL,
  reason VARCHAR(255),
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_txn_id UNIQUE (transaction_id),
  CONSTRAINT chk_amount CHECK (amount > 0),
  CONSTRAINT fk_txn_src FOREIGN KEY (source_account) REFERENCES account(account_id),
  CONSTRAINT fk_txn_dst FOREIGN KEY (destination_account) REFERENCES account(account_id),
  INDEX idx_txn_src (source_account),
  INDEX idx_txn_dst (destination_account),
  INDEX idx_txn_created (created_at)
);

CREATE TABLE transaction_audit (
  audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id VARCHAR(30) NOT NULL,
  event_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  thread_name VARCHAR(60),
  operation VARCHAR(20),
  result VARCHAR(20),
  duration_ms INT,
  CONSTRAINT fk_audit_txn FOREIGN KEY (transaction_id)
    REFERENCES transactions(transaction_id),
  INDEX idx_audit_txn (transaction_id)
);

CREATE TABLE failed_transaction (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id VARCHAR(30) NOT NULL,
  error_category VARCHAR(40) NOT NULL,
  reason VARCHAR(255) NOT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_fail_txn FOREIGN KEY (transaction_id)
    REFERENCES transactions(transaction_id),
  INDEX idx_fail_category (error_category)
);