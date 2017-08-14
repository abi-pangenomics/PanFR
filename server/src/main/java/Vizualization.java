package main.java;
import javax.management.Query;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by shubhangkulkarni on 6/27/17.
 *
 * This class would be the Model of the project, in an MVC architecture.
 */

public class Vizualization {

    // temporary data structures
    public static HashMap<Integer, TreeSet<Integer>> leafiFRs;
    public static DisjointSet ds;

    // permanent data structures
    public static HashMap<Integer, TreeSet<Clusternode>> hierarchies; // could very well be an Arraylist. Has all the hierarchies
    public static HashMap<Integer, Clusternode> nodecluster; // mapping form a node's id to corresponding clusternode
    public static HashMap<Integer, Clusternode> ifrcluster; // mapping from an ifr's id to corresponding clusternode
    public static ArrayList<Clusternode> rootIfrs; // All the root ifr clusternodes
    public static VizGraph pathgraph;   // reference graph. Same for every user, changes according to dataset
    public static VizGraph displaygraph;    // different for every user. saves the user's state
    public static HashMap<Integer, Integer[]> pathid_pathnodes; // mapping of path id to path nodes
    public static String[] pathnames; // Mapping of path id too pathname.index of a pathname is its path id

    public static final boolean verbose = true; // command line view option
    public static final boolean debug = true;   // command line debug option
    public static final boolean dbnodesviz = false; // option to add the de bruijn graph nodes to visualization. default should be false
    public static int numFrs = 0;   // counts number of FRs

    public static String getHierarchiesText(){
        StringBuilder hierarchytext = new StringBuilder("");
        for (Clusternode root : rootIfrs){
            if (root.children == null)
                continue;
            if (!root.children.isEmpty()) {
                hierarchytext.append("" + root.node + " : ");
                for (Clusternode child : root.children){
                    hierarchytext.append(child.node + ", ");
                }
                hierarchytext.substring(0, hierarchytext.length() - 2);
                hierarchytext.append("|");
                for (Clusternode child : root.children){
                    getHierarchy(child, hierarchytext);
                }
            }
        }
        return hierarchytext.toString();
    }

    private static void getHierarchy(Clusternode root, StringBuilder hierarchytext){
        hierarchytext.append(root.node + " : ");

        // base case - leaf node
        if (root.children == null || root.children.isEmpty()) {
            hierarchytext.append("-1|-----|");
            return;
        }

        for (Clusternode child : root.children) {
            hierarchytext.append("" + child.node + ", ");
        }
        hierarchytext.append("\n");
        for (Clusternode child : root.children){
            getHierarchy(child, hierarchytext);
        }
    }

    public static String getNodePaths(){
        if (debug)
            System.out.println("[debug] Generating Node Paths");
        return displaygraph.getNodePaths();
    }

    public static String getPathNames(){
        if (debug)
            System.out.println("[debug] Generating Path Names");
        StringBuilder index_to_names = new StringBuilder("id,name\n");
        for (int i = 0; i < pathnames.length; i++) {
            index_to_names.append(i + "," + pathnames[i] + "\n");
        }
        return index_to_names.toString();
    }

    public static String getPathNodes(int path_id){

        if (pathid_pathnodes.get(path_id) == null){
            System.out.println("Error : Invalid Path Id - " + path_id);
            return null;
        }

        if (debug) {
            System.out.println("[Debug] Query id : " + path_id);
            System.out.println("[Debug] Query path name : " + pathnames[path_id]);
        }

        StringBuilder frspath = new StringBuilder("");
        for (Integer i : pathid_pathnodes.get(path_id)) {
            frspath.append(ifrcluster.get(i).group.node + " ");
        }


        return frspath.toString().substring(0, frspath.length()-1);
    }

