/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.util.test;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.main.Nar;
import org.opennars.operator.Operator.ExecutionResult;

import java.util.*;

/**
 * OutputCondition that watches for a specific String output,
 * while collecting similar results (according to Levenshtein text distance).
 * 
 */
public class OutputContainsCondition extends OutputCondition<Task> {
    
    public final List<Task> exact = new ArrayList();
    
    public static class SimilarOutput implements Comparable<SimilarOutput> {
        public final String signal;
        public final int distance;

        public SimilarOutput(final String signal, final int distance) {
            this.signal = signal;
            this.distance = distance;
        }

        @Override
        public int hashCode() {  return signal.hashCode();        }

        @Override
        public boolean equals(final Object obj) { return signal.equals(((SimilarOutput)obj).signal); }

        @Override
        public String toString() {
            return "similar(" + distance + "): " + signal;
        }

        @Override
        public int compareTo(final SimilarOutput o) {
            return Integer.compare(distance, o.distance);
        }
        
        
    }
    
    
    
    final String containing;
    public final NavigableSet<SimilarOutput> almost = new TreeSet();
    final boolean saveSimilar;
    int maxSimilars = 5;

    /** 
     * 
     * @param nar
     * @param containing
     * @param maxSimilars # of similar results to collect, -1 to disable
     */
    public OutputContainsCondition(final Nar nar, final String containing, final int maxSimilars) {
        super(nar);
        this.containing = containing;
        this.maxSimilars = maxSimilars;
        this.saveSimilar = maxSimilars != -1;
    }

    @Override
    public String getFalseReason() {
        String s = "FAIL: No substring match: " + containing;
        if (!almost.isEmpty()) {
            for (final SimilarOutput cs : getCandidates(5)) {
                s += "\n\t" + cs;
            }
        }
        return s;
    }

    public Collection<SimilarOutput> getCandidates(final int max) {
        return almost;
    }
    
        /**
     * @author http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    public static int levenshteinDistance(final CharSequence a, final CharSequence b) {
        final int len0 = a.length() + 1;
        final int len1 = b.length() + 1;
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            final char bj = b.charAt(j - 1);
            for (int i = 1; i < len0; i++) {
                final int match = (a.charAt(i - 1) == bj) ? 0 : 1;
                final int cost_replace = cost[i - 1] + match;
                final int cost_insert = cost[i] + 1;
                final int cost_delete = newcost[i - 1] + 1;
                
                int c = cost_insert;
                if (cost_delete < c) c = cost_delete;
                if (cost_replace < c) c = cost_replace;
                
                newcost[i] = c;
            }
            final int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        return cost[len0 - 1];
    }

    public boolean cond(final Class channel, final Object signal) {
        if ((channel == OUT.class) || (channel == EXE.class)) {
            final String o;
            if (signal instanceof Task) {
                //only compare for Sentence string, faster than TextOutput.getOutputString
                //which also does unescaping, etc..
                final Task t = (Task)signal;
                final Sentence s = t.sentence;
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
                final int dist = levenshteinDistance(o, containing);
                
                if (almost.size() >= maxSimilars) {
                    final SimilarOutput last = almost.last();
                    
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
    public boolean condition(final Class channel, final Object signal) {
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
