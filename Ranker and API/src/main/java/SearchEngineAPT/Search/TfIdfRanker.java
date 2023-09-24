package SearchEngineAPT.Search;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.in;

public class TfIdfRanker {
    static mongoDB DB = new mongoDB();
    private static MongoCollection<Document> inverted_index=DB.getDB().getCollection("inverted_index1");
    private static MongoCollection<Document> Crawler = DB.getDB().getCollection("Crawler");
    private static ArrayList<String> StopWords;
    private static HashMap<String, Integer> HStopWords;
    private static PorterStemmer stemmer = new PorterStemmer();

    // Initialize maps and sets to store data
    static Map<String, Double> urlScores = new ConcurrentHashMap<>(); // Maps URL to its relevance score
    static Map<String, String> urlTags = new ConcurrentHashMap<>(); // Maps URL to its HTML tag content
    static Map<String, String> urlTitles = new ConcurrentHashMap<>(); // Maps URL to its title
    static HashSet<String> uniqueStems = new HashSet<>(); // Stores unique stemmed query terms
    static HashSet<String> uniqueUrls = new HashSet<>(); // Stores unique URLs that contain one or more query terms
    static Map<String, String> stemToWord = new ConcurrentHashMap<>(); // Maps stemmed term to its original unstemmed form
    static Map<String, Map<String,Integer>> urlWords = new ConcurrentHashMap<>(); // Maps URL to the set of query terms it contains
    static Map<String, Set<tagRanker>> urlDes = new ConcurrentHashMap<>();
    static Map<String, Document> wordDocuments = new ConcurrentHashMap<>();
    public static Map<String, Double> tagWeights= new HashMap<>();
    static Map<String ,Document> CrawlerDocumentsMap=new HashMap<>();

    public static double alldocsCount = Crawler.countDocuments();


