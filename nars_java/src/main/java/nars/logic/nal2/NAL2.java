package nars.logic.nal2;

import nars.logic.BudgetFunctions;
import nars.logic.NAL;
import nars.logic.TruthFunctions;
import nars.logic.entity.*;
import nars.logic.nal1.Inheritance;
import nars.logic.nal5.Equivalence;

/**
 * Created by me on 1/13/15.
 */
public class NAL2 {
    /**
     * {<S --> P>, <P --> S} |- <S <-> p> Produce Similarity/Equivalence from a
     * pair of reversed Inheritance/Implication
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param nal Reference to the memory
     */
    public static void inferToSym(Sentence judgment1, Sentence judgment2, NAL nal) {
        Statement s1 = (Statement) judgment1.term;
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Term content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder());
        }
        TruthValue value1 = judgment1.truth;
        TruthValue value2 = judgment2.truth;
        TruthValue truth = TruthFunctions.intersection(value1, value2);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false);
    }
}
