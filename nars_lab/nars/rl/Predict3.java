/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl;

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

    /** predicts the beliefs of a set of concepts */
    public static class BeliefPrediction extends AbstractObserver {

        final Random rnd = new Random();
        
        public final List<Concept> concepts;
        int cyclesPerTrain = 1;
        int cyclesPerPredict = 1;
        private final NAR nar;
        private final Net net;
        private SampleSet data;
        int maxDataFrames = 128; //time entries
        private final double[] noActivity;
        double previousValueDecayRate = 0.9;
        protected double[] predictedOutput;
        private GradientDescent trainer;
        private final int frameSize;

        public BeliefPrediction(NAR n, Concept... concepts) {
            this(n, Lists.newArrayList(concepts));            
        }

        public BeliefPrediction(NAR n, List<Concept> concepts) {
            super(n, true, CycleEnd.class);
            this.concepts = concepts;
            this.nar = n;
            
            this.frameSize = concepts.size();
            
            //https://github.com/JANNLab/JANNLab/blob/master/examples/de/jannlab/examples/recurrent/AddingExample.java
            /*LSTMGenerator gen = new LSTMGenerator();
            gen.inputLayer(frameSize);
            gen.hiddenLayer(
                concepts.size()*4, 
                CellType.SIGMOID, CellType.TANH, CellType.TANH, false
            );            
            gen.outputLayer(frameSize,  CellType.TANH);
            */
            RNNGenerator gen = new RNNGenerator();
            gen.inputLayer(frameSize);
            gen.hiddenLayer(concepts.size()*8, CellType.TANH);
            gen.hiddenLayer(concepts.size()*8, CellType.TANH);
            gen.outputLayer(frameSize, CellType.TANH);
        
            
            
            
            //leave as zeros
            noActivity = new double[frameSize];
            
            net = gen.generate();
            net.rebuffer(maxDataFrames);   
            net.initializeWeights(rnd);

            
        }

        protected void train() {
            //
            //double[] target = {((data[x(i1)] + data[x(i2)])/2.0)};
            //new Sample(data, target, 2, length, 1, 1);

            
            TreeMap<Integer,double[]> d = new TreeMap();
            
            int cc = 0;
            for (Concept c : concepts) {
                for (Sentence s : c.beliefs) {
                    if (s.isEternal()) continue;
                    
                    int o = (int)s.getOccurenceTime();
                    if (o > nar.time()) continue; //non-future beliefs
                    
                    int hd = nar.memory.getDuration()/2;
                    for (int oc = o - hd; oc <= o + hd; oc++) {

                        double[] x = d.get(oc);
                        if (x == null) {
                            x = new double[frameSize];
                            d.put(oc, x);
                        }
                        float freq = 2f * (s.truth.getFrequency() - 0.5f);

                        float conf = s.truth.getConfidence();

                        if (freq < 0) {
                            //negative
                            //freq = -freq;
                            //ic++;
                        }
                        x[cc] += freq * conf;
                    }
                }
                cc++;

            }

            
            if (d.size() > 2) {
                
                data = new SampleSet();            
                int first = d.firstKey();
                int last = (int)nar.time();
                if (last - first > maxDataFrames)
                    first = last - maxDataFrames;

                int frames = (int)(last - first);
                int bsize = frameSize * frames;
                
                
                double[] actual = new double[bsize];
                double[] ideal = new double[bsize];
                int ac = 0, id = 0;
                
                double[] prevX = null;
                for (int i = first; i <= last; i++) {
                    
                    double[] x = d.get(i);
                    if (x == null) x = new double[frameSize];
 
                    if (prevX!=null) {
                        System.arraycopy(prevX, 0, ideal, id, frameSize);
                        System.arraycopy(x, 0, actual, ac, frameSize);
                        ac += frameSize;
                        id += frameSize;
                    }
                    
                    prevX = x;
                }
                
                Sample s = new Sample(actual /* actual */, ideal /*ideal */, frameSize, frameSize );
                data.add(s);
            }
            else {
                data = null;
                return;
            }
            
            System.out.println(data);

            /*
            final int maxlength = data.maxSequenceLength();
            net.rebuffer(maxDataFrames);   
            */
            
            final int epochs = 32;
            final double learningrate = 0.02;
            final double momentum     = 0.9;
            
            if (trainer ==null) {
                trainer = new GradientDescent();
                trainer.setNet(net);
                trainer.setRnd(rnd);
                trainer.setPermute(false);
                trainer.setTrainingSet(data);
                trainer.setLearningRate(learningrate);
                trainer.setMomentum(momentum);
                trainer.setEpochs(epochs);            
                trainer.clearListener();
                
            }
            else {
                //trainer.reset();
            }

            trainer.train();          
            
            //System.out.println("LSTM error: " + trainer.getTrainingError());
            
        }
        
        protected void predict() {

            if (data == null) return;
            
            if (predictedOutput == null)
                predictedOutput = new double[frameSize];
            
            
            Sample lastSample = data.get(data.size()-1);
            double error = NetTools.performForward(this.net, lastSample);            
            net.output(predictedOutput, 0);
            
            System.out.println("output: " + Arrays.toString(predictedOutput) + " " + error);
                        
        }
        
        @Override
        public void event(Class event, Object[] args) {
            long time = nar.time();
            if (event == CycleEnd.class) {
                if (time % cyclesPerTrain == 0) {
                    train();
                }
                if (time % cyclesPerPredict == 0) {
                    predict();
                }
            }
        }

    }


    static double[] trainingHistory;



    static double polarize(double v) {
         return 2 * (v - 0.5);
    }
    static double unpolarize(double v) {
         v = (v/2) + 0.5;
         if (v < 0) v = 0;
         if (v > 1.0) v = 1.0;
         return v;
    }
    
    
    static int discretization = 4;
    
    public static int f(float p) {
        if (p < 0) {
            p = 0;
        }
        if (p > 1f) {
            p = 1f;
        }
        int l = (int) (p * (discretization));
        if (l > discretization-1)
            l = discretization-1;
        return l;
    }
    
    public static Term getValueTerm(String prefix, int level) {
        return Instance.make( Term.get(prefix), Term.get("y" + level));       }
    
    public static Term[] getValueTerms(String prefix) {
        Term t[] = new Term[discretization];
        for (int i = 0; i < discretization; i++) {
            t[i] = getValueTerm(prefix, i);
        }
        return t;
    }
    public static Concept[] getValueConcepts(NAR n, String prefix) {
        Concept t[] = new Concept[discretization];
        for (int i = 0; i < discretization; i++) {
            t[i] = n.memory.concept(getValueTerm(prefix, i));
        }
        return t;
    }    
    
    public static Term getValueTerm(double y) {
        return Term.get("y" + f((float)y));
    }
    
    
    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {


        //----
        int duration = 8;
        float freq = 1.0f / duration * 0.5f;        
        int minCyclesAhead = 0;
        double noise = 0.01;
        boolean onlyNoticeChange = false;
        int thinkInterval = onlyNoticeChange ? 1 : duration/4;

        NAR n = new NAR(new Default().setInternalExperience(null));
        n.param.duration.set(duration);
        //n.param.duration.setLinear(0.5);
        n.param.conceptBeliefsMax.set(4);
        //n.param.conceptForgetDurations.set(16);




        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);
        //error = new TreeMLData("error", Color.ORANGE, numIterations).setRange(0, discretization);

        TreeMLData elmanPredict = new TreeMLData("elman prediction", Color.WHITE).setRange(0, 1f);

        
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
                new LineChart(reflections).thickness(16f).height(128),
                /*new LineChart(predictions[1]).thickness(16f).height(128),
                new LineChart(predictions[2]).thickness(16f).height(128),*/
                new LineChart(elmanPredict).thickness(16f).height(128)
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

        for (Term t : getValueTerms("x"))
            n.believe(t.toString(), Tense.Present, 0.5f, 0.5f);
        
        n.believe("<(||,y0,y1,y2,y3,y4) --> y>", Tense.Eternal, 1.0f, 0.95f);
        
        n.run(discretization*4);

        //new TextOutput(n, System.out);
        
        Concept[] valueBeliefConcepts = getValueConcepts(n,"x");
        
        NARSwing.themeInvert();
        new NWindow("x", new ConceptsPanel(n, valueBeliefConcepts)).show(900, 600, true);
        
        BeliefPrediction predictor = new BeliefPrediction(n, valueBeliefConcepts) {

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

            //float signal  = (float)Math.sin(freq * n.time()) * 0.5f + 0.5f;
            float signal = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;

            if (Math.random() > noise)
                observed.add((int) n.time(), signal);

            
            prevY = curY;
            curY = f(signal);

            if ((curY == prevY) && (onlyNoticeChange)) {
                continue;
            }


                
            n.believe(getValueTerm("x", curY).toString(), Tense.Present, 1f, 0.95f);
            
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
