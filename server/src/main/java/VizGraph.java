package main.java;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by shubhangkulkarni on 7/5/17.
 */
public class VizGraph {
    private HashMap<Clusternode, HashSet<Clusternode>> adjList;
    private boolean allow_self_edges = false;
    private HashMap<Clusternode, HashSet<Clusternode>> revadjList;
    private HashMap<Clusternode, HashMap<Clusternode, Integer>> edgeScores;
    private UpdateEdge[] updates;
    private HashMap<Integer, HashMap<Integer, TreeSet<Integer>>> edgepaths;


    public VizGraph(){
        adjList = new HashMap<>();
        revadjList = new HashMap<>();
        edgeScores = new HashMap<>();
    }

    public String getNodePaths(){
        StringBuilder nodepaths = new StringBuilder("node,path,positions\n");
        for (Clusternode cnode : Vizualization.pathgraph.adjList.keySet()){
            String temp = "";
            for (Integer path : cnode.pathspositions.keySet()) {
                temp += cnode.node + "," + path + ",";
                assert (!cnode.pathspositions.get(path).isEmpty()); // every path has to pass through the node in at least one position=
                for (Integer position : cnode.pathspositions.get(path)){
                    temp += position + " ";
                }
                temp = temp.substring(0, temp.length()-1) + "\n";
            }
            nodepaths.append(temp);
        }
        return nodepaths.toString();
    }

    /**
     * @deprecated
     * */
    public String _getNodePaths(){
        StringBuilder nodepaths = new StringBuilder("node,paths\n");
        for (Clusternode cnode : adjList.keySet()){
            String temp = "";
            temp += cnode.node + ",";
            for (Integer path : cnode.paths){
                temp += path + " ";
            }
            temp = temp.substring(0, temp.length()-1) + "\n";
            nodepaths.append(temp);
        }

        return nodepaths.toString();
    }

    public void addEdgePaths(Clusternode u, Clusternode v, int path_index){
        if (edgepaths == null)
            edgepaths = new HashMap<>();
        if (!edgepaths.containsKey(u.node))
            edgepaths.put(u.node, new HashMap<>());
        if (!edgepaths.get(u.node).containsKey(v.node)) {
            edgepaths.get(u.node).put(v.node, new TreeSet<>());
        }

        edgepaths.get(u.node).get(v.node).add(path_index);
    }


    public void clearEdgeScores(){
        //@TODO probably won't clear everything out. Check back later
        edgeScores.clear();
        edgeScores = null;
    }

    public void incrementEdgeScore(Clusternode u, Clusternode v){
        if (edgeScores == null)
            edgeScores = new HashMap<>();

        if (!edgeScores.containsKey(u))
            edgeScores.put(u, new HashMap<>());

        if (!edgeScores.get(u).containsKey(v))
            edgeScores.get(u).put(v, 0);

        edgeScores.get(u).put(v, edgeScores.get(u).get(v) + 1);
    }

    public void addEdge(Clusternode u, Clusternode v){
        if (u == null || v == null) return;
        addVertex(u);
        addVertex(v);

        incrementEdgeScore(u, v);

        if (u.equals(v) && !allow_self_edges)
            return;
        adjList.get(u).add(v);
        revadjList.get(v).add(u);
    }

    public void addVertex(Clusternode u){
        if (!adjList.containsKey(u))
            adjList.put(u, new HashSet<>());
        if (!revadjList.containsKey(u))
            revadjList.put(u, new HashSet<>());
    }

    public boolean hasEdge(Clusternode u, Clusternode v){
        if (!adjList.containsKey(u)) return false;
        return adjList.get(u).contains(v);
    }

    public boolean isleaf(Clusternode u) {
        if (!adjList.containsKey(u)) return false;
        return adjList.get(u).size() == 0;
    }

    public void removeEdge(Clusternode u, Clusternode v){
        if (!adjList.containsKey(u)){
            System.out.println("Error : no edge");
            return;
        }

        adjList.get(u).remove(v);
    }

    public void removeVertex(Clusternode u) {
        if (!adjList.containsKey(u)) {
            System.out.println("Error : no vertex");
            return;
        }

        adjList.get(u).clear();
        adjList.remove(u);

        for (Clusternode v : adjList.keySet()){
            if (adjList.get(v).contains(u))
                adjList.get(v).remove(u);
        }

    }

    public void _removeVertex(Clusternode u){
        if (!adjList.containsKey(u)) {
            System.out.println("Error : no vertex");
            return;
        }
        for (Clusternode c : adjList.get(u)){
            revadjList.get(c).remove(u);
        }
        adjList.remove(u);
        for (Clusternode c : revadjList.get(u)){
                adjList.get(u).remove(u);
        }
        revadjList.remove(u);
    }

    public void eraseAll() {
        adjList.clear();

        // reset the scores [should work]
        edgeScores.clear();
        edgeScores = null;
    }

    public HashMap<Clusternode, HashSet<Clusternode>> getAdjList(){
        return adjList;
    }

    public int getEdgeScore(Clusternode u, Clusternode v) {
        Integer score = edgeScores.get(u).get(v);
        if (score == null) {
            System.out.println("[debug - Vizgraph] Null score between edge " + u.node + " -> " + v.node);
            return 0;
        }
        return score.intValue();
    }

