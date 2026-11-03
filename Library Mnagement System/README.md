# 📚 Library Management System

> A console-based Java application to manage books and library transactions using a MySQL database.
> Built as **Task 2 (Easy)** of the **Auspify Technologies Software Development Internship**.

---

## 📌 Project Name

**Library Management System (Java + MySQL)**

---

## 📝 Project Description

The Library Management System helps a library manage its books and track which books are issued to which members. It replaces manual registers with a database-driven application.

The system stores book details, keeps track of available copies, records every issue and return, and generates useful reports. Data is stored permanently in a **MySQL database** with two related tables (`books` and `issue_records`) connected using a **foreign key**.

Issue and return operations use **database transactions (commit / rollback)**, so the book count and the issue record are always updated together and the data never becomes inconsistent.

---

## 🛠️ Technologies Used

| Component | Technology |
|---|---|
| Programming Language | Java (JDK 8 or above) |
| Database | MySQL |
| Database Connectivity | JDBC (MySQL Connector/J 8.0.33) |
| Database Tool | MySQL Workbench |
| Editor / IDE | Notepad / VS Code |
| Version Control | Git & GitHub |

---

## ✨ Features

- ➕ **Add Book:** Store book ID, title, author, and number of copies
- 📋 **View All Books:** Display all books with total and available copies
- 🔍 **Search Book:** Find books by title or author (full or partial match)
- 📤 **Issue Book:** Issue a book to a member (available copies reduce automatically)
- 📥 **Return Book:** Mark a book as returned (available copies increase automatically)
- 📊 **Reports:**
  - Summary (total titles, total copies, available, issued)
  - Currently issued books
  - Complete issue / return history
- ✅ **Validation:** Handles duplicate Book ID, empty fields, invalid numbers, unavailable books, and already returned books
- 🔄 **Transactions:** Uses commit and rollback to keep data consistent
- 🔒 **Secure Queries:** Uses `PreparedStatement` to prevent SQL injection

---

## 🗄️ Database Structure

**Database:** `library_db`

### Table: `books`

| Column | Data Type | Description |
|---|---|---|
| book_id | INT | Primary Key |
| title | VARCHAR(150) | Book title (required) |
| author | VARCHAR(100) | Author name |
| total_copies | INT | Total copies owned by the library |
| available_copies | INT | Copies currently on the shelf |

### Table: `issue_records`

| Column | Data Type | Description |
|---|---|---|
| issue_id | INT | Primary Key (auto increment) |
| book_id | INT | Foreign Key → `books.book_id` |
| member_name | VARCHAR(100) | Name of the member |
| issue_date | DATE | Date the book was issued |
| return_date | DATE | Date returned (`NULL` until returned) |

> Both tables are created automatically when the program runs for the first time.

---

## 🔄 How Issue and Return Work

**Issue a book**
1. Check that `available_copies` is greater than 0
2. Reduce `available_copies` by 1
3. Insert a new row in `issue_records`
4. Commit (if anything fails, everything is rolled back)

**Return a book**
1. Check that the issue record exists and is not already returned
2. Set `return_date` to today's date
3. Increase `available_copies` by 1
4. Commit (if anything fails, everything is rolled back)

---

## 📁 Project Structure

```
library-management-system/
 ├── LibraryApp.java
 ├── README.md
 └── screenshots/
      ├── home.png
      ├── add_book.png
      ├── view_books.png
      ├── search.png
      ├── issue.png
      ├── return.png
      └── reports.png
```

---

## 📸 Screenshots

### Home Menu
![Home Menu](screenshots/home.png)

### Add Book
![Add Book](screenshots/add_book.png)

### View All Books
![View Books](screenshots/view_books.png)

### Search Book
![Search Book](screenshots/search.png)

### Issue Book
![Issue Book](screenshots/issue.png)

### Return Book
![Return Book](screenshots/return.png)

### Reports
![Reports](screenshots/reports.png)

---

## ⚙️ Installation Steps

### 1. Install the prerequisites

- **JDK** (Java Development Kit). Check with:
  ```
  java -version
  ```
- **MySQL Server** and **MySQL Workbench**
- **MySQL Connector/J** jar file (`mysql-connector-j-8.0.33.jar`)

### 2. Clone the repository

```
git clone https://github.com/ansh-zero/library-management-system.git
cd library-management-system
```

*(Or simply download the project as a ZIP and extract it.)*

### 3. Add the MySQL Connector

Download `mysql-connector-j-8.0.33.jar` and place it in the same folder as `LibraryApp.java`:

```
https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
```

### 4. Create the database

Open MySQL Workbench and run:

```sql
CREATE DATABASE library_db;
```

### 5. Configure your MySQL credentials

Open `LibraryApp.java` and update these lines with your own details:

```java
static final String URL = "jdbc:mysql://localhost:3306/library_db";
static final String USER = "root";
static final String PASSWORD = "your_password";
```

---

## ▶️ How to Run the Project

Open Command Prompt / Terminal inside the project folder.

**Step 1: Compile**
```
javac LibraryApp.java
```

**Step 2: Run**

Windows:
```
java -cp ".;mysql-connector-j-8.0.33.jar" LibraryApp
```

Mac / Linux:
```
java -cp ".:mysql-connector-j-8.0.33.jar" LibraryApp
```

On successful connection you will see:

```
MySQL se connect ho gaya!

===== Library Management System =====
1. Add Book
2. View All Books
3. Search Book
4. Issue Book
5. Return Book
6. Reports
7. Exit
```

> **Note:** MySQL Server must be running. If you get `Communications link failure`, start the MySQL service (`net start MySQL80` on Windows).

---

## 🧪 Testing Performed

- Added multiple books, including one with a single copy
- Viewed all books and searched by title and author
- Issued a book and verified that available copies decreased
- Tried to issue a book with no copies left (blocked correctly)
- Returned a book and verified that available copies increased
- Tried to return the same issue ID twice (blocked correctly)
- Generated all three reports
- Verified both tables in MySQL Workbench using `SELECT * FROM books;` and `SELECT * FROM issue_records;`

---

## 🚀 Future Improvements

- Java Swing GUI
- Member management and login system
- Due dates with automatic fine calculation
- Export reports to CSV / PDF

---

## 📚 What I Learned

- Designing a database with two related tables and a foreign key
- Using `JOIN` queries to combine data from multiple tables
- Handling transactions with `commit` and `rollback`
- Writing secure queries with `PreparedStatement`
- Generating reports from database data
- Validating input and testing a database application

---

## 📬 Contact

**GitHub:** [https://github.com/ansh-zero](https://github.com/ansh-zero)

Feel free to open an issue or reach out for suggestions and feedback.

---

## 🏢 About the Internship

This project was completed as part of the **Software Development Internship** at **Auspify Technologies**.

`#Auspify` `#AuspifyTechnologies` `#AuspifyInternship` `#AuspifyProjects`
