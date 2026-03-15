package terminal;

public class ResizeCommand implements Command {
    private int width;
    private int height;

    public ResizeCommand(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void execute(TerminalBuffer terminal) {
        terminal.resize(width, height);
    }
}