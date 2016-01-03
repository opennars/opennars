package nars.nal.meta.op;

import nars.Op;
import nars.Symbols;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.PostCondition;
import nars.nal.meta.TruthOperator;
import nars.truth.BeliefFunction;
import nars.truth.DesireFunction;

/**
 * Evaluates the truth of a premise
 */
abstract public class Solve extends AtomicBooleanCondition<PremiseMatch> {

    private final transient String id;

    private final Derive derive;

    public Solve(String id, Derive derive) {
        super();
        this.id = id;
        this.derive = derive;
    }

    public static Solve the(PostCondition p, PremiseRule rule, boolean anticipate, boolean eternalize,
                            BooleanCondition[] postPreconditions) {

        char puncOverride = p.puncOverride;

        BeliefFunction belief = BeliefFunction.get(p.beliefTruth);
        DesireFunction desire = DesireFunction.get(p.goalTruth);

        String beliefLabel = belief == null ? "_" :
                p.beliefTruth.toString();
        String desireLabel = desire == null ? "_" :
                p.goalTruth.toString();

        String sn = "Truth:(";
        String i = puncOverride == 0 ?
                sn + beliefLabel + ',' + desireLabel :
                sn + beliefLabel + ',' + desireLabel + ",punc:\"" + puncOverride + '\"';

        i += ')';

        //this.rule = rule;


        Derive der = new Derive(rule, p.term,
                postPreconditions,
                anticipate,
                eternalize);

        if (puncOverride == 0) {
            //Inherit from task
            return new Solve(i, der) {
                @Override public boolean booleanValueOf(PremiseMatch m) {
                    return measure(m,
                            m.premise.getTask().getPunctuation(),
                            belief, desire);
                }
            };
        } else {
            //Override
            return new Solve(i, der) {
                @Override public boolean booleanValueOf(PremiseMatch m) {
                    return measure(m, puncOverride, belief, desire);
                }
            };
        }


//
//        try {
//            MethodHandles.Lookup l = MethodHandles.publicLookup();
//
//            this.method = puncOverride != 0 ? Binder.from(boolean.class, PremiseMatch.class)
//                    .append(puncOverride)
//                    .append(TruthOperator.class, belief)
//                    .append(TruthOperator.class, desire)
//                    .invokeStatic(l, Solve.class, "measureTruthOverride") : Binder.from(boolean.class, PremiseMatch.class)
//                    .append(TruthOperator.class, belief)
//                    .append(TruthOperator.class, desire)
//                    .invokeStatic(l, Solve.class, "measureTruthInherit");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public String toString() {
        return id;
    }

//    @Override
//    public String toJavaConditionString() {
//        String s = "";
//        String solver = Solve.class.getName();
//        s += solver + ".measureTruth(m, ";
//        s += puncOverride == 0 ? "p.getTask().getPunctuation()" : "'" + puncOverride + "'";
//        s += ", nars.truth.BeliefFunction." + belief + ", ";
//        s += desire != null ? "nars.truth.DesireFunction." + desire : "null";
//        s += ")";
//        return s;
//    }

//    @Override
//    public final boolean booleanValueOf(PremiseMatch m) {
//        boolean r = false;
//        try {
//            r=(boolean)method.invokeExact(m);
//        } catch (Throwable throwable) {
//            //throw new RuntimeException(throwable);
//            // throwable.printStackTrace();  // return false;
//        }
//        return r;
//    }


//    /** inherits punctuation from task */
//    public static class SolveInherit extends Solve {
//
//        @Override public boolean booleanValueOf(PremiseMatch m) {
//            char punct = m.premise.getTask().getPunctuation();
//            return measureTruthOverride(m, punct, belief, desire);
//        }
//    }
//
//    /** overrides punctuation */
//    public static class SolveOverride extends Solve {
//
//    }

//    public static boolean measureTruthInherit(PremiseMatch m, TruthOperator belief, TruthOperator desire) {
//        char punct = m.premise.getTask().getPunctuation();
//        return measureTruthOverride(m, punct, belief, desire);
//    }

    static boolean measure(PremiseMatch m, char punct, TruthOperator belief, TruthOperator desire) {
        boolean r;
        switch (punct) {
            case Symbols.JUDGMENT:
            case Symbols.GOAL:
                TruthOperator tf = (punct == Symbols.JUDGMENT) ? belief : desire;
                r = (tf != null) && (!m.cyclic || tf.allowOverlap()) && (tf.apply(m));
                break;
            case Symbols.QUESTION:
            case Symbols.QUEST:
                r = true; //a truth function is not involved, so succeed
                break;
            default:
                throw new Op.InvalidPunctuationException(punct);
        }

        m.punct.set(punct);
        return r;
    }

    public Derive getDerive() {
        return derive;
    }


}

