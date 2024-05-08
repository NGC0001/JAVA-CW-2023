package edu.uob;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class STAGTests {
    private static final String errorFlag = "Error";
    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
        "Server took too long to respond (probably stuck in an infinite loop)");
    }

    void assertSuccess(String response) {
        assertFalse(response.startsWith(errorFlag));
    }
    void assertFailure(String response) {
        assertTrue(response.startsWith(errorFlag));
    }

    @Test
    void testPlayerName() {
        String response = sendCommandToServer("simon.: look");
        assertFailure(response);
    }

    @Test
    void testCommandMatching() {
        String response;
        // Upper case commands
        response = sendCommandToServer("simon: Get Potion Health"); // builtin command health won't match
        assertSuccess(response);
        response = sendCommandToServer("simon: Get Potion");
        assertFailure(response);
        response = sendCommandToServer("simon: simon Drop Potion potion");
        assertSuccess(response);
        response = sendCommandToServer("simon: drop potion");
        assertFailure(response);

        // Ambiguous commands
        response = sendCommandToServer("simon: look health");
        assertFailure(response);
        response = sendCommandToServer("simon: get potion drink");
        assertFailure(response);
    }

    @Test
    void testNoMatchingCommand() {
        String response;
        response = sendCommandToServer("simon: goto cabin");
        assertFailure(response);
        response = sendCommandToServer("simon: get trapdoor");
        assertFailure(response);
    }

    @Test
    void testUnavailableOrExtraneousSubjects() {
        String response;
        response = sendCommandToServer("simon: get potion");
        assertSuccess(response);

        response = sendCommandToServer("simon: get potion");
        assertFailure(response);
        response = sendCommandToServer("simon: get log");
        assertFailure(response);

        response = sendCommandToServer("simon: drop potion");
        assertSuccess(response);

        response = sendCommandToServer("simon: get cabin axe");
        assertFailure(response);

        response = sendCommandToServer("simon: goto cellar");
        assertFailure(response);
    }

    @Test
    void testAction() {
        String response;
        response = sendCommandToServer("simon: get axe");
        assertSuccess(response);
        response = sendCommandToServer("simon: goto forest");
        assertSuccess(response);
        response = sendCommandToServer("simon: cut tree");
        assertSuccess(response);
        response = sendCommandToServer("simon: look");
        assertSuccess(response);
        assertFalse(response.contains("tree"));
        assertTrue(response.contains("log"));
        response = sendCommandToServer("simon: get key");
        assertSuccess(response);
        response = sendCommandToServer("simon: inv");
        assertSuccess(response);
        assertTrue(response.contains("key"));
        response = sendCommandToServer("simon: open trapdoor");
        assertFailure(response);
        response = sendCommandToServer("simon: goto cabin");
        assertSuccess(response);
        response = sendCommandToServer("simon: open trapdoor");
        assertSuccess(response);
        response = sendCommandToServer("simon: look");
        assertSuccess(response);
        assertTrue(response.contains("cellar"));
        response = sendCommandToServer("simon: inv inventory");
        assertSuccess(response);
        assertFalse(response.contains("key"));
        response = sendCommandToServer("simon: open trapdoor");
        assertFailure(response);
    }

    @Test
    void testPunctuation() {
        String response;
        response = sendCommandToServer("simon:hi,look!");
        assertSuccess(response);
        response = sendCommandToServer("simon:hi,look.inv");
        assertFailure(response);
    }
}