package nars.util.graph;

import nars.$;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.link.TermLinkTemplate;
import nars.term.Term;
import nars.term.compound.Compound;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DirectedPseudograph;

import java.io.PrintStream;
import java.util.Set;

/**
 * Generates a graph of a set of Concept's TermLinks. Each TermLink is an edge,
 * and the set of unique Concepts and Terms linked are the vertices.
 */
public class TermLinkGraph2 extends DirectedPseudograph<Term, Compound> {

    public TermLinkGraph2() {
        super(new EdgeFactory<Term, Compound>() {
            @Override
            public Compound createEdge(Term a, Term b) {
                return $.p(a,b);
            }
        });
    }


    public TermLinkGraph2(NAR n) {
        this();
        add(n, true);
    }

    public TermLinkGraph2(Concept... c) {
        this();
        for (Concept x : c)
            add(x, true);
    }

    @Override
    public String toString() {
        return '[' + vertexSet().toString() + ", " + edgeSet().toString() + ']';
    }

    public void print(PrintStream out) {

        Set<Term> vs = vertexSet();

        out.println(getClass().getSimpleName() + " numTerms=" + vs.size() + ", numTermLinks=" + edgeSet().size() );
        out.print("\t");
        out.println(this);

        for (Term t : vs) {
            out.print(t + ":  ");
            outgoingEdgesOf(t).forEach(e ->
                out.print("  " + e)
            );
            out.println();
        }
        out.println();

    }

    public static class TermLinkTemplateGraph extends TermLinkGraph2 {

        public TermLinkTemplateGraph(NAR n) {
            super(n);
        }

        /** add the termlink templates instead of termlinks */
        @Override protected void addTermLinks(Concept c) {
            final Term sourceTerm = c.getTerm();

            for (TermLinkTemplate t : c.getTermLinkTemplates()) {
                final Term targetTerm = t.getTerm().getTerm();
                if (!containsVertex(targetTerm)) {
                    addVertex(targetTerm);
                }

                addEdge(sourceTerm, targetTerm );
            }
        }
    }

    public TermLinkGraph2 add(Concept c, boolean includeTermLinks/*, boolean includeTaskLinks, boolean includeOtherReferencedConcepts*/) {
        final Term source = c.getTerm();

        if (!containsVertex(source)) {
            addVertex(source);
        }

        if (includeTermLinks) {
            addTermLinks(c);
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


        return this;
    }

    protected void addTermLinks(Concept c) {
        if (c == null)
            throw new RuntimeException("null concept");

        final Term cterm = c.getTerm();

        Bag<TermLinkKey, TermLink> tl = c.getTermLinks();
        if (tl == null) return;

        for (TermLink t : tl.values()) {
            final Term target = t.getTerm();
            if (!containsVertex(target)) {
                addVertex(target);
            }

            addEdge(cterm, target);
        }
    }


    public TermLinkGraph2 add(NAR n, boolean includeTermLinks/*, boolean includeTaskLinks, boolean includeOtherReferencedConcepts*/) {

        n.forEachConcept(c -> add(c, includeTermLinks));

        return this;
    }

    public boolean isConnected() {
        ConnectivityInspector<Term, TermLink> ci = new ConnectivityInspector(this);
        return ci.isGraphConnected();
    }

//    public void add(Memory memory) {
//        add(memory.getCycleProcess(), true);
//    }

    /*public boolean includeLevel(int l) {
        return true;
    }*/

}
