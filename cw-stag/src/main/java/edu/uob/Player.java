package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class Player extends Character {
    private static final int maxPlayerHealth = 3;
    private Map<String, Artefact> inventory;
    private int health;

    public Player(String name) {
        super(name, "");
        this.inventory = new HashMap<String, Artefact>();
        this.health = maxPlayerHealth;
    }

    public Map<String, Artefact> getInventory() {
        return this.inventory;
    }

    public void addArtefact(Artefact artefact) {
        if (artefact == null) { return; }
        this.inventory.put(artefact.getName(), artefact);
        artefact.setOwner(this);
    }

    public boolean removeArtefact(Artefact artefact) {
        if (artefact != null
                && this.inventory.remove(artefact.getName(), artefact)) {
            artefact.setOwner(null);
            return true;
        }
        return false;
    }

    public boolean ownsArtefact(Artefact artefact) {
        return artefact != null && this.inventory.get(artefact.getName()) == artefact;
    }

    public boolean ownsEntity(GameEntity entity) {
        return (entity instanceof Artefact) && ownsArtefact((Artefact)entity);
    }

    public int getHealth() {
        return this.health;
    }
}
