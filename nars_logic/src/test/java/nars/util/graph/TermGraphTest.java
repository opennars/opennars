package nars.util.graph;

import nars.NAR;
import nars.nar.Default;


public class TermGraphTest {


    NAR n = new NAR( new Default().setInternalExperience(null) );


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


}
