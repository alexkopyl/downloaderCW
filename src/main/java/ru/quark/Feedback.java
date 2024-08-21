package ru.quark;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Feedback {
    private final String uri = "mongodb://admin:pass@127.0.0.1:27017/db_name?authSource=admin";
    private final String idDB = "db_name";
    private final MongoClient mongoClient = MongoClients.create(uri);
    private final MongoDatabase db = mongoClient.getDatabase(idDB);

    public Feedback() {
    }

    private MongoDatabase getDb() {
        return db;
    }

    public void setFeedBack(String username, String message) {
        String feedbackColl = "feedback";
        MongoCollection<Document> feedbackDocColl = getDb().getCollection(feedbackColl);
        Document messageDoc = new Document(username, message);
        feedbackDocColl.insertOne(messageDoc);
//        mongoClient.close();
    }
}
