package nars.process.concept;

import nars.Memory;
import nars.Op;
import nars.link.TermLink;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Compound;
import nars.term.transform.FindSubst;

import java.util.function.Consumer;

/**
 * Reports all matching beliefs to a query-variable task
 * incomplete
 * TODO use volume and substructure hash to accelerate queries
 */
public class QueryVariableExhaustiveResults extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptProcess f, @Deprecated TermLink termLink) {
        Task t = f.getTask();
        if (t.isQuestion() && t.hasQueryVar()) {
            forEachMatch(f.memory, t.getTerm(), f);
        }
        return true;
    }

    public static void forEachMatch(Memory m, Compound queryTerm, Consumer<Task> withBelief) {
        FindSubst f = new FindSubst(Op.VAR_QUERY, m.random);
        m.getCycleProcess().forEachConcept(c -> {
            if (!c.hasBeliefs())
                return;

            f.clear();
            if (f.next(queryTerm, c.getTerm())) {
                System.out.println("match: " + queryTerm + " " + c.getBeliefs().top());
                withBelief.accept(c.getBeliefs().top());
            }

        });
    }

}
