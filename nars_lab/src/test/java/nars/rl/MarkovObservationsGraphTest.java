//package nars.rl;
//
//import nars.NAR;
//import nars.concept.Concept;
//import nars.nar.Default;
//import nars.rl.example.MarkovObservationsGraph;
//import org.junit.Test;
//
///**
// * Created by me on 5/11/15.
// */
//public class MarkovObservationsGraphTest {
//
//    @Test
//    public void test() {
//        NAR n = new NAR(new Default());
//        MarkovObservationsGraph m = new MarkovObservationsGraph(n) {
//
//            @Override
//            public boolean contains(Concept c) {
//                return true;
//            }
//
//        };
//
//
//        n.input("<a --> b>. :|:");
//        n.input("<eternal --> something>.");
//        n.frame();
//        n.input("<b --> c>. :|:");
//        n.frame();
//        n.input("<c --> d>. :|:");
//        n.input("<a --> d>. :|:");
//        n.frame();
//        n.input("<x --> y>. :|:");
//        n.frame();
//        n.frame();
//        n.frame();
//
//        System.out.println(m.graph);
//    }
// }
