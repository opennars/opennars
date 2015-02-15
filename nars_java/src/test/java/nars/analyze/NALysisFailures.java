//package nars.analyze;
//
//import com.google.common.base.Predicate;
//import nars.core.NewNAR;
//import org.junit.Ignore;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//
///**
// * report filtered by failures
// */
//@Ignore
//public class NALysisFailures extends NALysis {
//
//
//    public NALysisFailures(NewNAR b) {
//        super(b);
//    }
//
//    public static void main(String[] args) throws FileNotFoundException {
//
//        for (int seed = 1; seed < 16; seed++) {
//            nal1(seed);
//        }
//
//        dataOut = new FileOutputStream("/tmp/fail.arff");
//
//        //results.printARFF(new PrintStream(dataOut));
//        results.printARFF(new PrintStream(dataOut), new Predicate<Object[]>() {
//            @Override
//            public boolean apply(Object[] objects) {
//                return (  ((Double)objects[results.indexOf("Score")]).intValue() == -1 );
//            }
//        });
//
//    }
//}
