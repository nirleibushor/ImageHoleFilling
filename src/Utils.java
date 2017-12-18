import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiFunction;

import static org.opencv.core.CvType.CV_64FC1;

/**
 * a class of utils methods for hole filling
 */
public class Utils {

    /**
     * fills the hole in the given matrix, according to algorithm in section 2
     * @param m matrix to fill
     * @param hole object contains information about boundaries and missing pixels of the hole
     * @param z configurable argument as specifies in the task description
     * @param eps configurable argument as specifies in the task description
     */
    public static void fillHole(MatOfDouble m, Hole hole, int z, double eps) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = getDefaultWeights(idx, hole.getBoundariesPixels(), z, eps);
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    /**
     * fills the hole in the given matrix, according to a given weights function
     * @param m matrix to fill
     * @param hole object contains information about boundaries and missing pixels of the hole
     */
    public static void fillHole(MatOfDouble m, Hole hole, BiFunction<Index, Index[], MatOfDouble> weightFunc) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = weightFunc.apply(idx, hole.getBoundariesPixels());
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    /**
     * approximately fills the hole in the given matrix, according to the requirements in section 5
     * @param m matrix to fill
     * @param hole object contains information about boundaries and missing pixels of the hole
     */
    public static void fillHoleCircular(MatOfDouble m, Hole hole) {
//    public static void fillHoleCircular(MatOfDouble m, Hole hole, double factor) {
        // find a pixel in the out most "circle" of the hole
        Neighborhood neighborhood = checkNeighborhood(m, hole.getBoundariesPixels()[0]);
        Index p = neighborhood.getMissingPixels().length > 0 ? neighborhood.getMissingPixels()[0] : null;
        if (p != null) {
            neighborhood = checkNeighborhood(m, p);
        }

        // circle around the hole and fill
        while (p != null) {
            // get weights based on pixels which aren't missing in the image, and are neighboring to the
            // inspected pixel p, then fill pixel according to the same formula as in section 2
            MatOfDouble w = getDefaultWeights(p, neighborhood.getImgPixels(), Defs.Z, Defs.EPSILON);
//            MatOfDouble w = getCircularWeights(p, neighborhood.getImgPixels(), hole.getBoundariesSet(),
//                    Defs.Z, Defs.EPSILON, factor);

            fillMissingPixel(m, neighborhood.getImgPixels(), p, w);

            // go to next missing pixel, from p's neighboring pixels. If non left, finish
            neighborhood = checkNeighborhood(m, p);
            p = neighborhood.getMissingPixels().length > 0 ? neighborhood.getMissingPixels()[0] : null;
        }
    }

    /**
     * uses the method below - getPixelFilling() and puts the needed value in the missing matrix pixel location
     * @param m the matrix to be manipulated
     * @param rangePositions see function below
     * @param missingIdx location of the current filled missing pixel
     * @param weights the weights function as an OpenCv MatOfDouble object
     */
    private static void fillMissingPixel(MatOfDouble m, Index[] rangePositions, Index missingIdx, MatOfDouble weights) {
        double p = getPixelFilling(m, rangePositions, weights);
        m.put(missingIdx.getRow(), missingIdx.getCol(), p);
    }

    /**
     * fill missing pixels according to the formula in section 2 (but can be used for any w)
     * @param m matrix to be manipulated
     * @param rangePositions the pixels which are considered in the weights function, e.g:
     *                    in the default weight function, these will be all the pixels on the hole's boundaries
     *                    in the circular algorithm, these will be only the non missing pixels from the current
     *                    pixel's neighborhood
     * @param weights the weights function as an OpenCv MatOfDouble object
     */
    public static double getPixelFilling(MatOfDouble m, Index[] rangePositions, MatOfDouble weights) {
        MatOfDouble rangePixels = new MatOfDouble(new Mat(rangePositions.length, 1, CV_64FC1));
        for (int i = 0; i < rangePositions.length; i++) {
            rangePixels.put(i, 0, m.get(rangePositions[i].getRow(), rangePositions[i].getCol()));
        }
        return rangePixels.dot(weights) / Core.sumElems(weights).val[0];
    }

