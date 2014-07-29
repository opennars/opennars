package nars.io;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public interface Output {
    
    public static interface Channel {   }
    
    /** implicitly repeated input (a repetition of all input) */
    public static interface IN extends Channel { }
    
    /** conversational (judgments, questions, etc...) output */
    public static interface OUT extends Channel { }
    
    /** warnings, errors & exceptions */
    public static interface ERR extends Channel { }
    
    /** explicitly repeated input (repetition of the content of input ECHO commands) */
    public static interface ECHO extends Channel { }
    
    /** operation execution */
    public static interface EXE extends Channel { }
    
    
    /**   
     * @param channel classifies this output, specified as a Java class
     * @param signal the content of this output (any type)
     */
    public void output(Class channel, Object signal);

}
