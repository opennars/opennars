package nars.io.meter;

import java.io.Serializable;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.SetExt;
import nars.language.Term;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class EmotionMeter implements Serializable {

    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;

    public EmotionMeter() {
    }

    public EmotionMeter(float happy, float busy) {
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

    public double lasthappy=-1;
    public void adjustHappy(float newValue, float weight, NAL nal) {
        //        float oldV = happyValue;
        happy += newValue * weight;
        happy /= 1.0f + weight;
        
        if(lasthappy!=-1) {
            float frequency=-1;
            if(happy>Parameters.HAPPY_EVENT_HIGHER_THRESHOLD && lasthappy<=Parameters.HAPPY_EVENT_HIGHER_THRESHOLD) {
                frequency=1.0f;
            }
            if(happy<Parameters.HAPPY_EVENT_LOWER_THRESHOLD && lasthappy>=Parameters.HAPPY_EVENT_LOWER_THRESHOLD) {
                frequency=0.0f;
            }
            if(frequency!=-1) { //ok lets add an event now
                Term predicate=SetExt.make(new Term("satisfied"));
                Term subject=new Term("SELF");
                Inheritance inh=Inheritance.make(subject, predicate);
                TruthValue truth=new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                Sentence s=new Sentence(inh,Symbols.JUDGMENT_MARK,truth,new Stamp(nal.memory));
                Task t=new Task(s,new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,Parameters.DEFAULT_JUDGMENT_DURABILITY,BudgetFunctions.truthToQuality(truth)));
                nal.addTask(t, "emotion");
                if(Parameters.REFLECT_META_HAPPY_GOAL) { //remind on the goal whenever happyness changes, should suffice for now
                    TruthValue truth2=new TruthValue(1.0f,Parameters.DEFAULT_GOAL_CONFIDENCE);
                    Sentence s2=new Sentence(inh,Symbols.GOAL_MARK,truth2,new Stamp(nal.memory));
                    Task t2=new Task(s,new BudgetValue(Parameters.DEFAULT_GOAL_PRIORITY,Parameters.DEFAULT_GOAL_DURABILITY,BudgetFunctions.truthToQuality(truth)));
                    nal.addTask(t2, "metagoal");
                }
            }
        }
        lasthappy=happy;
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void adjustBusy(float newValue, float weight) {
        //        float oldV = busyValue;
        busy += newValue * weight;
        busy /= (1.0f + weight);
        //        if (Math.abs(oldV - busyValue) > 0.1) {
        //            Record.append("BUSY: " + (int) (oldV*10.0) + " to " + (int) (busyValue*10.0) + "\n");
    }
}
