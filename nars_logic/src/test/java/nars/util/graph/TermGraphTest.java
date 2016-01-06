package nars.util.graph;

import com.gs.collections.api.tuple.primitive.ObjectIntPair;
import com.gs.collections.impl.tuple.primitive.PrimitiveTuples;
import nars.$;
import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;
import org.jgrapht.experimental.equivalence.UniformEquivalenceComparator;
import org.jgrapht.experimental.isomorphism.GraphIsomorphismInspector;
import org.jgrapht.experimental.isomorphism.GraphOrdering;
import org.jgrapht.experimental.isomorphism.IsomorphismRelation;
import org.jgrapht.experimental.permutation.CollectionPermutationIter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.util.PrefetchIterator;
import org.junit.Test;

import java.util.*;

import static nars.$.$;


public class TermGraphTest {
    public static final Compound ROOT = $.p(new Term[] { });


//    NAR n = new Default();


//    @Test
//    public void testOutgoingTermLinks() {
//        n.believe("<a =/> b>");
//        n.run(4);
//
//        n.concept("a").termLinks.printAll(System.err);
//        n.concept("b").termLinks.printAll(System.err);
//
//        TermGraph g = new TermGraph(n) {
//
//            @Override public boolean include(Concept c, TermLink l, boolean towardsSubterm) {
//                //System.out.println("considering " + l + " of " + c);
//                //System.out.println("  target=" + l.getTarget() + " , term=" + l.getTerm() + " , type=" + l.type);
//                return true;
//            }
//        };
//        g.outgoingEdgesOf(n.concept("a"));
//    }
//
//    @Test public void testInheritance() {
//
//        //TODO complete this
//
//        n.believe("<a --> b>");
//        n.believe("<c <-> d>");
//        n.run(4);
//
////        System.err.println("A's termlinks");
////        n.concept("a").termLinks.printAll(System.err);
////        System.err.println("B's termlinks");
////        n.concept("b").termLinks.printAll(System.err);
////        System.err.println("<a --> b>'s termlinks");
////        n.concept("<a --> b>").termLinks.printAll(System.err);
//
//        TermGraph g = new TermGraph.ParameterizedTermGraph(n, NALOperator.INHERITANCE, true, true);
//
//        assertEquals(1, g.incomingEdgesOf(n.concept("a")).size());
//        assertEquals(1, g.outgoingEdgesOf(n.concept("a")).size());
//
//        assertEquals(1, g.outgoingEdgesOf(n.concept("b")).size());
//        assertEquals(1, g.incomingEdgesOf(n.concept("b")).size());
//
//        assertEquals(0, g.incomingEdgesOf(n.concept("c")).size());
//        assertEquals(0, g.outgoingEdgesOf(n.concept("c")).size());
//        assertEquals(0, g.incomingEdgesOf(n.concept("d")).size());
//        assertEquals(0, g.outgoingEdgesOf(n.concept("d")).size());
//
//        //test disabling superterm direction
//        g = new TermGraph.ParameterizedTermGraph(n, NALOperator.INHERITANCE, true, true);
//        System.out.println(g.incomingEdgesOf(n.concept("a")));
//        System.out.println(g.outgoingEdgesOf(n.concept("a")));
//        assertEquals(0, g.incomingEdgesOf(n.concept("a")).size());
//        assertEquals(1, g.outgoingEdgesOf(n.concept("a")).size());
//        assertEquals(1, g.outgoingEdgesOf(n.concept("b")).size());
//        assertEquals(0, g.incomingEdgesOf(n.concept("b")).size());
//
//    }

    @Test
    public void testIso() {
        //testIsomorphism( "<%a-->%b>", "<a-->b>" );
        //testIsomorphism( "<{%a,%b}-->%c>", "<{x,y}-->d>" );
        testIsomorphism( "<{%a,%b}-->%c>", "<{x,y}-->{w,z}>" );


        //testIsomorphism( "<%a<->%b>", "<a<->b>" );
    }

