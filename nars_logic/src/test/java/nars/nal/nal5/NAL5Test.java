package nars.nal.nal5;

import nars.NAR;
import nars.meter.RuleTest;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL5Test extends AbstractNALTest {

    public NAL5Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Iterable configurations() {
        return AbstractNALTest.nars(5, false);
    }

    final int cycles = 650;
    @Test
    public void revision() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.00f,0.60f); //.en("If robin can fly then robin may not a type of bird.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> bird>>",0.86f,0.91f); //.en("If robin can fly then robin is a type of bird.");
        tester.run();
    }

    @Test
    public void deduction() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin can fly then robin is a type of animal.");
        tester.run();
    }

    @Test
    public void exemplification() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> animal> ==> <robin --> [flying]>>.",1.00f,0.45f); //.en("I guess if robin is a type of animal then robin can fly.");
        tester.run();
    }


    @Test
    public void induction() throws InvalidInputException {

        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.39f); //.en("I guess if robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> animal> ==> <robin --> [flying]>>",0.80f,0.45f); //.en("I guess if robin is a type of animal then robin can fly.");
        tester.run();
    }


    @Test
    public void abduction() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.8f,0.9f); //.en("If robin can fly then robin is probably a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> bird> ==> <robin --> [flying]>>",1.00f,0.39f); //.en("I guess if robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> bird>>",0.80f,0.45f); //.en("I guess if robin can fly then robin is a type of bird.");
        tester.run();
    }


    @Test
    public void detachment() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin can fly.");
        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
        tester.mustBelieve(cycles,"<robin --> animal>",1.00f,0.81f); //.en("Robin is a type of animal.");
        tester.run();
    }


    @Test
    public void detachment2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.70f,0.90f); //.en("Usually if robin is a type of bird then robin is a type of animal.");
        tester.believe("<robin --> animal>"); //.en("Robin is a type of animal.");
        tester.mustBelieve(cycles,"<robin --> bird>",1.00f,0.36f); //.en("I guess robin is a type of bird.");
        tester.run();
    }


    @Test
    public void comparison() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> animal> <=> <robin --> [flying]>>",0.80f,0.45f); //.en("I guess robin is a type of animal if and only if robin can fly.");
        tester.run();
    }


    @Test
    public void comparison2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.7f,0.9f); //.en("If robin is a type of bird then usually robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>"); //.en("If robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> bird> <=> <robin --> [flying]>>",0.70f,0.45f); //.en("I guess robin is a type of bird if and only if robin can fly.");
        tester.run();
    }


    @Test
    public void analogy() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f); //.en("Usually, robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",0.80f,0.65f); //.en("If robin can fly then probably robin is a type of animal.");
        tester.run();
    }


    @Test
    public void analogy2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f); //.en("Usually, robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",0.80f,0.65f); //.en("I guess usually robin can fly.");
        tester.run();
    }


    @Test
    public void resemblance() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> animal> <=> <robin --> bird>>"); //.en("Robin is a type of animal if and only if robin is a type of bird.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.9f,0.9f); //.en("Robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles," <<robin --> animal> <=> <robin --> [flying]>>",0.90f,0.81f); //.en("Robin is a type of animal if and only if robin can fly.");
        tester.run();
    }


    @Test
    public void conversions_between_Implication_and_Equivalence() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.9f,0.9f); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> <=> <robin --> [flying]>>",0.81f,0.81f); //.en("Robin can fly if and only if robin is a type of bird.");
        tester.run();
    }


    @Test
    public void compound_composition_two_premises() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> ==> (&&,<robin --> [flying]>,<robin --> animal>)>",0.90f,0.81f); //.en("If robin is a type of bird then usually robin is a type of animal and can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> ==> (||,<robin --> [flying]>,<robin --> animal>)>",1.00f,0.81f); //.en("If robin is a type of bird then robin is a type of animal or can fly.");
        tester.run();
    }


    @Test
    public void compound_composition_two_premises2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.9f,0.9f); //.en("If robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles," <(&&,<robin --> bird>, <robin --> [flying]>) ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin can fly and is a type of bird then robin is a type of animal.");
        tester.mustBelieve(cycles," <(||,<robin --> bird>, <robin --> [flying]>) ==> <robin --> animal>>",0.90f,0.81f); //.en("If robin can fly or is a type of bird then robin is a type of animal.");
        tester.run();
    }


    @Test
    public void compound_decomposition_two_premises1() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>",0.0f,0.9f); //.en("If robin is a type of bird then robin is not a type of flying animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>"); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> ==> <robin --> animal>>",0.00f,0.81f); //.en("It is unlikely that if a robin is a type of bird then robin is a type of animal.");
        tester.run();
    }


    @Test
    public void compound_decomposition_two_premises2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("(&&,<robin --> [flying]>,<robin --> swimmer>)",0.0f,0.9f); //.en("Robin cannot be both a flyer and a swimmer.");
        tester.believe("<robin --> [flying]>"); //.en("Robin can fly.");
        tester.mustBelieve(cycles,"<robin --> swimmer>",0.00f,0.81f); //.en("Robin cannot swim.");
        tester.run();
    }


    @Test
    public void compound_decomposition_two_premises3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("(||,<robin --> [flying]>,<robin --> swimmer>)"); //.en("Robin can fly or swim.");
        tester.believe("<robin --> swimmer>",0.0f,0.9f); //.en("Robin cannot swim.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",1.00f,0.81f); //.en("Robin can fly.");
        tester.run();
    }


    @Test
    public void compound_composition_one_premises() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> [flying]>"); //.en("Robin can fly.");
        tester.ask("(||,<robin --> [flying]>,<robin --> swimmer>)"); //.en("Can robin fly or swim?");
        tester.mustBelieve(cycles," (||,<robin --> swimmer>,<robin --> [flying]>)",1.00f,0.81f); //.en("Robin can fly or swim.");
        tester.run();
    }


    @Test
    public void compound_decomposition_one_premises() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)",0.9f,0.9f); //.en("Robin can fly and swim.");
        tester.mustBelieve(cycles,"<robin --> swimmer>",0.9f,0.73f); //.en("Robin can swim.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",0.9f,0.73f); //.en("Robin can fly.");
        tester.run();
    }


    @Test
    public void negation() throws InvalidInputException {

        TestNAR tester = test();
        tester.believe("(--,<robin --> [flying]>)",0.1f,0.9f); //.en("It is unlikely that robin cannot fly.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",0.90f,0.90f); //.en("Robin can fly.");
        tester.run();
    }


    @Test
    public void negation2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<robin --> [flying]>",0.9f,0.9f); //.en("Robin can fly.");
        tester.ask("(--,<robin --> [flying]>)"); //.en("Can robin fly or not?");
        tester.mustBelieve(cycles,"(--,<robin --> [flying]>)",0.10f,0.90f); //.en("It is unlikely that robin cannot fly.");
        tester.run();
    }


    @Test
    public void contraposition() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(--,<robin --> bird>) ==> <robin --> [flying]>>", 0.1f, 0.9f); //.en("It is unlikely that if robin is not a type of bird then robin can fly.");
        tester.ask("<(--,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin cannot fly then is robin a type of bird ? ");
        tester.mustBelieve(cycles, " <(--,<robin --> [flying]>) ==> <robin --> bird>>", 0.00f, 0.45f); //.en("I guess it is unlikely that if robin cannot fly then robin is a type of bird.");
        tester.run();
    }


    @Test
    public void conditional_deduction() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> bird>>"); //.en("If robin can fly and has wings then robin is a bird.");
        tester.believe("<robin --> [flying]>"); //.en("robin can fly.");
        tester.mustBelieve(cycles," <<robin --> [withWings]> ==> <robin --> bird>>",1.00f,0.81f); //.en("If robin has wings then robin is a bird");
        tester.run();
    }


    @Test
    public void conditional_deduction2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> bird>>"); //.en("If robin can fly, has wings, and chirps, then robin is a bird");
        tester.believe("<robin --> [flying]>"); //.en("robin can fly.");
        tester.mustBelieve(cycles," <(&&,<robin --> [chirping]>,<robin --> [withWings]>) ==> <robin --> bird>>",1.00f,0.81f); //.en("If robin has wings and chirps then robin is a bird.");
        tester.run();
    }


    @Test
    public void conditional_deduction3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> bird>,<robin --> [living]>) ==> <robin --> animal>>"); //.en("If robin is a bird and it's living, then robin is an animal");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly, then robin is a bird");
        tester.mustBelieve(cycles," <(&&,<robin --> [flying]>,<robin --> [living]>) ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin is living and it can fly, then robin is an animal.");
        tester.run();
    }


    @Test
    public void conditional_abduction() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a bird.");
        tester.believe("<(&&,<robin --> swimmer>,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin both swims and flys then robin is a bird.");
        tester.mustBelieve(cycles," <robin --> swimmer>",1.00f,0.45f); //.en("I guess robin swims.");
        tester.run();
    }


    @Test
    public void conditional_abduction2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [withWings]>,<robin --> [chirping]>) ==> <robin --> bird>>"); //.en("If robin is has wings and chirps, then robin is a bird");
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>,<robin --> [chirping]>) ==> <robin --> bird>>"); //.en("If robin can fly, has wings, and chirps, then robin is a bird");
        tester.mustBelieve(cycles," <robin --> [flying]>",1.00f,0.45f); //.en("I guess that robin can fly.");
        tester.run();
    }


    @Test
    public void conditional_abduction3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> [living]>>",0.9f,0.9f); //.en("If robin can fly and it has wings, then robin is living.");
        tester.believe("<(&&,<robin --> [flying]>,<robin --> bird>) ==> <robin --> [living]>>."); //.en("If robin can fly and robin is a bird then robin is living.");
        tester.mustBelieve(cycles,"<<robin --> bird> ==> <robin --> [withWings]>>",1.00f,0.42f); //.en("I guess if robin is a bird, then robin has wings.");
        tester.mustBelieve(cycles,"<<robin --> [withWings]> ==> <robin --> bird>>",0.90f,0.45f); //.en("I guess if robin has wings, then robin is a bird.");
        tester.run();
    }


    @Test
    public void conditional_induction() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin can fly and robin chirps, then robin is a bird");
        tester.believe("<<robin --> [flying]> ==> <robin --> [withBeak]>>",0.9f,0.9f); //.en("If robin can fly then usually robin has a beak.");
        tester.mustBelieve(cycles,"<(&&,<robin --> [chirping]>,<robin --> [withBeak]>) ==> <robin --> bird>>",1.00f,0.42f); //.en("I guess that if robin chirps and robin has a beak, then robin is a bird.");
        tester.run();
    }

    /* will be moved to NAL multistep test file!!
    //this is a multistep example, I will add a special test file for those with Default configuration
    //it's not the right place here but the example is relevant
    @Test public void deriveFromConjunctionComponents() { //this one will work after truthfunctions which allow evidental base overlap are allowed
        TestNAR tester = test();
        tester.believe("(&&,<a --> b>,<b-->a>)", Eternal, 1.0f, 0.9f);

        //TODO find the actual value for these intermediate steps, i think it is 81p
        tester.mustBelieve(70, "<a --> b>", 1f, 0.81f);
        tester.mustBelieve(70, "<b --> a>", 1f, 0.81f);

        tester.mustBelieve(70, "<a <-> b>", 1.0f, 0.66f);
        tester.run();
    }*/


