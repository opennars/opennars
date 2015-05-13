package nars.nal.nal8;

import com.sun.xml.internal.bind.v2.TODO;
import nars.NARSeed;
import nars.io.TextOutput;
import nars.nal.JavaNALTest;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Collection;

public class TermFunctionTest extends JavaNALTest {

    public TermFunctionTest(NARSeed b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return NAL8Test.configurations();
    }



    public void testIO(String input, String output) {

        //TextOutput.out(nar);

        nar.mustOutput(16, output);
        nar.input(input);

        nar.run(4);

    }

    @Test public void testOutputInVariablePosition() {
        testIO("count({a,b}, #x)!",
                "<2 --> (/,^count,{a,b},_,SELF)>. :|: %1.00;0.99%");
    }


//TODO: allow this in a special eval operator

//    //almost finished;  just needs condition to match the occurence time that it outputs. otherwise its ok
//
//    @Test
//    public void testRecursiveEvaluation1() {
//        testIO("add( count({a,b}), 2)!",
//                "<(^add,(^count,{a,b},SELF),2,$1,SELF) =/> <$1 <-> 4>>. :|: %1.00;0.90%"
//        );
//    }
//
//    @Test public void testRecursiveEvaluation2() {
//        testIO("count({ count({a,b}), 2})!",
//                "<(^count,{(^count,{a,b},SELF),2},$1,SELF) =/> <$1 <-> 1>>. :|: %1.00;0.90%"
//        );
//    }
}
