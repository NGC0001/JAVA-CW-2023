package edu.uob;
import edu.uob.Table.TableException.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This class represents a talbe of a database. */
public class Table {
  public static String idAttrName = "id";

  public static class Entity {
    private long id;
    private ArrayList<String> attributes;

    // TODO: validate
    protected Entity(long id, Collection<? extends String> attributes) {
      this.id = id;
      this.attributes = new ArrayList<String>(attributes);
    }

    @Override
    public String toString() {
        String str = "" + id;
        for (String attr : attributes) { str += " " + attr; }
        return str;
    }
  }

  public static class TableException extends Exception {
    @Serial private static final long serialVersionUID = 1;
    public TableException(String message) {
        super(message);
    }

    public static class InvalidOrDuplicateAttributeName extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public InvalidOrDuplicateAttributeName(String attrName) {
            super(attrName + " is invalid or duplicate for attribute name");
        }
    }

    public static class MissingOrInvalidNextId extends TableException {
        @Serial private static final long serialVersionUID = 1;
        public MissingOrInvalidNextId(long id) {
            super(id + " is invalid for nextId");
        }
        public MissingOrInvalidNextId() {
            super("missing nextId");
        }
    }
  }

  // TODO
  public static boolean isValidAttributeName(String attrName) {
    return true; // != idAttrName, only letter/digit
  }

  private ArrayList<String> attrNames;
  private long nextId;
  private ArrayList<Entity> entities;

  public Table(Collection<? extends String> attrNames, long nextId) throws TableException {
    HashSet<String> attrNameSet = new HashSet<String>();
    for (String attrName : attrNames) {
        if (!isValidAttributeName(attrName)
                || !attrNameSet.add(attrName.toLowerCase())) {
            throw new InvalidOrDuplicateAttributeName(attrName);
        }
    }
    this.attrNames = new ArrayList<String>(attrNames);

    if (nextId < 0) { throw new MissingOrInvalidNextId(nextId); }
    this.nextId = nextId;

    this.entities = new ArrayList<Entity>();
  }

  public Table(Collection<? extends String> attrNames) throws TableException {
    this(attrNames, 0);
  }

  public static Table createFromString(String str) throws TableException {
      Pattern tableStrPattern = Pattern.compile("id\\((\\d+)\\)(\\s.*)?", Pattern.DOTALL);
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

  protected boolean addEntity(Entity entity) {
    return entities.add(entity);
  }

  protected boolean addEntity(long id, Collection<? extends String> attributes) {
    return addEntity(new Entity(id, attributes));
  }

  public boolean addEntity(Collection<? extends String> attributes) {
    long newId = this.nextId++;
    return addEntity(newId, attributes);
  }

  public boolean addEntityFromString(String str) {
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
      String str = "id(" + nextId + ")";
      for (String attrName : attrNames) { str += " " + attrName; }
      for (Entity entity : entities) { str += "\n" + entity.toString(); }
      return str;
  }
}
