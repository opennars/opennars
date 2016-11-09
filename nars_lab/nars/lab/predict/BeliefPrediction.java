/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.predict;

import com.google.common.collect.Lists;
import java.util.List;
import nars.util.Events;
import nars.core.NAR;
import nars.entity.Concept;
import nars.inference.AbstractObserver;

/**
 *
 * @author me
 */
public abstract class BeliefPrediction extends AbstractObserver {
    public final List<Concept> concepts;
    public final NAR nar;
    
    /** =1: do each cycle */
    int cyclesPerTrain = 1;
    
    /** =1: do each cycle */
    int cyclesPerPredict = 1;
    
    public BeliefPrediction(NAR n, Concept... concepts) {
        this(n, Lists.newArrayList(concepts));
    }
    
    public BeliefPrediction(NAR n, List<Concept> concepts) {
        super(n, true, Events.CycleEnd.class);
        this.nar = n;
        this.concepts = concepts;
        
    }



    protected abstract void train();

    protected abstract double[] predict();

    @Override
    public void event(Class event, Object[] args) {
        long time = nar.time();
        if (event == Events.CycleEnd.class) {
            if (time % cyclesPerTrain == 0) {
                train();
            }
            if (time % cyclesPerPredict == 0) {
                predict();
            }
        }
    }
    
}
