/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl;

import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.BarChart;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.StackedPercentageChart;
import automenta.vivisect.timeline.TimelineVis;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Events.FrameStart;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.ChangedTextInput;
import nars.model.Default;
import nars.nal.entity.Sentence;
import nars.nal.entity.Task;
import nars.nal.language.Inheritance;
import nars.nal.language.Term;
import nars.util.TreeMLData;

import static nars.io.Texts.n2;

/**
 *
 * @author me
 */
public class Predict1 {

    private final NAR nar;

    float t = 0;
    int cyclesPerDuration = 5;
    int cyclesPerFrame = 10;
    final static int discretization = 3;
    float errorRate = 0.05f;
    final static int numIterations = 3500;
    final TreeMLData observed, observedDiscrete, bestPredictions, error;
    final TreeMLData[] predictions;

    float signalFreq = 0.75f;
    float sampleConfidence = 0.99f;
    float sampleFreq = 1.00f; //0.5f + 0.5f / (discretization);

    float predictionFutureCycles = cyclesPerDuration * 1f;
    
    transient boolean inputting = true;
    int signalMode = 0;

    /**
     * weighs a set of positive and negative beliefs
     */
    abstract public class BeliefSet {

        //TODO store solutions separately in different Map per channel

        Map<Term, Sentence> belief = new TreeMap();

        public void add(Sentence s, int duration) {
            Term term = getValue(s.term);
            if (term == null) {
                return;
            }

            float conf = s.truth.getConfidence();

            Sentence existingSolution = belief.get(s.term);
            boolean sameTime = false, newer = false, moreConfident = false;
            if (existingSolution!=null) {
                sameTime = existingSolution.stamp.getOccurrenceTime() == s.stamp.getOccurrenceTime();
                newer = s.after(existingSolution, duration);
                moreConfident = (existingSolution.truth.getConfidence() <= conf);
            }
                    
            if ((existingSolution == null) || 
                    (sameTime && moreConfident) || (newer)) {                      
                belief.put(s.term, s);
            }

        }

        public void forget(float rate) {
            //TODO apply forget to confidence
            belief.clear();
        }

        public Collection<Term> getBeliefs() {
            return belief.keySet();
        }

        public float getExpectation(Term b) {

            for (Sentence s : belief.values()) {
                if (getValue(s.term).equals(b)) {
                    double dt = Math.max(0, s.stamp.getOccurrenceTime() - (predictionFutureCycles + nar.time()));
                    dt /= cyclesPerDuration;
                    return (s.truth.getExpectation()+1f)/2f * (float)(1.0f / ( 1f + dt));
                }
            }

            return 0;
        }

        public float getLikelihood(Term b) {
            return getExpectation(b);
        }

        abstract public Term getValue(Term s);

        public Set<Term> values() {
            Set<Term> s = new HashSet();
            for (Term e : belief.keySet()) {
                s.add(getValue(e));
            }
            return s;
        }

        public Term getMostLikely() {
            Term best = null;
            float bestSol = Float.NEGATIVE_INFINITY;
            for (Term t : values()) {
                float s = getLikelihood(t);
                if (s > bestSol) {
                    bestSol = s;
                    best = t;
                }
            }
            return best;
        }
    }

