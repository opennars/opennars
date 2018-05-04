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
package org.opennars.plugin.mental;

import org.opennars.main.NAR;
import org.opennars.main.Parameters;
import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.inference.BudgetFunctions;
import org.opennars.io.Symbols;
import org.opennars.language.Inheritance;
import org.opennars.language.SetInt;
import org.opennars.language.Term;
import org.opennars.plugin.Plugin;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class Emotions implements Plugin {

    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;

    public void resetEmotions() {
        this.happy = 0.5f;
        this.busy = 0.5f;
        this.lastbusy = 0.5f;
        this.lasthappy = 0.5f;
    }
    
    public Emotions() {}

    public Emotions(float happy, float busy) {
        set(happy, busy);
    }

    public void set(float happy, float busy) {
        this.happy = happy;
        this.busy = busy;
    }

    public float happy() {
        return happy;
    }

    public float busy() {
        return busy;
    }

    public double lasthappy=0.5;
    public long last_happy_time = 0;
    public long last_busy_time = 0;
    public long change_steps_demanded = 1000;
    public void adjustSatisfaction(float newValue, float weight, DerivationContext nal) {
        
        //        float oldV = happyValue;
        happy += newValue * weight;
        happy /= 1.0f + weight;
        
        if(!enabled) {
            return;
        }
        
        float frequency=-1;
        if(Math.abs(happy-lasthappy) > CHANGE_THRESHOLD && nal.memory.time()-last_happy_time > change_steps_demanded) {
            if(happy>Parameters.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy<=Parameters.HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(happy<Parameters.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy>=Parameters.HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            lasthappy=happy;
            last_happy_time = nal.memory.time();
        }
        
        if(frequency!=-1) { //ok lets add an event now
            Term predicate=SetInt.make(new Term("satisfied"));
            Term subject=Term.SELF;
            Inheritance inh=Inheritance.make(subject, predicate);
            TruthValue truth=new TruthValue(happy,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
            Sentence s=new Sentence(inh,Symbols.JUDGMENT_MARK,truth,new Stamp(nal.memory));
            s.stamp.setOccurrenceTime(nal.memory.time());
            Task t=new Task(s,new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,BudgetFunctions.truthToQuality(truth)), true);
            nal.addTask(t, "emotion");
            /*if(Parameters.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now
                TruthValue truth2=new TruthValue(1.0f,Parameters.DEFAULT_GOAL_CONFIDENCE);
                Sentence s2=new Sentence(inh,Symbols.GOAL_MARK,truth2,new Stamp(nal.memory));
                s2.stamp.setOccurrenceTime(nal.memory.time());
                Task t2=new Task(s2,new BudgetValue(Parameters.DEFAULT_GOAL_PRIORITY,Parameters.DEFAULT_GOAL_DURABILITY,BudgetFunctions.truthToQuality(truth2)));
                nal.addTask(t2, "metagoal");
                //this is a good candidate for innate belief for consider and remind:
                Operator consider=nal.memory.getOperator("^consider");
                Operator remind=nal.memory.getOperator("^remind");
                Term[] arg=new Term[1];
                arg[0]=inh;
                if(InternalExperience.enabled && Parameters.CONSIDER_REMIND) {
                    Operation op_consider=Operation.make(consider, arg, true);
                    Operation op_remind=Operation.make(remind, arg, true);
                    Operation[] op=new Operation[2];
                    op[0]=op_remind; //order important because usually reminding something
                    op[1]=op_consider; //means it has good chance to be considered after
                    for(Operation o : op) {
                        TruthValue truth3=new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                        Sentence s3=new Sentence(o,Symbols.JUDGMENT_MARK,truth3,new Stamp(nal.memory));
                        s3.stamp.setOccurrenceTime(nal.memory.time());

                        //INTERNAL_EXPERIENCE_DURABILITY_MUL
                        BudgetValue budget=new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,BudgetFunctions.truthToQuality(truth3));
                        budget.setPriority(budget.getPriority()*InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL);
                        budget.setDurability(budget.getPriority()*InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL);
                        Task t3=new Task(s3,budget);
                        nal.addTask(t3, "internal experience for consider and remind");
                    }
                }
            }*/
        }
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }
    
    public double lastbusy=0.5;
    public double CHANGE_THRESHOLD = 0.25f;
    public void adjustBusy(float newValue, float weight, DerivationContext nal) {

        busy += newValue * weight;
        busy /= (1.0f + weight);
        
        if(!enabled) {
            return;
        }
        
        float frequency=-1;
        if(Math.abs(busy-lastbusy) > CHANGE_THRESHOLD && nal.memory.time()-last_busy_time > change_steps_demanded) {
            if(busy>Parameters.BUSY_EVENT_HIGHER_THRESHOLD && lastbusy<=Parameters.BUSY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(busy<Parameters.BUSY_EVENT_LOWER_THRESHOLD && lastbusy>=Parameters.BUSY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            lastbusy=busy;
            last_busy_time = nal.memory.time();
        }
        
        if(frequency!=-1) { //ok lets add an event now
            Term predicate=SetInt.make(new Term("busy"));
            Term subject=new Term("SELF");
            Inheritance inh=Inheritance.make(subject, predicate);
            TruthValue truth=new TruthValue(busy,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
            Sentence s = new Sentence(
                inh,
                Symbols.JUDGMENT_MARK,
                truth,
                new Stamp(nal.memory));
            s.stamp.setOccurrenceTime(nal.memory.time());
            Task t=new Task(s,
                            new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
                                            Parameters.DEFAULT_JUDGMENT_DURABILITY,
                                            BudgetFunctions.truthToQuality(truth)),
                            true);
            nal.addTask(t, "emotion");
        }
    }

    boolean enabled = false; //false means it needs to be retrieved using feelSatisfied / feelBusy instead
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        this.enabled = enabled;
        return enabled;
    }
}
