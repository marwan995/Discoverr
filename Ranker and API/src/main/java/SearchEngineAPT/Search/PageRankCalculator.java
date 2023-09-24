package SearchEngineAPT.Search;

import java.util.ArrayList;
import java.util.Set;

public class PageRankCalculator implements Runnable {

    private final double[] pageRank;
    private final ArrayList<Set<Integer>> GIN;
    private final ArrayList<Set<Integer>> G;
    private final double d;
    private final int startIndex;
    private final int endIndex;
    static public double[] currentPageRanked;

    public PageRankCalculator(double[] pageRank, ArrayList<Set<Integer>> GIN, ArrayList<Set<Integer>> G, double d, int startIndex, int endIndex) {
        this.pageRank = pageRank;
        this.GIN = GIN;
        this.G = G;
        this.d = d;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.currentPageRanked = new double[pageRank.length];
    }


    @Override
    public void run() {
        for (int i = startIndex; i < endIndex; i++) {
            Double sum = 0.0;
            for (int j : GIN.get(i)) {
                int sz = G.get(j).size();
                if (sz == 0)
                    sz = 1;
                sum += (pageRank[j] / sz);
            }
            currentPageRanked[i] = (1 - d + (d * sum));
        }
    }
}
