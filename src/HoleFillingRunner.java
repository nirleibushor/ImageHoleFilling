import org.opencv.core.Core;
import org.opencv.core.CvType;

import java.io.File;
import java.nio.file.Paths;

/**
 * runner class for the task's routines
 */
public class HoleFillingRunner {

    /**
     * parsing command line arguments to Defs, e.g input image path, epsilon and z as they are defined in the task, ext.
     * @param args String[] normally args array as it's given from main method
     * @throws Exception in case that there was no input image path given as command line argument
     */
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
        Defs.MOCK_MODE = args[6].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.MOCK_MODE : Boolean.parseBoolean(args[6]);
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            parseAgrs(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        if (Defs.MOCK_MODE) {
            runMockHoleFilling();
        } else {
            runRealHoleFilling();
        }

        System.out.println("Done ...");
    }

    /**
     * creates a directory under the project's base path, called outputImages, in which the images created by the
     * algorithm will be created
     */
    private static void createOutputImgsDir() {
        File dir = new File(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR).toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * runs the routine, which performs the task's requirements
     */
    private static void runMockHoleFilling() {
        // load color rgb image, coverts in to grayscale, and saves it in outputImgs folder
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
            // logs average of pixels in the locations which will hold the mock hole,
            // so would be able to compare to values we get later
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

        // set the mock hole
        Index[] missingPixels = MockUtils.getMockSquareHole(new Index(startRow, startCol), height, width);
        MockUtils.setMockHole(hf.getScaledMat(), missingPixels);
        hf.setHole(Utils.getHoleBoundaries(hf.getScaledMat()));

        if (Defs.TEST_MODE) {
            // logs average of pixels in the locations which were set as the mock hole,
            // now we expect it to be -1.0
            double sum = 0.0;
            for (Index idx: hf.getHole().getMissingPixels()) {
                sum += hf.getScaledMat().get(idx.getRow(), idx.getCol())[0];
            }
            int n = hf.getHole().getMissingPixels().length;
            double avr = sum / n;
            System.out.format("after setting mock hole, average of missing pixels = %f%n", avr);
        }

        // set visualization of out mock hole's boundaries, ad a black line, and output the
        // grayscale image with this visualization
        HoleFiller hf2 = new HoleFiller(Defs.INPUT_IMG_PATH);
        Utils.setVisualBoundaries(hf2.getScaledMat(), hf.getHole().getBoundariesPixels(), 0.0);

        hf2.getScaledMat().convertTo(hf2.getMat(), CvType.CV_8UC1);
        hf2.setImg(Utils.matToImg(hf2.getMat()));
        hf2.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.BOUNDARY_VIS_IMG_NAME).toString());

        // load the input image again, set our mock hole, and fill it according to the chosen algorithm
        HoleFiller hf3 = new HoleFiller(Defs.INPUT_IMG_PATH);
        MockUtils.setMockHole(hf3.getScaledMat(), missingPixels);
        hf3.setHole(Utils.getHoleBoundaries(hf3.getScaledMat()));

        if (Defs.ALG == 0) {
            Utils.fillHole(hf3.getScaledMat(), hf3.getHole(), Defs.Z, Defs.EPSILON);
        } else if (Defs.ALG == 1) {
            Utils.fillHoleCircular(hf3.getScaledMat(), hf3.getHole());
        }

        if (Defs.TEST_MODE) {
            // logs the average of the missing pixels, after they where filled, and the average of the pixels in the
            // boundaries - we expect them to be reasonably close
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

        // output the final image after missing pixels where filled
        hf3.getScaledMat().convertTo(hf3.getMat(), CvType.CV_8UC1);
        hf3.setImg(Utils.matToImg(hf3.getMat()));
        hf3.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.FINAL_FILLED_IMG_NAME).toString());
    }

    private static void runRealHoleFilling() {
        // load color rgb image, coverts in to grayscale, and saves it in outputImgs folder
        HoleFiller hf = new HoleFiller(Defs.INPUT_IMG_PATH);
        hf.getScaledMat().convertTo(hf.getMat(), CvType.CV_8UC1);
        hf.setImg(Utils.matToImg(hf.getMat()));

        createOutputImgsDir();

        hf.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.INPUT_GRAYSCALE_IMG_NAME).toString());

        // find missing pixels, and boundary pixels, of the hole
        Hole hole = Utils.getHoleBoundaries(hf.getScaledMat());
        hf.setHole(hole);

        // fill the hole
        if (Defs.ALG == 0) {
            Utils.fillHole(hf.getScaledMat(), hf.getHole(), Defs.Z, Defs.EPSILON);
        } else if (Defs.ALG == 1) {
            Utils.fillHoleCircular(hf.getScaledMat(), hf.getHole());
        }

        // visualize the hole's boundaries in the final image
        Utils.setVisualBoundaries(hf.getScaledMat(), hf.getHole().getBoundariesPixels(), 0.0);

        // output the final image with filling and boundaries
        hf.getScaledMat().convertTo(hf.getMat(), CvType.CV_8UC1);
        hf.setImg(Utils.matToImg(hf.getMat()));
        hf.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.FINAL_FILLED_IMG_NAME).toString());
    }
}