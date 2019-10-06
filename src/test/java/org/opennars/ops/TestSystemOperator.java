package org.opennars.ops;

import org.opennars.interfaces.NarseseConsumer;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * (integration) testing of the ^system op
 */
public class TestSystemOperator {
    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        { // 0 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test0Ret(nar, "bool");
            nar.cycles(500);

            // TODO< check result of call >

            int here = 6;
        }

        { // 1 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test1Ret(nar, "bool");
            nar.cycles(500);

            // TODO< check result of call >

            int here = 6;
        }

        { // 2 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test2Ret(nar, "bool");
            nar.cycles(500);

            // TODO< check result of call >

            int here = 6;
        }

        { // 3 parameters, boolean result
            Nar nar = new Nar();
            nar.addPlugin(new org.opennars.operator.misc.System());
            test3Ret(nar, "bool");
            nar.cycles(500);

            // TODO< check result of call >

            int here = 6;
        }
    }

    /**
     *
     * @param consumer
     * @param expectedResultType datatype of the expected result
     */
    private static void test0Ret(NarseseConsumer consumer, String expectedResultType) {
        //consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ls, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./TestscriptRet"+expectedResultType+".sh, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test1Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./TestscriptRet"+expectedResultType+".sh, Arg0, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test2Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./TestscriptRet"+expectedResultType+".sh, Arg0, Arg1, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }

    private static void test3Ret(NarseseConsumer consumer, String expectedResultType) {
        consumer.addInput("<(&/, <cond0-->Cond0>, (^system, {SELF}, ./TestscriptRet"+expectedResultType+".sh, Arg0, Arg1, Arg2, $ret)) =/> <{$ret}-->res>>.");
        consumer.addInput("<cond0-->Cond0>. :|:");
        consumer.addInput("<{#0}-->res>!");
    }
}
