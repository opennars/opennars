package nars.op.meta;

import nars.nal.nal8.Execution;
import nars.nal.nal8.operator.SyncOperator;
import nars.term.Term;
import nars.util.graph.TermLinkGraph;
import org.jgrapht.alg.ConnectivityInspector;

import java.util.Set;

/**
 * Created by me on 5/18/15.
 */
public class connectivity extends SyncOperator {

    @Override
    public void execute(Execution e) {

        TermLinkGraph g = new TermLinkGraph(e.nar);


        ConnectivityInspector<Term,String> ci = new ConnectivityInspector(g);
        int set = 0;
        for (Set<Term> s : ci.connectedSets()) {
            for (Term v : s)
                System.out.println(set + ": " + v);
            set++;
        }

        //o.stop();
    }
}
