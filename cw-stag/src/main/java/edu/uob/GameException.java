package edu.uob;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class GameException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public GameException(String message) {
        super(message);
    }

    public static class InvalidCommandFormatException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public InvalidCommandFormatException() {
            super("invalid command format");
        }
    }

    public static class InvalidPlayerNameException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public InvalidPlayerNameException(String playerName) {
            super("invalid player name \"" + playerName + "\"");
        }
    }

    public static class AmbiguousCommandException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public AmbiguousCommandException(Command... commands) {
            super("ambiguous command at least matching\n" + String.join("\n",
                    Arrays.asList(commands).stream().map(cmd -> "  " + cmd.toString()).collect(Collectors.toList())
            ));
        }
    }

    public static class NoMatchingCommandException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public NoMatchingCommandException() {
            super("no matching command found");
        }
    }
}