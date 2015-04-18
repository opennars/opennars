///*
// * Here comes the text of your license
// * Each line should be prefixed with  * 
// */
//package nars.rl;
//
//import automenta.vivisect.swing.NWindow;
//import automenta.vivisect.swing.PCanvas;
//import automenta.vivisect.timeline.LineChart;
//import automenta.vivisect.timeline.TimelineVis;
//import java.awt.Color;
//import java.other.Iterator;
//import java.other.List;
//import nars.core.Events.CycleEnd;
//import nars.core.Memory;
//import nars.core.NAR;
//import nars.build.Default;
//import nars.logic.ImmediateProcess;
//import nars.logic.entity.Concept;
//import nars.logic.entity.Sentence;
//import nars.logic.entity.Stamp;
//import nars.logic.entity.Task;
//import nars.logic.entity.TruthValue;
//import nars.gui.NARSwing;
//import nars.gui.output.ConceptsPanel;
//import nars.logic.AbstractObserver;
//import nars.logic.nal7.TemporalRules;
//import nars.io.Answered;
//import nars.io.Symbols;
//import nars.io.TextOutput;
//import nars.io.narsese.Narsese;
//import nars.logic.entity.CompoundTerm;
//import nars.logic.nal5.Conjunction;
//import nars.logic.nal5.Implication;
//import nars.logic.nal1.Inheritance;
//import nars.logic.nal7.Interval;
//import nars.logic.nal7.Tense;
//import nars.logic.entity.Term;
//import nars.logic.nal8.Operation;
//import nars.logic.nal8.Operator;
//import org.encog.engine.network.activation.ActivationSigmoid;
//import org.encog.ml.data.MLData;
//import org.encog.ml.data.MLDataSet;
//import org.encog.ml.data.basic.BasicMLData;
//import org.encog.neural.networks.BasicNetwork;
//import org.encog.neural.networks.training.Train;
//import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
//import org.encog.neural.pattern.ElmanPattern;
//import org.encog.neural.pattern.FeedForwardPattern;
//import org.encog.other.arrayutil.TemporalWindowArray;
//
///**
// *
// * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/predict/sunspot/PredictSunspotElman.java
// * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/recurrent/elman/ElmanXOR.java
// *
// * @author me
// */
//public class Predict2 {
//
//    public static class TimeModel extends AbstractObserver {
//
//        public final List<Concept> concepts;
//
//        public TimeModel(NAR n, List<Concept> concepts) {
//            super(n, true, CycleEnd.class);
//            this.concepts = concepts;
//        }
//
//        @Override
//        public void event(Class event, Object[] args) {
//            if (event == CycleEnd.class) {
//
//            }
//        }
//
//    }
//
//    static BasicNetwork createElmanNetwork() {
//        // construct an Elman type network
//        ElmanPattern pattern = new ElmanPattern();
//        pattern.setActivationFunction(new ActivationSigmoid());
//        pattern.setInputNeurons(1);        
//        pattern.addHiddenLayer(5);
//        pattern.setOutputNeurons(1);
//        return (BasicNetwork) pattern.generate();
//    }
//
//    static BasicNetwork createFeedforwardNetwork() {
//        // construct a feedforward type network
//        FeedForwardPattern pattern = new FeedForwardPattern();
//        pattern.setActivationFunction(new ActivationSigmoid());
//        pattern.setInputNeurons(1);
//        pattern.addHiddenLayer(6);
//        pattern.setOutputNeurons(1);
//        return (BasicNetwork) pattern.generate();
//    }
//
//    static double[] trainingHistory;
//
//    public static MLDataSet getTraining(double nextValue, int inputWindow, int predictionWindow, int historySize) {
//        if ((trainingHistory == null) || (trainingHistory.length != historySize)) {
//            trainingHistory = new double[historySize];
//        }
//
//        System.arraycopy(trainingHistory, 1, trainingHistory, 0, trainingHistory.length - 1);
//        trainingHistory[trainingHistory.length-1] = nextValue;
//        
//        TemporalWindowArray temp = new TemporalWindowArray(inputWindow, predictionWindow);
//
//        temp.analyzeUselessStores(trainingHistory);
//
//        return temp.process(trainingHistory);
//    }
//
//    static double polarize(double v) {
//         return 2 * (v - 0.5);
//    }
//    static double unpolarize(double v) {
//         v = (v/2) + 0.5;
//         if (v < 0) v = 0;
//         if (v > 1.0) v = 1.0;
//         return v;
//    }
//    
//    public static class Notice extends Operator {
//
//        public Notice() {
//            super("^notice");
//        }
//                
//        protected void set(Term term, Term value, float freq, float conf, Memory memory) {
//            
//            
//            Term beliefTerm = Inheritance.make(term, value);
//            
//            int maxBeliefs = memory.param.conceptBeliefsMax.get();
//            
//            
//            Sentence belief = new Sentence(beliefTerm, Symbols.JUDGMENT,new TruthValue(freq, conf), new Stamp(memory, Tense.Present) );
//            
//            Concept c = memory.concept(beliefTerm);
//
//            //1. clear any eternal beliefs
//            Iterator<Sentence> b = c.beliefs.iterator();
//            while (b.hasNext())
//                if (b.next().isEternal())
//                    b.remove();
//            
//            Concept.addToTable(belief, c.beliefs, maxBeliefs);
//            
//            System.err.println(memory.time() + ": " + belief);
//        }
//        
//        @Override
//        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {            
//            
//            //only process input tasks
//            if (!operation.getTask().isInput()) {
//                //System.out.println("not input: " + operation.getTask());
//                //System.out.println(operation.getTask().getExplanation());
//                return null;
//            }
//            
//            //Arg 0: term
//            Term term = (Term)args[0];
//            
//            //Arg 1: value
//            Term value = (Term)args[1];
//            
//            //Arg 2: set of terms which are non-valued (may include 'value' but it is not processed as a nonvalue)
//            CompoundTerm nonvalues = (CompoundTerm)args[2];
//            
//            set(term, value, 1.0f, 0.95f, memory);            
//            for (Term v : nonvalues.term) {
//                if (v.equals(value)) continue;
//                set(term, v, 0.0f, 0.95f, memory);
//            }
//
//            return null;
//        }
//        
//    }
//    
//    
//    
//    static int discretization = 5;
//    
//    public static int f(float p) {
//        if (p < 0) {
//            p = 0;
//            //p = 0;
//        }
//        if (p > 1f) {
//            p = 1f;
//        }
//        return (int) (p * (discretization-1));
//    }
//    
//    public static Term getValueTerm(double y) {
//        return Term.get("y" + f((float)y));
//    }
//    
//    public static void input(long lastTime, Term prevTerm, double prevY, Term term, double y, float truth, Memory memory) {
//            
//            long dt = memory.time() - lastTime;
//            
//            Term prevBelief = Inheritance.make(prevTerm, getValueTerm(prevY));
//            Interval interval = Interval.interval(dt, memory);
//            //System.out.println(dt + " cycles -> " + interval.getTime(memory));
//            
//            Term currentBelief = Inheritance.make(term, getValueTerm(y));
//            
//            if (prevBelief.equals(currentBelief)) return;
//            
//            Term conj = Conjunction.make(new Term[] {
//                prevBelief, interval
//            }, TemporalRules.ORDER_FORWARD);
//            
//            Term implication = Implication.make(conj, currentBelief, TemporalRules.ORDER_CONCURRENT);
//            
//            
//            //Sentence belief = new Sentence(beliefTerm, Symbols.JUDGMENT,new TruthValue(freq, conf), new Stamp(memory, Tense.Present) );
//            
//            //Concept c = memory.concept(beliefTerm);
//
//
//            //Task t = 
//            
//            //new ImmediateProcess(memory, memory.newTaskAt(implication, Symbols.JUDGMENT, truth, 0.90f, 1.0f, 0.8f), 0).run();
//            
//            //memory.inputTask(t);
//            
//            new ImmediateProcess(memory, memory.newTaskAt(currentBelief, Symbols.JUDGMENT, truth, 0.90f, 1.0f, 0.8f), 0).run();
//       }        
//    
//    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {
//
//        final BasicNetwork elmanNetwork = createElmanNetwork();
//
//
//        //----
//        int duration = 12;
//        float freq = 1.0f / duration * 0.35f;        
//        int minCyclesAhead = 0;
//        double noise = 0.01;
//        boolean onlyNoticeChange = true;
//        int thinkInterval = onlyNoticeChange ? 1 : duration/1;
//
//        NAR n = new NAR(new Default().setInternalExperience(null));
//        n.param.duration.set(duration);
//        //n.param.duration.setLinear(0.5);
//        n.param.conceptBeliefsMax.set(16);
//        //n.param.conceptForgetDurations.set(16);
//
//        String xPrevFuncEq0 = "<x_tMin1 --> y0>";
//        String xPrevFuncEq1 = "<x_tMin1 --> y1>";
//        String xFuncEq0 = "<x_t0 --> y0>";
//        String xFuncEq1 = "<x_t0 --> y1>";
//        //String xNextFunc = "<x_tPlus1 --> y>";
//        //n.believe(xPrevFuncEq0, Tense.Present, 0.50f, 0.50f).run(1);
//        //n.believe(xPrevFuncEq1, Tense.Present, 0.50f, 0.50f).run(1);
//        n.believe(xFuncEq0, Tense.Present, 0.50f, 0.50f).run(1);
//        n.believe(xFuncEq1, Tense.Present, 0.50f, 0.50f).run(1);
//        //n.believe("<(&/," + xPrevFunc + ",+1) =/> " + xFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
//        //n.believe("<(&/," + xPrevFunc + ",+1," + xFunc + ",+1) =/> " + xNextFunc + ">", Tense.Eternal, 1.00f, 0.95f).run(1);
//        //n.believe("<(&/," + xFunc + ",+1) =/> " + xNextNextFunc + ">", Tense.Eternal, 1.00f, 0.90f).run(1);
//
//        Answered a = new Answered() {
//            @Override
//            public void onSolution(Sentence belief) {
//                System.out.println(belief);
//            }
//
//            @Override
//            public void onChildSolution(Task child, Sentence belief) {
//                System.out.println(belief);
//            }
//
//        };
//
//        n.ask("<<x_t0 --> $e> =/> ?f>");
//        n.ask("<(&/,<x_t0 --> $a>,+1) =|> ?x>");
//        n.ask("<(&/,<x_t0 --> $b>,+2) =|> ?y>");
//        n.ask("<(&/,<x_t0 --> $c>,+3) =|> ?z>");
//        n.ask("<(&/,<x_t0 --> $d>,+4) =|> ?w>");
//
//        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);
//        //error = new TreeMLData("error", Color.ORANGE, numIterations).setRange(0, discretization);
//
//        TreeMLData elmanPredict = new TreeMLData("elman prediction", Color.WHITE).setRange(0, 1f);
//
//        
//        TreeMLData[] predictions = new TreeMLData[discretization];
//        TreeMLData[] reflections = new TreeMLData[discretization];
//
//        for (int i = 0; i < predictions.length; i++) {
//            predictions[i] = new TreeMLData("Pred" + i,
//                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
//            predictions[i].setDefaultValue(0.0);
//            
//            reflections[i] = new TreeMLData("Refl" + i,
//                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
//            reflections[i].setDefaultValue(0.0);
//            //predictions[i].setRange(0, 0.5);
//        }
//        TimelineVis tc = new TimelineVis(
//                new LineChart(observed).thickness(16f).height(128),                          new LineChart(predictions).thickness(16f).height(128),
//                new LineChart(reflections).thickness(16f).height(128),
//                /*new LineChart(predictions[1]).thickness(16f).height(128),
//                new LineChart(predictions[2]).thickness(16f).height(128),*/
//                new LineChart(elmanPredict).thickness(16f).height(128)
//        );
//        //new BarChart(error).height(4)
//        new NWindow("_", new PCanvas(tc)).show(800, 800, true);
//
//        new TextOutput(n, System.out) {
//
//            /** dt = relative to center */
//            public double getPredictionEnvelope(double dt, double duration) {
//                //guassian curve width=duration
//                //  e^(-(4*x/(dur))^2)    
//                double p = (4 * dt / duration);
//                return Math.exp( -(p * p) );
//            }
//            
//            /**
//             * only allow future predictions
//             */
//            protected boolean allowTask(Task t) {
//                if (t.sentence.isEternal()) {
//                    return false;
//                }
//                
//                boolean future = false;
//                if ((t.sentence.getOccurenceTime() > n.time() + minCyclesAhead)) {
//                    System.out.print(n.time() + ".." + t.sentence.getOccurenceTime() + ": ");
//                    future = true;
//                }
//                
//                    Term term = t.getTerm();
//                    int time = (int) t.sentence.getOccurenceTime();
//                    int value = -1;
//                    float conf = t.sentence.truth.getConfidence();
//                    float expect = 2f * (t.sentence.truth.getFrequency() - 0.5f) * conf;
//                    String ts = term.toString();
//                    if (ts.startsWith("<x_t0 --> y")) {
//                        char cc = ts.charAt("<x_t0 --> y".length());
//                                
//                        value = cc - '0';
//                        
//                    }
//                    
//                    
//                    if (value != -1) {
//
//                        //predictions[(int)value].addPlus(time, expect);
//                        for (int tt = time - duration / 2; tt <= time + duration / 2; tt++) {
// 
//                            double smooth = 1;
//                            expect *= getPredictionEnvelope(time-tt, smooth * duration*2f);
//                            
//                            if (future)
//                                predictions[value].addPlus(tt, expect);
//                            else
//                                reflections[value].addPlus(tt, expect);
//
//                        }
//
//                    }
//
//
//                    return true;
//
//            }
//        };
//
//        n.run(10);
//        //n.addPlugin(new Notice());
//
//        Concept x0Prev = n.concept(xPrevFuncEq0);
//        Concept x1Prev = n.concept(xPrevFuncEq1);
//        Concept x0Now = n.concept(xFuncEq0);
//        Concept x1Now = n.concept(xFuncEq1);
//
//        NARSwing.themeInvert();
//        new NWindow("x", new ConceptsPanel(n, x0Prev, x1Prev, x0Now, x1Now)).show(900, 600, true);
//        //new NARSwing(n);
//
//        float y = 0.5f;
//        long prevT = n.time();
//        while (true) {
//
//            MLData inputData = new BasicMLData(1);
//            inputData.setData(0, polarize(y));
//            MLData predict = elmanNetwork.compute(inputData);
//            elmanPredict.add((int) n.time(), unpolarize(predict.getData(0)));
//
//            n.run(thinkInterval);
//
//            Thread.sleep(3);
//            //n.memory.addSimulationTime(1);
//
//            float curY  = (float)Math.sin(freq * n.time()) * 0.5f + 0.5f;
//            //float curY = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;
//
//            if (Math.random() > noise)
//                observed.add((int) n.time(), curY);
//
//            final Train train = new ResilientPropagation(elmanNetwork, getTraining(polarize(curY), 16, 1, 96));
//            
//            
//            for (int ti = 0; ti < 64; ti++) {
//                train.iteration();
//            }
//            //System.out.println("elman error: " + train.getError());
//
//            if ((curY == y) && (onlyNoticeChange)) {
//                continue;
//            }
//
//            float prevY = y;
//            
//            //input(Term.get("x_tMin1"), Term.get(value), 1.0f, 0.95f, n.memory);
//            //n.addInput("notice(x_tMin1," + value + ",(||,y0,y1))!");
//            
//            /*
//            n.believe(xPrevFuncEq0, Tense.Past, 1.0f, y);
//            n.believe(xPrevFuncEq0, Tense.Past, 0.0f, 1f - y);
//            n.believe(xPrevFuncEq1, Tense.Past, 1.0f, 1f - y);
//            n.believe(xPrevFuncEq1, Tense.Past, 0.0f, y);
//                    */
//            input(prevT, Term.get("x_t0"), prevY, Term.get("x_t0"), curY, 1f, n.memory); 
//            //input(prevT, Term.get("x_t0"), prevY, Term.get("x_t0"), 1f-curY, 0.5f, n.memory);             
//            y = curY;
//            
//            
//            //n.addInput("notice(x_t0," + value + ",(||,y0,y1))!");
//            
//            //input(prevT, Term.get("x_tMin1"), prevY, Term.get("x_t0"), 1f-y, 0f, n.memory); 
//            
//            //input(Term.get("x_t0"), Term.get(value), 0.0f, 0.0f, n.memory);              //input(Term.get("x_t0"), Term.get(otherValue), 1.0f, 0f, n.memory);  */
//            /*
//            n.believe(xFuncEq0, Tense.Present, 1.0f, y);
//            n.believe(xFuncEq0, Tense.Present, 0.0f, 1f - y);
//            n.believe(xFuncEq1, Tense.Present, 1.0f, 1f - y);
//            n.believe(xFuncEq1, Tense.Present, 0.0f, y);
//                    */
//
//            prevT = n.time();
//        }
//
//    }
//}
