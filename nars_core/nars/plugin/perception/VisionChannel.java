package nars.plugin.perception;

import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.main.NAR;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.io.events.EventEmitter;
import nars.io.events.Events;
import nars.io.events.Events.CycleEnd;
import nars.io.events.Events.ResetEnd;
import nars.language.Inheritance;
import nars.language.SetExt;
import nars.language.Tense;
import nars.language.Term;
import nars.main.Parameters;

public class VisionChannel extends SensoryChannel  {
    public float DEFAULT_OUTPUT_CONFIDENCE = 0.2f;
    double[][] inputs;
    boolean[][] updated;
    int cnt_updated = 0;
    int px = 0;
    int py = 0;
    Term label;
    NAR nar;
    boolean HadNewInput = false; //only generate frames if at least something was input since last "commit to NAR"
    public EventEmitter.EventObserver obs;
    public VisionChannel(Term label, NAR nar, SensoryChannel reportResultsTo, int width, int height, int duration) {
        super(nar,reportResultsTo, width, height, duration);
        this.nar = nar;
        this.label = label;
        inputs = new double[height][width];
        updated = new boolean[height][width];
        obs = new EventEmitter.EventObserver() {
            @Override
            public void event(Class ev, Object[] a) {
                if(HadNewInput && ev == CycleEnd.class) {
                    empty_cycles++;
                    if(empty_cycles > duration) { //a deadline, pixels can't appear more than duration after each other
                        step_start(); //so we know we can input, not only when all pixels were re-set.
                    }
                }
                else 
                if(ev == ResetEnd.class) {
                    inputs = new double[height][width];
                    updated = new boolean[height][width];
                    cnt_updated = 0;
                    px = 0;
                    py = 0;
                    termid = 0;
                    subj = "";
                }
            }
        };
        nar.memory.event.set(obs, true, Events.CycleEnd.class);
        nar.memory.event.set(obs, true, Events.ResetEnd.class);
    }
    
    String subj = ""; 
    int empty_cycles = 0;
    public boolean AddToMatrix(Task t) {
        Inheritance inh = (Inheritance) t.getTerm(); //channels receive inheritances
        String cur_subj = ((SetExt)inh.getSubject()).term[0].index_variable.toString();
        if(!cur_subj.equals(subj)) { //when subject changes, we start to collect from scratch,
            if(!subj.isEmpty()) { //but only if subj isn't empty
                step_start(); //flush to upper level what we so far had
            }
            cnt_updated = 0; //this way multiple matrices can be processed by the same vision channel
            updated = new boolean[height][width];
            subj = cur_subj;
        }
        HadNewInput = true;
        empty_cycles = 0;
        int x = t.getTerm().term_indices[2];
        int y = t.getTerm().term_indices[3];
        if(!updated[y][x]) {
            inputs[y][x] = t.sentence.getTruth().getFrequency();
            cnt_updated++;
            updated[y][x] = true;
        } else { //a second value, so take average of frequencies
                 //revision wouldn't be proper as each sensory point can just have 1 vote
            inputs[y][x] = (inputs[y][x]+t.sentence.getTruth().getFrequency()) / 2.0f;
        }
        if(cnt_updated == height*width) {
            return true;
        }
        return false;
    }
    
    boolean isEternal = false; //don't use increasing ID if eternal
    @Override
    public NAR addInput(Task t) {
        isEternal = t.sentence.isEternal();
        if(AddToMatrix(t)) //new data complete
            step_start();
        return nar;
    }
    
    int termid=0;
    @Override
    public void step_start()
    {
        cnt_updated = 0;
        HadNewInput = false;
        termid++;
        Term V;
        if(isEternal) {
            V = SetExt.make(new Term(subj));
        } else {
            V = SetExt.make(new Term(subj+termid));   
        }
        //the visual space has to be a copy.
        float[][] cpy = new float[height][width];
        for(int i=0;i<height;i++) {
            for(int j=0;j<width;j++) {
                cpy[i][j] = (float) inputs[i][j];
            }
        }
        updated = new boolean[height][width];
        inputs = new double[height][width];
        subj = "";
        VisualSpace vspace = new VisualSpace(nar, cpy, py, px, height, width);
        //attach sensation to term:
        V.imagination = vspace;
        Stamp stamp = isEternal ? new Stamp(nar.memory, Tense.Eternal) : new Stamp(nar.memory);
        
        Sentence s = new Sentence(Inheritance.make(V, this.label), 
                                                   Symbols.JUDGMENT_MARK, 
                                                   new TruthValue(1.0f,
                                                   DEFAULT_OUTPUT_CONFIDENCE), 
                                                   stamp);
        Task T = new Task(s, new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
                                             Parameters.DEFAULT_JUDGMENT_DURABILITY,
                                             BudgetFunctions.truthToQuality(s.truth)), true);
        this.results.add(T);//feeds results into "upper" sensory channels:
        this.step_finished(); 
    }
}
