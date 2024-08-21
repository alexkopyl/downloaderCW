package ru.quark;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class DownloaderBotConsumer implements LongPollingSingleThreadUpdateConsumer {
    private static final Logger logger = LogManager.getRootLogger();
    private final TelegramClient telegramClient;
    //переменные файлов
    private String fileCtn;
    private String fileId;
    private String fileName;
    private String fileYear;
    private String fileQuality;
    private String fileRating;
    private static final Set<String> usernameList = new HashSet<>();
    private final List <String> genreList = new ArrayList<>();

    static String uri = "mongodb://admin:pass@127.0.0.1:27017/db_name?authSource=admin";
    static String idDB = "db_name";
    static MongoClient mongoClient = MongoClients.create(uri);
    private static MongoDatabase db = mongoClient.getDatabase(idDB);

    //коллекции для выгрузки видео и параметров из БД
    private final MongoCollection<Document> ctnDocsColl;
    private final MongoCollection<Document> ntcDocsColl;
    private final MongoCollection<Document> idDocsColl;
    private final MongoCollection<Document> nameDocsColl;
    private final MongoCollection<Document> yearDocsColl;
    private final MongoCollection<Document> qualityDocsCall ;
    private final MongoCollection<Document> ratingDocsCall;
    private final MongoCollection<Document> usernameDocsCall;
    private final MongoCollection<Document> genreDocsColl;

    public DownloaderBotConsumer(String botToken, String uri, String idDB) {
        telegramClient = new OkHttpTelegramClient(botToken);
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase db = mongoClient.getDatabase(idDB);
        String ctnColl = "ctn";
        String ntcColl = "ntc";
        String idColl = "id";
        String nameColl = "name";
        String yearColl = "year";
        String genreColl = "genre";
        String qualityColl = "quality";
        String ratingColl = "rating";
        String usernameColl = "username";
        ctnDocsColl = db.getCollection(ctnColl);
        ntcDocsColl = db.getCollection(ntcColl);
        idDocsColl = db.getCollection(idColl);
        nameDocsColl = db.getCollection(nameColl);
        yearDocsColl = db.getCollection(yearColl);
        genreDocsColl = db.getCollection(genreColl);
        qualityDocsCall = db.getCollection(qualityColl);
        ratingDocsCall = db.getCollection(ratingColl);
        usernameDocsCall = db.getCollection(usernameColl);
    }

    @Override
    public void consume(Update update) {
        Genres.setCallbackDataGenreMap();
        //обработчик текста
        if (update.hasMessage() && update.getMessage().hasText()) {
            long text_chat_id = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
            String inMessage = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();
            logger.info("{}: {}", username, inMessage);
            if (username != null && !username.isEmpty()) {
                Document query = new Document("username", username);
                Document user = usernameDocsCall.find(query).first();
                if (user == null) {
                    Document newUser = new Document("username", username);
                    usernameDocsCall.insertOne(newUser);
                }
            }

            //Обработка ReplyMarkup кливиатуры
            try {
                switch (inMessage) {
                    case "/start", "◀️ Назад" -> {
                        ReplyKeyboard replyKeyboard = new ReplyKeyboard();
                        telegramClient.executeAsync(replyKeyboard.filmSerialInstructionKeyboard(text_chat_id));
                    }
                    case "\uD83D\uDCE7Обратная связь\uD83D\uDEF0" -> {
                        ReplyKeyboard replyKeyboard = new ReplyKeyboard();
                        telegramClient.executeAsync(replyKeyboard.feedbackKeyboard(text_chat_id));
                    }
                    case "\uD83E\uDD19Написать нам" -> {
                        SendMessage sendMessage = SendMessage.builder()
                                .chatId(text_chat_id)
                                .text("Чтобы связаться с нами, \nотправьте боту сообщение в виде \n'/r текст сообщения'" +
                                        "\nВ тексте сообщения вы можете задать \nинтересующий вас вопрос, \nа так же оставить жалобу.")
                                .build();
                        telegramClient.execute(sendMessage);
                    }
                    case "\uD83D\uDCB0Помощь проекту" -> {
                        telegramClient.executeAsync(Donate.donateMessageInit(text_chat_id, "donate"));
                        ReplyKeyboard replyKeyboard = new ReplyKeyboard();
                        telegramClient.executeAsync(replyKeyboard.qrCodeButton(text_chat_id));

                    }
                    case "\uD83C\uDFACФильмы\uD83D\uDCFD" -> {
                        ReplyKeyboard replyKeyboard = new ReplyKeyboard();
                        telegramClient.executeAsync(replyKeyboard.genresSearchListFilmKeyboard(text_chat_id));
                    }
                    case "\uD83C\uDF9EЖанры" -> {
                        telegramClient.executeAsync(Genres.genresFilmsList1Keyboard(text_chat_id));
                    }
                    case "\uD83D\uDCCBСписок" -> {
                        telegramClient.executeAsync(Checklist.filmInlineKeyboardInitSM(text_chat_id, messageId, 0));
                    }
                    case "\uD83D\uDD0EПоиск" -> {
                        SendMessage sendMessage = SendMessage.builder()
                                .chatId(text_chat_id)
                                .text("Чтобы найти фильм, \nотправьте боту его полное название, \nлибо часть")
                                .build();
                        telegramClient.executeAsync(sendMessage);
                    }
                    case "/online" -> {
                        logger.warn("Пользователей: {}", usernameDocsCall.countDocuments());
                    }
                    case "/filmcount" -> {
                        logger.warn("Фильмов: {}", idDocsColl.countDocuments());
                    }
                    case "QR код кошелька" -> {
                        telegramClient.executeAsync(SendPhoto.builder()
                                .chatId(text_chat_id)
                                .photo(new InputFile("AgACAgIAAxkBAAIqy2ZvDkgwsxM5kKnZPZgB_atUMz0lAAI-3zEb5n55SwRxPRbYyOxiAQADAgADcwADNQQ"))
                                .build());
                    }
                    default -> {
                        try {
                            telegramClient.executeAsync(Search.filmInlineKeyboardInit(text_chat_id, 0, inMessage));
                            fileId = null;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (inMessage.equals("/start")) {
                    usernameList.add(username);
                    logger.info("localUsers: {}", usernameList.size());
                }
                if (inMessage.contains("/r")) {
                    String[] msgArr = inMessage.split("/r ");
                    Feedback feedback = new Feedback();
                    feedback.setFeedBack(username, msgArr[1]);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(text_chat_id)
                            .text("Спасибо, ваше сообщение отправлено админу!")
                            .build();
                    telegramClient.executeAsync(sendMessage);
                }
            } catch (TelegramApiException e) {
                System.out.println("Ошибка при выполнении команды");
                e.printStackTrace();
            }
        }

        //обработчик callbackQuery (кнопок inline клавиатуры)
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long callback_chat_id = callbackQuery.getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String showName = callbackQuery.getData().replaceAll("\\s*\\(\\d{4}\\)$", "");
            String username = callbackQuery.getFrom().getUserName();
            String showNameFilm = getShowName(callbackQuery.getData());
            try {
                switch (showName) {
                    case "down2List" -> {
                        System.out.println(username + ": ");
                        telegramClient.executeAsync(Genres.genresFilmsList1Keyboard(callback_chat_id));
                    }
                    case "up1List", "down3List" -> {
                        System.out.println(username + ": ");
                        telegramClient.executeAsync(Genres.genresFilmsList2Keyboard(callback_chat_id));
                    }
                    case "up2List" -> {
                        System.out.println(username + ": ");
                        telegramClient.executeAsync(Genres.genresFilmsList3Keyboard(callback_chat_id));
                    }
                }
                Map<String, String> genreMap = Map.ofEntries(
                        Map.entry("Комедии", "Комедии"),
                        Map.entry("Мультфильмы", "Мультфильмы"),
                        Map.entry("Ужасы", "Ужасы"),
                        Map.entry("Фантастика", "Фантастика"),
                        Map.entry("Триллеры", "Триллеры"),
                        Map.entry("Боевики", "Боевики"),
                        Map.entry("Мелодрамы", "Мелодрамы"),
                        Map.entry("Детективы", "Детективы"),
                        Map.entry("Музыкальные", "Музыкальные"),
                        Map.entry("Мюзиклы", "Мюзиклы"),
                        Map.entry("Приключения", "Приключения"),
                        Map.entry("Фэнтези", "Фэнтези"),
                        Map.entry("Военные", "Военные"),
                        Map.entry("Семейные", "Семейные"),
                        Map.entry("Аниме", "Аниме"),
                        Map.entry("Исторические", "Исторические"),
                        Map.entry("Драмы", "Драмы"),
                        Map.entry("Детские", "Детские"),
                        Map.entry("Криминал", "Криминал"),
                        Map.entry("Биографии", "Биографии"),
                        Map.entry("Вестерны", "Вестерны"),
                        Map.entry("Спортивные", "Спортивные")
                );
                //обработчик кнопок "назад" и "далее"
                genreMap.forEach((genre, value) -> {
                    if (showName.contains(genre)) {
                        String[] callbackParts = showName.split("_");
                        try {
                            if (callbackParts.length > 1) {
                                System.out.println(callbackParts[1]);
                                telegramClient.executeAsync(InlineKeyboard.filmListToGenresInlineKeyboardEM(
                                        value,
                                        callback_chat_id,
                                        messageId,
                                        Integer.parseInt(callbackParts[1])));
                            } else {
                                telegramClient.executeAsync(InlineKeyboard.filmListToGenresInlineKeyboardSM(
                                        value,
                                        callback_chat_id,
                                        messageId,
                                        0
                                ));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.warn(e);
                        }
                    }
                });
                if (showName.contains("films")) {
                    String[] callbackParts = showName.split("_");
                    telegramClient.executeAsync(Checklist.filmInlineKeyboardInitEM(callback_chat_id, messageId, Integer.parseInt(callbackParts[1])));
                }

                setDocData(showNameFilm);
                if (fileId != null) {
                    SendVideo sendVideo = SendVideo.builder()
                            .chatId(callback_chat_id)
                            .video(new InputFile(getFileId()))
                            .caption(getFileName().toUpperCase() + " " + getFileYear() +
                                    "\n" + getFileQuality() +
                                    "\n" + getFileRating())
                            .build();
                    telegramClient.executeAsync(sendVideo);
                    logger.info("{} запустил {}", username, getFileName() + getFileYear());
                    fileId = null;
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        //обработчик загрузки видео на сервера телеграм uploaderCW
        if (update.hasMessage() && update.getMessage().hasVideo()) {
            Video video = update.getMessage().getVideo();
            String username = update.getMessage().getFrom().getUserName();
            if (video != null) {
                //partsColl[] -> 0-name, 1-year, 2-genreList(genre1,genre2) 3-quality 4-rating 5-count
                String[] partsColl = update.getMessage().getCaption().split("/");
                if (partsColl.length == 6) {
                    Document idDoc = new Document(partsColl[0], video.getFileId());
                    Document nameDoc = new Document(partsColl[0], partsColl[0]);
                    Document yearDoc = new Document(partsColl[0], partsColl[1]);
                    String[] genreArray= partsColl[2].split(",");
                    setGenreList(genreArray);
                    for (String genre : getGenreList()) {
                        Document genreDoc = new Document(genre, partsColl[0]);
                        genreDocsColl.insertOne(genreDoc);
                    }
                    Document qualityDoc = new Document(partsColl[0], partsColl[3]);
                    Document ratingDoc = new Document(partsColl[0], partsColl[4]);
                    Document ntcDoc = new Document(partsColl[0], partsColl[5]);
                    Document ctnDoc = new Document(partsColl[5] + "a", partsColl[0]);
                    logger.warn("Загружено видео: {}", update.getMessage().getCaption());
                    idDocsColl.insertOne(idDoc);
                    nameDocsColl.insertOne(nameDoc);
                    yearDocsColl.insertOne(yearDoc);
                    qualityDocsCall.insertOne(qualityDoc);
                    ratingDocsCall.insertOne(ratingDoc);
                    ntcDocsColl.insertOne(ntcDoc);
                    ctnDocsColl.insertOne(ctnDoc);
                } else {
                    logger.warn("Попытка загрузки видео не по формату! :{}", username);
                }
            }
        }
    }

    private String getFileId() {
        return fileId;
    }

    private String getFileName() {
        return fileName;
    }

    private String getFileYear() {
        return fileYear;
    }

    private String getFileQuality() {
        return fileQuality;
    }

    private String getFileRating() {
        return fileRating;
    }

    private void setDocData(String callbackData) {
        List<Document> idList = idDocsColl.find().into(new ArrayList<>());
        List<Document> nameList = nameDocsColl.find().into(new ArrayList<>());
        List<Document> yearList = yearDocsColl.find().into(new ArrayList<>());
        List<Document> qualityList = qualityDocsCall.find().into(new ArrayList<>());
        List<Document> ratingList = ratingDocsCall.find().into(new ArrayList<>());
        for (Document document : idList) {
            if (document.get(callbackData) != null) {
                fileId = document.get(callbackData).toString();
            }
        }
        for (Document document : nameList) {
            if (document.get(callbackData) != null) {
                fileName = document.get(callbackData).toString();
            }
        }
        for (Document document : yearList) {
            if (document.get(callbackData) != null) {
                fileYear = document.get(callbackData).toString();
            }
        }
        for (Document document : qualityList) {
            if (document.get(callbackData) != null) {
                fileQuality = document.get(callbackData).toString();
            }
        }
        for (Document document : ratingList) {
            if (document.get(callbackData) != null) {
                fileRating = document.get(callbackData).toString();
            }
        }
    }
    private String getShowName(String callbackFilm) {
        String ntcColl = "ctn";
        String ctn = "";
        MongoCollection<Document> mongoIdColl = db.getCollection(ntcColl);
        List<Document> idList = mongoIdColl.find().into(new ArrayList<>());
        for (Document document : idList) {
            for (String keyStr : document.keySet()) {
                if (Objects.equals(keyStr, callbackFilm)) {
                    ctn = document.get(callbackFilm).toString();
                }
            }
        }
        return ctn;
    }

    public void setGenreList(String[] genreArray) {
        genreList.clear();
        genreList.addAll(Arrays.asList(genreArray));
    }
    public List<String> getGenreList() {
        return genreList;
    }
}
