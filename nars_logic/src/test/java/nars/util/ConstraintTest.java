package nars.util;

import nars.Global;
import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nar.Default2;
import nars.term.Term;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.netflow.NetworkBuilder;
import org.jacop.constraints.netflow.NetworkFlow;
import org.jacop.constraints.netflow.simplex.Node;
import org.jacop.core.BooleanVar;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by me on 10/19/15.
 */
public class ConstraintTest {

//    static class FastStore extends Store {
//        public FastStore(int size) {
//            super(size);
//            this.mutableVariables = Global.newArrayList(size);
//        }
//    }

    @Test
    public void testJacop1() {

        NAR n = new Default2(1000, 1, 1, 3);
        SATNetwork sat = new SATNetwork(n, "<x-->y>") {


            @Override
            public void addConstraintsFor(Concept c) {
                Term t = c.getTerm();
                if (t.op() == Op.IMPLICATION) {
                    Implication i = (Implication)t;
                    Term effect = i.getPredicate();
                    if (!concepts.contains(effect))
                        return;

                    Term causeTerm = i.getSubject();


                    //TODO recurse the contents of this node if conjunction, etc

                    IntVar causeVar = termAndConstraints(causeTerm);
                    if (causeVar!=null) {
                        store.impose(new XeqY(
                                causeVar,
                                termBoolean(effect)
                        ));
                    }
                }
            }

            private IntVar termAndConstraints(Term causeTerm) {

                Concept cause = n.concept(causeTerm);
                if (cause == null || !cause.hasBeliefs())
                    return null;

                float e = cause.getBeliefs().top().getTruth().getExpectation();
                if ((e < 0.75) && (e > 0.25)) {
                    return null; //too indeterminate
                }
                return term(causeTerm, e > 0.5);
            }


        };

        sat.solve();

        n.input("<a-->b>.");
        n.input("<c-->d>.");
        n.input("(&&,<a-->b>,<c-->d>).");
        n.input("<(&&,<a-->b>,<c-->d>) ==> <x-->y>>.");
        n.input("<x-->y>?");

        n.stdout();
        n.frame(5);

        sat.solve();
        sat.store.print();

        n.input("<b <-> c>. %0.75%");
        n.input("<a <-> d>. %0%");
        n.input("<<x --> y> ==> (b,c)>. %0.75%");
        n.input("<<a <-> d> ==> <x --> y>>. %0%");
        n.frame(100);

        sat.solve();
        sat.store.print();
    }

    /** boolean satisfiability among a set of terms */
    abstract public static class SATNetwork {
        private final NAR nar;
        protected final Set<Term> concepts;
        protected Store store;
        final Map<Term,IntVar> termVars = Global.newHashMap();

        public SATNetwork(NAR n, String... terms) {
            this(n, Arrays.stream(terms)
                    .map(t -> (Term)n.term(t))
                    .collect(Collectors.toSet()));
        }

        public SATNetwork(NAR n, Set<Term> concepts) {
            this.nar = n;
            this.concepts = concepts;
        }

        /** handle a related concept by doing nothing, or
         *  creating constraints (in 'store') and variables */
        abstract public void addConstraintsFor(Concept c);

        void related(Term t) {
            nar.concepts().forEach( c-> {
                if (c.getTerm().containsTermRecursively(t)) {
                    addConstraintsFor(c);
                }
            });
        }


        public IntVar termBoolean(Term t) {
            return termVars.computeIfAbsent(t,
                    tt -> new BooleanVar(store, t.toString())
            );
        }

        public BooleanVar term(Term t, boolean constant) {
            int v = constant ? 1 : 0;
            BooleanVar b = new BooleanVar(store, t.toString(), v, v);
            termVars.put(t, b);
            return b;
        }

        /**
         evaluate all concepts involving these terms
         to build a set of constraints that can
         be evaluated for consistency and solved for
         one or more unknown variables in terms
         of known ones
         */
        public boolean solve() {
            store = new Store();
            termVars.clear();

            concepts.forEach(this::related);

            if (termVars.isEmpty()) return false;

            Search<IntVar> label = new DepthFirstSearch<IntVar>();

            IntVar[] vars = termVars.values().toArray(new IntVar[termVars.size()]);
            SelectChoicePoint<IntVar> select =
                    new InputOrderSelect<IntVar>(store,
                            vars, new IndomainMin<IntVar>());

            boolean result = label.labeling(store, select);
            System.out.println(result);

            return result;
        }

    }


