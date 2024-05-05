package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Location extends GameEntity {
    private Map<String, GameEntity> entities;

    public Location(String name, String description) {
        super(name, description);
        this.entities = new HashMap<String, GameEntity>();
    }

    public Map<String, GameEntity> getEntities() {
        return this.entities;
    }

    private boolean addEntity(GameEntity entity) {
        if (entity == null) { return false; }
        this.entities.put(entity.getName(), entity);
        return true;
    }

    private boolean removeEntity(GameEntity entity) {
        return entity != null && this.entities.remove(entity.getName(), entity);
    }

    private boolean containsEntity(GameEntity entity) {
        return entity != null && this.entities.get(entity.getName()) == entity;
    }

    public void addLocatedEntity(LocatedEntity entity) {
        if (addEntity(entity)) {
            entity.setLocation(this);
        }
    }

    public boolean removeLocatedEntity(LocatedEntity entity) {
        boolean removed = removeEntity(entity);
        if (removed) {
            entity.setLocation(null);
        }
        return removed;
    }

    public void addPathTo(Location otherLocation) {
        addEntity(otherLocation);
    }

    public boolean removePathTo(Location otherLocation) {
        return removeEntity(otherLocation);
    }

    public boolean hasPathTo(Location destination) {
        return containsEntity(destination);
    }

    public void printEntities() {
        System.out.println(toString());
        this.entities.forEach((name, entity) -> {
            System.out.println("  " + entity.toString());
        });
    }
}