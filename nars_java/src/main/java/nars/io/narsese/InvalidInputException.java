package nars.io.narsese;

/**
 * All kinds of invalid addInput lines
 */
public class InvalidInputException extends Exception {

    /**
     * An invalid addInput line.
     * @param s type of error
     */
    InvalidInputException(String s) {
        super(s);
    }
}
