package main.java;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by shubhangkulkarni on 7/7/17.
 */
@Path("server")
public class Controller {
    private static boolean multi_init = false;
    private static boolean server_debug = true;

    // return initial display graph
    @GET
    @Produces(value =  MediaType.APPLICATION_JSON)
    @Path("/init-graph")
    public Response initGraph(){

        System.out.println("[server-debug] Request for Graph Received");

        // this will help in testing and development of frontend. Remove later on
        if (!multi_init) {
            Vizualization.start(Server.frsfile, Server.frpathsfile);
            multi_init = true;
        }

        System.out.println("[server-debug] Sending Response : Displaygraph (response not displayed below)");

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(Vizualization.displaygraph.getDisplayGraph())
                .build();
    }

    // return updated display graph
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @Path("/clicked")
    public Response expand(@QueryParam(value = "node") int node){

        Vizualization.clicked(node);

        String ret = Vizualization.displaygraph.getUpdates();

        if (server_debug) {
            System.out.println("[server-debug : display graph ]");
            Vizualization.displaygraph.display();
            System.out.println("[server-debug] Sending Response : \n" +
                   ret);
        }



        return Response.ok()
                .entity(ret)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }

    // return desired sub-graph
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/scrutinize")
    public Response scrutinize(@QueryParam(value = "node") int node){
        if (server_debug){
            System.out.println();
            System.out.println("--- Scrutiny Graph ---");
            Vizualization.displaygraph.display(node);
        }
        return Response.ok()
                .entity(Vizualization.displaygraph.getDisplayGraph(node))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }

    @GET
    @Path("/getpathnames")
    public Response getPathNames() {
        if (server_debug)
            System.out.println("[server-debug] Received Request for Path Names and Indices");
        String pathnames = Vizualization.getPathNames();

        if (server_debug)
            System.out.println("[server-debug] Sending Response : \n" + pathnames);

        return Response.ok()
                .entity(pathnames)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }

    @GET
    @Path("/getpathnodes")
    public Response getPathNodes(@QueryParam(value = "pathid") int pathid) {
        if (server_debug)
            System.out.println("[server-debug] Received Request for Path Nodes - path#" + pathid);

        String pathnodes = Vizualization.getPathNodes(pathid);

        if (server_debug)
            System.out.println("[server-debug] Path Generation Complete : \n" + pathnodes);

        return Response.ok()
                .entity(pathnodes)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }

    @GET
    @Path("/getnodepaths")
    public Response getNodePaths(){
        if (server_debug)
            System.out.println("[server-debug] Generating Path Nodes");

        String nodepaths = Vizualization.getNodePaths();

        if (server_debug)
            System.out.println("[server-debug] Path Nodes generation complete.\n" +
                    "Sending Response : \n" +
                    nodepaths);

        return Response.ok()
                .entity(nodepaths)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }

    // @TODO - support get hierarchies query during graph initialization
    @GET
    @Path("/gethierarchies")
    public Response gethierarchies(){
        if (server_debug)
            System.out.println("[server-debug] Generating Hierarchies");

        String hierarchies = Vizualization.getHierarchiesText();

        if (server_debug)
            System.out.println("[server-debug] Hierarchy generation complete.\n" +
                    "Sending Response : \n" +
                    hierarchies);

        return Response.ok()
                .entity(hierarchies)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS").build();
    }
}
