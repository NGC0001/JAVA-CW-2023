package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Player extends Character {
    private Map<String, Artefact> inventory;

    public Player(String name, String description, Location location) {
        super(name, description, location);
        this.inventory = new HashMap<String, Artefact>();
    }
}
