package main.java.Tools;

import java.io.*;
import java.util.Scanner;

/**
 * Created by shubhangkulkarni on 7/12/17.
 */
public class CSVgenerator {

    public static boolean isspace(char c){
        return (c == ' ' || c == '\t');
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter input file");
        String file = scan.nextLine();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
            StringBuilder sb = new StringBuilder();
            String line;
            System.out.println("Reading File");
            while((line = br.readLine())!=null) {
                sb.append(line + "\n");
            }

            System.out.println("Done. Generating csv");
            br.close();
            for (int i = 0; i < sb.length(); i++) {
                if (isspace(sb.charAt(i)) ) {
                    if (i != 0 && sb.charAt(i-1)!=',')
                        sb.replace(i, i + 1, ",");
                    else {
                        sb.deleteCharAt(i);
                        i--;
                    }
                }
            }
            System.out.println("Done. Writing Csv");
            String outfile = file.replace(".txt", "") + ".csv.txt";
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outfile))));
            bw.write(sb.toString());
            bw.close();
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
