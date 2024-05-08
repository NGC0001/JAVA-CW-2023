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

    public void dropInventory() {
        this.inventory.values().forEach((artefact) ->{
            artefact.setOwner(null);
            getLocation().addLocatedEntity(artefact);
        });
    }

    public boolean ownsArtefact(Artefact artefact) {
        return artefact != null && this.inventory.get(artefact.getName()) == artefact;
    }

    public int getHealth() {
        return this.health;
    }

    public void increaseHealth(int inc) {
        this.health = Math.min(this.health + inc, maxPlayerHealth);
    }

    public void decreaseHealth(int inc) {
        this.health = Math.max(this.health - inc, 0);
    }

    public void resetHealth() {
        this.health = maxPlayerHealth;
    }
}
