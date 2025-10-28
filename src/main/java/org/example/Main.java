package org.example;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.*;
import static org.example.DB.*;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            if (conn != null) {
                DB.push_connection(conn);
                System.out.println("Подключение к базе данных успешно установлено.");
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new BotController());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}