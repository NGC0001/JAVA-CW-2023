package edu.uob;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GameAction implements Command {
    private List<String> triggers;
    private List<GameEntity> subjects;
    private List<GameEntity> consumed;
    private List<GameEntity> produced;
    private String narration;

    public GameAction(List<String> triggers, List<GameEntity> subjects,
            List<GameEntity> consumed, List<GameEntity> produced, String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumed = consumed;
        this.produced = produced;
        this.narration = narration;
    }

    @Override
    public List<String> getTriggers() {
        return this.triggers;
    }

    @Override
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities) {
        Map<String, GameEntity> matchedSubjects = Command.matchPlayerCommand(
                playerCommand, this.triggers, gameEntities);
        if (matchedSubjects == null || matchedSubjects.isEmpty()) {
            return null;
        }
        for (GameEntity entity : matchedSubjects.values()) {
            if (!this.subjects.contains(entity.getName())) {
                return null;
            }
        }
        for (String entityName : this.subjects) {
            GameEntity entity = gameEntities.get(entityName);
            if (entity == null) {
                return null;
            }
            if (!player.getLocation().getEntities().containsKey(entity.getName())) {
                if (!(entity instanceof Artefact)) {
                    return null;
                }
                Artefact artefact = (Artefact)entity;
                if (artefact.getOwner() != player) {
                    return null;
                }
            }
        }
        return new Task() {
            @Override
            public String run() {
                return "";
            }
        };
    }

    @Override
    public String toString() {
        return "ACTION" + this.triggers + this.subjects + this.consumed + this.produced
                + "(" + this.narration + ")";
    }
}
