package nars.analyze.experimental;

import nars.Global;
import nars.ProtoNAR;
import nars.analyze.NALysis;
import nars.io.ExampleFileInput;
import nars.io.test.TestNAR;
import nars.nal.AbstractNALTest;
import nars.prototype.Default;

import java.util.List;

/**
 * Created by me on 4/24/15.
 */

public class ExampleScores {

    public final double totalCost;

    public ExampleScores(ProtoNAR p, int cycleLimit) {
        super();

        Global.DEBUG = false;

        NALysis.showFail = false;

        AbstractNALTest.reset();

        for (String dir : ExampleFileInput.directories) {
            List<TestNAR> x = NALysis.runDir(dir, cycleLimit, 0, p);
        }

        //AbstractNALTest.results.printCSV(System.out);

        double totalCost = 0;
        for (double x : AbstractNALTest.results.doubleArray("Cost")) {
            if (!Double.isFinite(x)) totalCost += cycleLimit;
            else totalCost += x;
        }

        this.totalCost = totalCost;



//        Iterator<Object[]> ii = AbstractNALTest.results.iterator();
//        while (ii.hasNext()) {
//            System.out.println(Arrays.toString(ii);)
//        }
//        System.out.println();

//        Collection<String> tests = ExampleFileInput.getPaths("test1");
//
//
//        for (String t : tests) {
//            long x = AbstractNALTest.runScript(nar, t, 2500);
//
//            String result = nar.evaluate();
//            if (result != null) {
//                TestCase.assertTrue(result, false);
//            }
//        }

    }

    /*public static void main(String[] args) {
        new ExampleScores(new Default(), 500);
    }*/

}
