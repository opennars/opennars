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
package org.opennars.plugin.perception;

import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CycleEnd;
import org.opennars.io.events.Events.ResetEnd;
import org.opennars.language.Inheritance;
import org.opennars.language.SetExt;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;

public class VisionChannel extends SensoryChannel  {
    public final float DEFAULT_OUTPUT_CONFIDENCE = 0.1f;
    double[][] inputs;
    boolean[][] updated;
    int cnt_updated = 0;
    int px = 0;
    int py = 0;
    final Term label;
    final Nar nar;
    boolean HadNewInput = false; //only generate frames if at least something was input since last "commit to Nar"
    public final EventEmitter.EventObserver obs;
    public VisionChannel(final Term label, final Nar nar, final SensoryChannel reportResultsTo, final int width, final int height, final int duration) {
        super(nar,reportResultsTo, width, height, duration, label);
        this.nar = nar;
        this.label = label;
        inputs = new double[height][width];
        updated = new boolean[height][width];
        obs = (ev, a) -> {
            if(HadNewInput && ev == CycleEnd.class) {
                empty_cycles++;
                if(empty_cycles > duration) { //a deadline, pixels can't appear more than duration after each other
                    step_start(); //so we know we can input, not only when all pixels were re-set.
                }
            }
            else
            if(ev == ResetEnd.class) {
                resetChannel();
            }
        };
        nar.memory.event.set(obs, true, Events.CycleEnd.class);
        nar.memory.event.set(obs, true, Events.ResetEnd.class);
    }
    
    private void resetChannel() {
        inputs = new double[height][width];
        updated = new boolean[height][width];
        cnt_updated = 0;
        px = 0;
        py = 0;
        termid = 0;
        subj = "";
    }
    
    String subj = ""; 
    int empty_cycles = 0;
    public boolean AddToMatrix(final Task t) {
        final Inheritance inh = (Inheritance) t.getTerm(); //channels receive inheritances
        final String cur_subj = ((SetExt)inh.getSubject()).term[0].index_variable;
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
        final int x = t.getTerm().term_indices[2];
        final int y = t.getTerm().term_indices[3];
        if(!updated[y][x]) {
            inputs[y][x] = t.sentence.getTruth().getFrequency();
            cnt_updated++;
            updated[y][x] = true;
        } else { //a second value, so take average of frequencies
                 //revision wouldn't be proper as each sensory point can just have 1 vote
            inputs[y][x] = (inputs[y][x]+t.sentence.getTruth().getFrequency()) / 2.0f;
        }
        return cnt_updated == height * width;
    }
    
    boolean isEternal = false; //don't use increasing ID if eternal
    @Override
    public Nar addInput(final Task t) {
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
        final Term V;
        if(isEternal) {
            V = SetExt.make(new Term(subj));
        } else {
            V = SetExt.make(new Term(subj+termid));   
        }
        //the visual space has to be a copy.
        final float[][] cpy = new float[height][width];
        for(int i=0;i<height;i++) {
            for(int j=0;j<width;j++) {
                cpy[i][j] = (float) inputs[i][j];
            }
        }
        updated = new boolean[height][width];
        inputs = new double[height][width];
        subj = "";
        final VisualSpace vspace = new VisualSpace(nar, cpy, py, px, height, width);
        //attach sensation to term:
        V.imagination = vspace;
        final Stamp stamp = isEternal ? new Stamp(nar.memory, Tense.Eternal) : new Stamp(nar.memory);
        
        final Sentence s = new Sentence(Inheritance.make(V, this.label),
                                                   Symbols.JUDGMENT_MARK, 
                                                   new TruthValue(1.0f,
                                                   DEFAULT_OUTPUT_CONFIDENCE), 
                                                   stamp);

        final BudgetValue budgetForNewTask = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
            Parameters.DEFAULT_JUDGMENT_DURABILITY,
            BudgetFunctions.truthToQuality(s.truth));
        final Task newTask = new Task(s, budgetForNewTask, Task.EnumType.INPUT);

        this.results.add(newTask);//feeds results into "upper" sensory channels:
        this.step_finished(); 
    }
    
    public double getWidth() {
        return this.width;
    }
    public void setWidth(double val) {
        this.width = (int) val;
        this.resetChannel();
    }
    public double getHeight() {
        return this.width;
    }
    public void setHeight(double val) {
        this.height = (int) val;
        this.resetChannel();
    }
}
