package edu.uob;

import java.util.List;
import java.util.Map;

public interface Command {
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities);
}