    /** a network representing truth relationships between concepts */
    public static class TruthNetBuilder extends NetworkBuilder {

    }

    @Test
    public void testNetworkFlow() {

        Store store = new Store();

        NetworkBuilder net = new NetworkBuilder();
        Node A = net.addNode("A", 0);
        Node B = net.addNode("B", 0);
        Node C = net.addNode("C", 0);
        Node D = net.addNode("D", 0);
        Node E = net.addNode("E", 0);
        Node F = net.addNode("F", 0);


        Node source = net.addNode("source", 9);  // should ne 5+3+3=11 but it does not work...

        Node sinkD = net.addNode("sinkD", -3);
        Node sinkE = net.addNode("sinkE", -3);
        Node sinkF = net.addNode("sinkF", -3);

        IntVar[] x = new IntVar[13];

        x[0] = new IntVar(store, "x_0", 0, 5);
        x[1] = new IntVar(store, "x_1", 0, 3);
        x[2] = new IntVar(store, "x_2", 0, 3);
        net.addArc(source, A, 0, x[0]);
        net.addArc(source, B, 0, x[1]);
        net.addArc(source, C, 0, x[2]);

        x[3] = new IntVar(store, "a->d", 0, 5);
        x[4] = new IntVar(store, "a->e", 0, 5);
        net.addArc(A, D, 3, x[3]);
        net.addArc(A, E, 1, x[4]);

        x[5] = new IntVar(store, "b->d", 0, 3);
        x[6] = new IntVar(store, "b->e", 0, 3);
        x[7] = new IntVar(store, "b->f", 0, 3);
        net.addArc(B, D, 4, x[5]);
        net.addArc(B, E, 2, x[6]);
        net.addArc(B, F, 4, x[7]);

        x[8] = new IntVar(store, "c->e", 0, 3);
        x[9] = new IntVar(store, "c->f", 0, 3);
        net.addArc(C, E, 3, x[8]);
        net.addArc(C, F, 3, x[9]);

        x[10] = new IntVar(store, "x_10", 3, 3);
        x[11] = new IntVar(store, "x_11", 3, 3);
        x[12] = new IntVar(store, "x_12", 3, 3);
        net.addArc(D, sinkD, 0, x[10]);
        net.addArc(E, sinkE, 0, x[11]);
        net.addArc(F, sinkF, 0, x[12]);

        IntVar cost = new IntVar(store, "cost", 0, 1000);
        net.setCostVariable(cost);

        IntVar[] vars = x;
        IntVar COST = cost;

        store.impose(new NetworkFlow(net));


        System.out.println("\nIntVar store size: " + store.size() +
                "\nNumber of constraints: " + store.numberConstraints()
        );

        boolean Result = true;
        SelectChoicePoint<IntVar> varSelect = new SimpleSelect<IntVar>(x, null,
                new IndomainMin<IntVar>());
        // Trace --->

        Search<IntVar> label = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new TraceGenerator<IntVar>(label, varSelect);

//      SelectChoicePoint<IntVar> select = new TraceGenerator<IntVar>(varSelect, false);
//      label.setConsistencyListener((ConsistencyListener)select);
//      label.setExitChildListener((ExitChildListener)select);
//      label.setExitListener((ExitListener)select);
        // <---

        DepthFirstSearch<IntVar> costSearch = new DepthFirstSearch<>();
        SelectChoicePoint<IntVar> costSelect = new SimpleSelect<>(new IntVar[]{cost}, null, new IndomainMin<IntVar>());
        costSearch.setSelectChoicePoint(costSelect);
        costSearch.setPrintInfo(false);
        //costSearch.setSolutionListener(new NetListener<IntVar>());

        label.addChildSearch(costSearch);

        label.setAssignSolution(true);
        label.setPrintInfo(true);

        Result = label.labeling(store, select, cost);


        if (Result) {
            System.out.println("*** Yes");
            System.out.println(cost);
        } else
            System.out.println("*** No");

    }
}
