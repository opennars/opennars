/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.testutils;

import nars.main.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Narsese;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Term;

/**
 *
 * @author patrick.hammer
 */
public class ConceptMonitor {
    
    public static Term stringToTerm(NAR nar, String s) {
        Narsese narsese = new Narsese(nar.memory);
        Task ret;
        try {
            ret = narsese.parseTask(s + Symbols.JUDGMENT_MARK);
        } catch (Narsese.InvalidInputException ex) {
            return null;
        }
        if(ret == null) {
            return null;
        }
        return ret.getTerm();
    }
    
    public static Concept concept(NAR nar, String s) {
        Term ts = stringToTerm(nar, s);
        if(ts == null) {
            return null;
        }
        return nar.memory.concept(ts);
    }
    
    public static Sentence strongestProjectedInputEventBelief(NAR nar, String st) {
        Concept c = ConceptMonitor.concept(nar, st);
        if(c != null) {
            for(Task t : c.beliefs) {
                if(t.isInput() && !t.sentence.isEternal()) {
                    Sentence s = t.sentence;
                    Sentence projected = s.projection(nar.memory.time(), nar.memory.time());
                    if(!projected.isEternal()) {
                        return projected;
                    }
                }
            }
        }
        return null;
    }
    
    public static Sentence strongestProjectedEternalizedBelief(NAR nar, String st) {
        Concept c = ConceptMonitor.concept(nar, st);
        if(c != null) {
            for(Task t : c.beliefs) {
                Sentence s = t.sentence;
                Sentence projected = s.projection(nar.memory.time(), nar.memory.time());
                return projected;
            }
        }
        return null;
    }
    
    public static Sentence strongestPrecondition(NAR nar, String conc, String statement) {
        Concept c = ConceptMonitor.concept(nar, conc);
        Term st = stringToTerm(nar, statement);
        if(c != null && st != null) {
            for(Task t : c.executable_preconditions) {
                if(CompoundTerm.replaceIntervals(t.getTerm()).equals(
                        CompoundTerm.replaceIntervals(st))) {
                    return t.sentence;
                }
            }
        }
        return null;
    }
    
    public static Sentence strongestPrecondition2(NAR nar, String conc, String statement) { //test to compare with previous
        return strongestProjectedEternalizedBelief(nar, statement);
    }
}
