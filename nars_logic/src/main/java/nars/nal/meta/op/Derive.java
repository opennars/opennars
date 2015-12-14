package nars.nal.meta.op;

import nars.Global;
import nars.Premise;
import nars.budget.Budget;
import nars.nal.PremiseRule;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.nal7.Tense;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.truth.Truth;

import static nars.truth.TruthFunctions.eternalizedConfidence;

/**
 * finally attempt to produce and input tasks
 */
public final class Derive extends PreCondition {

    @Deprecated final PremiseRule rule;
    final boolean anticipate;
    final boolean eternalize;

    private final transient String id;

    public Derive(PremiseRule rule, boolean anticipate, boolean eternalize) {
        this.rule = rule;
        this.anticipate = anticipate;
        this.eternalize = eternalize;
        id = !anticipate && !eternalize ? "Derive" : "Derive:{" +
                (anticipate ? "anticipate" : "") +
                (anticipate && eternalize ? "," : "") +
                (eternalize ? "eternalize" : "") + '}';
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean test(RuleMatch m) {

        Term t = m.derived.get().normalized();

        if (t==null || Variable.hasPatternVariable(t))
            return false;

        Compound c = Task.validTaskTerm(t);
        if (c == null)
            return false;

        derive(m, c);

        return false; //match finish
    }

    private void derive(RuleMatch m, Compound c) {

        Premise premise = m.premise;

        Truth truth = m.truth.get();

        Budget budget = m.getBudget(truth, c);
        if (budget == null)
            return;


        Task task = premise.getTask();
        Task belief = premise.getBelief();


        char punct = m.punct.get();

        MutableTask deriving = new MutableTask(c);

        long now = premise.time();

        int occurence_shift = m.occurrenceShift.getIfAbsent(Tense.TIMELESS);
        long taskOcc = task.getOccurrenceTime();
        long occ = occurence_shift > Tense.TIMELESS ? taskOcc + occurence_shift : taskOcc;


        //just not able to measure it, closed world assumption gone wild.
        if (occ != Tense.ETERNAL && premise.isEternal() && !premise.nal(7)) {
            throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
        }

        if ((Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ||Global.DEBUG_LOG_DERIVING_RULE) && Global.DEBUG) {
            deriving.log(rule);
        }

        Task derived = deriving
                .punctuation(punct)
                .truth(truth)
                .budget(budget)
                .time(now, occ)
                .parent(task, belief /* null if single */)
                .anticipate(occ != Tense.ETERNAL && anticipate);

        if ((derived = m.derive(derived)) == null)
            return;

        //--------- TASK WAS DERIVED if it reaches here


        if (truth != null && eternalize && !derived.isEternal()) {

            m.derive(
                    new MutableTask(derived.get())
                            .punctuation(punct)
                            .truth(
                                    truth.getFrequency(),
                                    eternalizedConfidence(truth.getConfidence())
                            )
                            .budgetCompoundForward(premise)
                            .time(now, Tense.ETERNAL)
                            .parent(task, belief)
            );

        }

    }
}
