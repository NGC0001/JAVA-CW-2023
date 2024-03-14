package edu.uob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class represents a talbe of a database.
public class Table {
    public static class TableException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public TableException(String message) {
            super(message);
        }

        public static class InvalidAttributeNameException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidAttributeNameException(String attrName) {
                super("invalid attribute name " + attrName);
            }

            public InvalidAttributeNameException(String attrName, String message) {
                super("invalid attribute name " + attrName + ": " + message);
            }
        }

        public static class NegativeNextIdException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public NegativeNextIdException(long nextId) {
                super("negative nextId " + nextId);
            }
        }

        public static class InvalidTableStringException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidTableStringException(String str) {
                super("string does not represent a table: " + str);
            }
        }

        public static class InvalidAttributeValueException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidAttributeValueException(String attrValue) {
                super("invalid attribute value " + attrValue);
            }
        }

        public static class InvalidNumberOfAttributesException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidNumberOfAttributesException(int expected, int actual) {
                super("invalid attributes number, expected " + expected + ", actually " + actual);
            }
        }

        public static class NegativeEntityIdException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public NegativeEntityIdException(long id) {
                super("negative entity id " + id);
            }
        }

        public static class InvalidEntityStringException extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidEntityStringException(String str) {
                super("string does not represent an entity: " + str);
            }
        }
    }

    public static class Entity {
        private final long id;
        private ArrayList<String> attributes;

        protected Entity(long id) throws DBException {
            if (id < 0) {
                throw new TableException.NegativeEntityIdException(id);
            }
            this.id = id;
            this.attributes = new ArrayList<String>();
        }

        protected void addAttribute(String attr) throws DBException {
            if (!Grammar.isValidAttributeValue(attr)) {
                throw new TableException.InvalidAttributeNameException(attr);
            }
            this.attributes.add(attr);
        }

        protected void addAttributes(Collection<? extends String> attributes) throws DBException {
            if (attributes == null) {
                throw new DBException.NullObjectException("null attributes to add");
            }
            for (String attr : attributes) {
                addAttribute(attr);
            }
        }

        @Override
        public String toString() {
            return "(" + exportToString(",") + ")";
        }

        public String exportToString(String delim) {
            if (delim == null) {
                delim = " ";
            }
            String str = "" + this.id;
            for (String attr : this.attributes) {
                str += delim + attr;
            }
            return str;
        }
    }

    private static final char metaFormatBracketLeft = '<';
    private static final char metaFormatBracketRight = '>';
    private static final String metaFormatDelim = "|";

    private long nextId;
    private HashSet<String> attrNameSet;
    private ArrayList<String> attrNames;
    private ArrayList<Entity> entities;

    public Table() throws DBException {
        this(0);
    }

    public Table(long nextId) throws DBException {
        if (nextId < 0) {
            throw new TableException.NegativeNextIdException(nextId);
        }
        this.nextId = nextId;
        this.attrNameSet = new HashSet<String>();
        this.attrNames = new ArrayList<String>();
        this.entities = new ArrayList<Entity>();
    }

    public static Table createFromMetaString(String meta) throws DBException {
        if (meta == null) {
            throw new DBException.NullObjectException("creating table from null meta");
        }
        meta = meta.trim();
        if (meta.length() < 2 || meta.charAt(0) != metaFormatBracketLeft
                || meta.charAt(meta.length() - 1) != metaFormatBracketRight) {
            throw new DBException.DatabaseStorageException(
                    "ill-formatted meta string for table: " + meta);
        }
        meta = meta.substring(1, meta.length() - 1);
        String[] nextIdAndAttrNames = meta.split(":", 2);
        if (nextIdAndAttrNames.length != 2) {
            throw new DBException.DatabaseStorageException(
                    "cannot split table meta string: " + meta);
        }
        String nextIdString = nextIdAndAttrNames[0].trim();
        String attrNamesString = nextIdAndAttrNames[1];
        long nextId = Long.parseLong(nextIdString);
        Table table = new Table(nextId);
        table.addAttrFieldsByString(attrNamesString, metaFormatDelim);
        return table;
    }

    public void loadFromFile(File file) throws DBException, IOException {
        if (file == null) {
            throw new DBException.NullObjectException("null file for loading table");
        }
        if (!file.isFile()) {
            throw new DBException.DatabaseStorageException(
                    "cannot find table file " + file.getPath());
        }
        try (BufferedReader bufReader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = bufReader.readLine()) != null && (line = line.trim()).length() == 0) {
            }
            if (!validateTableHeader(Arrays.asList(line.split("\\s+")))) {
                throw new DBException.DatabaseStorageException(
                        "invalid table header in " + file.getPath());
            }
            while ((line = bufReader.readLine()) != null) {
                if ((line = line.trim()).length() == 0) {
                    continue;
                }
                addEntityFromString(line);
            }
        }
    }

    public boolean validateTableHeader(Collection<? extends String> headerFields) {
        if (getNumberOfAttrFields() + 1 != headerFields.size()) {
            return false;
        }
        int fieldIdx = 0;
        for (String field : headerFields) {
            field = field.toLowerCase();
            if (fieldIdx == 0 && !Grammar.getIdAttrName().equals(field)) {
                return false;
            }
            if (fieldIdx > 0 && !this.attrNames.get(fieldIdx - 1).toLowerCase().equals(field)) {
                return false;
            }
            fieldIdx++;
        }
        return true;
    }

    public long getNextId() {
        return this.nextId;
    }

    public void addAttrFieldsByString(String attrNamesString, String delim) throws DBException {
        if (attrNamesString == null || delim == null) {
            throw new DBException.NullObjectException(
                    "null arguments while adding attributes by string");
        }
        attrNamesString = attrNamesString.trim();
        if (attrNamesString.length() == 0) {
            return;
        }
        String[] attrNames = attrNamesString.split(Pattern.quote(delim));
        for (String attrName : Arrays.asList(attrNames)) {
            attrName = attrName.trim();
            addAttrField(attrName);
        }
    }

    public void addAttrFields(Collection<? extends String> attrNames) throws DBException {
        if (attrNames == null) {
            throw new DBException.NullObjectException("null attrNames to add");
        }
        for (String attrName : attrNames) {
            addAttrField(attrName);
        }
    }

    public void addAttrField(String attrName) throws DBException {
        if (!Grammar.isValidAttributeName(attrName)) {
            throw new TableException.InvalidAttributeNameException(attrName);
        }
        if (!this.attrNameSet.add(attrName.toLowerCase())) {
            throw new TableException.InvalidAttributeNameException(attrName, "duplicate");
        }
        this.attrNames.add(attrName);
        for (Entity entity : this.entities) {
            entity.addAttribute(Grammar.Keyword.NULL.toString());
        }
    }

    public int getNumberOfAttrFields() {
        return this.attrNames.size();
    }

    protected void addEntity(long id, Collection<? extends String> attributes) throws DBException {
        if (getNumberOfAttrFields() != attributes.size()) {
            throw new TableException.InvalidNumberOfAttributesException(
                    getNumberOfAttrFields(), attributes.size());
        }
        Entity entity = new Entity(id);
        entity.addAttributes(attributes);
        this.entities.add(entity);
    }

    public void addEntity(Collection<? extends String> attributes) throws DBException {
        long newId = this.nextId++;
        addEntity(newId, attributes);
    }

    public void addEntityFromString(String str) throws DBException {
        if (str == null) {
            throw new DBException.NullObjectException("null string for adding entity");
        }
        Pattern entityStrPattern = Pattern.compile("\\s*(\\d+)(\\s.*)?", Pattern.DOTALL);
        Matcher entityStrMatcher = entityStrPattern.matcher(str);
        if (!entityStrMatcher.matches()) {
            throw new TableException.InvalidEntityStringException(str);
        }
        long entityId = Long.parseLong(entityStrMatcher.group(1));
        ArrayList<String> attrValues = new ArrayList<String>();
        String attributesString = entityStrMatcher.group(2);
        if (attributesString != null) {
            attrValues.addAll(Grammar.getTokensFromString(attributesString));
        }
        addEntity(entityId, attrValues);
    }

    public void clear() {
        this.entities.clear();
    }

    @Override
    public String toString() {
        return "" + metaFormatBracketLeft + this.nextId + ":"
                + String.join(metaFormatDelim, this.attrNames)
                + metaFormatBracketRight;
    }

    public String exportToString(String delim) {
        if (delim == null) {
            delim = " ";
        }
        String str = Grammar.getIdAttrName();
        for (String attrName : this.attrNames) {
            str += delim + attrName;
        }
        for (Entity entity : this.entities) {
            str += "\n" + entity.exportToString(delim);
        }
        return str;
    }
}
