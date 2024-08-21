package ru.quark;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
public class Main {
    public static void main(String[] args) {
        //BOT
        String botToken = "token";
        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new DownloaderBotConsumer(
                    botToken,
                    "mongodb://admin:pass@127.0.0.1:27017/db_name?authSource=admin",
                    "db_name"));
            System.out.println("Бот downloaderCW запущен");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
