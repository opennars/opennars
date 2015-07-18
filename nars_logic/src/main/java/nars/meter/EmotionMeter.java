package nars.meter;

import nars.Global;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetInt;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.op.mental.InternalExperience;
import nars.op.mental.consider;
import nars.op.mental.remind;
import nars.process.NAL;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
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

    /*public EmotionMeter(float happy, float busy) {
        set(happy, busy);
    }*/

    public void set(float happy, float busy) {
        this.happy = happy;
        this.busy = busy;
        commit();
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
        float happy = this.happy;

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
            if ((frequency != -1) && (nal.nal(7))) { //ok lets add an event now

                Inheritance inh = Inheritance.make(nal.self(), satisfiedSetInt);

                nal.deriveSingle(
                        nal.newTask(inh)
                                .judgment()
                                .truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                .budget(task.getBudget(), Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                                .parent(task)
                                .occurrNow()
                                .reason("Happy")
                );

                if (Global.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now

                    //TODO convert to fluent format

                    nal.deriveSingle(
                            nal.newTask(inh).goal().truth(1.0f, Global.DEFAULT_GOAL_CONFIDENCE).parent(task)
                                    .occurrNow()
                                    .budget(
                                            Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY)
                                    .reason("Happy Metagoal")
                    );

                    //this is a good candidate for innate belief for consider and remind:

                    if (InternalExperience.enabled && Global.CONSIDER_REMIND) {
                        Operation op_consider = Operation.make(consider.consider, Product.only(inh));
                        Operation op_remind = Operation.make(remind.remind, Product.only(inh));

                        //order important because usually reminding something
                        //means it has good chance to be considered after
                        for (Operation o : new Operation[]{op_remind, op_consider}) {

                            nal.deriveSingle(
                                    nal.newTask(o).parent(task).judgment().occurrNow().truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                                            .budget(task.getBudget(), InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL, InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL)
                                            .reason("Happy Remind/Consider")
                            );
                        }
                    }
                }
            }
        }

        lasthappy = this.happy;

        this.happy = happy;

        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void busy(NAL nal) {
        busy(nal.getTask(), nal);
    }

    public double lastbusy = -1;

    protected void busy(Task cause, NAL nal) {

        float busy = nal.getTask().getPriority();

        if (lastbusy != -1) {
            float frequency = -1;
            if (busy > Global.BUSY_EVENT_HIGHER_THRESHOLD && lastbusy <= Global.BUSY_EVENT_HIGHER_THRESHOLD) {
                frequency = 1.0f;
            }
            if (busy < Global.BUSY_EVENT_LOWER_THRESHOLD && lastbusy >= Global.BUSY_EVENT_LOWER_THRESHOLD) {
                frequency = 0.0f;
            }
            if ((frequency != -1) && (nal.nal(7))) { //ok lets add an event now
                final Inheritance busyTerm = Inheritance.make(nal.self(), SetInt.make(Atom.the("busy")));

                nal.deriveSingle(
                        nal.newTask(busyTerm).judgment().truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE).parent(cause).occurrNow()
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY).
                                parent(cause).reason("Busy"));

            }
        }

        lastbusy = this.busy;

        this.busy = busy;


    }

    public void commit() {
        happyMeter.set(happy);
        busyMeter.set(busy);
    }

    public void clear() {
        set(0.5f, 0.5f);
    }
}
