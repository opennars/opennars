package nars.meter;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetInt;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.mental.InternalExperience;
import nars.op.mental.consider;
import nars.op.mental.remind;
import nars.util.meter.event.DoubleMeter;

import java.io.Serializable;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class EmotionMeter implements Serializable {

    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;

    public final DoubleMeter happyMeter = new DoubleMeter("happy");
    public final DoubleMeter busyMeter = new DoubleMeter("busy");

    public static final Atom satisfied = Atom.the("satisfied");


    public EmotionMeter() {
    }

    public EmotionMeter(float happy, float busy) {
        set(happy, busy);
    }

    public void set(float happy, float busy) {
        this.happy = happy;
        this.busy = busy;
        update();
    }

    public float happy() {
        return happy;
    }

    public float busy() {
        return busy;
    }

    public double lasthappy=-1;
    
    public void happy(final float newValue, final Task task, final NAL nal) {
        //        float oldV = happyValue;

        final float weight = task.getPriority();
        happy += newValue * weight;
        happy /= 1.0f + weight;
        
        if(lasthappy!=-1) {
            float frequency=-1;
            if(happy> Global.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy<=Global.HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(happy<Global.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy>=Global.HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            if(frequency!=-1) { //ok lets add an event now
                Term predicate= SetInt.make(satisfied);
                Term subject= nal.self();
                Inheritance inh=Inheritance.make(subject, predicate);
                Truth truth=new DefaultTruth(1.0f,Global.DEFAULT_JUDGMENT_CONFIDENCE);
                Sentence s=new Sentence(inh, Symbols.JUDGMENT, truth, nal.newStamp(task.getStamp(), nal.time()));

                nal.deriveTask(
                        new Task(s,
                            new Budget(Global.DEFAULT_JUDGMENT_PRIORITY,Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth)),
                        task),
                        false, false, "emotion"
                );

                if(Global.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now

                    Truth truth2=new DefaultTruth(1.0f,Global.DEFAULT_GOAL_CONFIDENCE);

                    Sentence s2=new Sentence(inh,Symbols.GOAL,truth2,
                            nal.newStampNow(task));


                    nal.deriveTask(
                            new Task(s2,
                                    new Budget(Global.DEFAULT_GOAL_PRIORITY,Global.DEFAULT_GOAL_DURABILITY,BudgetFunctions.truthToQuality(truth2)),
                            task),
                            false, true, "metagoal"
                    );

                    //this is a good candidate for innate belief for consider and remind:

                    if(InternalExperience.enabled && Global.CONSIDER_REMIND) {
                        Operation op_consider= Operation.make(consider.consider, Product.make(inh));
                        Operation op_remind = Operation.make(remind.remind, Product.make(inh));
                        Operation[] op=new Operation[2];
                        op[0]=op_remind; //order important because usually reminding something
                        op[1]=op_consider; //means it has good chance to be considered after
                        for(Operation o : op) {

                            nal.deriveTask(
                                    nal.memory.task(o).parent(task).judgment().present().truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                            .budgetScaled(InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL, InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL)
                                            .get(),
                                    false, true,
                                    "internal experience for consider and remind"
                            );
                        }
                    }
                }
            }
        }
        lasthappy=happy;
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void busy(NAL nal) {
        busy(nal.getCurrentTask(), nal);
    }

    public double lastbusy=-1;
    public void busy(Task cause, NAL nal) {
        if(lastbusy!=-1) {
            float frequency=-1;
            if(busy>Global.BUSY_EVENT_HIGHER_THRESHOLD && lastbusy<=Global.BUSY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(busy<Global.BUSY_EVENT_LOWER_THRESHOLD && lastbusy>=Global.BUSY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            if(frequency!=-1) { //ok lets add an event now
                Term predicate=SetInt.make(Atom.the("busy"));
                Term subject=Atom.the("SELF");
                Inheritance inh=Inheritance.make(subject, predicate);
                Truth truth=new DefaultTruth(1.0f,Global.DEFAULT_JUDGMENT_CONFIDENCE);
                Sentence s=new Sentence(inh,Symbols.JUDGMENT,truth,
                        nal.newStampNow(cause));

                nal.deriveTask(
                        new Task(s, new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth))),
                        false, true, "emotion");
            }
        }
        lastbusy=busy;
    }
    
    protected void update() {
        happyMeter.set(happy);
        busyMeter.set(busy);
    }
}
