package terminal;

public class Test {

    public static void main(String[] args) {
        testCursorBounds();
        testScrollbackLimit();
        testWideCharacterNormal();
        testWideCharacterEdgeOfScreen();
        testResizeReflow();
    }

    private static void testCursorBounds() {
        System.out.println("Cursor Bounds");
        TerminalBuffer term = new TerminalBuffer(10, 4, 5);

        System.out.println("user@java-term:~$ term.setCursorPosition(-5, -10)");
        term.setCursorPosition(-5, -10);
        System.out.println("Cursor X: " + term.getCursorPositionX());
        System.out.println("Cursor Y: " + term.getCursorPositionY());

        System.out.println("\nuser@java-term:~$ term.setCursorPosition(100, 100)");
        term.setCursorPosition(100, 100);
        System.out.println("Cursor X: " + term.getCursorPositionX());
        System.out.println("Cursor Y: " + term.getCursorPositionY());
        System.out.println();
    }

    private static void testScrollbackLimit() {
        System.out.println("Scrollback");
        TerminalBuffer term = new TerminalBuffer(10, 4, 5);

        System.out.println("user@java-term:~$ for i in 1..10: term.writeText(\"Line \" + i)");
        for (int i = 1; i <= 10; i++) {
            term.writeText("Line " + i + (i == 10 ? "" : "\n"));
        }

        System.out.println("user@java-term:~$ term.getScreenScrollBackAsString()");
        System.out.println(term.getScreenScrollBackAsString());
        System.out.println();
    }

    private static void testWideCharacterNormal() {
        System.out.println("Wide Character");
        TerminalBuffer term = new TerminalBuffer(10, 4, 5);

        System.out.println("user@java-term:~$ term.writeText(\"A\uD83D\uDE02B\")");
        term.writeText("A\uD83D\uDE02B");

        System.out.println("Char at (0,0): [" + term.getCharacterAtPosition(0, 0) + "]");
        System.out.println("Char at (0,1): [" + term.getCharacterAtPosition(0, 1) + "]");
        System.out.println("Char at (0,3): [" + term.getCharacterAtPosition(0, 3) + "]");
        System.out.println("Cursor X: " + term.getCursorPositionX());
        System.out.println();
    }

    private static void testWideCharacterEdgeOfScreen() {
        System.out.println("Wide Character Test (edge of screen)");
        TerminalBuffer term = new TerminalBuffer(10, 4, 5);

        System.out.println("user@java-term:~$ term.setCursorPosition(9, 0)");
        term.setCursorPosition(9, 0);

        System.out.println("user@java-term:~$ term.writeText(\"\uD83D\uDE02\")");
        term.writeText("\uD83D\uDE02");

        System.out.println("Char at (0,9): [" + term.getCharacterAtPosition(0, 9) + "]");
        System.out.println("Char at (1,0): [" + term.getCharacterAtPosition(1, 0) + "]");
        System.out.println();
    }

    private static void testResizeReflow() {
        System.out.println("Resizing method");
        TerminalBuffer term = new TerminalBuffer(20, 5, 5);

        System.out.println("user@java-term:~$ term.writeText(\"1234567890ABCDE\")");
        term.writeText("1234567890ABCDE");
        System.out.println("Line 0: " + term.getLineAsString(0).trim());

        System.out.println("\nuser@java-term:~$ term.resize(10, 5)");
        term.resize(10, 5);
        System.out.println("Line 0: " + term.getLineAsString(0).trim());
        System.out.println("Line 1: " + term.getLineAsString(1).trim());

        System.out.println("\nuser@java-term:~$ term.resize(20, 5)");
        term.resize(20, 5);
        System.out.println("Line 0: " + term.getLineAsString(0).trim());
        System.out.println();
    }
}