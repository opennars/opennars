package nars.analyze;

import nars.core.NewNAR;
import org.junit.Ignore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


@Ignore
public class NALysisAll extends NALysis {

    public NALysisAll(NewNAR b) {        super(b);    }

    public static void main(String[] args) throws FileNotFoundException {

        analyzeStack = true;

        //csvOut = System.out;
        dataOut = new FileOutputStream("/tmp/out.arff");

        nal1(1);
        nal2();
        nal3();
        nal4();
        nal5();
        nal6();
        nal7();
        nal8();

        results.printARFF(new PrintStream(dataOut));


    }
}


