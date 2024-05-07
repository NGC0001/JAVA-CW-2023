package edu.uob;

import java.util.List;

public interface Command {
    public List<String> getTriggers();
    public Task buildTask(Player player, List<GameEntity> subjects);
}