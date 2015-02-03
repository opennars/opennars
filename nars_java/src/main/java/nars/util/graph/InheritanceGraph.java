package nars.util.graph;

import nars.core.NAR;
import nars.logic.NALOperator;
import nars.logic.entity.*;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class InheritanceGraph extends SentenceGraph {

    float minConfidence = 0.01f;
    private final boolean includeInheritance;
    private final boolean includeSimilarity;
    
    public InheritanceGraph(NAR nar) {
        this(nar, true, true);
    }

    public InheritanceGraph(NAR nar, boolean includeInheritance, boolean includeSimilarity) {
        super(nar.memory);
        this.includeInheritance = includeInheritance;
        this.includeSimilarity = includeSimilarity;
    }
    
    @Override
    public boolean allow(final Sentence s) {        
        float conf = s.truth.getConfidence();
        return conf > minConfidence;
    }

    @Override
    public boolean allow(final CompoundTerm st) {
        NALOperator o = st.operator();
        
        
        
        if ((o == NALOperator.INHERITANCE) && includeInheritance)
            return true;
        if ((o == NALOperator.SIMILARITY) && includeSimilarity)
            return true;

        return false;
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
