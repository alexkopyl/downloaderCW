package ru.quark;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class InlineKeyboard {
    private static final List<String> fileNameGenreList = new CopyOnWriteArrayList<>();
    private static final List<String> nameYearList = new CopyOnWriteArrayList<>();
    private static final List<List<String>> fileNameGenrePartList = new CopyOnWriteArrayList<>();
    private static final List<InlineKeyboardRow> inlineKeyboardRows = new CopyOnWriteArrayList<>();
    private static final Map<Integer, Map<String, String>> callbackMap = new HashMap<>();
    private static final String uri = "mongodb://admin:pass@127.0.0.1:27017/db_name?authSource=admin";
    private static final String idDB = "db_name";
    private static final MongoClient mongoClient = MongoClients.create(uri);
    private static final MongoDatabase db = mongoClient.getDatabase(idDB);;


    public static SendMessage filmListToGenresInlineKeyboardSM(String genre, long chat_id, int messageId, int pageNumber) throws InterruptedException {
        setFileNameGenreList(genre);
        setNameYearList(getFileNameGenreList());
        setFileNameGenrePartList(getNameYearList());
        setCallbackMap(genre);
        setInlineKeyboardRows(pageNumber);

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(getInlineKeyboardRows())
                .build();

        return SendMessage.builder()
                .chatId(chat_id)
                .text("Список фильмов по жанру: " + genre)
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    public static EditMessageReplyMarkup filmListToGenresInlineKeyboardEM(String genre, long chat_id, int messageId, int pageNumber) throws InterruptedException {
        setFileNameGenreList(genre);
        setNameYearList(getFileNameGenreList());
        setFileNameGenrePartList(getNameYearList());
        setCallbackMap(genre);
        setInlineKeyboardRows(pageNumber);

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(getInlineKeyboardRows())
                .build();

        return EditMessageReplyMarkup.builder()
                .chatId(chat_id)
                .messageId(messageId)
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    //метод сетает кнопки на инлайн клавиатуре жанров фильмов
    //учитываются условия расположения кнопок на 1ой и крайней странице, а так же средних страниц
    private static synchronized void setInlineKeyboardRows(int pageNumber) {
        inlineKeyboardRows.clear();
        List<String> fileNameList = getFileNameGenrePartList().get(pageNumber);
        for (String fileNameYear : fileNameList) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            String fileName = fileNameYear.replaceAll("\\s*\\(\\d{4}\\)$", "");
            keyboardRow.add(InlineKeyboardButton.builder().text(fileNameYear).callbackData(getCallbackName(fileName) + "a").build());
            inlineKeyboardRows.add(keyboardRow);
        }
        //кнопка "далее" + колбэк на 1ю страницу
        int totalPages = getFileNameGenrePartList().size();
        // Кнопка "Далее" для первой страницы
        if (inlineKeyboardRows.size() == 10 && pageNumber < totalPages - 1 && pageNumber == 0) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("Далее ▶").callbackData(getGenre(pageNumber + 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }

        // Кнопки "Назад" и "Далее" для средних страниц
        if (inlineKeyboardRows.size() == 10 && pageNumber >= 1 && pageNumber < totalPages - 1) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("◀ Назад").callbackData(getGenre(pageNumber - 1)).build());
            keyboardRow.add(InlineKeyboardButton.builder().text("Далее ▶").callbackData(getGenre(pageNumber + 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }

        // Кнопка "Назад" для последней страницы
        if (inlineKeyboardRows.size() <= 10 && pageNumber == totalPages - 1 && totalPages > 1) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("◀ Назад").callbackData(getGenre(pageNumber - 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }
    }


    //создание общего списка фильмов по жанру
    private static void setFileNameGenreList(String genre) throws InterruptedException {
        fileNameGenreList.clear();
        String genreColl = "genre";
        MongoCollection<Document> genreDocsColl = db.getCollection(genreColl);
        List<Document> genreList = genreDocsColl.find().into(new ArrayList<>());
        for (Document document : genreList) {
            if (document.get(genre) != null) {
                fileNameGenreList.add(document.get(genre).toString());
                Collections.sort(fileNameGenreList);
            }
        }

    }

    private static List<String> getFileNameGenreList() {
        return fileNameGenreList;
    }

    //меод возвращает список массивок кнопок в инлайн клавиатуре
    private static List<InlineKeyboardRow> getInlineKeyboardRows() {
        return inlineKeyboardRows;
    }

    //метод возвращае общий список фильмов одного по жанрово
    private static List<List<String>> getFileNameGenrePartList() {
        return fileNameGenrePartList;
    }

    //метод сетает список страниц жанров с кнопками
    private static synchronized void setFileNameGenrePartList(List<String> fileNameGenreList) {
        fileNameGenrePartList.clear();
        int partSize = 10;
        int fullListSize = fileNameGenreList.size();
        int partCount = (int) Math.ceil((double) fullListSize / partSize);

        for (int i = 0; i < partCount; i++) {
            int startIndex = i * partSize;
            int endIndex = Math.min((i + 1) * partSize, fullListSize);
            List<String> part = new ArrayList<>(fileNameGenreList.subList(startIndex, endIndex));
            fileNameGenrePartList.add(part);
        }
    }

    //метод возвращает коллекцию колбэков кнопок "вперед" и "назад" с привязкой к номеру страницы
    private static Map<Integer, Map<String, String>> getCallbackMap() {
        return callbackMap;
    }

    //метод сетает колбэки кнопок "вперед" и "назад" с привязкой к номеру сраницы
    private static void setCallbackMap(String genre) {
        callbackMap.clear();
        int genrePartListCount = getFileNameGenrePartList().size();
        for (int i = 0; i < genrePartListCount; i++) {
            String callBackGenre = genre + "_" + i;
            Map<String, String> map = new HashMap<>();
            map.put("Genre", callBackGenre);
            callbackMap.put(i, map);
        }
    }

    //метод возвражает колбэк кнопки "далее"
    private static String getGenre(int pageNumber) {
        Map<String, String> map = getCallbackMap().get(pageNumber);
        if (map != null) {
            return map.get("Genre");
        }
        return null;
    }

    private static List<String> getNameYearList() {
        return nameYearList;
    }

    private static synchronized void setNameYearList(List<String> fileNameGenreList) {
        nameYearList.clear();
        String yearColl = "year";
        MongoCollection<Document> yearDocsColl = db.getCollection(yearColl);
        List<Document> yearList = yearDocsColl.find().into(new ArrayList<>());
        for (String name : fileNameGenreList) {
            for (Document document : yearList) {
                if (document.get(name) != null) {
                    nameYearList.add(name + " " + document.get(name));
                }
            }
        }
    }

    public static String getCallbackName(String key) {
        String ntcColl = "ntc";
        String oid = "";
        MongoCollection<Document> mongoIdColl = db.getCollection(ntcColl);
        List<Document> idList = mongoIdColl.find().into(new ArrayList<>());
        for (Document document : idList) {
            for (String keyStr : document.keySet()) {
                if (Objects.equals(keyStr, key)) {
                    oid = document.get(key).toString();
                }
            }
        }
        return oid;
    }
}
