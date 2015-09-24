package nars.nal.nal1;

import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.meter.experiment.DeductiveChainTest;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL1Test extends AbstractNALTest {


    public NAL1Test(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTest.core6;
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

        int time = 64;
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
        test.debug()
                .believe("<bird --> swimmer>")
            .ask("<swimmer --> bird>") //.en("Is swimmer a type of bird?");
            .mustOutput(time, "<swimmer --> bird>. %1.00;0.47%")
            .run();
    }

    @Test
    public void yesnoQuestion() throws InvalidInputException {
        test().believe("<bird --> swimmer>")
                .ask("<bird --> swimmer>")
                .mustOutput(25, "<bird --> swimmer>. %1.00;0.90%")
                .run();
    }


    @Test
    public void whQuestion() throws InvalidInputException {
//        System.out.println("\n\n\n START ------------");
//        TextOutput.out(n);
//        NARTrace.out(n);

        test().believe("<bird --> swimmer>", 1.0f, 0.8f)
                .ask("<?x --> swimmer>") //.en("What is a type of swimmer?")
                .mustOutput(150, "<bird --> swimmer>. %1.00;0.80%")
                .run();
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


    @Test
    public void multistep2() {
        DeductiveChainTest dc = new DeductiveChainTest(nar(), 2, 600);
        dc.nar.stdout();
        dc.run();

    }

    @Test
    public void multistep3() {
        new DeductiveChainTest(nar(), 3, 900).run();
    }


    @Test
    public void multistep() throws InvalidInputException {
        long time = 150;

        //TextOutput.out(n);

        TestNAR test = test();

        //we know also 73% is the theoretical maximum it can reach
        if (test.nar.nal() <= 2)
            test.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);
        else
            //originally checked for 0.25% exact confidence
            test.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        test.believe("<a --> b>", 1.0f, 0.9f);
        test.believe("<b --> c>", 1.0f, 0.9f);
        test.believe("<c --> d>", 1.0f, 0.9f);


        test.run();
    }


}
