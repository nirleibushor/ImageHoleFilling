public class Defs {

    public final static int[][] clockWise = new int[][] {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
            {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    public static double EPSILON = 1e-8;
    public static int Z = 4;
    public static boolean SCALE_MODE = false;
    public static boolean TEST_MODE = false;
    public static int ALG = 0;

    public static String PROJECT_PATH = System.getProperty("user.dir");
    public static String OUTPUT_IMGS_DIR = "\\outputImgs";
    public static String IMG_PATH;


    public final static double HOLE_VALUE = -1.0;

    public final static String CMD_LINE_ARG_DEF = "[default]";

}
