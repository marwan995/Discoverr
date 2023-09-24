import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.PorterStemmer;
import com.github.luben.zstd.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Indexer {

    private static final int TheadsNumber = 15; // set number of threads
    private static MongoCollection<Document> Crawler;
    private static MongoCollection<Document> invertedIndex;
    private static MongoCursor<Document> cursor;
    private static ArrayList<String> StopWords;
    private static HashMap<String, Integer> HStopWords;
    private static HashMap<String, Integer> UrlsMap;
    private static PorterStemmer stemmer;
    private static MongoClient mongoClient;

    public static void main(String[] args) throws IOException, InterruptedException {

        long startTime = System.nanoTime();
        Brotli4jLoader.ensureAvailability(); // make sure Brotli4j is available
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(java.util.logging.Level.WARNING);
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("SearchEngine");
        Crawler = database.getCollection("Crawler");
        invertedIndex = database.getCollection("inverted_index1");
        invertedIndex.createIndex(new Document("key", 1));
        cursor = Crawler.find().iterator();
        StopWords = new ArrayList<String>((Arrays.asList(new String[]{"a", "about", "above", "across", "after", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "among", "an", "and", "another", "any", "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around", "as", "ask", "asked", "asking", "asks", "at", "away", "b", "back", "backed", "backing", "backs", "be", "became", "because", "become", "becomes", "been", "before", "began", "behind", "being", "beings", "best", "better", "between", "big", "both", "but", "by", "c", "came", "can", "cannot", "case", "cases", "certain", "certainly", "clear", "clearly", "come", "could", "d", "did", "differ", "different", "differently", "do", "does", "done", "down", "down", "downed", "downing", "downs", "during", "e", "each", "early", "either", "end", "ended", "ending", "ends", "enough", "even", "evenly", "ever", "every", "everybody", "everyone", "everything", "everywhere", "f", "face", "faces", "fact", "facts", "far", "felt", "few", "find", "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", "furthering", "furthers", "g", "gave", "general", "generally", "get", "gets", "give", "given", "gives", "go", "going", "good", "goods", "got", "great", "greater", "greatest", "group", "grouped", "grouping", "groups", "h", "had", "has", "have", "having", "he", "her", "here", "herself", "high", "high", "high", "higher", "highest", "him", "himself", "his", "how", "however", "i", "if", "important", "in", "interest", "interested", "interesting", "interests", "into", "is", "it", "its", "itself", "j", "just", "k", "keep", "keeps", "kind", "knew", "know", "known", "knows", "l", "large", "largely", "last", "later", "latest", "least", "less", "let", "lets", "like", "likely", "long", "longer", "longest", "m", "made", "make", "making", "man", "many", "may", "me", "member", "members", "men", "might", "more", "most", "mostly", "mr", "mrs", "much", "must", "my", "myself", "n", "necessary", "need", "needed", "needing", "needs", "never", "new", "new", "newer", "newest", "next", "no", "nobody", "non", "noone", "not", "nothing", "now", "nowhere", "number", "numbers", "o", "of", "off", "often", "old", "older", "oldest", "on", "once", "one", "only", "open", "opened", "opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", "others", "our", "out", "over", "p", "part", "parted", "parting", "parts", "per", "perhaps", "place", "places", "point", "pointed", "pointing", "points", "possible", "present", "presented", "presenting", "presents", "problem", "problems", "put", "puts", "q", "quite", "r", "rather", "really", "right", "right", "room", "rooms", "s", "said", "same", "saw", "say", "says", "second", "seconds", "see", "seem", "seemed", "seeming", "seems", "sees", "several", "shall", "she", "should", "show", "showed", "showing", "shows", "side", "sides", "since", "small", "smaller", "smallest", "so", "some", "somebody", "someone", "something", "somewhere", "state", "states", "still", "still", "such", "sure", "t", "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore", "these", "they", "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts", "three", "through", "thus", "to", "today", "together", "too", "took", "toward", "turn", "turned", "turning", "turns", "two", "u", "under", "until", "up", "upon", "us", "use", "used", "uses", "v", "very", "w", "want", "wanted", "wanting", "wants", "was", "way", "ways", "we", "well", "wells", "went", "were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose", "why", "will", "with", "within", "without", "work", "worked", "working", "works", "would", "x", "y", "year", "years", "yet", "you", "young", "younger", "youngest", "your", "yours"})));
        HStopWords = new HashMap<String, Integer>();
        UrlsMap = new HashMap<>();
        MongoCursor<Document> cursor1 = Crawler.find(new Document()).projection(new Document("url", 1)).iterator();
        Integer x = 1;
        while (cursor1.hasNext()) {
            Document doc = cursor1.next();
            String url = doc.getString("url");
            UrlsMap.put(url, x);
            x++;
        }
        stemmer = new PorterStemmer();
        for (String SW : StopWords)
            HStopWords.put(SW, 1);

        Thread[] threads = new Thread[TheadsNumber];
        for (int i = 0; i < TheadsNumber; i++) {
            threads[i] = new Thread(new Indexing());
            threads[i].start();
        }

        for (int i = 0; i < TheadsNumber; i++) {
            threads[i].join();
        }

        cursor.close();
        long endTime = System.nanoTime();
        long durationInNano = (endTime - startTime);
        double durationInSeconds = (double) durationInNano / 1_000_000_000.0;

        System.out.println("Duration in seconds: " + durationInSeconds);
    }

    public static HashMap<String, Integer> convert(String[] arr) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String s : arr) {
            s = s.toLowerCase();
            if (map.containsKey(s)) {
                map.put(s, map.get(s) + 1);
            } else {
                map.put(s, 1);
            }
        }
        return map;
    }


    public static boolean Is_Contain(ArrayList<Document> documentArray, String value) {
        for (Document document : documentArray) {
            if (document.get("url").equals(value)) {
                return true;
            }
        }
        return false;
    }
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BrotliOutputStream brotliOutputStream = new BrotliOutputStream(baos);
        brotliOutputStream.write(data);
        brotliOutputStream.close();
        return baos.toByteArray();
    }
    private static class Indexing implements Runnable {
        public Set<Document> Tag_Docements(String term, Document doc) throws IOException {
            Elements tags = Jsoup.parse(doc.toJson()).select(":containsOwn(" + term + ")");
            Set<Document> tagDocuments = new HashSet<Document>(); // create array of tag documents
            for (Element tag : tags) {
                if (tag.tagName().equals("body")) continue;
                String content = tag.text();
                byte[] compressedContent = compress(content.getBytes()); // compress the content using gzip
                Document tagDoc = new Document("name", tag.tagName()).append("content", compressedContent); // create document for each tag
                tagDocuments.add(tagDoc);
            }
            return tagDocuments;
        }
        private byte[] compressGzip(String content){
                return  Zstd.compress(content.getBytes());
        }


        @Override
        public void run() {

            while (true) {
                try {
                    Document doc;
                    synchronized (cursor) {
                        if (cursor.hasNext())
                            doc = cursor.next();
                        else
                            break;
                    }
                    String content = doc.getString("page");
                    content = content.replaceAll("<[^>]*>", "");
                    String[] words = content.split("\\W+");
                    String url = doc.getString("url");
                    HashMap<String, Integer> Map_count = convert(words);
                    Map_count.forEach((term, count) -> {
                        boolean isword = Pattern.compile("^(?:[\\d]+|[a-zA-Z']+|[\\d]+[a-zA-Z']+|[a-zA-Z']+[\\d]+(?![a-zA-Z']+))$").matcher(term).matches();
                        term = term.toLowerCase();
                        String stem = term;
                        if (!isword || (term.length() < 3 && !term.matches("-?\\d+(\\.\\d+)?"))|| HStopWords.containsKey(term)) return;
                        FindIterable<Document> check = invertedIndex.find(new Document("key", term));
                        if (!check.iterator().hasNext()) {// if the word doesn't exist
                            synchronized (stemmer) {
                                stemmer.setCurrent(stem);
                                stemmer.stem();
                                stem = stemmer.getCurrent();
                            }
                            Document word = new Document("key", term);
                            Set<Document> tagDocuments = null;
                            try {
                                tagDocuments = Tag_Docements(term, doc);
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                            if (tagDocuments.isEmpty()) return;
                            word.append("stem", stem)
                                    .append("WordUrls", Arrays.asList(new Document("url", url).append("count", count).append("title", doc.getString("title")).append("tags", tagDocuments))); // add tag documents to "tags" field
                            try {
                                invertedIndex.insertOne(word);
                            }
                            catch (MongoWriteException e){
                                Document update = new Document("$push", new Document("WordUrls", new Document("url", url).append("count", count).append("title", doc.getString("title")).append("tags", tagDocuments))); // add tag documents to "tags" field
                                invertedIndex.updateOne(new Document("key", term), update);
                            }
                        } else {
                            Document exist = check.first();
                            ArrayList<Document> array = (ArrayList<Document>) exist.get("WordUrls");
                            if (!Is_Contain(array, url)) {
                                Set<Document> tagDocuments = null;
                                try {
                                    tagDocuments = Tag_Docements(term, doc);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                                if (tagDocuments.isEmpty()) return;
                                Document update = new Document("$push", new Document("WordUrls", new Document("url", url).append("count", count).append("title", doc.getString("title")).append("tags", tagDocuments))); // add tag documents to "tags" field
                                invertedIndex.updateOne(new Document("key", term), update);
                            }
                        }
                    });
                    System.out.println(Thread.currentThread().getName() + " done url: " + url + "ranked: " + UrlsMap.get(url));
                } catch (Exception e) {
                    // handle the exception here
                    System.err.println("Error occurred: " + e.getMessage());
                }
            }
        }
    }
}