    public TfIdfRanker() {
        this.StopWords = new ArrayList<String>((Arrays.asList(new String[]{"a", "about", "above", "across", "after", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "among", "an", "and", "another", "any", "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around", "as", "ask", "asked", "asking", "asks", "at", "away", "b", "back", "backed", "backing", "backs", "be", "became", "because", "become", "becomes", "been", "before", "began", "behind", "being", "beings", "best", "better", "between", "big", "both", "but", "by", "c", "came", "can", "cannot", "case", "cases", "certain", "certainly", "clear", "clearly", "come", "could", "d", "did", "differ", "different", "differently", "do", "does", "done", "down", "down", "downed", "downing", "downs", "during", "e", "each", "early", "either", "end", "ended", "ending", "ends", "enough", "even", "evenly", "ever", "every", "everybody", "everyone", "everything", "everywhere", "f", "face", "faces", "fact", "facts", "far", "felt", "few", "find", "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", "furthering", "furthers", "g", "gave", "general", "generally", "get", "gets", "give", "given", "gives", "go", "going", "good", "goods", "got", "great", "greater", "greatest", "group", "grouped", "grouping", "groups", "h", "had", "has", "have", "having", "he", "her", "here", "herself", "high", "high", "high", "higher", "highest", "him", "himself", "his", "how", "however", "i", "if", "important", "in", "interest", "interested", "interesting", "interests", "into", "is", "it", "its", "itself", "j", "just", "k", "keep", "keeps", "kind", "knew", "know", "known", "knows", "l", "large", "largely", "last", "later", "latest", "least", "less", "let", "lets", "like", "likely", "long", "longer", "longest", "m", "made", "make", "making", "man", "many", "may", "me", "member", "members", "men", "might", "more", "most", "mostly", "mr", "mrs", "much", "must", "my", "myself", "n", "necessary", "need", "needed", "needing", "needs", "never", "new", "new", "newer", "newest", "next", "no", "nobody", "non", "noone", "not", "nothing", "now", "nowhere", "number", "numbers", "o", "of", "off", "often", "old", "older", "oldest", "on", "once", "one", "only", "open", "opened", "opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", "others", "our", "out", "over", "p", "part", "parted", "parting", "parts", "per", "perhaps", "place", "places", "point", "pointed", "pointing", "points", "possible", "present", "presented", "presenting", "presents", "problem", "problems", "put", "puts", "q", "quite", "r", "rather", "really", "right", "right", "room", "rooms", "s", "said", "same", "saw", "say", "says", "second", "seconds", "see", "seem", "seemed", "seeming", "seems", "sees", "several", "shall", "she", "should", "show", "showed", "showing", "shows", "side", "sides", "since", "small", "smaller", "smallest", "so", "some", "somebody", "someone", "something", "somewhere", "state", "states", "still", "still", "such", "sure", "t", "take", "taken", "than", "that", "the", "their", "them", "then", "there", "therefore", "these", "they", "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts", "three", "through", "thus", "to", "today", "together", "too", "took", "toward", "turn", "turned", "turning", "turns", "two", "u", "under", "until", "up", "upon", "us", "use", "used", "uses", "v", "very", "w", "want", "wanted", "wanting", "wants", "was", "way", "ways", "we", "well", "wells", "went", "were", "what", "when", "where", "whether", "which", "while", "who", "whole", "whose", "why", "will", "with", "within", "without", "work", "worked", "working", "works", "would", "x", "y", "year", "years", "yet", "you", "young", "younger", "youngest", "your", "yours"})));
        this.HStopWords = new HashMap<String, Integer>();
        for (String SW : StopWords)
            HStopWords.put(SW, 1);
        alldocsCount = Crawler.countDocuments();
        tagWeights.put("h1", 2.0);
        tagWeights.put("h2", 1.5);
        tagWeights.put("h3", 1.5);
        tagWeights.put("h4", 1.2);
        tagWeights.put("h5", 1.0);
        tagWeights.put("h6", 0.8);
        tagWeights.put("div", 1.0);
        tagWeights.put("a", 0.8);
        tagWeights.put("p", 0.8);
        tagWeights.put("ul", 0.5);
        tagWeights.put("ol", 0.5);
        tagWeights.put("table", 2.0);
        tagWeights.put("img", 1.5);
        tagWeights.put("form", 1.5);
        tagWeights.put("label", 1.0);
        tagWeights.put("input", 0.5);
        tagWeights.put("textarea", 1.2);

    }

