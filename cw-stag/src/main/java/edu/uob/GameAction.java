package edu.uob;

import java.util.List;
import java.util.stream.Collectors;

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
    public Task buildTask(Player player, List<GameEntity> subjects) {
        if (player == null || subjects == null || subjects.isEmpty()) {
            return null;
        }
        for (GameEntity subject : subjects) { // check extraneous subject
            if (!this.subjects.contains(subject)) { // be careful here, no type checking
                return null;
            }
        }
        for (GameEntity subject : this.subjects) {
            if (!player.getLocation().getEntities().containsKey(subject.getName())) {
                if (!(subject instanceof Artefact)) {
                    return null;
                }
                Artefact artefact = (Artefact)subject;
                if (artefact.getOwner() != player) {
                    return null;
                }
            }
        }
        // TODO:
        // NOT possible to perform an action if subject/consumed/produced entity in another player's inventory
        // NOT possible to consume/produce current location
        // trigger phrases, rather than single word
        // punctuation: make drink with tee, sugar
        return (entityDefaultLocation, playerBornLocation) -> {
            for (GameEntity entity : consumed) {
                consumeEntity(entity);
            }
            for (GameEntity entity : produced) {
                produceEntity(entity);
            }
            return "";
        };
    }

    private static void consumeEntity(GameEntity entity) {
        if (entity instanceof Location) {}
    }

    @Override
    public String toString() {
        return "ACTION" + this.triggers
                + this.subjects.stream().map(e -> e.getName()).collect(Collectors.toList())
                + this.consumed.stream().map(e -> e.getName()).collect(Collectors.toList())
                + this.produced.stream().map(e -> e.getName()).collect(Collectors.toList())
                + "(" + this.narration + ")";
    }
}
