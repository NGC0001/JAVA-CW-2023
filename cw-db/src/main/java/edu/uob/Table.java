package edu.uob;

import edu.uob.Table.TableException.*;

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

/** This class represents a talbe of a database. */
public class Table {
    public static class TableException extends Exception {
        @Serial
        private static final long serialVersionUID = 1;

        public TableException(String message) {
            super(message);
        }

        public static class InvalidArgument extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidArgument(String message) {
                super(message);
            }
        }

        public static class InvalidOrDuplicateAttributeName extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public InvalidOrDuplicateAttributeName(String attrName) {
                super(attrName + " is invalid or duplicate for attribute name");
            }
        }

        public static class MissingOrInvalidNextId extends TableException {
            @Serial
            private static final long serialVersionUID = 1;

            public MissingOrInvalidNextId(long id) {
                super(id + " is invalid for nextId");
            }

            public MissingOrInvalidNextId() {
                super("missing nextId");
            }
        }
    }

    public static class Entity {
        private long id;
        private ArrayList<String> attributes;

        protected Entity(long id, Collection<? extends String> attributes) {
            this.id = id;
            this.attributes = new ArrayList<String>(attributes);
        }

        @Override
        public String toString() {
            String str = "" + id;
            for (String attr : attributes) {
                str += " " + attr;
            }
            return str;
        }
    }

    private ArrayList<String> attrNames;
    private long nextId;
    private ArrayList<Entity> entities;

    public Table(Collection<? extends String> attrNames, long nextId) throws TableException {
        if (attrNames == null) {
            throw new InvalidArgument("null attribute names for Table");
        }
        HashSet<String> attrNameSet = new HashSet<String>();
        for (String attrName : attrNames) {
            if (attrName == null
                    || !Grammar.isValidAttributeName(attrName)
                    || !attrNameSet.add(attrName.toLowerCase())) {
                throw new InvalidOrDuplicateAttributeName(attrName);
            }
        }
        this.attrNames = new ArrayList<String>(attrNames);
        if (nextId < 0) {
            throw new MissingOrInvalidNextId(nextId);
        }
        this.nextId = nextId;
        this.entities = new ArrayList<Entity>();
    }

    public Table(Collection<? extends String> attrNames) throws TableException {
        this(attrNames, 0);
    }

    public static Table loadFromFile(File file) {
        Table table = null;
        // FileReader throws IOException
        try (FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) { // throws IOException
                if ((line = line.trim()).length() == 0) {
                    continue;
                }
                if (table == null) {
                    table = Table.createFromString(line);
                    continue;
                }
                if (!table.addEntityFromString(line)) {
                    System.err.println("failed to add entity from line: " + line);
                }
            }
        } catch (TableException te) {
            System.err.println("failed to create table from " + file.getPath() + ": " + te);
            return null;
        } catch (IOException ioe) {
            System.err.println("while reading table from " + file.getPath() + ": " + ioe);
            return null;
        }
        return table;
    }

    public static Table createFromString(String str) throws TableException {
        if (str == null) {
            throw new InvalidArgument("creating Table from null");
        }
        Pattern tableStrPattern = Pattern.compile(
                Grammar.getIdAttrName() + "\\((\\d+)\\)(\\s.*)?", Pattern.DOTALL);
        Matcher tableStrMatcher = tableStrPattern.matcher(str.trim());
        if (!tableStrMatcher.matches()) {
            throw new MissingOrInvalidNextId();
        }
        long nextId = Long.parseLong(tableStrMatcher.group(1));
        String attrNamesString = tableStrMatcher.group(2);
        if (attrNamesString == null
                || (attrNamesString = attrNamesString.trim()).length() == 0) {
            return new Table(new ArrayList<String>(), nextId);
        }
        return new Table(Arrays.asList(attrNamesString.split("\\s+")), nextId);
    }

    // TODO: validate id and attributes
    protected boolean addEntity(long id, Collection<? extends String> attributes) {
        if (attributes == null) {
            return false;
        }
        entities.add(new Entity(id, attributes));
        return true;
    }

    public boolean addEntity(Collection<? extends String> attributes) {
        long newId = this.nextId++;
        return addEntity(newId, attributes);
    }

    public boolean addEntityFromString(String str) {
        if (str == null) {
            return false;
        }
        Pattern entityStrPattern = Pattern.compile("(\\d+)(\\s.*)?", Pattern.DOTALL);
        Matcher entityStrMatcher = entityStrPattern.matcher(str.trim());
        if (!entityStrMatcher.matches()) {
            return false;
        }
        long entityId = Long.parseLong(entityStrMatcher.group(1));
        String attributesString = entityStrMatcher.group(2);
        if (attributesString == null
                || (attributesString = attributesString.trim()).length() == 0) {
            return addEntity(entityId, new ArrayList<String>());
        }
        return addEntity(entityId, Arrays.asList(attributesString.split("\\s+")));
    }

    @Override
    public String toString() {
        String str = Grammar.getIdAttrName() + "(" + nextId + ")";
        for (String attrName : attrNames) {
            str += " " + attrName;
        }
        for (Entity entity : entities) {
            str += "\n" + entity.toString();
        }
        return str;
    }
}
