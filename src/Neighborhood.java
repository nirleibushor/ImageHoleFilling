public class Neighborhood {
    Index[] missingPixels;
    Index[]imgPixels;

    public Neighborhood(Index[] m, Index[] ip) {
        missingPixels = m;
        imgPixels = ip;
    }
}
