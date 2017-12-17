import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiFunction;

import static org.opencv.core.CvType.CV_64FC1;

public class Utils {

    public static void fillHole(MatOfDouble m, Hole hole, int z, double eps) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = getDefaultWeights(idx, hole.getBoundariesPixels(), z, eps);
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    public static void fillHole(MatOfDouble m, Hole hole, BiFunction<Index, Index[], MatOfDouble> weightFunc) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = weightFunc.apply(idx, hole.getBoundariesPixels());
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    public static void fillHoleCircular(MatOfDouble m, Hole hole) {
        // find a pixel in the out most "circle" of the hole
        Neighborhood neighborhood = checkNeighborhood(m, hole.getBoundariesPixels()[0]);
        Index p = neighborhood.getMissingPixels().length > 0 ? neighborhood.getMissingPixels()[0] : null;

        // circle around hole and fill
        while (p != null) {
            MatOfDouble w = getDefaultWeights(p, neighborhood.getImgPixels(), Defs.Z, Defs.EPSILON);
            fillMissingPixel(m, neighborhood.getImgPixels(), p, w);
            neighborhood = checkNeighborhood(m, p);
            p = neighborhood.getMissingPixels().length > 0 ? neighborhood.getMissingPixels()[0] : null;
        }
    }

    private static MatOfDouble getDefaultWeights(Index pixelIdx, Index[] boundaries, int z, double eps) {
        MatOfDouble w = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++){
            double d = Utils.dist(pixelIdx, boundaries[i]);
            d = Math.pow(d, z) + eps;
            w.put(i, 0, 1.0 / d);
        }
        return w;
    }

    private static double dist(Index idx1, Index idx2) {
        int y1 = idx1.getRow();
        int x1 = idx1.getCol();
        int y2 = idx2.getRow();
        int x2 = idx2.getCol();
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static Hole getHoleBoundaries(Mat m) {
        ArrayList<Index> holeArrayList = new ArrayList<>();
        HashSet<Index> boundariesSet = new HashSet<>();

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                checkMissingPixel(i, j, m, holeArrayList);
                checkBoundaryPixel(i, j, m, boundariesSet);
            }
        }

        Index[] hole = holeArrayList.toArray(new Index[holeArrayList.size()]);
        Index[] boundaries = boundariesSet.toArray(new Index[boundariesSet.size()]);

        return new Hole(hole, boundaries);
    }

    public static void checkBoundaryPixel(int i, int j, Mat m, HashSet<Index> boundariesPixels) {
        int jPrev = (j > 0) ? j - 1 : j;
        int iPrev = (i > 0) ? i - 1 : i;

        if (m.get(i, jPrev)[0] != Defs.HOLE_VALUE && m.get(i, j)[0] == Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, jPrev));
        } else if (m.get(i, jPrev)[0] == Defs.HOLE_VALUE && m.get(i, j)[0] != Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, j));
        }
        if (m.get(iPrev, j)[0] != Defs.HOLE_VALUE && m.get(i, j)[0] == Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(iPrev, j));
        } else if (m.get(iPrev, j)[0] == Defs.HOLE_VALUE && m.get(i, j)[0] != Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, j));
        }
    }

    public static void checkMissingPixel(int i, int j, Mat m, ArrayList<Index> missingPixels) {
        if (m.get(i, j)[0] == Defs.HOLE_VALUE) {
            missingPixels.add(new Index(i, j));
        }
    }

    private static void fillMissingPixel(MatOfDouble m, Index[] boundaries, Index missingIdx, MatOfDouble weights) {
        double p = getPixelFilling(m, boundaries, weights);
        m.put(missingIdx.getRow(), missingIdx.getCol(), p);
    }

    public static double getPixelFilling(MatOfDouble m, Index[] boundaries, MatOfDouble weights) {
        MatOfDouble boundariesPixels = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++) {
            boundariesPixels.put(i, 0, m.get(boundaries[i].getRow(), boundaries[i].getCol()));
        }
        return boundariesPixels.dot(weights) / Core.sumElems(weights).val[0];
    }

    public static void colorPixel(Mat m, Index idx, double color) {
        m.put(idx.getRow(), idx.getCol(), color);
    }

    private static Neighborhood checkNeighborhood(MatOfDouble m, Index idx) {
        int r = idx.getRow();
        int c = idx.getCol();

        ArrayList<Index> nMissing = new ArrayList<>();
        ArrayList<Index> nImg = new ArrayList<>();
        for (int[] neighbor: Defs.clockWise) {
            int rowDir = neighbor[0];
            int colDir = neighbor[1];

            int nRow = r + rowDir;
            int nCol = c + colDir;
            if (nRow > 0 && nRow < m.rows() && nCol > 0 && nCol < m.cols()) {
                Index nIdx = new Index(nRow, nCol);
                double p = m.get(nRow, nCol)[0];
                if (p == Defs.HOLE_VALUE) {
                    nMissing.add(nIdx);
                } else {
                    nImg.add(nIdx);
                }
            }
        }

        Index[] nMissingArray = nMissing.toArray(new Index[nMissing.size()]);
        Index[] nImgArray = nImg.toArray(new Index[nImg.size()]);

        return new Neighborhood(nMissingArray, nImgArray);
    }

    private static double matAverage(MatOfDouble m, Index[] idxs) {
        double sum = 0.0;
        for (Index idx: idxs) {
            sum += m.get(idx.getRow(), idx.getCol())[0];
        }
        return sum / idxs.length;
    }

    public static void setVisualBoundaries(Mat m, Index[] missingPixels, double color) {
        for (Index idx: missingPixels) {
            colorPixel(m, idx, color);
        }
    }

    public static BufferedImage matToImg(Mat m) {
        byte[] dstData = new byte[m.rows() * m.cols() * (int)(m.elemSize())];
        m.get(0, 0, dstData);
        BufferedImage dstImg = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_BYTE_GRAY);
        dstImg.getRaster().setDataElements(0, 0, m.cols(), m.rows(), dstData);
        return dstImg;
    }

}
