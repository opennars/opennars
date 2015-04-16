package nars.nal.nal4;

import nars.prototype.Curve;
import nars.prototype.Default;
import nars.ProtoNAR;
import nars.io.narsese.InvalidInputException;
import nars.nal.JavaNALTest;
import nars.prototype.Discretinuous;
import nars.prototype.Solid;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL4Test extends JavaNALTest {

    public NAL4Test(ProtoNAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null) },
                {new Default().level(5)},

                //{new DefaultBuffered().setInternalExperience(null) },

                {new Curve().setInternalExperience(null)},
                //{new Discretinuous().level(5) }
                {new Solid(1, 96, 0, 4, 0, 3).level(5)}


        });
    }

    @Test public void recursionSmall() throws InvalidInputException {
        /*
        <0 --> num>. %1.00;0.90% {0 : 1}

        <<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}

        <(*,(*,(*,0))) --> num>?  {0 : 3}

        1200

        ''outputMustContain('<(*,0) --> num>.')
        ''outputMustContain('<(*,(*,0)) --> num>.')
        ''outputMustContain('<(*,(*,(*,0))) --> num>.')
        ''outputMustContain('<(*,(*,(*,0))) --> num>. %1.00;0.26%')
        */

        //TextOutput.out(nar);


        long time = 2000;

        float minConf = 0.66f;
        nar.believe("<0 --> num>", 1.0f, 0.9f);
        nar.believe("<<$1 --> num> ==> <(*,$1) --> num>>", 1.0f, 0.9f);
        nar.ask("<(*,(*,(*,0))) --> num>");
        nar.mustBelieve(time, "<(*,0) --> num>", 1.0f, 1.0f, 0.81f, 1.0f);
        nar.mustBelieve(time, "<(*,(*,0)) --> num>", 1.0f, 1.0f, 0.73f, 1.0f);
        nar.mustBelieve(time, "<(*,(*,(*,0))) --> num>", 1.0f, 1.0f, minConf, 1.0f);
        nar.run();

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

        if (nar.nal() <= 6) {
            time = 75; //less time for the nal6 config
        }
        else {
            time = 800;
        }

        nar.believe(" <0 --> n>", 1.0f, 0.9f);
        nar.believe("<<$1 --> n> ==> <(/,next,$1,_) --> n>>", 1.0f, 0.9f);
        nar.ask("<(/,next,(/,next,0,_),_) --> n>");
        nar.mustBelieve(time, "<(/,next,0,_) --> n>", 1.0f, 1.0f, 0.81f, 1.0f);
        nar.mustBelieve(time, "<(/,next,(/,next,0,_),_) --> n>", 1.0f, 1.0f, finalConf, 1.0f);
        nar.run();
    }

}
