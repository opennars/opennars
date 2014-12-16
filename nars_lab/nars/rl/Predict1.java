/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.StackedPercentageChart;
import automenta.vivisect.timeline.TimelineVis;
import java.awt.Color;
import java.util.List;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.output.ConceptsPanel;
import nars.inference.AbstractObserver;
import nars.io.Answered;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.language.Tense;
import nars.language.Term;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.ElmanPattern;
import org.encog.neural.pattern.FeedForwardPattern;

/**
 *
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/predict/sunspot/PredictSunspotElman.java
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/recurrent/elman/ElmanXOR.java
 *
 * @author me
 */
public class Predict1 {

    public static class TimeModel extends AbstractObserver {

        public final List<Concept> concepts;

        public TimeModel(NAR n, List<Concept> concepts) {
            super(n, true, CycleEnd.class);
            this.concepts = concepts;
        }

        @Override
        public void event(Class event, Object[] args) {
            if (event == CycleEnd.class) {

            }
        }

    }

    static BasicNetwork createElmanNetwork() {
        // construct an Elman type network
        ElmanPattern pattern = new ElmanPattern();
        pattern.setActivationFunction(new ActivationSigmoid());
        pattern.setInputNeurons(1);
        pattern.addHiddenLayer(6);
        pattern.setOutputNeurons(1);
        return (BasicNetwork) pattern.generate();
    }

    static BasicNetwork createFeedforwardNetwork() {
        // construct a feedforward type network
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setActivationFunction(new ActivationSigmoid());
        pattern.setInputNeurons(1);
        pattern.addHiddenLayer(6);
        pattern.setOutputNeurons(1);
        return (BasicNetwork) pattern.generate();
    }

    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {

        final BasicNetwork elmanNetwork = createElmanNetwork();

        MLDataSet result = new BasicMLDataSet();
				
        //----
        int duration = 8;
        float freq = 0.125f * 1.0f / duration;
        int thinkInterval = 1;

        NAR n = new NAR(new Default().setInternalExperience(null));
        n.param.duration.set(duration);
        //n.param.duration.setLinear(0.5);
        n.param.conceptBeliefsMax.set(16);
        //n.param.conceptForgetDurations.set(16);

        String xPrevFuncEq0 = "<x_tMin1 --> y0>";
        String xPrevFuncEq1 = "<x_tMin1 --> y1>";
        String xFuncEq0 = "<x_t0 --> y0>";
        String xFuncEq1 = "<x_t0 --> y1>";
        //String xNextFunc = "<x_tPlus1 --> y>";
        n.believe(xPrevFuncEq0, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xPrevFuncEq1, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xFuncEq0, Tense.Present, 0.50f, 0.90f).run(1);
        n.believe(xFuncEq1, Tense.Present, 0.50f, 0.90f).run(1);
        //n.believe("<(&/," + xPrevFunc + ",+1) =/> " + xFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
        //n.believe("<(&/," + xPrevFunc + ",+1," + xFunc + ",+1) =/> " + xNextFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
        //n.believe("<(&/," + xFunc + ",+1) =/> " + xNextNextFunc + ">", Tense.Eternal, 1.00f, 0.90f).run(1);

        Answered a = new Answered() {
            @Override
            public void onSolution(Sentence belief) {
                System.out.println(belief);
            }

            @Override
            public void onChildSolution(Task child, Sentence belief) {
                System.out.println(belief);
            }

        };

        n.ask("<(&/,<x_t0 --> $x>,+1) =/> <x_t0 --> y0>>");
        n.ask("<(&/,<x_t0 --> $y>,+1) =/> <x_t0 --> y1>>");
        n.ask("<x_t0 --> y0>");
        n.ask("<x_t0 --> y1>");

        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);
        //error = new TreeMLData("error", Color.ORANGE, numIterations).setRange(0, discretization);

        TreeMLData elmanPredict = new TreeMLData("elman prediction", Color.WHITE).setRange(0, 1f);
                
