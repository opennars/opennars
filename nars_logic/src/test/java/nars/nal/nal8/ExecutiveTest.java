//package nars.nal.nal8;
//
//import nars.model.impl.Default;
//import nars.NAR;
//import nars.exec.BeliefTruthExecutive;
//import org.apache.commons.math3.stat.Frequency;
//import org.junit.Test;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by me on 3/23/15.
// */
//public class ExecutiveTest {
//
//    @Test
//    public void testBeliefTruthExecutive() {
//
//        NAR n = new NAR(new Default());
//
//        Frequency firings = new Frequency();
//
//        BeliefTruthExecutive bte;
//        n.on(bte = new BeliefTruthExecutive("act"));
//
//        BeliefTruthExecutive.Action a = bte.add("a", new Runnable() {
//            @Override public void run() {
//                firings.addValue("a");
//            }
//        }).setFrequency(0.1f);
//        BeliefTruthExecutive.Action b = bte.add("b", new Runnable() {
//            @Override public void run() {
//                firings.addValue("b");
//            }
//        }).setFrequency(1f);
//
//        n.run(256);
//
//        assertTrue(firings.getCount("b") > 8 * firings.getCount("a"));
//
//
//
//    }
//}