//    @Test
//    public void missingEdgeCase1() {
//        //((<p1 ==> p2>, <(&&, p1, p3) ==> p2>), (<p3 ==> p2>, (<DecomposeNegativePositivePositive --> Truth>, <ForAllSame --> Order>)))
//        //((<p1 ==> p2>, <(&&, p1, p3) ==> p2>), (<p3 ==> p2>, (<DecomposeNegativePositivePositive --> Truth>, <ForAllSame --> Order>)))
//        new RuleTest(
//                "<p1 ==> p2>. %0.05;0.9%", "<(&&, p1, p3) ==> p2>.",
//                "<p3 ==> p2>.",
//                0.95f, 0.95f, 0.77f, 0.77f)
//                .run();
//    }

    @Test
    public void posNegQuestion() {
        //((p1, (--,p1), task("?")), (p1, (<BeliefNegation --> Truth>, <Judgment --> Punctuation>)))
        //  ((a:b, (--,a:b), task("?")), (a:b, (<BeliefNegation --> Truth>, <Judgment --> Punctuation>)))
        new RuleTest(
                "a:b?", "(--,a:b).",
                "a:b.",
                0,0,0.9f,0.9f)
                .run();
    }


    //NO	((<(--,p1) ==> p2>, p2), (<(--,p2) ==> p1>, (<Contraposition --> Truth>, <AllowBackward --> Derive>)))

}
