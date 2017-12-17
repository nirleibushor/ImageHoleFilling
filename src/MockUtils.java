import org.opencv.core.Mat;
import java.util.ArrayList;

public class MockUtils {

    public static void setMockHole(Mat m, Index[] holePixels) {
        for (Index idx: holePixels) {
            m.put(idx.getRow(), idx.getCol(), -1.0);
        }
    }

    public static Index[] getMockSquareHole(Index topLeft, int height, int width) {
        int startRow = topLeft.getRow();
        int startCol = topLeft.getCol();

        ArrayList<Index> idxs = new ArrayList<>();
        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                idxs.add(new Index(i, j));
            }
        }

        return idxs.toArray(new Index[idxs.size()]);
    }
}
