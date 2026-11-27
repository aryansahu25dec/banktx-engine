\# BankTx Engine

Multi-threaded banking transaction processing system (Java 17, Spring Boot, MySQL).



\## Setup

1\. Run `sql/schema.sql`, then `sql/seed.sql`, in MySQL.

2\. Set the DB password: `set DB\_PASSWORD=<your mysql password>`

3\. Start the backend: `mvnw.cmd spring-boot:run` (port 8080)



\## Progress log

\- Day 1: Spring Boot project, MySQL schema, seed data, CORS config

\- Day 2: models, custom exceptions, JDBC repositories, GET /api/accounts

\- Day 3: transaction service (deposit/withdraw/transfer), POST /api/transactions

