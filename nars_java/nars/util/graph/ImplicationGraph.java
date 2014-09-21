package nars.util.graph;

import java.util.HashSet;
import java.util.Set;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Implication;
import nars.language.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


public class ImplicationGraph extends DirectedMultigraph<Term, Sentence> {

    Set<Sentence> sentences = new HashSet();
    Set<Term> vertices = new HashSet();

    public ImplicationGraph() {
        super(/*null*/new EdgeFactory() {

            @Override public Object createEdge(Object v, Object v1) {
                return null;
            }
            
        });
    }    

    public ImplicationGraph(NAR nar) {
        this();
        for (Concept c : nar.memory.getConcepts()) {
            for (Sentence s : c.beliefs) {
                addIfRelevant(s);
            }
        }
        
    }
    
    public void addIfRelevant(final Sentence s) {
        boolean include = false;
        Implication implication = null;
        if (s.content instanceof CompoundTerm) {
            CompoundTerm cs = (CompoundTerm)s.content;
            
            //TODO other implication types
            NativeOperator o = cs.operator();
            if ((o == NativeOperator.IMPLICATION_WHEN) || (o == NativeOperator.IMPLICATION_BEFORE)) {
                include = true;
                implication = (Implication)cs;
            }
        }
        
        if (!include)
            return;
        
        boolean added = sentences.add(s);
        if (added) {            
            //index the edge
            Term subject = implication.getSubject();
            Term predicate = implication.getPredicate();
            addVertex(subject);
            addVertex(predicate);
            addEdge(subject, predicate, s);
        }
    }
    
    
}
