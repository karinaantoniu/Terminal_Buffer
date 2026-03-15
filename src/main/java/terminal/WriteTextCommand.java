package terminal;

public class WriteTextCommand implements Command {
    private String textToWrite;

    public WriteTextCommand(String textToWrite) {
        this.textToWrite = textToWrite;
    }

    public void execute(TerminalBuffer terminal) {

        terminal.writeText("user@java-term:~$ " + textToWrite + "\n");
    }
}