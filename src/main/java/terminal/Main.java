package terminal;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TerminalBuffer terminal = new TerminalBuffer(80, 24, 1000);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Terminal Emulator v1.0. Type '/help' for commands.");

        while (true) {
            System.out.print("user@java-term:~$ ");
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equals("/exit")) {
                break;
            } else if (input.equals("/clear")) {
                terminal.clearScreen();
            } else if (input.equals("/history")) {
                System.out.println(terminal.getScreenScrollBackAsString().trim());
            } else if (input.equals("/help")) {
                System.out.println("Commands: /exit, /clear, /history, /screen");
            } else if (input.equals("/screen")) {
                System.out.print(terminal.getScreenAsString());
            } else {
                // save the input in buffer
                terminal.writeText(input + "\n");
            }
        }

        scanner.close();
    }
}