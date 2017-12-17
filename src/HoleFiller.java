import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import static org.opencv.core.CvType.CV_64FC1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

public class HoleFiller {

    private String inputImgPath;
    private BufferedImage img;
    private Mat mat;
    private MatOfDouble scaledMat;
    private Hole hole;

    public HoleFiller(String imgPath) { // todo - add boolean scaling arg
        inputImgPath = imgPath;
        this.loadGrayScaleImg(imgPath);
    }

    private void loadGrayScaleImg(String path){
        try {
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

    public void writeImg(String outputPath) {
        try {
            String[] inputPathSplit = inputImgPath.split("\\.");
            String inputType = inputPathSplit[inputPathSplit.length-1];
            File ouptut = new File(outputPath + "." + inputType);
            ImageIO.write(img, inputType, ouptut);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void setImg(BufferedImage bi) {
        img = bi;
    }

    public void setMat(Mat m) {
        mat = m;
    }

    public void setScaledMat(MatOfDouble sm) {
        scaledMat = sm;
    }

    public void setHole(Hole h) {
        hole = h;
    }


    public BufferedImage getImg() {
        return img;
    }

    public Mat getMat() {
        return mat;
    }

    public MatOfDouble getScaledMat() {
        return scaledMat;
    }

    public Hole getHole() {
        return hole;
    }
}