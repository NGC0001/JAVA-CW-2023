package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameAction implements Command {
    private List<String> triggers;
    private List<GameEntity> subjects;
    private List<GameEntity> consumed;
    private int consumedHealth;
    private List<GameEntity> produced;
    private int producedHealth;
    private String narration;

    public GameAction(List<String> triggers, List<GameEntity> subjects, String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumed = new ArrayList<GameEntity>();
        this.consumedHealth = 0;
        this.produced = new ArrayList<GameEntity>();
        this.producedHealth = 0;
        this.narration = narration;
    }

    public void setConsumed(List<GameEntity> consumed, int consumedHealth) {
        this.consumed = consumed;
        this.consumedHealth = consumedHealth;
    }

    public void setProduced(List<GameEntity> produced, int producedHealth) {
        this.produced = produced;
        this.producedHealth = producedHealth;
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
        if (!allSubjectsAvailableForPlayer(player) || !allConsumeProduceSuitableForPlayer(player)) {
            return null;
        }
        return (entityDefaultLocation, playerBornLocation) -> {
            doConsume();
            doProduce();
            return this.narration;
        };
    }

    private boolean allSubjectsAvailableForPlayer(Player player) {
        return this.subjects.stream().allMatch((entity) -> {
            return player.getLocation().containsOrHasPathTo(entity) || player.ownsEntity(entity);
        });
    }

    private boolean allConsumeProduceSuitableForPlayer(Player player) {
        return Stream.concat(this.consumed.stream(), this.produced.stream()).allMatch((entity) -> {
            if (entity == player.getLocation()) {
                return false;
            }
            if (!(entity instanceof Artefact)) {
                return true;
            }
            Artefact artefact = (Artefact) entity;
            return artefact.getOwner() == null || artefact.getOwner() == player;
        });
    }

    private void doConsume() {}

    private void doProduce() {}

    @Override
    public String toString() {
        return "ACTION" + this.triggers
                + this.subjects.stream().map(e -> e.getName()).collect(Collectors.toList())
                + this.consumed.stream().map(e -> e.getName()).collect(Collectors.toList())
                + "<hp-" + this.consumedHealth + ">"
                + this.produced.stream().map(e -> e.getName()).collect(Collectors.toList())
                + "<hp+" + this.producedHealth + ">"
                + "(" + this.narration + ")";
    }
}
