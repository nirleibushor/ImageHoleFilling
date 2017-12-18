import java.util.HashSet;

/**
 * an object which represents a hole in an image
 */
public class Hole {
    private Index[] missingPixels;
    private Index[] boundariesPixels;
    private HashSet<Index> boundariesSet;

    public Hole(Index[] m, Index[] b) {
        missingPixels = m;
        boundariesPixels = b;
    }

    public Index[] getBoundariesPixels() {
        return boundariesPixels;
    }

    public Index[] getMissingPixels() {
        return missingPixels;
    }

    public HashSet<Index> getBoundariesSet() {
        return boundariesSet;
    }

    public void setMissingPixels(Index[] m) {
        missingPixels = m;
    }

    public void setBoundariesPixels(Index[] b) {
        boundariesPixels = b;
    }

    public void setBoundariesSet(HashSet<Index> bs) {
        boundariesSet = bs;
    }
}
