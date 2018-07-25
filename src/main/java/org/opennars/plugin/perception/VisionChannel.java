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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.io.Narsese;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CycleEnd;
import org.opennars.io.events.Events.ResetEnd;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.operator.NullOperator;
import org.opennars.operator.Operator;

public class VisionChannel extends SensoryChannel  {
    public float defaultOutputConfidence = 0.5f;
    public int nPrototypes = 0;
    public ArrayList<Prototype> prototypes;
    class Prototype {
        int observationCount;
        Task task;
        public Prototype(Task t) {
            this.observationCount = 1; //as the task itself is a case
            this.task = t;
        }
        public void incrementObservationCount() {
            this.observationCount++;
        }
        public int getObservationCount() {
            return this.observationCount;
        }
    }
    double[][] inputs;
    boolean[][] updated;
    int cnt_updated = 0;
    int px = 0;
    int py = 0;
    final Term label;
    final Nar nar;
    boolean HadNewInput = false; //only generate frames if at least something was input since last "commit to Nar"
    public final EventEmitter.EventObserver obs;

    public VisionChannel(final String label, final Reasoner nar, final Reasoner reportResultsTo, final int width, final int height, final int duration,
            float defaultOutputConfidence, int nPrototypes) {
        super((Nar)nar,(SensoryChannel)reportResultsTo, width, height, duration, SetInt.make(new Term(label)));
        this.nar = (Nar)nar;
        this.label = SetInt.make(new Term(label));
        this.defaultOutputConfidence = defaultOutputConfidence;
        this.nPrototypes = nPrototypes;
        prototypes = new ArrayList<Prototype>();
        inputs = new double[height][width];
        updated = new boolean[height][width];
        obs = (ev, a) -> {
            if(HadNewInput && ev == CycleEnd.class) {
                empty_cycles++;
                if(empty_cycles > duration) { //a deadline, pixels can't appear more than duration after each other
                    step_start(nar); //so we know we can input, not only when all pixels were re-set.
                }
            }
            else
            if(ev == ResetEnd.class) {
                resetChannel();
            }
        };
        
    }
    
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        n.memory.event.set(obs, enabled, Events.CycleEnd.class);
        n.memory.event.set(obs, enabled, Events.ResetEnd.class);
        return true;
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
    public boolean AddToMatrix(final Task t, final Timable time) {
        final Inheritance inh = (Inheritance) t.getTerm(); //channels receive inheritances
        final String cur_subj = ((SetExt)inh.getSubject()).term[0].index_variable;
        if(!cur_subj.equals(subj)) { //when subject changes, we start to collect from scratch,
            if(!subj.isEmpty()) { //but only if subj isn't empty
                step_start(time); //flush to upper level what we so far had
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
    public Nar addInput(final Task t, final Timable time) {
        isEternal = t.sentence.isEternal();
        if(AddToMatrix(t, time)) //new data complete
            step_start(time);
        return nar;
    }
    
    int termid=0;
    @Override
    public void step_start(final Timable time)
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
        Stamp stamp = isEternal ? new Stamp(time, nar.memory, Tense.Eternal) : new Stamp(time, nar.memory);
        
        final Sentence s = new Sentence(Inheritance.make(V, this.label),
                                                   Symbols.JUDGMENT_MARK, 
                                                   new TruthValue(1.0f,
                                                   defaultOutputConfidence, nar.narParameters), 
                                                   stamp);

        final BudgetValue budgetForNewTask = new BudgetValue(nar.narParameters.DEFAULT_JUDGMENT_PRIORITY,
            nar.narParameters.DEFAULT_JUDGMENT_DURABILITY,
            BudgetFunctions.truthToQuality(s.truth), nar.narParameters);
        final Task newTask = new Task(s, budgetForNewTask, Task.EnumType.INPUT);
        newTask.setElemOfSequenceBuffer(true);
        if(this.nPrototypes == 0) { //report directly to NARS as there are no prototypes
            this.results.add(newTask);//feeds results into "upper" sensory channels:
            this.step_finished(time);
        } else {
            //if there is no other prototype yet we return
            if(this.prototypes.isEmpty()) {
                this.prototypes.add(new Prototype(newTask));
                this.results.add(newTask);//feeds results into "upper" sensory channels:
                this.step_finished(time);
            } else {
                //1. determine the most similar prototype
                float similarity = 0;
                Prototype best = null;
                for(Prototype p : prototypes) {
                    Inheritance inh = (Inheritance) p.task.getTerm();
                    float simCur = inh.getSubject().imagination.AbductionOrComparisonTo(vspace, true).getExpectation();
                    if(simCur > similarity) {
                        best = p;
                        similarity = simCur;
                    }
                }
                //2. replace the rarest seen prototype with the new prototype when full
                // else just add it
                if(prototypes.size() >= this.nPrototypes) {
                    int lowestValue = Integer.MAX_VALUE;
                    int lowestIndex = -1;
                    for(int i=0;i<prototypes.size();i++) {
                        Prototype cur = prototypes.get(i);
                        if(cur.getObservationCount() < lowestValue) {
                            lowestValue = i;
                            lowestIndex = i;
                        }
                    }
                    if(similarity < 0.8f) {
                        prototypes.set(lowestIndex, new Prototype(newTask));
                    }
                } else {
                    if(similarity < 0.8f) {
                        prototypes.add(new Prototype(newTask));
                    }
                }

                //3. build spatial relation to previous
                if(lastPrototype != null) {
                    //int oldFocusX = this.focusX;
                    //int oldFocusY = this.focusY;
                    VisualSpace lastSpace = (VisualSpace) ((Inheritance) lastPrototype.task.getTerm()).getSubject().imagination;
                    VisualSpace newSpace = (VisualSpace) ((Inheritance) best.task.getTerm()).getSubject().imagination;
                    int oldFocusX = lastSpace.px;
                    int oldFocusY = lastSpace.py;
                    int newFocusX = newSpace.px;
                    int newFocusY = newSpace.py;
                    Operator selectedX = null;
                    Operator selectedY = null;
                    if(newFocusX > oldFocusX) {
                        selectedX = newSpace.ops.get("right");
                    } else {
                        selectedX = newSpace.ops.get("left");
                    }
                    if(newFocusY > oldFocusY) {
                        selectedY = newSpace.ops.get("up");
                    } else {
                        selectedY = newSpace.ops.get("down");
                    }
                    try {
                        //timing to make sure procedure learning observes the operation after the last prototype
                        this.nar.cycles(this.nar.narParameters.DURATION);
                        Task taskX = new Narsese(this.nar).parseTask("("+selectedX.name()+",{SELF}). :|:");
                        Task taskY = new Narsese(this.nar).parseTask("("+selectedY.name()+",{SELF}). :|:");
                        taskX.setElemOfSequenceBuffer(true);
                        taskY.setElemOfSequenceBuffer(true);
                        this.results.add(taskX);
                        this.results.add(taskY);                  
                        this.step_finished(time);
                        //timing to make sure procedure learning observes the operation before the new prototype
                        this.nar.cycles(this.nar.narParameters.DURATION);
                        //stamp has to be re-built according to new timing
                        if(!isEternal) {
                            stamp = new Stamp(time, nar.memory);
                        }
                    } catch (Narsese.InvalidInputException ex) {
                        Logger.getLogger(VisionChannel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                //4. add the best prototype as identified sensation
                //but with current time stamp
                Sentence bestSentence = new Sentence(best.task.getTerm(),
                                                     best.task.sentence.punctuation,
                                                     best.task.sentence.truth.clone(),
                                                     stamp.clone());
                final Task bestTask = new Task(bestSentence, best.task.budget.clone(), Task.EnumType.INPUT);
                bestTask.setElemOfSequenceBuffer(true);
                this.results.add(bestTask);//feeds results into "upper" sensory channels:
                this.step_finished(time);
                //5. reward the best prototype and set as the last observed one
                best.incrementObservationCount();
                lastPrototype = best;
            }
        }
    }
    
    Prototype lastPrototype = null;
    
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
    
    public void setFocus(int px, int py) {
        this.px = px;
        this.py = py;
    }
}
