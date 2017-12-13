import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import static org.opencv.core.CvType.CV_64FC1;

public class HoleFiller {

    public BufferedImage img;
    public Mat mat;
    public MatOfDouble scaledMat;

    public HoleFiller(String imgPath) {
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

}