    public static void constructHierarchies() {
        rootIfrs = new ArrayList<>();
        for (Integer group : hierarchies.keySet()){
            TreeSet<Clusternode> ifrs = hierarchies.get(group);
            Clusternode root = ifrs.pollLast();
            root.hierarchy_number = root.node;
            root.group = root;
            Clusternode c;
            while((c = ifrs.pollLast()) != null){
                // all the nodes in the same hierarchy have the same hierarchy number - possible coloring ??
                c.hierarchy_number = root.node;

                c.group = root;
                attach(root, c);
            }

            // for attaching leaves if we want - will clutter the resulting visualization
            if (dbnodesviz) {
                for (Integer node : root.members){
                    Clusternode leaf = nodecluster.get(node);
                    attach(root, leaf);
                    leaf.group = root;
                }
            }
            rootIfrs.add(root);
        }
        //hierarchies.clear();
        //hierarchies = null;
    }

    public static void attach(Clusternode root, Clusternode clusternode){
        if (root.children == null)
            root.children = new ArrayList<>();

        for (Clusternode c : root.children){
            if (isOverlap(c, clusternode)){
                attach(c, clusternode);
                return;
            }
        }

        clusternode.parent = root;
        //if (!clusternode.isleaf)
        root.children.add(clusternode);
    }

   public static boolean isOverlap(Clusternode c1, Clusternode c2){
       if(c1 == null || c2 == null)
           return false;

       Clusternode smaller;
       Clusternode larger;

       if (c1.numMembers < c2.numMembers){
           smaller = c1;
           larger = c2;
       } else {
           smaller = c2;
           larger = c1;
       }
       assert (larger.equals(c1)); // debug

       if (!larger.members.containsAll(smaller.members)){
           for (int i = 0; i < smaller.members.size(); i++){
               if (!larger.members.contains(smaller.members.get(i)))
                   System.out.println("Well damn :| ..." +
                           "\nNODE --> " + smaller.members.get(i).intValue() + " <--");
           }
       }
       return larger.members.containsAll(smaller.members);
   }

    public static void initDS() {
        ds = new DisjointSet(numFrs);

        for (Integer i : leafiFRs.keySet()){
            int temp_root = leafiFRs.get(i).first();
            for (Integer j : leafiFRs.get(i))
                ds.union(temp_root,j);
        }
        leafiFRs.clear();
        leafiFRs = null;
    }


