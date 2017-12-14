import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.HashSet;

import static org.opencv.core.CvType.CV_64FC1;

public class HoleFiller {

    public BufferedImage img;
    public Mat mat;
    public MatOfDouble scaledMat;

    public HoleFiller(String imgPath) {
//        this.loadGrayScaleImg(imgPath);
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
            mat.convertTo(scaledMat, CV_64FC1, 1.0 / 255.0);

            img = dstImg;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void setMockSquareHole() {
        for (int i = 20; i < 120; i++) {
            for (int j = 20; j < 120; j++) {
                mat.put(i, j, 255);
            }
        }
    }

    public void updateImg() {
        byte[] dstData = new byte[mat.rows() * mat.cols() * (int)(mat.elemSize())];
        mat.get(0, 0, dstData);
        BufferedImage dstImg = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        dstImg.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dstData);
        img = dstImg;
    }

    public void writeImg(String path) {
        try {
            File ouptut = new File(path);
            ImageIO.write(img, "jpg", ouptut);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static HashSet<Index> getHoleBounderies(MatOfDouble m) {
        HashSet<Index> idxs = new HashSet<>();
        double HOLE_VALUE = -1.0;

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                int jPrev = (j > 0) ? j - 1 : j;
                int iPrev = (i > 0) ? i - 1 : i;

                if (m.get(i, jPrev)[0] != HOLE_VALUE && m.get(i, j)[0] == HOLE_VALUE) {
                    idxs.add(new Index(i, jPrev));
                } else if (m.get(i, jPrev)[0] == HOLE_VALUE && m.get(i, j)[0] != HOLE_VALUE) {
                    idxs.add(new Index(i, j));
                }
                if (m.get(iPrev, j)[0] != HOLE_VALUE && m.get(i, j)[0] == HOLE_VALUE) {
                    idxs.add(new Index(iPrev, j));
                } else if (m.get(iPrev, j)[0] == HOLE_VALUE && m.get(i, j)[0] != HOLE_VALUE) {
                    idxs.add(new Index(i, j));
                }
            }
        }

        return idxs;
    }

    public Mat getDefaultWeights(Index pixelIdx, Index[] boundaries) {
        double EPSILON = 1e-8;
        MatOfDouble w = new MatOfDouble(boundaries.length, 1, CV_64FC1);
        for (int i = 0; i < boundaries.length; i++){
            double d = HoleFiller.dist(pixelIdx, boundaries[i]);
            d = Math.pow(d, 2) + EPSILON;
            w.put(i, 0, 1.0 / d);
        }
        return w;
    }

    public double getPixelFilling(Index pixelIdx, Index[] boundaries, Mat weights) {
        MatOfDouble boundariesPixels = new MatOfDouble(boundaries.length, 1, CV_64FC1);
        for (int i = 0; i < boundaries.length; i++) {
            boundariesPixels.put(i, 0, scaledMat.get(boundaries[i].row, boundaries[i].col));
        }
        double res = boundariesPixels.dot(weights);
        return res / Core.sumElems(boundariesPixels).val[0];
    }

    public static double dist(Index idx1, Index idx2) {
        int y1 = idx1.row;
        int x1 = idx1.col;
        int y2 = idx2.row;
        int x2 = idx2.col;
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

}