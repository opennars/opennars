package nars.util.graph;

import nars.core.Memory;
import nars.logic.entity.*;


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
    public boolean add(Sentence s, CompoundTerm ct, Item c) {
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
