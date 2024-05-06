package edu.uob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameAction implements Command {
    private Set<String> triggers;
    private Set<String> subjects;
    private Set<String> consumed;
    private Set<String> produced;
    private String narration;

    public GameAction(List<String> triggers, String narration) {
        this.triggers = new HashSet<String>();
        this.subjects = new HashSet<String>();
        this.consumed = new HashSet<String>();
        this.produced = new HashSet<String>();
        this.triggers.addAll(triggers);
        this.narration = narration;
    }

    public GameAction addSubjects(Collection<? extends String> entities) {
        this.subjects.addAll(entities);
        return this;
    }

    public GameAction addConsumed(Collection<? extends String> entities) {
        this.consumed.addAll(entities);
        return this;
    }

    public GameAction addProduced(Collection<? extends String> entities) {
        this.produced.addAll(entities);
        return this;
    }

    @Override
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities) {
        Map<String, GameEntity> matchedSubjects = Command.matchPlayerCommand(playerCommand,
                new ArrayList<String>(this.triggers), gameEntities);
        if (matchedSubjects == null) {
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ACTION" + this.triggers + this.subjects + this.consumed + this.produced
                + "(" + this.narration + ")";
    }
}
