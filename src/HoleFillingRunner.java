import org.opencv.core.Core;
import org.opencv.core.CvType;

import java.io.File;
import java.nio.file.Paths;

public class HoleFillingRunner {

    private static void parseAgrs(String[] args) throws Exception {
        Defs.INPUT_IMG_PATH = args[0].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.INPUT_IMG_PATH : args[0];
        if (Defs.INPUT_IMG_PATH == null) {
            throw new ImagePathException("You must specify image path, e.g: -Dimg=imgs\\img1 (absolute path " +
                    "also permitted), aborting ...");
        }
        Defs.Z = args[1].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.Z : Integer.parseInt(args[1]);
        Defs.EPSILON = args[2].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.EPSILON : Double.parseDouble(args[2]);
        Defs.SCALE_MODE = args[3].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.SCALE_MODE : Boolean.parseBoolean(args[3]);
        Defs.TEST_MODE = args[4].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.TEST_MODE : Boolean.parseBoolean(args[4]);
        Defs.ALG = args[5].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.ALG : Integer.parseInt(args[5]);
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            parseAgrs(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        runHoleFilling();

        System.out.println("Done ...");
    }

    private static void createOutputImgsDir() {
        File dir = new File(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR).toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static void runHoleFilling() {
        HoleFiller hf = new HoleFiller(Defs.INPUT_IMG_PATH);
        hf.getScaledMat().convertTo(hf.getMat(), CvType.CV_8UC1);
        hf.setImg(Utils.matToImg(hf.getMat()));

        createOutputImgsDir();

        hf.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.INPUT_GRAYSCALE_IMG_NAME).toString());

        int startRow = 100;
        int height = 50;
        int startCol = 50;
        int width = 100;

        if (Defs.TEST_MODE) {
            double sum = 0.0;
            int n = 0;
            for (int i = startRow; i < startRow + height; i++) {
                for (int j = startCol; j < startCol + width; j++) {
                    sum += hf.getScaledMat().get(i,j)[0];
                    n++;
                }
            }
            double avr = sum / n;
            System.out.format("before setting mock hole, average of missing pixels = %f%n", avr);
        }

        Index[] missingPixels = MockUtils.getMockSquareHole(new Index(startRow, startCol), height, width);
        MockUtils.setMockHole(hf.getScaledMat(), missingPixels);
        hf.setHole(Utils.getHoleBoundaries(hf.getScaledMat()));

        if (Defs.TEST_MODE) {
            double sum = 0.0;
            for (Index idx: hf.getHole().getMissingPixels()) {
                sum += hf.getScaledMat().get(idx.getRow(), idx.getCol())[0];
            }
            int n = hf.getHole().getMissingPixels().length;
            double avr = sum / n;
            System.out.format("after setting mock hole, average of missing pixels = %f%n", avr);
        }

        HoleFiller hf2 = new HoleFiller(Defs.INPUT_IMG_PATH);
        Utils.setVisualBoundaries(hf2.getScaledMat(), hf.getHole().getBoundariesPixels(), 0.0);

        hf2.getScaledMat().convertTo(hf2.getMat(), CvType.CV_8UC1);
        hf2.setImg(Utils.matToImg(hf2.getMat()));
        hf2.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.BOUNDARY_VIS_IMG_NAME).toString());

        HoleFiller hf3 = new HoleFiller(Defs.INPUT_IMG_PATH);
        MockUtils.setMockHole(hf3.getScaledMat(), missingPixels);
        hf3.setHole(Utils.getHoleBoundaries(hf3.getScaledMat()));

        if (Defs.ALG == 0) {
            Utils.fillHole(hf3.getScaledMat(), hf3.getHole(), Defs.Z, Defs.EPSILON);
        } else if (Defs.ALG == 1) {
            Utils.fillHoleCircular(hf3.getScaledMat(), hf3.getHole());
        }

        if (Defs.TEST_MODE) {
            double sum = 0.0;
            for (Index idx: hf3.getHole().getMissingPixels()) {
                sum += hf3.getScaledMat().get(idx.getRow(), idx.getCol())[0];
            }
            int n = hf3.getHole().getMissingPixels().length;
            double avr = sum / n;
            System.out.format("after filling hole, average of missing pixels = %f%n", avr);

            sum = 0.0;
            for (Index idx: hf3.getHole().getBoundariesPixels()) {
                sum += hf3.getScaledMat().get(idx.getRow(), idx.getCol())[0];
            }
            n = hf3.getHole().getBoundariesPixels().length;
            avr = sum / n;
            System.out.format("average of boundaries pixels = %f%n", avr);
        }

        hf3.getScaledMat().convertTo(hf3.getMat(), CvType.CV_8UC1);
        hf3.setImg(Utils.matToImg(hf3.getMat()));
        hf3.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.FINAL_FILLED_IMG_NAME).toString());
    }
}