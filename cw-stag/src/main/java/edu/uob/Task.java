package edu.uob;

@FunctionalInterface
public interface Task {
    public String run() throws GameException;
}
