package nars.util.graph;

import nars.NAR;
import nars.concept.Concept;
import nars.term.Statement;
import nars.term.Term;


abstract public class StatementGraph extends SentenceGraph {

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

    abstract public boolean containsStatement(Statement term);

    @Override
    ConceptRelation[] getRelations(Concept c) {
        Statement t = (Statement)c.getTerm();
        Term subj = t.getSubject();
        Concept subjTerm = nar.concept(subj);
        Term pred = t.getPredicate();
        Concept predTerm = nar.concept(pred);

        if (subjTerm == null || predTerm == null) {
            return null;
        }

        return new ConceptRelation[] {
                new ConceptRelation(c, subjTerm, predTerm)
        };
    }



}
