//package nars.analyze;
//
//
//import nars.NARSeed;
//import nars.io.TraceWriter;
//import nars.io.in.LibraryInput;
//import nars.io.out.TextOutput;
//import nars.meter.TestNAR;
//import nars.meter.condition.OutputCondition;
//import nars.nal.AbstractNALTest;
//import nars.nar.Default;
//import org.junit.Ignore;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Consumer;
//
//import static nars.io.in.LibraryInput.getPaths;
//
///**
// * Collects detailed telemetry for a test suite
// */
//@Ignore
//public class NALysis extends AbstractNALTest {
//
//    public static boolean showInput = false;
//    static boolean showOutput = false;
//    static boolean showTrace = false;
//    public static boolean showFail = true; //failure report
//
//    public NALysis(NARSeed b) {
//        super(b);
//    }
//
///*    public NALysis(NewNAR b, String input) {
//        super(b, input);
//    }
//    */
//
////    @Parameterized.Parameters(name= "{1} {0}")
////    public static Collection configurations() {
////        return getParams(
////                new String[] { "test2", "test3", "test4" },
////                new Default(),
////                new Default().setInternalExperience(null),
////                new Curve() );
////    }
//
////    public int getMaxCycles() { return 200; }
//
//
//    /** run the test using the returned TestNAR's .run() method */
//    public static TestNAR analyze(NARSeed build, String path, int maxCycles, long seed) {
//
//        String testName = path + "_" + build;
//
//
//
//        TestNAR n = new TestNAR(build) {
//
//            @Override
//            public void run() {
//
//                memory.clear(seed);
//
//                long nanos = runScript(this, path, maxCycles);
//
//                //String report = "";
//                boolean suc = getError()==null;
//                for (OutputCondition e : requires) {
//                    if (!e.isTrue()) {
//                        //report += e.getFalseReason().toString() + '\n';
//                        suc = false;
//                    }
//                    else {
//                        //report += e.getTrueReasons().toString() + '\n';
//                    }
//                }
//
//                String[] p = path.split("/");
//                endAnalysis(p[p.length-1], this, build, nanos, seed, suc);
//
//                //results.printCSVLastLine(System.out);
//
//                if (!suc && showFail) {
//                    System.out.println("-------------------------------------------");
//
//                    System.out.println("FAIL: " + testName);
//
//                    System.out.println(LibraryInput.getExample(path).trim());
//
//                    report(System.out, true, true, true);
//
//                }
//
//                stop();
//            }
//
//
//        };
//
//        if (showOutput)
//            TextOutput.out(n);
//        if (showTrace)
//            new TraceWriter(n, System.out);
//
//        initAnalysis(n);
//
//
//        return n;
//    }
//
////    public void finish(Description test, String status, long nanos) {
////
////        boolean success = status.equals("fail") ? false : true;
////
////        String label = test.getDisplayName();
////
////        endAnalysis(label, nar, build, nanos, success);
////
////    }
////
////
////    @Rule
////    public final Stopwatch stopwatch = new Stopwatch() {
////        @Override
////        protected void succeeded(long nanos, Description description) {
////            finish(description, "success", nanos);
////        }
////
////        @Override
////        protected void failed(long nanos, Throwable e, Description description) {
////            finish(description, "fail", nanos);
////        }
////
////        @Override
////        protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
////            finish(description, "skip", nanos);
////        }
////
////        @Override
////        protected void finished(long nanos, Description description) {
////            //finish(description, "finish", nanos);
////        }
////    };
//
//
//
//
//    public synchronized static List<TestNAR> runDir(String dirPath, int maxCycles, long seed, NARSeed... builds) {
//        return runDir(dirPath, maxCycles, seed, null, builds);
//    }
//
//    public synchronized static List<TestNAR> runDir(String dirPath, int maxCycles, long seed, Consumer<TestNAR> eachNewNAR, NARSeed... builds) {
//        Collection<String> paths = getPaths(dirPath);
//
//        List<TestNAR> nars = new ArrayList(paths.size() * builds.length);
//
//        for (String p : paths) {
//            if (p.contains("README")) continue;
//            for (NARSeed b : builds) {
//                TestNAR n = analyze(b, p, maxCycles, seed);
//                if (eachNewNAR!=null)
//                    eachNewNAR.accept(n);
//                nars.add(n);
//                n.run();
//            }
//        }
//
//        return nars;
//    }
//
//    public static void nal(String dirPath, String filter, NARSeed build, int maxCycles) {
//        Collection<String> paths = getPaths(dirPath);
//
//        for (String p : paths) {
//            if (p.contains(filter))
//                analyze(build, p, maxCycles, 1).run();
//        }
//    }
//
//
//    public static void nal1Default(long seed) {
//        runDir("test1", 5000, seed,
//                new Default().setInternalExperience(null)); //HACK: nal1.8 with internal experience enabled takes forever
//    }
//
//    /** runs the standard set of tests */
//    public static void nal1(long seed) {
//        nal1Default(seed);
//        runDir("test1", 1000, seed,
//                new Default(),
//                new Default().nal(1));
//    }
//
//    public static void nal2Default(long seed) {
//        runDir("test2", 1000, seed, new Default().setInternalExperience(null));
//    }
//    public static void nal2(long seed) {
//        nal2Default(seed);
//        runDir("test2", 1200, seed,
//                new Default(),
//                new Default().nal(3));
//    }
//    public static void nal3Default(long seed) {
//        runDir("test3", 1500, seed, new Default().setInternalExperience(null));
//    }
//    public static void nal3(long seed) {
//        nal3Default(seed);
//        runDir("test3", 1500, seed,
//                new Default(),
//                new Default().nal(3));
//    }
//
//
//    public static void nal4Default(long seed) {
//        runDir("test4", 1000, seed, new Default().setInternalExperience(null));
//    }
//    public static void nal4(long seed) {
//        nal4Default(seed);
//        runDir("test4", 1000, seed,
//                new Default(),
//                new Default().nal(4));
//    }
//
//    public static void nal5Default() {
//        runDir("test5", 2000, 1, new Default().setInternalExperience(null));
//    }
//    public static void nal5() {
//        nal5Default();
//        runDir("test5", 2000, 1,
//                new Default(),
//                new Default().nal(5));
//    }
//    public static void nal6Default() {
//        runDir("test6", 3000, 1, new Default().setInternalExperience(null));
//    }
//    public static void nal6() {
//        nal6Default();
//        runDir("test6", 3000, 1,
//                new Default(),
//                new Default().nal(6));
//    }
//    public static void nal7Default() {
//        runDir("test7", 3000, 1, new Default().setInternalExperience(null));
//    }
//    public static void nal7() {
//        nal7Default();
//        runDir("test7", 3000, 1,
//                new Default(),
//                new Default().nal(7));
//    }
//    public static void nal8Default() {
//        runDir("test8", 3000, 1, new Default().setInternalExperience(null));
//    }
//    public static void nal8() {
//        nal8Default();
//        runDir("test8", 3000, 1,
//                new Default());
//    }
//
//
//
////    @After
////    public void test() {
////        super.test();
////    }
//}
//
