import org.opencv.core.*;

public class Main {

    public static void main( String[] args ) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        runHoleFilling();

        System.out.println("done");
    }

    private static void runHoleFilling() {
        HoleFiller hf = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        hf.scaledMat.convertTo(hf.mat, CvType.CV_8UC1);
        hf.img = hf.matToImg();
        hf.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_input.jpg");

        double sum = 0.0;
        int n = 0;

        int startRow = 100;
        int height = 50;
        int startCol = 50;
        int width = 100;

        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                sum += hf.scaledMat.get(i,j)[0];
                n++;
            }
        }

        double avr = sum / n;
        System.out.format("before setting mock hole, average of missing pixels = %f%n", avr);

        Index[] missingPixels = HoleFiller.getMockSquareHole(new Index(startRow, startCol), height, width);
        HoleFiller.setMockHole(hf.scaledMat, missingPixels);
        hf.hole = HoleFiller.getHoleBounderies(hf.scaledMat);

        sum = 0.0;
        for (Index idx: hf.hole.missingPixels) {
            sum += hf.scaledMat.get(idx.row, idx.col)[0];
        }
        n = hf.hole.missingPixels.length;
        avr = sum / n;
        System.out.format("after setting mock hole, average of missing pixels = %f%n", avr);

        HoleFiller hf2 = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        HoleFiller.setVisualBounderies(hf2.scaledMat, hf.hole.boundaries, 0.0);

        hf2.scaledMat.convertTo(hf2.mat, CvType.CV_8UC1);
        hf2.img = hf2.matToImg();
        hf2.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_with_boundaries.jpg");

        HoleFiller hf3 = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");
        HoleFiller.setMockHole(hf3.scaledMat, missingPixels);
        hf3.hole = HoleFiller.getHoleBounderies(hf3.scaledMat);

//        // un-efficient //
//        double eps = 1e-8;
//        int z = 2;
//        HoleFiller.fillHole(hf3.scaledMat, hf3.hole, z, eps);

        // efficient //
        HoleFiller.fillHoleCircular(hf3.scaledMat, hf3.hole);

        sum = 0.0;
        for (Index idx: hf3.hole.missingPixels) {
            sum += hf3.scaledMat.get(idx.row, idx.col)[0];
        }
        avr = sum / (double) n;
        System.out.format("after filling hole, average of missing pixels = %f%n", avr);

        sum = 0.0;
        n = hf3.hole.boundaries.length;
        for (Index idx: hf3.hole.boundaries) {
            sum += hf3.scaledMat.get(idx.row, idx.col)[0];
        }
        avr = sum / (double) n;
        System.out.format("average of bounderies pixels = %f%n", avr);

        hf3.scaledMat.convertTo(hf3.mat, CvType.CV_8UC1);
        hf3.img = hf3.matToImg();
        hf3.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\filled_img.jpg");
    }
}