    /**
     * Called only on initialization
     * Was previously called on click as well, but now we maintain a list of nodes that point to every node as well
     * */
    public String getDisplayGraph(){
        //@TODO - update all the edge path mappings. Very bad Software Engineering. Handle this elsewhere.
        updateEdgePaths();

        // ArrayList<String> temp = new ArrayList<>();
        final String header = "source,target,value,paths\n";
        String ret = "";
        ret += header;
        for (Clusternode source : adjList.keySet()){
            for (Clusternode target : adjList.get(source)){
                ret += source.node + "," + target.node + "," + getEdgeScore(source, target) + ",";
                for (Integer i : edgepaths.get(source.node).get(target.node)){
                    ret += i.intValue() + " ";
                }
                ret = ret.substring(0, ret.length() - 1) + "\n"; // get rid of the last space
            }
        }
        return ret;
    }

    // O(E)
    public void updateEdgePaths(){
        // @TODO - is deep clearing needed? - check via debugger
        if (edgepaths != null)
            edgepaths.clear();
        edgepaths = null; // gc-ed

        VizGraph pathgraph = Vizualization.pathgraph;
        for (Integer u : pathgraph.edgepaths.keySet()){
            for (Integer v : pathgraph.edgepaths.get(u).keySet()){
                for (Integer path : pathgraph.edgepaths.get(u).get(v)){
                    addEdgePaths(Vizualization.ifrcluster.get(u).group, Vizualization.ifrcluster.get(v).group, path);
                }
            }
        }
    }


    @Deprecated
    public String[] _getDisplayGraph(){
        String[] temp = new String[adjList.keySet().size()];
        int i = 0;
        for (Clusternode c : adjList.keySet()){
            temp[i] = "";
            temp[i] += "{\"node\":"+c.node+",\"links\":[";
            for (Clusternode node : adjList.get(c)){
                temp[i] += node.node + ",";
            }
            if (temp[i].endsWith(","))
                temp[i] = temp[i].substring(0,temp[i].length()-1);
            temp[i] += "]}";
            i++;
        }
        return temp;
    }

    /**
     * Called on scrutinize
     * */
    public String[] getDisplayGraph(int n){
        ArrayList<String> ret = new ArrayList<>();
        for (Clusternode c : adjList.keySet()){
            if (adjList.get(c).contains(Vizualization.ifrcluster.get(n))) {
                String temp = "";
                temp += "{\"node\":" + c.node + ",\"links\":[";
                for (Clusternode node : adjList.get(c)) {
                    temp += node.node + ",";
                }
                if (temp.endsWith(","))
                    temp = temp.substring(0,temp.length()-1);
                temp += "]}";
                ret.add(temp);
            }
        }
        String[] temp = new String[ret.size()];
        int i = 0;
        for (String s : ret){
            temp[i++] = s;
        }

        return temp;
    }

    /**
     * Called for all the updates on click - dynamic addition/deletion of nodes
     * instead of redrawing the entire graph.
     * */
    public void setUpdates(ArrayList<UpdateEdge> updates){
        this.updates = new UpdateEdge[updates.size()];
        int i = 0;
        for (UpdateEdge u : updates) {
            this.updates[i] = u;
            i++;
        }
    }

    public String getUpdates(){
        StringBuilder ret = new StringBuilder("source,target,paths\n");
        for (UpdateEdge updateEdge : this.updates){
            ret.append(updateEdge.u.node + "," + updateEdge.v.node + ",");
            TreeSet<Integer> paths = this.edgepaths.get(updateEdge.u.node).get(updateEdge.v.node);
            for (Integer path : paths){
                ret.append(path + " ");
            }
            ret.delete(ret.length()-1, ret.length());
            ret.append("\n");
        }
        return ret.toString();
    }

    /**
     * Called for all the updates on click - dynamic addition/deletion of nodes
     * instead of redrawing the entire graph.
     * */
    public String[] _getUpdates(){
        String[] temp = new String[updates.length];
        int i = 0;
        for (UpdateEdge updateEdge : this.updates){
            temp[i++] = "{\"source\":" + updateEdge.u.node + ", \"target\":" + updateEdge.v.node + "}";
        }
        updates = null; //gc-ed
        return temp;
    }

    /**
     * Server Debug
     * */
    public void display(){
        int i = 0;
        for (Clusternode c : adjList.keySet()){
                i++;
                System.out.println("------");
                System.out.print(i + ") " + c.node + " : ");
                for (Clusternode connected : adjList.get(c))
                    System.out.print(connected.node + ", ");
                System.out.println();
        }
    }


    /**
     * Server Debug
     * */
    public void display(int n){
        int i = 0;
        for (Clusternode c : adjList.keySet()){
            if (adjList.get(c).contains(Vizualization.ifrcluster.get(n))) {
                i++;
                System.out.println("------");
                System.out.print(i + ") " + c.node + " : ");
                for (Clusternode connected : adjList.get(c))
                    System.out.print(connected.node + ", ");
                System.out.println();
            }
        }
        System.out.println("---> " + i + " <---");
    }
}
