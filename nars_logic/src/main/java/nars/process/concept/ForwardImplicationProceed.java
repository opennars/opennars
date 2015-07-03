package nars.process.concept;


import nars.Symbols;
import nars.budget.BudgetFunctions;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Term;
import nars.term.Terms;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.HashSet;
import java.util.Set;

import static nars.nal.nal7.TemporalRules.ORDER_CONCURRENT;
import static nars.nal.nal7.TemporalRules.ORDER_FORWARD;

/*
 if the premise task is a =/>
    <(&/,<a --> b>,<b --> c>,<x --> y>,pick(a)) =/> <goal --> reached>>.
    (&/,<a --> b>,<b --> c>,<x --> y>). :|:
    |-
    <pick(a) =/> <goal --> reached>>. :|:
*/
public class ForwardImplicationProceed extends ConceptFireTaskTerm {

    /**
     * //new inference rule accelerating decision making: https://groups.google.com/forum/#!topic/open-nars/B8veE-WDd8Q
     */
    public static int PERCEPTION_DECISION_ACCEL_SAMPLES = 1;

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {
        if (!f.nal(7)) return true;
        if (PERCEPTION_DECISION_ACCEL_SAMPLES == 0) return true;

        Concept concept = f.getCurrentTermLinkConcept();
        if (concept == null) return true;

        Task taskLinkTask = f.getCurrentTask();// taskLink.getTask();


        if (!(taskLinkTask.sentence.isJudgment() || taskLinkTask.sentence.isGoal())) return true;

        Term taskTerm = taskLinkTask.sentence.getTerm();
        if (!(taskTerm instanceof Implication)) return true;
        Implication imp = (Implication) taskLinkTask.sentence.getTerm();

        if (!((imp.getTemporalOrder() == ORDER_FORWARD || (imp.getTemporalOrder() == ORDER_CONCURRENT))))
            return true;


        if (!(imp.getSubject() instanceof Conjunction)) return true;
        Conjunction conj = (Conjunction) imp.getSubject();


        //the conjunction must be of size > 2 in order for a smaller one to match its beginning subsequence
        if (conj.length() <= 2)
            return true;

        if (!((conj.getTemporalOrder() == ORDER_FORWARD) || (conj.getTemporalOrder() == ORDER_CONCURRENT)))
            return true;

        Set<Term> alreadyInducted = new HashSet();

        for (int i = 0; i < PERCEPTION_DECISION_ACCEL_SAMPLES; i++) {


            Concept next = f.memory.nextConcept();

            if ((next == null) || (next.equals(concept))) continue;

            final Term t = next.getTerm();
            if (!(t instanceof Conjunction)) continue;

            final Conjunction conj2 = (Conjunction) t;

            if (conj.getTemporalOrder() == conj2.getTemporalOrder() && alreadyInducted.add(t)) {

                Task s = null;
                if ((taskLinkTask.sentence.punctuation == Symbols.JUDGMENT) && (next.hasBeliefs())) {
                    s = next.getBeliefs().top();
                }
                else if ((taskLinkTask.sentence.punctuation == Symbols.GOAL) && (next.hasGoals())) {
                    s = next.getGoals().top();
                }
                if (s == null) continue;


                //conj2 conjunction has to be a minor of conj
                //the case where its equal is already handled by other inference rule
                if (conj2.term.length < conj.term.length) {


                    //ok now check if it is really a minor (subsequence)
                    boolean equal = Terms.equals(conj.term, conj2.term);
                    if (!equal) continue;

                    //ok its a minor, we have to construct the residue implication now
                    Term[] residue = new Term[conj.term.length - conj2.term.length];
                    System.arraycopy(conj.term, conj2.term.length + 0, residue, 0, residue.length);

                    Term C = Conjunction.make(residue, conj.getTemporalOrder());
                    Implication resImp = Implication.make(C, imp.getPredicate(), imp.getTemporalOrder());

                    if (resImp == null)
                        continue;

                    //todo add
                    Truth truth = TruthFunctions.deduction(s.getTruth(), taskLinkTask.sentence.truth);


//                    Task newTask = new Task(
//                            new Sentence(resImp, s.getPunctuation(), truth,
//                                    f.newStamp(taskLinkTask.sentence, f.memory.time())),
//                            new Budget(BudgetFunctions.forward(truth, f)),
//                            taskLinkTask
//                            );
//                    //f.setCurrentBelief(s);
//                    f.derive(newTask, false, false, taskLinkTask, false);

                    f.derive(f.newTask(resImp).punctuation(s.getPunctuation())
                            .truth(truth).parent(taskLinkTask).occurrNow()
                            .budget(BudgetFunctions.forward(truth, f))
                    );

                }
            }
        }


        return true;
    }
}
