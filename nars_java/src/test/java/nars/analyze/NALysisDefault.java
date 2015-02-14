package nars.analyze;

import nars.core.NewNAR;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by me on 2/14/15.
 */
public class NALysisDefault extends NALysis {

    public NALysisDefault(NewNAR b) {        super(b);    }

    public static void main(String[] args) throws FileNotFoundException {

        //csvOut = System.out;
        dataOut = new FileOutputStream("/tmp/out.arff");

        nal1Default(1);
        nal2Default();
        nal3Default();
        nal4Default();
        nal5Default();
        nal6Default();
        nal7Default();
        nal8Default();

        results.printARFF(new PrintStream(dataOut));


    }
}


