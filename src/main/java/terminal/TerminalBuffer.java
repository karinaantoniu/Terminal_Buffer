package terminal;

import java.util.*;

public class TerminalBuffer {
    private int width;
    private int height;
    private int maxScrollBack; // maximum number of lines accepted

    private int cursorX;
    private int cursorY;

    private Color currentBg;
    private Color currentFg;
    private Set<Style> currentStyles;

    private final Deque<TerminalRow> scrollBack;
    private TerminalRow[] screen;
    private List<TerminalObserver> observers = new ArrayList<>();
    private List<String> commandHistory = new ArrayList<>();


    public TerminalBuffer(int w, int h, int maxScroll) {
        this.width = w;
        this.height = h;
        this.maxScrollBack = maxScroll;

        this.cursorX = 0;
        this.cursorY = 0;

        this.currentBg = Color.DEFAULT;
        this.currentFg = Color.DEFAULT;
        this.currentStyles = EnumSet.noneOf(Style.class);

        this.screen = new TerminalRow[height];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.screen[i] = new TerminalRow(width);

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
        if (cursorY >= height) {
            cursorY = height - 1;
            insertEmptyLineAtBottomScreen(); // saves the first row in scrollback and inserts new line
        }
    }

    public void writeText(String text) {
        if (text == null)
            return;

        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String c = new String(Character.toChars(codePoint));

            int charWidth = getCharacterWidth(codePoint);

            if (c.equals("\n")) {
                screen[cursorY].setWrapped(false);
                cursorX = 0;
                cursorY++;
                isEndOfTerminalRow();
                i += charCount;
                continue;
            }

            // check if we are at the end of the row and we need to print a wider character
            if (charWidth == 2 && cursorX == width - 1) {
                // if there is not enough space, put " " on the end of the row and the emoji on the next row
                screen[cursorY].getCell(cursorX).set(" ", currentBg, currentFg, currentStyles);
                screen[cursorY].setWrapped(true);
                cursorX = 0;
                cursorY++;
                isEndOfTerminalRow();
            } else if (cursorX >= width) {
                // otherwise normal incrementation
                screen[cursorY].setWrapped(true);
                cursorX = 0;
                cursorY++;
                isEndOfTerminalRow();
            }

            screen[cursorY].getCell(cursorX).set(c, currentBg, currentFg, currentStyles);
            cursorX++;

            // a wide character has 2 cells
            if (charWidth == 2 && cursorX < width) {
                screen[cursorY].getCell(cursorX).setPlaceholder();
                cursorX++;
            }

            i += charCount;
        }
        notifyObservers();
    }

    public void fillLine(String c) {
        // fill the end of the line with the characters
        if (cursorX != width - 1)
            for (int i = cursorX; i < width; i++)
                screen[cursorY].getCell(i).set(c, currentBg, currentFg, currentStyles);
        else
            for (int i = 0; i < width; i++)
                screen[cursorY].getCell(i).set(c, currentBg, currentFg, currentStyles);
    }

    public void insertLine(String text, int column, int row) {
        // when inserting i need to move the characters to the right to make room for the text
        int size = text.length();

        // i make a gap in the termina for the text
        makeSpace(cursorX, cursorY, size);

        for (int i = 0; i < size; i++) {
            String c = String.valueOf(text.charAt(i));
            if (cursorX >= width) {
                screen[cursorY].setWrapped(true);
                cursorX = 0;
                cursorY++;
                isEndOfTerminalRow();
            }
            screen[cursorY].getCell(cursorX).set(c, currentBg, currentFg, currentStyles);
            cursorX++;
        }
    }

    public void makeSpace(int cursorX, int cursorY, int size) {
        for (int x = width - 1; x >= cursorX + size; x--) {
            Cell dst = screen[cursorY].getCell(x);
            Cell src = screen[cursorY].getCell(x - size);

            dst.set(src.getCharacter(), src.getBg(), src.getFg(), src.getStyles());
        }
    }

    public void insertEmptyLineAtBottomScreen() {
        // if the scrollback dqueue is full, we must remove the first line
        if (scrollBack.size() >= maxScrollBack)
            scrollBack.pollFirst();

        // then copy the first line of the terminal in the scrollback
        TerminalRow firstRow = new TerminalRow(width);
        firstRow.setWrapped(screen[0].isWrapped());
        for (int i = 0; i < width; i++) {
            Cell oldCell = screen[0].getCell(i);
            firstRow.getCell(i).set(oldCell.getCharacter(), oldCell.getBg(), oldCell.getFg(), oldCell.getStyles());
        }
        scrollBack.addLast(firstRow);

        for (int i = 1; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Cell dst = screen[i - 1].getCell(j);
                Cell src = screen[i].getCell(j);
                dst.set(src.getCharacter(), src.getBg(), src.getFg(), src.getStyles());
            }
        }
        int lastRow = height - 1;
        screen[lastRow].setWrapped(false);
        for (int i = 0; i < width; i++) {
            screen[lastRow].getCell(i).resetCell();
        }
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                screen[i].getCell(j).resetCell();
        cursorX = 0;
        cursorY = 0;
        notifyObservers();
    }

    public void clearScreenScrollBack() {
        clearScreen();
        while (!scrollBack.isEmpty()) {
            scrollBack.pollFirst();
        }
    }

    public String getCharacterAtPosition(int row, int column) {
        int scrollSize;
        if (scrollBack == null)
            scrollSize = 0;
        else
            scrollSize = scrollBack.size();

        // check if we are within the screen
        if (column < 0 || column >= width || row < 0 || row >= scrollSize + height)
            return " ";

        // check if we are in the scrollback
        if (row < scrollSize) {
            int currentIndex = 0;
            for (TerminalRow savedRow : scrollBack) {
                if (currentIndex == row)
                    return savedRow.getCell(column).getCharacter();
                currentIndex++;
            }
        } else {
            // check on screen
            int screenRow = row - scrollSize;
            return screen[screenRow].getCell(column).getCharacter();
        }
        return " ";
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
            for (TerminalRow savedRow : scrollBack) {
                if (currentIndex == row)
                    return savedRow.getCell(column);
                currentIndex++;
            }
        } else {
            // check on screen
            int screenRow = row - scrollSize;
            return screen[screenRow].getCell(column);
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
            for (TerminalRow savedRow : scrollBack) {
                if (currentIndex == row) {
                    for (int x = 0; x < width; x++)
                        sb.append(savedRow.getCell(x).getCharacter());
                    return sb.toString();
                }
                currentIndex++;
            }
        } else {
            // else is in the scroll back. the indexing is continuous
            int screenRow = row - scrollSize;
            for (int x = 0; x < width; x++)
                sb.append(screen[screenRow].getCell(x).getCharacter());
        }
        return sb.toString();
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        int scrollSize;
        if (scrollBack == null)
            scrollSize = 0;
        else
            scrollSize = scrollBack.size();

        for (int y = 0; y < height; y++) {
            sb.append(getLineAsString(scrollSize + y));

            if (y < height - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    public String getScreenScrollBackAsString() {
        StringBuilder sb = new StringBuilder();

        // first we add the scrollBack then the screen
        if (scrollBack != null) {
            for (TerminalRow savedRow : scrollBack) {
                for (int x = 0; x < width; x++)
                    sb.append(savedRow.getCell(x).getCharacter());
                sb.append("\n");
            }
        }

        sb.append(getScreenAsString());
        return sb.toString();
    }

    public int getCharacterWidth(int code) {
        if (code >= 0x4E00 && code <= 0x9FFF)
            return 2; // CJK ideographs
        if (code >= 0x1F300 && code <= 0x1F9FF)
            return 2; // emojis

        return 1;
    }

    public void removeEmptyCells(List<Cell> line) {
        // removes all the empty cells from the end of a logic line
        // when we combine 2 lines, we must ignore the empty cells, and not treat them as part of the paragraph
        for (int i = line.size() - 1; i >= 0; i--) {
            String ch = line.get(i).getCharacter();
            if (ch == null || ch.equals(" ") || ch.equals("")) {
                line.remove(i);
            } else {
                break;
            }
        }
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0)
            return;

        // combine all text (scrollback and screen)
        List<TerminalRow> allOldRows = new ArrayList<>();
        if (scrollBack != null)
            allOldRows.addAll(scrollBack);
        for (int i = 0; i < height; i++)
            allOldRows.add(screen[i]);

        // reconstruct paragraphs
        List<List<Cell>> logicalLines = new ArrayList<>();
        List<Cell> currentParagraph = new ArrayList<>();

        for (TerminalRow row : allOldRows) {
            for (int i = 0; i < width; i++) {
                currentParagraph.add(row.getCell(i));
            }

            if (!row.isWrapped()) {
                removeEmptyCells(currentParagraph); // clean spaces
                logicalLines.add(currentParagraph);       // save it
                currentParagraph = new ArrayList<>();     // start a new one
            }
        }
        if (!currentParagraph.isEmpty()) {
            removeEmptyCells(currentParagraph);
            logicalLines.add(currentParagraph);
        }


        // resize the paragraphs
        List<TerminalRow> allNewRows = new ArrayList<>();

        for (List<Cell> paragraph : logicalLines) {
            if (paragraph.isEmpty()) {
                TerminalRow emptyRow = new TerminalRow(newWidth);
                emptyRow.setWrapped(false);
                allNewRows.add(emptyRow);
                continue;
            }

            int index = 0;
            while (index < paragraph.size()) {
                TerminalRow newRow = new TerminalRow(newWidth);
                int charsToTake = Math.min(newWidth, paragraph.size() - index);

                for (int i = 0; i < charsToTake; i++) {
                    Cell oldCell = paragraph.get(index + i);
                    newRow.getCell(i).set(oldCell.getCharacter(), oldCell.getBg(), oldCell.getFg(), oldCell.getStyles());
                }

                index += charsToTake;
                if (index < paragraph.size()) {
                    newRow.setWrapped(true);
                } else {
                    newRow.setWrapped(false);
                }
                allNewRows.add(newRow);
            }
        }

        // remove all filler lines from the end
        while (allNewRows.size() > 1) {
            TerminalRow lastRow = allNewRows.get(allNewRows.size() - 1);
            boolean isEmpty = true;
            for (int i = 0; i < newWidth; i++) {
                String ch = lastRow.getCell(i).getCharacter();
                if (ch != null && !ch.equals(" ") && !ch.equals("")) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                allNewRows.remove(allNewRows.size() - 1);
            } else {
                break;
            }
        }


        // print the new format (also set it in history)
        int totalNewRows = allNewRows.size();

        // last newHeight rows are printed on the screen
        int screenStartIndex = Math.max(0, totalNewRows - newHeight);

        // clear the old history
        scrollBack.clear();
        TerminalRow[] newScreen = new TerminalRow[newHeight];
        for (int i = 0; i < newHeight; i++) {
            newScreen[i] = new TerminalRow(newWidth);
        }

        // add in scrollback the paragraphs
        int scrollStartIndex = Math.max(0, screenStartIndex - maxScrollBack);
        for (int i = scrollStartIndex; i < screenStartIndex; i++) {
            scrollBack.addLast(allNewRows.get(i));
        }

        // also for the screen
        int screenIdx = 0;
        for (int i = screenStartIndex; i < totalNewRows; i++) {
            newScreen[screenIdx] = allNewRows.get(i);
            screenIdx++;
        }

        // move cursor to the end of the screen
        this.cursorX = 0;
        if (totalNewRows < newHeight) {
            this.cursorY = Math.max(0, totalNewRows - 1);
        } else {
            this.cursorY = newHeight - 1;
        }

        this.screen = newScreen;
        this.width = newWidth;
        this.height = newHeight;
        notifyObservers();
    }

    public void addObserver(TerminalObserver observer) {
        this.observers.add(observer);
    }

    private void notifyObservers() {
        for (TerminalObserver obs : observers) {
            obs.onTerminalChanged();
        }
    }

    public void saveCommandToHistory(String command) {
        commandHistory.add(command);
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }

    public void clearCommandHistory() {
        if (commandHistory != null) {
            commandHistory.clear();
        }
    }
}
