/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.opennars.main.NAR;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.operator.Operator.ExecutionResult;

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
    
        /**
     * @author http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    public static int levenshteinDistance(final CharSequence a, final CharSequence b) {
        int len0 = a.length() + 1;
        int len1 = b.length() + 1;
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            final char bj = b.charAt(j - 1);
            for (int i = 1; i < len0; i++) {
                int match = (a.charAt(i - 1) == bj) ? 0 : 1;
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;
                
                int c = cost_insert;
                if (cost_delete < c) c = cost_delete;
                if (cost_replace < c) c = cost_replace;
                
                newcost[i] = c;
            }
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        return cost[len0 - 1];
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
                
                o = TextOutputHandler.getOutputString(channel, signal, false, false, nar).toString();
                
                if (o.contains(containing)) {
                    if ((saveSimilar) && (t!=null)) {                        
                        exact.add(t);
                    }
                    return true;
                }                
            }
            if (saveSimilar) {
                int dist = levenshteinDistance(o, containing);
                
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
