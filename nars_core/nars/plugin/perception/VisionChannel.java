package nars.plugin.perception;

import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.main.NAR;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.Term;
import nars.main.Parameters;

public class VisionChannel extends SensoryChannel {
    double[][] inputs;
    boolean[][] updated;
    int cnt_updated = 0;
    int height = 0;
    int width = 0;
    Term label;
    NAR nar;
    public VisionChannel(Term label, NAR nar, SensoryChannel reportResultsTo, int width, int height) {
        super(nar,reportResultsTo);
        this.nar = nar;
        this.height = height;
        this.width = width;
        this.label = label;
        inputs = new double[height][width];
        updated = new boolean[height][width];
    }
    
    public boolean AddToMatrix(Task t) {
        int x = t.getTerm().term_indices[2];
        int y = t.getTerm().term_indices[3];
        inputs[y][x] = t.sentence.getTruth().getFrequency();
        if(!updated[y][x]) {
            cnt_updated++;
            updated[y][x] = true;
        }
        if(cnt_updated == height*width) {
            cnt_updated = 0;
            updated = new boolean[height][width];
            return true;
        }
        return false;
    }
    
    @Override
    public NAR addInput(Task t) {
        if(AddToMatrix(t)) //new data complete
            step_start();
        return nar;
    }
    
    @Override
    public void step_start()
    {
        Sentence s = new Sentence(Inheritance.make(new Term("A"), this.label), 
                                                   Symbols.JUDGMENT_MARK, 
                                                   new TruthValue(1.0f,
                                                   Parameters.DEFAULT_JUDGMENT_CONFIDENCE), 
                                                   new Stamp(nar.memory));
        Task T = new Task(s, new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
                                             Parameters.DEFAULT_JUDGMENT_DURABILITY,
                                             BudgetFunctions.truthToQuality(s.truth)), true);
        this.results.add(T);//feeds results into "upper" sensory channels:
        this.step_finished(); 
    }
    
}
