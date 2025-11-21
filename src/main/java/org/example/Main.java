package org.example;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.*;
import static org.example.DB.*;

public class Main {
    public static void main(String[] args) {
        try {
            new Thread(() -> {
                try {
                    start_connection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            System.out.println("Подключение к базе данных успешно установлено.");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotController());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}