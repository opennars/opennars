/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.web;

import nars.NAR;
import nars.io.out.Output;
import nars.io.out.TextOutput;

/**
 * An instance of a web socket session to a NAR
 * @author me
 */
abstract public class NARConnection extends TextOutput {
    public final NAR nar;
    int cycleIntervalMS;
    //private final TextReaction extraParser;
        
    
    public NARConnection(NAR nar, int cycleIntervalMS) {
        super(nar);
        this.nar = nar;
        this.cycleIntervalMS = cycleIntervalMS;
             
    }

    public void read(final String message) {
        nar.input(message);
                
        if (!running)
            resume();
    }


    
    boolean running = false;
    
    public void resume() {
        if (!running) {        
            running = true;
            nar.start(cycleIntervalMS);
        }
    }
    public void stop() {
        running = false;
        nar.stop();
    }
    
    
}
