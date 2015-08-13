package nars.analyze;

import nars.NARSeed;
import org.junit.Ignore;


@Ignore
public class NALysisDefault extends NALysis {

    public NALysisDefault(NARSeed b) {        super(b);    }

    public static void main(String[] args) {

        //csvOut = System.out;
        //dataOut = new FileOutputStream("/tmp/out.arff");

        nal1Default(1);
        nal2Default(1);
        nal3Default(1);
        nal5Default();

        //multistep issues:
        nal4Default(1);
        nal6Default();

        nal7Default();
        nal8Default();

        //results.printARFF(new PrintStream(dataOut));


    }
}