    /**
     * returns the weights function described in section 2 of task description
     * @param pixelIdx location of the missing pixel
     * @param boundaries array of locations of pixels which are in the boundary of the hole
     * @param z configurable argument as specifies in the task description
     * @param eps configurable argument as specifies in the task description
     * @return
     */
    private static MatOfDouble getDefaultWeights(Index pixelIdx, Index[] boundaries, int z, double eps) {
        MatOfDouble w = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++){
            double d = Utils.dist(pixelIdx, boundaries[i]);
            d = Math.pow(d, z) + eps;
            w.put(i, 0, 1.0 / d);
        }
        return w;
    }

    /**
     * a function which expresses the distance between two elements in a 2-dim array, based on euclidean distance
     * @param idx1
     * @param idx2
     * @return
     */
    private static double dist(Index idx1, Index idx2) {
        int y1 = idx1.getRow();
        int x1 = idx1.getCol();
        int y2 = idx2.getRow();
        int x2 = idx2.getCol();
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

//    private static MatOfDouble getCircularWeights(Index missingPixelIdx, Index[] imgPixels,
//                                                  HashSet<Index> boundaries, int z, double eps, double factor) {
//        MatOfDouble w = new MatOfDouble(new Mat(imgPixels.length, 1, CV_64FC1));
//
//        for (int i = 0; i < imgPixels.length; i++){
//            double d = Utils.dist(missingPixelIdx, imgPixels[i]);
//            d = Math.pow(d, z);
//            d = boundaries.contains(imgPixels[i]) ? d * factor : d;
//            d += eps;
//            w.put(i, 0, 1.0 / d);
//        }
//        return w;
//    }

    /**
     * returns a Hole object, based on the missing pixels in the given matrix
     * @param m matrix which is to be inspected for a hole
     * @return Hole object
     */
    public static Hole getHoleBoundaries(Mat m) {
        ArrayList<Index> holeArrayList = new ArrayList<>();
        HashSet<Index> boundariesSet = new HashSet<>(); // set because it's needed to prevent duplicates

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

    /**
     * if the matrix's pixel in the given location is a boundary pixel of a hole (4-connectivity based)
     * it is added to the boundary pixels set
     * @param i row of pixel to be checked
     * @param j col of pixel to be checked
     * @param m matrix to be checked
     * @param boundariesPixels the set to which the pixel will be added in case it's indeed on the boundary
     */
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

    /**
     * if the matrix's pixel in the given location is missing pixel of a hole,
     * it is added to the missing pixels arrayList
     * @param i row of pixel to be checked
     * @param j col of pixel to be checked
     * @param m matrix to be checked
     * @param missingPixels the set to which the pixel will be added in case it's indeed missing
     */
    private static void checkMissingPixel(int i, int j, Mat m, ArrayList<Index> missingPixels) {
        if (m.get(i, j)[0] == Defs.HOLE_VALUE) {
            missingPixels.add(new Index(i, j));
        }
    }

    /**
     * checks the neighboring pixels to the given index in m, and returns a Neighborhood object
     * in which all neighbors divided to missing pixels (in the hole), and existing pixels
     * @param m the matrix to be checked
     * @param idx index of pixel to be checked
     * @return Neighborhood objetc
     */
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
            if (nRow >= 0 && nRow < m.rows() && nCol >= 0 && nCol < m.cols()) {
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

//    private static double matAverage(MatOfDouble m, Index[] idxs) { // todo - delete this
//        double sum = 0.0;
//        for (Index idx: idxs) {
//            sum += m.get(idx.getRow(), idx.getCol())[0];
//        }
//        return sum / idxs.length;
//    }

    /**
     * given anm array of locations of pixels which are in the boundaries of a hole,
     * sets a line in this boundary in the matrix (to visualize the hole's boundaries)
     * @param m matrix to be checked
     * @param boundariesPixels
     * @param color
     */
    public static void setVisualBoundaries(Mat m, Index[] boundariesPixels, double color) {
        for (Index idx: boundariesPixels) {
            m.put(idx.getRow(), idx.getCol(), color);
        }
    }

    /**
     * given a matrix object, given as OpenCv Mat object, returns a BufferredImage object which holds the image
     * @param m the matrix representing the image
     * @return BufferedImage corresponds to the given m
     */
    public static BufferedImage matToImg(Mat m) {
        byte[] dstData = new byte[m.rows() * m.cols() * (int)(m.elemSize())];
        m.get(0, 0, dstData);
        BufferedImage dstImg = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_BYTE_GRAY);
        dstImg.getRaster().setDataElements(0, 0, m.cols(), m.rows(), dstData);
        return dstImg;
    }

}
