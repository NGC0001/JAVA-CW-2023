package edu.uob;

import java.io.Serial;

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

    public static class AmbiguousCommandException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public AmbiguousCommandException() {
            super("ambiguous command");
        }
    }

    public static class NoMatchingCommandException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public NoMatchingCommandException() {
            super("no matching command found");
        }
    }

    public static class CommandFailureException extends GameException {
        @Serial
        private static final long serialVersionUID = 1;

        public CommandFailureException(String message) {
            super(message);
        }
    }
}