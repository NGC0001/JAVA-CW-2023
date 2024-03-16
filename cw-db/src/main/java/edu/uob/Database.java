package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// This class represents a database.
public class Database {
    private static final String tableFileNameSuffix = "tab";
    private static final char metaFormatBracketLeft = '[';
    private static final char metaFormatBracketRight = ']';
    private static final String metaFormatDelim = ",";

    private HashMap<String, Table> tables;

    public Database() {
        this.tables = new HashMap<String, Table>();
    }

    public void loadByMetaString(String meta, Path dataDir, String tableFileNamePrefix)
            throws DBException, IOException {
        if (meta == null) {
            throw new DBException.NullObjectException(
                    "null argument(s) while loading database from meta string");
        }
        meta = meta.trim();
        if (meta.length() < 2 || meta.charAt(0) != metaFormatBracketLeft
                || meta.charAt(meta.length() - 1) != metaFormatBracketRight) {
            throw new DBException.DatabaseStorageException(
                    "ill-formatted meta string for database");
        }
        meta = meta.substring(1, meta.length() - 1).trim();
        clear();
        if (meta.length() == 0) {
            return;
        }
        String[] tableDescriptions = meta.split(Pattern.quote(metaFormatDelim));
        for (String tableDescription : Arrays.asList(tableDescriptions)) {
            try {
                loadTableByDescription(tableDescription, dataDir, tableFileNamePrefix);
            } catch (Exception e) {
                System.err.println("exception loading table: " + e);
            }
        }
    }

    public void loadTableByDescription(String description, Path dataDir, String tableFileNamePrefix)
            throws DBException, IOException {
        if (description == null || dataDir == null || tableFileNamePrefix == null) {
            throw new DBException.NullObjectException(
                    "null argument(s) while loading table by description");
        }
        String[] tableNameAndMeta = description.split(":", 2);
        if (tableNameAndMeta.length != 2) {
            throw new DBException.DatabaseStorageException(
                    "ill-formatted description string for table");
        }
        String tableName = tableNameAndMeta[0].trim();
        String tableMeta = tableNameAndMeta[1];
        Table table = Table.createFromMetaString(tableMeta);
        String tableFileName = getFilePathForTable(tableFileNamePrefix, tableName, table);
        File tableFile = Paths.get(dataDir.toString(), tableFileName).toFile();
        table.loadFromFile(tableFile);
        addTable(tableName, table);
    }

    public String storeToDirectory(Path dataDir, String tableFileNamePrefix)
            throws DBException, IOException {
        if (dataDir == null || tableFileNamePrefix == null) {
            throw new DBException.NullObjectException(
                    "null arguments while storing database to directory");
        }
        ArrayList<String> tableDescriptions = new ArrayList<String>();
        for (Map.Entry<String, Table> entry : this.tables.entrySet()) {
            String tableName = entry.getKey();
            Table table = entry.getValue();
            String tableFileName = getFilePathForTable(tableFileNamePrefix, tableName, table);
            Path tableFilePath = Paths.get(dataDir.toString(), tableFileName);
            String tableMeta = table.storeToFile(tableFilePath);
            tableDescriptions.add(tableName + ": " + tableMeta);
        }
        String meta = String.join(metaFormatDelim + "\n", tableDescriptions);
        if (meta.length() > 0) {
            meta = "\n" + meta;
        }
        meta = meta.replace("\n", "\n  ") + "\n";
        meta = metaFormatBracketLeft + meta + metaFormatBracketRight;
        return meta;
    }

    public String getFilePathForTable(String fileNamePrefix, String tableName, Table table)
            throws DBException {
        if (tableName == null || table == null) {
            throw new DBException.NullObjectException(
                    "getting table name from null objects");
        }
        String tableFileName = tableName + "." + table.getNextId() + "." + tableFileNameSuffix;
        if (fileNamePrefix != null) {
            tableFileName = fileNamePrefix + "." + tableFileName;
        }
        tableFileName = tableFileName.toLowerCase();
        return tableFileName;
    }

    public void addTable(String tableName, Table table) throws DBException {
        if (table == null) {
            throw new DBException.NullObjectException("adding null table");
        }
        if (!Grammar.isValidTableName(tableName)) {
            throw new DBException.InvalidTableNameException(tableName);
        }
        Table oldTable = this.tables.putIfAbsent(tableName.toLowerCase(), table);
        if (oldTable != null) {
            throw new DBException.InvalidTableNameException(tableName, "duplicate");
        }
    }

    public void clear() {
        tables.clear();
    }

    @Override
    public String toString() {
        return "" + metaFormatBracketLeft
                + String.join(metaFormatDelim, this.tables.entrySet().stream()
                        .map((entry) -> entry.getKey() + ":" + entry.getValue())
                        .collect(Collectors.toList()))
                + metaFormatBracketRight;
    }
}
