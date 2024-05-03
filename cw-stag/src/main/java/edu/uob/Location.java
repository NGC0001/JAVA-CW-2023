package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Location extends GameEntity {
    private Map<String, GameEntity> entities;

    public Location(String name, String description) {
        super(name, description);
        this.entities = new HashMap<String, GameEntity>();
    }

    public void addLocatedEntity(LocatedEntity entity) {
        if (entity == null) { return; }
        this.entities.put(entity.getName(), entity);
        entity.setLocation(this);
    }

    public boolean removeLocatedEntity(LocatedEntity entity) {
        if (entity != null
                && this.entities.remove(entity.getName(), entity)) {
            entity.setLocation(null);
            return true;
        }
        return false;
    }

    public void addPathTo(Location otherLocation) {
        if (otherLocation == null) { return; }
        this.entities.put(otherLocation.getName(), otherLocation);
    }

    public boolean removePathTo(Location otherLocation) {
        return otherLocation != null && this.entities.remove(otherLocation.getName(), otherLocation);
    }

    public void printEntities() {
        System.out.println(toString());
        this.entities.forEach((name, entity) -> {
            System.out.println("  " + entity.toString());
        });
    }
}
