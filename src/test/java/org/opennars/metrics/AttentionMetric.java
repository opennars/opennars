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
package org.opennars.metrics;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.io.Narsese;
import org.opennars.io.events.EventHandler;
import org.opennars.io.events.OutputHandler;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.Operator;
import org.opennars.util.io.ExampleFileInput;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

// TODO< run more tests >

public class AttentionMetric {
    public static String[] directories = new String[] {"/nal/multi_step/", "/nal/application/"};

    public static boolean showOutput = true;

    public static int numberOfSamples = 8;

    public static Random rng = new Random(23+42);

    public static void main(String[] args) {


        final Map<String, Object> et = ExampleFileInput.getUnitTests(directories);

        final Collection t = et.values();

        for(Map.Entry<String, Object> iTest : et.entrySet()) {
            boolean enTest = false;
            if (iTest.getKey().equals("toothbrush2.nal")) {
                enTest = true;
            }

            if (enTest) {
                Object[] paths = (Object[])iTest.getValue();

                double scoreSum = 0.0;
                for(int iSample = 0; iSample < numberOfSamples; iSample++) {
                    scoreSum += runMetricTest((String)paths[0]);
                }
                double averageScore = scoreSum / numberOfSamples;
                System.out.println(iTest.getKey() + "  avg score = "+averageScore);

            }
        }

        int debugHere = 5;
    }

    public static double calcScore(Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime, Parameters narParams) {
        double score = 0.0;

        double exponentialDecayTimeWeightFactor = 0.0003; // how fast does the "score" decay for a solution?

        double weightOfbestSolution = 1.0;
        double weightOfFirstSolution = 3.0;

        // we sum up the solutions, faster solutions with a better time get a better score
        for(Map.Entry<String, ExecOrAnswerByTime> iEntry : execOrQaAnswersByTime.entrySet()) {
            ExecOrAnswerByTime iEntryVal = iEntry.getValue();

            double bestWeight = TruthFunctions.c2w(iEntryVal.bestTruth.getConfidence(), narParams); // we care about weight because it doesn't converge to 1.0 like conf, so we can compute a more meaningful score
            double bestTimeWeight = Math.exp(-iEntry.getValue().bestTime * exponentialDecayTimeWeightFactor); // weight faster answers with a better ranking

            double firstWeight = TruthFunctions.c2w(iEntryVal.firstTruth.getConfidence(), narParams); // we care about weight because it doesn't converge to 1.0 like conf, so we can compute a more meaningful score
            double firstTimeWeight = Math.exp(-iEntry.getValue().firstTime * exponentialDecayTimeWeightFactor); // weight faster answers with a better ranking

            double scoreOfThisEntry = (bestWeight*bestTimeWeight)*weightOfbestSolution + (firstWeight*firstTimeWeight)*weightOfFirstSolution;

            if(true) System.out.println("score of solution " + iEntry.getKey() + " = " + scoreOfThisEntry);

            score += scoreOfThisEntry;
        }

        return score;
    }

    public static double runMetricTest(String name) {
        Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime = new HashMap<>();

        Reasoner n = null;
        try {
            n = new Nar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((Nar)n).memory.randomNumber.setSeed(rng.nextInt(10000)); // start it with another seed

        if (showOutput) {
            //new TextOutputHandler((Nar)n, System.out);

            n.on(OutputHandler.EXE.class, new OutputHandler2((Nar)n, execOrQaAnswersByTime));
        }

        try {
            for (String iLine : readFile(name)) {
                System.out.println(iLine);

                boolean isCommented = iLine.startsWith("'");
                boolean isQuestion = !isCommented && iLine.endsWith("?");
                if (isQuestion) {
                    String question = iLine.substring(0, iLine.length()-1);
                    n.ask(question, new AnswerHandler(n, execOrQaAnswersByTime));
                }
                else {
                    n.addInput(iLine);
                }
            }
        } catch (IOException | Narsese.InvalidInputException e) {
            e.printStackTrace();
        }

        int minCycles = 1000;
        n.cycles(minCycles);

        double scoreOfThisTest = calcScore(execOrQaAnswersByTime, ((Nar)n).narParameters);

        System.out.println("score of "+name+" = "+scoreOfThisTest);

        int here = 5;

        return scoreOfThisTest;
    }

    public static List<String> readFile(String filepath) throws IOException {
        List<String> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                res.add(line);
            }
        }
        return res;
    }

    public static class OutputHandler2 extends EventHandler {
        private final Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime;
        private final Nar nar;

        public OutputHandler2(Nar nar, Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime) {
            super(nar, true);
            this.nar = nar;
            this.execOrQaAnswersByTime = execOrQaAnswersByTime;
        }

        @Override
        public void event(Class event, Object[] args) {
            Operator.ExecutionResult exeResult = (Operator.ExecutionResult)args[0];
            Task task = exeResult.getTask();
            update(execOrQaAnswersByTime, task.sentence, nar);
        }
    }

    private static class AnswerHandler extends org.opennars.io.events.AnswerHandler {
        private final Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime;
        private final Reasoner reasoner;

        public AnswerHandler(Reasoner reasoner, Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime) {
            this.reasoner = reasoner;
            this.execOrQaAnswersByTime = execOrQaAnswersByTime;
        }

        @Override
        public void onSolution(Sentence belief) {
            update(execOrQaAnswersByTime, belief, reasoner);

            int here = 5;
        }
    }

    // TODO< handle truth of answer correctly >
    // updates execOrQaAnswersByTime with the result from the sentence
    public static void update(Map<String, ExecOrAnswerByTime> execOrQaAnswersByTime, Sentence s, Reasoner nar) {
        ExecOrAnswerByTime exec;

        if (execOrQaAnswersByTime.containsKey(s.term.toString())) {
            // was executed before

            exec = execOrQaAnswersByTime.get(s.term.toString());
        }
        else {
            // is first time execution

            exec = new ExecOrAnswerByTime("exec", s.term.toString());
            exec.firstTime = nar.time();
            exec.firstTruth = s.truth.clone();


            execOrQaAnswersByTime.put(s.term.toString(), exec);
        }

        if (exec.bestTruth == null) { // is it the first time?
            exec.bestTime = nar.time(); // the first is the best
            exec.bestTruth = s.truth.clone();
        }
        else if (s.truth.clone().getConfidence() > exec.bestTruth.getConfidence() ) { // is the TV this time better than the recorded one?
            exec.bestTime = nar.time();
            exec.bestTruth = s.truth.clone();
        }
    }


    // used to record the first and best answer or exec of op by time
    private static class ExecOrAnswerByTime {
        public final String narseseTerm;
        public final String type;

        public Long firstTime;
        public TruthValue firstTruth;

        public Long bestTime;
        public TruthValue bestTruth;

        // /param type is the type, "exec" or "q&a"
        // /param narseseTerm term as string
        public ExecOrAnswerByTime(String type, String narseseTerm) {
            this.type = type;
            this.narseseTerm = narseseTerm;
        }
    }
}
