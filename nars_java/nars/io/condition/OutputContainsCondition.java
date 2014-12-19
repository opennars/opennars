/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.TextOutput;
import nars.io.Texts;

/**
 *
 * @author me
 */
public class OutputContainsCondition extends OutputCondition {
    
    public static class SimilarOutput {
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
        
        
    }
    
    final String containing;
    public Map<SimilarOutput, Integer> almost = new HashMap();
    final boolean saveSimilar;

    public OutputContainsCondition(NAR nar, String containing, boolean saveSimilar) {
        super(nar);
        this.containing = containing;
        this.saveSimilar = saveSimilar;
    }

    @Override
    public String getFailureReason() {
        String s = "FAIL: No substring match: " + containing;
        if (!almost.isEmpty()) {
            for (String cs : getCandidates(5)) {
                s += "\n\t" + cs;
            }
        }
        return s;
    }

    public List<String> getCandidates(int max) {
        List<String> c = new ArrayList(almost.keySet());
        Collections.sort(c, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(almost.get(a), almost.get(b));
            }
        });
        if (c.size() < max) {
            return c;
        } else {
            return c.subList(0, max);
        }
    }

    public boolean cond(Class channel, Object signal) {
        if ((channel == OUT.class) || (channel == EXE.class)) {
            String o;
            if (signal instanceof Task) {
                //only compare for Sentence string, faster than TextOutput.getOutputString
                //which also does unescaping, etc..
                Sentence s = ((Task) signal).sentence;
                o = s.toString(nar, false).toString();
                if (o.contains(containing)) {
                    if (saveSimilar) {
                        exact.add(o);
                    }
                    return true;
                }
            } else {
                o = TextOutput.getOutputString(channel, signal, false, false, nar).toString();
                
                if (o.contains(containing)) {
                    if (saveSimilar) {
                        exact.add(o);
                    }
                    return true;
                }
            }
            if (saveSimilar) {
                int dist = Texts.levenshteinDistance(o, containing);
                almost.put(new SimilarOutput(o, dist), dist);
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
    
}
