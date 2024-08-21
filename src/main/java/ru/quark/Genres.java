package ru.quark;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.util.*;

public class Genres {
    private static final Map<String, String> callbackDataGenreMap = new HashMap<>();
    public static SendMessage genresFilmsList1Keyboard(long chat_id) {
        List<String[]> buttonData = Arrays.asList(
                new String[]{"\uD83D\uDE01Комедии", "Комедии"},
                new String[]{"\uD83E\uDD8BМультфильмы", "Мультфильмы"},
                new String[]{"\uD83D\uDE31Ужасы", "Ужасы"},
                new String[]{"\uD83D\uDC40Фантастика", "Фантастика"},
                new String[]{"\uD83D\uDCA5Триллеры", "Триллеры"},
                new String[]{"\uD83D\uDCA3Боевики", "Боевики"},
                new String[]{"\uD83D\uDC8CМелодрамы", "Мелодрамы"},
                new String[]{"\uD83D\uDD75\uFE0F\u200D♂\uFE0FДетективы", "Детективы"},
                new String[]{"\uD83C\uDFB6Музыкальные", "Музыкальные"},
                new String[]{"Далее ▶", "up1List"}
        );

        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        for (String[] data : buttonData) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(InlineKeyboardButton.builder().text(data[0]).callbackData(data[1]).build());
            inlineKeyboardRows.add(row);
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(inlineKeyboardRows)
                .build();

        return SendMessage.builder()
                .chatId(chat_id)
                .replyMarkup(inlineKeyboardMarkup)
                .text("Выберите интересующие вас фильмы и сериалы по жанрам")
                .build();
    }

    public static SendMessage genresFilmsList2Keyboard(long chat_id) {
        List<String[]> buttonData = Arrays.asList(
                new String[]{"\uD83C\uDFA4Мюзиклы", "Мюзиклы"},
                new String[]{"\uD83E\uDDEDПриключения", "Приключения"},
                new String[]{"\uD83E\uDDDA\u200D♀\uFE0FФэнтези", "Фэнтези"},
                new String[]{"⚔\uFE0FВоенные", "Военные"},
                new String[]{"\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66Семейные", "Семейные"},
                new String[]{"⛩\uFE0FАниме", "Аниме"},
                new String[]{"\uD83D\uDCDAИсторические", "Исторические"},
                new String[]{"\uD83C\uDFADДрамы", "Драмы"},
                new String[]{"\uD83E\uDDF8Детские", "Детские"},
                new String[]{"◀ Назад", "down2List"},
                new String[]{"Далее ▶", "up2List"}
        );

        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        for (int i = 0; i < buttonData.size() - 2; i++) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(InlineKeyboardButton.builder().text(buttonData.get(i)[0]).callbackData(buttonData.get(i)[1]).build());
            inlineKeyboardRows.add(row);
        }

        // Обработка последнего ряда с двумя кнопками "Назад" и "Вперед"
        InlineKeyboardRow lastRow = new InlineKeyboardRow();
        lastRow.add(InlineKeyboardButton.builder().text(buttonData.get(buttonData.size() - 2)[0]).callbackData(buttonData.get(buttonData.size() - 2)[1]).build());
        lastRow.add(InlineKeyboardButton.builder().text(buttonData.getLast()[0]).callbackData(buttonData.getLast()[1]).build());
        inlineKeyboardRows.add(lastRow);

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(inlineKeyboardRows)
                .build();

        return SendMessage.builder()
                .chatId(chat_id)
                .replyMarkup(inlineKeyboardMarkup)
                .text("Выберите интересующие вас фильмы и сериалы по жанрам")
                .build();
    }

    public static SendMessage genresFilmsList3Keyboard(long chat_id) {
        List<String[]> buttonData = Arrays.asList(
                new String[]{"\uD83D\uDE94Криминал", "Криминал"},
                new String[]{"⭐\uFE0FБиографии", "Биографии"},
                new String[]{"\uD83E\uDD20Вестерны", "Вестерны"},
                new String[]{"⚽\uFE0FСпортивные", "Спортивные"},
                new String[]{"◀ Назад", "down3List"}
        );

        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        for (String[] data : buttonData) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(InlineKeyboardButton.builder().text(data[0]).callbackData(data[1]).build());
            inlineKeyboardRows.add(row);
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(inlineKeyboardRows)
                .build();

        return SendMessage.builder()
                .chatId(chat_id)
                .replyMarkup(inlineKeyboardMarkup)
                .text("Выберите интересующие вас фильмы и сериалы по жанрам")
                .build();
    }


    public static void setCallbackDataGenreMap() {
        String[] genres = {
                "Комедии", "Мультфильмы", "Ужасы", "Фантастика", "Триллеры",
                "Боевики", "Мелодрамы", "Детективы", "Музыкальные", "Мюзиклы",
                "Приключения", "Фэнтези", "Военные", "Семейные", "Аниме",
                "Исторические", "Драмы", "Детские", "Криминал", "Биографии",
                "Вестерны", "Спортивные"
        };

        for (String genre : genres) {
            callbackDataGenreMap.put(genre, genre);
        }
    }

    public static Map<String, String> getCallbackDataGenreMap() {
        return callbackDataGenreMap;
    }
}
