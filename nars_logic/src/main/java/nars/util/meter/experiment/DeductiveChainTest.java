package nars.util.meter.experiment;

import nars.$;
import nars.Global;
import nars.NAR;
import nars.nar.Default;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.util.meter.TestNAR;

/**
 * Created by me on 8/25/15.
 */
public class DeductiveChainTest extends TestNAR {

    public final Compound q;
    public final Compound[] beliefs;

    @FunctionalInterface
    public interface IndexedStatementBuilder {
        Compound apply(int x, int y);
    }

    public static final IndexedStatementBuilder inh = (int x, int y) ->
            (Compound)$.inh(a(x), a(y));
    public static final IndexedStatementBuilder sim = (int x, int y) ->
            (Compound)$.sim(a(x), a(y));
    public static final IndexedStatementBuilder impl = (int x, int y) ->
            (Compound)$.impl(a(x), a(y));
    public static final IndexedStatementBuilder equiv = (int x, int y) ->
            (Compound)$.equiv(a(x), a(y));

    public DeductiveChainTest(NAR n, int length, int timeLimit, IndexedStatementBuilder b) {
        super(n);

        beliefs = new Compound[length];
        for (int x = 0; x < length; x++) {
            beliefs[x] = b.apply(x, x+1);
        }

        q = b.apply(0, length);

        for (Compound belief : beliefs) {
            n.believe(belief);
        }
        n.ask( q );

        mustBelieve(timeLimit, q.toString(), 1f, 1f, 0.01f, 1f);

    }


    public static Atom a(int i) {
        return Atom.the((byte)('a' + i));
    }


    public static void main(String[] args) {

        Global.DEBUG = false;

        for (int length = 3; length < 10; length++) {
            test(new Default(1024,1,1,3).nal(6), length, 1000*length, inh);
        }
    }

    static void test(NAR n, int chainLen, int cycles, IndexedStatementBuilder statementType) {


        DeductiveChainTest test = new DeductiveChainTest(n, chainLen, cycles, statementType) {
//            @Override
//            public TestNAR mustBelieve(long withinCycles, String term, float confidence, float x, float y, float z) throws InvalidInputException {
//                return this;
//            }
        };

        System.out.print(DeductiveChainTest.class.getSimpleName() + " test: "
                + test.q + "?\t");

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


        test.run(false);


        //n.stdout();
        //n.frame(5000);

        int nc = ((Default) n).core.active.size();
        String ts = timestamp(start);
        long time = n.time();

        //n.stdout();
        //n.frame(55); //to print the ending

        //while (true) {

        Report r = test.getReport();

        System.out.println(
                (r.isSuccess() ? "OK" : "ERR") +
                "\t@" + time + " (" + ts + "ms) " +
                nc + 'C');


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
