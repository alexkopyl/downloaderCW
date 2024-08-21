package ru.quark;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.ArrayList;
import java.util.List;

public class Donate {
    private static final String uri = "mongodb://admin:KBerf016@127.0.0.1:27017/cinema_wines?authSource=admin";
    private static final String idDB = "cinema_wines";
    private static final MongoClient mongoClient = MongoClients.create(uri);
    private final static MongoDatabase db = mongoClient.getDatabase(idDB);

    public static SendAnimation donateMessageInit(long chat_id, String key) {
        String idAnim = "";
        String animationColl = "animation";
        MongoCollection<Document> mongoAnimColl = db.getCollection(animationColl);
        List<Document> nameList = mongoAnimColl.find().into(new ArrayList<>());
        for (Document document: nameList) {
            idAnim = document.get(key).toString();
        }
//        mongoClient.close();
        return SendAnimation.builder()
                .chatId(chat_id)
                .animation(new InputFile(idAnim))
                .caption("Если вы хотите поддержать наш проект, " +
                        "\nмы будем вам очень благодарны!" +
                        "\n\nПожертвования принимаем на наш адрес TON:" +
                        "\n\uD83D\uDCB8 \uD83D\uDCB0 \uD83D\uDCB5" +
                        "\nUQCRjFOugfba-5vBW8OC_HBcNg5vruCCapOgYn3TvZ6g36Ms" +
                        "\n" +
                        "\n\nСпасибо за вашу поддержку! \uD83D\uDE4F")
                .build();
    }
}
