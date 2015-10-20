package nars.process.concept;

import nars.Global;
import nars.Memory;
import nars.Op;
import nars.concept.Concept;
import nars.nal.nal2.Property;
import nars.nal.nal3.SetExt;
import nars.task.Task;
import nars.task.TaskSeed;
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

    private final Memory memory;
    private final On active;

    public QueryVariableExhaustiveResults(Memory m) {
        this.memory = m;
        active = m.eventInput.on(this);
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
            forEachMatch(memory.concepts, t.getTerm(), (Task bestBelief) -> {
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
                Task x = new TaskSeed().term(
                    Property.make(
                        SetExt.make(terms),
                        t.getTerm()
                    )
                ).belief().truth(1f, 0.9f).budget(t.getBudget()).present(memory)
                        .time(memory.time(), memory.time());

                //System.out.println(x);

                memory.eventInput.emit(x);
            }
        }
    }

    public void forEachMatch(Iterable<Concept> m, Compound queryTerm, Consumer<Task> withBelief) {
        FindSubst f = new FindSubst(Op.VAR_QUERY, memory.random);
        m.forEach(c -> {
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
