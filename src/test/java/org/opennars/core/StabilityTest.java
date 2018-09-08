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

import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.opennars.io.events.TextOutputHandler;
import org.opennars.main.Nar;
import org.opennars.main.MiscFlags;
import org.opennars.storage.Memory;
import org.opennars.util.io.ExampleFileInput;
import org.opennars.util.test.OutputCondition;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class StabilityTest {
    static {
        Memory.randomNumber.setSeed(1);
        MiscFlags.DEBUG = false;
        MiscFlags.TEST = true;
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

    public Nar newNAR() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        return new Nar();
        //return Nar.build(Default.fromJSON("nal/build/pei1.fast.nar"));
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }


    @Parameterized.Parameters
    public static Collection params() {
        final String[] directories = new String[] { "/nal/stability/" };

        final Map<String, Object> et = ExampleFileInput.getUnitTests(directories);
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
        runTests(org.opennars.core.NALTest.class);
    }

    public StabilityTest(final String scriptPath) {
        this.scriptPath = scriptPath;

    }

    public double run() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        return testNAL(scriptPath);
    }

    protected double testNAL(final String path) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        Memory.resetStatic();

        final List<OutputCondition> expects = new ArrayList();

        Nar n = null;
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
            System.err.println('\n' + path + " @" + n.time());
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
    public void test() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        testNAL(scriptPath);
    }
}
