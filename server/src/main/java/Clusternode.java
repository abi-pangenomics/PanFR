package main.java;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by shubhangkulkarni on 6/28/17.
 */
public class Clusternode implements Comparable{
    public Clusternode parent;

    public int node;
    public int numMembers;
    public Clusternode group;
    public ArrayList<Integer> members;
    public boolean isleaf;
    public boolean isifr;
    public ArrayList<Clusternode> children;
    public HashSet<Integer> paths;
    HashMap<Integer, ArrayList<Integer>> pathspositions; // all the paths the node occurs in - all the positions in the path
    public int hierarchy_number;

    public Clusternode(int node, Clusternode parent, Clusternode group, boolean isifr, boolean isleaf, int numMembers) {
        this.parent = parent;
        this.node = node;
        this.group = group;
        this.isifr = isifr;
        this.isleaf = isleaf;
        this.numMembers = numMembers;
        this.paths = new HashSet<>();
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.members.size(), ((Clusternode) o).members.size());
    }

    public void addMember(int member){
        if (members == null)
            members = new ArrayList<>();

        members.add(member);
    }

    public void addPath(Integer path, Integer i) {
        if (pathspositions == null)
            pathspositions = new HashMap<>();

        if (!pathspositions.containsKey(path))
            pathspositions.put(path, new ArrayList<>());

        pathspositions.get(path).add(i);
    }

    /**
     * @deprecated
     * */
    public void _addPath(Integer path){
        if (paths == null)
            paths = new HashSet<>();

        paths.add(path);
    }

    public void addChild(Clusternode child){
        if (children == null)
            children = new ArrayList<>();

        children.add(child);
    }
}
