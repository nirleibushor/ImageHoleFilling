public class Index {
    private int row = -1;
    private int col = -1;

    public Index(int r, int c) {
        row = r;
        col = c;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Index)){
            return false;
        } else {
            return ((Index) other).row == row && ((Index) other).col == col;
        }
    }

    @Override
    public int hashCode() {
        return row - col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int r) {
        row = r;
    }

    public void setCol(int c) {
        col = c;
    }
}