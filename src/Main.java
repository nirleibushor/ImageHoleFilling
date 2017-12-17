import org.opencv.core.*;

public class Main {

    public static void main( String[] args ) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//        runHoleFilling();

        System.out.println("done");
    }

    private static void runHoleFilling() {
        HoleFiller hf = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        hf.getScaledMat().convertTo(hf.getMat(), CvType.CV_8UC1);
        hf.setImg(Utils.matToImg(hf.getMat()));
        hf.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_input.jpg");

        double sum = 0.0;
        int n = 0;

        int startRow = 100;
        int height = 50;
        int startCol = 50;
        int width = 100;

        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                sum += hf.getScaledMat().get(i,j)[0];
                n++;
            }
        }

        double avr = sum / n;
        System.out.format("before setting mock hole, average of missing pixels = %f%n", avr);

        Index[] missingPixels = MockUtils.getMockSquareHole(new Index(startRow, startCol), height, width);
        MockUtils.setMockHole(hf.getScaledMat(), missingPixels);
        hf.setHole(Utils.getHoleBounderies(hf.getScaledMat()));

        sum = 0.0;
        for (Index idx: hf.getHole().getMissingPixels()) {
            sum += hf.getScaledMat().get(idx.getRow(), idx.getCol())[0];
        }
        n = hf.getHole().getMissingPixels().length;
        avr = sum / n;
        System.out.format("after setting mock hole, average of missing pixels = %f%n", avr);

        HoleFiller hf2 = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        Utils.setVisualBounderies(hf2.getScaledMat(), hf.getHole().getBoundariesPixels(), 0.0);

        hf2.getScaledMat().convertTo(hf2.getMat(), CvType.CV_8UC1);
        hf2.setImg(Utils.matToImg(hf2.getMat()));
        hf2.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_with_boundaries.jpg");

        HoleFiller hf3 = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        MockUtils.setMockHole(hf3.getScaledMat(), missingPixels);
        hf3.setHole(Utils.getHoleBounderies(hf3.getScaledMat()));

//        // Algorithm //
//        double eps = 1e-8;
//        int z = 2;
//        Utils.fillHole(hf3.getScaledMat(), hf3.getHole(), z, eps);

        // Approx. Algorithm //
        Utils.fillHoleCircular(hf3.getScaledMat(), hf3.getHole());

        sum = 0.0;
        for (Index idx: hf3.getHole().getMissingPixels()) {
            sum += hf3.getScaledMat().get(idx.getRow(), idx.getCol())[0];
        }
        avr = sum / (double) n;
        System.out.format("after filling hole, average of missing pixels = %f%n", avr);

        sum = 0.0;
        n = hf3.getHole().getBoundariesPixels().length;
        for (Index idx: hf3.getHole().getBoundariesPixels()) {
            sum += hf3.getScaledMat().get(idx.getRow(), idx.getCol())[0];
        }
        avr = sum / (double) n;
        System.out.format("average of bounderies pixels = %f%n", avr);

        hf3.getScaledMat().convertTo(hf3.getMat(), CvType.CV_8UC1);
        hf3.setImg(Utils.matToImg(hf3.getMat()));
        hf3.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\filled_img.jpg");
    }
}