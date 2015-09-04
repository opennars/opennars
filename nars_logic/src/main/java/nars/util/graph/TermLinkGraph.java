package nars.util.graph;

import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.term.Term;
import org.jgrapht.graph.DirectedMultigraph;

;

/**
 * Generates a graph of a set of Concept's TermLinks. Each TermLink is an edge,
 * and the set of unique Concepts and Terms linked are the vertices.
 */
public class TermLinkGraph extends DirectedMultigraph<Term, TermLink> {

    public TermLinkGraph() {
        super(TermLink.class);
    }

    public TermLinkGraph(NAR n) {
        this();
        add(n.memory.getCycleProcess(), true);
    }

    public TermLinkGraph add(Concept c, boolean includeTermLinks/*, boolean includeTaskLinks, boolean includeOtherReferencedConcepts*/) {
        final Term source = c.getTerm();

        if (!containsVertex(source)) {
            addVertex(source);

            if (includeTermLinks) {
                for (TermLink t : c.getTermLinks().values()) {
                    Term target = t.getTerm().getTerm();
                    if (!containsVertex(target)) {
                        addVertex(target);
                    }
                    addEdge(source, target, t);
                }
            }

                /*
                if (includeTaskLinks) {
                    for (TaskLink t : c.taskLinks.values()) {
                        Task target = t.targetTask;
                        if (!containsVertex(target)) {
                            addVertex(target);
                        }
                        addEdge(source, target, t);
                    }
                }
                */

        }

        return this;
    }

    public TermLinkGraph add(Iterable<Concept> concepts, boolean includeTermLinks/*, boolean includeTaskLinks, boolean includeOtherReferencedConcepts*/) {

        for (final Concept c : concepts) {
            add(c, includeTermLinks);

        }

        return this;
    }

    public void add(Memory memory) {
        add(memory.getCycleProcess(), true);
    }

    /*public boolean includeLevel(int l) {
        return true;
    }*/

}
