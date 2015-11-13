package nars.op.meta;

import nars.link.TermLink;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;
import nars.term.Term;
import nars.util.graph.TermLinkGraph;
import org.jgrapht.alg.ConnectivityInspector;

import java.util.List;
import java.util.Set;

/**
 * Created by me on 5/18/15.
 */
public class connectivity extends SyncOperator {

    @Override
    public List<Task> apply(Task<Operation> o) {

        TermLinkGraph g = new TermLinkGraph(nar);


        ConnectivityInspector<Term,TermLink> ci = new ConnectivityInspector(g);
        int set = 0;
        for (Set<Term> s : ci.connectedSets()) {
            for (Term v : s)
                System.out.println(set + ": " + v);
            set++;
        }

        //o.stop();
        return null;
    }
}
