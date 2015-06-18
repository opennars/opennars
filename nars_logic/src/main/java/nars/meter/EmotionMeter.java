package nars.meter;

import nars.Global;
import nars.nal.NAL;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetInt;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.op.mental.InternalExperience;
import nars.op.mental.consider;
import nars.op.mental.remind;
import nars.util.meter.event.DoubleMeter;

import java.io.Serializable;

/**
 * emotional value; self-felt internal mental states; variables used to record emotional values
 */
public class EmotionMeter implements Serializable {

    /**
     * average desire-value
     */
    private float happy;
    /**
     * average priority
     */
    private float busy;

    public final DoubleMeter happyMeter = new DoubleMeter("happy");
    public final DoubleMeter busyMeter = new DoubleMeter("busy");

    public static final Atom satisfied = Atom.the("satisfied");
    final static Compound satisfiedSetInt = SetInt.make(satisfied);

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

    public double lasthappy = -1;

    public void happy(final float newValue, final Task task, final NAL nal) {
        //        float oldV = happyValue;

        final float weight = task.getPriority();
        happy += newValue * weight;
        happy /= 1.0f + weight;

        if (lasthappy != -1) {
            float frequency = -1;
            if (happy > Global.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy <= Global.HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency = 1.0f;
            }
            if (happy < Global.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy >= Global.HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency = 0.0f;
            }
            if (frequency != -1) { //ok lets add an event now

                Inheritance inh = Inheritance.make(nal.self(), satisfiedSetInt);

                nal.deriveSingle(
                        nal.newTask(inh)
                                .judgment()
                                .truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                                .parent(task, nal.time())
                                .reason("emotion")
                );

                if (Global.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now

                    //TODO convert to fluent format

                    nal.deriveSingle(
                            nal.newTask(inh).goal().truth(1.0f, Global.DEFAULT_GOAL_CONFIDENCE).parent(task).occurrNow()
                                    .budget(
                                            Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY)
                                    .parent(task).reason("metagoal")
                    );

                    //this is a good candidate for innate belief for consider and remind:

                    if (InternalExperience.enabled && Global.CONSIDER_REMIND) {
                        Operation op_consider = Operation.make(consider.consider, Product.only(inh));
                        Operation op_remind = Operation.make(remind.remind, Product.only(inh));

                        //order important because usually reminding something
                        //means it has good chance to be considered after
                        for (Operation o : new Operation[]{op_remind, op_consider}) {

                            nal.deriveSingle(
                                    nal.newTask(o).parent(task).judgment().present().truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                            .budgetScaled(InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL, InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL)
                                            .reason("internal experience for consider and remind")
                            );
                        }
                    }
                }
            }
        }
        lasthappy = happy;
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void busy(NAL nal) {
        busy(nal.getCurrentTask(), nal);
    }

    public double lastbusy = -1;

    public void busy(Task cause, NAL nal) {
        if (lastbusy != -1) {
            float frequency = -1;
            if (busy > Global.BUSY_EVENT_HIGHER_THRESHOLD && lastbusy <= Global.BUSY_EVENT_HIGHER_THRESHOLD) {
                frequency = 1.0f;
            }
            if (busy < Global.BUSY_EVENT_LOWER_THRESHOLD && lastbusy >= Global.BUSY_EVENT_LOWER_THRESHOLD) {
                frequency = 0.0f;
            }
            if (frequency != -1) { //ok lets add an event now
                final Inheritance busyTerm = Inheritance.make(nal.self(), SetInt.make(Atom.the("busy")));

                nal.deriveSingle(
                        nal.newTask(busyTerm).judgment().truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE).parent(cause).occurrNow()
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY).
                                parent(cause).reason("emotion"));

            }
        }
        lastbusy = busy;
    }

    protected void update() {
        happyMeter.set(happy);
        busyMeter.set(busy);
    }
}
