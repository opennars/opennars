package nars.nal.nal6;

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

/**
 * Created by me on 8/19/15.
 */
public class NAL6Test extends JavaNALTest {

        private final NARSeed seed;

        public NAL6Test(NARSeed b) { super(b); this.seed = b; }

        @Parameterized.Parameters(name= "{0}")
        public static Collection configurations() {
            return Arrays.asList(new Object[][]{
                    {new Default()},
                    {new DefaultDeep()},
                    {new NewDefault()},
                    {new NewDefault().setInternalExperience(null)},
                    {new Default().setInternalExperience(null) },
                    {new Default().level(6)},
                    {new Classic().setInternalExperience(null) },

                    {new Solid(1, 128, 1, 1, 1, 2).level(6)}


            });
        }


        @Test
    public void recursionSmall() throws InvalidInputException {
        /*
        <0 --> num>. %1.00;0.90% {0 : 1}

        <<$1 --> num> ==> <($1) --> num>>. %1.00;0.90% {0 : 2}

        <(((0))) --> num>?  {0 : 3}

        1200

        ''outputMustContain('<(0) --> num>.')
        ''outputMustContain('<((0)) --> num>.')
        ''outputMustContain('<(((0))) --> num>.')
        ''outputMustContain('<(((0))) --> num>. %1.00;0.26%')
        */

        //TextOutput.out(nar);


        long time = seed instanceof Solid ? 100 : 2500;

        float minConf = 0.66f;
        n.believe("<0 --> num>", 1.0f, 0.9f);
        n.believe("<<$1 --> num> ==> <($1) --> num>>", 1.0f, 0.9f);
        n.ask("<(((0))) --> num>");
        n.mustBelieve(time, "<(0) --> num>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<((0)) --> num>", 1.0f, 1.0f, 0.73f, 1.0f);
        n.mustBelieve(time, "<(((0))) --> num>", 1.0f, 1.0f, minConf, 1.0f);
        n.run();

    }

}
