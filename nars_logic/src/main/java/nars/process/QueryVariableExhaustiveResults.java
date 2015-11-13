package nars.process;

import nars.Global;
import nars.NAR;
import nars.Op;
import nars.nal.nal2.Property;
import nars.nal.nal3.SetExt;
import nars.task.FluentTask;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.util.event.On;

import java.util.List;
import java.util.function.Consumer;

/**
 * EXPERIMENTAL
 * aggregates matching beliefs to a query-variable task
 */
public class QueryVariableExhaustiveResults implements Consumer<Task> {

    private final NAR nar;
    private final On active;

    public QueryVariableExhaustiveResults(NAR n) {
        this.nar = n;
        active = n.memory.eventInput.on(this);
    }

    public void off() {
        active.off();
    }

    @Override
    public void accept(Task t) {
        if (t.isQuestion() && t.hasQueryVar()) {

            //Topic<Twin<Task>> ea = memory.eventAnswer;
            //List<Task> tasks = Global.newArrayList();
            List<Term> terms = Global.newArrayList();


            //TODO AIKR finite limit
            forEachMatch(nar, t.getTerm(), (Task bestBelief) -> {
                if (bestBelief!=null) {
                    //tasks.add(bestBelief);
                    terms.add(bestBelief.getTerm());
                }
            });


//            ea.emit(
//                    Tuples.twin(t, bestBelief)
//            );


            if (!terms.isEmpty()) {

                //generates a similarity group
                long now = nar.time();
                Task x = new FluentTask().term(
                    Property.make(
                        SetExt.make(terms),
                        t.getTerm()
                    )
                ).belief().truth(1f, 0.9f).budget(t.getBudget()).present(nar.memory)
                        .time(now, now);

                //System.out.println(x);

                nar.memory.eventInput.emit(x);
            }
        }
    }

    public static void forEachMatch(NAR n, Compound queryTerm, Consumer<Task> withBelief) {
        FindSubst f = new FindSubst(Op.VAR_QUERY, n.memory.random);
        n.forEachConcept(c -> {
            if (!c.hasBeliefs())
                return;

            if (f.next(queryTerm, c.getTerm(), Global.UNIFICATION_POWER)) {
                //System.out.println("match: " + queryTerm + " " + c.getBeliefs().top());
                withBelief.accept(c.getBeliefs().top());
            }
            f.clear();
        });
    }

}
