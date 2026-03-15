package terminal;

import java.util.EnumSet;
import java.util.Set;

public class Cell {
    private String character;
    private boolean isPlaceHolder;

    private Color bg;
    private Color fg;

    // since a char can be both bold and italic we need a set
    // if it were a list, we could have had duplicates
    // by doing so we make sure to remove them
    private Set<Style> style;

    public Cell () {
        this.character = " ";
        this.isPlaceHolder = false;
        this.bg = Color.DEFAULT;
        this.fg = Color.DEFAULT;
        this.style = EnumSet.noneOf(Style.class);
    }

    public void set(String car, Color c_bg, Color c_fg, Set<Style> st) {
        this.character = car;
        this.bg = c_bg;
        this.fg = c_fg;
        if (st == null || st.isEmpty()) {
            this.style = EnumSet.noneOf(Style.class);
        } else {
            this.style = EnumSet.copyOf(st);
        }
    }

    public String getCharacter() {
        return character;
    }

    public Color getBg() { return bg; }

    public Color getFg() { return fg; }

    public Set<Style> getStyles() { return style; }

    public void resetCell() {
        this.character = " ";
        this.isPlaceHolder = false;
        this.bg = Color.DEFAULT;
        this.fg = Color.DEFAULT;
        this.style = EnumSet.noneOf(Style.class);
    }

    public void setPlaceholder() {
        this.character = "";
        this.isPlaceHolder = true;
    }
}
