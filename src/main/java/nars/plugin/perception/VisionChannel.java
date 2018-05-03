/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    int px = 0;
    int py = 0;
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
    
    String subj = ""; 
    public boolean AddToMatrix(Task t) {
        Inheritance inh = (Inheritance) t.getTerm(); //channels receive inheritances
        String cur_subj = inh.getSubject().index_variable.toString();
        if(!cur_subj.equals(subj)) { //when subject changes, we start to collect from scratch,
            cnt_updated = 0; //this way multiple matrices can be processed by the same vision channel
            updated = new boolean[height][width];
            subj = cur_subj;
        }
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
    
    int termid=0;
    @Override
    public void step_start()
    {
        termid++;
        Term V = new Term(subj+termid);
        //the visual space has to be a copy.
        float[][] cpy = new float[height][width];
        for(int i=0;i<height;i++) {
            for(int j=0;j<width;j++) {
                cpy[i][j] = (float) inputs[i][j];
            }
        } 
        VisualSpace vspace = new VisualSpace(nar, cpy, py, px, height, width);
        //attach sensation to term:
        V.imagination = vspace;
        
        Sentence s = new Sentence(Inheritance.make(V, this.label), 
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