    public List<Document> mergeAnd(List<Document>a,List<Document>b){//o of n^2
        List<Document> answer=new ArrayList<>();
        int k=0;
        for (int i = 0; i < a.size(); i++)
        {
            Document s1=a.get(i);

            for (int j = 0; j < b.size(); j++)
            {
                Document s2=b.get(j);

                if (s1.getString("url").equals(s2.getString("url"))) {
                    k++;
                    s1.put("body", s1.getString("body") + " " + s2.getString("body"));
                    s1.put("score", s1.getDouble("score") + s2.getDouble("score"));
                    // Append the key list
                    Set<String> keySet = new HashSet<>();
                    keySet.addAll(s1.get("key", Set.class));
                    keySet.addAll(s2.get("key", Set.class));
                    s1.put("key", keySet);
                    answer.add(s1);
                    // add common elements
                    break;
                }
            }
        }
        System.out.println(answer.size()+" "+k);
        return answer;
    }
    public void mergeNot(List<Document>a){
        for(int i=0;i<a.size();i++){
            Document x=  a.get(i);
            x.put("score",(-10*a.get(i).getDouble("score")));
            a.set(i,x);
        }
    }
    public void mergeOr(List<Document>a,List<Document>b){//o of n^2

        Map<String,Document>tempMap=new LinkedHashMap<>();
        for(Document d:a)
            tempMap.put(d.getString("url"),d);



        for (int j = 0; j <b.size() ; j++) {
            Document s2=b.get(j);
            if(tempMap.containsKey(s2.getString("url"))){
                Document s1=tempMap.get(s2.getString("url"));
                s1.put("body", s1.getString("body") + " " + s2.getString("body"));
                s1.put("score", s1.getDouble("score") + s2.getDouble("score"));
                // Append the key list
                Set<String> keySet = new HashSet<>();
                keySet.addAll(s1.get("key", Set.class));
                keySet.addAll(s2.get("key", Set.class));
                s1.put("key", keySet);
                for(int i=0;i<a.size();i++){
                    if(a.get(i).getString("url").equals(s2.getString("url"))){
                        a.set(i,s1);
                        break;
                    }
                }

            }
            else {
                a.add(s2);
            }

        }

    }
    public List<Document> RankAll(String query) {
        List<String> quotedStrings  = Arrays.stream(query.split("(?<=')\\s+|\\s+(?=')")).filter(s -> s.startsWith("'") && s.endsWith("'")).map(s -> s.replaceAll("^'|'$", "")).collect(Collectors.toList());
        boolean isSpecial=quotedStrings.size()!=0;
        String q=isSpecial?quotedStrings.get(0):"";
        String [] normalQuery=query.replaceAll("'[^']*'", "").split(" ");
        String [] SpecialQuery=q.split(" ");
        List<Document> Ndocs=rank(normalQuery,false,q);
        resetMaps();
        List<Document> Sdocs =rank(SpecialQuery,true,q);
        q=query.substring(q.length()+2).trim();
        resetMaps();
        for(int i=1;quotedStrings.size()>i;i++)
        {
            System.out.println(Sdocs.size());
            List<Document>x=rank(quotedStrings.get(i).split(" "), true, quotedStrings.get(i));
            System.out.println(x.size());
            System.out.println(Sdocs.size());
            System.out.println(q);
            if(q.startsWith("and")) {
                q=q.substring(4).trim();
                Sdocs = mergeAnd(Sdocs,x);
            }

            else if(q.startsWith("or")) {
              q=q.substring(3).trim();
//                Sdocs.addAll(x);
                mergeOr(Sdocs,x);
            }
            else if(q.startsWith("not")){
                q=q.substring(4).trim();
                mergeNot(x);
                mergeOr(Sdocs,x);
            }
            resetMaps();
            q=q.substring(quotedStrings.get(i).length()+2).trim();


        }

        resetMaps();
        return answerList(Ndocs,Sdocs);
    }
    public List<Document>answerList( List<Document> Ndocs, List<Document> Sdocs){
        List<Document> combinedDocs = new ArrayList<>();
        for(Document doc : Sdocs) {
            combinedDocs.add(doc);
        }
        for(Document doc : Ndocs) {
            boolean urlExists = false;
            for(Document combinedDoc : combinedDocs) {
                if(doc.getString("url").equals(combinedDoc.getString("url"))) {
                    combinedDoc.put("score",combinedDoc.getDouble("score") + doc.getDouble("score"));
                    urlExists = true;
                    break;
                }
            }
            if(!urlExists) {
                combinedDocs.add(doc);
            }
        }
        combinedDocs.sort((doc1, doc2) -> doc2.getDouble("score").compareTo(doc1.getDouble("score")));
        return combinedDocs;
    }
    public static List<Document> rank(  String[]  QueryTerms,boolean isSpecial,String q) {

        int maxThreads = 8;

        // Loop through each query term and add its stemmed form to the set of unique stems
        if(!isSpecial)
            SetuniqueStem(QueryTerms);

        Set<Document> invertedIndexDocuments = new HashSet<>();
        //Get all the documents from db
        MongoCursor<Document> cursor = inverted_index.find(isSpecial?in("key", QueryTerms) :in("stem", uniqueStems)).iterator();

        // Retrieve inverted index documents that contain any of the unique stems

        if(isSpecial)
            SpecialDocs(cursor, invertedIndexDocuments,q);
        else
            normalDocument(invertedIndexDocuments,cursor,maxThreads);

        MongoCursor<Document> cursorCrawler = Crawler.find(in("url", uniqueUrls)).projection(new Document("url", 1).append("links", 1).append("WordsCount", 1).append("title",1)).iterator();//Get the Crwaler Docs
        ArrayList<Document> CrawlingDocuments = new ArrayList<>();

        SetCrawling(cursorCrawler,CrawlingDocuments);
        ExecuteDocument(urlWords.keySet(),maxThreads,QueryTerms);

        Map<String, Double> PageScore = new AdjacencyList(CrawlingDocuments).CalcultePageRank();
        List<Document> answer = new ArrayList<>();
        makeAnswer(PageScore,answer);
        //  resetMaps();
        return answer;
    }
    public static String[] SpecialQuery(List<String> resultList){
        Set<String> all = new LinkedHashSet<>();
        for(String r:resultList){
            String []arr= r.split(" ");
            for(String x :arr)
                all.add(x);

        }
        return  all.toArray(new String[all.size()]);
    }
    private static void resetMaps(){
        urlTags.clear();
        urlDes.clear();
        uniqueUrls.clear();
        urlWords.clear();
        urlScores.clear();
        uniqueStems.clear();
    }
    private static void SetCrawling(MongoCursor<Document>  cursorCrawler, ArrayList<Document> CrawlingDocuments ){
        while (cursorCrawler.hasNext()) {
            Document doc=cursorCrawler.next();
            CrawlingDocuments.add(doc);
            CrawlerDocumentsMap.put(doc.getString("url"),doc);
        }
    }

