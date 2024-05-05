package edu.uob;

public class Artefact extends LocatedEntity {
    private Player owner; // If this.location == null, then this.owner must != null

    public Artefact(String name, String description) {
        super(name, description);
        this.owner = null;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        if (owner != null) {
            super.setLocation(null);
        }
        this.owner = owner;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.owner = null;
        }
        super.setLocation(location);
    }
}
