package main.java;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by shubhangkulkarni on 6/27/17.
 */
public class DisjointSet {

    private int[] id;
    private int[] size;

    public DisjointSet(int N){
        id = new int[N];
        size = new int[N];
        for(int i = 0; i < id.length; i++) {
            id[i] = i;
            size[i] = 1;
        }
    }

    private int find(int i){
        int temp = i;

        //get root
        while(i != id[i])
            i = id[i];

        //path compression - extra pass
        while(temp != i){
            int j = temp;
            temp = id[temp];
            id[j] = i;
        }
        return i;
    }


    public boolean isconnected(int i, int j){
        return find(i) == find(j);
    }

    private int size(int i){
        return size[i];
    }

    public void union(int i, int j){
        int root_i = find(i);
        int root_j = find(j);

        // return if in the same group
        if (root_i == root_j)
            return;

        // link the smaller tree to the larger tree
        if (size(root_i) < size(root_j)) {
            id[root_i] = root_j;
            size[root_j] += size(root_i);
        } else {
            id[root_j] = root_i;
            size[root_i] += size(j);
        }
    }

    public HashMap<Integer, TreeSet<Clusternode>> getClusters() {

        // compress all paths
        for (int i = 0; i < id.length; i++)
            find(i);

        HashMap<Integer, TreeSet<Clusternode>> hierarchies = new HashMap<>();

        for (int i = 0; i < id.length; i++) {
            if (!hierarchies.containsKey(id[i]))
                hierarchies.put(id[i], new TreeSet<>());

            hierarchies.get(id[i]).add(Vizualization.ifrcluster.get(i));
        }

        return hierarchies;
    }
}
