package SearchEngineAPT.Search;

import org.bson.Document;

import java.util.*;

public class AdjacencyList {
    private  static ArrayList<Set<Integer>> G;
    private HashMap<String, Integer> indexMap;
    private HashMap<Integer,String>InverseIndexMap;
    private static ArrayList<Set<Integer>> GIN;
    private static double [] PageRank;

    private int size;
    double threshold = 0.0001;

    private static double d=0.85;
    public AdjacencyList(ArrayList<Document> urls) {
        this.indexMap=new HashMap<>();
        this.InverseIndexMap=new HashMap<>();
//        facebook--> youtube  facebook/profile  instegram
//        stackOverflow  --> news geeksforgeeks youtube


        this.indexMap = new HashMap<String, Integer>();
        int index = 0;
        Set<String> setOfUrls = new LinkedHashSet<>();
        for (Document urlDoc : urls) {
            String url = urlDoc.getString("url");
            setOfUrls.add(url);
            setOfUrls.addAll(urlDoc.getList("links", String.class));
        }
        for (String url : setOfUrls) {
            if(indexMap.containsKey(url))continue;
            indexMap.put(url, index);
            InverseIndexMap.put(index,url);
            index++;
        }

        this.G = new ArrayList<Set<Integer>>();
        this.GIN=new ArrayList<Set<Integer>>();
        for (int i = 0; i < index; i++) {
            this.G.add(new HashSet<Integer>());
            this.GIN.add(new HashSet<Integer>());
        }


        for (Document urlDoc : urls) {
            String url = urlDoc.getString("url");
            int from = indexMap.get(url);
            for (String linkUrl : urlDoc.getList("links", String.class)) {
                int to = indexMap.get(linkUrl);
                G.get(from).add(to);
            }
        }
        for (Document urlDoc : urls) {
            String url = urlDoc.getString("url");
            int to = indexMap.get(url);
            for (String linkUrl : urlDoc.getList("links", String.class)) {
                int from = indexMap.get(linkUrl);
                GIN.get(from).add(to);
            }
        }
        PageRank=new double [index];

        for(int i=0;i<index;i++) {
            int size=G.get(i).size();
            if(size>0)
            PageRank[i]=1.0/size;//should be 1
            else
                PageRank[i] = 0.0;
        }

    }
    public Boolean CheckConvertion(double []cP){
        for(int i=0;i< cP.length;i++)
            if(cP[i]-PageRank[i]>threshold)
                return false;
        return true;
    }
//    public Map<String,Double> CalcultePageRank(){
//        int MaxItretions=20;
//        double []CurrentPageRank= new double[PageRank.length];
//        while (MaxItretions--!=0) {
//            for (int i = 0; i < GIN.size(); i++) {
//                Double Sum = 0.0;
//                for (int j : GIN.get(i)) {
//                    int sz = G.get(j).size();
//                    if(sz == 0) sz = 1;
//                    Sum += (PageRank[j] / sz);
//                }
//                CurrentPageRank[i]=( 1 - d + (d * Sum));
//            }
//            if(CheckConvertion(CurrentPageRank))
//                break;
//            for(int i=0;i< PageRank.length;i++)
//                PageRank[i]=CurrentPageRank[i];
//        }
//        Map<String,Double>answer=new HashMap<>();
//        for(int j=0;j<PageRank.length;j++)
//            answer.put(InverseIndexMap.get(j),PageRank[j]);
//
//        return answer;
//    }
public Map<String, Double> CalcultePageRank() {
    int maxIterations = 20;
    double []currentPageRank = new double[PageRank.length];
    while (maxIterations-- != 0) {
        List<Thread> threads = new ArrayList<>();
        final int numThreads = 12; // set the number of threads to use
        final int chunkSize = GIN.size() / numThreads; // divide the loop into equal chunks
        for (int i = 0; i < numThreads; i++) {
            final int startIndex = i * chunkSize;
            final int endIndex = (i + 1) * chunkSize;
            Thread thread = new Thread(new pagerank(startIndex, endIndex,currentPageRank));
            thread.start();
            threads.add(thread);
        }
        // wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (CheckConvertion(currentPageRank)) {
            break;
        }
        for (int i = 0; i < PageRank.length; i++) {
            PageRank[i] = currentPageRank[i];
        }
    }
    Map<String, Double> answer = new HashMap<>();
    for (int j = 0; j < PageRank.length; j++) {
        answer.put(InverseIndexMap.get(j), PageRank[j]);
    }
    return answer;
}

    private static class pagerank implements Runnable {
        int startIndex;
        int endIndex;
         static double []currentPageRank;

        public pagerank( int startIndex, int endIndex,double []c) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.currentPageRank=c;
        }
        @Override
        public void run() {
            for (int i = startIndex; i < endIndex; i++) {
                Double sum = 0.0;
                for (int j : GIN.get(i)) {
                    int sz = G.get(j).size();
                    if (sz == 0)
                        sz = 1;
                    sum += (PageRank[j] / sz);
                }
                currentPageRank[i] = (1 - d + (d * sum));
            }
        }
    }



}



