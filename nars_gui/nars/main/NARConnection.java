/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.main;

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
    private final ExperienceReader reader;
    private final ExperienceWriter writer;

    public NARConnection(NAR nar) {
        this.nar = nar;
        
        this.reader = new ExperienceReader(nar);
        
        
        this.writer = new ExperienceWriter(nar, this);

        nar.addOutputChannel(writer);
    }

    public void read(final String message) {
        System.out.println("READ: " + message);                
        this.reader.parse(message);        
    }
    
    abstract public void println(String output);
    
    
    
}
