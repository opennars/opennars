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
import nars.core.NAR;
import nars.nlp.NLPInputParser;

/**
 * An instance of a web socket session to a NAR
 * @author me
 */
abstract public class NARConnection implements LineOutput {
    public final NAR nar;
    private final ExperienceWriter writer;
    int cycleIntervalMS;
    private final NLPInputParser nlp;
        
    public NARConnection(NAR nar, NLPInputParser nlp, int cycleIntervalMS) {
        this.nar = nar;
        this.nlp = nlp;
        this.cycleIntervalMS = cycleIntervalMS;
     
        
        this.writer = new ExperienceWriter(nar, this);
    }

    public void read(final String message) {
        ExperienceReader e = new ExperienceReader(nar, new BufferedReader( new StringReader(message)), nlp);
                
        if (!running)
            resume();
    }
    
    abstract public void println(String output);
    
    
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
