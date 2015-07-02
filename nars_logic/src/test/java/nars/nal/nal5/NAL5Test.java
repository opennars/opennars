package nars.nal.nal5;

import nars.Events;
import nars.NAR;
import nars.NARSeed;
import nars.event.NARReaction;
import nars.nal.JavaNALTest;
import nars.nar.Curve;
import nars.nar.Default;
import nars.nar.DefaultMicro;
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
                { new DefaultMicro().level(5) },
                //{ new Neuromorphic(4) },
                //{ new DefaultBuffered().setInternalExperience(null) },
                {new Curve().setInternalExperience(null)}

        });
    }

    @Test public void deriveFromConjunctionComponents() {
        nar.believe("(&&,<a --> b>,<b-->a>)", Eternal, 1.0f, 0.9f);
        nar.mustBelieve(70, "<a <-> b>", 1.0f, 0.66f);
        nar.run();
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

        nar.believe("<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>", Eternal, 0.0f, 0.9f)
                .en("If robin is a type of bird then robin is not a type of flying animal.");
        /*n.believe("(--,<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>)", Eternal, 1.0f, 0.9f)
                .en("If robin is a type of bird then robin is not a type of flying animal.");*/


        nar.believe("<<robin --> bird> ==> <robin --> [flying]>>", Eternal, 1f, 0.9f )
                .en("If robin is a type of bird then robin can fly.");

        nar.mustBelieve(time, "<<robin --> bird> ==> <robin --> animal>>", 0f, 1f,0f, 1f); //matches any truth value

        nar.mustBelieve(time, "<<robin --> bird> ==> <robin --> animal>>", 0f, 0f, 0.81f, 0.81f)
                .en("It is unlikely that if a robin is a type of bird then robin is a type of animal.");

        nar.run();

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

        long time = 15;

        /*
        Global.DEBUG = true;
        Global.DEBUG_TRACE_EVENTS = true;
        Global.DEBUG_DERIVATION_STACKTRACES = true;
        Global.DEBUG_TASK_HISTORY = true;
        TextOutput.out(nar);
        new DerivationOutput(nar);
        */

        nar.mustBelieve(time, "<robin --> swimmer>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can swim.");
        nar.mustBelieve(time, "<robin --> [flying]>", 0.90f, 0.90f, 0.73f, 0.73f)
                .en("robin can fly.")
                .en("robin is one of the flying.");

        nar.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)", Eternal, 0.9f, 0.9f)
                .en("robin can fly and swim.")
                .en("robin is one of the flying and is a swimmer.");

        nar.run();

    }

    private class DerivationOutput extends NARReaction {

        public DerivationOutput(NAR nar) {
            super(nar, Events.TaskDerive.class);
        }

        @Override public void event(Class event, Object[] args) {
            Task t = (Task)args[0];
            System.out.println("Derived: " + t + " " + t.getSentence() + " "  + t.getHistory());
        }

    }
}
