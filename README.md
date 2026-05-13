# Speedrun Management System

A prototype for managing speedrun submissions using **Java (JDBC)** and **Oracle Database**. It demonstrates transaction management, Personal Best (PB) validation, and the use of **Stored Procedures with JSON** parsing.

## Prerequisites
- **Docker & Docker Compose** (for the Oracle Database)
- **Java JDK 17+**

## Setup and Run

### 1. Clone the repository
```bash
git clone https://github.com/Dustix1/ds2-projekt.git
cd ds2-projekt
```

### 2. Start the Oracle Database
This will start the database and automatically run `files/init.sql` to setup tables and procedures.
```bash
docker-compose up -d
```
*Wait ~3-5 minutes for the database to fully initialize on the first run.*

### 3. Compile the application
```bash
mkdir -p bin
javac -cp "lib/ojdbc17.jar:src" -d bin src/Program.java src/orm/dao/*.java src/orm/dto/*.java
```

### 4. Run the application
```bash
java -cp "bin:lib/ojdbc17.jar" Program
```

## Features Demonstrated
- **F12 Requirement:** `FinishRun` logic implemented in both pure Java (`TransactionsDao.finishRun`) and as an Oracle Stored Procedure (`FinishRun` in PL/SQL).
- **JSON Support:** Passing a list of splits as a JSON string to the database and parsing it using `JSON_TABLE`.
- **Validation:** 
  - Prevents submitting runs slower than the current Personal Best.
  - Validates that the number of splits matches the category requirements.
- **Transaction Safety:** Uses manual transaction control (Begin/Commit/Rollback) to ensure data consistency.
