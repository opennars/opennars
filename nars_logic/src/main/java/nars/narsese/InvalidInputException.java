package nars.narsese;



/**
 * All kinds of invalid addInput lines
 */
public class InvalidInputException extends RuntimeException {

    /**
     * An invalid addInput line.
     * @param s type of error
     */
    InvalidInputException(String s) {
        super(s);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
