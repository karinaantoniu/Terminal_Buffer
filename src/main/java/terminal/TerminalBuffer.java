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
}
