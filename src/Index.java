public class Index {

    public int row = -1;
    public int col = -1;

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
}