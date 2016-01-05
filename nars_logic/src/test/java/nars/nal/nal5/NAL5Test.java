package nars.nal.nal5;

import nars.NAR;
import nars.nal.AbstractNALTester;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL5Test extends AbstractNALTester {

    public NAL5Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(5, true, true);
    }

    final int cycles = 1055;

    @Test public void revision(){
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.00f,0.60f); //.en("If robin can fly then robin may not a type of bird.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> bird>>",0.86f,0.91f); //.en("If robin can fly then robin is a type of bird.");
        
    }

    @Test
    public void deduction(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin can fly then robin is a type of animal.");
         
    }

    @Test
    public void exemplification(){
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> animal> ==> <robin --> [flying]>>.",1.00f,0.45f); //.en("I guess if robin is a type of animal then robin can fly.");

    }


    @Test
    public void induction(){

        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",1.00f,0.39f); //.en("I guess if robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> animal> ==> <robin --> [flying]>>",0.80f,0.45f); //.en("I guess if robin is a type of animal then robin can fly.");

    }


    @Test
    public void abduction(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.8f,0.9f); //.en("If robin can fly then robin is probably a type of animal.");
        tester.mustBelieve(cycles*3,"<<robin --> bird> ==> <robin --> [flying]>>",1.00f,0.39f); //.en("I guess if robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles*3,"<<robin --> [flying]> ==> <robin --> bird>>",0.80f,0.45f); //.en("I guess if robin can fly then robin is a type of bird.");

    }


    @Test
    public void detachment(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin can fly.");
        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
        tester.mustBelieve(cycles,"<robin --> animal>",1.00f,0.81f); //.en("Robin is a type of animal.");

    }


    @Test
    public void detachment2(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.70f,0.90f); //.en("Usually if robin is a type of bird then robin is a type of animal.");
        tester.believe("<robin --> animal>"); //.en("Robin is a type of animal.");
        tester.mustBelieve(cycles,"<robin --> bird>",1.00f,0.36f); //.en("I guess robin is a type of bird.");

    }


    @Test
    public void comparison(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.8f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> animal> <=> <robin --> [flying]>>",0.80f,0.45f); //.en("I guess robin is a type of animal if and only if robin can fly.");

    }


    @Test
    public void comparison2(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.7f,0.9f); //.en("If robin is a type of bird then usually robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>"); //.en("If robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles,"<<robin --> bird> <=> <robin --> [flying]>>",0.70f,0.45f); //.en("I guess robin is a type of bird if and only if robin can fly.");

    }


    @Test
    public void analogy(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f); //.en("Usually, robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles,"<<robin --> [flying]> ==> <robin --> animal>>",0.80f,0.65f); //.en("If robin can fly then probably robin is a type of animal.");
          
    }


    @Test
    public void analogy2(){
        TestNAR tester = test();
        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.80f,0.9f); //.en("Usually, robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",0.80f,0.65f); //.en("I guess usually robin can fly.");

    }


    @Test
    public void resemblance(){
        TestNAR tester = test();
        tester.believe("<<robin --> animal> <=> <robin --> bird>>"); //.en("Robin is a type of animal if and only if robin is a type of bird.");
        tester.believe("<<robin --> bird> <=> <robin --> [flying]>>",0.9f,0.9f); //.en("Robin is a type of bird if and only if robin can fly.");
        tester.mustBelieve(cycles," <<robin --> animal> <=> <robin --> [flying]>>",0.90f,0.81f); //.en("Robin is a type of animal if and only if robin can fly.");

    }


    @Test
    public void conversions_between_Implication_and_Equivalence(){
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>",0.9f,0.9f); //.en("If robin can fly then robin is a type of bird.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> <=> <robin --> [flying]>>",0.81f,0.81f); //.en("Robin can fly if and only if robin is a type of bird.");

    }


    @Test
    public void compound_composition_two_premises(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>",0.9f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> ==> (&&,<robin --> [flying]>,<robin --> animal>)>",0.90f,0.81f); //.en("If robin is a type of bird then usually robin is a type of animal and can fly.");
        tester.mustBelieve(cycles," <<robin --> bird> ==> (||,<robin --> [flying]>,<robin --> animal>)>",1.00f,0.81f); //.en("If robin is a type of bird then robin is a type of animal or can fly.");

    }


    @Test
    public void compound_composition_two_premises2(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin is a type of animal.");
        tester.believe("<<robin --> [flying]> ==> <robin --> animal>>",0.9f,0.9f); //.en("If robin can fly then robin is a type of animal.");
        tester.mustBelieve(cycles," <(&&,<robin --> bird>, <robin --> [flying]>) ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin can fly and is a type of bird then robin is a type of animal.");
        tester.mustBelieve(cycles," <(||,<robin --> bird>, <robin --> [flying]>) ==> <robin --> animal>>",0.90f,0.81f); //.en("If robin can fly or is a type of bird then robin is a type of animal.");

    }


    @Test
    public void compound_decomposition_two_premises1(){
        TestNAR tester = test();
        tester.believe("<<robin --> bird> ==> (&&,<robin --> animal>,<robin --> [flying]>)>",0.0f,0.9f); //.en("If robin is a type of bird then robin is not a type of flying animal.");
        tester.believe("<<robin --> bird> ==> <robin --> [flying]>>"); //.en("If robin is a type of bird then robin can fly.");
        tester.mustBelieve(cycles*2," <<robin --> bird> ==> <robin --> animal>>",0.00f,0.81f); //.en("It is unlikely that if a robin is a type of bird then robin is a type of animal.");

    }


    @Test
    public void compound_decomposition_two_premises2(){
        TestNAR tester = test();
        tester.believe("(&&,<robin --> [flying]>,<robin --> swimmer>)",0.0f,0.9f); //.en("Robin cannot be both a flyer and a swimmer.");
        tester.believe("<robin --> [flying]>"); //.en("Robin can fly.");
        tester.mustBelieve(cycles,"<robin --> swimmer>",0.00f,0.81f); //.en("Robin cannot swim.");

    }


    @Test
    public void compound_decomposition_two_premises3(){
        TestNAR tester = test();
        tester.believe("(||,<robin --> [flying]>,<robin --> swimmer>)"); //.en("Robin can fly or swim.");
        tester.believe("<robin --> swimmer>",0.0f,0.9f); //.en("Robin cannot swim.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",1.00f,0.81f); //.en("Robin can fly.");

    }


    @Test
    public void compound_composition_one_premises(){
        TestNAR tester = test();
        tester.believe("<robin --> [flying]>"); //.en("Robin can fly.");
        tester.ask("(||,<robin --> [flying]>,<robin --> swimmer>)"); //.en("Can robin fly or swim?");
        tester.mustBelieve(cycles," (||,<robin --> swimmer>,<robin --> [flying]>)",1.00f,0.81f); //.en("Robin can fly or swim.");

    }


    @Test public void compound_decomposition_one_premises(){
        TestNAR tester = test();
        tester.believe("(&&,<robin --> swimmer>,<robin --> [flying]>)",0.9f,0.9f); //.en("Robin can fly and swim.");
        tester.mustBelieve(cycles*4,"<robin --> swimmer>",0.9f,0.73f); //.en("Robin can swim.");
        tester.mustBelieve(cycles*4,"<robin --> [flying]>",0.9f,0.73f); //.en("Robin can fly.");

    }
    @Test public void compound_decomposition_one_premises_2(){
        //TODO, mirroring: compound_decomposition_one_premises
//        TestNAR tester = test();
//        tester.believe("(||,<robin --> swimmer>,<robin --> [flying]>)",0.9f,0.9f); //.en("Robin can fly and swim.");
//        tester.mustBelieve(cycles*4,"<robin --> swimmer>", ..); //.en("Robin can swim.");
//        tester.mustBelieve(cycles*4,"<robin --> [flying]>", ..); //.en("Robin can fly.");
//        tester.run();
    }


    @Test
    public void negation(){

        TestNAR tester = test();
        tester.believe("(--,<robin --> [flying]>)",0.1f,0.9f); //.en("It is unlikely that robin cannot fly.");
        tester.mustBelieve(cycles,"<robin --> [flying]>",0.90f,0.90f); //.en("Robin can fly.");

    }


    @Test
    public void negation2(){
        TestNAR tester = test();
        tester.believe("<robin --> [flying]>",0.9f,0.9f); //.en("Robin can fly.");
        tester.ask("(--,<robin --> [flying]>)"); //.en("Can robin fly or not?");
        tester.mustBelieve(cycles,"(--,<robin --> [flying]>)",0.10f,0.90f); //.en("It is unlikely that robin cannot fly.");

    }


    @Test
    public void contraposition(){
        TestNAR tester = test();
        tester.believe("<(--,<robin --> bird>) ==> <robin --> [flying]>>", 0.1f, 0.9f); //.en("It is unlikely that if robin is not a type of bird then robin can fly.");
        tester.ask("<(--,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin cannot fly then is robin a type of bird ? ");
        tester.mustBelieve(cycles, " <(--,<robin --> [flying]>) ==> <robin --> bird>>", 0.00f, 0.45f); //.en("I guess it is unlikely that if robin cannot fly then robin is a type of bird.");

    }


    @Test
    public void conditional_deduction(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> bird>>"); //.en("If robin can fly and has wings then robin is a bird.");
        tester.believe("<robin --> [flying]>"); //.en("robin can fly.");
        tester.mustBelieve(cycles," <<robin --> [withWings]> ==> <robin --> bird>>",1.00f,0.81f); //.en("If robin has wings then robin is a bird");

    }


    @Test
    public void conditional_deduction2(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> bird>>"); //.en("If robin can fly, has wings, and chirps, then robin is a bird");
        tester.believe("<robin --> [flying]>"); //.en("robin can fly.");
        tester.mustBelieve(cycles," <(&&,<robin --> [chirping]>,<robin --> [withWings]>) ==> <robin --> bird>>",1.00f,0.81f); //.en("If robin has wings and chirps then robin is a bird.");

    }


    @Test
    public void conditional_deduction3(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> bird>,<robin --> [living]>) ==> <robin --> animal>>"); //.en("If robin is a bird and it's living, then robin is an animal");
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly, then robin is a bird");
        tester.mustBelieve(cycles," <(&&,<robin --> [flying]>,<robin --> [living]>) ==> <robin --> animal>>",1.00f,0.81f); //.en("If robin is living and it can fly, then robin is an animal.");

    }


    @Test
    public void conditional_abduction_viaMultiConditionalSyllogism(){
        //((&&,M,A_1..n) ==> C), ((&&,A_1..n) ==> C) |- M, (Truth:Abduction, Order:ForAllSame)
        TestNAR tester = test();
        tester.believe("<<robin --> [flying]> ==> <robin --> bird>>"); //.en("If robin can fly then robin is a bird.");
        tester.believe("<(&&,<robin --> swimmer>,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin both swims and flys then robin is a bird.");
        tester.mustBelieve(cycles*2,"<robin --> swimmer>",1.00f,0.45f); //.en("I guess robin swims.");

    }


    @Test
    public void conditional_abduction2_viaMultiConditionalSyllogism(){
        //((&&,M,A_1..n) ==> C), ((&&,A_1..n) ==> C) |- M, (Truth:Abduction, Order:ForAllSame)
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [withWings]>,<robin --> [chirping]>) ==> <robin --> bird>>"); //.en("If robin is has wings and chirps, then robin is a bird");
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>,<robin --> [chirping]>) ==> <robin --> bird>>"); //.en("If robin can fly, has wings, and chirps, then robin is a bird");
        tester.mustBelieve(cycles*2," <robin --> [flying]>",1.00f,0.45f); //.en("I guess that robin can fly.");

    }


    @Test
    public void conditional_abduction3(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [flying]>,<robin --> [withWings]>) ==> <robin --> [living]>>",0.9f,0.9f); //.en("If robin can fly and it has wings, then robin is living.");
        tester.believe("<(&&,<robin --> [flying]>,<robin --> bird>) ==> <robin --> [living]>>"); //.en("If robin can fly and robin is a bird then robin is living.");
        tester.mustBelieve(cycles,"<<robin --> bird> ==> <robin --> [withWings]>>",1.00f,0.42f); //.en("I guess if robin is a bird, then robin has wings.");
        tester.mustBelieve(cycles,"<<robin --> [withWings]> ==> <robin --> bird>>",0.90f,0.45f); //.en("I guess if robin has wings, then robin is a bird.");

    }

    @Test
    public void conditional_abduction3_semigeneric(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [f]>,<robin --> [w]>) ==> <robin --> [l]>>",0.9f,0.9f);
        tester.believe("<(&&,<robin --> [f]>,<robin --> b>) ==> <robin --> [l]>>");
        tester.mustBelieve(cycles*2,"<<robin --> b> ==> <robin --> [w]>>",1.00f,0.42f);
        tester.mustBelieve(cycles*2,"<<robin --> [w]> ==> <robin --> b>>",0.90f,0.45f);
    }
    @Test
    public void conditional_abduction3_semigeneric2(){
        TestNAR tester = test();
        tester.believe("<(&&,<ro --> [f]>,<ro --> [w]>) ==> <ro --> [l]>>",0.9f,0.9f);
        tester.believe("<(&&,<ro --> [f]>,<ro --> b>) ==> <ro --> [l]>>");
        tester.mustBelieve(cycles*2,"<<ro --> b> ==> <ro --> [w]>>",1.00f,0.42f);
        tester.mustBelieve(cycles*2,"<<ro --> [w]> ==> <ro --> b>>",0.90f,0.45f);
    }
    @Test
    public void conditional_abduction3_semigeneric3(){
        TestNAR tester = test();
        tester.believe("<(&&,<R --> [f]>,<R --> [w]>) ==> <R --> [l]>>",0.9f,0.9f);
        tester.believe("<(&&,<R --> [f]>,<R --> b>) ==> <R --> [l]>>");
        tester.mustBelieve(cycles,"<<R --> b> ==> <R --> [w]>>",1.00f,0.42f);
        tester.mustBelieve(cycles,"<<R --> [w]> ==> <R --> b>>",0.90f,0.45f);
    }
    @Test
    public void conditional_abduction3_semigeneric4(){
        TestNAR tester = test();
        tester.believe("<(&&,<s --> [f]>,<s --> [w]>) ==> <s --> [l]>>",0.9f,0.9f);
        tester.believe("<(&&,<s --> [f]>,<s --> b>) ==> <s --> [l]>>");
        tester.mustBelieve(cycles,"<<s --> b> ==> <s --> [w]>>",1.00f,0.42f);
        tester.mustBelieve(cycles,"<<s --> [w]> ==> <s --> b>>",0.90f,0.45f);
    }
    @Test
    public void conditional_abduction3_generic(){
        TestNAR tester = test();
        tester.believe("<(&&,<r --> [f]>,<r --> [w]>) ==> <r --> [l]>>",0.9f,0.9f);
        tester.believe("<(&&,<r --> [f]>,<r --> b>) ==> <r --> [l]>>");
        tester.mustBelieve(cycles*8,"<<r --> b> ==> <r --> [w]>>",1.00f,0.42f);
        tester.mustBelieve(cycles*8,"<<r --> [w]> ==> <r --> b>>",0.90f,0.45f);
    }

    @Test
    public void conditional_induction(){
        TestNAR tester = test();
        tester.believe("<(&&,<robin --> [chirping]>,<robin --> [flying]>) ==> <robin --> bird>>"); //.en("If robin can fly and robin chirps, then robin is a bird");
        tester.believe("<<robin --> [flying]> ==> <robin --> [withBeak]>>",0.9f,0.9f); //.en("If robin can fly then usually robin has a beak.");
        tester.mustBelieve(cycles,"<(&&,<robin --> [chirping]>,<robin --> [withBeak]>) ==> <robin --> bird>>",1.00f,0.42f); //.en("I guess that if robin chirps and robin has a beak, then robin is a bird.");

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




    //NO	((<(--,p1) ==> p2>, p2), (<(--,p2) ==> p1>, (<Contraposition --> Truth>, <AllowBackward --> Derive>)))

}
