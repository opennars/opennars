package nars.nal.meta.op;

import nars.Global;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.nal.meta.PreCondition;
import nars.nal.nal1.Inheritance;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Tense;
import nars.op.mental.Anticipate;
import nars.task.FluentTask;
import nars.task.PreTask;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.truth.DefaultTruth;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.function.Consumer;

/**
 * finally attempt to produce and input tasks
 */
public final class MakeTasks extends PreCondition {

    @Deprecated final TaskRule rule;

    public MakeTasks(TaskRule taskRule) {
        this.rule = taskRule;
    }

    @Override
    public String toString() {
        return "MakeTasks";
        //return "MakeTasks[" + rule + "]";
    }

    @Override
    public boolean test(RuleMatch m) {

        final RuleMatch.PostMods post = m.post;
        final Premise premise = m.premise;

        Term derivedTerm = post.derivedTerm;

        //test for reactor leak
        // TODO prevent this from happening
        if (Variable.hasPatternVariable(derivedTerm)) {
            return false;
        }

        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
        derivedTerm = derivedTerm.normalized();

        if (!(derivedTerm instanceof Compound))
            return false;

        final Truth truth = post.truth;
        final Budget budget;
        if (truth != null) {
            budget = BudgetFunctions.compoundForward(truth, derivedTerm, premise);
            //budget = BudgetFunctions.forward(truth, premise);
        } else {
            budget = BudgetFunctions.compoundBackward(derivedTerm, premise);
            budget.setDurability(budget.getDurability()*0.5f);
        }

        if (!premise.validateDerivedBudget(budget)) {
            if (Global.DEBUG && Global.DEBUG_REMOVED_INSUFFICIENT_BUDGET_DERIVATIONS) {
                RuleMatch.removeInsufficientBudget(premise, new PreTask(derivedTerm, post.punct, truth, budget, post.occurence_shift, premise));
            }
            return false;
        }

        final Task task = premise.getTask();

        /** calculate derived task truth value */

        final Task belief = premise.getBelief();

        /*boolean valid_excuse=derivedTerm instanceof Implication && derivedTerm.complexity()<=17 && ((Implication) derivedTerm).getPredicate() instanceof Inheritance && derivedTerm.getTemporalOrder()== Tense.ORDER_FORWARD;
        if(!valid_excuse && derivedTerm.complexity()>11) { //17
            return false;
        }*/


        final char punct = post.punct;

      //  if(punct == Symbols.JUDGMENT && truth.getExpectation()<0.3) {
      //      return false;
       // }

        /*if (punct == 0)
            throw new RuntimeException("invalid punctuation");*/

        FluentTask deriving = premise.newTask((Compound) derivedTerm); //, task, belief, allowOverlap);
        if (deriving != null) {

            if(punct==Symbols.JUDGMENT && derivedTerm.hasVarQuery()) {
                return false;
            }

            final long now = premise.time();

            final long occurence_shift = post.occurence_shift;
            long shift = (occurence_shift > Stamp.TIMELESS && task.getOccurrenceTime() != Stamp.ETERNAL) ? occurence_shift : 0;
            final long occ = task.getOccurrenceTime() + shift;

            if (occ != Stamp.ETERNAL && premise.isEternal() && !premise.nal(7)) {
                throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
            }

            Task derived = premise.validate(deriving
                    .punctuation(punct)
                    .truth(truth)
                    .budget(budget)
                    .time(now, occ)
                    .parent(task, belief /* null if single */)
            );

            if (derived != null && budget.getPriority() >= Global.BUDGET_EPSILON && budget.getDurability() >= Global.BUDGET_EPSILON) {
                //potential anticipation
                premise.memory().the(Anticipate.class).anticipate(premise, derived);
                if (Global.DEBUG && Global.DEBUG_LOG_DERIVING_RULE) { //just not able to measure it, closed world assumption gone wild.
                    derived.log(rule.toString());
                }

                final Consumer<Task> receiver = m.receiver;

                receiver.accept(derived);

                if (truth != null && !derived.isEternal() /* && derived.isJudgment() && rule.immediate_eternalize */ ) {
                    Truth et = TruthFunctions.eternalize(new DefaultTruth(truth.getFrequency(), truth.getConfidence()));
                    FluentTask deriving2 = premise.newTask((Compound) derivedTerm);
                    Budget budget2 = BudgetFunctions.compoundForward(et, derivedTerm, premise);

                    final Task derivedEternal = premise.validate(deriving2
                            .punctuation(punct)
                            .truth(et)
                            .budget(budget2)
                            .time(now, Stamp.ETERNAL)
                            .parent(task, belief // null if single
                            )
                    );

                    if (derivedEternal != null) {
                        receiver.accept(derivedEternal);
                    }
                }

            }
        }

        return false; //finished
    }
}
