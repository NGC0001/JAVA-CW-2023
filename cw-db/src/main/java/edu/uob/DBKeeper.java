package edu.uob;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// This class represents a collection of databases.
// It is also an executor of user commands.
public class DBKeeper {
    private static final String databasesMetaFileName = "databases.meta";
    private static final char metaFormatBracketLeft = '{';
    private static final char metaFormatBracketRight = '}';
    private static final String metaFormatDelim = ";";

    private HashMap<String, Database> databases;
    private Database currentDb;
    private boolean updatedByTask;

    public DBKeeper() {
        this.databases = new HashMap<String, Database>();
        this.currentDb = null;
        this.updatedByTask = false;
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

    public void storeToDirectory(String directoryPath) throws DBException, IOException {
        if (directoryPath == null) {
            throw new DBException.NullObjectException(
                    "storing database keeper to null directory");
        }
        Path metaFilePath = Paths.get(directoryPath, databasesMetaFileName);
        ArrayList<String> dbDescriptions = new ArrayList<String>();
        for (Map.Entry<String, Database> entry : this.databases.entrySet()) {
            String dbName = entry.getKey();
            Database db = entry.getValue();
            String dbMeta = db.storeToDirectory(metaFilePath.getParent(), dbName);
            dbDescriptions.add(dbName + ": " + dbMeta);
        }
        String meta = String.join(metaFormatDelim + "\n", dbDescriptions);
        if (meta.length() > 0) {
            meta = "\n" + meta;
        }
        meta = meta.replace("\n", "\n  ") + "\n";
        meta = metaFormatBracketLeft + meta + metaFormatBracketRight;
        Files.write(metaFilePath, meta.getBytes(), StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public Result executeTask(Task task) throws DBException {
        if (task instanceof Task.UseTask) {
            return executeUse((Task.UseTask) task);
        } else if (task instanceof Task.CreateDatabaseTask) {
            return executeCreateDatabase((Task.CreateDatabaseTask) task);
        } else if (task instanceof Task.CreateTableTask) {
            return executeCreateTable((Task.CreateTableTask) task);
        } else if (task instanceof Task.DropDatabaseTask) {
            return executeDropDatabase((Task.DropDatabaseTask) task);
        } else if (task instanceof Task.DropTableTask) {
            return executeDropTable((Task.DropTableTask) task);
        } else if (task instanceof Task.AlterTask) {
            return executeAlter((Task.AlterTask) task);
        } else {
            throw new DBException("executing unknown type of task");
        }
    }

    private Result executeUse(Task.UseTask task) throws DBException {
        setCurrentDatabase(task.getDatabaseName());
        return new Result();
    }

    private Result executeCreateDatabase(Task.CreateDatabaseTask task) throws DBException {
        String dbName = task.getDatabaseName();
        Database db = new Database();
        addDatabase(dbName, db);
        setUpdatedByTask();
        return new Result();
    }

    private Result executeCreateTable(Task.CreateTableTask task) throws DBException {
        String tableName = task.getTableName();
        Table table = new Table();
        table.addAttrFields(task.getAttrNames());
        getCurrentDatabase().addTable(tableName, table);
        setUpdatedByTask();
        return new Result();
    }

    private Result executeDropDatabase(Task.DropDatabaseTask task) throws DBException {
        dropDatabase(task.getDatabaseName());
        setUpdatedByTask();
        return new Result();
    }

    private Result executeDropTable(Task.DropTableTask task) throws DBException {
        getCurrentDatabase().dropTable(task.getTableName());
        setUpdatedByTask();
        return new Result();
    }

    private Result executeAlter(Task.AlterTask task) throws DBException {
        Table table = getCurrentDatabase().getTable(task.getTableName());
        if (task.isAdding()) {
            table.addAttrField(task.getAttrName());
        } else {
            table.dropAttrField(task.getAttrName());
        }
        setUpdatedByTask();
        return new Result();
    }

    public void addDatabase(String databaseName, Database db) throws DBException {
        if (db == null) {
            throw new DBException.NullObjectException("adding null database");
        }
        if (!Grammar.isValidDatabaseName(databaseName)) {
            throw new DBException.InvalidDatabaseNameException(databaseName);
        }
        Database oldDatabase = this.databases.putIfAbsent(databaseName.toLowerCase(), db);
        if (oldDatabase != null) {
            throw new DBException.InvalidDatabaseNameException(databaseName, "duplicate");
        }
    }

    public void dropDatabase(String databaseName) throws DBException {
        if (databaseName == null) {
            throw new DBException.NullObjectException("null database name for dropping");
        }
        Database removedDatabase = this.databases.remove(databaseName.toLowerCase());
        if (removedDatabase == null) {
            throw new DBException.InvalidDatabaseNameException(databaseName, "not exists");
        }
        if (removedDatabase == this.currentDb) {
            this.currentDb = null;
        }
    }

    public Database getDatabase(String databaseName) throws DBException {
        if (databaseName == null) {
            throw new DBException.NullObjectException("null database name");
        }
        Database db = this.databases.get(databaseName.toLowerCase());
        if (db == null) {
            throw new DBException.InvalidDatabaseNameException(databaseName, "not exists");
        }
        return db;
    }

    public boolean getUpdatedByTask() {
        return this.updatedByTask;
    }

    private void setUpdatedByTask() {
        this.updatedByTask = true;
    }

    public void resetUpdatedByTask() {
        this.updatedByTask = false;
    }

    private Database getCurrentDatabase() throws DBException {
        if (this.currentDb == null) {
            throw new DBException("current database not set yet");
        }
        return this.currentDb;
    }

    public void setCurrentDatabase(String dbName) throws DBException {
        this.currentDb = getDatabase(dbName);
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
