package nars.nal.nal1;

import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import nars.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class NAL1Test extends AbstractNALTest {


    public NAL1Test(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTest.nars(1, false);
    }
//
//                new Supplier[]{
//                //{new Default()},
////                {new Default().setInternalExperience(null)},
//
//                //{new NewDefault()},
//                { () -> new Default().nal(1)},
//                { () -> new Default().nal(2)},
//                { () -> new Default() },
//
////                {new Default().level(2)}, //why does this need level 2 for some tests?
////                {new DefaultMicro().level(2) },
////                {new Classic()},
//
//                { () -> new DefaultAlann(48)},
//
//                //{new Solid(1, 48, 1, 2, 1, 3).level(1)},
//                //{new Solid(1, 64, 1, 2, 1, 3).level(2)},
//        });
//}

//
//    @Before
//    public void setup() {
//
//        //tester.setTemporalTolerance(50 /* cycles */);
//    }


    @Test
    public void revision() throws InvalidInputException {

        final String belief = "<bird --> swimmer>";

        test()
                .believe(belief)                 //.en("bird is a type of swimmer.");
                .believe(belief, 0.10f, 0.60f)                 //.en("bird is probably not a type of swimmer."); //.en("bird is very likely to be a type of swimmer.");*/
                .mustBelieve(3, belief, 0.87f, 0.91f) // .en("bird is very likely to be a type of swimmer.");
                .run();
    }


    @Test
    public void deduction() throws InvalidInputException {

        test().believe("<bird --> animal>")
                /*.en("bird is a type of animal.")
                .es("bird es un tipo de animal.")
                .de("bird ist eine art des animal.");*/
                .believe("<robin --> bird>")
                        //.en("robin is a type of bird.");
                .mustBelieve(123, "<robin --> animal>", 0.81f)
                        //.en("robin is a type of animal.");
                .run();
    }

    @Test
    public void abduction() throws InvalidInputException {

        int time = 128;
        test().mustBelieve(time, "<sport --> chess>", 1.0f, 0.42f)
              /*  .en("I guess sport is a type of chess.")
                .en("sport is possibly a type of chess.")
                .es("es posible que sport es un tipo de chess.");*/
                .mustBelieve(time, "<chess --> sport>", 0.90f, 0.45f)
                        //.en("I guess chess is a type of sport");
                .believe("<sport --> competition>")
                        //.en("sport is a type of competition.");
                .believe("<chess --> competition>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                        //.en("chess is a type of competition.");
                .run();
    }

    @Test
    public void induction() throws InvalidInputException {
        test().believe("<swan --> swimmer>", 0.90f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                //.en("Swan is a type of swimmer.");
                .believe("<swan --> bird>")
                        //.en("Swan is a type of bird.");

                .mustBelieve(123, "<bird --> swimmer>", 0.90f, 0.45f)
                        //.en("I guess bird is a type of swimmer.");
                .mustBelieve(123, "<swimmer --> bird>", 1f, 0.42f)
                        //.en("I guess swimmer is a type of bird.");
                .run();
    }

    @Test
    public void exemplification() throws InvalidInputException {

        test()
            //.debug()
            .believe("<robin --> bird>")
            .believe("<bird --> animal>")
            .mustOutput(125, "<animal --> robin>. %1.00;0.4475%")
                    //.en("I guess animal is a type of robin.");
            .run();
    }


    @Test
    public void conversion() throws InvalidInputException {

        long time = /*tester.nal() <= 2 ? 15 :*/ 32;
        TestNAR test = test();
        test.believe("<bird --> swimmer>")
            .ask("<swimmer --> bird>") //.en("Is swimmer a type of bird?");
            .mustOutput(time, "<swimmer --> bird>. %1.00;0.47%")
            .run();
    }


    @Test
    public void whQuestionUnifyQueryVar() throws InvalidInputException {
        testQuestionAnswer(16, "<bird --> swimmer>", "<?x --> swimmer>", "<bird --> swimmer>");
    }

    @Test
    public void yesNoQuestion() throws InvalidInputException {
        testQuestionAnswer(16, "<bird --> swimmer>", "<bird --> swimmer>", "<bird --> swimmer>");
    }

    /** question to answer matching */
    public void testQuestionAnswer(int cycles, String belief, String question, String expectedSolution) {
        AtomicBoolean solved = new AtomicBoolean(false);

        NAR nar = nar();

        //TODO abstract the %1.0;0.8% hardcoded here

        Task expectedTask = nar.task(expectedSolution + ". %1.00;0.80%");

        //nar.stdout();

        nar.believe(belief, 1.0f, 0.8f)
           .answer(question, a -> { //.en("What is a type of swimmer?")

                //test for a few task conditions, everything except for evidence
                if (a.getPunctuation() == expectedTask.getPunctuation())
                    if (a.getTerm().equals(expectedTask.getTerm())) {
                        if (Objects.equals(a.getTruth(), expectedTask.getTruth()))
                            solved.set(true);
                }

            }).frame(cycles);

        assertTrue(solved.get());
    }


    @Test
    public void backwardInference() throws InvalidInputException {
        long time = /*nar instanceof Solid ? 15 :*/ 32;


        test().mustOutput(time, "<?1 --> bird>?") //.en("What is a type of bird?");
                .mustOutput(time, "<bird --> ?1>?") //.en("What is the type of bird?");
                .believe("<bird --> swimmer>", 1.0f, 0.8f)
                .ask("<?1 --> swimmer>")
                .run();
    }




}
