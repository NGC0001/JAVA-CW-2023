package edu.uob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// This class represent an operation on databases.
// It is the parse reuslt of an incomming user command.
public abstract class Task {
    public Task() {
    }

    public static class UseTask extends Task {
        private String databaseName;

        public UseTask(String databaseName) {
            super();
            this.databaseName = databaseName;
        }

        public String getDatabaseName() {
            return this.databaseName;
        }
    }

    public static class CreateDatabaseTask extends Task {
        private String databaseName;

        public CreateDatabaseTask(String databaseName) {
            super();
            this.databaseName = databaseName;
        }

        public String getDatabaseName() {
            return this.databaseName;
        }
    }

    public static class CreateTableTask extends Task {
        private String tableName;
        private List<String> attrNames;

        public CreateTableTask(String tableName) {
            super();
            this.tableName = tableName;
            this.attrNames = new ArrayList<String>();
        }

        public String getTableName() {
            return this.tableName;
        }

        public List<String> getAttrNames() {
            return this.attrNames;
        }

        public void addAttrNames(Collection<? extends String> attrNames) {
            this.attrNames.addAll(attrNames);
        }
    }

    public static class DropDatabaseTask extends Task {
        private String databaseName;

        public DropDatabaseTask(String databaseName) {
            super();
            this.databaseName = databaseName;
        }

        public String getDatabaseName() {
            return this.databaseName;
        }
    }

    public static class DropTableTask extends Task {
        private String tableName;

        public DropTableTask(String tableName) {
            super();
            this.tableName = tableName;
        }

        public String getTableName() {
            return this.tableName;
        }
    }

    public static class AlterTask extends Task {
        private String tableName;
        private String attrName;
        private boolean adding;

        public AlterTask(String tableName, String attrName, boolean adding) {
            super();
            this.tableName = tableName;
            this.attrName = attrName;
            this.adding = adding;
        }

        public String getTableName() {
            return this.tableName;
        }

        public String getAttrName() {
            return this.attrName;
        }

        public boolean isAdding() {
            return this.adding;
        }
    }

    public static class InsertTask extends Task {
        private String tableName;
        private List<String> values;

        public InsertTask(String tableName, List<String> values) {
            super();
            this.tableName = tableName;
            this.values = values;
        }

        public String getTableName() {
            return this.tableName;
        }

        public List<String> getValues() {
            return this.values;
        }
    }
}
