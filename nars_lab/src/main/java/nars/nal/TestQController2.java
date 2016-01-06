//package nars.nal;
//
//
//import nars.NAR;
//import nars.Param;
//import nars.io.Texts;
//import nars.io.in.LibraryInput;
//import nars.nar.Default;
//
//import java.io.PrintStream;
//import java.text.DecimalFormat;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.StringJoiner;
//
///**
// * Dynamic logic controller experiment, using QLearning
// * <p>
// * <p>
// * Experiment:
// * <sseehh_> normally, concept priority drops
// * <sseehh_> to like 0.03
// * <sseehh_> average concept priority
// * <sseehh_> this applies every N cycles
// * <sseehh_> so its looking at the average over the waiting period
// * <sseehh_> priority may spike for a few concepts, but this affects little
// * <sseehh_> if it can raise the avg concept priority, then it has significantly affected logic behavior
// */
//public class TestQController2 {
//
//    final static String cpm = "concept.priority.mean";
//    final static String td = "task.derived";
//    final static String cpv = "concept.priority.variance";
//    final static String cph0 = "concept.priority.hist.0";
//    final static String cph1 = "concept.priority.hist.1";
//    final static String cph2 = "concept.priority.hist.2";
//    final static String cph3 = "concept.priority.hist.3";
//    final static String nt = "task.novel.total";
//
//
//    public static class TestController extends QController {
//
//
//        private double conceptNewMean;
//        private double taskDerivedMean;
//
//
//        public TestController(NAR n, int period) {
//            super(n, period);
//
//
//
//            Param p = nar.param;
//
//            add(new NControlSensor(p.conceptForgetDurations, 3));
//
//            add(new ControlSensor(2) {
//
//                @Override
//                public double get() {
//                    return n.memory.emotion.busy();
//                }
//            });
//            add(new ControlSensor(2) {
//
//                @Override
//                public double get() {
//                    return n.memory.logic.JUDGMENT_PROCESS.count();
//                }
//            });
//            add(new ControlSensor(2) {
//
//                @Override
//                public double get() {
//                    return n.memory.logic.TASK_ADD_NOVEL.count();
//                }
//            });
//
//            //add(new EventValueControlSensor(nar, nar.memory.logic.CONCEPT_NEW, 0, 1, 7));
//            //add(new EventValueControlSensor(nar, nar.memory.logic.JUDGMENT_PROCESS, 0, 1, 7));
//
//            init(3);
//            q.brain.setUseBoltzmann(true);
//            q.brain.setRandActions(0.15);
//        }
//
//        @Override
//        protected int[] getFeedForwardLayers(int inputSize) {
//            //return new int[ (int)Math.ceil(inputSize * 0.5) ];
//            //return new int[ (int)Math.ceil(inputSize * 2) ];
//
//            //return new int[ ] { 18 }; //fixed # of hidden
//            return new int[]{8};
//        }
//
//        @Override
//        protected void act(int action) {
//            Param p = nar.param;
//
//
//            float a = p.inputActivationFactor.floatValue();
//
//            switch (action) {
//                case 0:
//                    //p.conceptForgetDurations.set(3);
//                    a = 0.05f;
//                    break;
//                case 1:
//                    a = 0.25f;
//                    break;
//                case 2:
//                    //p.conceptForgetDurations.set(7);
//                    a = 1f;
//                    break;
//            }
//
//            a = Math.max(a, 0.01f);
//            a = Math.min(a, 1.5f);
//
//            p.inputActivationFactor.set(a);
//            p.conceptActivationFactor.set( 0.5f * (1f + a) /** half as attenuated */ );
//
//
//        }
//
//        @Override
//        public double reward() {
//
//            //maximize concept priority
//            //return conceptPriority;
//            // + conceptNewMean;
//
//            //target: 1.0 business
//            double r = -0.5 + 1.0f / (1.0 + Math.abs(nar.memory.emotion.busyMeter.get() - 1.0));
//            // + nar.memory.logic.JUDGMENT_PROCESS.getValue(null, 0);
//            return r;
//            //return conceptNewMean + taskDerivedMean + 1* nar.memory.logic.d("task.solution.best");
//        }
//
//
//    }
//
//    public static void input(String example, NAR... n) {
//        String e = getExample(example);
//        for (NAR x : n)
//            x.input(e);
//    }
//
//
//    public static void main(String[] arg) {
//
//        int controlPeriod = 2;
//
//        NAR n = new NAR(new Default(512, 4, 3));
//        TestController qn = new TestController(n, controlPeriod);
//        qn.setActive(false);
//
//
//
//
//        int displayCycles = 1;
//
//        input("nal/other/nars_multistep_1.nal", n);
//
//        //TextOutput.out(n);
//
////        new FrameReaction(n) {
////
////            @Override
////            public void onFrame() {
////                double r = qn.reward();
////            }
////        };
//
//
//        n.frame(1000);
//
//        for (int i = 0; i < 250; i++) {
//
//
//            n.frame(1);
//
//            double r = qn.reward();
//
//            if (n.time() % displayCycles == 0) {
//                System.out.println(
//                        //((nn-mm)/((nn+mm)/2.0)*100.0) + " , " +
//                        n.time() + ", " +
//                                //Arrays.toString(qn.getInput()) + ", " +
//                                Texts.n4(n.memory.emotion.busy()) + " ,  " +
//                                //Texts.n4(r) + " ,  " +
//                        //Arrays.toString(qn.getOutput()).replace("[", " , ").replace("]"," ")
//                        n.param.inputActivationFactor.get()
//                );
//
//            }
//
//        }
//
//    }
//
//
//    protected final static DecimalFormat df = new DecimalFormat("#.###");
//
//    public static void printCSVLine(PrintStream out, List<String> o) {
//        StringJoiner line = new StringJoiner(",", "", "");
//        int n = 0;
//        for (String x : o) {
//            line.add(x + "_" + (n++));
//        }
//        out.println(line.toString());
//    }
//
//    public static void printCSVLine(PrintStream out, double[] a) {
//        StringJoiner line = new StringJoiner(",", "", "");
//        for (double x : a)
//            line.add(df.format(x));
//        out.println(line.toString());
//    }
//
//
//    protected static Map<String, String> exCache = new HashMap(); //path -> script data
//
//    /**
//     * duplicated from NALTest.java  -- TODO use a comon copy of this method
//     */
//    public static String getExample(String path) {
//        return LibraryInput.getExample(path);
//    }
//
// }
