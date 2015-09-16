package nars.meter.experiment;

import nars.Global;
import nars.NAR;
import nars.io.qa.AnswerReaction;
import nars.meter.TestNAR;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
import nars.task.Task;
import nars.term.Atom;

/**
 * Created by me on 8/25/15.
 */
public class DeductiveChainTest extends TestNAR {

    public final Inheritance q;
    public final Inheritance[] beliefs;

    public DeductiveChainTest(NAR n, int length, int timeLimit) {
        super(n);



        beliefs = new Inheritance[length];
        for (int x = 0; x < length; x++) {
            beliefs[x] = i(x, x+1);
        }

        q = i(0, length);

        for (int x = 0; x < beliefs.length; x++) {
            n.believe( beliefs[x]  );
        }
        n.ask( q );

        mustBelieve(timeLimit, q.toString(), 1f, 1f, 0.01f, 0.99f);

    }


    public static Atom a(int i) {
        return Atom.the((byte)('a' + i));
    }

    public static Inheritance i(int x, int y) {
        return Inheritance.make(a(x), a(y));
    }

    public static void main(String[] args) throws InterruptedException {

        int length = 3;

        Global.DEBUG = true;

        Default da = new Default().nal(6);
        //DefaultAlann da = new DefaultAlann(32);
        //da.nal(3);

        NAR n = //new TestNAR(
                //new Equalized(1000, 8, 3) //.level(1)
                //new NewDefault().level(2)
                //new ParallelAlann(4,2)
                da;
        //);

        da.stdout();

        DeductiveChainTest test = new DeductiveChainTest(n, length, 100000);

        System.out.println("derivation chain test: " + test.q + "?");

        final long start = System.currentTimeMillis();

        new AnswerReaction(n) {

            @Override
            public void onSolution(Task belief) {
                if (belief.getTerm().equals(test.q)) {
                    System.out.println(belief + " " + timestamp(start) + " " +
                            n.concepts().size() + " concepts");
                    System.out.println(belief.getExplanation());
                    System.out.println();
                }
            }
        };

        n.run(128);

        //TextOutput.out(n).setOutputPriorityMin(0.85f);

//        while (true) {
//
//            n.run(500);
//            //sleep(20);
//
//            if (n.time() % printEvery == 0) {
//                System.out.println(n.time() + " " + timestamp(start) + " " +
//                        n.memory().size());
//            }
//        }


    }

    private static String timestamp(long start) {
        return (System.currentTimeMillis() - start) + " ms";
    }
}
