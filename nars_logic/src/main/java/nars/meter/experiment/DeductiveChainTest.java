package nars.meter.experiment;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.io.qa.AnswerReaction;
import nars.nal.nal1.Inheritance;
import nars.nar.experimental.Alann;
import nars.task.Task;
import nars.term.Atom;

/**
 * Created by me on 8/25/15.
 */
public class DeductiveChainTest {

    public static Atom a(int i) {
        return Atom.the((byte)('a' + i));
    }

    public static Inheritance i(int x, int y) {
        return Inheritance.make(a(x), a(y));
    }

    public static void main(String[] args) {

        int length = 9;
        NAR n = new NAR(
                //new Equalized(1000, 1, 3).level(1)
                //new Default().level(1)
                new Alann(16)
        );

        //Global.OVERLAP_ALLOW = true;
        //TextOutput.out(n);

        for (int x = 0; x < length; x++) {
            n.believe( i(x, x+1) );
        }

        Inheritance q = i(0, length);
        n.ask( q );

        System.out.println("derivation chain test: " + q + "?");

        final long start = System.currentTimeMillis();

        new AnswerReaction(n) {

            @Override
            public void onSolution(Task belief) {
                if (belief.getTerm().equals(q))
                    System.out.println(belief + " "  + timestamp(start));
            }
        };

        n.frame(5000);

        System.out.println("end @ " + n.time() + "   " + timestamp(start) + " " +
                n.memory.numConcepts(true,true) + " total concepts");

    }

    private static String timestamp(long start) {
        return (System.currentTimeMillis() - start) + "ms";
    }
}