    private static void makeAnswer( Map<String, Double> PageScore ,List<Document>answer){
        double α = 0.6;
        for (String url : uniqueUrls) {
            double score = urlScores.get(url);
            score = (1 - α) * PageScore.get(url) + α * score;
            Set<String> key = urlWords.get(url).keySet();

            if(key.size()==1&&key.iterator().next().matches("[0-9]")) score/=4;//remove numbers only outputs
            String urltitle = urlTitles.get(url);
            String tagContent = urlTags.get(url).replace("\\n", " ");
            if (tagContent.length()<10)score/=4;

            Document Temp_store_the_document = new Document("score", score).append("key", key).append("url", url).append("title", urltitle).append("body", tagContent);

            answer.add(Temp_store_the_document);
        }
        //   answer.sort((doc1, doc2) -> doc2.getDouble("score").compareTo(doc1.getDouble("score")));
    }
    private static void ExecuteDocument(Set<String> urls,int maxThreads,String []QueryTerms){

            ExecutorService threadPool2 = Executors.newFixedThreadPool(maxThreads);
            for (String url:urls) {
                // Create a new UrlThread task and submit it to the thread pool
                UrlThread thread = new UrlThread(url, urlWords.get(url).keySet(), QueryTerms, urlScores, HStopWords, DB, alldocsCount, wordDocuments,CrawlerDocumentsMap.get(url));
                threadPool2.submit(thread);
            }
            threadPool2.shutdown();// Shut down the thread pool and wait for all tasks to complete
            try {
                threadPool2.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                System.out.println("Error in threading");
            }

    }
    private static void SetuniqueStem( String[] QueryTerms ){
        for (String term : QueryTerms) {
            if (HStopWords.containsKey(term)) continue; // Skip stop words
            stemmer.setCurrent(term);
            stemmer.stem();
            String stemmed = stemmer.getCurrent();
            stemToWord.put(stemmed, term);
            uniqueStems.add(stemmed);
        }
    }
    private static List<Document>SpecialDocs(MongoCursor<Document>  cur,Set<Document> invertedIndexDocuments,String q ){
        // collect documents with common URL
        Map<String,Integer> commonUrls = new ConcurrentHashMap<>();
        List<Document> result = new ArrayList<>();
        int cnt=0;
        while (cur.hasNext()) {
            Document doc = cur.next();
            for(Document x:doc.getList("WordUrls", Document.class)){
                String url=x.getString("url");
                if(commonUrls.containsKey(url))
                    commonUrls.put(url,commonUrls.get(url)+1);
                else
                    commonUrls.put(url,1);
            }
            result.add(doc);
            cnt+=doc.getList("WordUrls", Document.class).size();
        }
        // filter out URLs that are not common for all words
        List<Document> filteredResult = new ArrayList<>();
        for (Document doc : result) {
            List<Document> wordUrls = new ArrayList<>();
            for (Document url : doc.getList("WordUrls", Document.class))
                if (commonUrls.get(url.getString("url"))==result.size())
                    wordUrls.add(url);
            doc.put("WordUrls", wordUrls);
            filteredResult.add(doc);
        }

        for (Document Current :filteredResult){
            invertedIndexDocuments.add(Current);
            List<Document> wordUrls = Current.getList("WordUrls", Document.class);
            String KeyWord = Current.getString("key");
            wordDocuments.put(KeyWord, Current);

            for (Document one : wordUrls) {
                String url = one.getString("url");
                urlWords.computeIfAbsent(url, k -> new ConcurrentHashMap<>()).put(KeyWord,1);
                tagRanker Tags = new tagRanker( one.getList("tags", Document.class));
                urlDes.computeIfAbsent(url, k -> new LinkedHashSet<>()).add(Tags);
            }

            for (Document one : wordUrls) {
                String url = one.getString("url");
                tagRanker Tags = new tagRanker();
                String x=Tags.Description(urlDes.get(url), urlWords.get(url).keySet(),true,result.size(),q);
                if (!uniqueUrls.contains(url))
                    urlTitles.put(url, one.getString("title"));
                if(x.length()!=0){
                    urlTags.put(url,x);
                    uniqueUrls.add(url);
                }
            }

        }

        // return the document with the common urls only
        return filteredResult;
    }
    private static void normalDocument(  Set<Document> invertedIndexDocuments,MongoCursor<Document>cursor,Integer maxThreads){
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        ExtractDocuemnt threadPreProcessing = new ExtractDocuemnt(cursor, invertedIndexDocuments);
        threadPool.submit(threadPreProcessing);

        threadPool.shutdown();// Shut down the thread pool and wait for all tasks to complete
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Error in threading");
        }
    }
    private static class ExtractDocuemnt implements Runnable {
        static Set<Document> invertedIndexDocuments;
        MongoCursor<Document> cursor;

        public ExtractDocuemnt(MongoCursor<Document> cursor, Set<Document> invertedIndexDocuments) {
            this.invertedIndexDocuments = invertedIndexDocuments;
            this.cursor = cursor;
        }

        @Override
        public void run() {
            while (true) {
                Document Current;
                synchronized (cursor) {
                    if (cursor.hasNext())
                        Current = cursor.next();
                    else
                        break;
                }
                List<Document> wordUrls = Current.getList("WordUrls", Document.class);
                String KeyWord = Current.getString("key");
                wordDocuments.put(KeyWord, Current);
                for (Document one : wordUrls) {
                    String url = one.getString("url");
                    urlWords.computeIfAbsent(url, k -> new ConcurrentHashMap<>()).put(KeyWord,0);
                    if (!urlTitles.containsKey(url))
                        urlTitles.put(url, one.getString("title"));
                    uniqueUrls.add(url);
                    tagRanker Tags = new tagRanker( one.getList("tags", Document.class));
                    urlDes.computeIfAbsent(url, k -> new LinkedHashSet<>()).add(Tags);
                }
                for (Document one : wordUrls) {
                    String url = one.getString("url");
                    tagRanker Tags = new tagRanker();
                    urlTags.put(url, Tags.Description(urlDes.get(url), urlWords.get(url).keySet(),false,0,""));
                }

            }
        }
    }
}