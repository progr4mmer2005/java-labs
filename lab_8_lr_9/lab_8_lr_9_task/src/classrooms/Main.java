package classrooms;

/*
 * Лабораторная работа №9 (по счёту — №8). Вариант 56.
 *
 * Число варианта: 928
 * Двоичное (10 бит): 1 1 1 0 1 0 0 0 0 0
 *
 * П.5  = 1 — консольное приложение
 * П.6  = 1 — сброс к значениям по умолчанию из ФАЙЛА (defaults.txt)
 * П.7  = 1 — вывод значений второй таблицы в лекс. порядке
 * П.8  = 0 — первое поле второй таблицы (ФИО ответственного)
 * П.10 = 100 (д) — вывести телефонный справочник (ФИО, телефон) в лекс. порядке;
 *                   найти среднюю площадь закреплённую за ответственными
 * П.14 = 0 — БД «Учебные аудитории»
 * П.15 = 0 — выбор записи по порядковому номеру в общем списке
 *
 * БД: ClassroomsDB (Apache Derby, network mode)
 * Таблица 1 — ROOMS:       {ID_ROOM, BUILDING, ROOM_NUMBER, NAME, AREA, ID_RESPONSIBLE(FK)}
 * Таблица 2 — RESPONSIBLES: {ID_RESPONSIBLE, FIO, POSITION, TELEPHONE, AGE}
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

    private static final String DB_URL        = "jdbc:derby://localhost:1527/databases/ClassroomsDB";
    private static final String DB_USER       = "db_user";
    private static final String DB_PASS       = "db_user";
    private static final String DEFAULTS_FILE = "defaults.txt";

    private static Connection conn = null;
    private static Statement  stmt = null;

    // П.15 = 0: список ID из последнего просмотра — для выбора по номеру строки
    private static List<int[]> lastViewedIds = new ArrayList<int[]>();
    // [0] = ID_ROOM, [1] = ID_RESPONSIBLE

    public static void main(String[] args) {
        System.out.println("=== Classrooms | Variant 56 | Naumov D.O. ===");

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            return;
        }

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": viewAll();           break;
                case "2": addRecord(sc);       break;
                case "3": editRecord(sc);      break;
                case "4": deleteRecord(sc);    break;
                case "5": phoneDirectory();    break;
                case "6": averageArea();       break;
                case "7": listFIOLexicographic(); break;
                case "8": resetToDefaults(sc); break;
                case "0": running = false;     break;
                default:  System.out.println("Invalid choice. Try again."); break;
            }
        }

        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Program terminated.");
    }

    private static void printMenu() {
        System.out.println("\n========== MENU ==========");
        System.out.println("1. View all records");
        System.out.println("2. Add record");
        System.out.println("3. Edit record [by list number - P.15=0]");
        System.out.println("4. Delete record [by list number - P.15=0]");
        System.out.println("--- Functions P.10 (d:100) ---");
        System.out.println("5. Phone directory (full name + phone) in lexicographic order");
        System.out.println("6. Average area assigned to responsible persons");
        System.out.println("--- P.7/P.8: output first field of second table ---");
        System.out.println("7. Responsible persons' full names in lexicographic order");
        System.out.println("--- P.6: reset to values from file ---");
        System.out.println("8. Reset record to default values (from " + DEFAULTS_FILE + ")");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    // ========== 1. Просмотр всех записей ==========
    private static void viewAll() {
        lastViewedIds.clear();
        String sql =
            "SELECT R.ID_ROOM, R.BUILDING, R.ROOM_NUMBER, R.NAME, R.AREA, "
          + "P.ID_RESPONSIBLE, P.FIO, P.POSITION, P.TELEPHONE, P.AGE "
          + "FROM ROOMS R, RESPONSIBLES P "
          + "WHERE R.ID_RESPONSIBLE = P.ID_RESPONSIBLE "
          + "ORDER BY R.ID_ROOM";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n No. | Building         | Room | Name                   | Area(m^2) | Responsible full name");
            System.out.println("-----|------------------|------|------------------------|----------|-----------------------");
            int n = 0;
            while (rs.next()) {
                n++;
                lastViewedIds.add(new int[]{
                    rs.getInt("ID_ROOM"),
                    rs.getInt("ID_RESPONSIBLE")
                });
                System.out.printf(" %-4d| %-16s | %-4d | %-22s | %-8.1f | %s%n",
                    n,
                    rs.getString("BUILDING"),
                    rs.getInt("ROOM_NUMBER"),
                    rs.getString("NAME"),
                    rs.getDouble("AREA"),
                    rs.getString("FIO"));
            }
            System.out.println("Records found: " + n);
        } catch (SQLException e) {
            System.out.println("View error: " + e.getMessage());
        }
    }

    // ========== 2. Добавить запись (транзакция — П.17) ==========
    private static void addRecord(Scanner sc) {
        System.out.println("\n--- Add classroom ---");
        System.out.print("Building: ");            String building  = sc.nextLine().trim();
        System.out.print("Room number: ");   int    roomNum   = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Name: ");      String name      = sc.nextLine().trim();
        System.out.print("Area (m^2): ");      double area      = Double.parseDouble(sc.nextLine().trim());
        System.out.println("--- Responsible person ---");
        System.out.print("Full name: ");               String fio       = sc.nextLine().trim();
        System.out.print("Position: ");         String position  = sc.nextLine().trim();
        System.out.print("Phone: ");           String telephone = sc.nextLine().trim();
        System.out.print("Age: ");           int    age       = Integer.parseInt(sc.nextLine().trim());

        PreparedStatement psResp = null;
        PreparedStatement psRoom = null;
        try {
            int[] maxIds     = findMaxIds();
            int   newIdResp  = maxIds[0] + 1;
            int   newIdRoom  = maxIds[1] + 1;

            conn.setAutoCommit(false);

            psResp = conn.prepareStatement(
                "INSERT INTO RESPONSIBLES (ID_RESPONSIBLE, FIO, POSITION, TELEPHONE, AGE) "
                + "VALUES (?, ?, ?, ?, ?)");
            psResp.setInt(1, newIdResp);
            psResp.setString(2, fio);
            psResp.setString(3, position);
            psResp.setString(4, telephone);
            psResp.setInt(5, age);
            psResp.executeUpdate();

            psRoom = conn.prepareStatement(
                "INSERT INTO ROOMS (ID_ROOM, BUILDING, ROOM_NUMBER, NAME, AREA, ID_RESPONSIBLE) "
                + "VALUES (?, ?, ?, ?, ?, ?)");
            psRoom.setInt(1, newIdRoom);
            psRoom.setString(2, building);
            psRoom.setInt(3, roomNum);
            psRoom.setString(4, name);
            psRoom.setDouble(5, area);
            psRoom.setInt(6, newIdResp);
            psRoom.executeUpdate();

            conn.commit();
            System.out.println("Record added. Classroom ID: " + newIdRoom
                + ", Responsible ID: " + newIdResp);
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("Transaction cancelled (rollback).");
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            System.out.println("Add error: " + e.getMessage());
        } finally {
            try {
                if (psResp != null) psResp.close();
                if (psRoom != null) psRoom.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ========== 3. Изменить запись (транзакция — П.17) ==========
    private static void editRecord(Scanner sc) {
        if (lastViewedIds.isEmpty()) {
            System.out.println("First view the list (option 1).");
            return;
        }
        System.out.print("Enter record number to edit: ");
        int n = Integer.parseInt(sc.nextLine().trim());
        if (n < 1 || n > lastViewedIds.size()) {
            System.out.println("Invalid number.");
            return;
        }
        int idRoom = lastViewedIds.get(n - 1)[0];
        int idResp = lastViewedIds.get(n - 1)[1];

        System.out.println("Enter new values (classroom):");
        System.out.print("Building: ");            String building  = sc.nextLine().trim();
        System.out.print("Room number: ");   int    roomNum   = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Name: ");      String name      = sc.nextLine().trim();
        System.out.print("Area (m^2): ");      double area      = Double.parseDouble(sc.nextLine().trim());
        System.out.println("Enter new values (responsible person):");
        System.out.print("Full name: ");               String fio       = sc.nextLine().trim();
        System.out.print("Position: ");         String position  = sc.nextLine().trim();
        System.out.print("Phone: ");           String telephone = sc.nextLine().trim();
        System.out.print("Age: ");           int    age       = Integer.parseInt(sc.nextLine().trim());

        PreparedStatement psRoom = null;
        PreparedStatement psResp = null;
        try {
            conn.setAutoCommit(false);

            psRoom = conn.prepareStatement(
                "UPDATE ROOMS SET BUILDING=?, ROOM_NUMBER=?, NAME=?, AREA=? WHERE ID_ROOM=?");
            psRoom.setString(1, building);
            psRoom.setInt(2, roomNum);
            psRoom.setString(3, name);
            psRoom.setDouble(4, area);
            psRoom.setInt(5, idRoom);
            psRoom.executeUpdate();

            psResp = conn.prepareStatement(
                "UPDATE RESPONSIBLES SET FIO=?, POSITION=?, TELEPHONE=?, AGE=? WHERE ID_RESPONSIBLE=?");
            psResp.setString(1, fio);
            psResp.setString(2, position);
            psResp.setString(3, telephone);
            psResp.setInt(4, age);
            psResp.setInt(5, idResp);
            psResp.executeUpdate();

            conn.commit();
            System.out.println("Record edited.");
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("Transaction cancelled (rollback).");
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            System.out.println("Edit error: " + e.getMessage());
        } finally {
            try {
                if (psRoom != null) psRoom.close();
                if (psResp != null) psResp.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ========== 4. Удалить запись (транзакция — П.17) ==========
    private static void deleteRecord(Scanner sc) {
        if (lastViewedIds.isEmpty()) {
            System.out.println("First view the list (option 1).");
            return;
        }
        System.out.print("Enter record number to delete: ");
        int n = Integer.parseInt(sc.nextLine().trim());
        if (n < 1 || n > lastViewedIds.size()) {
            System.out.println("Invalid number.");
            return;
        }
        int idRoom = lastViewedIds.get(n - 1)[0];
        int idResp = lastViewedIds.get(n - 1)[1];

        PreparedStatement psRoom = null;
        PreparedStatement psResp = null;
        try {
            conn.setAutoCommit(false);

            // Сначала удаляем аудиторию (FK), затем ответственного
            psRoom = conn.prepareStatement("DELETE FROM ROOMS WHERE ID_ROOM = ?");
            psRoom.setInt(1, idRoom);
            psRoom.executeUpdate();

            psResp = conn.prepareStatement("DELETE FROM RESPONSIBLES WHERE ID_RESPONSIBLE = ?");
            psResp.setInt(1, idResp);
            psResp.executeUpdate();

            conn.commit();
            lastViewedIds.remove(n - 1);
            System.out.println("Record deleted.");
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("Transaction cancelled (rollback).");
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            System.out.println("Delete error: " + e.getMessage());
        } finally {
            try {
                if (psRoom != null) psRoom.close();
                if (psResp != null) psResp.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ========== 5. [П.10 д] Телефонный справочник ==========
    private static void phoneDirectory() {
        String sql = "SELECT FIO, TELEPHONE FROM RESPONSIBLES ORDER BY FIO";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Phone directory (lexicographic order) ---");
            System.out.println("Full name                                 | Phone");
            System.out.println("------------------------------------------|----------------");
            int n = 0;
            while (rs.next()) {
                n++;
                System.out.printf("%-42s| %s%n",
                    rs.getString("FIO"),
                    rs.getString("TELEPHONE"));
            }
            System.out.println("Records: " + n);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 6. [П.10 д] Средняя площадь за ответственными ==========
    private static void averageArea() {
        String sql =
            "SELECT P.FIO, AVG(R.AREA) AS AVG_AREA, COUNT(R.ID_ROOM) AS CNT "
          + "FROM RESPONSIBLES P, ROOMS R "
          + "WHERE R.ID_RESPONSIBLE = P.ID_RESPONSIBLE "
          + "GROUP BY P.FIO "
          + "ORDER BY P.FIO";
        try {
            // Сохраняем результаты в список — потому что далее выполним второй запрос на stmt
            List<Object[]> rows = new ArrayList<Object[]>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("FIO"),
                    rs.getDouble("AVG_AREA"),
                    rs.getInt("CNT")
                });
            }

            System.out.println("\n--- Average area assigned to responsible persons ---");
            System.out.println("Full name                                 | Avg.area   | No. of rooms");
            System.out.println("------------------------------------------|------------|------------");
            for (Object[] row : rows) {
                System.out.printf("%-42s| %-10.1f | %d%n",
                    row[0], row[1], row[2]);
            }

            // Второй запрос — общая средняя по всем аудиториям
            rs = stmt.executeQuery("SELECT AVG(AREA) AS TOTAL_AVG FROM ROOMS");
            if (rs.next()) {
                System.out.printf("Total average area across all classrooms: %.1f m^2%n",
                    rs.getDouble("TOTAL_AVG"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 7. [П.7/П.8] ФИО ответственных в лекс. порядке ==========
    // П.7=1 — вторая таблица (RESPONSIBLES), П.8=0 — первое поле (FIO)
    private static void listFIOLexicographic() {
        String sql = "SELECT FIO FROM RESPONSIBLES ORDER BY FIO";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Responsible persons' full names (lexicographic order) ---");
            int n = 0;
            while (rs.next()) {
                n++;
                System.out.println(n + ". " + rs.getString("FIO"));
            }
            System.out.println("Records: " + n);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 8. [П.6] Сброс к значениям по умолчанию из файла (транзакция — П.17) ==========
    private static void resetToDefaults(Scanner sc) {
        if (lastViewedIds.isEmpty()) {
            System.out.println("First view the list (option 1).");
            return;
        }
        System.out.print("Enter record number to reset: ");
        int n = Integer.parseInt(sc.nextLine().trim());
        if (n < 1 || n > lastViewedIds.size()) {
            System.out.println("Invalid number.");
            return;
        }
        int idRoom = lastViewedIds.get(n - 1)[0];
        int idResp = lastViewedIds.get(n - 1)[1];

        // Читаем значения по умолчанию из файла (П.6=1 — из файла, не из программы)
        Properties defaults = new Properties();
        try {
            FileInputStream fis = new FileInputStream(DEFAULTS_FILE);
            defaults.load(new InputStreamReader(fis, "UTF-8"));
            fis.close();
        } catch (IOException e) {
            System.out.println("Error reading file " + DEFAULTS_FILE + ": " + e.getMessage());
            return;
        }

        String building  = defaults.getProperty("BUILDING",    "Main building");
        int    roomNum   = Integer.parseInt(defaults.getProperty("ROOM_NUMBER",  "100"));
        String name      = defaults.getProperty("NAME",         "Classroom");
        double area      = Double.parseDouble(defaults.getProperty("AREA",       "50.0"));
        String fio       = defaults.getProperty("FIO",          "Ivanov Ivan Ivanovich");
        String position  = defaults.getProperty("POSITION",     "Teacher");
        String telephone = defaults.getProperty("TELEPHONE",    "89000000000");
        int    age       = Integer.parseInt(defaults.getProperty("AGE",          "40"));

        PreparedStatement psRoom = null;
        PreparedStatement psResp = null;
        try {
            conn.setAutoCommit(false);

            psRoom = conn.prepareStatement(
                "UPDATE ROOMS SET BUILDING=?, ROOM_NUMBER=?, NAME=?, AREA=? WHERE ID_ROOM=?");
            psRoom.setString(1, building);
            psRoom.setInt(2, roomNum);
            psRoom.setString(3, name);
            psRoom.setDouble(4, area);
            psRoom.setInt(5, idRoom);
            psRoom.executeUpdate();

            psResp = conn.prepareStatement(
                "UPDATE RESPONSIBLES SET FIO=?, POSITION=?, TELEPHONE=?, AGE=? WHERE ID_RESPONSIBLE=?");
            psResp.setString(1, fio);
            psResp.setString(2, position);
            psResp.setString(3, telephone);
            psResp.setInt(4, age);
            psResp.setInt(5, idResp);
            psResp.executeUpdate();

            conn.commit();
            System.out.println("Record reset to values from " + DEFAULTS_FILE + ".");
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.out.println("Transaction cancelled (rollback).");
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            System.out.println("Reset error: " + e.getMessage());
        } finally {
            try {
                if (psRoom != null) psRoom.close();
                if (psResp != null) psResp.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ========== Вспомогательный метод: максимальные ID ==========
    private static int[] findMaxIds() throws SQLException {
        int[] r = {0, 0};
        // Оба запроса выполняются на stmt последовательно,
        // результат первого сохраняется в r[0] до выполнения второго
        ResultSet rs = stmt.executeQuery("SELECT MAX(ID_RESPONSIBLE) AS M FROM RESPONSIBLES");
        if (rs.next() && rs.getString("M") != null) r[0] = rs.getInt("M");
        rs = stmt.executeQuery("SELECT MAX(ID_ROOM) AS M FROM ROOMS");
        if (rs.next() && rs.getString("M") != null) r[1] = rs.getInt("M");
        return r;
    }
}