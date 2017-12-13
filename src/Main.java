public class Main {

    public static void main( String[] args ) {
        HoleFiller hf = new HoleFiller("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img.jpg");

        hf.setMockSquareHole();
        hf.updateImg();
        hf.writeImg("C:\\Users\\nirle\\IdeaProjects\\LightricksTask\\src\\imgs\\img_gray.jpg");

//        Core.MinMaxLocResult mat = Core.minMaxLoc(hf.mat);
//        Core.MinMaxLocResult scaledMat = Core.minMaxLoc(hf.scaledMat);

//        double m = hf.mat.get(0,0)[0];
//        double scaledM = hf.scaledMat.get(0, 0)[0];
//        System.out.println("m=");
//        System.out.println(m);
//        System.out.println("scaled m=");
//        System.out.println(scaledM);
    }
}