    public void testIsomorphism(String _pattern, String _term) {

        TermGraph pattern = new TermGraph($(_pattern));
        TermGraph term = new TermGraph($(_term));

        //Graph<Term, Pair<Compound,Integer>> pattern;

        EquivalenceComparator<Term, Graph<Term, ObjectIntPair<Compound>>>
                vertexCompare = new EquivalenceComparator<Term, Graph<Term, ObjectIntPair<Compound>>>() {

            public boolean isRoot(Term s1) {
                return s1==pattern.root || s1 == term.root;
            }

            public boolean areRoots(Term s1, Term s2) {
                return isRoot(s1) && isRoot(s2);
            }

            @Override
            public boolean equivalenceCompare(Term s1, Term s2, Graph<Term, ObjectIntPair<Compound>> g1, Graph<Term, ObjectIntPair<Compound>> g2) {
                System.out.println("compare " + s1 + ' ' + s2);
                if ( areRoots(s1, s2) ||
                        s1.op(Op.VAR_PATTERN) || (s2.op(Op.VAR_PATTERN)))
                    return true;
                else {
                    return s1 instanceof Compound ? s1.op() == s2.op() : s1.equals(s2);
                }
            }

            @Override
            public int equivalenceHashcode(Term t, Graph<Term, ObjectIntPair<Compound>> g) {
                if (isRoot(t))
                    return 0;
                return t instanceof Compound ? t.op().hashCode() : t.hashCode();
                //return Util.hashCombine(t.hashCode(), g.hashCode());
            }
        };


        EquivalenceComparator<ObjectIntPair<Compound>, Graph<Term, ObjectIntPair<Compound>>> edgeCompare =
                new EquivalenceComparator<ObjectIntPair<Compound>, Graph<Term, ObjectIntPair<Compound>>>() {
                    @Override
                    public boolean equivalenceCompare(ObjectIntPair<Compound> e1,
                                                      ObjectIntPair<Compound> e2,
                                                      Graph<Term, ObjectIntPair<Compound>> g1,
                                                      Graph<Term, ObjectIntPair<Compound>> g2) {
                        return e1.getOne().isCommutative() || e1.getTwo() == e2.getTwo();
                    }

                    @Override
                    public int equivalenceHashcode(ObjectIntPair<Compound> e, Graph<Term, ObjectIntPair<Compound>> context) {
                        return e.getOne().isCommutative() ? 0 : e.getTwo();
                    }
                }
                ;

        GraphIsomorphismInspector vf2 = new PermutationIsomorphismInspector(
                //2 /* TREE */,
                pattern, term, vertexCompare, edgeCompare
        );


        System.out.println("\n");

        System.out.println(_term + ": " + term);
        System.out.println(_pattern + ": " + pattern);
        System.out.println(vf2.isIsomorphic());

        Iterator<GraphMapping<String, DefaultEdge>> iter = vf2;
        while (iter.hasNext()) {
            GraphMapping<String, DefaultEdge> x = iter.next();
            System.out.println(x);
        }


//        Set<String> mappings =
//                new HashSet<String>(Arrays.asList("[v1=v1 v2=v2 v3=v3]",
//                        "[v1=v1 v2=v3 v3=v2]",
//                        "[v1=v2 v2=v1 v3=v3]",
//                        "[v1=v2 v2=v3 v3=v1]",
//                        "[v1=v3 v2=v1 v3=v2]",
//                        "[v1=v3 v2=v2 v3=v1]"));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(true, mappings.remove(iter.next().toString()));
//        assertEquals(false, iter.hasNext());


//        /*
//         *   1 ---> 2 <--- 3
//         */
//        DefaultDirectedGraph<Integer, DefaultEdge> g2 =
//                new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
//
//        g2.addVertex(1);
//        g2.addVertex(2);
//        g2.addVertex(3);
//
//        g2.addEdge(1, 2);
//        g2.addEdge(3, 2);
//
//
//        VF2GraphIsomorphismInspector<Integer, DefaultEdge> vf3 =
//                new VF2GraphIsomorphismInspector<Integer, DefaultEdge>(g2, g2);
//
//        Iterator<GraphMapping<Integer, DefaultEdge>> iter2 = vf3.getMappings();
//
//        Set<String> mappings2 =
//                new HashSet<String>(Arrays.asList("[1=1 2=2 3=3]",
//                        "[1=3 2=2 3=1]"));
//        assertEquals(true, mappings2.remove(iter2.next().toString()));
//        assertEquals(true, mappings2.remove(iter2.next().toString()));
//        assertEquals(false, iter2.hasNext());

    }