    /**
     * @TODO - Get cleared
     * Potential errors - if an ifr is a leaf node [although I believe that's unlikely]
     * */
    public static void readFrs(String frsfile) {
        try {
            ifrcluster = new HashMap<>();
            nodecluster = new HashMap<>();
            leafiFRs = new HashMap<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(frsfile))));
            String line;
            while ((line = br.readLine()) != null) {

                String[] frLine = line.split(",");
                int frNum = Integer.parseInt(frLine[0].split("-")[1]);

                if(ifrcluster.containsKey(frNum)){
                    System.out.println("Error : Duplicate FR #" + frNum + " in frs file");
                    System.exit(1);
                }

                ifrcluster.put(frNum, new Clusternode(frNum, null, null, true, false, 1));

                numFrs++;

                for (int i = 0; i < frLine.length - 1; i++) {
                    int node = Integer.parseInt(frLine[i + 1]);

                    ifrcluster.get(frNum).addMember(node);

                    if (!nodecluster.containsKey(node))
                        nodecluster.put(node, new Clusternode(node, null, null, false, true, 1));

                    nodecluster.get(node).addMember(node); // used during adding leaves to hierarchy

                    if (!leafiFRs.containsKey(node))
                        leafiFRs.put(node, new TreeSet<Integer>());

                    leafiFRs.get(node).add(frNum);
                }
                //@TODO - optimize this later on. unnecessary accesses to the map.
                ifrcluster.get(frNum).numMembers = ifrcluster.get(frNum).members.size();

            }
            br.close();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
            System.exit(-1);
        }
    }


    public static void printhierarchies(){
        for (Clusternode root : rootIfrs){
            System.out.println("------");
            printhierarchies(root);
        }
    }

    private static void printhierarchies(Clusternode root){
        System.out.print(root.node + " : ");
        if (root.children == null){
            System.out.print("-1");
            System.out.println();
            return;
        }
        for (Clusternode c : root.children){
            System.out.print(c.node + ", ");
        }
        System.out.println();
        for (Clusternode c : root.children){
            printhierarchies(c);
        }
    }


    public static void buildVizualization(String frpathsfile) {
        pathgraph = new VizGraph();
        displaygraph = new VizGraph();

        if (verbose)
            System.out.println("reading frPaths file: " + frpathsfile);
        readFRpaths(frpathsfile);
        System.out.println("Done...");
    }

    private static void addEdgePath(VizGraph graph, Clusternode u, Clusternode v, int path_index){
        graph.addEdgePaths(u, v , path_index);
    }

    public static void readFRpaths(String frpathsfile) {
        // Initialize data structure to hold path nodes
        pathid_pathnodes = new HashMap<>();
        ArrayList<String> temppathnames = new ArrayList<>();
        ArrayList<Integer> pathnodes = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(frpathsfile))));
            String line;

            int path_index = 0;
            while ((line = br.readLine()) != null) {
                String[] frLine = line.split(",");
                String[] path = frLine[1].split(" ");
                for (int i = 0; i < path.length; i++) {
                    if (path[i].startsWith("[fr-")) {
                        String x = path[i].replace("[fr-", "");
                        pathnodes.add(Integer.parseInt(x.split(":")[0]));
                    }
                }

                for (int i = 0; i < pathnodes.size(); i++) {
                    Clusternode temp = ifrcluster.get(pathnodes.get(i));
                    temp.addPath(path_index, i);
                }

                /*for (Integer i : pathnodes){
                    Clusternode temp = ifrcluster.get(i);
                    temp.addPath(path_index, i);
                }*/

                // Add the nodes in the path to keep track of them
                Integer[] temp = new Integer[pathnodes.size()];
                int j = 0;
                for (Integer node : pathnodes)
                        temp[j++] = node;
                pathid_pathnodes.put(path_index, temp);
                temppathnames.add(frLine[0]);

                // Version 2.0
                for (int i = 1; i < pathnodes.size(); i++) {
                    Clusternode source = ifrcluster.get(pathnodes.get(i-1));
                    Clusternode target = ifrcluster.get(pathnodes.get(i));

                    if (source == target)
                        continue;

                    addEdgePath(pathgraph, source, target, path_index);
                    pathgraph.addEdge(source, target);
                    //addEdgePath(displaygraph, temp_u.group, temp_v.group, path_index); // taken care of in Vizgraph.getDisplayGraph
                    displaygraph.addEdge(source.group, target.group);
                }
                
                /*
                // Version 3.0
                int u = 0, v = 0;
                Clusternode temp_u = ifrcluster.get(pathnodes.get(u));
                Clusternode temp_v = temp_u;
                
                while (v < pathnodes.size()){
                    while(temp_v.group == temp_u.group){
                        if (v >= pathnodes.size()-1)
                            break;
                        v++;
                        temp_v = ifrcluster.get(pathnodes.get(v));
                        continue;
                    }
                    while (u != v) {
                        addEdgePath(pathgraph, temp_u, temp_v, path_index);
                        pathgraph.addEdge(temp_u, temp_v);
                        //addEdgePath(displaygraph, temp_u.group, temp_v.group, path_index); // taken care of in Vizgraph.getDisplayGraph
                        displaygraph.addEdge(temp_u.group, temp_v.group);
                        u++;
                        temp_u = ifrcluster.get(pathnodes.get(u));
                    }
                    v++;
                    if (v > pathnodes.size()-1)
                        break;
                    temp_v = ifrcluster.get(pathnodes.get(v));

                }

                while (u != v) {
                    addEdgePath(pathgraph, temp_u, temp_v, path_index);
                    pathgraph.addEdge(temp_u, temp_v);
                    //addEdgePath(displaygraph, temp_u.group, temp_v.group, path_index); // taken care of in Vizgraph.getDisplayGraph
                    displaygraph.addEdge(temp_u.group, temp_v.group);
                    u++;
                    if (u >= pathnodes.size())
                        break;
                    temp_u = ifrcluster.get(pathnodes.get(u));
                }*/
                pathnodes.clear();
                path_index++;
            }

            // save all the path names in the global static variable
            pathnames = new String[temppathnames.size()];
            int i = 0;
            for (String s : temppathnames)
                pathnames[i++] = s;

            br.close();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static boolean isLeaf(Clusternode clusternode){
        return (clusternode.children == null || clusternode.children.size() == 0);
    }

    public static void clicked(Integer index){
        if (!ifrcluster.containsKey(index)) {
            System.out.println("Error : node #" + index + "cannot be clicked");
            return;
        }
        System.out.println("[ debug : Node #"+ index + " clicked ]");

        Clusternode root = ifrcluster.get(index);
        assert (root != null);

        //set root group to null if you want to remove it from the new graph
        root.group = root;

        if (isLeaf(root)){
            root.group = root;
            System.out.println("Leaf cannot be clicked...");
            return;
        }

        for (Clusternode c : root.children)
            setHierarchyGroup(c, c);

        updateDisplayGraph(root);
        //updateDisplayGraph();
    }

    public static void setHierarchyGroup(Clusternode root, Clusternode group){
        root.group = group;
        if (isLeaf(root))
            return;
        for (Clusternode c : root.children)
            setHierarchyGroup(c, group);
    }

    public static void updateDisplayGraph(Clusternode c){
        // remove vertex
        displaygraph.removeVertex(c);
        // get original graph
        HashMap<Clusternode, HashSet<Clusternode>> adjList = pathgraph.getAdjList();
        ArrayList<UpdateEdge> updates = new ArrayList<>();

        for (Clusternode u : adjList.keySet()){
            for (Clusternode v : adjList.get(u)){
                if (!displaygraph.hasEdge(u.group, v.group)) {
                    updates.add(new UpdateEdge(u.group, v.group));
                    displaygraph.addEdge(u.group, v.group);
                }
            }
        }
        displaygraph.updateEdgePaths();
        displaygraph.setUpdates(updates);
    }

    public static void updateDisplayGraph() {
        HashMap<Clusternode, HashSet<Clusternode>> adjList = pathgraph.getAdjList();
        displaygraph.eraseAll();
        for (Clusternode u : adjList.keySet()){
            for (Clusternode v : adjList.get(u)){
                displaygraph.addEdge(u.group, v.group);
            }
        }
    }

    public static void start(String frsfile, String frpathsfile){
        if (verbose)
            System.out.println("Reading frs file : " + frsfile);
        readFrs(frsfile);
        if (verbose)
            System.out.println("Done...");

        initDS();
        hierarchies = ds.getClusters();
        ds = null;

        constructHierarchies();

        if (debug)
            printhierarchies();
        buildVizualization(frpathsfile);
        System.out.println("--- Display Graph ---");
        displaygraph.display();
    }

    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Error Usage : <frs file> <frpaths file>");
            System.exit(1);
        }

        String frsfile = args[0];
        if (verbose)
            System.out.println("Reading frs file : " + frsfile);
        readFrs(frsfile);
        if (verbose)
            System.out.println("Done...");

        initDS();
        hierarchies = ds.getClusters();
        ds = null;

        constructHierarchies();

        if (debug)
            printhierarchies();

        //System.exit(1);
        String frpathsfile = args[1];
        buildVizualization(frpathsfile);
       // System.out.println("--- Path Graph ---");
       // pathgraph.display();
        System.out.println("--- Display Graph ---");
        displaygraph.display();
        Scanner scan = new Scanner(System.in);

        while(true) {
            System.out.println("Enter Option:\n" +
                    "[1] Expand node\n" +
                    "[2] Display Graph\n" +
                    "[3] Scrutinize node\n" +
                    "[4] Exit");
            int option = scan.nextInt();
            int n;
            switch (option) {
                case 1:
                    System.out.println("click on a node to expand... ");
                    n = scan.nextInt();
                    clicked(n);
                    if (debug)
                        displaygraph.display();
                    break;
                case 2:
                    displaygraph.display();
                    break;
                case 3:
                    System.out.println("Click on node to scrutinize");
                    n = scan.nextInt();
                    displaygraph.display(n);
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Error : Invalid Option Number");
            }
        }
    }
}
