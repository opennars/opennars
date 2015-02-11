package nars.logic.reason.concept;

import nars.core.Memory;
import nars.io.Symbols;
import nars.logic.*;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;
import nars.logic.reason.ConceptFire;


public class Contraposition extends ConceptFireTask {

    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink) {
        final Sentence taskSentence = taskLink.getSentence();

        final Term taskTerm = taskSentence.term;

        if (((taskLink.type!= TermLink.TRANSFORM) && (taskTerm instanceof Implication))) {
            //there would only be one concept which has a term equal to another term... so samplingis totally unnecessary

            //Concept d=memory.concepts.sampleNextConcept();
            //if(d!=null && d.term.equals(taskSentence.term)) {

            double n = taskTerm.getComplexity(); //don't let this reason apply every time, make it dependent on complexity
            double w = 1.0 / ((n * (n - 1)) / 2.0); //let's assume hierachical tuple (triangle numbers) amount for this
            if (Memory.randomNumber.nextDouble() < w) { //so that NARS memory will not be spammed with contrapositions

                contraposition((Statement) taskTerm, taskSentence, f);
                //}
            }
        }

        return true;
    }

    /**
     * {<A ==> B>, A@(--, A)} |- <(--, B) ==> (--, A)>
     *
     * @param statement The premise
     */
    protected static boolean contraposition(final Statement statement, final Sentence sentence, final NAL nal) {
        Memory memory = nal.memory;
        memory.logic.CONTRAPOSITION.hit();

        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();

        Statement content = Statement.make(statement,
                Negation.make(pred),
                Negation.make(subj),
                TemporalRules.reverseOrder(statement.getTemporalOrder()));

        if (content == null) return false;

        TruthValue truth = sentence.truth;
        BudgetValue budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (content instanceof Implication) {
                budget = BudgetFunctions.compoundBackwardWeak(content, nal);
            } else {
                budget = BudgetFunctions.compoundBackward(content, nal);
            }
            return nal.singlePremiseTask(content, Symbols.QUESTION, truth, budget);
        } else {
            if (content instanceof Implication) {
                truth = TruthFunctions.contraposition(truth);
            }
            budget = BudgetFunctions.compoundForward(truth, content, nal);
            return nal.singlePremiseTask(content, Symbols.JUDGMENT, truth, budget);
        }
    }


}
