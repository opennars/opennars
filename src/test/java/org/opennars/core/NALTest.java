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
package org.opennars.core;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.main.Nar;
import org.opennars.main.Debug;
import org.opennars.util.io.ExampleFileInput;
import org.opennars.util.test.OutputCondition;
import org.opennars.util.test.OutputContainsCondition;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Test for the integrity of the different NAL levels.
 * Tests example, multistep etc.
 */
@RunWith(ParallelizedParameterized.class)
public class NALTest  {
    final int minCycles = 1550; //TODO reduce this to one or zero to avoid wasting any extra time during tests
    static public boolean showOutput = false;
    static public  boolean showSuccess = false;
    static public final boolean showFail = true;
    static public final boolean showReport = true;
    static public final boolean requireSuccess = true;
    static public final int similarsToSave = 5;
    protected static final Map<String, String> examples = new LinkedHashMap<>(); //path -> script data
    public static final Map<String, Boolean> tests = new LinkedHashMap<>();

    // we store a list of scores to keep track of each sample
    public static final Map<String, List<Double>> scores = new LinkedHashMap<>();
    final String scriptPath;

    /** how many times should one test be run (to collect run scores) */
    public static int numberOfSamples = 1;

    // exposed to be able to change it from the outside
    public static String[] directories = new String[] {"/nal/single_step/", "/nal/multi_step/", "/nal/application/"};

    public static double scoreSum = 0.0; // sum of all scores
    public static double scoreSumWithTime = 0.0; // sum of all scores

    public static double qaScoreDecayFactor = 0.001; // how fast does the score decay? - later answers get less score


    public static double timeSum = 0.0;
    public static double bestAnswerConfSum = 0.0;
    public static long samplesCnt = 0;

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
    
    public Nar newNAR() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        return new Nar();
    }
    
    
    @Parameterized.Parameters
    public static Collection params() {
        // return all test-paths of all files in the directories

        final Map<String, Object> et = ExampleFileInput.getUnitTests(directories);
        final Collection t = et.values();
        for (final String x : et.keySet()) addTest(x);
        return t;
    }
    
    
    public static void addTest(String name) {
        name = name.substring(3, name.indexOf(".nal"));
        tests.put(name, true);
    }

    public static void runTests(final Class c) {

        tests.clear();
        scores.clear();

        final Result result = JUnitCore.runClasses(new ParallelComputer(true, true), c);
              
        
        for (final Failure f : result.getFailures()) {
            final String test = f.getMessage().substring(f.getMessage().indexOf("/nal/single_step") + 8, f.getMessage().indexOf(".nal"));
            
            tests.put(test, false);
        }

        /* commented because name.split() is broken for a special case in NalTestMetrics
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
        }
         */
    }


    public NALTest(final String scriptPath) {
        this.scriptPath = scriptPath;
        
    }

    public void testNAL(final String path) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        for (int iSample = 0; iSample < numberOfSamples; iSample++) {
            sample(path);
        }
    }

    public double sample(final String path) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        final String example = getExample(path);

        if (showOutput) {
            System.out.println(example);
            System.out.println();
        }

        Nar n = newNAR();

        final List<OutputCondition> extractedExpects = OutputCondition.getConditions(n, example, similarsToSave);
        final List<OutputCondition> expects = new ArrayList<>(extractedExpects);


        if (showOutput) {
            new TextOutputHandler(n, System.out);
        }

        n.addInputFile(path);
        n.cycles(minCycles);

        if (showOutput) {
            System.err.flush();
            System.out.flush();
        }

        boolean success = expects.size() > 0;
        for (final OutputCondition e: expects) {
            if (!e.succeeded) {
                success = false;
            }
        }

        double score = 0.0;
        double scoreWithTime = 0.0;

        if (success) {
            //long lastSuccess = -1;
            for (final OutputCondition e: expects) {
/*
                if (e.getTrueTime()!=-1) {
                    if (lastSuccess < e.getTrueTime()) {
                        lastSuccess = e.getTrueTime();
                    }
                }*/
                if (e instanceof OutputContainsCondition) {
                    OutputContainsCondition occ = (OutputContainsCondition)e;

                    score += occ.confOfBestAnswer;
                    scoreWithTime += ((Math.exp(-occ.timeOfBestAnswer * qaScoreDecayFactor)) * occ.confOfBestAnswer);

                    // special handling, because occ.timeOfBestAnswer is set to max if no answer was given
                    if (occ.timeOfBestAnswer != 0) { // was answer recorded?
                        timeSum += occ.timeOfBestAnswer;
                    }

                    bestAnswerConfSum += occ.confOfBestAnswer;
                    samplesCnt++;
                }
            }

            //if (lastSuccess!=-1) {
                //score = 1.0 + 1.0 / (1+lastSuccess);
                //score = lastSuccess;

                if (scores.containsKey(path)) {
                    scores.get(path).add(score);
                }
                else {
                    List<Double> scoresList = new ArrayList<>();
                    scoresList.add(score);
                    scores.put(path, scoresList);
                }
            //}
        }
        else {
            if (scores.containsKey(path)) {
                scores.get(path).add(0.0);
            }
            else {
                List<Double> scoresList = new ArrayList<>();
                scoresList.add(0.0);
                scores.put(path, scoresList);
            }
        }

        System.out.println(path + " score = " + score);
        System.out.println(path + " score with time = " + scoreWithTime);
        scoreSum += score; // accumulate score
        scoreSumWithTime += scoreWithTime;
        
        //System.out.println(lastSuccess + " ,  " + path + "   \t   excess cycles=" + (n.time() - lastSuccess) + "   end=" + n.time());

        if ((!success & showFail) || (success && showSuccess)) {
            System.err.println('\n' + path + " @" + n.time());
            for (final OutputCondition e: expects) {
                System.err.println("  " + e);
            }
        }

        if (requireSuccess) {
            assertTrue(path, success);
        }

        return score;  
    }
    
    @Test
    public void test() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        testNAL(scriptPath);
    }

    @AfterClass
    public static void doYourOneTimeTeardown() {
        System.out.println("");
        System.out.println("=======");
        System.out.println("RESULTS");
        System.out.println("=======");
        System.out.println("");

        System.out.println("score sum = "+scoreSum);
        System.out.println("score sum with time = "+scoreSumWithTime);

        System.out.println("---");

        // average time and conf of best answers
        System.out.println("avg best time = " + (timeSum / samplesCnt));
        System.out.println("avg best conf = " + (bestAnswerConfSum / samplesCnt));
    }

    public static void main(final String[] args) {
        runTests(NALTest.class);
    }

    static {
        Debug.DETAILED = false;
        Debug.TEST = true;
    }
}
