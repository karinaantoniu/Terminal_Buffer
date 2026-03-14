package terminal;

import java.util.EnumSet;
import java.util.Set;

public class Cell {
    private char character;

    private Color bg;
    private Color fg;

    // since a char can be both bold and italic we need a set
    // if it were a list, we could have had duplicates
    // by doing so we make sure to remove them
    private Set<Style> style;

    public Cell () {
        this.character = ' ';
        this.bg = Color.DEFAULT;
        this.fg = Color.DEFAULT;
        this.style = EnumSet.noneOf(Style.class);
    }

    public void set(char car, Color c_bg, Color c_fg, Set<Style> st) {
        this.character = car;
        this.bg = c_bg;
        this.fg = c_fg;
        if (st == null || st.isEmpty()) {
            this.style = EnumSet.noneOf(Style.class);
        } else {
            this.style = EnumSet.copyOf(st);
        }
    }

}
