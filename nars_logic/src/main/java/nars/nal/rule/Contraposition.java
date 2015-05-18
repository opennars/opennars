package nars.nal.rule;

import nars.Memory;
import nars.budget.Budget;
import nars.Symbols;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.term.Statement;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal1.Negation;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Term;


public class Contraposition extends ConceptFireTask {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink) {
        final Sentence taskSentence = taskLink.getSentence();

        final Term taskTerm = taskSentence.term;

        if ((taskLink.type!= TermLink.TRANSFORM) && (taskTerm instanceof Implication)) {
            //there would only be one concept which has a term equal to another term... so samplingis totally unnecessary

            //Concept d=memory.concepts.sampleNextConcept();
            //if(d!=null && d.term.equals(taskSentence.term)) {

            float n = taskTerm.getComplexity(); //don't let this rule apply every time, make it dependent on complexity
            float w = 1.0f / ((n * (n - 1)) / 2.0f); //let's assume hierachical tuple (triangle numbers) amount for this
            if (Memory.randomNumber.nextFloat() < w) { //so that NARS memory will not be spammed with contrapositions

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
        //TODO this method can end earlier if it detects an input implication with freq=1, because the resulting confidence should be 0 which is useless

        Memory memory = nal.memory;
        memory.logic.CONTRAPOSITION.hit();

        Term subj = statement.getSubject();
        Term pred = statement.getPredicate();

        Statement content = Statement.make(statement,
                Negation.make(pred),
                Negation.make(subj),
                TemporalRules.reverseOrder(statement.getTemporalOrder()));

        if (content == null) return false;

        Truth truth = sentence.truth;
        Budget budget;
        if (sentence.isQuestion() || sentence.isQuest()) {
            if (content instanceof Implication) {
                budget = BudgetFunctions.compoundBackwardWeak(content, nal);
            } else {
                budget = BudgetFunctions.compoundBackward(content, nal);
            }
        } else {
            if (content instanceof Implication) {
                truth = TruthFunctions.contraposition(truth);
            }
            else {
                throw new RuntimeException("contraposition for non-implication are not implemented yet");
            }
            budget = BudgetFunctions.compoundForward(truth, content, nal);
        }

        return nal.singlePremiseTask(content, sentence.punctuation, truth, budget);
    }


}
