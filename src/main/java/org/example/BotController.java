package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;

public class BotController extends TelegramLongPollingBot {
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
        var msg = update.getMessage();
        var user = msg.getFrom();
        boolean isCommand = msg.isCommand();
        if(msg.getText().equals("/start")){
            try {
                if(DB.checkIfUserExists((int)user.getId().longValue())){
                    sendText(user.getId(),"С возвращением, " + user.getFirstName() + "!");
                } else {
                    sendText(user.getId(),"Привет, " + user.getFirstName() + "! Это модель gpt2 без VPN! Задай мне любой вопрос!");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        DB.insert_message(user.getId(),msg.getText(),user.getUserName(),user.getFirstName());
        if(isCommand){
            if(msg.getText().equals("/stats")){
                String stats = DB.get_user_statistics((int)user.getId().longValue());
                sendText(user.getId(),stats);
            }
            if(msg.getText().equals("/info")){
                String stats = DB.get_user_info((int)user.getId().longValue());
                sendText(user.getId(),stats+"Использумая модель: gpt2 \nСтатус работы: в самом разгаре!)");
            }
            if(msg.getText().equals("/history")){
                String history = DB.get_user_messages((int)user.getId().longValue());
                sendText(user.getId(),history);
            }
            if(msg.getText().equals("/help")){
                String help = "Доступные команды:\n" + "/stats - статистика\n"
                        + "/history - история сообщений\n" + "/help - помощь\n" + "/start - приветствие";
                sendText(user.getId(),help);
            }
        } else {
            sendText(user.getId(),LLM.generate(msg.getText()));
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
