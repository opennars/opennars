package nars.gui.graph;

import nars.main.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.main.NAR.PortableDouble;
import nars.language.Statement;
import nars.language.Term;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class InheritanceGraph extends SentenceGraph {

    float minConfidence = 0.01f;
    private final boolean includeInheritance;
    private final boolean includeSimilarity;

    public InheritanceGraph(NAR nar, boolean includeInheritance, boolean includeSimilarity, PortableDouble minConceptPri) {
        super(nar.memory, minConceptPri);
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
            System.out.println(subject.toString().trim() + " " +
                               predicate.toString().trim()+" " +
                               s.truth.getExpectation() +
                               s.truth.getFrequency() + " " + 
                               s.truth.getConfidence() + " " +
                               " Inheritance");
            addEdge(subject, predicate, s);        
            return true;
        }
        return false;
        
    }    
    
    
}
