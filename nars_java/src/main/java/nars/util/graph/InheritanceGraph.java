package nars.util.graph;

import nars.core.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;

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
        if (conf > minConfidence)
            return true;
        return false;
    }

    @Override
    public boolean allow(final CompoundTerm st) {
        Symbols.NativeOperator o = st.operator();
        
        
        
        if ((o == Symbols.NativeOperator.INHERITANCE) && includeInheritance)
            return true;
        if ((o == Symbols.NativeOperator.SIMILARITY) && includeSimilarity) {
            
            return true;
        }

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
