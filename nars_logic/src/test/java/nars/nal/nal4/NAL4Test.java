package nars.nal.nal4;

import nars.NARSeed;
import nars.nal.JavaNALTest;
import nars.nar.Classic;
import nars.nar.Curve;
import nars.nar.Default;
import nars.nar.Solid;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL4Test extends JavaNALTest {

    public NAL4Test(NARSeed b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null) },
                {new Default().level(5)},
                {new Classic().setInternalExperience(null) },
                //{new DefaultBuffered().setInternalExperience(null) },

                {new Curve().setInternalExperience(null)},
                //{new Discretinuous().level(5) }
                {new Solid(1, 128, 1, 1, 1, 2).level(6)}


        });
    }

    @Test public void recursionSmall() throws InvalidInputException {
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


        long time = 1500;

        float minConf = 0.66f;
        n.believe("<0 --> num>", 1.0f, 0.9f);
        n.believe("<<$1 --> num> ==> <($1) --> num>>", 1.0f, 0.9f);
        n.ask("<(((0))) --> num>");
        n.mustBelieve(time, "<(0) --> num>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<((0)) --> num>", 1.0f, 1.0f, 0.73f, 1.0f);
        n.mustBelieve(time, "<(((0))) --> num>", 1.0f, 1.0f, minConf, 1.0f);
        n.run();

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

        if (n.nal() <= 6) {
            time = 75; //less time for the nal6 config
        }
        else {
            time = 800;
        }

        n.believe(" <0 --> n>", 1.0f, 0.9f);
        n.believe("<<$1 --> n> ==> <(/,next,$1,_) --> n>>", 1.0f, 0.9f);
        n.ask("<(/,next,(/,next,0,_),_) --> n>");
        n.mustBelieve(time, "<(/,next,0,_) --> n>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<(/,next,(/,next,0,_),_) --> n>", 1.0f, 1.0f, finalConf, 1.0f);
        n.run();
    }

}
