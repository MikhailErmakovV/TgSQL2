package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BotController extends TelegramLongPollingBot {
    static {

    }
    @Override
    public String getBotUsername() {
        return "Зеркало";
    }

    @Override
    public String getBotToken() {
        return "7470332227:AAFt7yF5b2UY5RBdAh1xz9OoLI9QbfJZVpk";
    }

    @Override
    public void onUpdateReceived(Update update) {
    }
}
