import org.opencv.core.*;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.Point;


import static org.opencv.core.CvType.CV_64FC1;

public class Main {

    public static void main( String[] args ) {
        HoleFiller hf = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");

//        hf.setMockSquareHole();
//        hf.updateImg();
//        hf.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_gray.jpg");

//        Core.MinMaxLocResult mat = Core.minMaxLoc(hf.mat);
//        Core.MinMaxLocResult scaledMat = Core.minMaxLoc(hf.scaledMat);

//        double m = hf.mat.get(0,0)[0];
//        double scaledM = hf.scaledMat.get(0, 0)[0];
//        System.out.println("m=");
//        System.out.println(m);
//        System.out.println("scaled m=");
//        System.out.println(scaledM);

        Mat m = Mat.zeros(7, 7,CV_64FC1);
        MatOfDouble m2 = new MatOfDouble();
        m.convertTo(m2, CV_64FC1);

        m2.put(2,2,-1.0);
        m2.put(2,3,-1.0);
        m2.put(2,4,-1.0);
        m2.put(3,3,-1.0);
        m2.put(3,4,-1.0);
        m2.put(4,4,-1.0);

        System.out.println(m2.dump());

        Mat bounderies = Mat.zeros(7,7,CV_64FC1);
        HashSet<Index> idxs = HoleFiller.getHoleBounderies(m2);

        for (Index i : idxs) {
            int r = i.row;
            int c = i.col;
            bounderies.put(r, c, 1);
            System.out.format("(%d, %d)%n", r, c);
        }
        System.out.println(bounderies.dump());

        System.out.println("done");

    }
}