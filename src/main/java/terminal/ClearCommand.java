package terminal;

public class ClearCommand implements Command {
    public void execute(TerminalBuffer terminal) {
        terminal.clearScreen();

        // remove also the commands from history
        terminal.clearCommandHistory();

        // to clear the terminal I printed endlines
        System.out.print("\n".repeat(50));
    }
}