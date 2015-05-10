package nars.tuprolog.interfaces;

import nars.tuprolog.PTerm;

public interface IParser {
	
	/**
     * Parses next term from the stream built on string.
     * @param endNeeded <tt>true</tt> if it is required to parse the end token
     * (a period), <tt>false</tt> otherwise.
     * @throws InvalidTermException if a syntax error is found. 
     */
    PTerm nextTerm(boolean endNeeded) throws Exception;
    
    /**
     * @return the current line number
     */
    int getCurrentLine();
    
    /**
     * @return the current offset
     */
    int getCurrentOffset();
    
    /**
     * @return the line correspondent to the current offset
     */
    int[] offsetToRowColumn(int offset);

}
