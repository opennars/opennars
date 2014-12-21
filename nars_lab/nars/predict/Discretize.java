/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.predict;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.core.NAR;
import nars.core.control.ImmediateProcess;
import nars.entity.Concept;
import nars.entity.Task;
import nars.io.Symbols;
import nars.io.narsese.Narsese;
import nars.language.Instance;
import nars.language.Tense;
import nars.language.Term;

/**
 *
 * @author me
 */
public class Discretize {
    private final NAR nar;
    private final int discretization;
    
    /** levels >=2 */
    public Discretize(NAR n, int levels) {
        this.nar = n;
        this.discretization = levels;
        
    }
    
    public int f(double p) {
        if (p <= 0) {
            return 0;
        }
        if (p >= 1f) {
            return discretization-1;
        }
        int x = (int)Math.round(-0.5 + p * (discretization));
        return x;
    }
    
    public double d(double p) {
        if (p <= 0) {
            return 0;
        }
        if (p >= 1f) {
            return discretization-1;
        }
        double x = (-0.5 + p * (discretization));
        return x;
        
    }    

    /** calculate proportion that value 'v' is at level 'l', or somewhere in between levels */
    public double pDiscrete(double v, int i) {        
        int center = f(v);
        if (i == center) return 1.0;
        return 0.0;
    }

    
    /** calculate proportion that value 'v' is at level 'l', or somewhere in between levels */
    public double pSmooth(double v, int l) {
        double center = d(v);
        double levelsFromCenter = Math.abs(l - center);
        double sharpness = 10.0;
        return 1.0 / (1 + levelsFromCenter/(discretization/2)*sharpness);        }
    
    /** assign 1.0 to the closest discretized level regardless */
    public double pSmoothDiscrete(double v, int l) {
        double center = d(v);
        int centerDisc = f(v);
        if (centerDisc == l) return 1.0; 
        double levelsFromCenter = Math.abs(l - center);
        double sharpness = 10.0;
        return 1.0 / (1 + levelsFromCenter/(discretization/2)*sharpness);
    }
    
    public Term getValueTerm(String prefix, int level) {
        return Instance.make( Term.get(prefix), Term.get("y" + level));       }
    
    public Term[] getValueTerms(String prefix) {
        Term t[] = new Term[discretization];
        for (int i = 0; i < discretization; i++) {
            t[i] = getValueTerm(prefix, i);
        }
        return t;
    }
    public Concept[] getValueConcepts(String prefix) {
        Concept t[] = new Concept[discretization];
        for (int i = 0; i < discretization; i++) {
            t[i] = nar.memory.concept(getValueTerm(prefix, i));
        }
        return t;
    }    
    
    public Term getValueTerm(double y) {
        return Term.get("y" + f((float)y));
    }

    /**
     * 
     * @param variable
     * @param signal
     * @param dt zero = now, + = future cycles, - = past cycles
     */
    void believe(String variable, double signal, int dt) {        
        for (int i = 0; i < discretization; i++) {
            double p = pDiscrete(signal, i);
            believe(variable, i, dt, (float)p, 0.99f, BeliefInsertion.Input);
        }
            
    }
    
    public static enum BeliefInsertion {
        Input, MemoryInput, ImmediateProcess, BeliefInsertion
    }
    
    Map<String,Term> oldTerm=new HashMap<String,Term>();
    //TODO input method: normal input, memory input, immediate process, direct belief insertion    
    void believe(String variable, int level, int dt, float freq, float conf, BeliefInsertion mode) {
                //TODO handle 'dt'

        Term tcur=getValueTerm(variable, level);
        
        if(oldTerm.containsKey(variable) && tcur.equals(oldTerm.get(variable))) {
            return; //it is the same discrete value as before, return!
        }
        
        oldTerm.put(variable, tcur);
        
        if (mode == BeliefInsertion.Input) {
            try {

                nar.believe(tcur.toString(), Tense.Present, freq, conf);
            } catch (Narsese.InvalidInputException ex) {
                Logger.getLogger(Discretize.class.getName()).log(Level.SEVERE, null, ex);
            }
            
          
        }
        else if ((mode == BeliefInsertion.MemoryInput)|| (mode == BeliefInsertion.ImmediateProcess)) {
            Task t = nar.memory.newTask(tcur, Symbols.JUDGMENT_MARK, freq, conf, 1.0f, 0.8f, Tense.Present);
        
            if (mode == BeliefInsertion.MemoryInput)
                nar.memory.inputTask(t);
            else if (mode == BeliefInsertion.ImmediateProcess)
                new ImmediateProcess(nar.memory, t, 0).run();
            
        }
    }            
}