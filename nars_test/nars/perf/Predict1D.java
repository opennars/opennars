/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Events.FrameStart;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Sentence;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.gui.output.chart.TimeSeries;
import nars.gui.output.timeline.BarChart;
import nars.gui.output.timeline.LineChart;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.gui.output.timeline.Timeline2DCanvas;
import nars.io.ChangedTextInput;
import static nars.io.Texts.n2;
import nars.language.Inheritance;
import nars.language.Term;

/**
 *
 * @author me
 */
public class Predict1D {
    private final NAR nar;
 
    float t = 0;
    int cyclesPerDuration = 5;
    int cyclesPerFrame = 100 * cyclesPerDuration;
    final static int discretization = 3;
    float errorRate = 0.05f;
    final static int numIterations = 1500;
    final TimeSeries observed, observedDiscrete, bestPredictions;
    final TimeSeries[] predictions;

    float frequency = 0.05f;
    float sampleConfidence = 0.8f;
    
    transient boolean inputting = true;
    private int cutoffTime = numIterations/10;
            
    
    /** weighs a set of positive and negative beliefs */
    abstract public static class BeliefSet {
        //TODO store solutions separately in different Map per channel
        Map<Term,Sentence> belief = new TreeMap();
        
        public void add(Sentence s, int duration) {
            Term term = getValue(s.content);
            if (term == null) return;
            
            float conf = s.truth.getConfidence();
            
            Sentence existingSolution = belief.get(s.content);
            if ((existingSolution == null) || (existingSolution.truth.getConfidence()< conf) || s.after(existingSolution, duration))
                belief.put(s.content, s);            
            
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
                if (getValue(s.content).equals(b)) {
                    return s.truth.getExpectation();                        
                }
            }
            
            return 0;
        }
        
        public float getLikelihood(Term b) {
            return (getExpectation(b));
        }
        
        abstract public Term getValue(Term s);

        public Set<Term> values() {            
            Set<Term> s = new HashSet();
            for (Term e : belief.keySet())
                s.add(getValue(e));
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

        @Override public Term getValue(Term s) {
            if (s instanceof Inheritance) {
                Inheritance i = (Inheritance)s;
                if (i.size() == 2) {
                    Term chanTerm = i.getPredicate();
                    Term valTerm = i.getSubject();
                                        
                    if (chanTerm.equals(Term.get("x"))) {
                        try {
                            int vi = Integer.parseInt(valTerm.toString());                        
                            return valTerm;
                        }
                        catch (Exception e) { return null; }
                    }
                }
            }

            return null;
        }

        
    };
    
    public void addAxioms() {
        String c = "";
//        for (int i = 1; i < discretization; i++) {
//            c += "<" + (i-1) + " <-> " + (i) + ">. %1.00;0.10%\n";
//        }
//        nar.addInput(c);
        
    }
        
    float nextSample() {        
        float v;
        
        float x = t * frequency;
        
        //STEP
        //v = ((int)x % 4.0f)/4f;
        
        //SINE
        v = (float)Math.sin(x)/2f+0.5f;
        
        
        t++;
        return v;        
    }
    
    /** discretize float (0..1.0) to a term */
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
            s += p.content + "=" + n2(p.truth.getExpectation()) + ",";
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
    
    public void observe(String channel, float value, float conf, long time) {
        int l = f(value);
        
        String x = "<";
        for (int i = 0; i < discretization; i++) { 
            //c += "<" + i + " --> " + channel + ">. :|: %" + n2( (i == l) ? 1.0f : 0.0f) + ";" + n2(conf) + "%\n";
            //positive
            String sss;
            if (i == l)
                sss = "" + i;
            else
                sss = "";
            
            x += sss;            
                      
        }
        x += " --> " + channel + ">";
        String observation = x + ". :|: %1.00;" + n2(conf) + "%";
        nar.addInput(observation, time);
        
        String interval = "+1";
        //String prediction = "<(&/," + x + "," + interval + ") =/> <$x --> " + channel + ">>?";
        
        String prediction = "<(&/," + x +"," + interval + ") =/> <#x --> " + channel + ">>?";     
        nar.addInput(prediction, time);
    }
    
