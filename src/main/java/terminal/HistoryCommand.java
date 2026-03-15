package terminal;

import java.util.List;

public class HistoryCommand implements Command {
    public void execute(TerminalBuffer terminal) {
        List<String> history = terminal.getCommandHistory();

        for (int i = 0; i < history.size(); i++)
            System.out.println("  " + (i + 1) + "  " + history.get(i));
    }
}