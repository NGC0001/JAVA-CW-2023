package edu.uob;

import java.util.List;
import java.util.Map;

public enum BuiltinCommand implements Command {
    INV(new String[] {"inventory", "inv"}),
    GET(new String[] {"get"}),
    DROP(new String[] {"drop"}),
    GOTO(new String[] {"goto"}),
    LOOK(new String[] {"look"}),
    HEALTH(new String[] {"health"});

    private String[] triggers;

    private BuiltinCommand(String[] triggers) {
        this.triggers = triggers;
    } 

    @Override
    public Task buildTask(Player player, List<String> playerCommand, Map<String, GameEntity> gameEntities) {
        return null;
    }
}