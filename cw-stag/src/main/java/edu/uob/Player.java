package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Player extends Character {
    private Map<String, Artefact> inventory;

    public Player(String name) {
        super(name, "");
        this.inventory = new HashMap<String, Artefact>();
    }

    public Map<String, Artefact> getInventory() {
        return this.inventory;
    }
}
