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
    public void variable_unification1() throws InvalidInputException {
        n.believe("<<$x --> bird> ==> <$x --> flyer>>").en("If something is a bird, then it is a flyer.");
        n.believe("<<$y --> bird> ==> <$y --> flyer>>",0.00f,0.70f).en("If something is a bird, then it is not a flyer.");
        n.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> flyer>>",0.79f,0.92f).en("If something is a bird, then usually, it is a flyer.");
    }


    @Test
    public void variable_unification2() throws InvalidInputException {
        n.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is a animal.");
        n.believe("<<$y --> robin> ==> <$y --> bird>>").en("If something is a robin, then it is a bird.");
        n.mustBelieve(100,"<<$1 --> robin> ==> <$1 --> animal>>",1.00f,0.81f).en("If something is a robin, then it is a animal.");
        n.mustBelieve(100,"<<$1 --> animal> ==> <$1 --> robin>>",1.00f,0.45f).en(" I guess that if something is a animal, then it is a robin.");
    }


    @Test
    public void variable_unification3() throws InvalidInputException {
        n.believe("<<$x --> swan> ==> <$x --> bird>>",1.00f,0.80f).en("If something is a swan, then it is a bird.");
        n.believe("<<$y --> swan> ==> <$y --> swimmer>>",0.80f,0.9f).en("If something is a swan, then it is a swimmer.");
        n.mustBelieve(100,"<<$1 --> swan> ==> (||,<$1 --> bird>,<$1 --> swimmer>)>",1.00f,0.72f).en("I believe that if something is a swan, then it is a bird or a swimmer.");
        n.mustBelieve(100,"<<$1 --> swan> ==> (&&,<$1 --> bird>,<$1 --> swimmer>)>",0.80f,0.72f).en("I believe that if something is a swan, then usually, it is both a bird and a swimmer.");
        n.mustBelieve(100,"<<$1 --> swimmer> ==> <$1 --> bird>>",1.00f,0.37f).en("I guess if something is a swimmer, then it is a bird.");
        n.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> swimmer>>",0.80f,0.42f).en("I guess if something is a bird, then it is a swimmer.");
        n.mustBelieve(100,"<<$1 --> bird> <=> <$1 --> swimmer>>",0.80f,0.42f).en("I guess something is a bird, if and only if it is a swimmer.");
    }


    @Test
    public void variable_unification4() throws InvalidInputException {
        n.believe("<<bird --> $x> ==> <robin --> $x>>").en("What can be said about bird can also be said about robin.");
        n.believe("<<swimmer --> $y> ==> <robin --> $y>>",0.70f,0.90f).en("What can be said about swimmer usually can also be said about robin.");
        n.mustBelieve(100,"<(&&,<bird --> $1>,<swimmer --> $1>) ==> <robin --> $1>>",1.00f,0.81f).en("What can be said about bird and swimmer can also be said about robin.");
        n.mustBelieve(100,"<(||,<bird --> $1>,<swimmer --> $1>) ==> <robin --> $1>>",0.70f,0.81f).en("What can be said about bird or swimmer can also be said about robin.");
        n.mustBelieve(100,"<<bird --> $1> ==> <swimmer --> $1>>",1.00f,0.36f).en("I guess what can be said about bird can also be said about swimmer.");
        n.mustBelieve(100,"<<swimmer --> $1> ==> <bird --> $1>>",0.70f,0.45f).en("I guess what can be said about swimmer can also be said about bird.");
        n.mustBelieve(100,"<<bird --> $1> <=> <swimmer --> $1>>",0.70f,0.45f).en("I guess bird and swimmer share most properties.");
    }


    @Test
    public void variable_unification5() throws InvalidInputException {
        n.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>) ==> <$x --> bird>>").en("If something can fly and chirp, then it is a bird.");
        n.believe("<<$y --> [with-wings]> ==> <$y --> flyer>>").en("If something has wings, then it can fly.");
        n.mustBelieve(100,"<(&&,<$1 --> [chirping]>,<$1 --> [with-wings]>) ==> <$1 --> bird>>",1.00f,0.81f).en("If something can chirp and has wings, then it is a bird.");
    }


    @Test
    public void variable_unification6() throws InvalidInputException {
        n.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>, <(*, $x, worms) --> food>) ==> <$x --> bird>>").en("If something can fly, chirp, and eats worms, then it is a bird.");
        n.believe("<(&&,<$y --> [chirping]>,<$y --> [with-wings]>) ==> <$y --> bird>>").en("If something can chirp and has wings, then it is a bird.");
        n.mustBelieve(100,"<(&&,<$1 --> flyer>,<(*,$1,worms) --> food>) ==> <$1 --> [with-wings]>>",1.00f,0.45f).en("If something can fly and eats worms, then I guess it has wings.");
        n.mustBelieve(100,"<<$1 --> [with-wings]> ==> (&&,<$1 --> flyer>,<(*,$1,worms) --> food>)>",1.00f,0.45f).en("I guess if something has wings, then it can fly and eats worms.");
    }


    @Test
    public void variable_unification7() throws InvalidInputException {
        n.believe("<(&&,<$x --> flyer>,<(*,$x,worms) --> food>) ==> <$x --> bird>>").en("If something can fly and eats worms, then it is a bird.");
        n.believe("<<$y --> flyer> ==> <$y --> [with-wings]>>").en("If something can fly, then it has wings.");
        n.mustBelieve(100,"<(&&,<$1 --> [with-wings]>,<worms --> (/,food,$1,_)>) ==> <$1 --> bird>>",1.00f,0.45f).en("If something has wings and eats worms, then I guess it is a bird.");
    }


    @Test
    public void variable_elimination() throws InvalidInputException {
        n.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is an animal.");
        n.believe("<robin --> bird>").en("A robin is a bird.");
        n.mustBelieve(100,"<robin --> animal>",1.00f,0.81f).en("A robin is an animal.");
    }


    @Test
    public void variable_elimination2() throws InvalidInputException {
        n.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is an animal.");
        n.believe("<tiger --> animal>").en("A tiger is an animal.");
        n.mustBelieve(100, "<tiger --> bird>", 1.00f,0.45f).en("I guess that a tiger is a bird.");
    }


    @Test
    public void variable_elimination3() throws InvalidInputException {
        n.believe("<<$x --> animal> <=> <$x --> bird>>").en("Something is a animal if and only if it is a bird.");
        n.believe("<robin --> bird>").en("A robin is a bird.");
        n.mustBelieve(100,"<robin --> animal>",1.00f,0.81f).en("A robin is a animal.");
    }


    @Test
    public void variable_elimination4() throws InvalidInputException {
        n.believe("(&&,<#x --> bird>,<#x --> swimmer>)").en("Some bird can swim.");
        n.believe("<swan --> bird>",0.90f,0.9f).en("Swan is a type of bird.");
        n.mustBelieve(100,"<swan --> swimmer>",0.90f,0.43f).en("I guess swan can swim.");
    }


    @Test
    public void variable_elimination5() throws InvalidInputException {
        n.believe("<{Tweety} --> [with-wings]>").en("Tweety has wings.");
        n.believe("<(&&,<$x --> [chirping]>,<$x --> [with-wings]>) ==> <$x --> bird>>").en("If something can chirp and has wings, then it is a bird.");
        n.mustBelieve(100,"<<{Tweety} --> [chirping]> ==> <{Tweety} --> bird>>",1.00f,0.81f).en("If Tweety can chirp, then it is a bird.");
    }


    @Test
    public void variable_elimination6() throws InvalidInputException {
        n.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>, <(*, $x, worms) --> food>) ==> <$x --> bird>>").en("If something can fly, chirp, and eats worms, then it is a bird.");
        n.believe("<{Tweety} --> flyer>").en("Tweety can fly.");
        n.mustBelieve(100,"<(&&,<{Tweety} --> [chirping]>,<(*,{Tweety},worms) --> food>) ==> <{Tweety} --> bird>>",1.00f,0.81f).en("If Tweety can chirp and eats worms, then it is a bird.");
    }


    @Test
    public void multiple_variable_elimination() throws InvalidInputException {
        n.believe("<(&&,<$x --> key>,<$y --> lock>) ==> <$y --> (/,open,$x,_)>>").en("Every lock can be opened by every key.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>",1.00f,0.81f).en("Lock-1 can be opened by every key.");
    }


    @Test
    public void multiple_variable_elimination2() throws InvalidInputException {
        n.believe("<<$x --> lock> ==> (&&,<#y --> key>,<$x --> (/,open,#y,_)>)>").en("Every lock can be opened by some key.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"(&&,<#1 --> key>,<{lock1} --> (/,open,#1,_)>)",1.00f,0.81f).en("Some key can open Lock-1.");
    }


    @Test
    public void multiple_variable_elimination3() throws InvalidInputException {
        n.believe("(&&,<#x --> lock>,<<$y --> key> ==> <#x --> (/,open,$y,_)>>)").en("There is a lock that can be opened by every key.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>",1.00f,0.43f).en("I guess Lock-1 can be opened by every key.");
    }


    @Test
    public void multiple_variable_elimination4() throws InvalidInputException {
        n.believe("(&&,<#x --> (/,open,#y,_)>,<#x --> lock>,<#y --> key>)").en("There is a key that can open some lock.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"(&&,<#1 --> key>,<(*,#1,{lock1}) --> open>)",1.00f,0.43f).en("I guess there is a key that can open Lock-1.");
    }


    @Test
    public void variable_introduction() throws InvalidInputException {
        n.believe("<swan --> bird>").en("A swan is a bird.");
        n.believe("<swan --> swimmer>",0.80f,0.9f).en("A swan is usually a swimmer.");
        n.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> swimmer>>",0.80f,0.45f).en("I guess a bird is usually a swimmer.");
        n.mustBelieve(100,"<<$1 --> swimmer> ==> <$1 --> bird>>",1.00f,0.39f).en("I guess a swimmer is a bird.");
        n.mustBelieve(100,"<<$1 --> bird> <=> <$1 --> swimmer>>",0.80f,0.45f).en("I guess a bird is usually a swimmer, and the other way around.");
        n.mustBelieve(100,"(&&,<#1 --> bird>,<#1 --> swimmer>)",0.80f,0.81f).en("Some bird can swim.");
    }


    @Test
    public void variable_introduction2() throws InvalidInputException {
        n.believe("<gull --> swimmer>").en("A gull is a swimmer.");
        n.believe("<swan --> swimmer>",0.80f,0.9f).en("Usually, a swan is a swimmer.");
        n.mustBelieve(100,"<<gull --> $1> ==> <swan --> $1>>",0.80f,0.45f).en("I guess what can be said about gull usually can also be said about swan.");
        n.mustBelieve(100,"<<swan --> $1> ==> <gull --> $1>>",1.00f,0.39f).en("I guess what can be said about swan can also be said about gull.");
        n.mustBelieve(100,"<<gull --> $1> <=> <swan --> $1>>",0.80f,0.45f).en("I guess gull and swan share most properties.");
        n.mustBelieve(100,"(&&,<gull --> #1>,<swan --> #1>)",0.80f,0.81f).en("Gull and swan have some common property.");
    }


    @Test
    public void variables_introduction() throws InvalidInputException {
        n.believe("<{key1} --> (/,open,_,{lock1})>").en("Key-1 opens Lock-1.");
        n.believe("<{key1} --> key>").en("Key-1 is a key.");
        n.mustBelieve(100, "<<$1 --> key> ==> <$1 --> (/,open,_,{lock1})>>", 1.00f,0.45f).en("I guess every key can open Lock-1.");
        n.mustBelieve(100,"(&&,<#1 --> (/,open,_,{lock1})>,<#1 --> key>)",1.00f, 0.81f).en("Some key can open Lock-1.");
    }


    @Test
    public void multiple_variables_introduction() throws InvalidInputException {
        n.believe("<<$x --> key> ==> <{lock1} --> (/,open,$x,_)>>").en("Lock-1 can be opened by every key.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)",1.00f,0.81f).en("There is a lock that can be opened by every key.");
        n.mustBelieve(100,"<(&&,<$1 --> key>,<$2 --> lock>) ==> <$2 --> (/,open,$1,_)>>",1.00f,0.45f).en("I guess every lock can be opened by every key.");
    }


    @Test
    public void multiple_variables_introduction2() throws InvalidInputException {
        n.believe("(&&,<#x --> key>,<{lock1} --> (/,open,#x,_)>)").en("Lock-1 can be opened by some key.");
        n.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        n.mustBelieve(100,"(&&,<#1 --> key>,<#2 --> lock>,<#2 --> (/,open,#1,_)>)",1.00f,0.81f).en("There is a key that can open some lock.");
        n.mustBelieve(100,"<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>",1.00f,0.45f).en("I guess every lock can be opened by some key.");
    }


    @Test
    public void second_level_variable_unification() throws InvalidInputException {
        n.believe("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)",1.00f,0.90f).en("there is a lock which is opened by all keys");
        n.believe("<{key1} --> key>",1.00f,0.90f).en("key1 is a key");
        n.mustBelieve(100,"(&&,<#1 --> lock>,<#1 --> (/,open,{key1},_)>)",1.00f,0.81f).en("there is a lock which is opened by key1");
    }


    @Test
    public void second_level_variable_unification2() throws InvalidInputException {
        n.believe("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>",1.00f,0.90f).en("all locks are opened by some key");
        n.believe("<{key1} --> key>",1.00f,0.90f).en("key1 is a key");
        n.mustBelieve(100,"<<$1 --> lock> ==> <$1 --> (/,open,{key1},_)>>",1.00f,0.43f).en("maybe all locks are opened by key1");
    }


    @Test
    public void second_variable_introduction_induction() throws InvalidInputException {
        n.believe("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>").en("if something opens lock1, it is a key");
        n.believe("<lock1 --> lock>").en("lock1 is a key");
        n.mustBelieve(100,"<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.45f).en("there is a lock with the property that when opened by something, this something is a key (induction)");
    }


    @Test
    public void variable_elimination_deduction() throws InvalidInputException {
        n.believe("<lock1 --> lock>",1.00f,0.90f).en("lock1 is a lock");
        n.believe("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.90f).en("there is a lock with the property that when opened by something, this something is a key");
        n.mustBelieve(100,"<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>",1.00f,0.43f).en("whatever opens lock1 is a key");
    }


    @Test
    public void abduction_with_variable_elimination_abduction() throws InvalidInputException {
        n.believe("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>",1.00f,0.90f).en("whatever opens lock1 is a key");
        n.believe("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.90f).en("there is a lock with the property that when opened by something, this something is a key");
        n.mustBelieve(100,"<lock1 --> lock>",1.00f,0.45f).en("lock1 is a lock");
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
