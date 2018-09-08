/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.plugin.mental;

import java.io.Serializable;
import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.io.Symbols;
import org.opennars.language.Inheritance;
import org.opennars.language.SetInt;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class Emotions implements Plugin, Serializable {

    public volatile float HAPPY_EVENT_HIGHER_THRESHOLD=0.75f;
    public volatile float HAPPY_EVENT_LOWER_THRESHOLD=0.25f;
    public volatile float BUSY_EVENT_HIGHER_THRESHOLD=0.9f; //1.6.4, step by step^, there is already enough new things ^^
    public volatile float BUSY_EVENT_LOWER_THRESHOLD=0.1f;
    public volatile int CHANGE_STEPS_DEMANDED = 1000;

    public double lasthappy=0.5;
    public long last_happy_time = 0;
    public long last_busy_time = 0;


    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;



    public void setHAPPY_EVENT_HIGHER_THRESHOLD(double val) {
        this.HAPPY_EVENT_HIGHER_THRESHOLD = (float) val;
    }
    public double getHAPPY_EVENT_HIGHER_THRESHOLD() {
        return HAPPY_EVENT_HIGHER_THRESHOLD;
    }

    public void setHAPPY_EVENT_LOWER_THRESHOLD(double val) {
        this.HAPPY_EVENT_LOWER_THRESHOLD = (float) val;
    }
    public double getHAPPY_EVENT_LOWER_THRESHOLD() {
        return HAPPY_EVENT_LOWER_THRESHOLD;
    }

    public void setBUSY_EVENT_HIGHER_THRESHOLD(double val) {
        this.BUSY_EVENT_HIGHER_THRESHOLD = (float) val;
    }
    public double getBUSY_EVENT_HIGHER_THRESHOLD() {
        return BUSY_EVENT_HIGHER_THRESHOLD;
    }

    public void setBUSY_EVENT_LOWER_THRESHOLD(double val) {
        this.BUSY_EVENT_LOWER_THRESHOLD = (float) val;
    }
    public double getBUSY_EVENT_LOWER_THRESHOLD() {
        return BUSY_EVENT_LOWER_THRESHOLD;
    }

    public void setCHANGE_STEPS_DEMANDED(double val) {
        this.CHANGE_STEPS_DEMANDED = (int) val;
    }
    public double getCHANGE_STEPS_DEMANDED() {
        return CHANGE_STEPS_DEMANDED;
    }


    public void resetEmotions() {
        this.happy = 0.5f;
        this.busy = 0.5f;
        this.lastbusy = 0.5f;
        this.lasthappy = 0.5f;
    }
    
    public Emotions(){}
    public Emotions(float HAPPY_EVENT_LOWER_THRESHOLD, float HAPPY_EVENT_HIGHER_THRESHOLD,
                    float BUSY_EVENT_LOWER_THRESHOLD, float BUSY_EVENT_HIGHER_THRESHOLD, int CHANGE_STEPS_DEMANDED) {
        this.BUSY_EVENT_LOWER_THRESHOLD = BUSY_EVENT_LOWER_THRESHOLD;
        this.BUSY_EVENT_HIGHER_THRESHOLD = BUSY_EVENT_HIGHER_THRESHOLD;
        this.HAPPY_EVENT_LOWER_THRESHOLD = HAPPY_EVENT_LOWER_THRESHOLD;
        this.HAPPY_EVENT_HIGHER_THRESHOLD = HAPPY_EVENT_HIGHER_THRESHOLD;
        this.CHANGE_STEPS_DEMANDED = CHANGE_STEPS_DEMANDED;
    }

    public void set(final float happy, final float busy) {
        this.happy = happy;
        this.busy = busy;
    }

    public float happy() {
        return happy;
    }

    public float busy() {
        return busy;
    }


    public void adjustSatisfaction(final float newValue, final float weight, final DerivationContext nal) {
        
        //        float oldV = happyValue;
        happy += newValue * weight;
        happy /= 1.0f + weight;
        
        if(!enabled) {
            return;
        }
        
        float frequency=-1;
        if(Math.abs(happy-lasthappy) > CHANGE_THRESHOLD && nal.time.time()-last_happy_time > CHANGE_STEPS_DEMANDED) {
            if(happy > HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy <= HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(happy < HAPPY_EVENT_LOWER_THRESHOLD && lasthappy >= HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            lasthappy=happy;
            last_happy_time = nal.time.time();
        }
        
        if(frequency!=-1) { //ok lets add an event now
            final Term predicate=SetInt.make(new Term("satisfied"));
            final Term subject=Term.SELF;
            final Inheritance inh=Inheritance.make(subject, predicate);
            final TruthValue truth=new TruthValue(happy,nal.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, nal.narParameters);
            final Sentence s=new Sentence(inh,Symbols.JUDGMENT_MARK,truth,new Stamp(nal.time, nal.memory));
            s.stamp.setOccurrenceTime(nal.time.time());

            final BudgetValue budgetOfNewTask = new BudgetValue(nal.narParameters.DEFAULT_JUDGMENT_PRIORITY,
                                                                nal.narParameters.DEFAULT_JUDGMENT_DURABILITY,
                                                                BudgetFunctions.truthToQuality(truth),
                                                                nal.narParameters);
            final Task t = new Task(s, budgetOfNewTask, Task.EnumType.INPUT);

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
    public final double CHANGE_THRESHOLD = 0.25f;
    public void adjustBusy(final float newValue, final float weight, final DerivationContext nal) {

        busy += newValue * weight;
        busy /= (1.0f + weight);
        
        if(!enabled) {
            return;
        }
        
        float frequency=-1;
        if(Math.abs(busy-lastbusy) > CHANGE_THRESHOLD && nal.time.time()-last_busy_time > CHANGE_STEPS_DEMANDED) {
            if(busy > BUSY_EVENT_HIGHER_THRESHOLD && lastbusy <= BUSY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(busy < BUSY_EVENT_LOWER_THRESHOLD && lastbusy >= BUSY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            lastbusy=busy;
            last_busy_time = nal.time.time();
        }
        
        if(frequency!=-1) { //ok lets add an event now
            final Term predicate=SetInt.make(new Term("busy"));
            final Term subject=new Term("SELF");
            final Inheritance inh=Inheritance.make(subject, predicate);
            final TruthValue truth=new TruthValue(busy,nal.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, nal.narParameters);
            final Sentence s = new Sentence(
                inh,
                Symbols.JUDGMENT_MARK,
                truth,
                new Stamp(nal.time, nal.memory));
            s.stamp.setOccurrenceTime(nal.time.time());

            final BudgetValue budgetForNewTask = new BudgetValue(nal.narParameters.DEFAULT_JUDGMENT_PRIORITY,
                nal.narParameters.DEFAULT_JUDGMENT_DURABILITY,
                BudgetFunctions.truthToQuality(truth), nal.narParameters);
            final Task t = new Task(s, budgetForNewTask, Task.EnumType.INPUT);
            nal.addTask(t, "emotion");
        }
    }

    boolean enabled = false; //false means it needs to be retrieved using feelSatisfied / feelBusy instead
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        this.enabled = enabled;
        if(this.enabled) {
            resetEmotions();
        }
        return enabled;
    }
}
