package nars.nal.nal8;

import nars.Symbols;
import nars.nal.concept.Concept;

/**
 * Created by me on 5/20/15.
 */
public class DesireThresholdExecutivePredicate extends DesireThresholdExecutive {

    public final static nars.nal.nal8.DesireThresholdExecutivePredicate the = new nars.nal.nal8.DesireThresholdExecutivePredicate();

    @Override
    public boolean decide(Concept c, Operation task) {
        if (task.getTask().getPunctuation() == Symbols.QUESTION)
            return true;

        return super.decide(c, task);
    }


}
