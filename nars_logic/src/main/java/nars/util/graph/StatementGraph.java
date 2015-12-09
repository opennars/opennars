package nars.util.graph;

import nars.NAR;
import nars.concept.Concept;
import nars.term.Statement;
import nars.term.Term;

import static nars.term.Statement.pred;
import static nars.term.Statement.subj;


public abstract class StatementGraph extends SentenceGraph {

    public StatementGraph(NAR nar) {
        this(nar, true);
    }

    public StatementGraph(NAR nar, boolean directed) {
        super(nar, directed);
    }


    @Override
    boolean containsRelation(Concept c) {
        if (c.getTerm() instanceof Statement)
            return containsStatement((Statement)c.getTerm());
        return false;
    }

    public abstract boolean containsStatement(Statement term);

    @Override
    ConceptRelation[] getRelations(Concept c) {
        Term tt = c.getTerm();
        if (!(tt.op().isStatement())) return null;

        Term subj = subj(tt);
        Concept subjTerm = nar.concept(subj);
        Term pred = pred(tt);
        Concept predTerm = nar.concept(pred);

        if (subjTerm == null || predTerm == null) {
            return null;
        }

        return new ConceptRelation[] {
                new ConceptRelation(c, subjTerm, predTerm)
        };
    }



}
