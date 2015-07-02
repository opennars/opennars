/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.testing.condition;

import nars.Events;
import nars.NAR;
import nars.io.Texts;
import nars.nal.nal8.ExecutionResult;
import nars.task.Sentence;
import nars.task.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * OutputCondition that watches for a specific String output,
 * while collecting similar results (according to Levenshtein text distance).
 * 
 */
public class OutputContainsCondition extends OutputCondition {


    public static class SimilarOutput implements Comparable<SimilarOutput>, Serializable {
        public final String signal;

        /** levenshtein text distance */
        public final int distance;

        public SimilarOutput(String signal, int distance) {
            if (signal == null)
                throw new RuntimeException("null signal");

            this.signal = signal;
            this.distance = distance;
        }

        @Override
        public int hashCode() {  return signal.hashCode();        }

        @Override
        public boolean equals(Object obj) {
            return signal.equals(((SimilarOutput)obj).signal);
        }

        @Override
        public String toString() {
            return "similar(" + distance + "): " + signal;
        }

        @Override
        public int compareTo(SimilarOutput o) {
            int d = Integer.compare(distance, o.distance);
            if (d == 0)
                return signal.compareTo(o.signal);
            return d;
        }
        
        
    }
    

    public static class InputContainsCondition extends OutputContainsCondition {

        public InputContainsCondition(NAR nar, String containing) {
            super(nar, containing, Events.IN.class);
        }

        @Override
        protected boolean validChannel(Class channel) {
            return channel == Events.IN.class;
        }
    }
    
    final String containing;

    public final List<Task> exact = new ArrayList();
    int maxExacts = 4;

    public final TreeSet<SimilarOutput> almost = new TreeSet();
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

    public OutputContainsCondition(NAR nar, String containing, Class... events) {
        super(nar, events);
        this.containing = containing;
        this.maxSimilars = 4;
        this.saveSimilar = true;
    }

    @Override
    public String getFalseReason() {
        StringBuilder s = new StringBuilder(128);
        s.append("FAIL: No substring match: ").append(containing);

        if (!almost.isEmpty()) {
            for (SimilarOutput cs : getCandidates()) {
                s.append("\n\t").append(cs);
            }
        }
        return s.toString();
    }

    public Collection<SimilarOutput> getCandidates() {
        return almost;
    }

    protected boolean validChannel(Class channel) {
        return (channel == Events.OUT.class) || (channel == Events.EXE.class) || (channel == Events.Answer.class);
    }


    public boolean cond(Class channel, Object signal) {

        if (validChannel(channel)) {
            String o;
            if (signal instanceof Task) {
                //only compare for Sentence string, faster than TextOutput.getOutputString
                //which also does unescaping, etc..
                Task t = (Task)signal;
                Sentence s = t.sentence;
                o = toString(s);
                if (o.contains(containing)) {
                    if ((saveSimilar) && (exact.size() < maxExacts)) {
                        exact.add(t);
                    }
                    return true;
                }
            } else  {
                Task t = null;
                if (signal instanceof ExecutionResult)
                    t = ((ExecutionResult)signal).getTask();

                //final StringBuilder buffer = new StringBuilder();

                //o = TextOutput.getOutputString(channel, signal, false, false, nar).toString();
                o = toString(channel, signal);

                if (o.contains(containing)) {
                    if ((saveSimilar) && (t!=null) && (exact.size() < maxExacts)) {
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
        boolean s = cond(channel, signal);
        if (s)
            setTrue();
        return s;
    }

    @Override
    public List<Task> getTrueReasons() {
        return exact;
    }
    
    
}
