/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.predict;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.TimelineVis;
import com.google.common.collect.Lists;
import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.RNNGenerator;
import de.jannlab.tools.NetTools;
import de.jannlab.training.GradientDescent;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.output.ConceptsPanel;
import nars.inference.AbstractObserver;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.language.Instance;
import nars.language.Tense;
import nars.language.Term;

/**
 *
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/predict/sunspot/PredictSunspotElman.java
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/recurrent/elman/ElmanXOR.java
 *
 * @author me
 */
public class Predict3 {

    static double polarize(double v) {
         return 2 * (v - 0.5);
    }
    
    static double unpolarize(double v) {
         v = (v/2) + 0.5;
         if (v < 0) v = 0;
         if (v > 1.0) v = 1.0;
         return v;
    }
    
    
    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {

        int duration = 8;
        float freq = 1.0f / duration * 0.15f;        
        int minCyclesAhead = 0;
        double noise = 0.01;
        boolean onlyNoticeChange = false;
        int thinkInterval = onlyNoticeChange ? 1 : 2;
        int discretization = 4;

        NAR n = new NAR(new Default().setInternalExperience(null));
        n.param.duration.set(duration);
        //n.param.duration.setLinear(0.5);
        n.param.conceptBeliefsMax.set(64);
        //n.param.conceptForgetDurations.set(16);
        
        Discretize discretize = new Discretize(n, discretization);



        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);

        
        TreeMLData[] predictions = new TreeMLData[discretization];
        TreeMLData[] reflections = new TreeMLData[discretization];

        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = new TreeMLData("Pred" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            //predictions[i].setDefaultValue(0.0);
            
            reflections[i] = new TreeMLData("Refl" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            reflections[i].setDefaultValue(0.0);
            //predictions[i].setRange(0, 0.5);
        }
        TimelineVis tc = new TimelineVis(
                new LineChart(observed).thickness(16f).height(128),                          new LineChart(predictions).thickness(16f).height(128),
                new LineChart(reflections).thickness(16f).height(128)
                /*new LineChart(predictions[1]).thickness(16f).height(128),
                new LineChart(predictions[2]).thickness(16f).height(128),*/
        );
        //new BarChart(error).height(4)
        new NWindow("_", new PCanvas(tc)).show(800, 800, true);

        new TextOutput(n, System.out) {

            /** dt = relative to center */
            public double getPredictionEnvelope(double dt, double duration) {
                //guassian curve width=duration
                //  e^(-(4*x/(dur))^2)    
                double p = (4 * dt / duration);
                return Math.exp( -(p * p) );
            }
            
            /**
             * only allow future predictions
             */
            protected boolean allowTask(Task t) {
                if (t.sentence.isEternal()) {
                    return false;
                }
                
                boolean future = false;
                if ((t.sentence.getOccurenceTime() > n.time() + minCyclesAhead)) {
                    System.out.print(n.time() + ".." + t.sentence.getOccurenceTime() + ": ");
                    future = true;
                }
                
                    Term term = t.getTerm();
                    int time = (int) t.sentence.getOccurenceTime();
                    int value = -1;
                    float conf = t.sentence.truth.getConfidence();
                    float expect = 2f * (t.sentence.truth.getFrequency() - 0.5f) * conf;
                    String ts = term.toString();
                    if (ts.startsWith("<x_t0 --> y")) {
                        char cc = ts.charAt("<x_t0 --> y".length());
                                
                        value = cc - '0';
                        
                    }
                    
                    
                    if (value != -1) {

                        //predictions[(int)value].addPlus(time, expect);
                        for (int tt = time - duration / 2; tt <= time + duration / 2; tt++) {
 
                            double smooth = 1;
                            expect *= getPredictionEnvelope(time-tt, smooth * duration*2f);

                            /*
                            if (future)
                                predictions[value].addPlus(tt, expect);
                            else
                                reflections[value].addPlus(tt, expect);
                            */

                        }

                    }


                    return true;

            }
        };

        for (Term t : discretize.getValueTerms("x"))
            n.believe(t.toString(), Tense.Present, 0.5f, 0.5f);
        
        //TODO move this to discretize.getDisjunctionBelief
        n.believe("<(||,y0,y1,y2,y3,y4,y5,y6,y7) --> y>", Tense.Eternal, 1.0f, 0.95f);
        
        n.run(discretization*4);

        //new TextOutput(n, System.out);
        
        Concept[] valueBeliefConcepts = discretize.getValueConcepts("x");
        
        NARSwing.themeInvert();
        new NWindow("x", new ConceptsPanel(n, valueBeliefConcepts)).show(900, 600, true);
        
        RNNBeliefPrediction predictor = new RNNBeliefPrediction(n, valueBeliefConcepts) {

            @Override
            protected void predict() {
                super.predict();
                double[] x = predictedOutput;
                if (x == null) return;
                long t = n.time();
                
                for (int i = 0; i < predictions.length; i++) {
                
                    predictions[i].add((int)t, x[i] ); //- x[i*2+1]);
                }
            }
          
            
        };
        
        //new NARSwing(n);

        int prevY = -1, curY = -1;
        long prevT = n.time();
        while (true) {


            n.run(thinkInterval);

            Thread.sleep(3);
            //n.memory.addSimulationTime(1);

            float signal  = (float)Math.sin(freq * n.time()) * 0.5f + 0.5f;
            //float signal = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;

            if (Math.random() > noise)
                observed.add((int) n.time(), signal);

            
            prevY = curY;
            curY = discretize.f(signal);

            if ((curY == prevY) && (onlyNoticeChange)) {
                continue;
            }


            discretize.believe("x", signal, 0);
            
            //input(prevT, Term.get("x_t0"), prevY, Term.get("x_t0"), curY, 1f, n.memory); 
            //input(prevT, Term.get("x_t0"), prevY, Term.get("x_t0"), 1f-curY, 0.5f, n.memory);             

            
            //n.addInput("notice(x_t0," + value + ",(||,y0,y1))!");
            
            //input(prevT, Term.get("x_tMin1"), prevY, Term.get("x_t0"), 1f-y, 0f, n.memory); 
            
            //input(Term.get("x_t0"), Term.get(value), 0.0f, 0.0f, n.memory);              //input(Term.get("x_t0"), Term.get(otherValue), 1.0f, 0f, n.memory);  */
            /*
            n.believe(xFuncEq0, Tense.Present, 1.0f, y);
            n.believe(xFuncEq0, Tense.Present, 0.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Present, 1.0f, 1f - y);
            n.believe(xFuncEq1, Tense.Present, 0.0f, y);
                    */

            prevT = n.time();
        }

    }
}
