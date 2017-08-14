package main.java;

import java.util.ArrayList;

/**
 * Created by shubhangkulkarni on 7/17/17.
 */
public class UpdateEdge {
    Clusternode u;
    Clusternode v;
    ArrayList<Integer> paths;
    int score;

    public UpdateEdge(Clusternode u, Clusternode v) {
        this.u = u;
        this.v = v;
    }

    public void addPathToEdge(int path){
        if (paths == null)
            paths = new ArrayList<>();
        paths.add(path);
    }

    public void setScore(int score){
        this.score = score;
    }
}
