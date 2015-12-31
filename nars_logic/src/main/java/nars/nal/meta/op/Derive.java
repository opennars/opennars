package nars.nal.meta.op;

import com.google.common.base.Joiner;
import nars.Global;
import nars.Op;
import nars.Premise;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.nal.meta.BooleanCondition;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Tense;
import nars.process.ConceptProcess;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.truth.Truth;

import static nars.term.Statement.pred;
import static nars.term.Statement.subj;
import static nars.truth.TruthFunctions.eternalizedConfidence;

/**
 * Created by me on 12/30/15.
 */
public class Derive extends BooleanCondition<PremiseMatch> {

    private final String id;

    private final boolean anticipate;
    private final boolean eternalize;
    private final PremiseRule rule;
    private final Term term;
    private final BooleanCondition[] postMatch;

    public Derive(PremiseRule rule, Term term, BooleanCondition[] postMatch, boolean anticipate, boolean eternalize) {
        this.rule = rule;
        this.postMatch = postMatch;
        this.term = term;
        this.anticipate = anticipate;
        this.eternalize = eternalize;

        String i = "Derive:(";
        if (eternalize || anticipate) {
            if (eternalize && anticipate) {
                i += "{eternalize,anticipate}, ";
            } else if (eternalize && !anticipate) {
                i += "{eternalize}, ";
            } else if (anticipate && !eternalize) {
                i += "{anticipate}, ";
            }
        }

        i += term.toString();

        if (postMatch.length > 0) {
            i += ", {" + Joiner.on(',').join(postMatch) + '}';
        }

        i += ")";
        this.id = i;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String toJavaConditionString() {
        return getClass().getName() + ".set(m)";
    }

    /** set derivation callback */
    public static boolean set(PremiseMatch m) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        m.derived.set(this);
        return true;
    }

//        public void partial(RuleMatch match) {
//            Term dt = solve(match);
//            if (dt == null) return ;
//
//            //maybe this needs applied somewhre diferent
//            if (!post(match))
//                return ;
//
//            VarCachedVersionMap secondary = match.secondary;
//
//            if (!secondary.isEmpty()) {
//
//                Term rederivedTerm = dt.apply(secondary, true);
//
//                //its possible that the substitution produces an invalid term, ex: an invalid statement
//                dt = rederivedTerm;
//                if (dt == null) return;
//            }
//
//            dt = dt.normalized();
//            if (dt == null) return;
//
//
//            derive(match, dt);
//        }

    public Term solve(PremiseMatch match) {

        Term derivedTerm = match.apply(term);

        if (null == derivedTerm)
            return null;

        Compound pattern = (Compound) rule.term(0);
        Term taskpart = pattern.term(0);
        Term beliefpart = pattern.term(1);

        Term possibleSequenceHolder = null;

        if (rule.sequenceIntervalsFromBelief) {
            possibleSequenceHolder = beliefpart;
        }
        if (rule.sequenceIntervalsFromTask) {
            possibleSequenceHolder = taskpart;
        }

        if (possibleSequenceHolder != null && possibleSequenceHolder.hasAny(Op.SEQUENCE)) {
            processSequence(match, derivedTerm, possibleSequenceHolder);

        }


        return derivedTerm;
    }

    public boolean post(PremiseMatch match) {

        for (BooleanCondition p : postMatch) {
            if (!p.booleanValueOf(match))
                return false;
        }

        return true;
    }

    public void processSequence(PremiseMatch match, Term derivedTerm, Term toInvestigate) {
        int TermIsSequence = 1;
        int TermSubjectIsSequence = 2;
        int TermPredicateIsSequence = 3;

        int mode = 0; //nothing
        //int sequence_term_amount = 0;


        if (rule.sequenceIntervalsFromBelief || rule.sequenceIntervalsFromTask) {
            if (toInvestigate instanceof Sequence) {
                //sequence_term_amount = ((Sequence) toInvestigate).terms().length;
                mode = TermIsSequence;
            } else if (toInvestigate.op().isStatement()) {

                if (subj(toInvestigate) instanceof Sequence) {
                    //sequence_term_amount = ((Sequence) st.getSubject()).terms().length;
                    mode = TermSubjectIsSequence;
                } else if (pred(toInvestigate) instanceof Sequence) {
                    //sequence_term_amount = ((Sequence) st.getPredicate()).terms().length;
                    mode = TermPredicateIsSequence;
                }
            }
        }

        int Nothing = 0;
        if (mode != Nothing) {

            Sequence paste = null; //where to paste it to

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE1
            if (mode == TermIsSequence && derivedTerm instanceof Sequence) {
                paste = (Sequence) derivedTerm;
            } else if (mode == TermSubjectIsSequence && derivedTerm instanceof Statement && subj(derivedTerm) instanceof Sequence) {
                paste = (Sequence) subj(derivedTerm);
            } else if (mode == TermPredicateIsSequence && derivedTerm instanceof Statement && pred(derivedTerm) instanceof Sequence) {
                paste = (Sequence) pred(derivedTerm);
            }
            //END CODE

            Term lookat = null;
            Premise premise = match.premise;

            if (rule.sequenceIntervalsFromTask) {
                lookat = premise.getTask().term();
            } else if (rule.sequenceIntervalsFromBelief) {
                lookat = premise.getBelief() != null ?
                        premise.getBelief().term() : null;
            }

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
            Sequence copy = null; //where to copy the interval data from
            if (mode == TermIsSequence && lookat instanceof Sequence) {
                copy = (Sequence) lookat;
            } else if (lookat != null && lookat.op().isStatement()) {

                if (mode == TermSubjectIsSequence && subj(lookat) instanceof Sequence) {
                    copy = (Sequence) subj(lookat);
                } else if (mode == TermPredicateIsSequence && pred(lookat) instanceof Sequence) {
                    copy = (Sequence) pred(lookat);
                }

            }
            //END CODE

            //ok now we can finally copy the intervals.

            if (copy != null) {

                int[] copyIntervals = copy.intervals();

                if (paste != null) {


                    int a = copy.terms().length;
                    int b = paste.terms().length;
                    boolean sameLength = a == b;
                    boolean OneLess = a - 1 == b;

                    if (!sameLength && !OneLess) {
                        System.err.println("result Sequence insufficient elements; rule:" + rule);
                    }

                    int[] pasteIntervals = paste.intervals();

                    if (OneLess) {
                        match.occurrenceShift.set(copyIntervals[1]); //we shift according to first interval
                        System.arraycopy(copyIntervals, 2, pasteIntervals, 1, copyIntervals.length - 2);
                    } else if (sameLength) {
                        System.arraycopy(copyIntervals, 0, pasteIntervals, 0, copyIntervals.length);
                    }
                } else /* if (paste == null)  */ {
                    //ok we reduced to a single element, so its a one less case
                    match.occurrenceShift.set(copyIntervals[1]);
                }
            }
        }
    }

    public boolean derive(PremiseMatch m, Term t) {

        if (t == null || Variable.hasPatternVariable(t))
            return false;

        Concept c = m.premise.memory().taskConcept(t);
        if (c == null)
            return false;

        derive(m, (Compound) c.term());

        return false; //match finish
    }

    private void derive(PremiseMatch m, Compound c) {

        ConceptProcess premise = m.premise;

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

        if ((Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS || Global.DEBUG_LOG_DERIVING_RULE) && Global.DEBUG) {
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
                    new MutableTask(derived.term())
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
