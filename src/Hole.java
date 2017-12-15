public class Hole {
    public Index[] missingPixels;
    public Index[] boundaries;

    public Hole(Index[] m, Index[] b) {
        missingPixels = m;
        boundaries = b;
    }
}
