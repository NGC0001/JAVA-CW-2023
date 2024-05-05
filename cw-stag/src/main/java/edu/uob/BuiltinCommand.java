package edu.uob;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum BuiltinCommand implements Command {
    INV(BuiltinCommand::buildInvTask, "inventory", "inv"),
    GET(BuiltinCommand::buildGetTask, "get"),
    DROP(BuiltinCommand::buildDropTask, "drop"),
    GOTO(BuiltinCommand::buildGotoTask, "goto"),
    LOOK(BuiltinCommand::buildLookTask, "look"),
    HEALTH(BuiltinCommand::buildHealthTask, "health");

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
        if (player == null) {
            return null;
        }
        Map<String, GameEntity> subjects = Command.matchPlayerCommand(playerCommand, this.triggers, gameEntities);
        if (subjects == null) {
            return null;
        }
        return this.builder.buildTaskFromSubjects(player, subjects);
    }

    private static Task buildInvTask(Player player, Map<String, GameEntity> subjects) {
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

    private static Task buildGetTask(Player player, Map<String, GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.values().iterator().next();
        return new Task() {
            @Override
            public String run() throws GameException {
                if (!(subject instanceof Artefact)) {
                    throw new GameException.CommandFailureException(subject.getName() + " is not an artifect");
                }
                Artefact artefact = (Artefact)subject;
                if (!player.getLocation().removeLocatedEntity(artefact)) {
                    String artefactPosition = null;
                    if (artefact.getLocation() == null) {
                        artefactPosition = "on " + artefact.getOwner().getName();
                    } else {
                        artefactPosition = "at " + artefact.getLocation().getName();
                    }
                    throw new GameException.CommandFailureException(artefact.getName() + " is " + artefactPosition
                            + ", not at " + player.getLocation().getName());
                }
                player.addArtefact(artefact);
                return "";
            }
        };
    }

    private static Task buildDropTask(Player player, Map<String, GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.values().iterator().next();
        return new Task() {
            @Override
            public String run() throws GameException {
                if (!(subject instanceof Artefact)) {
                    throw new GameException.CommandFailureException(subject.getName() + " is not an artifect");
                }
                Artefact artefact = (Artefact)subject;
                if (!player.removeArtefact(artefact)) {
                    throw new GameException.CommandFailureException(artefact.getName() + " is not on "
                            + player.getName());
                }
                player.getLocation().addLocatedEntity(artefact);
                return "";
            }
        };
    }

    private static Task buildGotoTask(Player player, Map<String, GameEntity> subjects) {
        if (subjects.size() != 1) {
            return null;
        }
        GameEntity subject = subjects.values().iterator().next();
        return new Task() {
            @Override
            public String run() throws GameException {
                if (!(subject instanceof Location)) {
                    throw new GameException.CommandFailureException(subject.getName() + " is not a location");
                }
                Location destination = (Location)subject;
                if (!player.getLocation().hasPathTo(destination)) {
                    throw new GameException.CommandFailureException(player.getLocation().getName() + " has no path to "
                            + destination.getName());
                }
                player.getLocation().removeLocatedEntity(player);
                destination.addLocatedEntity(player);
                return "";
            }
        };
    }

    private static Task buildLookTask(Player player, Map<String, GameEntity> subjects) {
        if (!subjects.isEmpty()) {
            return null;
        }
        return new Task() {
            private String result;

            @Override
            public String run() {
                this.result = "";
                player.getLocation().getEntities().forEach((name, entity) -> {
                    if (entity != player) {
                        this.result += name + " : " + entity.getDescription() + "\n";
                    }
                });
                return this.result;
            }
        };
    }

    private static Task buildHealthTask(Player player, Map<String, GameEntity> subjects) {
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