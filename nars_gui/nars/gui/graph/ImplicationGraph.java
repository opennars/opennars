package nars.gui.graph;

import com.google.common.util.concurrent.AtomicDouble;
import nars.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class ImplicationGraph extends SentenceGraph {

    float minConfidence = 0.01f;
    private final boolean includeImplication;
    private final boolean includeEquivalence;

    public ImplicationGraph(NAR nar, boolean includeImplication, boolean includeEquivalence, AtomicDouble minConceptPri) {
        super(nar.memory, minConceptPri);
        this.includeImplication = includeImplication;
        this.includeEquivalence = includeEquivalence;
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
        
        
        
        if ((o == Symbols.NativeOperator.IMPLICATION ||
             o == Symbols.NativeOperator.IMPLICATION_BEFORE || 
             o == Symbols.NativeOperator.IMPLICATION_AFTER ||
             o == Symbols.NativeOperator.IMPLICATION_WHEN) && includeImplication)
            return true;
        if ((o == Symbols.NativeOperator.EQUIVALENCE ||
             o == Symbols.NativeOperator.EQUIVALENCE_AFTER ||
             o == Symbols.NativeOperator.EQUIVALENCE_WHEN) && includeEquivalence) {
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
