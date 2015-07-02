package nars.op.meta;

import nars.Memory;
import nars.task.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.term.Term;
import nars.link.TermLink;
import nars.util.graph.TermLinkGraph;
import org.jgrapht.alg.ConnectivityInspector;

import java.util.List;
import java.util.Set;

/**
 * Created by me on 5/18/15.
 */
public class connectivity extends SynchOperator {

    @Override
    protected List<Task> execute(Operation o, Memory memory) {

        TermLinkGraph g = new TermLinkGraph(nar);

        g.add(memory);

        ConnectivityInspector<Term,TermLink> ci = new ConnectivityInspector(g);
        int set = 0;
        for (Set<Term> s : ci.connectedSets()) {
            for (Term v : s)
                System.out.println(set + ": " + v);
            set++;
        }

        o.stop(memory);
        return null;
    }
}
