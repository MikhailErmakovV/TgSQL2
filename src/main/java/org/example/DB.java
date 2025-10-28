package org.example;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DB {
    static String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
    static String user = "postgres";
    static String pass = "123";
    public static Connection get_connection(){
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            if (conn != null) {
                System.out.println("Подключение к базе данных успешно установлено.");
                return conn;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    public static void insert_message(Connection conn, int user_id, String message_text, String username, String first_name, String registration_date){
        String sql = "INSERT INTO messages(user_id, message_text) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            boolean exists = checkIfUserExists(conn, user_id);

            // Шаг 2: Если пользователя нет, добавляем его
            if (!exists) {
                addUser(conn, user_id, username, first_name, registration_date);
            }
            pstmt.setInt(1, user_id);
            pstmt.setString(2, message_text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean checkIfUserExists(Connection conn, int user_id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    private static void addUser(Connection conn, int user_id, String username, String first_name, String registration_date) throws SQLException {
        String sql = "INSERT INTO users(user_id, username, first_name, registration_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            pstmt.setInt(1, user_id);
            pstmt.setString(2, username);
            pstmt.setString(3, first_name);
            pstmt.setDate(4, new java.sql.Date(sdf.parse(registration_date).getTime()));
            pstmt.executeUpdate();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static void get_user_messages(Connection conn, int user_id){
        String sql = "SELECT * FROM messages WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String messageText = rs.getString("message_text");
                String timeStamp = rs.getString("timestamp");
                System.out.println("ID: " + id + ", User_id: " + userId + ", MessageText: " + messageText + ", Time: " + timeStamp);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void get_all_messages(Connection conn){
        String sql = "SELECT * FROM messages ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String messageText = rs.getString("message_text");
                String timeStamp = rs.getString("timestamp");
                System.out.println("ID: " + id + ", User_id: " + userId + ", MessageText: " + messageText + ", Time: " + timeStamp);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void update_messages(Connection conn, int message_id, String new_text){
        String sql = "UPDATE messages SET message_text = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, new_text); // Новое значение email
            pstmt.setInt(2, message_id);
            int updatedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void delete_message(Connection conn, int message_id){
        String sql = "DELETE FROM messages WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, message_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void get_user_statistics(Connection conn, int user_id){
        String sql = """
            SELECT COUNT(*) as total_messages,
                   MIN(timestamp) as first_message_date,
                   MAX(timestamp) as last_message_date
            FROM messages
            WHERE user_id = ?
            """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalMessages = rs.getInt("total_messages");
                    Timestamp firstMessageDate = rs.getTimestamp("first_message_date");
                    Timestamp lastMessageDate = rs.getTimestamp("last_message_date");
                    System.out.println("Общая статистика пользователя:");
                    System.out.println("Количество сообщений: " + totalMessages);
                    System.out.println("Первое сообщение: " + firstMessageDate);
                    System.out.println("Последнее сообщение: " + lastMessageDate);
                } else {
                    System.out.println("Нет сообщений для данного пользователя.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
