package nars.util.graph;

import nars.NAR;
import nars.concept.Concept;
import nars.util.data.ConceptSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores relationships of certain sentences that relate other concepts
 * TODO store a set of 'missing' vertices that are not available
 * when a relation edge is created. then watch for them to appear
 * TODO when a vertex is removed and the superterm edge still exists,
 * add it to the missing set.
 */
public abstract class SentenceGraph extends ConceptGraph<SentenceGraph.ConceptRelation> {

    public final ConceptSet edgeConcepts;

    public static class ConceptRelation {
        public final Concept edge;
        public final Concept from;
        public final Concept to;
        private final int hash;

        public ConceptRelation(Concept relation, Concept from, Concept to) {
            edge = relation;
            this.from = from;
            this.to = to;
            hash = Objects.hash(relation, from, to);
        }

        @Override
        public String toString() {
            return edge.toString();
        }

        @Override
        public boolean equals(Object obj) {
            ConceptRelation other = (ConceptRelation)obj;
            return edge.equals(other.edge) && from.equals(other.from) && to.equals(other.to);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }



    protected SentenceGraph(NAR nar, boolean directed) {
        super(nar);
        edgeConcepts = new ConceptSet(nar) {

            /** buffer the concepts until the end of the cycle to help ensure that subterms will also be conceptualized, otherwise edges will fail tot be created */
            List<Concept> toAdd = new ArrayList();

            @Override
            protected void onCycle() {
                super.onCycle();
                toAdd.forEach(SentenceGraph.this::addConcept);
            }

            @Override
            public boolean contains(Concept c) {
                return containsRelation(c);
            }

            @Override
            protected boolean onConceptActive(Concept c) {
                if (super.onConceptActive(c)) {
                    toAdd.add(c);
                    return true;
                }
                return false;
            }

            @Override
            protected boolean onConceptForget(Concept c) {
                if (super.onConceptForget(c)) {
                    ConceptRelation[] cr = getRelations(c);
                    for (ConceptRelation r : cr) {
                        graph.removeEdge(r);
                    }
                    return true;
                }
                return false;
            }
        };
    }

    /** if returns true, the two concepts referred by the compound
     * will be included in the graph.  the concept itself
     * represents the edge(s) that will be created between them.
     */
    abstract boolean containsRelation(Concept c);


    /**
     * create the set of relation edges which represent the relationship
     * concept.
     */
    abstract ConceptRelation[] getRelations(Concept c);

    @Override
    public boolean contains(Concept c) {
        return graph.containsVertex(c);
    }

    protected void addConcept(Concept c) {
        ConceptRelation[] cr = getRelations(c);
        if (cr == null) return;
        for (ConceptRelation r : cr) {
            graph.addVertex(r.from);
            graph.addVertex(r.to);
            graph.addEdge(r.from, r.to, r);
        }
    }


}