    public BeliefSet solutions = new BeliefSet() {

        @Override
        public Term getValue(Term s) {
            if (s instanceof Inheritance) {
                Inheritance i = (Inheritance) s;
                if (i.size() == 2) {
                    Term chanTerm = i.getSubject();
                    Term valTerm = i.getPredicate();

                    if (chanTerm.equals(Term.get("x"))) {
                        try {
                            int vi = Integer.parseInt(valTerm.toString());
                            return valTerm;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }
            }

            return null;
        }

    };

    public void addAxioms() {
        String c = "";
        /*for (int i = 1; i < discretization; i++) {
         c += "<" + (i-1) + " <-> " + (i) + ">. %1.00;0.10%\n";
         }*/
//        nar.addInput(c);

    }

    float delayUntil = 0;
    
    float nextSample() {
        float v;

        t++;
        
        if (delayUntil > 0) {
            if (delayUntil > t) {
                return Float.NaN;
            }
            delayUntil = 0;
        }
        
        float x = ((float) t / cyclesPerFrame) * signalFreq;

        v = 0;
        switch (signalMode %2) {
            case 0:
                v = (float) Math.sin(x) / 2f + 0.5f;
                break;
            case 1:
                v = (x % discretization)/discretization;
                break;
        }
        

        if (Math.random() < 0.0001) {
            delayUntil = t + (int)(cyclesPerFrame * cyclesPerDuration);
        }
        if (Math.random() < 0.0005) {
            signalMode++;
        }
        
        
        return v;
    }

    /**
     * discretize float (0..1.0) to a term
     */
    public int f(float p) {
        if (p < 0) {
            p = 0;
            //p = 0;
        }
        if (p > 1f) {
            p = 1f;
        }
        return (int) (p * discretization);
    }

    public String summarizeExpectation(List<Sentence> l) {
        String s = "";
        for (Sentence p : l) {
            s += p.term + "=" + n2(p.truth.getExpectation()) + ",";
        }
        return s;
    }
//    public Term getMostLikelyPrediction(String channel) {
//        Term best = null;
//        Iterator<Sentence> p = solutionPos.values().iterator();
//        Iterator<Sentence> n = solutionNeg.values().iterator();
//        for (int i = 0; i < solutionPos.size(); i++) {
//            Sentence p = solutionPos.
//            if (best == null) { best = s; continue; }
//            if (best.truth.getExpectation() < s.truth.getExpectation())
//                best = s;            
//        }
//        if (best!=null)
//            return solutionTerm(channel, best.content, "y");
//        return null;
//    }

    public float getSurprise(float newValue) {
        //TODO characterization of how predicted value equals/doesn't equal current observations        
        return 0;
    }

    ChangedTextInput[] observedInput;
    float lastValue = Float.NaN;

    public void observe(String channel, float value, float conf, long time) {
        if (Float.isNaN(lastValue)) {
            lastValue = value;
            return;
        }
       

        int prev = f(lastValue);
        int curr = f(value);
        
        if (prev == curr) {
            return;
        }

        for (int i = 0; i < discretization; i++) {
            //negative            
            if (i != curr) {
                String n = "<" + i + " --> " + channel + ">";

                String antiObservation = n + ". :|: %" + n2(1.0f - sampleFreq) + ";" + n2(conf) + "%";
                nar.addInput(antiObservation);
            }
        }

//        nar.addInput(antiObservation);
        String x0 = "<" + channel + " --> " + prev + ">";
        String x = "<" + channel + " --> " + curr + ">";
        nar.addInput(x + ". :|: %" + n2(sampleFreq) + ";" + n2(conf) + "%");

        nar.addInput("<(&/," + x0 + ",+1) =/> " + x + ">.");

        String interval = "+1";
        String prediction = "<(&/," + x + "," + interval + ") =/> <" + channel + " --> ?x>>?";

        nar.addInput(prediction, time);

        lastValue = value;
    }

    public void tick() {
        //nar.addInput("<time --> now>. :|:");

    }

    public Integer getSampleObservation(Term t, String channel) {
        if ((t instanceof Inheritance) && (!t.hasVar()) && (t.name().toString().startsWith("<" + channel + " --> ")) && (t.getComplexity() == 3)) {
            return Integer.parseInt(((Inheritance) t).getPredicate().name().toString());
        }
        return null;
    }

    float evidence[] = new float[discretization];

    public void onBelief(Sentence s) {
        Term t = s.term;
        if (!s.isEternal()) {
            Integer i = getSampleObservation(t, "x");
            if (i == null) return;
            
            //System.out.println(c + " " + c.beliefs);
            if (nar.time() <= s.getOccurenceTime()) {
                float dt = s.getOccurenceTime() - nar.time();
                float weight = s.truth.getExpectation();
                evidence[i] = Math.max(evidence[i], weight);
                //System.out.println((dt >= 0 ? "+" : "") + dt + " " + s + "---> " + i + " " + weight + " " + evidence[i]);
                solutions.add(s, 0);
                //System.out.println("  "+ solutions.getExpectation(t));
            }

        }

    }

    public Predict1() {
        //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]

        Parameters.DEBUG = true;
        
        this.nar = new Default().simulationTime().build();

        nar.param.conceptForgetDurations.set(10);
        nar.param.taskLinkForgetDurations.set(16);
        nar.param.termLinkForgetDurations.set(40);
        nar.param.novelTaskForgetDurations.set(8);
        nar.param.duration.set(cyclesPerDuration);
        /*
         this.nar = new NeuromorphicNARBuilder(4).
         setTaskLinkBagSize(4).
         setTermLinkBagSize(100).   
         simulationTime().                
         build();
         */

        //new TextOutput(nar, System.out, 0.95f);
        
        

        nar.on(FrameStart.class, new EventObserver() {

            @Override
            public void event(Class event, Object[] arguments) {

                long d = nar.time();

                float sample = nextSample();

                if (!solutions.belief.isEmpty()) {

                    
                    for (Term s : solutions.values()) {
                        try {
                            int is = Integer.valueOf(s.toString());
                            float e = solutions.getLikelihood(s);
                            
                            predictions[is].add((int)d, e);                            
                        } catch (NumberFormatException nfe) {

                        }
                    }

                    //System.out.println(solutions.belief);

                    Term predTerm = solutions.getMostLikely();
                    int predicted = Integer.valueOf(predTerm.toString());
                    bestPredictions.add((int)d, predicted);                    
                    
                    if (!Float.isNaN(sample)) {
                        float e = Math.abs(f(sample) - predicted);
                        error.add((int)d, e);
                    }
                    /*
                     System.out.println("@" + nar.time() + ": " + prediction + "? " + f(sample) + " ");*/
                    //System.out.println(summarizeExpectation(getLikeliness("x")));
                }

                if (!Float.isNaN(sample) /*&& (nar.time() % cyclesPerFrame == 0)*/) {
                    //solutions.forget(1);

                    if (inputting && Math.random() > errorRate) {
                        observed.add((int)d+1, sample);
                        observedDiscrete.add((int)d+1, f(sample));
                        observe("x", sample, sampleConfidence, nar.time());
                    }
                }

                tick();

            }
        });

        nar.on(Events.ConceptBeliefAdd.class, new EventObserver() {

            @Override
            public void event(Class event, Object[] args) {
                Sentence s = ((Task) args[1]).sentence;

                //onBelief(s);
            }
        });

        nar.on(Events.Answer.class, new EventObserver() {
            @Override
            public void event(Class event, Object[] arguments) {
                Task t = (Task) arguments[0];
                Sentence newSolution = (Sentence) arguments[1];
                System.out.println("solution: " + newSolution);

                onBelief(newSolution);
            }
        });

        observedInput = new ChangedTextInput[discretization];
        for (int i = 0; i < discretization; i++) {
            observedInput[i] = new ChangedTextInput(nar);
        }

        observed = new TreeMLData("value", Color.WHITE, numIterations).setRange(0, 1f);

        observedDiscrete = new TreeMLData("observed", Color.YELLOW, numIterations).setRange(0, discretization);
        bestPredictions = new TreeMLData("predict", Color.ORANGE, numIterations).setRange(0, discretization);
        error = new TreeMLData("error", Color.ORANGE, numIterations).setRange(0, discretization);
        
        predictions = new TreeMLData[discretization];
        
        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = new TreeMLData("Pred" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f), numIterations);
        }

        TimelineVis tc = new TimelineVis(
                new BarChart(observed).height(4),
                new LineChart(observedDiscrete).height(4),
                new LineChart(bestPredictions).height(4),                
                new StackedPercentageChart(predictions).height(8),
                new LineChart(predictions).height(8),
                new BarChart(error).height(4)
                
        //            new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
        //
        //            new LineChart(
        //                    new TreeMLData.ConceptBagTreeMLData(nar, nar.memory.concepts, cycles, TreeMLData.ConceptBagTreeMLData.Mode.ConceptPriorityTotal)            
        //            ).height(4),
        //
        //            new LineChart(
        //                    new TreeMLData.ConceptBagTreeMLData(nar, nar.memory.concepts, cycles, TreeMLData.ConceptBagTreeMLData.Mode.TermLinkPriorityMean),
        //                    new TreeMLData.ConceptBagTreeMLData(nar, nar.memory.concepts, cycles, TreeMLData.ConceptBagTreeMLData.Mode.TaskLinkPriorityMean)
        //            
        //            ).height(4),
        //
        //
        //            new LineChart(t, "task.novel.add", "task.immediate_processed").height(3),
        //            new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
        //            new LineChart(t, "emotion.busy").height(1),
        //            new EventChart(t, false, false, true).height(3)                
        );

        new NWindow("_", new PCanvas(tc)).show(800, 800, true);
        //new NARSwing(nar);

        addAxioms();

        for (int p = 0; p < numIterations; p++) {

            nar.frame(1);

            nar.memory.addSimulationTime(1);
        }

    }

    public static void main(String[] args) {
        new Predict1();
    }
}
