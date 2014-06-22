/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.io;

/**
 * An interface to be implemented in all output channel
 */
public interface Output {
    
    public static enum Channel {
        IN("IN"),
        OUT("OUT"),
        ERR("ERR"), 
        ECHO("ECHO");
        
        public final String id;
        
        Channel(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
                
    };
    
    /**
     * 
     * @param sentence  sentence object
     * @param input     true if the sentence originated from input, false if NARS generated for output
     */
    public void output(Channel c, Object o);


}
