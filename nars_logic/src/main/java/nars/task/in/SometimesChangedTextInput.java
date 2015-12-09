/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.task.in;

import nars.NAR;
import nars.util.data.random.XORShiftRandom;

/**
 * probability parameter determines the possibility that it re-input
 * a subsequent duplicate input. probability==0 yields same behavior as its superclass
 */
public class SometimesChangedTextInput extends ChangedTextInput {
    private double prob;

    public SometimesChangedTextInput(NAR n, double probability) {
        super(n);
        prob = probability;
    }

    public void setProbability(double prob) {
        this.prob = prob;
    }

    @Override
    public boolean enable() {
        return super.allowRepeats() || (XORShiftRandom.global.nextFloat() < prob);
    }
    
    
    
}
