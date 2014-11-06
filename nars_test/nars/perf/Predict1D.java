/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf;

import java.util.Map;
import java.util.TreeMap;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Events.FrameStart;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Output.IN;
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
    float dt = 0.25f;
    int cyclesPerDuration = 1000;
    int discretization = 5;
    
    
    Map<Term,Sentence> solution = new TreeMap();

    protected void forgetSolution(float rate) {
            //TODO apply forget to confidence
            solution.clear();
    }
        
    
    public void addAxioms() {
        String c = "";
        for (int i = 1; i < discretization; i++) {
            c += "<" + (i-1) + " <-> " + (i) + ">. %0.90;0.50%\n";
        }
        nar.addInput(c);
    }
        
    float nextSample() {        
        float v = (float)Math.sin(t)/2f+0.5f;
        t += dt;
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

    protected Term solutionTerm(String channel, Term s) {
        if (s.getComplexity() == 3) {
            if (s instanceof Inheritance) {
                Inheritance i = (Inheritance)s;
                if (i.getPredicate().equals(Term.get(channel)))
                    return i.getSubject();
            }
        }
        return null;
    }
    
    public Term getMostLikelyPrediction(String channel) {
        Sentence best = null;
        for (Sentence s : solution.values()) {            
            if (best == null) { best = s; continue; }
            if (best.truth.getExpectation() < s.truth.getExpectation())
                best = s;            
        }
        if (best!=null)
            return solutionTerm(channel, best.content);
        return null;
    }
    
    public float getSurprise(float newValue) {
        //TODO characterization of how predicted value equals/doesn't equal current observations        
        return 0;
    }
    
    public void observe(String channel, float value, float conf) {
        int l = f(value);
        
        String c = "";
        for (int i = 0; i < discretization; i++) { 
            c += "<" + i + " --> " + channel + ">. :|: %" + n2( (i == l) ? 1.0f : 0.0f) + ";" + n2(conf) + "%\n";
        }
        nar.addInput(c);
        
        c = "";       
        for (int i = 0; i < discretization; i++) {
            c += "<" + i + " --> " + channel + ">? :/:" + '\n';
        }
        nar.addInput(c);
    }

    public void trySolution(Sentence newSolution) {
        if (solutionTerm("x", newSolution.content)!=null) {
            float conf = newSolution.truth.getConfidence();

            Sentence existingSolution = solution.get(newSolution.content);

            if ((existingSolution == null) || (existingSolution.truth.getConfidence()< conf) || newSolution.after(existingSolution, nar.memory.getDuration()))
                solution.put(newSolution.content, newSolution);                
        }
    }
    
    public Predict1D() {
        //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]
        
        this.nar = new DefaultNARBuilder().simulationTime().build();

        /*
        this.nar = new NeuromorphicNARBuilder(4).
                setTaskLinkBagSize(4).
                setTermLinkBagSize(100).   
                simulationTime().                
                build();
        nar.param().conceptForgetDurations.set(4);
        nar.param().beliefForgetDurations.set(16);
        nar.param().taskForgetDurations.set(2);
        */
        
        //new TextOutput(nar, System.out);
        
        nar.on(IN.class, new Observer() {
            @Override public void event(Class event, Object[] arguments) {
                if (arguments[0] instanceof Task) {
                    Task t = (Task)arguments[0];
                    //trySolution(t.sentence);
                }
            }            
        });
        nar.on(FrameStart.class, new Observer() {
            
            @Override public void event(Class event, Object[] arguments) {
                                   
                float sample = nextSample();
                
                if (!solution.isEmpty()) {
                    Term prediction = getMostLikelyPrediction("x");
                    
                    System.out.println("@" + nar.time() + ": " + prediction + "? " + f(sample) + " ");
                    System.out.println(solution.values());
                }

                observe("x", sample, 0.95f);
            }            
        });
        nar.on(Events.Solved.class, new Observer() {

            
            @Override
            public void event(Class event, Object[] arguments) {
                Sentence newSolution = (Sentence)arguments[1];
                
                trySolution(newSolution);
            }
           
            
        });
        
        nar.param().duration.set(cyclesPerDuration);

        addAxioms();
        
        for (int p = 0; p < 1000; p++) {
            
            //forgetSolution(0.01f);
            
            nar.frame(cyclesPerDuration);
            
            nar.memory.addSimulationTime(cyclesPerDuration);
        }
        //nar.startFPS(1, 10, 1.0f);
        
    }
    
    public static void main(String[] args) {
        new Predict1D();
    }
}
