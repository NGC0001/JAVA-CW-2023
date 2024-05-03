package edu.uob;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    public static final String entityTypeArtefact = "artefacts";
    public static final String entityTypeFurniture = "furniture";
    public static final String entityTypeCharacter = "characters";

    private Map<String, GameEntity> entities;
    private Map<String, GameAction> actions;
    private Map<String, Player> players;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        this.entities = new HashMap<String, GameEntity>();
        this.actions = new HashMap<String, GameAction>();
        this.players = new HashMap<String, Player>();
        loadEntitiesFromFile(entitiesFile);
        loadActionsFromFile(actionsFile);
    }

    private void loadEntitiesFromFile(File entitiesFile) {
        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();
            // The locations will always be in the first subgraph
            parseAllLocationsGraph(sections.get(0));
            // The paths will always be in the second subgraph
            parsePathsGraph(sections.get(1));
        } catch (Exception e) {
            System.err.println("when reading entities file: " + e.toString());
        }
        System.out.println(this.entities.toString());
    }

    private void parseAllLocationsGraph(Graph allLocationsGraph) {
        for (Graph locationGraph : allLocationsGraph.getSubgraphs()) {
            parseLocationGraph(locationGraph);
        }
    }

    private void parseLocationGraph(Graph locationGraph) {
        Node locationDetails = locationGraph.getNodes(true).get(0);
        String locationName = locationDetails.getId().getId();
        String locationDescription = locationDetails.getAttribute("description");
        Location location = new Location(locationName, locationDescription);
        addEntity(location);
        for (Graph entitiesGraph : locationGraph.getSubgraphs()) {
            String entityType = entitiesGraph.getId().getId();
            for (Node entityNode : entitiesGraph.getNodes(true)) {
                String entityName = entityNode.getId().getId();
                String entityDescription = entityNode.getAttribute("description");
                GameEntity entity = createEntity(entityType, entityName, entityDescription);
                addEntity(entity);
            }
        }
    }

    // TODO: ensure valid entity name
    public static GameEntity createEntity(String type, String name, String description) {
        if (type == null) { return null; }
        switch (type) {
            case entityTypeArtefact: return new Artefact(name, description);
            case entityTypeFurniture: return new Furniture(name, description);
            case entityTypeCharacter: return new Character(name, description);
            default: return null;
        }
    }

    public GameEntity addEntity(GameEntity entity) {
        if (entity == null) { return null; }
        return this.entities.put(entity.getName(), entity);
    }

    private void parsePathsGraph(Graph pathsGraph) {
        for (Edge pathEdge : pathsGraph.getEdges()) {
            parsePath(pathEdge);
        }
    }

    private void parsePath(Edge pathEdge) {
        Node fromLocation = pathEdge.getSource().getNode();
        String fromName = fromLocation.getId().getId();
        Node toLocation = pathEdge.getTarget().getNode();
        String toName = toLocation.getId().getId();
        System.out.println(fromName + "-->" + toName);
    }

    private void loadActionsFromFile(File actionsFile) {
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        return "";
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
