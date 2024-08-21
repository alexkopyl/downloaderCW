package ru.quark;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboard {

    public SendMessage filmSerialInstructionKeyboard(long chat_id) {
        List<KeyboardButton> buttons = List.of(
                createButton("\uD83C\uDFACФильмы\uD83D\uDCFD"),
                createButton("\uD83D\uDCE7Обратная связь\uD83D\uDEF0")
        );
        return createSendMessage(chat_id, "Меню", createKeyboardMarkup(buttons, 1));
    }

    public SendMessage genresSearchListFilmKeyboard(long chat_id) {
        List<KeyboardButton> buttons = List.of(
                createButton("\uD83C\uDF9EЖанры"),
                createButton("\uD83D\uDCCBСписок"),
                createButton("◀️ Назад"),
                createButton("\uD83D\uDD0EПоиск")
        );
        return createSendMessage(chat_id, "Фильмы", createKeyboardMarkup(buttons, 2));
    }

    public SendMessage feedbackKeyboard(long chat_id) {
        List<KeyboardButton> buttons = List.of(
                createButton("\uD83E\uDD19Написать нам"),
                createButton("\uD83D\uDCB0Помощь проекту"),
                createButton("◀️ Назад")
        );
        return createSendMessage(chat_id, "\uD83D\uDCEE", createKeyboardMarkup(buttons, 2));
    }

    private KeyboardButton createButton(String text) {
        return KeyboardButton.builder().text(text).build();
    }

    private ReplyKeyboardMarkup createKeyboardMarkup(List<KeyboardButton> buttons, int buttonsPerRow) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < buttons.size(); i++) {
            currentRow.add(buttons.get(i));
            if ((i + 1) % buttonsPerRow == 0 || i == buttons.size() - 1) {
                keyboardRows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }
        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(true)
                .selective(true)
                .build();
    }

    private SendMessage createSendMessage(long chat_id, String text, ReplyKeyboardMarkup replyMarkup) {
        return SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .replyMarkup(replyMarkup)
                .disableWebPagePreview(true)
                .build();
    }

    public SendMessage qrCodeButton(long chat_id) {
        List<KeyboardButton> buttons = List.of(
                createButton("◀️ Назад"),
                createButton("QR код кошелька")
        );
        return createSendMessage(chat_id, "Кино не умрет, пока в кинотеатрах будет темно.", createKeyboardMarkup(buttons, 1));
    }
}
