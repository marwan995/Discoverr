import java.util.ArrayList;
import java.util.HashMap;
public class AdjacencyMatrix {
    private HashMap<Integer,ArrayList<Integer>> matrix;
    private HashMap<String, Integer> indexMap;
    private int size;

    public AdjacencyMatrix(ArrayList<String> urls) {
        this.size = urls.size();
        this.matrix = new ArrayList<ArrayList<Integer>>(size);
        this.indexMap = new HashMap<String, Integer>(size);

        for (int i = 0; i < size; i++) {
            ArrayList<Integer> row = new ArrayList<Integer>(size);
            for (int j = 0; j < size; j++) {
                row.add(0);
            }
            this.matrix.add(row);
            this.indexMap.put(urls.get(i), i);
        }
    }

    public void addEdge(String fromUrl, String toUrl, int weight) {
        int from = indexMap.get(fromUrl);
        int to = indexMap.get(toUrl);
        matrix.get(from).set(to, weight);
    }

    public void removeEdge(String fromUrl, String toUrl) {
        int from = indexMap.get(fromUrl);
        int to = indexMap.get(toUrl);
        matrix.get(from).set(to, 0);
    }

    public boolean hasEdge(String fromUrl, String toUrl) {
        int from = indexMap.get(fromUrl);
        int to = indexMap.get(toUrl);
        return matrix.get(from).get(to) > 0;
    }

    public int getWeight(String fromUrl, String toUrl) {
        int from = indexMap.get(fromUrl);
        int to = indexMap.get(toUrl);
        return matrix.get(from).get(to);
    }

    public int getSize() {
        return size;
    }
}

public class PageRanking {

}
