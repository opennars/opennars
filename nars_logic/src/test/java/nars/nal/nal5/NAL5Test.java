package nars.nal.nal5;

import nars.Events;
import nars.NAR;
import nars.NARSeed;
import nars.event.NARReaction;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.nar.DefaultDeep;
import nars.nar.DefaultMicro;
import nars.nar.NewDefault;
import nars.narsese.InvalidInputException;
import nars.task.Task;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static nars.nal.nal7.Tense.Eternal;


public class NAL5Test extends JavaNALTest {

    public NAL5Test(NARSeed b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                { new Default() },
                { new Default().setInternalExperience(null) },
                { new Default().level(5) },
                { new Default().level(6) },
                {new NewDefault()},
                {new NewDefault().setInternalExperience(null)},
                { new DefaultMicro().level(5) },
                { new DefaultDeep().level(5) }

                //{ new Neuromorphic(4) },

        });
    }

    @Test
    public void revision() throws InvalidInputException {
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>").en("If robin can fly then robin is a type of bird.");
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.00f,0.60f).en("If robin can fly then robin may not a type of bird.");
        n.mustBelieve(100,"<<robin --> [flying]> ==> <robin --> bird>>",0.86f,0.91f).en("If robin can fly then robin is a type of bird.");
    }

    @Test
    public void deduction() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>").en("If robin can fly then robin is a type of bird.");
        n.mustBelieve(100,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.81f).en("If robin can fly then robin is a type of animal.");
    }

    @Test
    public void exemplification() throws InvalidInputException {
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>").en("If robin can fly then robin is a type of bird.");
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.mustBelieve(100,"<<robin --> animal> ==> <robin --> [flying]>>.",1.00f,0.45f).en("I guess if robin is a type of animal then robin can fly.");
    }


    @Test
    public void induction() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f).en("If robin is a type of bird then robin can fly.");
        n.mustBelieve(100,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.39f).en("I guess if robin can fly then robin is a type of animal.");
        n.mustBelieve(100,"<<robin --> animal> ==> <robin --> [flying]>>",0.80f,0.45f).en("I guess if robin is a type of animal then robin can fly.");
    }


    @Test
    public void abduction() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.8f,0.9f).en("If robin can fly then robin is probably a type of animal.");
        n.mustBelieve(100,"<<robin --> bird> ==> <robin --> [flying]>>",1.00f,0.39f).en("I guess if robin is a type of bird then robin can fly.");
        n.mustBelieve(100,"<<robin --> [flying]> ==> <robin --> bird>>",0.80f,0.45f).en("I guess if robin can fly then robin is a type of bird.");
    }


    @Test
    public void detachment() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin can fly.");
        n.believe("<robin --> bird>").en("Robin is a type of bird.");
        n.mustBelieve(100,"<robin --> animal>",1.00f,0.81f).en("Robin is a type of animal.");
    }


    @Test
    public void detachment2() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>",0.70f,0.90f).en("Usually if robin is a type of bird then robin is a type of animal.");
        n.believe("<robin --> animal>").en("Robin is a type of animal.");
        n.mustBelieve(100,"<robin --> bird>",1.00f,0.36f).en("I guess robin is a type of bird.");
    }


    @Test
    public void comparison() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f).en("If robin is a type of bird then robin can fly.");
        n.mustBelieve(100,"<<robin --> animal> <=> <robin --> [flying]>>",0.80f,0.45f).en("I guess robin is a type of animal if and only if robin can fly.");
    }


    @Test
    public void comparison2() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>",0.7f,0.9f).en("If robin is a type of bird then usually robin is a type of animal.");
        n.believe("<<robin --> [flying]> ==> <robin --> animal>>").en("If robin can fly then robin is a type of animal.");
        n.mustBelieve(100,"<<robin --> bird> <=> <robin --> [flying]>>",0.70f,0.45f).en("I guess robin is a type of bird if and only if robin can fly.");
    }


    @Test
    public void analogy() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f).en("Usually, robin is a type of bird if and only if robin can fly.");
        n.mustBelieve(100,"<<robin --> [flying]> ==> <robin --> animal>>",0.80f,0.65f).en("If robin can fly then probably robin is a type of animal.");
    }


    @Test
    public void analogy2() throws InvalidInputException {
        n.believe("<robin --> bird>").en("Robin is a type of bird.");
        n.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f).en("Usually, robin is a type of bird if and only if robin can fly.");
        n.mustBelieve(100,"<robin --> [flying]>",0.80f,0.65f).en("I guess usually robin can fly.");
    }


    @Test
    public void resemblance() throws InvalidInputException {
        n.believe("<<robin --> animal> <=> <robin --> bird>>").en("Robin is a type of animal if and only if robin is a type of bird.");
        n.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.9f,0.9f).en("Robin is a type of bird if and only if robin can fly.");
        n.mustBelieve(100," <<robin --> animal> <=> <robin --> [flying]>>",0.90f,0.81f).en("Robin is a type of animal if and only if robin can fly.");
    }


    @Test
    public void conversions_between_Implication_and_Equivalence() throws InvalidInputException {
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.9f,0.9f).en("If robin can fly then robin is a type of bird.");
        n.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f).en("If robin is a type of bird then robin can fly.");
        n.mustBelieve(100," <<robin --> bird> <=> <robin --> [flying]>>",0.81f,0.81f).en("Robin can fly if and only if robin is a type of bird.");
    }


    @Test
    public void compound_composition_two_premises() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f).en("If robin is a type of bird then robin can fly.");
        n.mustBelieve(100," <<robin --> bird> ==> (&&,<robin --> [flying]>,<robin --> animal>)>",0.90f,0.81f).en("If robin is a type of bird then usually robin is a type of animal and can fly.");
        n.mustBelieve(100," <<robin --> bird> ==> (||,<robin --> [flying]>,<robin --> animal>)>",1.00f,0.81f).en("If robin is a type of bird then robin is a type of animal or can fly.");
    }


    @Test
    public void compound_composition_two_premises2() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> <robin --> animal>>").en("If robin is a type of bird then robin is a type of animal.");
        n.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.9f,0.9f).en("If robin can fly then robin is a type of animal.");
        n.mustBelieve(100," <(&&,<robin --> [flying]>,<robin --> bird>) ==> <robin --> animal>>",1.00f,0.81f).en("If robin can fly and is a type of bird then robin is a type of animal.");
        n.mustBelieve(100," <(||,<robin --> [flying]>,<robin --> bird>) ==> <robin --> animal>>",0.90f,0.81f).en("If robin can fly or is a type of bird then robin is a type of animal.");
    }


    @Test
    public void compound_decomposition_two_premises1() throws InvalidInputException {
        n.believe("<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>",0.0f,0.9f).en("If robin is a type of bird then robin is not a type of flying animal.");
        n.believe("<<robin --> bird> ==> <robin --> [flying]>>").en("If robin is a type of bird then robin can fly.");
        n.mustBelieve(100," <<robin --> bird> ==> <robin --> animal>>",0.00f,0.81f).en("It is unlikely that if a robin is a type of bird then robin is a type of animal.");
    }


    @Test
    public void compound_decomposition_two_premises2() throws InvalidInputException {
        n.believe("(&&,<robin --> [flying]>,<robin --> swimmer>)",0.0f,0.9f).en("Robin cannot be both a flyer and a swimmer.");
        n.believe("<robin --> [flying]>").en("Robin can fly.");
        n.mustBelieve(100,"<robin --> swimmer>",0.00f,0.81f).en("Robin cannot swim.");
    }


    @Test
    public void compound_decomposition_two_premises3() throws InvalidInputException {
        n.believe("(||,<robin --> [flying]>,<robin --> swimmer>)").en("Robin can fly or swim.");
        n.believe("<robin --> swimmer>",0.0f,0.9f).en("Robin cannot swim.");
        n.mustBelieve(100,"<robin --> [flying]>",1.00f,0.81f).en("Robin can fly.");
    }


    @Test
    public void compound_composition_one_premises() throws InvalidInputException {
        n.believe("<robin --> [flying]>").en("Robin can fly.");
        n.ask("(||,<robin --> [flying]>,<robin --> swimmer>)").en("Can robin fly or swim?");
        n.mustBelieve(100," (||,<robin --> swimmer>,<robin --> [flying]>)",1.00f,0.81f).en("Robin can fly or swim.");
    }


    @Test
    public void compound_decomposition_one_premises() throws InvalidInputException {
        n.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)",0.9f,0.9f).en("Robin can fly and swim.");
        n.mustBelieve(100,"<robin --> swimmer>",0.9f,0.73f).en("Robin can swim.");
        n.mustBelieve(100,"<robin --> [flying]>",0.9f,0.73f).en("Robin can fly.");
    }


    @Test
    public void negation() throws InvalidInputException {
        n.believe("(--,<robin --> [flying]>)",0.1f).en("It is unlikely that robin cannot fly.");
        n.mustBelieve(100,"<robin --> [flying]>",0.90f,0.90f).en("Robin can fly.");
    }


    @Test
    public void negation2() throws InvalidInputException {
        n.believe("<robin --> [flying]>",0.9f,0.9f).en("Robin can fly.");
        n.ask("(--,<robin --> [flying]>)").en("Can robin fly or not?");
        n.mustBelieve(100,"(--,<robin --> [flying]>)",0.10f,0.90f).en("It is unlikely that robin cannot fly.");
    }


    @Test
    public void contraposition() throws InvalidInputException {
        n.believe("<(--,<robin --> bird>) ==> <robin --> [flying]>>", 0.1f, 0.9f).en("It is unlikely that if robin is not a type of bird then robin can fly.");
        n.ask("<(--,<robin --> [flying]>) ==> <robin --> bird>>").en("If robin cannot fly then is robin a type of bird ? ");
        n.mustBelieve(100, " <(--,<robin --> [flying]>) ==> <robin --> bird>>", 0.00f, 0.45f).en("I guess it is unlikely that if robin cannot fly then robin is a type of bird.");
    }


    @Test
    public void conditional_deduction() throws InvalidInputException {
        n.believe("<(&&,<robin --> [flying]>,<robin --> [with-wings]>) ==> <robin --> bird>>").en("If robin can fly and has wings then robin is a bird.");
        n.believe("<robin --> [flying]>").en("robin can fly.");
        n.mustBelieve(100," <<robin --> [with-wings]> ==> <robin --> bird>>",1.00f,0.81f).en("If robin has wings then robin is a bird");
    }


    @Test
    public void conditional_deduction2() throws InvalidInputException {
        n.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>,<robin --> [with-wings]>) ==> <robin --> bird>>").en("If robin can fly, has wings, and chirps, then robin is a bird");
        n.believe("<robin --> [flying]>").en("robin can fly.");
        n.mustBelieve(100," <(&&,<robin --> [chirping]>,<robin --> [with-wings]>) ==> <robin --> bird>>",1.00f,0.81f).en("If robin has wings and chirps then robin is a bird.");
    }


    @Test
    public void conditional_deduction3() throws InvalidInputException {
        n.believe("<(&&,<robin --> bird>,<robin --> [living]>) ==> <robin --> animal>>").en("If robin is a bird and it's living, then robin is an animal");
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>").en("If robin can fly, then robin is a bird");
        n.mustBelieve(100," <(&&,<robin --> [flying]>,<robin --> [living]>) ==> <robin --> animal>>",1.00f,0.81f).en("If robin is living and it can fly, then robin is an animal.");
    }


    @Test
    public void conditional_abduction() throws InvalidInputException {
        n.believe("<<robin --> [flying]> ==> <robin --> bird>>").en("If robin can fly then robin is a bird.");
        n.believe("<(&&,<robin --> swimmer>,<robin --> [flying]>) ==> <robin --> bird>>").en("If robin both swims and flys then robin is a bird.");
        n.mustBelieve(100," <robin --> swimmer>",1.00f,0.45f).en("I guess robin swims.");
    }


    @Test
    public void conditional_abduction2() throws InvalidInputException {
        n.believe("<(&&,<robin --> [with-wings]>,<robin --> [chirping]>) ==> <robin --> bird>>").en("If robin is has wings and chirps, then robin is a bird");
        n.believe("<(&&,<robin --> [flying]>,<robin --> [with-wings]>,<robin --> [chirping]>) ==> <robin --> bird>>").en("If robin can fly, has wings, and chirps, then robin is a bird");
        n.mustBelieve(100," <robin --> [flying]>",1.00f,0.45f).en("I guess that robin can fly.");
    }


    @Test
    public void conditional_abduction3() throws InvalidInputException {
        n.believe("<(&&,<robin --> [flying]>,<robin --> [with-wings]>) ==> <robin --> [living]>>",0.9f,0.9f).en("If robin can fly and it has wings, then robin is living.");
        n.believe("<(&&,<robin --> [flying]>,<robin --> bird>) ==> <robin --> [living]>>.").en("If robin can fly and robin is a bird then robin is living.");
        n.mustBelieve(100,"<<robin --> bird> ==> <robin --> [with-wings]>>",1.00f,0.42f).en("I guess if robin is a bird, then robin has wings.");
        n.mustBelieve(100,"<<robin --> [with-wings]> ==> <robin --> bird>>",0.90f,0.45f).en("I guess if robin has wings, then robin is a bird.");
    }


    @Test
    public void conditional_induction() throws InvalidInputException {
        n.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>) ==> <robin --> bird>>").en("If robin can fly and robin chirps, then robin is a bird");
        n.believe("<<robin --> [flying]> ==> <robin --> [with-beak]>>",0.9f,0.9f).en("If robin can fly then usually robin has a beak.");
        n.mustBelieve(100,"<(&&,<robin --> [chirping]>,<robin --> [with-beak]>) ==> <robin --> bird>>",1.00f,0.42f).en("I guess that if robin chirps and robin has a beak, then robin is a bird.");
    }



    @Test public void deriveFromConjunctionComponents() {
        n.believe("(&&,<a --> b>,<b-->a>)", Eternal, 1.0f, 0.9f);

        //TODO find the actual value for these intermediate steps, i think it is 81%
        n.mustBelieve(70, "<a --> b>", 1f, 0.81f);
        n.mustBelieve(70, "<b --> a>", 1f, 0.81f);

        n.mustBelieve(70, "<a <-> b>", 1.0f, 0.66f);
        n.run();
    }

    /** 5.15 */
    @Test public void compoundDecompositionTwoPremises() throws InvalidInputException {
        /*
        'If robin is a type of bird then robin is not a type of flying animal.
        <<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>. %0%

        'If robin is a type of bird then robin can fly.
        <<robin --> bird> ==> <robin --> [flying]>>.

        8

        'It is unlikely that if a robin is a type of bird then robin is a type of animal.
        ''outputMustContain('<<robin --> bird> ==> <robin --> animal>>. %0.00;0.81%')
        */

        long time = 512;
        //TextOutput.out(n);

        n.believe("<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>", Eternal, 0.0f, 0.9f)
                .en("If robin is a type of bird then robin is not a type of flying animal.");
        /*n.believe("(--,<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>)", Eternal, 1.0f, 0.9f)
                .en("If robin is a type of bird then robin is not a type of flying animal.");*/


        n.believe("<<robin --> bird> ==> <robin --> [flying]>>", Eternal, 1f, 0.9f )
                .en("If robin is a type of bird then robin can fly.");

        n.mustBelieve(time, "<<robin --> bird> ==> <robin --> animal>>", 0f, 1f,0f, 1f); //matches any truth value

        n.mustBelieve(time, "<<robin --> bird> ==> <robin --> animal>>", 0f, 0f, 0.81f, 0.81f)
                .en("It is unlikely that if a robin is a type of bird then robin is a type of animal.");

        n.run();

    }

    /** 5.19 */
    @Test public void compoundDecompositionOnePremise() throws InvalidInputException {
        /*

        'Robin can fly and swim.
        $0.90;0.90$ (&&,<robin --> swimmer>,<robin --> [flying]>). %0.9%
        1
        'Robin can swim.
        ''outputMustContain('<robin --> swimmer>. %0.90;0.73%')
        5
        ''//+2 from original
        'Robin can fly.
        ''outputMustContain('<robin --> [flying]>. %0.90;0.73%')

        */

        /*

        1.6.4 Output:
       IN (&&,<robin --> [flying]>,<robin --> swimmer>). %0.90;0.90% {0 : 0 : }
       (&&,<robin --> [flying]>,<robin --> swimmer>). %0.90;0.90% {0 : 0 : }

       <robin --> [flying]>. %1.00;0.73% {1 : 0(&&,<robin --> [flying]>,<robin --> swimmer>)}
       <robin --> swimmer>. %1.00;0.73% {2 : 0(&&,<robin --> [flying]>,<robin --> swimmer>)}



         */

        long time = 1500;

        /*
        Global.DEBUG = true;
        Global.DEBUG_TRACE_EVENTS = true;
        Global.DEBUG_DERIVATION_STACKTRACES = true;
        Global.DEBUG_TASK_HISTORY = true;
        TextOutput.out(nar);
        new DerivationOutput(nar);
        */

        n.mustBelieve(time, "<robin --> swimmer>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can swim.");
        n.mustBelieve(time, "<robin --> [flying]>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can fly.")
                .en("robin is one of the flying.");

        n.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)", Eternal, 0.9f, 0.9f)
                .en("robin can fly and swim.")
                .en("robin is one of the flying and is a swimmer.");

        n.run();

    }

    private class DerivationOutput extends NARReaction {

        public DerivationOutput(NAR nar) {
            super(nar, Events.TaskDerive.class);
        }

        @Override public void event(Class event, Object[] args) {
            Task t = (Task)args[0];
            System.out.println("Derived: " + t + " "  + t.getLog());
        }

    }
}
