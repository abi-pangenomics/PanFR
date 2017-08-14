package main.java.Tools;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by shubhangkulkarni on 7/7/17.
 */
public class Analysis {
    public static void main(String[] args) {
        Scanner scan1 = new Scanner(System.in);
        Scanner scan2 = new Scanner(System.in);
        System.out.println("Enter option:");
        int n = scan1.nextInt();
        switch(n) {
            case 1:
                String frpathsfile = scan2.nextLine();
                isfrpathsnested(frpathsfile);
                break;
            case 2:
                String seq = "";
                String line;
                while(!(line = scan2.next()).equals("#"))
                    seq+=line;
                System.out.println("length = " + seq.length());
                break;
            case 3:
                String hasN = "";
                String in;
                while(!(in = scan2.next()).equals("#"))
                    hasN+=in;
                reportHasN(hasN);
                break;
            default :
                System.out.println("Error");
        }
    }

    private static void reportHasN(String hasN) {
        if (hasN.contains("n") || hasN.contains("N"))
            System.out.println("has n or N");
        else
            System.out.println("Nope");
    }

    private static void isfrpathsnested(String frpathsfile) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(frpathsfile))));
            String line;
            while ((line = br.readLine()) != null) {
                String[] frLine = line.split(",");
                String[] path = frLine[1].split(" ");
                for (int i = 0; i < path.length-1; i++) {
                    if (path[i].startsWith("[fr-") && path[i+1].startsWith("[fr-")) {
                        System.out.println(i/2 + "/" +path.length/2 +" : " +line);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }



}
