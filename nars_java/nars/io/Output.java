/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.io;

/**
 * An interface to be implemented in all output channel
 */
public interface Output {
    
    public static interface Channel {   }
    public static interface IN extends Channel { }
    public static interface OUT extends Channel { }
    public static interface ERR extends Channel { }
    public static interface ECHO extends Channel { }
        
    /**
     * 
     */
    public void output(Class channel, Object o);

}
