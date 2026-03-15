package terminal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;

public class TerminalBuffer {
    private int width;
    private int height;
    private int maxScrollBack; // maximum number of lines accepted

    private int cursorX;
    private int cursorY;

    private Color currentBg;
    private Color currentFg;
    private Set<Style> currentStyles;

    private final Deque<Cell[]> scrollBack;

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

        this.scrollBack = new ArrayDeque<>(maxScrollBack);
    }

    public void setCursorPosition(int column, int row) {
        if (column < 0)
            this.cursorX = 0;
        else if (column >= width)
            this.cursorX = width - 1;
        else
            this.cursorX = column;

        if (row < 0)
            this.cursorY = 0;
        else if (row >= height)
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

        insertEmptyLineAtBottomScreen(); // saves the first row in scrollback and inserts new line
    }

    public void writeText(String text) {
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i); // get the character

                if (c == '\n') {
                    cursorX = 0;
                    cursorY++;
                    isEndOfTerminalRow();
                    continue;
                }

                // check if we arrived at the end of the terminal
                isEndOfTerminalColumn();
                isEndOfTerminalRow();

                // set the char in the matrix
                screen[cursorY][cursorX].set(c, currentBg, currentFg, currentStyles);

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

        // i make a gap in the termina for the text
        makeSpace(cursorX, cursorY, size);

        for (int i = 0; i < size; i++) {
            char c = text.charAt(i);
            if (cursorX >= width) {
                cursorX = 0;
                cursorY++;
                isEndOfTerminalRow();
            }
            screen[cursorY][cursorX].set(c, currentBg, currentFg, currentStyles);
        }
    }

    public void makeSpace(int cursorX, int cursorY, int size) {
        for (int x = width - 1; x >= cursorX + size; x--) {
            Cell dst = screen[cursorY][x];
            Cell src = screen[cursorY][x - size];

            dst.set(src.getCharacter(), src.getBg(), src.getFg(), src.getStyles());
        }
    }

    public void insertEmptyLineAtBottomScreen() {
        // if the scrollback dqueue is full, we must remove the first line
        if (scrollBack.size() > (maxScrollBack * width))
            scrollBack.pollFirst();

        // then copy the first line of the terminal in the scrollback
        Cell[] firstRow = new Cell[width];
        for (int i = 0; i < width; i++)
            firstRow[i] = screen[0][i];
        scrollBack.addLast(firstRow);

        for (int i = 1; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Cell dst = screen[i - 1][j];
                Cell src = screen[i][j];
                dst.set(src.getCharacter(), src.getBg(), src.getFg(), src.getStyles());
            }
        }
        int lastRow = height - 1;
        for (int i = 0; i < width; i++) {
            screen[lastRow][i].resetCell();
        }
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                screen[i][j].resetCell();
        cursorX = 0;
        cursorY = 0;
    }

    public void clearScreenScrollBack() {
        clearScreen();
        while (!scrollBack.isEmpty()) {
            scrollBack.pollFirst();
        }
    }

    public char getCharacterAtPosition(int row, int column) {
        int scrollSize;
        if (scrollBack == null)
            scrollSize = 0;
        else
            scrollSize = scrollBack.size();

        // check if we are within the screen
        if (column < 0 || column >= width || row < 0 || row >= scrollSize + height)
            return ' ';

        // check if we are in the scrollback
        if (row < scrollSize) {
            int currentIndex = 0;
            for (Cell[] savedRow : scrollBack) {
                if (currentIndex == row)
                    return savedRow[column].getCharacter();
                currentIndex++;
            }
        } else {
            // check on screen
            int screenRow = row - scrollSize;
            return screen[screenRow][column].getCharacter();
        }
        return ' ';
    }

    public Cell getAttributesAtPosition(int row, int column) {
        int scrollSize;
        if (scrollBack == null)
            scrollSize = 0;
        else
            scrollSize = scrollBack.size();

        if (column < 0 || column >= width || row < 0 || row >= scrollSize + height)
            return new Cell();

        if (row < scrollSize) {
            // check in scrollback
            int currentIndex = 0;
            for (Cell[] savedRow : scrollBack) {
                if (currentIndex == row)
                    return savedRow[column];
                currentIndex++;
            }
        } else {
            // check on screen
            int screenRow = row - scrollSize;
            return screen[screenRow][column];
        }

        return new Cell();
    }

    public String getLineAsString(int row) {
        int scrollSize;
        if (scrollBack == null)
            scrollSize = 0;
        else
            scrollSize = scrollBack.size();

        StringBuilder sb = new StringBuilder();

        if (row < scrollSize) {
            int currentIndex = 0;
            for (Cell[] savedRow : scrollBack) {
                if (currentIndex == row) {
                    for (int x = 0; x < width; x++)
                        sb.append(savedRow[x].getCharacter());
                    return sb.toString();
                }
                currentIndex++;
            }
        } else {
            // else is in the scroll back. the indexing is continuous
            int screenRow = row - scrollSize;
            for (int x = 0; x < width; x++)
                sb.append(screen[screenRow][x].getCharacter());
        }
        return sb.toString();
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            sb.append(getLineAsString(y));

            if (y < height - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    public String getScreenScrollBackAsString() {
        StringBuilder sb = new StringBuilder();

        // first we add the scrollBack then the screen
        if (scrollBack != null) {
            for (Cell[] savedRow : scrollBack) {
                for (int x = 0; x < width; x++)
                    sb.append(savedRow[x].getCharacter());
                sb.append("\n");
            }
        }

        sb.append(getScreenAsString());
        return sb.toString();
    }

}
