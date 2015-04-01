package nars.util.graph;

import nars.Memory;
import nars.nal.Item;
import nars.nal.Sentence;
import nars.nal.Statement;
import nars.nal.term.Compound;
import nars.nal.term.Term;


public abstract class StatementGraph extends SentenceGraph {
    float minConfidence = 0.01f;

    public StatementGraph(Memory memory) {
        super(memory);
    }

    @Override
    public boolean allow(final Sentence s) {
        float conf = s.truth.getConfidence();
        return conf > minConfidence;
    }

    @Override
    public boolean add(Sentence s, Compound ct, Item c) {
        if (ct instanceof Statement) {
            Statement st = (Statement)ct;
            Term subject = st.getSubject();
            Term predicate = st.getPredicate();
            addVertex(subject);
            addVertex(predicate);
            addEdge(subject, predicate, s);
            return true;
        }
        return false;

    }
}
