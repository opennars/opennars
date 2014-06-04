/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.web;

import java.io.BufferedReader;
import java.io.StringReader;
import nars.io.ExperienceReader;
import nars.io.ExperienceWriter;
import nars.io.ExperienceWriter.LineOutput;
import nars.main_nogui.NAR;

/**
 * An instance of a web socket session to a NAR
 * @author me
 */
abstract public class NARConnection implements LineOutput {
    public final NAR nar;
    private final ExperienceWriter writer;
    int cycleIntervalMS = 50;
        
    public NARConnection(NAR nar) {
        this.nar = nar;
        
        this.writer = new ExperienceWriter(nar, this);
        nar.addOutputChannel(writer);        
    }

    public void read(final String message) {
        new ExperienceReader(nar, new BufferedReader( new StringReader(message)) );
        
        if (!running)
            resume();
    }
    
    abstract public void println(String output);
    
    
    boolean running = false;
    
    public void resume() {
        if (!running) {        
            running = true;
            nar.start(0);
        }
    }
    public void stop() {
        running = false;
        nar.stop();
    }
    
    /*public void run() {
        while (running) {
            
            //nar.tick();
            
            
            
            if (cycleIntervalMS > 0) {
                try {
                    Thread.sleep(cycleIntervalMS);
                } catch (InterruptedException ex) {            }            
            }
            
            if (nar.isFinishedInputs()) {
                break;
            }
                
        }
        running = false;
        thread = null;
    }*/

    
}
