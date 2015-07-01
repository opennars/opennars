//package nars.analyze.experimental;
//
//import nars.model.impl.Curve;
//import nars.model.impl.Default;
//import nars.NAR;
//import nars.Global;
//import nars.nal.NALTest;
//
//import java.util.Collection;
//
//import static nars.analyze.experimental.NALStressMeasure.perfNAL;
//
///**
// * Runs NALTestPerf continuously, for profiling
// */
//@Deprecated public class NALPerfCompare {
//    static int repeats = 100;
//    static int warmups = 2;
//    static int extraCycles = 256;
//
//    public static void compareRope(String examplePath) {
//
//        for (int r = 0; r < 96; r+=12) {
//            Global.ROPE_TERMLINK_TERM_SIZE_THRESHOLD = r;
//            System.out.print("ROPE_TERMLINK_TERM_SIZE_THRESHOLD " + r + ":\t");
//            perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
//        }
//
//        Global.ROPE_TERMLINK_TERM_SIZE_THRESHOLD = -1;
//        System.out.print("ROPE_TERMLINK_TERM_SIZE_THRESHOLD NO:\t");
//        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
//
//        System.out.println();
//
//    }
//
//    public static void compareDiscreteContinuousBag(String examplePath) {
//        System.out.print("DISCRETE:");
//        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
//
//        System.out.print("CONTINUOUS:\t");
//        perfNAL(new NAR(new Curve()), examplePath,extraCycles,repeats,warmups,true);
//
//        System.out.print("DISCRETE:");
//        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
//
//
//        System.out.println();
//
//    }
//
//    public static void main(String[] args) {
//
//
//        NAR n = new NAR(new Default());
//
//        Collection c = NALTest.params();
//        while (true) {
//            for (Object o : c) {
//                String examplePath = (String)((Object[])o)[0];
//                compareRope(examplePath);
//                //compareDiscreteContinuousBag(examplePath);
//            }
//        }
//    }
//}
