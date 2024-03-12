package edu.uob;

import java.util.HashMap;

/** This class represents a database. */
public class Database {
    private HashMap<String, Table> tables;

    public Database() {
        tables = new HashMap<String, Table>();
    }

    public boolean addTable(String tableName, Table table) { // TODO: throws
        if (tableName == null || table == null
                || !Grammar.isValidTableName(tableName)) {
            return false;
        }
        tableName = tableName.toLowerCase();
        if (tables.putIfAbsent(tableName, table) != null) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "Database{"
                + String.join(",", tables.keySet())
                + "}";
        return str;
    }
}
