package SearchEngineAPT.Search;

import org.bson.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static SearchEngineAPT.Search.TfIdfRanker.tagWeights;
import static java.lang.Math.log;

public class UrlThread extends Thread {
    private final Set<String> query;

    private final Set<String>keys;
    private final Map<String, Double> urlScores;
    private final Map<String, Integer> HStopWords;
    private final mongoDB DB;
    private final String url;
    private static Double alldocsCount=0.0;
    private static Map<String, Document> WordsDocumentsMap;
    private  Document CrawlDoc;

    public UrlThread(String url, Set<String> keys, String[] terms, Map<String, Double> urlScores, Map<String, Integer> HStopWords,
                     mongoDB DB, Double alldocsCount, Map<String, Document> termDoc, Document Crawldoc) {
        this.query = Arrays.stream(terms).collect(Collectors.toSet());
        this.urlScores = urlScores;
        this.url=url;
        this.HStopWords = HStopWords;
        this.keys=keys;
        this.DB = DB;
        this.alldocsCount = alldocsCount;
        this.WordsDocumentsMap = termDoc;
        this.CrawlDoc=Crawldoc;
    }

    @Override
    public void run() {
        if(urlScores.containsKey(url)){
        return;
        }
        double score = calculateScore();
        
        urlScores.put(url, score);
    }

    private double calculateScore() {
        double sum = 0;
        int matched_word = 0;
        for (String s : keys) {
            if (HStopWords.containsKey(s)) continue;
            double res = (inverseDocumentFrequency(s) * termFrequency(s, url));
            if(s.matches("[0-9]")) {
                res /= 2;
                if(keys.size()==1)
                    res /= 4;

            }
            if(query.contains(s)){
                matched_word++;
                sum += res;
            }
            else
                sum += res*0.8;//this score from stem not actual word

        }
        return sum+matched_word;
    }
    public static double inverseDocumentFrequency(String term ) {
        try {
                Document TermDoc=WordsDocumentsMap.get(term);
                List<Document> matchedWordUrls = TermDoc.getList("WordUrls", Document.class);
                double term_count = matchedWordUrls.size();
                double score = log((double) alldocsCount / term_count);
                return (double) score;

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return 0;
    }
    double termFrequency(String term, String url)  {
            double count = 0.0;
            Document TermDoc=WordsDocumentsMap.get(term);
            Document matchedWordUrl =getDocumentByUrl( TermDoc.getList("WordUrls", Document.class),url);
            double len_of_document=CrawlDoc.getInteger("WordsCount");
            double last_count = matchedWordUrl.getInteger("count");
            List<Document> tags = matchedWordUrl.getList("tags", Document.class);
            for (Document tag : tags) {
                if (tagWeights.containsKey(tag.getString("name")))
                    count += (last_count * tagWeights.get(tag.getString("name")));
                else
                    count += (last_count * .6);//unknow tag
            }
            if (count <= 0)
                count = last_count;
            if (CrawlDoc.getString("title").toLowerCase().contains(term))
                count *= 3;

        if(CrawlDoc.getString("url").toLowerCase().contains(term)) count *= 4;
        double score=((double) count / len_of_document);
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if(domain.contains(term))score += 3;
        } catch (URISyntaxException e) {}
            return score;
    }
    public static Document getDocumentByUrl(List<Document> documents, String targetUrl) {
        Map<String, Document> documentMap = new HashMap<>();
        for (Document document : documents) {
            documentMap.put(document.getString("url"), document);
        }
        return documentMap.get(targetUrl);
    }


}

