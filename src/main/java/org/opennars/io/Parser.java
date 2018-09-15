package org.opennars.io;

import org.opennars.entity.Task;

/**
 * abstraction for parsing of narsese
 *
 * @author Robert Wünsche
 */
public interface Parser {
    Task parseTask(final String narsese) throws InvalidInputException;


    /**
     * All kinds of invalid addInput lines
     */
    class InvalidInputException extends Exception {
        /**
         * An invalid addInput line.
         * @param s type of error
         */
        InvalidInputException(final String s) {
            super(s);
        }
    }
}
