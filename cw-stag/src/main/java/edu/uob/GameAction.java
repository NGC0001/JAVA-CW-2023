package edu.uob;

import java.util.HashSet;
import java.util.Set;

public class GameAction {
    private Set<String> triggers;
    private Set<String> subjects;
    private Set<String> consumed;
    private Set<String> produced;
    private String narration;

    public GameAction(String trigger, String subject, String narration) {
        this.triggers = new HashSet<String>();
        this.subjects = new HashSet<String>();
        this.consumed = new HashSet<String>();
        this.produced = new HashSet<String>();
        this.triggers.add(trigger);
        this.subjects.add(subject);
        this.narration = narration;
    }
}
