package nars.nal;

import nars.$;
import nars.Global;
import nars.Op;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.op.Solve;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.term.transform.VarCachedVersionMap;
import nars.truth.Truth;
import nars.util.version.Versioned;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;


/**
 * rule matching context, re-recyclable as thread local
 */
public class RuleMatch extends FindSubst {

    /** Global Context */
    public Consumer<Task> receiver;

    /** current Premise */
    public ConceptProcess premise;

    public final VarCachedVersionMap secondary;
    public final Versioned<Integer> occurrenceShift;
    public final Versioned<Truth> truth;
    public final Versioned<Character> punct;
    @Deprecated public final Versioned<Solve.Derive> derived;

    public boolean cyclic;

    final Map<Operator, ImmediateTermTransform> transforms =
            Global.newHashMap();

    public RuleMatch(Random r) {
        super(Op.VAR_PATTERN, r );

        for (Class<? extends ImmediateTermTransform> c : PremiseRule.Operators) {
            addTransform(c);
        }

        secondary = new VarCachedVersionMap(this);
        occurrenceShift = new Versioned(this);
        truth = new Versioned(this);
        punct = new Versioned(this);
        derived = new Versioned(this);
    }

    private void addTransform(Class<? extends ImmediateTermTransform> c) {
        Operator o = $.operator(c.getSimpleName());
        try {
            transforms.put(o, c.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(c + ": " + e);
        }
    }

    @Override public ImmediateTermTransform getTransform(Operator t) {
        return transforms.get(t);
    }


    @Override
    public void onPartial() {
        onMatch();
    }

    @Override
    public boolean onMatch() {


        Solve.Derive dd = derived.get();

        Term tt = dd.solve(this);
        if (tt == null) return true;
        tt = tt.normalized();
        if (tt == null) return true;

        if (!dd.post(this))
            return false;

        dd.derive(this, tt);

        return true;
    }

    public Task derive(Task derived) {

        //HACK this should exclude the invalid rules which form any of these
        if (!derived.get().levelValid( premise.nal()) )
            return null;

        //pre-normalize to avoid discovering invalidity after having consumed space and survived the input queue
        derived = derived.normalize(premise.memory());
        if (derived == null)
            return null;

        derived = premise.derive(derived);
        if (derived!=null) {
            receiver.accept(derived);
        }
        return derived;
    }




    @Override
    public String toString() {
        return "RuleMatch:{" +
                "premise:" + premise +
                ", subst:" + super.toString() +
                (derived.get()!=null ? (", derived:" + derived) : "")+
                (truth.get()!=null ? (", truth:" + truth) : "")+
                (!secondary.isEmpty() ? (", secondary:" + secondary) : "")+
                (occurrenceShift.get()!=null ? (", occShift:" + occurrenceShift) : "")+
                //(branchPower.get()!=null ? (", derived:" + branchPower) : "")+
                '}';

    }


    /**
     * set the next premise
     */
    public final void start(ConceptProcess p, Consumer<Task> receiver, Deriver d) {
        clear();

        premise = p;
        this.receiver = receiver;

        Compound taskTerm = p.getTask().term();
        Termed beliefTerm = p.getBelief() != null ?
            p.getBelief().get()
            : p.getTermLink().get(); //experimental, prefer to use the belief term's Term in case it has more relevant TermMetadata (intermvals)

        term.set( new TaskBeliefPair(
            taskTerm.term(),
            beliefTerm.term()
        ) );
        cyclic = premise.isCyclic();

//        //set initial power which will be divided by branch
//        setPower(
//            //LERP the power in min/max range by premise mean priority
//            (int) ((p.getMeanPriority() * (Global.UNIFICATION_POWER - Global.UNIFICATION_POWERmin))
//                    + Global.UNIFICATION_POWERmin)
//        );

        //setPower(branchPower.get()); //HACK is this where it should be assigned?

        d.run(this);
    }





    public final void occurrenceAdd(long cyclesDelta) {
        //TODO move to post
        int oc = occurrenceShift.getIfAbsent(Tense.TIMELESS);
        if (oc == Tense.TIMELESS)
            oc = 0;
        oc += cyclesDelta;
        occurrenceShift.set((int)oc);
    }

    /** calculates Budget used in a derived task,
     *  returns null if invalid / insufficient */
    public final Budget getBudget(Truth truth, Compound c) {

        ConceptProcess p = this.premise;

        Budget budget = truth != null ?
                BudgetFunctions.compoundForward(truth, c, p) :
                BudgetFunctions.compoundBackward(c, p);

        if (Budget.isDeleted(budget.getPriority())) {
            throw new RuntimeException("why is " + budget + " deleted");
        }

        if (!!budget.summaryLessThan(p.memory().derivationThreshold.floatValue())) {
//            if (false) {
//                RuleMatch.removeInsufficientBudget(premise, new PreTask(t,
//                        m.punct.get(), truth, budget,
//                        m.occurrenceShift.getIfAbsent(Tense.TIMELESS), premise));
//            }
            return null;
        }

        return budget;
    }
}


