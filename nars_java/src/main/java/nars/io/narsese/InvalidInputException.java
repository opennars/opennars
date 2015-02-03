package nars.io.narsese;

import org.parboiled.errors.ParserRuntimeException;

/**
 * All kinds of invalid addInput lines
 */
public class InvalidInputException extends ParserRuntimeException {

    /**
     * An invalid addInput line.
     * @param s type of error
     */
    InvalidInputException(String s) {
        super(s);
    }

    public InvalidInputException(String message, Exception cause) {
        super(cause, message);
    }
}
