import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiFunction;

import static org.opencv.core.CvType.CV_64FC1;

public class HoleFiller {

    public BufferedImage img;
    public Mat mat;
    public MatOfDouble scaledMat;
    public Hole hole;

    public HoleFiller(String imgPath) { // todo - add boolean scaling arg
        this.loadGrayScaleImg(imgPath);
    }

    public void loadGrayScaleImg(String path){
        try {
            System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
            File input = new File(path);
            BufferedImage srcImg = ImageIO.read(input);

            byte[] srcData = ((DataBufferByte) srcImg.getRaster().getDataBuffer()).getData();
            Mat srcMat = new Mat(srcImg.getHeight(), srcImg.getWidth(), CvType.CV_8UC3);
            srcMat.put(0, 0, srcData);

            Mat dstMat = new Mat(srcImg.getHeight(),srcImg.getWidth(),CvType.CV_8UC1);
            Imgproc.cvtColor(srcMat, dstMat, Imgproc.COLOR_RGB2GRAY);

            byte[] dstData = new byte[dstMat.rows() * dstMat.cols() * (int)(dstMat.elemSize())];
            dstMat.get(0, 0, dstData);
            BufferedImage dstImg = new BufferedImage(dstMat.cols(),dstMat.rows(), BufferedImage.TYPE_BYTE_GRAY);
            dstImg.getRaster().setDataElements(0, 0, dstMat.cols(), dstMat.rows(), dstData);

            mat = dstMat;

            scaledMat = new MatOfDouble();
//            mat.convertTo(scaledMat, CV_64FC1, 1.0 / 255.0);
            mat.convertTo(scaledMat, CV_64FC1);

            img = dstImg;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void setMockHole(Mat m, Index[] holePixels) {
        for (Index idx: holePixels) {
            m.put(idx.row, idx.col, -1.0);
        }
    }

    // todo - add idx out of bounce checks
    public static Index[] getMockSquareHole(Index topLeft, int height, int width) {
        int startRow = topLeft.row;
        int startCol = topLeft.col;

        ArrayList<Index> idxs = new ArrayList<>();
        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                idxs.add(new Index(i, j));
            }
        }

        return idxs.toArray(new Index[idxs.size()]);
    }

    public static void setVisualBounderies(Mat m, Index[] missingPixels, double color) {
        for (Index idx: missingPixels) {
            HoleFiller.colorPixel(m, idx, color);
        }
    }

    private static void colorPixel(Mat m, Index idx, double color) {
        m.put(idx.row, idx.col, color);
    }

    public BufferedImage matToImg() {
        byte[] dstData = new byte[mat.rows() * mat.cols() * (int)(mat.elemSize())];
        mat.get(0, 0, dstData);
        BufferedImage dstImg = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        dstImg.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dstData);
        return dstImg;
    }

    public void writeImg(String path) {
        try {
            File ouptut = new File(path);
            ImageIO.write(img, "jpg", ouptut);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Hole getHoleBounderies(Mat m) {
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

    public static void checkBoundaryPixel(int i, int j, Mat m, HashSet<Index> boundaries) {
        double HOLE_VALUE = -1.0;

        int jPrev = (j > 0) ? j - 1 : j;
        int iPrev = (i > 0) ? i - 1 : i;

        if (m.get(i, jPrev)[0] != HOLE_VALUE && m.get(i, j)[0] == HOLE_VALUE) {
            boundaries.add(new Index(i, jPrev));
        } else if (m.get(i, jPrev)[0] == HOLE_VALUE && m.get(i, j)[0] != HOLE_VALUE) {
            boundaries.add(new Index(i, j));
        }
        if (m.get(iPrev, j)[0] != HOLE_VALUE && m.get(i, j)[0] == HOLE_VALUE) {
            boundaries.add(new Index(iPrev, j));
        } else if (m.get(iPrev, j)[0] == HOLE_VALUE && m.get(i, j)[0] != HOLE_VALUE) {
            boundaries.add(new Index(i, j));
        }
    }

    public static void checkMissingPixel(int i, int j, Mat m, ArrayList<Index> hole) {
        double HOLE_VALUE = -1.0;
        if (m.get(i, j)[0] == HOLE_VALUE) {
            hole.add(new Index(i, j));
        }
    }

    public static double getPixelFilling(MatOfDouble m, Index[] boundaries, MatOfDouble weights) {
        MatOfDouble boundariesPixels = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++) {
            boundariesPixels.put(i, 0, m.get(boundaries[i].row, boundaries[i].col));
        }
        return boundariesPixels.dot(weights) / Core.sumElems(weights).val[0];
    }

    public static void fillHole(MatOfDouble m, Hole hole, int z, double eps) {
        for (Index idx : hole.missingPixels) {
            MatOfDouble w = getDefaultWeights(idx, hole.boundaries, z, eps);
            fillMissingPixel(m, hole, idx, w);
        }
    }

    private static MatOfDouble getDefaultWeights(Index pixelIdx, Index[] boundaries, int z, double eps) {
        MatOfDouble w = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++){
            double d = HoleFiller.dist(pixelIdx, boundaries[i]);
            d = Math.pow(d, z) + eps;
            w.put(i, 0, 1.0 / d);
        }
        return w;
    }

    public static void fillHole(MatOfDouble m, Hole hole, BiFunction<Index, Index[], MatOfDouble> weightFunc) {
        for (Index idx : hole.missingPixels) {
            MatOfDouble w = weightFunc.apply(idx, hole.boundaries);
            fillMissingPixel(m, hole, idx, w);
        }
    }

    private static void fillMissingPixel(MatOfDouble m, Hole hole, Index missingIdx, MatOfDouble weights) {
        double p = getPixelFilling(m, hole.boundaries, weights);
        m.put(missingIdx.row, missingIdx.col, p);
    }

    private static double dist(Index idx1, Index idx2) {
        int y1 = idx1.row;
        int x1 = idx1.col;
        int y2 = idx2.row;
        int x2 = idx2.col;
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

}