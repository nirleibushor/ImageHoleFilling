/**
 * an exception which is thrown in case path to input image was not given when the program was ran,
 * in this case the program of course aborts. It's used at HoleFillingRunner.parseAgrs()
 */
public class ImagePathException extends Exception {
    public ImagePathException(String msg) {
        super(msg);
    }
}
