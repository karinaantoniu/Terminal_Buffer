package terminal;

import java.util.EnumSet;
import java.util.Set;

public class TerminalBuffer {
    private int width;
    private int height;
    private int maxScrollBack;

    private int cursorX;
    private int cursorY;

    private Color currentBg;
    private Color currentFg;
    private Set<Style> currentStyles;

    private Cell[][] screen;

    public TerminalBuffer(int w, int h, int maxScroll) {
        this.width = w;
        this.height = h;
        this.maxScrollBack = maxScroll;

        this.cursorX = 0;
        this.cursorY = 0;

        this.currentBg = Color.DEFAULT;
        this.currentFg = Color.DEFAULT;
        this.currentStyles = EnumSet.noneOf(Style.class);

        this.screen = new Cell[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.screen[i][j] = new Cell();
    }

    public void setCursorPosition(int column, int row) {
        if (column < 0)
            this.cursorX = 0;
        else if (column < width)
            this.cursorX = width - 1;
        else
            this.cursorX = column;

        if (row < 0)
            this.cursorY = 0;
        else if (row < height)
            this.cursorY = height - 1;
        else
            this.cursorY = row;
    }

    public int getCursorPositionX() {
        return cursorX;
    }

    public int getCursorPositionY() {
        return cursorY;
    }

    public void setAttributes(Color bg, Color fg, Set<Style> styles) {
        this.currentBg = bg;
        this.currentFg = fg;
        this.currentStyles = EnumSet.copyOf(styles);
    }

    public void isEndOfTerminalColumn() {
        if (cursorX >= width) {
            cursorX = 0;
            cursorY++;
        }
    }

    public void isEndOfTerminalRow() {
        if (cursorY >= height)
            cursorY = height - 1;

        // must implement the scroll
    }

    public void writeText(String text) {
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i); // get the character

                // check if we arrived at the end of the terminal
                isEndOfTerminalColumn();
                isEndOfTerminalRow();

                // set the char in the matrix
                screen[cursorX][cursorY].set(c, currentBg, currentFg, currentStyles);

                cursorX++;
            }
        }
    }

    public void fillLine(char c) {
        // fill the end of the line with the characters
        if (cursorX != width - 1)
            for (int i = cursorX; i < width; i++)
                screen[cursorY][i].set(c, currentBg, currentFg, currentStyles);
        else
            for (int i = 0; i < width; i++)
                screen[cursorY][i].set(c, currentBg, currentFg, currentStyles);
    }

    public void insertLine(String text, int column, int row) {
        // when inserting i need to move the characters to the right to make room for the text
        int size = text.length();

        for (int i = cursorX; i >= row; i--) {
            for (int j = cursorY; j >= column; j--) {

            }
        }
    }

    public void insertEmptyLineAtBottomScreen() {}

    public void clearScreen() {}

    public void clearScreenScrollBack() {}

    public void getCharacterAtPosition() {}

    public void getAttributesAtPosition() {}

    public void getLineAsString() {}

    public void getScreenAsString() {}

    public void getScreenScrollBackAsString() {}

}
