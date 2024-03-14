package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
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

    public String getFilePathForTable(String fileNamePrefix, String tableName, Table table)
            throws DBException {
        if (tableName == null || table == null) {
            throw new DBException.NullObjectException("getting table name from null objects");
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
