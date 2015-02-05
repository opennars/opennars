package nars.logic.nal4;

import nars.build.Curve;
import nars.build.DefaultBuffered;
import nars.core.Build;
import nars.build.Default;
import nars.logic.AbstractNALTest;
import nars.io.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL4Test extends AbstractNALTest {

    public NAL4Test(Build b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null) },
                {new DefaultBuffered().setInternalExperience(null) },
                {new Default().level(6)},
                {new Curve().setInternalExperience(null)}

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

        long time = 1400;

        float minConf = 0.26f;
        n.believe("<0 --> num>", 1.0f, 0.9f);
        n.believe("<<$1 --> num> ==> <(*,$1) --> num>>", 1.0f, 0.9f);
        n.ask("<(*,(*,(*,0))) --> num>");
        n.mustBelieve(time, "<(*,0) --> num>", 1.0f, 1.0f, 0.1f, 1.0f);
        n.mustBelieve(time, "<(*,(*,0)) --> num>", 1.0f, 1.0f, 0.1f, 1.0f);
        n.mustBelieve(time, "<(*,(*,(*,0))) --> num>", 1.0f, 1.0f, minConf, 1.0f);

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
        float finalConf;

        if (n.nal() <= 6) {
            time = 20;
            finalConf = 0.73f;
        }
        else {
            time = 800;
            finalConf = 0.29f;
        }

        n.believe(" <0 --> n>", 1.0f, 0.9f);
        n.believe("<<$1 --> n> ==> <(/,next,$1,_) --> n>>", 1.0f, 0.9f);
        n.ask("<(/,next,(/,next,0,_),_) --> n>");
        n.mustBelieve(time, "<(/,next,0,_) --> n>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<(/,next,(/,next,0,_),_) --> n>", 1.0f, 1.0f, finalConf, 1.0f);
    }

}
