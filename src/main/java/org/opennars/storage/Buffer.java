/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennars.storage;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.main.Parameters;


public class Buffer extends Bag<Task<Term>,Sentence<Term>> {
    
    Timable timable;
    Parameters narParameters;
    long max_duration;
    
    public Buffer(Timable timable, int levels, int capacity, Parameters narParameters) {
        super(levels, capacity, narParameters);
        this.timable = timable;
        this.narParameters = narParameters;
    }
    
    @Override
    public boolean expired(long creationTime) {
        long currentTime = timable.time();
        long delta = currentTime - creationTime;
        long maxDuration = narParameters.DURATION * narParameters.MAX_BUFFER_DURATION_FACTOR;
        return delta > maxDuration;
    }
    
    //TODO move event bag inference code over here (sequences, implications, equivalences=
    //remove event bag, use the Buffer instead
}
