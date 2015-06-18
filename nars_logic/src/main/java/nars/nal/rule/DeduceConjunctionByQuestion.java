package nars.nal.rule;

import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.process.ConceptProcess;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal5.Conjunction;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static nars.nal.TruthFunctions.intersection;

/** --------------- questions which contain answers which are of no value for NARS but need to be answered ---------------
 * {(&&,A,B,...)?, A,B} |- {(&&,A,B)} {(&&,A,_components_1_)?,
 * (&&,_part_of_components_1_),A} |- {(&&,A,_part_of_components_1_,B)} and
 * also the case where both are conjunctions, all components need to be
 * subterm of the question-conjunction in order for the subterms of both
 * conjunctions to be collected together.
 * */
public class DeduceConjunctionByQuestion extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if (f.getCurrentBelief()!=null)
            dedConjunctionByQuestion(
                    taskLink.getTask(), f.getCurrentBelief(), f);
        return true;
    }

    /**
     *
     * @param sentence The first premise
     * @param belief   The second premise
     * @param nal      Reference to the memory
     */
    static void dedConjunctionByQuestion(final Task sentence, final Sentence belief, final NAL nal) {
        if (sentence == null || belief == null || !sentence.isJudgment() || !belief.isJudgment()) {
            return;
        }
        Set<Concept> memoryQuestionConcepts = nal.memory.getQuestionConcepts();
        if (memoryQuestionConcepts.isEmpty())
            return;



        final Term term1 = sentence.getTerm();
        final boolean term1ContainVar = term1.hasVar();
        final boolean term1Conjunction = term1 instanceof Conjunction;

        if ((term1Conjunction) && (term1ContainVar)) {
            return;
        }

        final Term term2 = belief.term;
        final boolean term2ContainVar = term2.hasVar();
        final boolean term2Conjunction = term2 instanceof Conjunction;

        if ((term2Conjunction) && (term2ContainVar)) {
            return;
        }


        memoryQuestionConcepts.forEach(new Consumer<Concept>() {
            @Override
            public void accept(Concept concept) {

                final Term pcontent = concept.getTerm();

                final List<Task> cQuestions = concept.getQuestions();
                if (/*cQuestions == null || */ cQuestions.isEmpty())
                    throw new RuntimeException("Concept " + concept + " present in Concept Questions index, but has no questions");


                if (!(pcontent instanceof Conjunction)) {
                    return;
                }

                final Conjunction ctpcontent = (Conjunction) pcontent;
                if (ctpcontent.hasVar()) {
                    return;
                }

                if (!term1Conjunction && !term2Conjunction) {
                    if (!ctpcontent.containsTerm(term1) || !ctpcontent.containsTerm(term2)) {
                        return;
                    }
                } else {
                    if (term1Conjunction) {
                        if (!term2Conjunction && !ctpcontent.containsTerm(term2)) {
                            return;
                        }
                        if (!ctpcontent.containsAllTermsOf(term1)) {
                            return;
                        }
                    }

                    if (term2Conjunction) {
                        if (!term1Conjunction && !ctpcontent.containsTerm(term1)) {
                            return;
                        }
                        if (!ctpcontent.containsAllTermsOf(term2)) {
                            return;
                        }
                    }
                }

                Compound conj = Sentence.termOrNull(Conjunction.make(term1, term2));
                if (conj == null) return;

            /*
            since we already checked for term1 and term2 having a variable, the result
            will not have a variable

            if (Variables.containVarDepOrIndep(conj.name()))
                continue;
             */
                Truth truthT = nal.getCurrentTask().sentence.truth;
                Truth truthB = nal.getCurrentBelief().truth;
            /*if(truthT==null || truthB==null) {
                //continue; //<- should this be return and not continue?
                return;
            }*/

                Truth truthAnd = intersection(truthT, truthB);
                Budget budget = BudgetFunctions.compoundForward(truthAnd, conj, nal);

                nal.derive(nal.newTask(conj).truth(truthAnd).budget(budget)
                        .parent(sentence, belief).temporalInducted(true));

                nal.memory.logic.DED_CONJUNCTION_BY_QUESTION.hit();

            }
        });

    }

}
