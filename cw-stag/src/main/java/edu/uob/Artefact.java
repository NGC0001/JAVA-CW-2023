package edu.uob;

public class Artefact extends LocatedEntity {
    private Player owner; // If this.location == null, then this.owner must != null

    public Artefact(String name, String description) {
        super(name, description);
        this.owner = null;
    }
}
