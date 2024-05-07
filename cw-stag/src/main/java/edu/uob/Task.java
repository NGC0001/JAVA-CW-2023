package edu.uob;

@FunctionalInterface
public interface Task {
    public String run(Location entityDefaultLocation, Location playerBornLocation);
}
