package edu.uob;

public class LocatedEntity extends GameEntity {
    private Location location;

    public LocatedEntity(String name, String description) {
        super(name, description);
        this.location = null;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        String locationString = this.location == null ? "" : this.location.getName();
        return super.toString() + "[" + locationString + "]";
    }
}