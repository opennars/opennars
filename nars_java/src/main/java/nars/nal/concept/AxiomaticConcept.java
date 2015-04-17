package nars.nal.concept;

import nars.Global;
import nars.Memory;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.budget.bag.CurveBag;
import nars.nal.ConceptBuilder;
import nars.nal.DirectProcess;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

import java.util.HashSet;
import java.util.Set;

/** concept which holds a fixed set of beliefs or goals, and rejects others */
public class AxiomaticConcept extends Concept {

    public Set<Task> axioms = Global.newHashSet(4);

    public AxiomaticConcept(Term t, Budget b, Memory m, Bag<TermLinkKey, TermLink> ttaskLinks, Bag<String, TaskLink> ttermLinks) {
        super(t, b, ttermLinks, ttaskLinks, m);
    }

    public void clearAxioms(final boolean beliefs, final boolean goals) {
        axioms.clear();
        axioms.removeIf( t -> {
            if (t.sentence.isJudgment() && beliefs) return true;
            if (t.sentence.isGoal() && goals) return true;
            return false;
        });
        if (beliefs)
            this.beliefs.clear();
        if (goals)
            this.goals.clear();

        taskLinks.clear();
    }

    public void addAxiom(Task t, boolean process) {
        axioms.add(t);
        if (process)
            new DirectProcess(this, t);
    }

    @Override
    public boolean valid(Task t) {
        if (!(t.sentence.isGoal() || t.sentence.isJudgment())) {
            //allow all questions and quests
            return true;
        }

        if (axioms.contains(t)) {
            return true;
        }
        else {
            onInvalid(t);
            return false;
        }
    }

    public void onInvalid(Task t) {
        /** can be overridden to discover blocked attempts to change the belief */
    }

    public static ConceptBuilder add(Memory m, Task... defaultAxioms) {

        Set<Term> terms = new HashSet();
        for (Task d : defaultAxioms) {
            terms.add(d.getTerm());
        }

        ConceptBuilder cb = new ConceptBuilder() {

            @Override
            public Concept newConcept(Budget b, Term t, Memory m) {
                Bag<String, TaskLink> ttaskLinks = new CurveBag(10, true);
                Bag<TermLinkKey, TermLink> ttermLinks = new CurveBag(10, true);;


                if (terms.contains(t)) {
                    AxiomaticConcept fbc = new AxiomaticConcept(t, b, m, ttermLinks, ttaskLinks);



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

        //conceptualize the concept
        for (Task d : defaultAxioms) {
            new DirectProcess(m, d).run();
        }

        return cb;
    }
}
