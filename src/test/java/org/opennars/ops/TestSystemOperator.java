package org.opennars.ops;

import org.junit.Test;
import org.opennars.entity.Sentence;
import org.opennars.interfaces.NarseseConsumer;
import org.opennars.io.events.AnswerHandler;
import org.opennars.language.Term;
import org.opennars.main.Nar;

import static org.junit.Assert.assertTrue;

/**
 * (integration) testing of the ^system op
 */
public class TestSystemOperator {
    @Test
    public void testOpCall() throws Exception {
        { // 0 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test0Ret(nar, "bool");
            nar.cycles(500);

            // check result of call
            MyAnswerHandler handler = new MyAnswerHandler();
            nar.ask("<{?0}-->res>",handler);
            nar.cycles(100);
            assertTrue(handler.lastAnswerTerm.toString().equals("<{true} --> res>"));
        }

        { // 1 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test1Ret(nar, "bool");
            nar.cycles(500);

            // check result of call
            MyAnswerHandler handler = new MyAnswerHandler();
            nar.ask("<{?0}-->res>",handler);
            nar.cycles(100);
            assertTrue(handler.lastAnswerTerm.toString().equals("<{true} --> res>"));
        }

        { // 2 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test2Ret(nar, "bool");
            nar.cycles(500);

            // check result of call
            MyAnswerHandler handler = new MyAnswerHandler();
            nar.ask("<{?0}-->res>",handler);
            nar.cycles(100);
            assertTrue(handler.lastAnswerTerm.toString().equals("<{true} --> res>"));
        }

        { // 3 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test3Ret(nar, "bool");
            nar.cycles(700);

            // check result of call
            MyAnswerHandler handler = new MyAnswerHandler();
            nar.ask("<{?0}-->res>",handler);
            nar.cycles(200);
            assertTrue(handler.lastAnswerTerm.toString().equals("<{true} --> res>"));
        }
    }

    /**
     *
     * @param consumer
     * @param expectedResultType datatype of the expected result
     */
    private static void test0Ret(NarseseConsumer consumer, String expectedResultType) {
        //consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ls, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./src/main/resources/unittest/TestscriptRet"+expectedResultType+".sh, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test1Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./src/main/resources/unittest/TestscriptRet"+expectedResultType+".sh, Arg0, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test2Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./src/main/resources/unittest/TestscriptRet"+expectedResultType+".sh, Arg0, Arg1, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test3Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./src/main/resources/unittest/TestscriptRet"+expectedResultType+".sh, Arg0, Arg1, Arg2, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    static class MyAnswerHandler extends AnswerHandler {
        public Term lastAnswerTerm = null;

        @Override
        public void onSolution(Sentence belief) {
            lastAnswerTerm = belief.term;
        }
    }
}
