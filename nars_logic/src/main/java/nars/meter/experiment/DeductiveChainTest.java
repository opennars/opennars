package nars.meter.experiment;

import nars.NAR;
import nars.io.qa.AnswerReaction;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
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

        int length = 20;
        NAR n = new NAR(
                //new Equalized(1000, 1, 3)
                new Default()
                        .level(1));





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

        n.frame(100000);


        System.out.println("end @ " + n.time() + "   " + timestamp(start));

    }

    private static String timestamp(long start) {
        return (System.currentTimeMillis() - start) + "ms";
    }
}
