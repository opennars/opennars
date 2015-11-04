//package nars.nal.nal3;
//
//
//import nars.NARSeed;
//import nars.nal.ScriptNALTest;
//import nars.nar.Classic;
//import nars.nar.Default;
//import nars.nar.NewDefault;
//import org.junit.runners.Parameterized;
//
//import java.util.Collection;
//
//import static LibraryInput.getParams;
//
//public class NAL3ScriptTests extends ScriptNALTest {
//
//    public NAL3ScriptTests(NARSeed b, String input) {
//        super(b, input);
//    }
//
//    @Parameterized.Parameters(name= "{1} {0}")
//    public static Collection configurations() {
//        return getParams(new String[]{"test3"},
//                new Default(),
//                new Default().setInternalExperience(null),
//                new Default().setInternalExperience(null).level(3),
//                new NewDefault().setInternalExperience(null),
//                //new NewDefault().setInternalExperience(null).level(3),
//                new Classic()
//        );
//    }
//
//    @Override
//    public int getMaxCycles() { return 250; }
//
//
//}
//
