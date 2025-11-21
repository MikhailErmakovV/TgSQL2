package org.example;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB {
    public static String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
    public static String user = "postgres";
    public static String pass = "123";
    public static Connection conn;
    public static void start_connection() throws SQLException {
        conn = DriverManager.getConnection(dbUrl, user, pass);
    }
    public static void insert_message(long user_id, String message_text, String username, String first_name){
        String sql = "INSERT INTO messages(user_id, message_text) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            boolean exists = checkIfUserExists((int) user_id);
            if (!exists) {
                addUser((int) user_id, username, first_name);
            } else {
                if (!if_last_activity_today((int) user_id)) {
                    increment_activity_count((int) user_id);
                }
                update_last_activity((int) user_id);
            }
            pstmt.setInt(1, (int) user_id);
            pstmt.setString(2, message_text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    private static void increment_activity_count(int user_id) {
        String sql = "UPDATE users SET count_activity = count_activity + 1 WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при увеличении счётчика активности", e);
        }
    }
    public static void update_last_activity(int user_id){
        String sql = "UPDATE users SET last_activity = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDate currentDate = LocalDate.now();
            // Создаем форматтер для даты в формате dd/MM/yyyy
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            // Форматируем дату
            String formattedDate = currentDate.format(formatter);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            pstmt.setDate(1, new Date(sdf.parse(formattedDate).getTime()));
            pstmt.setInt(2, user_id);
            pstmt.executeUpdate();
        } catch (SQLException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean if_last_activity_today(int user_id) {
        String sql = "SELECT last_activity FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Date dbDate = rs.getDate("last_activity"); // Получаем дату из БД
                LocalDate today = LocalDate.now();
                // Преобразуем дату из БД в LocalDate для сравнения
                LocalDate activityDate = dbDate.toLocalDate();
                return activityDate.isEqual(today);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке даты последней активности", e);
        }
        return false; // Пользователь не найден
    }

    public static boolean checkIfUserExists(int user_id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    private static void addUser(int user_id, String username, String first_name) throws SQLException {
        String sql = "INSERT INTO users(user_id, username, first_name, registration_date, last_activity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDate currentDate = LocalDate.now();
            // Создаем форматтер для даты в формате dd/MM/yyyy
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            // Форматируем дату
            String formattedDate = currentDate.format(formatter);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            pstmt.setInt(1, user_id);
            pstmt.setString(2, username);
            pstmt.setString(3, first_name);
            pstmt.setDate(4, new Date(sdf.parse(formattedDate).getTime()));
            pstmt.setDate(5, new Date(sdf.parse(formattedDate).getTime()));
            pstmt.executeUpdate();
            increment_activity_count(user_id);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static String get_user_messages(int user_id){
        String sql = "SELECT * FROM messages WHERE user_id = ? ORDER BY id DESC LIMIT 5";
        String messages = "";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String messageText = rs.getString("message_text");
                String timeStamp = rs.getString("timestamp");
                messages = messages + ("ID: " + id + ", User_id: " + userId + ", MessageText: " + messageText + ", Time: " + timeStamp + "\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }
    public static void get_all_messages(){
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
    public static void update_messages(int message_id, String new_text){
        String sql = "UPDATE messages SET message_text = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, new_text); // Новое значение email
            pstmt.setInt(2, message_id);
            int updatedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void delete_message(int message_id){
        String sql = "DELETE FROM messages WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, message_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String get_user_info(int user_id){
        String sql = "SELECT COUNT(*) as total_messages FROM users WHERE user_id = ?";
        String user_info = "";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user_info = String.valueOf(rs.getInt("total_messages"));
            }
            return "Количество сообщений в диалоге: "+user_info+"\n";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String get_user_statistics(int user_id){
        String sql = """
            SELECT COUNT(*) as total_messages,
                   MIN(timestamp) as first_message_date,
                   MAX(timestamp) as last_message_date
            FROM messages
            WHERE user_id = ?
            """;
        String stats;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalMessages = rs.getInt("total_messages");
                    Timestamp firstMessageDate = rs.getTimestamp("first_message_date");
                    Timestamp lastMessageDate = rs.getTimestamp("last_message_date");
                    stats = "Общая статистика пользователя:\n"+
                            "Количество сообщений: " + totalMessages + "\n" +
                            "Первое сообщение: " + firstMessageDate + "\n" +
                            "Последнее сообщение: " + lastMessageDate;
                } else {
                    stats = "Нет сообщений для данного пользователя.";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stats;
    }
}
