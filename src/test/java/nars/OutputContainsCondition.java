/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars;

import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.TextOutput;
import nars.io.Texts;
import nars.operator.Operator.ExecutionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * OutputCondition that watches for a specific String output,
 * while collecting similar results (according to Levenshtein text distance).
 * 
 */
public class OutputContainsCondition extends OutputCondition<Task> {
    
    public List<Task> exact = new ArrayList();
    
    public static class SimilarOutput implements Comparable<SimilarOutput> {
        public final String signal;
        public final int distance;

        public SimilarOutput(String signal, int distance) {
            this.signal = signal;
            this.distance = distance;
        }

        @Override
        public int hashCode() {  return signal.hashCode();        }

        @Override
        public boolean equals(Object obj) { return signal.equals(((SimilarOutput)obj).signal); }

        @Override
        public String toString() {
            return "similar(" + distance + "): " + signal;
        }

        @Override
        public int compareTo(SimilarOutput o) {
            return Integer.compare(distance, o.distance);
        }
        
        
    }
    
    
    
    final String containing;
    public TreeSet<SimilarOutput> almost = new TreeSet();
    final boolean saveSimilar;
    int maxSimilars = 5;

    /** 
     * 
     * @param nar
     * @param containing
     * @param maxSimilars # of similar results to collect, -1 to disable
     */
    public OutputContainsCondition(NAR nar, String containing, int maxSimilars) {
        super(nar);
        this.containing = containing;
        this.maxSimilars = maxSimilars;
        this.saveSimilar = maxSimilars != -1;
    }

    @Override
    public String getFalseReason() {
        String s = "FAIL: No substring match: " + containing;
        if (!almost.isEmpty()) {
            for (SimilarOutput cs : getCandidates(5)) {
                s += "\n\t" + cs;
            }
        }
        return s;
    }

    public Collection<SimilarOutput> getCandidates(int max) {
        return almost;
    }

    public boolean cond(Class channel, Object signal) {
        if ((channel == OUT.class) || (channel == EXE.class)) {
            String o;
            if (signal instanceof Task) {
                //only compare for Sentence string, faster than TextOutput.getOutputString
                //which also does unescaping, etc..
                Task t = (Task)signal;
                Sentence s = t.sentence;
                o = s.toString(nar, false).toString();
                if (o.contains(containing)) {
                    if (saveSimilar) {
                        exact.add(t);
                    }
                    return true;
                }
            } else  {
                Task t = null;
                if (signal instanceof ExecutionResult)
                    t = ((ExecutionResult)signal).getTask();
                
                o = TextOutput.getOutputString(channel, signal, false, false, nar).toString();
                
                if (o.contains(containing)) {
                    if ((saveSimilar) && (t!=null)) {                        
                        exact.add(t);
                    }
                    return true;
                }                
            }
            if (saveSimilar) {
                int dist = Texts.levenshteinDistance(o, containing);
                
                if (almost.size() >= maxSimilars) {
                    SimilarOutput last = almost.last();
                    
                    if (dist < last.distance) {
                        almost.remove(last);
                        almost.add(new SimilarOutput(o, dist));
                    }
                }
                else {
                    almost.add(new SimilarOutput(o, dist));
                }
            }
        }
        /*if (channel == ERR.class) {
            Assert.assertTrue(signal.toString(), false);
        }*/
        return false;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        if (succeeded) {
            return true;
        }
        return cond(channel, signal);
    }

    @Override
    public List<Task> getTrueReasons() {
        return exact;
    }
    
    
}
