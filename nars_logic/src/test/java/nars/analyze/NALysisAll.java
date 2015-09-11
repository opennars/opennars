//package nars.analyze;
//
//import nars.NARSeed;
//import org.junit.Ignore;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//
//
//@Ignore
//public class NALysisAll extends NALysis {
//
//    public NALysisAll(NARSeed b) {        super(b);    }
//
//    public static void main(String[] args) throws FileNotFoundException {
//
//        analyzeStack = true;
//
//        //csvOut = System.out;
//        dataOut = new FileOutputStream("/tmp/out.arff");
//
//
//        for (int seed : new int[] { 1, 2, 3, 4 } ) {
//            nal1(seed);
//            nal2(seed);
//            nal3(seed);
//            nal4(seed);
//        }
//        nal5();
//        nal6();
//        nal7();
//        nal8();
//
//        results.printARFF(new PrintStream(dataOut));
//
//
//    }
//}
//
//
