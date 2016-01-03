package nars.nal;

import nars.Global;
import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.meta.*;
import nars.task.PreTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.term.transform.Subst;
import nars.term.transform.Substitution;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;


/**
 * rule matching context, re-recyclable as thread local
 */
public class RuleMatch {

    /**
     * thread-specific pool of RuleMatchers
     * this pool is local to this deriver
     */
    public static final ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XorShift1024StarRandom(1));
    });


    /**
     * Global Context
     */
    public Consumer<Task> receiver;


    @Deprecated
    public TaskBeliefPair taskBelief;

    /**
     * Premise Context
     */
    public Premise premise;
    public enum MatchStage {
        Pre, Pattern, Post
    }

    MatchStage stage = MatchStage.Pre;


    @Deprecated
    public TaskRule rule;


    /** MUTABLE DATA */
    /**
     * stage S: primary pattern substutitions
     */
    public Subst subst;

    public static final class PostMods {
        public Truth truth;
        public Term derivedTerm;
        public long occurence_shift;
        public char punct;

        public void clear() {
            truth = null;
            derivedTerm = null;
            occurence_shift = Stamp.TIMELESS;
            punct = 0;
        }

        public void copyTo(PostMods m) {
            m.occurence_shift = occurence_shift;
            m.truth = truth;
            m.derivedTerm = derivedTerm;
            m.punct = punct;
        }

        @Override
        public String toString() {
            return "PostMods{" +
                    "truth=" + truth +
                    ", derivedTerm=" + derivedTerm +
                    ", occurence_shift=" + occurence_shift +
                    ", punct=" + punct +
                    '}';
        }
    }

    /**
     * stage P: postcondition modifiers
     */
    public PostMods post;

    public static final class SecondarySubs {

        /**
         * pair of maps available for temporary use by conditions and modifiers
         * NOTE: if you use this in a Precondition, make sure to clear() it before it exits
         * ie. return it in the condition you took it, empty
         */
        public final Map<Term, Term> left = Global.newHashMap(0);
        public final Map<Term, Term> right = Global.newHashMap(0);

        /**
         * 'outp' used by substitute to hold the current proposed / candidate
         * additional substitutions. if the modifier doesn't
         * succeed, the values it contains will have no effect
         * before it is cleared.
         * <p>
         * temporary, re-cycled immediately
         */
        public final Map<Term, Term> outp = Global.newHashMap();

        public void clear() {
            left.clear(); right.clear();
            outp.clear(); // necessary?
        }

        public void copyTo(SecondarySubs m) {
            m.clear();
            m.left.putAll(left);
            m.right.putAll(right);
            m.outp.putAll(outp); // necessary?
        }

        @Override
        public String toString() {
            return "SecondarySubs{" +
                    "left=" + left +
                    ", right=" + right +
                    ", outp=" + outp +
                    '}';
        }
    }

    /**
     * stage S2: secondary substutitions
     */
    public SecondarySubs sub2;


    @Override
    public String toString() {
        return "RuleMatch{" +
                "premise=" + premise +
                ", taskBelief=" + taskBelief +
                ", subst=" + subst +
                ", post=" + post +
                ", sub2=" + sub2 +
                '}';

    }
    public RuleMatch(Random r) {
        this.subst = new FindSubst(Op.VAR_PATTERN,
                Global.newHashMap(0),
                Global.newHashMap(0),
                r);
        this.sub2 = new SecondarySubs();
        this.post = new PostMods();
    }

    /**
     * set the next premise
     */
    public final void start(Premise p, Consumer<Task> receiver) {
        start();

        this.premise = p;
        this.receiver = receiver;

        //scale unification power according to premise's mean priority linearly between min and max
        int unificationPower =
                (int) ((p.getMeanPriority() * (Global.UNIFICATION_POWER - Global.UNIFICATION_POWERmin))
                        + Global.UNIFICATION_POWERmin);

        taskBelief = new TaskBeliefPair(p.getTask().getTerm(), p.getTermLink().getTerm());
        this.subst.y = taskBelief;
        this.subst.power = unificationPower;
    }


    public static final class Stage extends PreCondition {
        public final MatchStage s;

        public Stage(MatchStage nextStage) {
            this.s = nextStage;
        }

        @Override
        public boolean test(RuleMatch ruleMatch) {
            ruleMatch.stage = s;
            return true;
        }

        @Override
        public String toString() {
            return "Stage{" + s + "}";
        }
    }

    public void copyTo(RuleMatch target ) {

        target.premise = premise;
        target.receiver = receiver;
        target.taskBelief = taskBelief;

        //if (stage == MatchStage.Pattern) {
        //if (stage != MatchStage.Pre) {
            subst.copyTo(target.subst);
        //}
        /*}
        else {
            //target.subst = subst; //it wont change outside Pattern stage
        }*/

        //if (stage != MatchStage.Pre) {
            post.copyTo(target.post);
            sub2.copyTo(target.sub2);
        /*}
        else {
            m.post = post; //it wont change outside Post stage
            m.sub2 = sub2;
        }*/


//        if (Global.DEBUG) {
//            //  FOR EXTREME EQUALITY TESTING
//            if (!m.toString().equals(toString()))
//                throw new RuntimeException("invalid copy");
//        }

    }


    /**
     * call at beginning to reset
     */
    public final void start() {
        subst.clear();
        sub2.clear();
        post.clear();
        stage = MatchStage.Pre;
    }

    /**
     * clear and re-use with a next rule
     */
    @Deprecated
    public final void start(TaskRule nextRule) {
        start();
        this.rule = nextRule;
    }

    /**
     * for debugging
     */
    public static void removeInsufficientBudget(Premise premise, PreTask task) {
        premise.memory().remove(task, "Insufficient Derived Budget");
    }


    @Deprecated
    protected static boolean cyclic(PostCondition outcome, Premise premise) {
        if(outcome.truth != null && outcome.truth.allowOverlap) {
            return false; //overlap allowed so no matter if there is an overlap or not it isnt cyclic
        }
        return premise.isCyclic(); //in the other cases we have to return whether there is an overlap or not no matter if it has a truth value or not
    }

    public static boolean cyclic(TruthFunction f, Premise premise) {
        return (f != null && !f.allowOverlap()) && premise.isCyclic();
    }


    /**
     * reusable instance (local only)
     */
    final Substitution substituter = new Substitution(null);

    /**
     * provides the cached result if it exists, otherwise computes it and adds to cache
     */
    public final Term resolve(final Term t) {

        return subst.resolve(t, substituter);
    }


    public final void occurrenceAdd(final long cyclesDelta) {
        //TODO move to post
        long oc = this.post.occurence_shift;
        if (oc == Stamp.TIMELESS)
            oc = 0;
        oc += cyclesDelta;
        this.post.occurence_shift = oc;
    }

    @Deprecated public boolean next(Term x, Term y, int unificationPower) {
        return subst.next(x, y, unificationPower);
    }

    public boolean next(TermPattern x, Term y, int unificationPower) {
        return subst.next(x, y, unificationPower);
    }


}
