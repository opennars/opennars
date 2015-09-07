package nars.meter.experiment;

import nars.NAR;
import nars.io.qa.AnswerReaction;
import nars.nal.nal1.Inheritance;
import nars.nar.experimental.DefaultAlann;
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

    public static void main(String[] args) throws InterruptedException {

        int length = 3;
        NAR n = new NAR(
                //new Equalized(1000, 8, 3) //.level(1)
                //new NewDefault().level(2)
                //new ParallelAlann(4,2)
                new DefaultAlann(32)
        );
        n.param.conceptActivationFactor.set(0.2f);

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
                if (belief.getTerm().equals(q)) {
                    System.out.println(belief + " " + timestamp(start) + " " + n.memory.concepts.size() + " concepts");
                    System.out.println(belief.getExplanation());
                    System.out.println();
                }
            }
        };

        //n.frame(5000);

        //TextOutput.out(n).setOutputPriorityMin(0.85f);

        final int printEvery = 6000;

        while (true) {

            n.frame(500);
            //sleep(20);

            if (n.time() % printEvery == 0) {
                System.out.println( n.time() + " " + timestamp(start) + " " +
                        n.memory.numConcepts(true, true) );
            }
        }



    }

    private static String timestamp(long start) {
        return (System.currentTimeMillis() - start) + " ms";
    }
}