    public void tick() {
        //nar.addInput("<time --> now>. :|:");
        
    }
    

    
    public Predict1D() {
        //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]
        
        this.nar = new Default().simulationTime().build();

        nar.param.conceptForgetDurations.set(10);
        nar.param.taskLinkForgetDurations.set(16);
        nar.param.termLinkForgetDurations.set(40);
        nar.param.novelTaskForgetDurations.set(8);
        /*
        this.nar = new NeuromorphicNARBuilder(4).
                setTaskLinkBagSize(4).
                setTermLinkBagSize(100).   
                simulationTime().                
                build();
        */
        
        
        //new TextOutput(nar, System.out, 0.95f);
        (nar.param).duration.set(cyclesPerDuration);
        
        nar.on(FrameStart.class, new Observer() {
            
            @Override public void event(Class event, Object[] arguments) {
                                   
                long d =nar.time()/cyclesPerFrame;
                
                float sample = nextSample();
                
                
                if (!solutions.belief.isEmpty()) {
                    
                    for (Term s : solutions.values()) {
                        try {
                            int is = Integer.valueOf( s.toString() );
                            float e = solutions.getLikelihood(s);
                            predictions[is].push(d, e);
                        }
                        catch (NumberFormatException nfe) {
                            
                        }
                    }
                    
                    
                    System.out.println(solutions.belief);
                    
                    Term prediction = solutions.getMostLikely();
                    bestPredictions.push(d, Integer.valueOf( prediction.toString() ));
                    
                            /*
                    System.out.println("@" + nar.time() + ": " + prediction + "? " + f(sample) + " ");*/
                    //System.out.println(summarizeExpectation(getLikeliness("x")));
                }

                solutions.forget(1);

                
                
                if (nar.time() % cyclesPerFrame == 0) {
                    if (inputting && Math.random() > errorRate) {
                        observed.push(d, sample);
                        observedDiscrete.push(d, f(sample));
                        observe("x", sample, sampleConfidence, nar.time());
                    }
                }
                
                tick();
                
            }            
        });
        nar.on(Events.Solved.class, new Observer() {           
            @Override public void event(Class event, Object[] arguments) {
                Sentence newSolution = (Sentence)arguments[1];
          
                solutions.add(newSolution, nar.memory.getDuration());
            }
        });
        
        observedInput = new ChangedTextInput[discretization];
        for (int i = 0; i < discretization; i++)
            observedInput[i] = new ChangedTextInput(nar);
                 
        observed = new TimeSeries("value", Color.WHITE, numIterations);
    
        observedDiscrete = new TimeSeries("value(disc)", Color.YELLOW, numIterations);        
        bestPredictions = new TimeSeries("predict", Color.ORANGE, numIterations);        
        predictions = new TimeSeries[discretization];
        for (int i = 0; i < predictions.length; i++)
            predictions[i] = new TimeSeries("Pred" + i, 
                    Color.getHSBColor(0.25f + i/4f, 0.85f, 0.85f), numIterations);
        
        
        
        
        
        
        Timeline2DCanvas tc = new Timeline2DCanvas(

            new BarChart(observed).height(4),
            new LineChart(observedDiscrete).height(4),
            new LineChart(bestPredictions).height(4),
            new StackedPercentageChart(predictions).height(8),
            new LineChart(predictions).height(8)

//            new StackedPercentageChart(t, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2),
//
//            new LineChart(
//                    new TimeSeries.ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, TimeSeries.ConceptBagTimeSeries.Mode.ConceptPriorityTotal)            
//            ).height(4),
//
//            new LineChart(
//                    new TimeSeries.ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, TimeSeries.ConceptBagTimeSeries.Mode.TermLinkPriorityMean),
//                    new TimeSeries.ConceptBagTimeSeries(nar, nar.memory.concepts, cycles, TimeSeries.ConceptBagTimeSeries.Mode.TaskLinkPriorityMean)
//            
//            ).height(4),
//
//
//            new LineChart(t, "task.novel.add", "task.immediate_processed").height(3),
//            new LineChart(t, "task.goal.process", "task.question.process", "task.judgment.process").height(3),
//            new LineChart(t, "emotion.busy").height(1),
//            new EventChart(t, false, false, true).height(3)                
        );
                
        
        new NWindow("_", tc).show(800, 800, true);
        new NARSwing(nar);
        
        
        addAxioms();
        
        
        
        /*for (int p = 0; p < numIterations; p++) {
                        
            nar.frame(cyclesPerFrame);
            
            nar.memory.addSimulationTime(cyclesPerFrame);
        }*/
        
      
    }
    
    public static void main(String[] args) {
        new Predict1D();
    }
}
