package main.java;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

/**
 * Created by shubhangkulkarni on 7/7/17.
 */
public class Server {
    public static String frsfile;
    public static String frpathsfile;


    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Error Usage : <frs file> <frpaths file>");
            System.exit(1);
        }

        frsfile = args[0];
        frpathsfile = args[1];

        try {
            ResourceConfig config = new ResourceConfig(Controller.class);
            JettyHttpContainerFactory.createServer(new URI("http://localhost:9997/"), config);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("I'm crashing now :(");
        }
    }
}
