package nars.meta;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.NALExecuter;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.process.ConceptProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variables;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.*;

/**
 * Created by me on 7/31/15.
 */
public class PostCondition //since there can be multiple tasks derived per rule
{

    private final Term term;
    private final Term[] modifiers;

    public final TruthFunction truth;


    public PostCondition(Term term, Term... modifiers) {
        this.term = term;

        @Deprecated List<Term> otherModifiers = new ArrayList();
        TruthFunction truthFunc = null;

        for (Term m : modifiers) {
            if (m instanceof Inheritance) {
                Inheritance i = (Inheritance)m;
                Term type = i.getPredicate();
                Term which = i.getSubject();
                if (type instanceof Atom) {
                    String typeStr = type.toString();
                    switch (typeStr) {
                        case "Truth":
                            if (truthFunc!=null)
                                throw new RuntimeException("truthFunc " + truthFunc + " already specified");

                            TruthFunction tm = TruthFunction.get(which);
                            if (tm!=null) {
                                truthFunc = tm;
                                continue;
                            }
                            else {
                                throw new RuntimeException("unknown TruthFunction " + which);
                            }
                    }
                }
            }

            otherModifiers.add(m);
        }


        this.truth = truthFunc;

        this.modifiers = otherModifiers.toArray(new Term[ otherModifiers.size() ]);
    }

    public boolean apply(boolean single_premise, Term[] preconditions, Task task, Sentence belief, ConceptProcess nal) {
        if (task == null)
            throw new RuntimeException("null task");

        final Truth T = task.truth;
        final Truth B = single_premise ? null : belief.truth;



        Truth truth = null;
        Truth desire = null;
        boolean deriveOccurrence = false; //if false its just the occurence time of the parent


        //todo consume and use also other meta information
        if (this.truth!=null) {
            truth = this.truth.get(T,B);
        }

        for (Term t : modifiers) {
            //String s = t.toString().replace("_", ".");//.replace("%","");
            String s;
            if (t instanceof Inheritance) {
                Inheritance i = (Inheritance)t;
                s = i.getPredicate() + "." + i.getSubject();
            }
            else {
                throw new RuntimeException("invalid meta: " + this);
            }


            switch (s) { //truth value calculation happens here


                //TODO all others also
                case "Desire.Strong": //implicit assumption: happens after, so it overwrites
                    desire = TruthFunctions.desireStrong(T, B);
                    break;
                case "Desire.Weak": //implicit assumption: happens after, so it overwrites
                    desire = TruthFunctions.desireWeak(T, B);
                    break;
                case "Desire.Negation": //implicit assumption: happens after, so it overwrites
                    desire = TruthFunctions.negation(T);
                    break;
                case "Desire.Induction": //implicit assumption: happens after, so it overwrites
                    desire = TruthFunctions.desireInd(T, B);
                    break;
                case "Desire.Deduction": //implicit assumption: happens after, so it overwrites
                    desire = TruthFunctions.desireDed(T, B);
                    break;

                case "Info.SeldomUseful":
                    if (nal.memory.random.nextFloat() > NALExecuter.seldomUsefulProbability)
                        return false;
                    break;

                case "Occurrence.Derive": //detachment
                    deriveOccurrence = true;
                    break;

                default: //only these 3 for now, can be extended later, lets go on with the rest for now
                    break;
            }
        }

        if (truth == null && task.isJudgment()) {
            System.err.println("truth rule not specified, deriving nothing: \n" + this);
            return false; //not specified!!
        }

        if (desire == null && task.isGoal()) {
            System.out.println("desire rule not specified, deriving nothing: \n" + this);
            return false; //not specified!!
        }

        //now match the rule with the task term <- should probably happen earlier ^^
        final Map<Term, Term> assign = Global.newHashMap();
        final Map<Term, Term> waste = Global.newHashMap();

        Term derive = term; //first entry is term

        //precon[0]
        //TODO checking the precondition again for every postcondition misses the point, but is easily fixable (needs to be moved down to Rule)
        if (single_premise) { //only match precondition pattern with task

            //match first rule pattern with task
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                return false;

            //now we have to apply this to the derive term
            derive = derive.substituted(assign);

        } else {

            //match first rule pattern with task
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                return false;

            //match second rule pattern with belief
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[1], belief.getTerm(), assign, waste, nal.memory.random))
                return false;

            //also check if the preconditions are met
            for (int i = 2; i < preconditions.length; i++) {
                Inheritance predicate = (Inheritance) preconditions[i];
                Term predicate_name = predicate.getPredicate();
                Term[] args = ((Product) (((SetExt) predicate.getSubject()).term(0))).terms();
                //ok apply substitution to both elements in args


                final Term arg1 = args[0].substituted(assign);
                final Term arg2 = args[1].substituted(assign);

                final String predicateNameStr = predicate_name.toString();

                switch (predicateNameStr) {
                    case "not_equal":
                        if (arg1.equals(arg2))
                            return false; //not_equal
                        break;
                    case "event":
                        //TODO refine check what it refers to, to task or belief
                        if (task.getOccurrenceTime() == Stamp.ETERNAL || belief.getOccurrenceTime() == Stamp.ETERNAL)
                            return false;
                        break;
                    case "no_common_subterm":
                        //TODO: don't we already have a function for this?
                        break;

                }
            }

            //now we have to apply this to the derive term
            derive = derive.substituted(assign);
        }

        //TODO also allow substituted evaluation on output side (used by 2 rules I think)

        Budget budget = BudgetFunctions.compoundForward(truth, derive, nal);


        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
        //a difference in occuring, not a difference in matching
        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!

        boolean allowOverlap = false; //to be refined

        if (derive instanceof Compound) {
            if (!single_premise) {

                TaskSeed<Compound> t = nal.newDoublePremise(task, belief, allowOverlap);
                if (t != null) {
                    t.term((Compound) derive).punctuation(task.punctuation)
                            .truth(truth).budget(budget);

                    if (t != null)
                        nal.deriveDouble(t);
                }

            } else {

                TaskSeed<Compound> t = nal.newSinglePremise(task, allowOverlap);
                if (t != null) {
                    t.term((Compound) derive).punctuation(task.punctuation)
                            .truth(truth).budget(budget);

                    if (t != null)
                        nal.deriveSingle(t);
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return term + "(" + Arrays.toString(modifiers) + ")";
    }

}
