package nars.util.experimental;

import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.util.NARGraph.NAREdge;
import nars.language.Term;
import org.jgrapht.DirectedGraph;
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
            
            final Term source = c.getTerm();
            
            if (!containsVertex(source)) {
                addVertex(source);

                if (includeTermLinks) {
                    for (TermLink t : c.termLinks.nameTable.values()) {
                        Term target = t.getTarget();
                        if (!containsVertex(target))  {
                            addVertex(target);
                        }
                        addEdge(source, target, t);
                    }
                }

                if (includeTaskLinks) {            
                    for (TaskLink t : c.taskLinks.nameTable.values()) {
                        Term target = t.getTarget();
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
}
