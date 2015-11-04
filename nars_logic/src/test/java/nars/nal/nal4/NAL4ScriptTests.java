//package nars.nal.nal4;
//
//
//import nars.NARSeed;
//import nars.nal.ScriptNALTest;
//import nars.nar.Default;
//import org.junit.runners.Parameterized;
//
//import java.util.Collection;
//
//import static LibraryInput.getParams;
//
//public class NAL4ScriptTests extends ScriptNALTest {
//
//    public NAL4ScriptTests(NARSeed b, String input) {
//        super(b, input);
//    }
//
//    @Parameterized.Parameters(name= "{1} {0}")
//    public static Collection configurations() {
//        return getParams(new String[]{"test4"},
//                new Default(),
//                new Default().setInternalExperience(null),
//                new Default().level(4)
//                //new Solid(1, 96, 1, 1, 1, 3).level(4)
//        );
//
//    }
//
//    @Override
//    public int getMaxCycles() {
//        return 1300;
//    }
//
//
//}
//
