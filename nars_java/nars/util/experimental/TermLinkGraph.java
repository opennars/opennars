package nars.util.experimental;

import nars.entity.Concept;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Generates a graph of a set of Concept's TermLinks.  Each TermLink is an edge, and the
 * set of unique Concepts and Terms linked are the vertices.
 */
public class TermLinkGraph extends DirectedMultigraph<Term, TermLink> {
    
    public TermLinkGraph()  {
        super(TermLink.class);        
    }
    
    public void add(Iterable<Concept> concepts, boolean includeTermLinks, boolean includeTaskLinks/*, boolean includeOtherReferencedConcepts*/) {
        
        for (final Concept c : concepts) {
            
            final Term source = c.term;
            
            if (!containsVertex(source)) {
                addVertex(source);

                if (includeTermLinks) {
                    for (TermLink t : c.termLinks.values()) {
                        Term target = t.target;
                        if (!containsVertex(target))  {
                            addVertex(target);
                        }
                        addEdge(source, target, t);
                    }
                }

                if (includeTaskLinks) {            
                    for (TaskLink t : c.taskLinks.values()) {
                        Term target = t.target;
                        if (!containsVertex(target))  {
                            addVertex(target);
                        }        
                        addEdge(source, target, t);                    
                    }            
                }

            }
        }
        
        
    }

    
    public boolean includeLevel(int l) {
        return true;
    }

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
