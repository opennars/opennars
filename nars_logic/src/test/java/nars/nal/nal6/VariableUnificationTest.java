///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.nal.nal6;
//
//import nars.NAR;
//import nars.event.AnswerReaction;
//import nars.meter.TestNAR;
//import nars.nal.AbstractNALTest;
//import nars.nar.Default;
//import nars.task.Task;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.function.Supplier;
//
//import static org.jgroups.util.Util.assertTrue;
//import static org.junit.Assert.assertNotNull;
//
///**
// *
// TODO convert this to AbstractNALTest
// */
//@RunWith(Parameterized.class)
//public class VariableUnificationTest extends AbstractNALTest {
//
//
//    public VariableUnificationTest(Supplier<NAR> b) {
//        super(b);
//    }
//
//    @Parameterized.Parameters(name= "{0}")
//    public static Collection configurations() {
//        return Arrays.asList(new Supplier[][]{
//                {()->new Default() },
//                //{new Default()},
//                //{new DefaultBuffered()},
//                //{new DefaultBuffered().setInternalExperience(null)},
//
//                /*{new Neuromorphic(1)},
//                {new Neuromorphic(4)}*/
//        });
//    }
//
//    @Test public void testDepQueryVariableDistinct() {
//
//        NAR tester = nar();
//
//         /*
//            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
//            shouldn't happen because it should not unify #wat with 4 because its not a query variable
//        */
//        new AnswerReaction(tester) {
//
//            @Override
//            public void onSolution(Task belief) {
//                //nothing should cause this event
//                assertTrue(belief.toString(), false);
//            }
//        };
//
//
//        //tester.nar.stdout();
//        //tester.requires.add(new OutputContainsCondition(tester.nar, "=/> <a --> 4>>.", 5));
//
//
//        tester.input(
//                "<a --> 3>. :\\: \n" +
//                        "<a --> 4>. :/: \n" +
//                        "<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");
//
//        tester.run(32);
//        assertNotNull(tester.concept("<(&/, <a --> 3>, /10/) =/> <a --> 4>>"));
//
//    }
//
//
//    void unaffected(String left, String right) {
//
//
//        TestNAR tester = test();
//
//        tester.mustInput(1, "<" + left + " ==> " + right + ">.");
//        tester.nar.input("<" + left + " ==> " + right + ">.");
//
//        tester.run(4);
//    }
//
//    /*
//        //should not become
//        //<<(*,bird,animal,$1,$2) --> AndShortcut> ==> <$1 --> $3>>>.
//    */
//    final String normA = "<(*,bird,animal,$1,$2) <-> AndShortcut>";
//    final String normB = "<$1 --> $2>";
//    final String normC = "<(*,bird,$1,$abc,$2) <-> AndShortcut>";
//
//    @Test public void testNormalizeSomeVars1ab() {
//        unaffected(normA, normB);
//    }
//    @Test public void testNormalizeSomeVars1ba() {
//        unaffected(normB, normA);
//    }
//
//    @Test @Ignore
//    public void testNormalizeSomeVars1ac() {
//        unaffected(normA, normC);
//    }
//
//
//
//}
