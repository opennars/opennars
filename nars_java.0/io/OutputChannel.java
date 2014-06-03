/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.io;
import java.util.ArrayList;

/**
 * An interface to be implemented in all output channel
 */
public interface OutputChannel {
    public void nextOutput(ArrayList<String> output);

    /** Update timer and its display */
    public void tickTimer();
}
