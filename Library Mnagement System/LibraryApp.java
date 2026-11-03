import java.sql.*;
import java.util.Scanner;

public class LibraryApp {

    
    static final String URL = "jdbc:mysql://localhost:3306/library_db";
    static final String USER = "root";
    static final String PASSWORD = "mera_asli_password";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("MySQL se connect ho gaya!");
            createTables();

            while (true) {
                System.out.println("\n===== Library Management System =====");
                System.out.println("1. Add Book");
                System.out.println("2. View All Books");
                System.out.println("3. Search Book");
                System.out.println("4. Issue Book");
                System.out.println("5. Return Book");
                System.out.println("6. Reports");
                System.out.println("7. Exit");
                System.out.print("Choice: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": addBook(); break;
                    case "2": viewBooks(); break;
                    case "3": searchBook(); break;
                    case "4": issueBook(); break;
                    case "5": returnBook(); break;
                    case "6": reports(); break;
                    case "7":
                        conn.close();
                        System.out.println("Bye!");
                        return;
                    default: System.out.println("Galat choice!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void createTables() throws SQLException {
        String books = "CREATE TABLE IF NOT EXISTS books ("
                + "book_id INT PRIMARY KEY, "
                + "title VARCHAR(150) NOT NULL, "
                + "author VARCHAR(100), "
                + "total_copies INT NOT NULL, "
                + "available_copies INT NOT NULL)";

        String records = "CREATE TABLE IF NOT EXISTS issue_records ("
                + "issue_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "book_id INT NOT NULL, "
                + "member_name VARCHAR(100) NOT NULL, "
                + "issue_date DATE NOT NULL, "
                + "return_date DATE NULL, "
                + "FOREIGN KEY (book_id) REFERENCES books(book_id))";

        try (Statement st = conn.createStatement()) {
            st.execute(books);
            st.execute(records);
        }
    }

    static int readInt(String msg) {
        System.out.print(msg);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Number likho!");
            return -1;
        }
    }

    // ---------- BOOK MANAGEMENT ----------
    static void addBook() {
        int id = readInt("Book ID: ");
        if (id < 0) return;
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        int copies = readInt("Copies: ");
        if (copies <= 0) {
            System.out.println("Copies 1 ya usse zyada honi chahiye!");
            return;
        }
        if (title.isEmpty()) {
            System.out.println("Title khali nahi ho sakta!");
            return;
        }

        String sql = "INSERT INTO books VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setInt(4, copies);
            ps.setInt(5, copies);
            ps.executeUpdate();
            System.out.println("Book add ho gayi!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Ye Book ID pehle se hai!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewBooks() {
        String sql = "SELECT * FROM books ORDER BY book_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            printBooks(rs);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void searchBook() {
        System.out.print("Title ya Author likho: ");
        String key = sc.nextLine().trim();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");
            try (ResultSet rs = ps.executeQuery()) {
                printBooks(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void printBooks(ResultSet rs) throws SQLException {
        System.out.printf("%-8s %-30s %-20s %-8s %-10s%n",
                "ID", "Title", "Author", "Total", "Available");
        System.out.println("---------------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-8d %-30s %-20s %-8d %-10d%n",
                    rs.getInt("book_id"), rs.getString("title"),
                    rs.getString("author"), rs.getInt("total_copies"),
                    rs.getInt("available_copies"));
        }
        if (!found) System.out.println("Koi book nahi mili.");
    }

    // ---------- ISSUE ----------
    static void issueBook() {
        int bookId = readInt("Book ID: ");
        if (bookId < 0) return;
        System.out.print("Member ka naam: ");
        String member = sc.nextLine().trim();
        if (member.isEmpty()) {
            System.out.println("Member ka naam khali nahi ho sakta!");
            return;
        }

        String reduce = "UPDATE books SET available_copies = available_copies - 1 "
                      + "WHERE book_id=? AND available_copies > 0";
        String record = "INSERT INTO issue_records (book_id, member_name, issue_date) "
                      + "VALUES (?,?,CURDATE())";
        try {
            conn.setAutoCommit(false);   // dono kaam ek saath honge
            try (PreparedStatement ps1 = conn.prepareStatement(reduce);
                 PreparedStatement ps2 = conn.prepareStatement(record)) {

                ps1.setInt(1, bookId);
                if (ps1.executeUpdate() == 0) {
                    conn.rollback();
                    System.out.println("Book nahi mili ya sab copies issue ho chuki hain.");
                    return;
                }
                ps2.setInt(1, bookId);
                ps2.setString(2, member);
                ps2.executeUpdate();
                conn.commit();
                System.out.println("Book issue ho gayi!");
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.out.println("Error: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ---------- RETURN ----------
    static void returnBook() {
        showCurrentlyIssued();
        int issueId = readInt("Kaunsi Issue ID wapas aa rahi hai? ");
        if (issueId < 0) return;

        String find = "SELECT book_id FROM issue_records WHERE issue_id=? AND return_date IS NULL";
        String mark = "UPDATE issue_records SET return_date=CURDATE() WHERE issue_id=?";
        String add  = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement psFind = conn.prepareStatement(find)) {
                psFind.setInt(1, issueId);
                int bookId;
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Ye Issue ID nahi mili, ya book pehle hi return ho chuki hai.");
                        return;
                    }
                    bookId = rs.getInt("book_id");
                }
                try (PreparedStatement ps1 = conn.prepareStatement(mark);
                     PreparedStatement ps2 = conn.prepareStatement(add)) {
                    ps1.setInt(1, issueId);
                    ps1.executeUpdate();
                    ps2.setInt(1, bookId);
                    ps2.executeUpdate();
                    conn.commit();
                    System.out.println("Book return ho gayi!");
                }
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.out.println("Error: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ---------- REPORTS ----------
    static void reports() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. Summary");
        System.out.println("2. Abhi issue ki hui books");
        System.out.println("3. Poori history");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": summaryReport(); break;
            case "2": showCurrentlyIssued(); break;
            case "3": historyReport(); break;
            default: System.out.println("Galat choice!");
        }
    }

    static void summaryReport() {
        String sql = "SELECT COUNT(*) AS titles, SUM(total_copies) AS total, "
                   + "SUM(available_copies) AS available FROM books";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int available = rs.getInt("available");
                System.out.println("Total titles      : " + rs.getInt("titles"));
                System.out.println("Total copies      : " + total);
                System.out.println("Available copies  : " + available);
                System.out.println("Issued copies     : " + (total - available));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void showCurrentlyIssued() {
        String sql = "SELECT r.issue_id, b.title, r.member_name, r.issue_date "
                   + "FROM issue_records r JOIN books b ON r.book_id = b.book_id "
                   + "WHERE r.return_date IS NULL ORDER BY r.issue_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-10s %-30s %-20s %-12s%n", "Issue ID", "Book", "Member", "Issue Date");
            System.out.println("--------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d %-30s %-20s %-12s%n",
                        rs.getInt("issue_id"), rs.getString("title"),
                        rs.getString("member_name"), rs.getDate("issue_date"));
            }
            if (!found) System.out.println("Abhi koi book issue nahi hai.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void historyReport() {
        String sql = "SELECT r.issue_id, b.title, r.member_name, r.issue_date, r.return_date "
                   + "FROM issue_records r JOIN books b ON r.book_id = b.book_id "
                   + "ORDER BY r.issue_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-10s %-25s %-18s %-12s %-12s%n",
                    "Issue ID", "Book", "Member", "Issued", "Returned");
            System.out.println("-----------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                Date ret = rs.getDate("return_date");
                System.out.printf("%-10d %-25s %-18s %-12s %-12s%n",
                        rs.getInt("issue_id"), rs.getString("title"),
                        rs.getString("member_name"), rs.getDate("issue_date"),
                        ret == null ? "Not returned" : ret.toString());
            }
            if (!found) System.out.println("Abhi koi record nahi hai.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}