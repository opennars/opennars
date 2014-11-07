package nars.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nars.core.Memory;
import nars.entity.Task;
import nars.language.Term;

/**
 * A decision is a set of mutually exclusive actions (each represented as a Term parameter to an Operator).
 * Each invocation of the operator effectively casts a vote towards one of the results.
 * After some time, the decision can be decided and the state reset.
 * Different policies determine which decision is selected.
 * 
 * TODO not complete yet
 */
abstract public class Decision extends Operator {

    //TODO use long not int
    Map<Term, Integer> vote = new HashMap();
    
    Term first, last;
    private DecisionMode mode;
    
    public enum DecisionMode {
        Democratic, Consensus, Random, First, Last
    }   
    
    public Decision(String name, DecisionMode mode) {
        super(name);
        this.mode = mode;
        reset();
    }    
    
    @Override protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        Term x = args[0];        
        vote.put(x, vote.getOrDefault(x, 0)+1);
        if (first == null)
            first = x;
        last = x;
        
        return null;
    }
    
    /** may return null if nothing voted, or if in Consensus mode and no consensus reached */
    public Term getDecision(boolean reset) {
        Term result = null;
        
        switch (mode) {
            case Democratic:
                int most = 0;
                for (final Map.Entry<Term, Integer> e : vote.entrySet()) {
                    int v = e.getValue();
                    if (v > most) {
                        result = e.getKey();
                        most = v;
                    }                        
                }
                break;
            case Consensus:
                throw new RuntimeException("not implemented yet");
                //break;
            case Random:
                throw new RuntimeException("not implemented yet");
                //break;
            case First: result = first; break;
            case Last: result = last; break;            
        }
        
        if (reset)
            reset();
        
        return result;
    }
    
    public void reset() {
        first = last = null;
        vote.clear();        
    }

    public void setMode(DecisionMode mode) {
        this.mode = mode;
    }

    public DecisionMode getMode() {
        return mode;
    }
    
    
}
