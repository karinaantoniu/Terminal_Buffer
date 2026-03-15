package terminal;

public class TerminalRow {
    private Cell[] cells;
    private boolean isWrapped; // to know if the row has been cut by the screen margin, ar \n

    public TerminalRow(int width) {
        this.cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            this.cells[i] = new Cell();
        }
        this.isWrapped = false;
    }

    public Cell[] getCells() {
        return cells;
    }

    public Cell getCell(int index) {
        if (index >= 0 && index < cells.length) {
            return cells[index];
        }
        return new Cell();
    }

    public boolean isWrapped() {
        return isWrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.isWrapped = wrapped;
    }
}