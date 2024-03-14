package edu.uob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// This class represents a collection of databases.
public class DBKeeper {
    private static final String databasesMetaFileName = "databases.meta";
    private static final char metaFormatBracketLeft = '{';
    private static final char metaFormatBracketRight = '}';
    private static final String metaFormatDelim = ";";

    private HashMap<String, Database> databases;
    private Database currentDb;

    public DBKeeper() {
        this.databases = new HashMap<String, Database>();
    }

    // Files.readAllBytes also throws Error
    // Files.readAllBytes also throws RuntimeException
    public void loadByMetaFile(Path metaFilePath) throws DBException, IOException {
        if (metaFilePath == null) {
            throw new DBException.NullObjectException(
                    "loading database keeper from null meta file");
        }
        String meta = new String(Files.readAllBytes(metaFilePath));
        loadByMetaString(meta, metaFilePath.getParent());
    }

    // Paths.get/Path.toFile/File.isFile also throws RuntimeException
    public void loadFromDirectory(String directoryPath) throws DBException, IOException {
        if (directoryPath == null) {
            throw new DBException.NullObjectException(
                    "loading database keeper from null directory");
        }
        Path metaFilePath = Paths.get(directoryPath, databasesMetaFileName);
        if (!metaFilePath.toFile().isFile()) {
            throw new DBException.DatabaseStorageException(
                    "cannot find databases meta file " + metaFilePath.toString());
        }
        loadByMetaFile(metaFilePath);
    }

    public void loadByMetaString(String meta, Path databasesDirPath)
            throws DBException, IOException {
        if (meta == null) {
            throw new DBException.NullObjectException(
                    "loading database keeper from null meta string");
        }
        meta = meta.trim();
        if (meta.length() < 2 || meta.charAt(0) != metaFormatBracketLeft
                || meta.charAt(meta.length() - 1) != metaFormatBracketRight) {
            throw new DBException.DatabaseStorageException(
                    "ill-formatted meta string for database keeper");
        }
        meta = meta.substring(1, meta.length() - 1).trim();
        clear();
        if (meta.length() == 0) {
            return;
        }
        String[] databaseDescriptions = meta.split(Pattern.quote(metaFormatDelim));
        for (String databaseDescription : Arrays.asList(databaseDescriptions)) {
            try {
                loadDatabaseByDescription(databaseDescription, databasesDirPath);
            } catch (Exception e) {
                System.err.println("exception loading database: " + e);
            }
        }
    }

    public void loadDatabaseByDescription(String description, Path databasesDirPath)
            throws DBException, IOException {
        if (description == null) {
            throw new DBException.NullObjectException(
                    "loading database from null description");
        }
        String[] databaseNameAndMeta = description.split(":", 2);
        if (databaseNameAndMeta.length != 2) {
            throw new DBException.DatabaseStorageException(
                    "ill-formatted description string for database");
        }
        String dbName = databaseNameAndMeta[0].trim();
        String dbMeta = databaseNameAndMeta[1];
        Database db = new Database();
        db.loadByMetaString(dbMeta, databasesDirPath, dbName);
        addDatabase(dbName, db);
    }

    public Result executeTask(Task task) {
        return new Result();
    }

    public void addDatabase(String databaseName, Database db) throws DBException {
        if (db == null) {
            throw new DBException.NullObjectException("adding null database");
        }
        if (!Grammar.isValidDatabaseName(databaseName)) {
            throw new DBException.InvalidDatabaseNameException(databaseName);
        }
        Database oldDataBase = this.databases.putIfAbsent(databaseName.toLowerCase(), db);
        if (oldDataBase != null) {
            throw new DBException.InvalidDatabaseNameException(databaseName, "duplicate");
        }
    }

    public Database getDatabase(String databaseName) {
        if (databaseName == null || databaseName == null) {
            return null;
        }
        return this.databases.get(databaseName.toLowerCase());
    }

    public void clear() {
        this.databases.clear();
        this.currentDb = null;
    }

    @Override
    public String toString() {
        return "" + metaFormatBracketLeft
                + String.join(metaFormatDelim, this.databases.entrySet().stream()
                        .map((entry) -> entry.getKey() + ":" + entry.getValue())
                        .collect(Collectors.toList()))
                + metaFormatBracketRight;
    }
}
