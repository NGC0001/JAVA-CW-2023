package edu.uob;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum BuiltinCommand implements Command {
    INV(BuiltinCommand::buildInvTask, "inventory", "inv"),
    GET(null, "get"),
    DROP(null, "drop"),
    GOTO(null, "goto"),
    LOOK(null, "look"),
    HEALTH(null, "health");

    @FunctionalInterface
    private interface TaskBuilder {
        public Task buildTaskFromSubjects(Player player, Map<String, GameEntity> subjects);
    }

    private TaskBuilder builder;
    private List<String> triggers;

    private BuiltinCommand(TaskBuilder builder, String... triggers) {
        this.builder = builder;
        this.triggers = Arrays.asList(triggers);
    } 

    @Override
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities) {
        Map<String, GameEntity> subjects = Command.matchPlayerCommand(playerCommand, this.triggers, gameEntities);
        if (subjects == null) {
            return null;
        }
        return this.builder.buildTaskFromSubjects(player, subjects);
    }

    private static Task buildInvTask(Player player, Map<String, GameEntity> subjects) {
        if (subjects == null || !subjects.isEmpty()) {
            return null;
        }
        if (player == null) {
            return null;
        }
        return new Task() {
            String result;

            @Override
            public String run() {
                this.result = "";
                player.getInventory().forEach((name, artefact) -> {
                    this.result += name + "\n";
                });
                return this.result;
            }
        };
    }
}