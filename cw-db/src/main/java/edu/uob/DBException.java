package edu.uob;

import java.io.Serial;

public class DBException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;

    public DBException(String message) {
        super(message);
    }

    public static class NullObjectException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public NullObjectException(String message) {
            super("null object: " + message);
        }
    }

    public static class DatabaseStorageException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public DatabaseStorageException(String message) {
            super(message);
        }
    }

    public static class InvalidDatabaseNameException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public InvalidDatabaseNameException(String databaseName) {
            super("invalid database name " + databaseName);
        }

        public InvalidDatabaseNameException(String databaseName, String message) {
            super("invalid table name " + databaseName + ": " + message);
        }
    }

    public static class InvalidTableNameException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public InvalidTableNameException(String tableName) {
            super("invalid table name " + tableName);
        }

        public InvalidTableNameException(String tableName, String message) {
            super("invalid table name " + tableName + ": " + message);
        }
    }
}
