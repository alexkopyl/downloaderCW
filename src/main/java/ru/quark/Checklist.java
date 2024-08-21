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

public class Checklist {
    private static final List<String> fullNameList = new CopyOnWriteArrayList<>(); // общий список фильмов
    private static final List<List<String>> partNameList = new CopyOnWriteArrayList<>();
    private static final List<String> nameYearList = new CopyOnWriteArrayList<>();
    private static final List<InlineKeyboardRow> inlineKeyboardRows = new CopyOnWriteArrayList<>();
    private static final Map<Integer, Map<String, String>> callbackMap = new HashMap<>();
    static String uri = "mongodb://admin:pass@127.0.0.1:27017/db_name?authSource=admin";
    static String idDB = "db_name";
    static MongoClient mongoClient = MongoClients.create(uri);
    private static MongoDatabase db = mongoClient.getDatabase(idDB);

    public static synchronized SendMessage filmInlineKeyboardInitSM(long chat_id,int messageId, int pageNumber) {
        setFullNameList();
        setNameYearList(getFullNameList());
        setPartNameList(getNameYearList());
        setCallbackMap();
        setInlineKeyboardRows(pageNumber);

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(getInlineKeyboardRows())
                .build();

        return SendMessage.builder()
                .chatId(chat_id)
                .text("Общий список фильмов")
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    public static synchronized EditMessageReplyMarkup filmInlineKeyboardInitEM(long chat_id,int messageId, int pageNumber) {
        setNameYearList(getFullNameList());
        setPartNameList(getNameYearList());
        setCallbackMap();
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

    private static synchronized void setInlineKeyboardRows(int pageNumber) {
        inlineKeyboardRows.clear();
        List<String> fileNameList = getPartNameList().get(pageNumber);
        int count = 0;
        //кнопка с колбэком на кнопку фильма
        for (String fileNameYear : fileNameList) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            String fileName = fileNameYear.replaceAll("\\s*\\(\\d{4}\\)$", "");
            keyboardRow.add(InlineKeyboardButton.builder().text(fileNameYear).callbackData(getCallbackName(fileName) + "a").build());
            inlineKeyboardRows.add(keyboardRow);
        }
        //кнопка "далее" + колбэк на 1ю страницу
        int totalPages = getPartNameList().size();
        if (inlineKeyboardRows.size() == 10 && pageNumber < totalPages - 1 && pageNumber == 0) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("Далее ▶").callbackData(getCallback(pageNumber + 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }
        //кнопка "назад" и "далее" + колбэк на средние страницы
        if (inlineKeyboardRows.size() == 10 && pageNumber >= 1 && pageNumber < totalPages - 1) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("◀ Назад").callbackData(getCallback(pageNumber - 1)).build());
            keyboardRow.add(InlineKeyboardButton.builder().text("Далее ▶").callbackData(getCallback(pageNumber + 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }
        //кнопка "назад" + колбэк на последнюю страницу
        if (inlineKeyboardRows.size() <= 10 && pageNumber == totalPages - 1 && totalPages > 1) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            keyboardRow.add(InlineKeyboardButton.builder().text("◀ Назад").callbackData(getCallback(pageNumber - 1)).build());
            inlineKeyboardRows.add(keyboardRow);
        }
    }

    private static List<InlineKeyboardRow> getInlineKeyboardRows() {
        return inlineKeyboardRows;
    }
    private static List<String> getFullNameList() {
        return fullNameList;

    }

    //создание общего списка фильмов по альфавиту
    private static void setFullNameList() {
        fullNameList.clear();
        String nameColl = "name";
        MongoCollection<Document> mongoNameColl = db.getCollection(nameColl);
        List<Document> nameList = mongoNameColl.find().into(new ArrayList<>());
        for (Document document : nameList) {
            for (String key : document.keySet()) {
                String value = document.get(key).toString();
                if (key.equals(value)) {
                    fullNameList.add(value);
                }
            }
        }
        Collections.sort(fullNameList);
    }

    private static List<List<String>> getPartNameList() {
        return partNameList;
    }

    //метод сетает список страниц по 10 фильмов
    private static void setPartNameList(List<String> fullNameList) {
        partNameList.clear();
        int partSize = 10;
        int fullListSize = fullNameList.size();
        int partCount = (int) Math.ceil((double) fullListSize / partSize);

        for (int i = 0; i < partCount; i++) {
            int startIndex = i * partSize;
            int endIndex = Math.min((i + 1) * partSize, fullListSize);
            List<String> part = new ArrayList<>(fullNameList.subList(startIndex, endIndex));
            partNameList.add(part);
        }
    }

    private static Map<Integer, Map<String, String>> getCallbackMap() {
        return callbackMap;
    }

    //метод сетает колбэки кнопок "вперед" и "назад" с привязкой к номеру сраницы
    private static void setCallbackMap() {
        callbackMap.clear();
        int genrePartListCount = getPartNameList().size();
        String callbackName = "films";
        for (int i = 0; i < genrePartListCount; i++) {
            String callBackGenre = callbackName + "_" + i;
            Map<String, String> map = new HashMap<>();
            map.put("List", callBackGenre);
            callbackMap.put(i, map);
        }
    }

    private static String getCallback(int pageNumber) {
        Map<String, String> map = getCallbackMap().get(pageNumber);
        if (map != null) {
            return map.get("List");
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
//    public static void setMongo() {
//        String ctn ="ctn";
//        String ntc ="ntc";
//        int count = 0;
//        MongoCollection<Document> ctnColl = db.getCollection(ctn);
//        MongoCollection<Document> ntcColl = db.getCollection(ntc);
//        for (String string : getFullNameList()) {
//            count++;
//            Document doc = new Document(count + "a", string);
//            Document doc1 = new Document( string, count);
//            ctnColl.insertOne(doc);
//            ntcColl.insertOne(doc1);
//        }
//    }
}
