package nars.meter.experiment;

import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
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

        Global.DEBUG = false;

        for (int length = 3; length < 10; length++) {
            test(new Default().nal(2), length);
        }
    }

    static void test(NAR n, int chainLen) {


        DeductiveChainTest test = new DeductiveChainTest(n, chainLen, 3000) {
//            @Override
//            public TestNAR mustBelieve(long withinCycles, String term, float confidence, float x, float y, float z) throws InvalidInputException {
//                return this;
//            }
        };

        System.out.println("derivation chain test: " + test.q + "?");

        final long start = System.currentTimeMillis();

//        new AnswerReaction(n) {
//
//            @Override
//            public void onSolution(Task belief) {
//                if (belief.getTerm().equals(test.q)) {
//                    System.out.println(belief + " " + timestamp(start) + " " +
//                            n.concepts().size() + " concepts");
//                    System.out.println(belief.getExplanation());
//                    System.out.println();
//                }
//            }
//        };


        test.run();

        //n.stdout();
        //n.frame(5000);

        int nc = ((Default)n).core.concepts().size();
        String ts = timestamp(start);
        long time = n.time();

        //n.stdout();
        n.frame(55); //to print the ending

        //while (true) {


        System.out.println("@" + time + " (" + ts + "ms) " +
                nc + " concepts");      //       }


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
