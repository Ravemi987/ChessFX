package fr.chessproject.chessfx.model.uci;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CommandDispatcher {
    private final List<UciCommand> commands = new ArrayList<>();

    public CommandDispatcher() {
        register(new PositionCommand());
        register(new GoCommand());
        register(new SetOptionCommand());
        register(new DisplayCommand());
        register(new QuitCommand());
    }

    public void register(UciCommand command) {
        commands.add(command);
    }

    public void dispatch(String input, BiConsumer<String, String[]> callback) {
        for (UciCommand cmd : commands) {
            if (cmd.matches(input)) {
                cmd.execute(input, callback);
                return;
            }
        }
        System.out.println("Unknown or malformed command: " + input);
    }
}
