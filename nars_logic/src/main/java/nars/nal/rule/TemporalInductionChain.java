package nars.nal.rule;

import com.google.common.collect.Lists;
import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.Symbols;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.process.ConceptProcess;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.truth.Truth;
import nars.nal.truth.TruthFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static nars.nal.Terms.equalSubTermsInRespectToImageAndProduct;

/**
* Created by me on 2/7/15.
*/
public class TemporalInductionChain extends ConceptFireTaskTerm {


    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {

        if (!f.nal(7)) return true;

        final Task beliefTask = f.getCurrentBeliefTask();
        final Sentence belief = f.getCurrentBelief();
        if (belief == null) return true;

        final Memory memory = f.memory;

        final Term beliefTerm = belief.getTerm();


        //this is a new attempt/experiment to make nars effectively track temporal coherences
        if (beliefTerm instanceof Implication &&
                (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

            final int chainSamples = Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES;

            //prevent duplicate inductions
            Set<Object> alreadyInducted = Global.newHashSet(chainSamples);

            for (int i = 0; i < chainSamples; i++) {

                Concept next = memory.cycle.nextConcept();
                if (next == null) continue;

                Term t = next.getTerm();

                if ((t instanceof Implication) && (alreadyInducted.add(t))) {

                    Implication implication = (Implication) t;

                    if (implication.isForward() || implication.isConcurrent()) {

                        Task s = next.getStrongestBelief();
                        if (s!=null) {
                            temporalInductionChain(s, belief, f);
                            temporalInductionChain(beliefTask, s.sentence, f);
                        }
                    }
                }
            }
        }

        return true;

    }

    // { A =/> B, B =/> C } |- (&/,A,B) =/> C
    // { A =/> B, (&/,B,...) =/> C } |-  (&/,A,B,...) =/> C
    //https://groups.google.com/forum/#!topic/open-nars/L1spXagCOh4
    public static Task temporalInductionChain(final Task s1, final Sentence s2, final NAL nal) {

        //prevent trying question sentences, causes NPE
        if ((s1.getTruth() == null) || (s2.truth == null))
            return null;

        //try if B1 unifies with B2, if yes, create new judgement
        Implication S1=(Implication) s1.getTerm();
        Implication S2=(Implication) s2.getTerm();
        Term A=S1.getSubject();
        Term B1=S1.getPredicate();
        Term B2=S2.getSubject();
        Term C=S2.getPredicate();
        ArrayList<Term> args=null;

        int beginoffset=0;
        if(B2 instanceof Conjunction) {
            Conjunction CB2=((Conjunction)B2);
            if(CB2.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                if(A instanceof Conjunction && A.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                    Conjunction ConjA=(Conjunction) A;
                    args=new ArrayList(CB2.term.length+ConjA.term.length);
                    beginoffset=ConjA.length();

                    Collections.addAll(args, ConjA.term);
                } else {
                    args = new ArrayList(CB2.term.length + 1);
                    args.add(A);
                    beginoffset=1;
                }
                Collections.addAll(args, CB2.term);
            }
        }
        else {
            args= Lists.newArrayList(A, B1);
        }

        if(args==null)
            return null;

        //ok we have our B2, no matter if packed as first argument of &/ or directly, lets see if it unifies
        Term[] term = args.toArray(new Term[args.size()]);
        Term realB2 = term[beginoffset];

        Map<Term, Term> res1 = Global.newHashMap();
        Map<Term, Term> res2 = Global.newHashMap();

        if(Variables.findSubstitute(Symbols.VAR_INDEPENDENT, B1, realB2, res1, res2,nal.memory.random)) {
            //ok it unifies, so lets create a &/ term
            for(int i=0;i<term.length;i++) {
                final Term ti = term[i];
                if (ti instanceof Compound) {
                    Term ts = ((Compound)ti).applySubstitute(res1);
                    if(ts!=null)
                        term[i] = ts;
                }
                else {
                    term[i] = res1.getOrDefault(ti,ti);
                }
            }
            int order1=s1.getTemporalOrder();
            int order2=s2.getTemporalOrder();

            //check if term has a element which is equal to C
            for(Term t : term) {
                if(equalSubTermsInRespectToImageAndProduct(t, C)) {
                    return null;
                }
                for(Term u : term) {
                    if(u!=t) { //important: checking reference here is as it should be!
                        if(equalSubTermsInRespectToImageAndProduct(t, u)) {
                            return null;
                        }
                    }
                }
            }

            Term S = Conjunction.make(term,order1);
            Implication whole=Implication.make(S, C,order2);

            if(whole!=null) {
                Truth truth = TruthFunctions.induction(s1.getTruth(), s2.getTruth());
                if (truth!=null) {
                    Budget budget = BudgetFunctions.compoundForward(truth, whole, nal);
                    budget.setPriority((float) Math.min(0.99, budget.getPriority()));

                    return nal.deriveDouble(
                            nal.newTask(whole).truth(truth).budget(budget)
                                    .parent(s1, s2).temporalInductable(true));
                }
            }
        }
        return null;
    }

}
