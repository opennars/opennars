package nars.core.logic.nal4;

import nars.core.AbstractNALTest;
import nars.core.Build;
import nars.core.build.Default;
import nars.io.narsese.Narsese;
import org.junit.Test;


public class NAL4Test extends AbstractNALTest {


    @Override
    public Build build() {
        return new Default().level(6);
    }

    @Test public void recursionSmall() throws Narsese.InvalidInputException {
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

        n.believe("<0 --> num>", 1.0f, 0.9f);
        n.believe("<<$1 --> num> ==> <(*,$1) --> num>>", 1.0f, 0.9f);
        n.ask("<(*,(*,(*,0))) --> num>");
        n.mustBelieve(time, "<(*,0) --> num>", 1.0f, 1.0f, 0.1f, 1.0f);
        n.mustBelieve(time, "<(*,(*,0)) --> num>", 1.0f, 1.0f, 0.1f, 1.0f);
        n.mustBelieve(time, "<(*,(*,(*,0))) --> num>", 1.0f, 1.0f, 0.26f, 1.0f);

    }
    
    @Test public void recursionSmall2() {
    /*
        <0 --> n>. %1.0000;0.9000%  {0 : 1<0 --> n>}
        <<$1 --> n> ==> <(/,next,$1,_) --> n>>. %1.0000;0.9000%  {0 : 2 : }
        <(/,next,(/,next,0,_),_) --> n>?  {0 : 3 : }

        27

        ''outputMustContain('<(/,next,0,_) --> n>.')
        ''outputMustContain('<(/,next,(/,next,0,_),_) --> n>.')
        ''outputMustContain('<(/,next,(/,next,0,_),_) --> n>. %1.00;0.73%')


     */
        
    }

}
