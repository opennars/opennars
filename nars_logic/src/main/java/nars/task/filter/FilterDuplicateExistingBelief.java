package nars.task.filter;

import nars.Premise;
import nars.Symbols;
import nars.concept.Concept;
import nars.task.Task;

/**
 * Prevent a duplicate belief from entering the system again
 */
public enum FilterDuplicateExistingBelief {
    ; //implements DerivationFilter {

//    public final static String DUPLICATE = "DuplicateExistingBelief";
//
//
//
//    @Override public final String reject(final Premise nal, final Task task, final boolean solution, final boolean revised) {
//
//
//        //only process non-solution judgments
//        if (solution || !task.isJudgment())
//            return VALID;
//
//        return isUniqueBelief(nal, task) ? VALID : DUPLICATE;
//    }

    public static boolean isUniqueBelief(Premise nal, Task t) {

        Concept c = nal.concept(t.term());

        if (c == null) {
            //concept doesnt even exist so this is not a duplciate of anything
            return true;
        }


        switch (t.getPunctuation()) {
            case Symbols.JUDGMENT:
                return !c.getBeliefs().contains(t);
            case Symbols.GOAL:
                return !c.getGoals().contains(t);
            default:
                return false;
        }

    }

}
