package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** This class represents a collection of databases. */
public class DBKeeper {
    private static final String tableFileNameSuffix = "tab";

    private HashMap<String, Database> databases;
    private Database currentDb;

    public DBKeeper() {
        databases = new HashMap<String, Database>();
    }

    public boolean addDatabase(String databaseName) { // TODO: throws
        if (databaseName == null || !Grammar.isValidDatabaseName(databaseName)) {
            return false;
        }
        databaseName = databaseName.toLowerCase();
        if (databases.putIfAbsent(databaseName, new Database()) != null) {
            return false;
        }
        return true;
    }

    public Database getOrCreateDatabase(String databaseName) {
        if (databaseName == null || !Grammar.isValidDatabaseName(databaseName)) {
            return null;
        }
        databaseName = databaseName.toLowerCase();
        Database db = databases.get(databaseName);
        if (db == null) {
            db = new Database();
            databases.put(databaseName, db);
        }
        return db;
    }

    public boolean addTable(Map.Entry<String, String> tableName, Table table) {
        Database db = getOrCreateDatabase(tableName.getValue());
        db.addTable(tableName.getKey(), table);
        return true;
    }

    @Override
    public String toString() {
        String str = "DBKeeper{"
                + String.join(", ",
                        databases.entrySet().stream().map((entry) -> entry.getKey() + ":" + entry.getValue())
                                .collect(Collectors.toList()))
                + "}";
        return str;
    }

    private static Map.Entry<String, String> getTableNameFromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        Pattern tableFileNamePattern = Pattern.compile("(.+)\\.(.+)\\." + tableFileNameSuffix);
        Matcher tableFileNameMatcher = tableFileNamePattern.matcher(fileName);
        if (!tableFileNameMatcher.matches()) {
            return null;
        }
        String databaseName = tableFileNameMatcher.group(1);
        String tableName = tableFileNameMatcher.group(2);
        return new AbstractMap.SimpleEntry<String, String>(tableName, databaseName);
    }

    public static DBKeeper loadFromDirectory(String directoryPath) {
        DBKeeper dbKeeper = new DBKeeper();
        if (directoryPath == null) {
            return dbKeeper;
        }
        try {
            // Paths.get throws RuntimeException:InvalidPathException
            // Files.list throws IOException, RuntimeException:SecurityException
            // Path.toFile throws RuntimeException:UnsupportedOperationException
            // File.isFile throws RuntimeException:SecurityException
            List<Path> filePaths = Files.list(Paths.get(directoryPath))
                    .collect(Collectors.toList());
            for (Path filePath : filePaths) {
                Map.Entry<String, String> tableName = getTableNameFromFileName(
                        filePath.getFileName().toString());
                if (tableName == null) {
                    continue;
                }
                File file = filePath.toFile();
                if (!file.isFile()) {
                    continue;
                }
                Table table = Table.loadFromFile(file);
                if (table == null) {
                    continue;
                }
                dbKeeper.addTable(tableName, table);
            }
        } catch (IOException ioe) {
            System.err.println("while loading databases from " + directoryPath + ": " + ioe);
        } catch (Exception e) {
            System.err.println(e);
        }
        return dbKeeper;
    }
}
