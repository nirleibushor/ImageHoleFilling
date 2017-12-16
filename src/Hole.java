import java.util.HashSet;

public class Hole {
    public Index[] missingPixels;
    public Index[] boundaries;

    public Hole(Index[] m, Index[] b) {
        missingPixels = m;
        boundaries = b;
    }

    public void seMissingPixels(Index[] m) {
        missingPixels = m;
    }

    public void setBoundaries(Index[] b) {
        boundaries = b;
    }
}
