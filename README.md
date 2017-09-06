# PanFR
Check out the wiki and the poster for details on tool implementation, use, and efficiency

## Running the client:  
    run ./app/client/index.html in your web browser

## Running the serverlet  
    java -jar ./app/serverlet/serverlet.jar <frs file> <frpaths file>
    
    [note - current version runs the server on localhost:9997, test data is in ./data]

## Serverlet Dependencies  
     org.glassfish.jersey.containers:jersey-container-jetty-http:2.26-b032  
     org.glassfish.jersey.media:jersey-media-json-jackson:2.26-b032
     
     [note - these dependencies are already present in serverlet.jar]
