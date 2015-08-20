package nars.nal.nal4;

import nars.NARSeed;
import nars.nal.JavaNALTest;
import nars.nar.Classic;
import nars.nar.Default;
import nars.nar.DefaultDeep;
import nars.nar.NewDefault;
import nars.nar.experimental.Solid;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL4Test extends JavaNALTest {

    private final NARSeed seed;

    public NAL4Test(NARSeed b) { super(b); this.seed = b; }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new DefaultDeep()},
                {new NewDefault()},
                {new NewDefault().setInternalExperience(null)},
                {new Default().setInternalExperience(null) },
                {new Default().level(5)},
                {new Classic().setInternalExperience(null) },

                {new Solid(1, 128, 1, 1, 1, 2).level(5)}


        });
    }


    @Test public void recursionSmall2() throws InvalidInputException {
    /*
        <0 --> n>. %1.0000;0.9000%  {0 : 1<0 --> n>}
        <<$1 --> n> ==> <(/,next,$1,_) --> n>>. %1.0000;0.9000%  {0 : 2 : }
        <(/,next,(/,next,0,_),_) --> n>?  {0 : 3 : }

        27

        ''outputMustContain('<(/,next,0,_) --> n>.')
        ''outputMustContain('<(/,next,(/,next,0,_),_) --> n>.')
        ''outputMustContain('<(/,next,(/,next,0,_),_) --> n>. %1.00;0.73%')


     */
        long time;
        final float finalConf = 0.73f;

        if (seed instanceof Solid) {
            time = 50;
        }
        else {
            if (n.nal() <= 6) {
                time = 400; //less time for the nal6 config
            } else {
                time = 800;
            }
        }

        n.believe(" <0 --> n>", 1.0f, 0.9f);
        n.believe("<<$1 --> n> ==> <(/,next,$1,_) --> n>>", 1.0f, 0.9f);
        n.ask("<(/,next,(/,next,0,_),_) --> n>");
        n.mustBelieve(time, "<(/,next,0,_) --> n>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<(/,next,(/,next,0,_),_) --> n>", 1.0f, 1.0f, finalConf, 1.0f);
        n.run();
    }

}
