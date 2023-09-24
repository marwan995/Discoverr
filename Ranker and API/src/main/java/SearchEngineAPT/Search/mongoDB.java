package SearchEngineAPT.Search;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

public class mongoDB {

    public static MongoClient mongoClient = new MongoClient("localhost", 27017);
    public static MongoDatabase database = mongoClient.getDatabase("SearchEngine");
    public static MongoCollection<Document> QuaryList = database.getCollection("Querys");
    public static Set<String> QuarylistSet = new HashSet<>();
    public mongoDB() {

        MongoCursor<Document> cursor = QuaryList.find().iterator();
        while (cursor.hasNext())
            QuarylistSet.add(cursor.next().getString("Quary"));
    }
    MongoDatabase getDB() {
        return database;
    }

    void Add_to_QuaryList(String q) {
        QuarylistSet.add(q);
        try {
            QuaryList.insertOne(new Document("Quary", q));
        }
        catch (MongoWriteException e){}
    }

    Set<String> GetQuearyList() {
        return QuarylistSet;
    }

}
