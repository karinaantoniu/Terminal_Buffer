package terminal;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TerminalBuffer terminal = new TerminalBuffer(80, 24, 1000);
        terminal.addObserver(new TerminalObserver() {
            public void onTerminalChanged() {}
        });

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("user@java-term:~$ ");
            String input = scanner.nextLine();

            if (input.isEmpty())
                continue;

            terminal.saveCommandToHistory(input);

            if (input.equals("/exit"))
                break;

            // command pattern
            Command command = parseCommand(input);
            if (command != null) {
                command.execute(terminal);
            }
        }

        scanner.close();
    }

    private static Command parseCommand(String input) {
        if (input.equals("/clear")) {
            return new ClearCommand();
        } else if (input.startsWith("/resize")) {
            try {
                String[] parts = input.split(" ");
                return new ResizeCommand(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } catch (Exception e) {
                System.err.println("Syntax Error");
                return null;
            }
        } else if (input.equals("/history")) {
            return new HistoryCommand();
        } else if (input.equals("/screen")) {
            return new ScreenCommand();
        } else {
            return new WriteTextCommand(input);
        }
    }
}