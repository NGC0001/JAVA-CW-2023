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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private static final String entityTypeArtefact = "artefacts";
    private static final String entityTypeFurniture = "furniture";
    private static final String entityTypeCharacter = "characters";
    private static final String defaultLocationName = "storeroom";
    private static final String healthKeyword = "health";

    private Map<String, GameEntity> entities;
    private Map<String, List<Command>> commands;
    private Map<String, Player> players;
    private Location entityDefaultLocation;
    private Location playerBornLocation;

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
        this.commands = new HashMap<String, List<Command>>();
        this.players = new HashMap<String, Player>();
        this.playerBornLocation = null;
        addCommands(BuiltinCommand.values());
        loadEntitiesFromFile(entitiesFile); // shall set player born location
        loadActionsFromFile(actionsFile);
        this.entityDefaultLocation = (Location)this.entities.get(defaultLocationName);
        if (this.entityDefaultLocation == null) {
            // ensures default location exists
            this.entityDefaultLocation = new Location(defaultLocationName, "fallback");
            addEntity(this.entityDefaultLocation);
        }
        if (this.playerBornLocation == null) {
            this.playerBornLocation = this.entityDefaultLocation;
        }
        printEntities(); // For debug.
        printCommands(); // For debug.
    }

    private void addCommands(Command[] commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    private boolean addCommand(Command command) {
        for (String trigger : command.getTriggers()) {
            if (defaultLocationName.equals(trigger)) {
                return false;
            }
            if (this.entities.containsKey(trigger)) { // trigger shouldn't be an existing entity name
                return false;
            }
        }
        for (String trigger : command.getTriggers()) {
            List<Command> commandList = this.commands.get(trigger);
            if (commandList == null) {
                commandList = new ArrayList<Command>();
                this.commands.put(trigger, commandList);
            }
            commandList.add(command);
        }
        return true;
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
    }

    private void parseAllLocationsGraph(Graph allLocationsGraph) {
        for (Graph locationGraph : allLocationsGraph.getSubgraphs()) {
            parseLocationGraph(locationGraph);
        }
    }

    private void parseLocationGraph(Graph locationGraph) {
        Node locationDetails = locationGraph.getNodes(true).get(0);
        String locationName = locationDetails.getId().getId().toLowerCase();
        String locationDescription = locationDetails.getAttribute("description");
        Location location = new Location(locationName, locationDescription);
        if (!addEntity(location)) { return; }
        if (this.playerBornLocation == null) {
            this.playerBornLocation = location;
        }
        for (Graph entitiesGraph : locationGraph.getSubgraphs()) {
            String entityType = entitiesGraph.getId().getId().toLowerCase();
            for (Node entityNode : entitiesGraph.getNodes(true)) {
                String entityName = entityNode.getId().getId().toLowerCase();
                String entityDescription = entityNode.getAttribute("description");
                LocatedEntity entity = createLocatedEntity(entityType, entityName, entityDescription);
                if (entity != null && addEntity(entity)) {
                    location.addLocatedEntity(entity);
                }
            }
        }
    }

    private static LocatedEntity createLocatedEntity(String type, String name, String description) {
        if (type == null) { return null; }
        switch (type) {
            case entityTypeArtefact: return new Artefact(name, description);
            case entityTypeFurniture: return new Furniture(name, description);
            case entityTypeCharacter: return new Character(name, description);
            default: return null;
        }
    }

    private void parsePathsGraph(Graph pathsGraph) {
        for (Edge pathEdge : pathsGraph.getEdges()) {
            parsePath(pathEdge);
        }
    }

    private void parsePath(Edge pathEdge) {
        Node fromNode = pathEdge.getSource().getNode();
        String fromLocationName = fromNode.getId().getId().toLowerCase();
        GameEntity fromEntity = getEntity(fromLocationName);
        if (fromEntity == null || !(fromEntity instanceof Location)) { return; }
        Location fromLocation = (Location)(fromEntity);
        Node toNode = pathEdge.getTarget().getNode();
        String toLocationName = toNode.getId().getId().toLowerCase();
        GameEntity toEntity = getEntity(toLocationName);
        if (toEntity == null || !(toEntity instanceof Location)) { return; }
        Location toLocation = (Location)(toEntity);
        fromLocation.addPathTo(toLocation);
    }

    private void loadActionsFromFile(File actionsFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            // Get the first action (only the odd items are actually actions - 1, 3, 5 etc.)
            for (int i = 0; i < actions.getLength(); ++i) {
                org.w3c.dom.Node actionNode = actions.item(i);
                if (actionNode instanceof Element) {
                    Element action = (Element) actionNode;
                    parseActionElement(action);
                }
            }
        } catch (Exception e) {
            System.err.println("when reading actions file: " + e.toString());
        }
    }

    private void parseActionElement(Element actionElement) {
        List<String> triggersList = getTaggedTextList(actionElement, "triggers", "keyphrase");
        Set<String> triggersSet = new HashSet<String>(triggersList);
        List<String> triggers = new ArrayList<String>(triggersSet);
        if (triggersSet.isEmpty()) {
            return;
        }
        List<String> subjectsNames = getTaggedTextList(actionElement, "subjects", "entity");
        List<String> consumedNames = getTaggedTextList(actionElement, "consumed", "entity");
        List<String> producedNames = getTaggedTextList(actionElement, "produced", "entity");
        int consumedHealth = pickoutStringFromList(consumedNames, healthKeyword);
        int producedHealth = pickoutStringFromList(producedNames, healthKeyword);
        List<GameEntity> subjects = getEntitiesByNames(new HashSet<String>(subjectsNames));
        List<GameEntity> consumed = getEntitiesByNames(new HashSet<String>(consumedNames));
        List<GameEntity> produced = getEntitiesByNames(new HashSet<String>(producedNames));
        if (subjects == null || consumed == null || produced == null) {
            // entities specified by the action do not exist
            return;
        }
        String narration = "";
        NodeList narrationNodes = actionElement.getElementsByTagName("narration");
        if (narrationNodes.getLength() > 0) {
            narration = narrationNodes.item(0).getTextContent().trim();
        }
        GameAction action = new GameAction(triggers, subjects, narration);
        action.setConsumed(consumed, consumedHealth);
        action.setProduced(produced, producedHealth);
        addCommand(action);
    }

    private List<String> getTaggedTextList(Element element, String parentTagName, String tagName) {
        List<String> result = new ArrayList<String>();
        NodeList parantTagNodes = element.getElementsByTagName(parentTagName);
        for (int iParent = 0; iParent < parantTagNodes.getLength(); ++iParent) {
            org.w3c.dom.Node parentTagNode = parantTagNodes.item(iParent);
            if (!(parentTagNode instanceof Element)) {
                continue;
            }
            Element parentTagElement = (Element)parentTagNode;
            NodeList tagNodes = parentTagElement.getElementsByTagName(tagName);
            for (int iTag = 0; iTag < tagNodes.getLength(); ++iTag) {
                String text = tagNodes.item(iTag).getTextContent();
                if (text == null) {
                    continue;
                }
                result.add(text.trim().toLowerCase());
            }
        }
        return result;
    }

    private static int pickoutStringFromList(List<String> list, String word) {
        int i = 0;
        int cnt = 0;
        while (i < list.size()) {
            if (word.equals(list.get(i))) {
                list.remove(i);
                ++cnt;
            } else {
                ++i;
            }
        }
        return cnt;
    }

    private List<GameEntity> getEntitiesByNames(Collection<String> names) {
        List<GameEntity> entities = new ArrayList<GameEntity>();
        for (String name : names) {
            GameEntity entity = getEntity(name);
            if (entity == null) {
                return null;
            }
            entities.add(entity);
        }
        return entities;
    }

    private GameEntity getEntity(String name) {
        return this.entities.get(name);
    }

    private boolean addEntity(GameEntity entity) {
        String entityName = entity.getName();
        if (healthKeyword.equals(entityName)) {
            return false;
        }
        if (defaultLocationName.equals(entityName) && !(entity instanceof Location)) {
            return false;
        }
        if (this.commands.containsKey(entityName)) { // entity name shouldn't be an existing trigger
            return false;
        }
        return this.entities.putIfAbsent(entityName, entity) == null; // entity name should be unique
    }

    public void printEntities() {
        this.entities.forEach((name, entity) -> {
            if (entity instanceof Location) {
                Location location = (Location)entity;
                location.printEntities();
            }
        });
    }

    public void printCommands() {
        Set<Command> commandSet = new HashSet<Command>();
        this.commands.values().forEach((commandList) -> {
            commandSet.addAll(commandList);
        });
        commandSet.forEach((command) -> {
            System.out.println(command.toString());
        });
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        try {
            String[] playerNameAndCommand = command.toLowerCase().split(":", 2);
            if (playerNameAndCommand.length != 2) {
                throw new GameException.InvalidCommandFormatException();
            }
            String playerName = playerNameAndCommand[0].trim();
            Player player = getOrCreatePlayer(playerName);
            List<String> playerCommand = Arrays.asList(playerNameAndCommand[1].trim().split("\\s+"));
            List<GameEntity> subjects = getSubjectsFromPlayerCommand(playerCommand);
            Task matchedTask = null;
            for (Command cmd : getCandidateCommandsFromPlayerCommand(playerCommand)) {
                Task task = cmd.buildTask(player, subjects);
                if (task == null) { continue; }
                if (matchedTask != null) {
                    throw new GameException.AmbiguousCommandException();
                }
                matchedTask = task;
            }
            if (matchedTask == null) {
                throw new GameException.NoMatchingCommandException();
            }
            return matchedTask.run(this.entityDefaultLocation, this.playerBornLocation);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Player getOrCreatePlayer(String playerName) {
        Player player = this.players.get(playerName);
        if (player != null) {
            return player;
        }
        player = new Player(playerName);
        this.players.put(playerName, player);
        this.playerBornLocation.addLocatedEntity(player);
        return player;
    }

    private List<GameEntity> getSubjectsFromPlayerCommand(Collection<String> playerCommand) {
        List<GameEntity> entities = new ArrayList<GameEntity>();
        for (String name : new HashSet<String>(playerCommand)) {
            GameEntity entity = getEntity(name);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    private Set<Command> getCandidateCommandsFromPlayerCommand(Collection<String> playerCommand) {
        Set<Command> candidateCommands = new HashSet<Command>();
        for (String trigger : playerCommand) {
            List<Command> commands = this.commands.get(trigger);
            if (commands != null) {
                candidateCommands.addAll(commands);
            }
        }
        return candidateCommands;
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
