package nars.nal.concept;

import nars.Global;
import nars.Memory;
import nars.NARSeed;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.bag.impl.CurveBag;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

import java.util.HashSet;
import java.util.Set;

/** concept which holds a fixed set of beliefs or goals, and rejects others
 *  UNTESTED
 * */

public abstract class AxiomaticConcept extends Concept {

    public Set<Task> goalAxioms = Global.newHashSet(4);
    public Set<Task> beliefAxioms = Global.newHashSet(4);
    boolean restrict;

    public AxiomaticConcept(Term t, Budget b, Memory m, Bag<TermLinkKey, TermLink> ttaskLinks, Bag<Sentence, TaskLink> ttermLinks) {
        super(t, b, ttermLinks, ttaskLinks, m);
        restrict = true;
    }

    /** whether to limit beliefs/goals to those contained (by equals()) in the axioms set */
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;

        //TODO if true, remove any restricted beliefs/goals
    }

    public void clearAxioms(final boolean beliefs, final boolean goals) {

        if (beliefs) {
            beliefAxioms.clear();
            this.beliefs.clear();
        }
        if (goals) {
            goalAxioms.clear();
            this.goals.clear();
        }

        taskLinks.clear();
    }

    public void addAxiom(Task t, boolean process) {
        if (t.sentence.isJudgment())
            beliefAxioms.add(t);
        if (t.sentence.isGoal())
            goalAxioms.add(t);
        if (process)
            new DirectProcess(this, t);
    }

    @Override
    public boolean valid(Task t) {

        if (!restrict) return true;

        if ((t.sentence.isJudgment() && !beliefAxioms.isEmpty() && !beliefAxioms.contains(t)) ||
        (t.sentence.isGoal() && !goalAxioms.isEmpty() && !goalAxioms.contains(t))) {
            onInvalid(t);
            return false;
        }

        return true;
    }

    public void onInvalid(Task t) {
        /** can be overridden to discover blocked attempts to change the belief */
        System.out.println(this + " rejected: " + t);
    }

    public static NARSeed.ConceptBuilder add(Memory m, Task... defaultAxioms) {

        Set<Term> terms = new HashSet();
        for (Task d : defaultAxioms) {
            terms.add(d.getTerm());
        }

        NARSeed.ConceptBuilder cb = new NARSeed.ConceptBuilder() {

            @Override
            public Concept newConcept(Term t, Budget b, Memory m) {
                Bag<Sentence, TaskLink> ttaskLinks = new CurveBag(10, true);
                Bag<TermLinkKey, TermLink> ttermLinks = new CurveBag(10, true);;


                if (terms.contains(t)) {
                    AxiomaticConcept fbc = new AxiomaticConcept(t, b, m, ttermLinks, ttaskLinks) {

                        @Override public void onActive() {
                            //conceptualize the concept
                            for (Task d : defaultAxioms) {
                                new DirectProcess(m, d).run();
                            }
                        }
                    };



                    for (Task d : defaultAxioms) {
                        if (d.getTerm().equals(t))
                            fbc.addAxiom(d, true);
                    }

                    return fbc;
                }

                return null;
            }
        };


        m.on(cb);




        return cb;
    }


}
