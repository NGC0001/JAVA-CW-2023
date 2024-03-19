package edu.uob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// This class represents an operation on databases.
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

    public static class SelectTask extends Task {
        private List<String> selection; // null means to select all
        private String tableName;
        private Condition condition;

        public SelectTask(String tableName, List<String> selection, Condition condition) {
            super();
            this.selection = selection;
            this.tableName = tableName;
            this.condition = condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }

        public List<String> getSelection() {
            return this.selection;
        }

        public String getTableName() {
            return this.tableName;
        }

        public Condition getCondition() {
            return this.condition;
        }
    }

    public static class UpdateTask extends Task {
        private List<Map.Entry<String, String>> modification;
        private String tableName;
        private Condition condition;

        public UpdateTask(String tableName, List<Map.Entry<String, String>> modification,
                Condition condition) {
            super();
            this.modification = modification;
            this.tableName = tableName;
            this.condition = condition;
        }

        public List<Map.Entry<String, String>> getModification() {
            return this.modification;
        }

        public String getTableName() {
            return this.tableName;
        }

        public Condition getCondition() {
            return this.condition;
        }
    }

    public static class DeleteTask extends Task {
        private String tableName;
        private Condition condition;

        public DeleteTask(String tableName, Condition condition) {
            super();
            this.tableName = tableName;
            this.condition = condition;
        }

        public String getTableName() {
            return this.tableName;
        }

        public Condition getCondition() {
            return this.condition;
        }
    }

    public static class JoinTask extends Task {
        private String tableNameOne;
        private String tableNameTwo;
        private String attrNameOne;
        private String attrNameTwo;

        public JoinTask(String tableNameOne, String tableNameTwo, String attrNameOne,
                String attrNameTwo) {
            super();
            this.tableNameOne = tableNameOne;
            this.tableNameTwo = tableNameTwo;
            this.attrNameOne = attrNameOne;
            this.attrNameTwo = attrNameTwo;
        }

        public String getTableNameOne() {
            return this.tableNameOne;
        }

        public String getTableNameTwo() {
            return this.tableNameTwo;
        }

        public String getAttrNameOne() {
            return this.attrNameOne;
        }

        public String getAttrNameTwo() {
            return this.attrNameTwo;
        }
    }
}
