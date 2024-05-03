package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Location extends GameEntity {
    private Map<String, GameEntity> entities;

    public Location(String name, String description) {
        super(name, description);
        this.entities = new HashMap<String, GameEntity>();
    }

    public GameEntity addEntity(GameEntity entity) {
        return this.entities.put(entity.getName(), entity);
    }
}
