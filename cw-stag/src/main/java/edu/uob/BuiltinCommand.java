package edu.uob;

import java.util.Arrays;
import java.util.List;

public enum BuiltinCommand implements Command {
    // triggers must be unique and in lower case.
    INV(BuiltinCommand::buildInvTask, "inventory", "inv"),
    GET(BuiltinCommand::buildGetTask, "get"),
    DROP(BuiltinCommand::buildDropTask, "drop"),
    GOTO(BuiltinCommand::buildGotoTask, "goto"),
    LOOK(BuiltinCommand::buildLookTask, "look"),
    HEALTH(BuiltinCommand::buildHealthTask, "health");

    @FunctionalInterface
    private interface TaskBuilder {
        public Task buildTaskFromSubjects(Player player, List<GameEntity> subjects);
    }

    private TaskBuilder builder;
    private List<String> triggers;

    private BuiltinCommand(TaskBuilder builder, String... triggers) {
        this.builder = builder;
        this.triggers = Arrays.asList(triggers);
    } 

    @Override
    public List<String> getTriggers() {
        return this.triggers;
    }

    @Override
    public String toString() {
        return name() + this.triggers;
    }

    @Override
    public Task buildTask(Player player, List<GameEntity> subjects) {
        if (player == null || subjects == null) {
            return null;
        }
        return this.builder.buildTaskFromSubjects(player, subjects);
    }

    private static Task buildInvTask(Player player, List<GameEntity> subjects) {
        if (!subjects.isEmpty()) {
            return null;
        }
        return new Task() {
            private String result;

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

    private static Task buildGetTask(Player player, List<GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.get(0);
        if (!(subject instanceof Artefact)) {
            return null;
        }
        Artefact artefact = (Artefact) subject;
        if (player.getLocation() != artefact.getLocation()) {
            return null;
        }
        return new Task() {
            @Override
            public String run() {
                if (player.getLocation().removeLocatedEntity(artefact)) {
                    player.addArtefact(artefact);
                    return "";
                }
                return "Error"; // shall never hapen
            }
        };
    }

    private static Task buildDropTask(Player player, List<GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.get(0);
        if (!(subject instanceof Artefact)) {
            return null;
        }
        Artefact artefact = (Artefact) subject;
        if (artefact.getOwner() != player) {
            return null;
        }
        return new Task() {
            @Override
            public String run() {
                if (player.removeArtefact(artefact)) {
                    player.getLocation().addLocatedEntity(artefact);
                    return "";
                }
                return "Error"; // shall never hapen
            }
        };
    }

    private static Task buildGotoTask(Player player, List<GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.get(0);
        if (!(subject instanceof Location)) {
            return null;
        }
        Location destination = (Location) subject;
        if (!player.getLocation().hasPathTo(destination)) {
            return null;
        }
        return new Task() {
            @Override
            public String run() {
                player.getLocation().removeLocatedEntity(player);
                destination.addLocatedEntity(player);
                return "";
            }
        };
    }

    private static Task buildLookTask(Player player, List<GameEntity> subjects) {
        if (!subjects.isEmpty()) {
            return null;
        }
        return new Task() {
            private String result;

            @Override
            public String run() {
                Location location = player.getLocation();
                this.result = location.getName() + " : " + location.getDescription() + "\n";
                player.getLocation().getEntities().forEach((name, entity) -> {
                    if (entity != player) {
                        this.result += name + " : " + entity.getDescription() + "\n";
                    }
                });
                return this.result;
            }
        };
    }

    private static Task buildHealthTask(Player player, List<GameEntity> subjects) {
        if (!subjects.isEmpty()) {
            return null;
        }
        return new Task() {
            @Override
            public String run() {
                return String.valueOf(player.getHealth());
            }
        };
    }
}