    private class TermGraph extends SimpleDirectedGraph<Term, ObjectIntPair<Compound>> {

        private final Compound root;

        public TermGraph(Compound t) {
            super((Class)null);

            this.root = t;

            addCompound(t);
        }

        private void addCompound(Compound t) {
            addVertex(t);

            int n = 0;
            for (Term x : t.terms()) {
                addVertex(x);

                ObjectIntPair<Compound> e =
                            PrimitiveTuples.pair(t, n);

                addEdge(x, t, e);
                if (x instanceof Compound)
                    addCompound((Compound) x);
                n++;
            }
        }
    }

    static class PermutationIsomorphismInspector<V, E>
            extends AbstractExhaustiveIsomorphismInspector<V, E>
    {


        /**
         * @param graph1
         * @param graph2
         * @param vertexChecker eq. group checker for vertexes. If null,
         * UniformEquivalenceComparator will be used as default (always return true)
         * @param edgeChecker eq. group checker for edges. If null,
         * UniformEquivalenceComparator will be used as default (always return true)
         */
        public PermutationIsomorphismInspector(
                Graph<V, E> graph1,
                Graph<V, E> graph2,

                // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
                // E>
                EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
                EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
        {
            super(graph1, graph2, vertexChecker, edgeChecker);
        }

        /**
         * Constructor which uses the default comparators.
         *
         * @see AbstractExhaustiveIsomorphismInspector#AbstractExhaustiveIsomorphismInspector(Graph,
         * Graph)
         */
        public PermutationIsomorphismInspector(
                Graph<V, E> graph1,
                Graph<V, E> graph2)
        {
            super(graph1, graph2);
        }



        /**
         * Creates the permutation iterator, not dependant on equality group, or the
         * other vertexset.
         *
         * @param vertexSet1 FIXME Document me
         * @param vertexSet2 FIXME Document me
         *
         * @return the permutation iterator
         */
        @Override
        protected CollectionPermutationIter<V> createPermutationIterator(
                Set<V> vertexSet1,
                Set<V> vertexSet2)
        {
            return new CollectionPermutationIter<V>(vertexSet2);
        }

        /**
         * FIXME Document me FIXME Document me
         *
         * @param vertexSet1 FIXME Document me
         * @param vertexSet2 FIXME Document me
         *
         * @return FIXME Document me
         */
        @Override
        protected boolean areVertexSetsOfTheSameEqualityGroup(
                Set<V> vertexSet1,
                Set<V> vertexSet2)
        {
            if (vertexSet1.size() != vertexSet2.size()) {
                return false;
            }
            Iterator<V> iter2 = vertexSet2.iterator();

            // only check hasNext() of one , cause they are of the same size
            for (Iterator<V> iter1 = vertexSet1.iterator(); iter1.hasNext();) {
                V vertex1 = iter1.next();
                V vertex2 = iter2.next();
                if (!this.vertexComparator.equivalenceCompare(
                        vertex1,
                        vertex2,
                        this.graph1,
                        this.graph2))
                {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Abstract base for isomorphism inspectors which exhaustively test the possible
     * mappings between graphs. The current algorithms do not support graphs with
     * multiple edges (Multigraph / Pseudograph). For the maintainer: The reason is
     * the use of GraphOrdering which currently does not support all graph types.
     *
     * @author Assaf Lehr
     * @since May 20, 2005 ver5.3
     */
    abstract static class AbstractExhaustiveIsomorphismInspector<V, E>
            implements GraphIsomorphismInspector<IsomorphismRelation>
    {


        public static EquivalenceComparator<Object, Object>
                edgeDefaultIsomorphismComparator =
                new UniformEquivalenceComparator<Object, Object>();
        public static EquivalenceComparator<Object, Object>
                vertexDefaultIsomorphismComparator =
                new UniformEquivalenceComparator<Object, Object>();



        protected EquivalenceComparator<? super E, ? super Graph<V, ? super E>>
                edgeComparator;
        protected EquivalenceComparator<? super V, ? super Graph<? super V, E>>
                vertexComparator;

        protected Graph<V, E> graph1;
        protected Graph<V, E> graph2;

        private PrefetchIterator<IsomorphismRelation> nextSupplier;

        // kept as member, to ease computations
        private GraphOrdering lableGraph1;
        private LinkedHashSet<V> graph1VertexSet;
        private LinkedHashSet<E> graph2EdgeSet;
        private CollectionPermutationIter<V> vertexPermuteIter;
        private Set<V> currVertexPermutation; // filled every iteration, used in the



        // result relation.

        /**
         * @param graph1
         * @param graph2
         * @param vertexChecker eq. group checker for vertexes. If null,
         * UniformEquivalenceComparator will be used as default (always return true)
         * @param edgeChecker eq. group checker for edges. If null,
         * UniformEquivalenceComparator will be used as default (always return true)
         */
        public AbstractExhaustiveIsomorphismInspector(
                Graph<V, E> graph1,
                Graph<V, E> graph2,

                // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
                // E>
                EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
                EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
        {
            this.graph1 = graph1;
            this.graph2 = graph2;

            this.vertexComparator = vertexChecker != null ? vertexChecker : vertexDefaultIsomorphismComparator;

            // Unlike vertexes, edges have better performance, when not tested for
            // Equivalence, so if the user did not supply one, use null
            // instead of edgeDefaultIsomorphismComparator.

            if (edgeChecker != null) {
                this.edgeComparator = edgeChecker;
            }

            init();
        }

        /**
         * Constructor which uses the default comparators.
         *
         * @param graph1
         * @param graph2
         *
         * @see #AbstractExhaustiveIsomorphismInspector(Graph,Graph,EquivalenceComparator,EquivalenceComparator)
         */
        public AbstractExhaustiveIsomorphismInspector(
                Graph<V, E> graph1,
                Graph<V, E> graph2)
        {
            this(
                    graph1,
                    graph2,
                    edgeDefaultIsomorphismComparator,
                    vertexDefaultIsomorphismComparator);
        }



        /**
         * Inits needed data-structures , among them:
         * <li>LabelsGraph which is a created in the image of graph1
         * <li>vertexPermuteIter which is created after the vertexes were divided to
         * equivalence groups. This saves order-of-magnitude in performance, because
         * the number of possible permutations dramatically decreases.
         *
         * <p>for example: if the eq.group are even/odd - only two groups. A graph
         * with consist of 10 nodes of which 5 are even , 5 are odd , will need to
         * test 5!*5! (14,400) instead of 10! (3,628,800).
         *
         * <p>besides the EquivalenceComparator`s supplied by the user, we also use
         * predefined topological comparators.
         */
        private void init()
        {
            this.nextSupplier =
                    new PrefetchIterator<IsomorphismRelation>(

                            // XXX hb 280106: I don't understand this warning, yet :-)
                            new NextFunctor());

            this.graph1VertexSet = new LinkedHashSet<V>(this.graph1.vertexSet());

            // vertexPermuteIter will be null, if there is no match
            this.vertexPermuteIter =
                    createPermutationIterator(
                            this.graph1VertexSet,
                            this.graph2.vertexSet());

            this.lableGraph1 =
                    new GraphOrdering<V, E>(
                            this.graph1,
                            this.graph1VertexSet,
                            this.graph1.edgeSet());

            this.graph2EdgeSet = new LinkedHashSet<E>(this.graph2.edgeSet());
        }

        /**
         * Creates the permutation iterator for vertexSet2 . The subclasses may make
         * either cause it to depend on equality groups or use vertexSet1 for it.
         *
         * @param vertexSet1 [i] may be reordered
         * @param vertexSet2 [i] may not.
         *
         * @return permutation iterator
         */
        protected abstract CollectionPermutationIter<V> createPermutationIterator(
                Set<V> vertexSet1,
                Set<V> vertexSet2);

        /**
         * <p>1. Creates a LabelsGraph of graph1 which will serve as a source to all
         * the comparisons which will follow.
         *
         * <p>2. extract the edge array of graph2; it will be permanent too.
         *
         * <p>3. for each permutation of the vertexes of graph2, test :
         *
         * <p>3.1. vertices
         *
         * <p>3.2. edges (in labelsgraph)
         *
         * <p>Implementation Notes and considerations: Let's consider a trivial
         * example: graph of strings "A","B","C" with two edges A->B,B->C. Let's
         * assume for this example that the vertex comparator always returns true,
         * meaning String value does not matter, only the graph structure does. So
         * "D" "E" "A" with D->E->A will be isomorphic , but "A","B,"C"with
         * A->B,A->C will not.
         *
         * <p>First let's extract the important info for isomorphism from the graph.
         * We don't care what the vertexes are, we care that there are 3 of them
         * with edges from first to second and from second to third. So the source
         * LabelsGraph will be: vertexes:[1,2,3] edges:[[1->2],[2->3]] Now we will
         * do several permutations of D,E,A. A few examples: D->E , E->A
         * [1,2,3]=[A,D,E] so edges are: 2->3 , 3->1 . does it match the source? NO.
         * [1,2,3]=[D,A,E] so edges are: 1->3 , 3->2 . no match either.
         * [1,2,3]=[D,E,A] so edges are: 1->2 , 2->3 . MATCH FOUND ! Trivial
         * algorithm: We will iterate on all permutations
         * [abc][acb][bac][bca][cab][cba]. (n! of them,3!=6) For each, first compare
         * vertexes using the VertexComparator(always true). Then see that the edges
         * are in the exact order 1st->2nd , 2nd->3rd. If we found a match stop and
         * return true, otherwise return false; we will compare vetices and edges by
         * their order (1st,2nd,3rd,etc) only. Two graphs are the same, by this
         * order, if: 1. for each i, sourceVertexArray[i] is equivalent to
         * targetVertexArray[i] 2. for each vertex, the edges which start in it (it
         * is the source) goes to the same ordered vertex. For multiple ones, count
         * them too.
         *
         * @return IsomorphismRelation for a permutation found, or null if no
         * permutation was isomorphic
         */
        private IsomorphismRelation<V, E> findNextIsomorphicGraph()
        {
            boolean result = false;
            IsomorphismRelation<V, E> resultRelation = null;
            if (this.vertexPermuteIter != null) {
                // System.out.println("Souce  LabelsGraph="+this.lableGraph1);
                while (this.vertexPermuteIter.hasNext()) {
                    currVertexPermutation = this.vertexPermuteIter.getNextSet();

                    // compare vertexes
                    if (!areVertexSetsOfTheSameEqualityGroup(
                            this.graph1VertexSet,
                            currVertexPermutation))
                    {
                        continue; // this one is not iso, so try the next one
                    }

                    // compare edges
                    GraphOrdering<V, E> currPermuteGraph =
                            new GraphOrdering<V, E>(
                                    this.graph2,
                                    currVertexPermutation,
                                    this.graph2EdgeSet);

                    // System.out.println("target LablesGraph="+currPermuteGraph);
                    if (this.lableGraph1.equalsByEdgeOrder(currPermuteGraph)) {
                        // create result object.
                        resultRelation =
                                new IsomorphismRelation<V, E>(
                                        new ArrayList<V>(graph1VertexSet),
                                        new ArrayList<V>(currVertexPermutation),
                                        graph1,
                                        graph2);

                        // if the edge comparator exists, check equivalence by it
                        boolean edgeEq =
                                areAllEdgesEquivalent(
                                        resultRelation,
                                        this.edgeComparator);
                        if (edgeEq) // only if equivalent

                        {
                            result = true;
                            break;
                        }
                    }
                }
            }

            return result == true ? resultRelation : null;
        }

        /**
         * Will be called on every two sets of vertexes returned by the permutation
         * iterator. From findNextIsomorphicGraph(). Should make sure that the two
         * sets are euqivalent. Subclasses may decide to implements it as an always
         * true methods only if they make sure that the permutationIterator will
         * always be already equivalent.
         *
         * @param vertexSet1 FIXME Document me
         * @param vertexSet2 FIXME Document me
         */
        protected abstract boolean areVertexSetsOfTheSameEqualityGroup(
                Set<V> vertexSet1,
                Set<V> vertexSet2);

        /**
         * For each edge in g1, get the Correspondence edge and test the pair.
         *
         * @param resultRelation
         * @param edgeComparator if null, always return true.
         */
        protected boolean areAllEdgesEquivalent(
                IsomorphismRelation<V, E> resultRelation,
                EquivalenceComparator<? super E, ? super Graph<V, E>> edgeComparator)
        {
            boolean checkResult = true;

            if (edgeComparator == null) {
                // nothing to check
                return true;
            }

            try {
                Set<E> edgeSet = this.graph1.edgeSet();

                for (E currEdge : edgeSet) {
                    E correspondingEdge =
                            resultRelation.getEdgeCorrespondence(currEdge, true);

                    // if one edge test fail , fail the whole method
                    if (!edgeComparator.equivalenceCompare(
                            currEdge,
                            correspondingEdge,
                            this.graph1,
                            this.graph2))
                    {
                        checkResult = false;
                        break;
                    }
                }
            } catch (IllegalArgumentException illegal) {
                checkResult = false;
            }

            return checkResult;
        }

        /**
         * return nextElement() casted as IsomorphismRelation
         */
        public IsomorphismRelation nextIsoRelation()
        {
            return next();
        }

        /**
         * Efficiency: The value is known after the first check for isomorphism
         * activated on this class and returned there after in O(1). If called on a
         * new ("virgin") class, it activates 1 iso-check.
         *
         * @return <code>true</code> iff the two graphs are isomorphic
         */
        @Override
        public boolean isIsomorphic()
        {
            return !(this.nextSupplier.isEnumerationStartedEmpty());
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        @Override
        public boolean hasNext()
        {
            boolean result = this.nextSupplier.hasMoreElements();

            return result;
        }

        /**
         * @see Iterator#next()
         */
        @Override
        public IsomorphismRelation next()
        {
            return this.nextSupplier.nextElement();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(
                    "remove() method is not supported in AdaptiveIsomorphismInspectorFactory."
                            + " There is no meaning to removing an isomorphism result.");
        }



        private class NextFunctor
                implements PrefetchIterator.NextElementFunctor<IsomorphismRelation>
        {
            @Override
            public IsomorphismRelation nextElement()
                    throws NoSuchElementException
            {
                IsomorphismRelation resultRelation = findNextIsomorphicGraph();
                if (resultRelation != null) {
                    return resultRelation;
                } else {
                    throw new NoSuchElementException(
                            "IsomorphismInspector does not have any more elements");
                }
            }
        }

    }
}
