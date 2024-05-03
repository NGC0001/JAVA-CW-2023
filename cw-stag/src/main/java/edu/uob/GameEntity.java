package edu.uob;

public abstract class GameEntity {
    private String name;
    private String description;

    public GameEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + getName() + ")";
    }
}
