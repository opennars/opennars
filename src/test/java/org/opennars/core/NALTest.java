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
package org.opennars.core;

import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.main.NAR;
import org.opennars.main.Parameters;
import org.opennars.storage.Memory;
import org.opennars.util.io.ExampleFileInput;
import org.opennars.util.test.OutputCondition;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class NALTest  {
        

    static {
        Memory.randomNumber.setSeed(1);
        Parameters.DEBUG = true;
        Parameters.TEST_RUNNING = true;
    }

    final int minCycles = 1550; //TODO reduce this to one or zero to avoid wasting any extra time during tests
    static public boolean showOutput = false;
    static public boolean saveSimilar = true;
    static public  boolean showSuccess = false;
    static public final boolean showFail = true;
    static public final boolean showReport = true;
    static public final boolean requireSuccess = true;
    static public final int similarsToSave = 5;
    private static final boolean waitForEnterKeyOnStart = false; //useful for running profiler or some other instrumentation
    protected static final Map<String, String> examples = new HashMap(); //path -> script data
    public static final Map<String, Boolean> tests = new HashMap();
    public static final Map<String, Double> scores = new HashMap();
    final String scriptPath;
    
    public static String getExample(final String path) {
        try {
            String existing = examples.get(path);
            if (existing!=null)
                return existing;

            existing = ExampleFileInput.load(path);

            examples.put(path, existing);
            return existing;
        } catch (final IOException e) {
            throw new IllegalStateException("Could not load path", e);
        }
    }
    
    public NAR newNAR() {
        return new NAR();
        //return NAR.build(Default.fromJSON("nal/build/pei1.fast.nar"));        
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }
    
    
    @Parameterized.Parameters
    public static Collection params() {
        final Map<String, Object> et = ExampleFileInput.getUnitTests();
        final Collection t = et.values();
        for (final String x : et.keySet()) addTest(x);
        return t;
    }
    
    
    public static void addTest(String name) {
        name = name.substring(3, name.indexOf(".nal"));
        tests.put(name, true);
    }

    public static double runTests(final Class c) {

        tests.clear();
        scores.clear();
        
        if (waitForEnterKeyOnStart) {
            System.out.println("When ready, press enter");
            try {
                System.in.read();
            } catch (final IOException ex) {
                throw new IllegalStateException("Could not read user input.", ex);
            }
        }
        
        //Result result = org.junit.runner.JUnitCore.runClasses(NALTest.class);
        
        final Result result = JUnitCore.runClasses(new ParallelComputer(true, true), c);
              
        
        for (final Failure f : result.getFailures()) {
            final String test = f.getMessage().substring(f.getMessage().indexOf("/nal/single_step") + 8, f.getMessage().indexOf(".nal"));
            
            tests.put(test, false);
        }
        
        final int[] levelSuccess = new int[10];
        final int[] levelTotals = new int[10];
        
        for (final Map.Entry<String, Boolean> e : tests.entrySet()) {
            final String name = e.getKey();
            int level = 0;
            level = Integer.parseInt(name.split("\\.")[0]);
            levelTotals[level]++;
            if (e.getValue()) {
                levelSuccess[level]++;
            }
        }

        double totalScore = 0;
        for (final Double d : scores.values())
            totalScore += d;
        
        if (showReport) {
            int totalSucceeded = 0, total = 0;
            for (int i = 0; i < 9; i++) {
                final float rate = (levelTotals[i] > 0) ? ((float)levelSuccess[i]) / levelTotals[i] : 0;
                final String prefix = (i > 0) ? ("NAL" + i) : "Other";

                System.out.println(prefix + ": " + (rate*100.0) + "%  (" + levelSuccess[i] + "/" + levelTotals[i] + ")" );
                totalSucceeded += levelSuccess[i];
                total += levelTotals[i];
            }
            System.out.println(totalSucceeded + " / " + total);

            System.out.println("Score: " + totalScore);
        }
        return totalScore;
    }
    
    public static void main(final String[] args) {
        runTests(NALTest.class);
    }

    public NALTest(final String scriptPath) {
        this.scriptPath = scriptPath;
        
    }
    
    public double run() {               
        return testNAL(scriptPath);
    }
    
    protected double testNAL(final String path) {               
        Memory.resetStatic();
        
        final List<OutputCondition> expects = new ArrayList();
        
        NAR n = null;
        final boolean error = false;
        n = newNAR();
        final String example = getExample(path);

        if (showOutput) {
            System.out.println(example);
            System.out.println();
        }

        final List<OutputCondition> extractedExpects = OutputCondition.getConditions(n, example, similarsToSave);
        expects.addAll(extractedExpects);

        if (showOutput)
            new TextOutputHandler(n, System.out);

        n.addInputFile(path);
        n.cycles(minCycles);
      
        System.err.flush();
        System.out.flush();
        
        boolean success = expects.size() > 0 && (!error);
        for (final OutputCondition e: expects) {
            if (!e.succeeded) success = false;
        }

        double score = Double.POSITIVE_INFINITY;
        if (success) {
            long lastSuccess = -1;
            for (final OutputCondition e: expects) {
                if (e.getTrueTime()!=-1) {
                    if (lastSuccess < e.getTrueTime())
                        lastSuccess = e.getTrueTime();
                }                
            }
            if (lastSuccess!=-1) {
                //score = 1.0 + 1.0 / (1+lastSuccess);
                score = lastSuccess;
                scores.put(path, score);
            }
        }
        else {
            scores.put(path, Double.POSITIVE_INFINITY);
        }
        
        //System.out.println(lastSuccess + " ,  " + path + "   \t   excess cycles=" + (n.time() - lastSuccess) + "   end=" + n.time());

        if ((!success & showFail) || (success && showSuccess)) {
            System.err.println('\n' + path + " @" + n.memory.time());
            for (final OutputCondition e: expects) {
                System.err.println("  " + e);
            }
        }
        
        //System.err.println("Status: " + success + " total=" + expects.size() + " " + expects);
        if (requireSuccess)
            assertTrue(path, success);
        
        return score;  
    }
    
    @Test
    public void test() {
        testNAL(scriptPath);
    }
}
