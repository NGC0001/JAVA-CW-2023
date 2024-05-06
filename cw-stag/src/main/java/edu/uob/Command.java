package edu.uob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Command {
    public List<String> getTriggers();
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities);

    public static Map<String, GameEntity> matchPlayerCommand(List<String> playerCommand, List<String> triggers,
            Map<String, GameEntity> gameEntities) {
        if (playerCommand == null || triggers == null || gameEntities == null) {
            return null;
        }
        boolean triggerMatched = false;
        Map<String, GameEntity> subjects = new HashMap<String, GameEntity>();
        for (String word : playerCommand) {
            if (wordMatchesTriggers(word, triggers)) {
                triggerMatched = true;
                continue;
            }
            GameEntity entity = gameEntities.get(word);
            if (entity == null) {
                continue;
            }
            if (subjects.putIfAbsent(entity.getName(), entity) != null) {
                return null;
            }
        }
        if (!triggerMatched) {
            return null;
        }
        return subjects;
    }

    private static boolean wordMatchesTriggers(String word, List<String> triggers) {
        for (String trigger : triggers) {
            if (trigger != null && trigger.equals(word)) {
                return true;
            }
        }
        return false;
    }
}