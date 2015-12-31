package nars.nal.meta.op;

import nars.Premise;
import nars.Symbols;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.TruthOperator;
import nars.term.Term;
import nars.truth.BeliefFunction;
import nars.truth.DesireFunction;

/**
 * Evaluates the truth of a premise
 */
public final class Solve extends BooleanCondition<PremiseMatch> {
    public final TruthOperator belief;
    public final TruthOperator desire;
    public final char puncOverride;

    private final transient String id;

    public final PremiseRule rule;
    private final Derive derive;


    public Solve(Term beliefTerm, Term desireTerm, char puncOverride,
                 PremiseRule rule, boolean anticipate, boolean eternalize, Term term,

                 BooleanCondition[] postPreconditions
    ) {
        this.puncOverride = puncOverride;

        belief = BeliefFunction.get(beliefTerm);
//        if (belief == null &&
//                !((puncOverride==Symbols.GOAL) || (puncOverride==Symbols.QUEST) || (puncOverride==Symbols.QUESTION)))
//            throw new RuntimeException("unknown belief function " + beliefTerm);

        desire = DesireFunction.get(desireTerm);


        String beliefLabel = belief == null ? "_" :
                beliefTerm.toString();
        String desireLabel = desire == null ? "_" :
                desireTerm.toString();

        String sn = "Truth:(";
        String i = puncOverride == 0 ?
                sn + beliefLabel + ',' + desireLabel :
                sn + beliefLabel + ',' + desireLabel + ",punc:\"" + puncOverride + '\"';

        i += ')';

        this.rule = rule;


        this.id = i;
        this.derive = new Derive(rule, term,
                postPreconditions,
                anticipate,
                eternalize);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String toJavaConditionString() {
        String s = "";
        String solver = Solve.class.getName();
        s += solver + ".measureTruth(m, ";
        if (puncOverride == 0) {
            s += "p.getTask().getPunctuation()";
        } else {
            s += "'" + puncOverride + "'";
        }
        s += ", nars.truth.BeliefFunction." + belief + ", ";
        s += desire != null ? "nars.truth.DesireFunction." + desire : "null";
        s += ")";
        return s;
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        Premise p = m.premise;

        /** calculate derived task punctuation,
         possibly using a default policy determined by parent task */
        return measureTruth(m,
                puncOverride == 0 ? p.getTask().getPunctuation() : puncOverride,
                belief, desire);
    }

    public static boolean measureTruth(PremiseMatch m, char punct, TruthOperator belief, TruthOperator desire) {

        TruthOperator tf;
        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            tf = (punct == Symbols.JUDGMENT) ? belief : desire;
        } else {
            tf = null;
        }

        m.punct.set(punct);

        if (tf == null)
            return false;

        /** filter cyclic double-premise results  */
        if (m.cyclic && !tf.allowOverlap()) {
            //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
            //                    match.removeCyclic(outcome, premise, truth, punct);
            //                }
            return false;
        }

        return tf.apply(m);
    }

    public Derive getDerive() {
        return derive;
    }


}

