package nars.nal.meta.op;

import nars.Global;
import nars.Premise;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.nal.meta.PreCondition;
import nars.task.DefaultTask;
import nars.task.FluentTask;
import nars.task.PreTask;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.truth.Stamp;
import nars.truth.Truth;

import static nars.truth.TruthFunctions.eternalizedConfidence;

/**
 * finally attempt to produce and input tasks
 */
public final class Derive extends PreCondition {

    @Deprecated final TaskRule rule;
    final boolean anticipate;
    final boolean eternalize;

    private final transient String id;

    public Derive(TaskRule rule, boolean anticipate, boolean eternalize) {
        this.rule = rule;
        this.anticipate = anticipate;
        this.eternalize = eternalize;
        if (!anticipate && !eternalize) {
            this.id = "Derive";
        }
        else {
            this.id = "Derive:{" +
                    (anticipate ? "anticipate" : "") +
                    (anticipate && eternalize ? "," : "") +
                    (eternalize ? "eternalize" : "") + '}';
        }
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean test(RuleMatch m) {


        final Premise premise = m.premise;

        Term derivedTerm = m.derived.get();

        //test for reactor leak
        // TODO prevent this from happening
        if (Variable.hasPatternVariable(derivedTerm)) {

            return false;
        }

        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
        derivedTerm = derivedTerm.normalized();

        if (!(derivedTerm instanceof Compound))
            return false;

        final Truth truth = m.truth.get();
        final Budget budget;
        if (truth != null) {
            budget = BudgetFunctions.compoundForward(truth, derivedTerm, premise);
            //budget = BudgetFunctions.forward(truth, premise);
        } else {
            budget = BudgetFunctions.compoundBackward(derivedTerm, premise);
        }

        if (!premise.validateDerivedBudget(budget)) {
            if (Global.DEBUG && Global.DEBUG_REMOVED_INSUFFICIENT_BUDGET_DERIVATIONS) {
                RuleMatch.removeInsufficientBudget(premise, new PreTask(derivedTerm,
                        m.punct.get(), truth, budget,
                        m.occurrenceShift.get(), premise));
            }
            return false;
        }

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();


        final char punct = m.punct.get();

        FluentTask deriving = DefaultTask.make((Compound) derivedTerm); //, task, belief, allowOverlap);
        if (deriving == null)
            return false;

        final long now = premise.time();
        final long occ;

        final long occurence_shift = m.occurrenceShift.get();
        long taskOcc = task.getOccurrenceTime();
        if (occurence_shift > Stamp.TIMELESS) {
            occ = taskOcc + occurence_shift;
        } else {
            occ = taskOcc; //inherit premise task's
        }

        //just not able to measure it, closed world assumption gone wild.

        if (occ != Stamp.ETERNAL && premise.isEternal() && !premise.nal(7)) {
            throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
        }

        if ((Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ||Global.DEBUG_LOG_DERIVING_RULE) && Global.DEBUG) {
            deriving.log(rule);
        }


        Task derived = m.derive(deriving
                .punctuation(punct)
                .truth(truth)
                .budget(budget)
                .time(now, occ)
                .parent(task, belief /* null if single */)
                .anticipate(occ != Stamp.ETERNAL ? anticipate : false));

        if (derived == null) return false;


        //--------- TASK WAS DERIVED if it reaches here




//                if (premise.nal(7) && rule.anticipate && task.isInput()) { //the prediction needs to be based on a observation
//                    premise.memory().the(Anticipate.class).anticipate(derived); //else the system can anticipate things it can not measure
//                }                    //thus these anticipations would fail, leading the system thinking that this did not happen altough it was




        if (truth != null && eternalize && !derived.isEternal()) {

            m.derive(premise.removeInvalid(
                new FluentTask(derived.getTerm())
                    .punctuation(punct)
                    .truth(
                            truth.getFrequency(),
                            eternalizedConfidence(truth.getConfidence())
                    )
                    .budgetCompoundForward(premise)
                    .time(now, Stamp.ETERNAL)
                    .parent(task, belief)
            ));

        }

        return false; //finished
    }
}
