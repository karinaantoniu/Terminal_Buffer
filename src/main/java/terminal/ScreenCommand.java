package terminal;

public class ScreenCommand implements Command {
    public void execute(TerminalBuffer terminal) {
        System.out.print(terminal.getScreenAsString());
    }
}