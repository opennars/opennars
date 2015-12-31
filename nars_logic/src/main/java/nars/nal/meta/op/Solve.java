package nars.nal.meta.op;

import com.headius.invokebinder.Binder;
import nars.Symbols;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.PostCondition;
import nars.nal.meta.TruthOperator;
import nars.truth.BeliefFunction;
import nars.truth.DesireFunction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Evaluates the truth of a premise
 */
public final class Solve extends AtomicBooleanCondition<PremiseMatch> {
    public final TruthOperator belief;
    public final TruthOperator desire;
    public final char puncOverride;

    private final transient String id;

    public final PremiseRule rule;
    private final Derive derive;
    private final MethodHandle method;


    public Solve(PostCondition p, PremiseRule rule, boolean anticipate, boolean eternalize,
                 BooleanCondition[] postPreconditions) {

        this.puncOverride = p.puncOverride;

        this.belief = BeliefFunction.get(p.beliefTruth);

//        if (belief == null &&
//                !((puncOverride==Symbols.GOAL) || (puncOverride==Symbols.QUEST) || (puncOverride==Symbols.QUESTION)))
//            throw new RuntimeException("unknown belief function " + beliefTerm);

        this.desire = DesireFunction.get(p.goalTruth);


        String beliefLabel = belief == null ? "_" :
                p.beliefTruth.toString();
        String desireLabel = desire == null ? "_" :
                p.goalTruth.toString();

        String sn = "Truth:(";
        String i = puncOverride == 0 ?
                sn + beliefLabel + ',' + desireLabel :
                sn + beliefLabel + ',' + desireLabel + ",punc:\"" + puncOverride + '\"';

        i += ')';

        this.rule = rule;


        this.id = i;
        this.derive = new Derive(rule, p.term,
                postPreconditions,
                anticipate,
                eternalize);




        try {
            MethodHandles.Lookup l = MethodHandles.publicLookup();

            this.method = puncOverride != 0 ? Binder.from(boolean.class, PremiseMatch.class)
                    .append(puncOverride)
                    .append(TruthOperator.class, belief)
                    .append(TruthOperator.class, desire)
                    .invokeStatic(l, Solve.class, "measureTruthOverride") : Binder.from(boolean.class, PremiseMatch.class)
                    .append(TruthOperator.class, belief)
                    .append(TruthOperator.class, desire)
                    .invokeStatic(l, Solve.class, "measureTruthInherit");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
        s += puncOverride == 0 ? "p.getTask().getPunctuation()" : "'" + puncOverride + "'";
        s += ", nars.truth.BeliefFunction." + belief + ", ";
        s += desire != null ? "nars.truth.DesireFunction." + desire : "null";
        s += ")";
        return s;
    }

    @Override
    public final boolean booleanValueOf(PremiseMatch m) {
        try {
            return (boolean)method.invokeExact(m);
        } catch (Throwable throwable) {
            return false;
            //throw new RuntimeException(throwable);
            // throwable.printStackTrace();  // return false;
        }
    }


    public static boolean measureTruthInherit(PremiseMatch m, TruthOperator belief, TruthOperator desire) {
        char punct = m.premise.getTask().getPunctuation();
        return measureTruthOverride(m, punct, belief, desire);
    }

    public static boolean measureTruthOverride(PremiseMatch m, char punct, TruthOperator belief, TruthOperator desire) {
        TruthOperator tf;
        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            tf = (punct == Symbols.JUDGMENT) ? belief : desire;

            if (tf == null)
                return false;

            /** filter cyclic double-premise results  */
            if (m.cyclic && !tf.allowOverlap()) {
                //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
                //                    match.removeCyclic(outcome, premise, truth, punct);
                //                }
                return false;
            }

            boolean b = tf.apply(m);
            if (!b)
                return false;

        }

        m.punct.set(punct);
        return true;
    }

    public Derive getDerive() {
        return derive;
    }


}

