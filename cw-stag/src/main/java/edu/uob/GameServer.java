package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.utils.HashMap;
import java.utils.Map;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;

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
        // TODO implement your server logic here
        this.entities = loadEntitiesFromFile(entitiesFile);
        this.actions = loadActionsFromFile(actionsFile);
        this·players = new HashMap<String, Player>();
    }

    private Map<String, GameEntity> loadEntitiesFromFile(File entitiesFile) {
        Map<String, GameEntity>() entities = new HashMap<String, GameEntity>();
        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();
            // The locations will always be in the first subgraph
            parseLocations(sections.get(0));
            // The paths will always be in the second subgraph
            parsePaths(sections.get(1));

        } catch (FileNotFoundException fnfe) {
            fail("FileNotFoundException was thrown when attempting to read basic entities file");
        } catch (ParseException pe) {
            fail("ParseException was thrown when attempting to read basic entities file");
        }
        return entities;
    }

    private void parseLocations(Graph locations) {
        for (Graph localtion : locations.getSubgraphs()) {
            parseLocation(location);
        }
    }

    private void parseLocation(Graph location) {
        Node locationDetails = firstLocation.getNodes(false).get(0);
        // Yes, you do need to get the ID twice !
        String locationName = locationDetails.getId().getId();
    }

    private void parsePaths(Graph paths) {
        for (Edge path : paths.getEdges()) {
            parsePath(path);
        }
    }

    private void parsePath(Edge path) {
            Node fromLocation = firstPath.getSource().getNode();
            String fromName = fromLocation.getId().getId();
            Node toLocation = firstPath.getTarget().getNode();
            String toName = toLocation.getId().getId();

            assertEquals("cabin", fromName, "First path should have been from 'cabin'");
            assertEquals("forest", toName, "First path should have been to 'forest'");
    }

    private Map<String, GameAction> loadActionsFromFile(File actionsFile) {
        Map<String, GameAction>() actions = new HashMap<String, GameAction>();
        return actions;
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