        int discretization = 2;
        TreeMLData[] predictions = new TreeMLData[discretization];
        TreeMLData[] predBalance = new TreeMLData[discretization];

        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = new TreeMLData("Pred" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            predictions[i].setDefaultValue(0.0);

            predBalance[i] = new TreeMLData("PredBalance" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            predBalance[i].setDefaultValue(0.0);
        }
        TimelineVis tc = new TimelineVis(
                new LineChart(observed).thickness(16f).height(128),
                new StackedPercentageChart(predBalance).height(16),
                new LineChart(predictions[0]).thickness(16f).height(128),
        
                new LineChart(elmanPredict).thickness(16f).height(128)
        
        );
        //new BarChart(error).height(4)
        new NWindow("_", new PCanvas(tc)).show(800, 800, true);

        new TextOutput(n, System.out) {

            /**
             * only allow future predictions
             */
            protected boolean allowTask(Task t) {
                if (t.sentence.isEternal()) {
                    return false;
                }
                if ((t.sentence.getOccurenceTime() > n.time())) {
                    System.out.print(n.time() + ".." + t.sentence.getOccurenceTime() + ": ");
                    Term term = t.getTerm();
                    int time = (int) t.sentence.getOccurenceTime();
                    float value = Float.NEGATIVE_INFINITY;
                    float expect = 2f * (t.sentence.truth.getFrequency() - 0.5f) * t.sentence.truth.getConfidence();
                    if (term.toString().equals("<x_t0 --> y0>")) {
                        value = 0;
                    }
                    if (term.toString().equals("<x_t0 --> y1>")) {
                        value = 1;
                    }
                    if (value != Double.NEGATIVE_INFINITY) {

                        //predictions[(int)value].addPlus(time, expect);
                        for (int tt = time - duration / 2; tt <= time + duration / 2; tt++) {
                            predictions[0].addPlus(tt, value == 0 ? expect : -expect);

                            if (expect < 0) {
                                value = 1 - value; //add to the other one
                                expect = -expect;
                            }
                            predBalance[(int) value].addPlus(tt, expect);
                        }

                    }

                    return true;
                }
                return false;
            }
        };

        n.run(10);

        Concept x0Prev = n.concept(xPrevFuncEq0);
        Concept x1Prev = n.concept(xPrevFuncEq1);
        Concept x0Now = n.concept(xFuncEq0);
        Concept x1Now = n.concept(xFuncEq1);

        NARSwing.themeInvert();
        new NWindow("x", new ConceptsPanel(n, x0Prev, x1Prev, x0Now, x1Now)).show(900, 600, true);
        //new NARSwing(n);

        float y = 0.5f;
        while (true) {

            MLData inputData = new BasicMLData(1);
            inputData.setData(0,y);            
            MLData predict = elmanNetwork.compute(inputData);
            elmanPredict.add((int)n.time(), predict.getData(0));
            
            n.run(thinkInterval);

            Thread.sleep(5);
            //n.memory.addSimulationTime(1);

            //n.run(thinkCycles/2);
            //y = (float)Math.sin(freq * t) * 0.5f + 0.5f;
            float curY = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;

            observed.add((int) n.time(), curY);

            
            MLData idealData = new BasicMLData(1);            
            idealData.setData(0,curY);
            result.add(inputData,idealData);

            final Train train = new ResilientPropagation(elmanNetwork, result);
            for (int ti = 0; ti < 10; ti++) {
                train.iteration();
            }
            System.out.println("elman error: " + train.getError());
            

            if (curY == y) {
                continue;
            }

            n.believe(xPrevFuncEq0, Tense.Past, 1.0f, y);
            n.believe(xPrevFuncEq0, Tense.Past, 0.0f, 1f - y);
            n.believe(xPrevFuncEq1, Tense.Past, 1.0f, 1f - y);
            n.believe(xPrevFuncEq1, Tense.Past, 0.0f, y);

            y = curY;

            n.believe(xFuncEq0, Tense.Future, 1.0f, y);
            n.believe(xFuncEq0, Tense.Future, 0.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Future, 1.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Future, 0.0f, y);

        }

    }
}
