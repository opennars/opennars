//package nars.nal.nal2;
//
//
//import nars.NARSeed;
//import nars.nal.ScriptNALTest;
//import nars.nar.Classic;
//import nars.nar.Default;
//import nars.nar.DefaultMicro;
//import org.junit.runners.Parameterized;
//
//import java.util.Collection;
//
//import static nars.io.in.LibraryInput.getParams;
//
//public class NAL2ScriptTests extends ScriptNALTest {
//
//    public NAL2ScriptTests(NARSeed b, String input) {
//        super(b, input);
//    }
//
//    @Parameterized.Parameters(name= "{1} {0}")
//    public static Collection configurations() {
//        return getParams(new String[] { "test2"},
//                new Default(),
//                new Default().setInternalExperience(null),
//                new Default().setInternalExperience(null).level(3), //needs 3 for sets
//                new DefaultMicro(),
//                new Classic()
//        );
//    }
//
//    @Override
//    public int getMaxCycles() { return 600; }
//
//
//}
//
