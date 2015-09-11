//package nars.nal.multistep;
//
//import nars.NAR;
//import nars.nar.Default;
//
//
//
//public class RealtimeSequenceExperiment {
//
//    public RealtimeSequenceExperiment() throws InterruptedException {
//        int seqLength = 8;
//        int framePeriodMS = 100;
//        int seqPeriodMS = 800;
//        int durationMS = 50;
//
//        NAR n = new NAR(new Default().realTime());
//
//        (n.param).duration.set(durationMS);
//        (n.param).outputVolume.set(0);
//        (n.param).executionThreshold.set(0.9);
//
////        new NARSwing(n);
////        new NWindow("Implication Graph",
////                            new ProcessingGraphPanel(n,
////                                    new ImplicationGraphCanvas(
////                                            n.memory.executive.graph))).show(500, 500);
//
//        n.start(framePeriodMS);
//
//        int last = 0;
//        int i = 1;
////        while (true) {
////            n.addInput("<(&/,e" + last + ",(pick,x" + last + ")) =/> e" + i + ">. :|:");
////            Thread.sleep(seqPeriodMS);
////            i++;
////            last++;
////            i%=seqLength;
////            last%=seqLength;
////            if (last == 0)
////                n.addInput("e" + last + "!");
////            else {
////                //n.addInput("e" + last + ". :|:");
////            }
////        }
//
//        while (true) {
//            n.input("pick(x" + i + ")!");
//            //n.addInput("<(^pick,x" + i + ") =/> e" + i + ">. :|:");
//            //n.addInput("e" + last + ". :|:");
//            Thread.sleep(seqPeriodMS);
//            i++;
//            last++;
//            i%=seqLength;
//            last%=seqLength;
//        }
//
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        new RealtimeSequenceExperiment();
//    }
//
//
//}
