package com.example.musicapp.data.database.controller;


import com.example.musicapp.data.modle.User;
import com.example.musicapp.ui.controller.ControlPanelController;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.musicapp.ui.view.AlertsView.showInformationMessage;

public class DatabaseController {

    private static final String DB_URL = "jdbc:sqlite:DB.db";

    // משתנה מופע יחיד
    private static DatabaseController instance;

    // בנאי פרטי למניעת יצירת מופעים נוספים
    private DatabaseController() {
        createDb();
    }

    // שיטה סטטית להחזרת המופע היחיד
    public static DatabaseController getInstance() {
        if (instance == null) {
            synchronized (DatabaseController.class) {
                if (instance == null) {
                    instance = new DatabaseController();
                }
            }
        }
        return instance;
    }

    public Connection createDb() {
        try {
            File dbFile = new File("DB.db");
            if (!dbFile.exists()) {
                Connection conn = DriverManager.getConnection(DB_URL);
                String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                        + "    username TEXT PRIMARY KEY,\n"
                        + "    password TEXT NOT NULL,\n"
                        + "    Admin BOOLEAN,\n"
                        + "    Ads BOOLEAN,\n"
                        + "    Dir BOOLEAN,\n"
                        + "    Users BOOLEAN\n"
                        + ");";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                System.out.println("Table 'users' created successfully.");
                return conn;
            } else {
                return DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createFirstAccount(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            ControlPanelController.showErrorPopup("Username or password is empty");
            return false;
        }

        String sql = "INSERT INTO users (username, password,Admin, Ads, Dir, Users) VALUES (?, ?, ?, ?, ?,?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = createDb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, true);
            pstmt.setBoolean(5, true);
            pstmt.setBoolean(6, true);

            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAccount(String username, String password,boolean admin, boolean Ads, boolean Dir, boolean Users) {
        if (username.isEmpty() || password.isEmpty()) {
            ControlPanelController.showErrorPopup("Username or password is empty");
            return Ads;
        }

        String sql = "INSERT INTO users (username, password,Admin, Ads, Dir, Users) VALUES (?, ?, ?, ?, ?,?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = createDb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setBoolean(3, admin);
            pstmt.setBoolean(4, Ads);
            pstmt.setBoolean(5, Dir);
            pstmt.setBoolean(6, Users);

            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");
            showInformationMessage("User has been created", "Succses");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Ads;
    }

    public User login(String username, String password) {
        String sql = "SELECT Admin, Ads, Dir, Users, password FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    boolean admin = rs.getBoolean("Admin");
                    boolean ads = rs.getBoolean("Ads");
                    boolean dir = rs.getBoolean("Dir");
                    boolean users = rs.getBoolean("Users");

                    if (BCrypt.checkpw(password, storedHash)) {
                        System.out.println("Login successful!");
                        return new User(username,ads, dir, users,admin);
                    } else {
                        ControlPanelController.showErrorPopup("Username or Password are incorrect");
                        return null;
                    }
                } else {
                    ControlPanelController.showErrorPopup("Username or Password are incorrect");
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUser(String userName) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        try(Connection con = DriverManager.getConnection(DB_URL);
        PreparedStatement pstm = con.prepareStatement(sql)){
            pstm.setString(1,userName);
            ResultSet rs =  pstm.executeQuery();
            if (rs.next()){
                return new User(rs.getString("username"),rs.getString("password"),rs.getBoolean("Admin"),rs.getBoolean("Ads"),rs.getBoolean("Dir"),rs.getBoolean("Users"));
            }else{
                return null;
            }
        }catch (SQLException e){
            e.printStackTrace();

            return null;
        }
    }
    public List<User> getUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                boolean ads = rs.getBoolean("Ads");
                boolean dir = rs.getBoolean("Dir");
                boolean usersAccess = rs.getBoolean("Users");
                boolean admin = rs.getBoolean("Users");
                users.add(new User(username, ads, dir,admin, usersAccess));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void updateUserAccess(String username,boolean admin, boolean ads, boolean dir, boolean users) {
        String sql = "UPDATE users SET  Ads = ?, Dir = ?, Users = ? WHERE username = ?,Admin = ?";

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setBoolean(1, ads);
            pstmt.setBoolean(2, dir);
            pstmt.setBoolean(3, users);
            pstmt.setString(4, username);
            pstmt.setBoolean(5, admin);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
