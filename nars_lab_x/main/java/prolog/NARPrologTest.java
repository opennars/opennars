//package nars.prolog;
//
//import nars.NAR;
//import nars.io.TextOutput;
//import nars.model.impl.Default;
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by me on 2/19/15.
// */
//public class NARPrologTest {
//
//    NAR n;
//    PrologContext p;
//
//    @Before
//    public void start() {
//        n = new NAR(new Default());
//        p = new PrologContext(n);
//
//        //TextOutput.out(n);
//    }
//
//    @Test
//    public void testFact() {
//
//        n.input("fact(<x --> y>)!");
//        n.run(5);
//        String s = p.getProlog(null).getDynamicTheoryCopy().toString();
//        assertTrue("contains: " + s, s.contains("inheritance(x,y)."));
//
//    }
//
//    @Test
//    public void testFactual() {
//
//        //TextOutput.out(n);
//
//        n.input("fact(<a --> y>)!");
//        n.input("fact(<b --> y>)!");
//        n.input("factual(<$q --> y>, #result)!");
//        n.run(4);
//
//        //contains("<$2 <-> {<x --> y>}>>")
//        //..
//
//    }
//}
