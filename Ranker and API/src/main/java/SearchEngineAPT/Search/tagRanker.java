package SearchEngineAPT.Search;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toMap;

public class tagRanker {
    List<Document> tags;
    public tagRanker() {}
    public tagRanker( List<Document> tags) {
        tags.sort((doc1, doc2) -> compare(doc1.getString("name"), doc2.getString("name")));
        Set<Document> mySet = new LinkedHashSet<>(tags);
        this.tags =  new ArrayList<>(mySet);
    }

    public static synchronized String decompress(byte[] compressedData) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
            InputStreamReader reader = new InputStreamReader(brotliInputStream, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            int c = reader.read();
            while (c != -1) {
                stringBuilder.append((char) c);
                c = reader.read();
            }
            brotliInputStream.close();
            return stringBuilder.toString();
        }

    String Description(Set<tagRanker> tagCollection, Set<String> keys,boolean isSpecial,int length,String q) {
        Brotli4jLoader.ensureAvailability(); // make sure Brotli4j is available
        String Result = GetBestMatchDescription(tagCollection,keys,isSpecial,length,q);
        if(Result!=""||isSpecial)
            return Result;
        return Result+SetNormalDescription(tagCollection,keys);
    }

    public int compare(String tag1, String tag2) {
        int priority1 = getPriority(tag1);
        int priority2 = getPriority(tag2);
        return Integer.compare(priority1, priority2);
    }

    private int getPriority(String tag) {
        List<String> tagHierarchy = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "center", "a", "ul", "ol", "li", "dd", "table", "tr", "th", "td", "div", "span", "strong", "br", "hr", "button");
        int tagIndex = tagHierarchy.indexOf(tag);
        if (tagIndex != -1) {
            return tagIndex;
        } else {
            return 0;
        }
    }
    public int getQeryTermsSize( Set<String> Keys){
        int s=0;
        for(String key:Keys)
            s+=key.length();
        return s;
    }
    public String getTagContent(Map.Entry<String, Integer> entry ){
        String[] parts = entry.getKey().split(" ");
        String tagContent = null;
        try
        {
            tagContent = decompress(Base64.getDecoder().decode(parts[1]));
        }
        catch (IOException e)
        {
            System.out.println("error");
        }
        return tagContent;
    }
    public Map<String ,Integer> setTagsMap(Set<tagRanker> tagCollection){
        Map<String ,Integer>TagMap=new ConcurrentHashMap<>();
        for (tagRanker tagsranker : tagCollection)
        {
            for (Document d : tagsranker.tags)
            {
                String id = d.getString("name") +" "+  Base64.getEncoder().encodeToString(d.get("content", Binary.class).getData());
                if (TagMap.containsKey(id))
                    TagMap.put(id, TagMap.get(id) + 1);
                else
                    TagMap.put(id, 1);
            }
        }
        Map<String, Integer> sortedTagMap = TagMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedTagMap;
    }
    public String GetBestMatchDescription(Set<tagRanker> tagCollection,Set<String> Keys,boolean isSpecial,int length,String q){
        String bestMatch="";
        Map<String ,Integer>TagMap=setTagsMap(tagCollection);
        int counter=Keys.size();
        for(Map.Entry<String, Integer> entry : TagMap.entrySet()) {
            if((((Keys.size()/2))>=entry.getValue())||(isSpecial&&entry.getValue()!=length))
                break;
            String tagContent=getTagContent(entry);
            if(isSpecial&&tagContent.toLowerCase().contains(q))
                bestMatch+="-"+tagContent+"-";
            else if (!isSpecial)
                bestMatch+="-"+tagContent+"-";
            counter-=entry.getValue();
            if(counter<=0)break;

        }
        TagMap.clear();
        return bestMatch;
    }
    public String SetNormalDescription(Set<tagRanker> tagCollection, Set<String> Keys){
        int i=0, QeryTermsSize=getQeryTermsSize(Keys);
        String normalResult="";
        do {
            int cnt=0;
            for (tagRanker tagR : tagCollection) {
                String tagContent = null;
                if(tagR.tags.size()-1<i)continue;//as It's for each so to make sure it doesn't go beyond the size of the tags set
                try {
                    Binary binData = tagR.tags.get(i).get("content", Binary.class);// why i? as they are sorted ,I get the best tag from each key in the query
                    byte[] compressedContent = binData.getData();
                    tagContent = decompress(compressedContent);
                } catch (IOException e) {
                    System.out.println("error");
                }
                if (normalResult.contains(tagContent)) continue;//check for any duplication in the string
                normalResult += (" " + tagContent);
                cnt++;
            }
            if(cnt==0)break;//if nothing has been added to result string break the loop
            i++;
        }while (normalResult.length()<=(QeryTermsSize+500));// if the best the sum of all best content not enuf loop again with the second best
        return normalResult;
    }
}
