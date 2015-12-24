package nars.nal.nal4;

import nars.NAR;
import nars.Narsese;
import nars.nal.AbstractNALTester;
import nars.util.meter.RuleTest;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL4Test extends AbstractNALTester {


    public static final int CYCLES = 150;

    public NAL4Test(Supplier<NAR> b) { super(b);  }

    @Parameterized.Parameters(name= "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(4, false);
    }

    @Test
    public void structural_transformation() throws Narsese.NarseseException {
        TestNAR t = test();
        t.believe("<(acid,base) --> reaction>",1.0f,0.9f); //en("An acid and a base can have a reaction.");
        t.mustBelieve(CYCLES, "<acid --> (/,reaction,_,base)>", 1.0f, 0.9f); //en("Acid can react with base.");
        t.mustBelieve(CYCLES, "<base --> (/,reaction,acid,_)>", 1.0f, 0.9f); //en("A base is something that has a reaction with an acid.");

    }

    @Test
     public void structural_transformation2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<acid --> (/,reaction,_,base)>",1.0f,0.9f); //en("Acid can react with base.");
        tester.mustBelieve(CYCLES, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("Acid can react with base.");

    }

    @Test
    public void structural_transformation3() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<base --> (/,reaction,acid,_)>",1.0f,0.9f); //en("A base is something that has a reaction with an acid.");
        tester.mustBelieve(CYCLES, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("Acid can react with base.");

    }

    @Test
    public void structural_transformation4() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<neutralization --> (acid,base)>",1.0f,0.9f); //en("Neutralization is a relation between an acid and a base. ");
        tester.mustBelieve(CYCLES, "<(\\,neutralization,_,base) --> acid>.", 1.0f, 0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> base>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");
    }

    @Test
    public void structural_transformation4_extended() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<neutralization --> (substance,acid,base)>",1.0f,0.9f);
        tester.mustBelieve(CYCLES, "<(\\,neutralization,_,acid,base) --> substance>.", 1.0f, 0.9f);
        tester.mustBelieve(CYCLES, "<(\\,neutralization,substance,_,base) --> acid>.", 1.0f, 0.9f);
        tester.mustBelieve(CYCLES, "<(\\,neutralization,substance,acid,_) --> base>", 1.0f, 0.9f);
    }


    @Test
    public void structural_transformation5() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,_,base) --> acid>",1.0f,0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(CYCLES, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Neutralization is a relation between an acid and a base.");
    }

    @Test
    public void structural_transformation5_extended() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,substance,_,base) --> acid>",1.0f,0.9f);
        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base)>", 1.0f, 0.9f);
    }
    @Test
    public void structural_transformation5_extended2a() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,substance,_,base,reaction) --> acid>",1.0f,0.9f);
        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
    }
    @Test
    public void structural_transformation5_extended2b() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,substance,acid,_,reaction) --> base>",1.0f,0.9f);
        tester.mustBelieve(CYCLES, "<neutralization --> (substance,acid,base,reaction)>", 1.0f, 0.9f);
    }

    @Test
    public void structural_transformation6() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,acid,_) --> base>",1.0f,0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(CYCLES, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");

    }

    @Test
    public void composition_on_both_sides_of_a_statement() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
        tester.ask("<(bird,plant) --> ?x>"); //en("What is the relation between a bird and a plant?");
        tester.mustBelieve(CYCLES, "<(bird,plant) --> (animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");

    }

    @Test
    public void composition_on_both_sides_of_a_statement_2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
        tester.ask("<(bird,plant) --> (animal,plant)>");
        tester.mustBelieve(CYCLES*24, "<(bird,plant) --> (animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");

    }

//    @Test public void composition_on_both_sides_of_a_statement_long0()  {
//        composition_on_both_sides_of_a_statement_long(0);
//    }
//
//    /** stresses product/image matching rules with a long product */
//    @Test public void composition_on_both_sides_of_a_statement_long1()  {
//        composition_on_both_sides_of_a_statement_long(1);
//    }
//    /** stresses product/image matching rules with a long product */
//    @Test public void composition_on_both_sides_of_a_statement_long2()  {
//        composition_on_both_sides_of_a_statement_long(2);
//    }
//    /** stresses product/image matching rules with a long product */
//    @Test public void composition_on_both_sides_of_a_statement_long3()  {
//        composition_on_both_sides_of_a_statement_long(3);
//    }

    /** stresses product/image matching rules with a long product */
    public void composition_on_both_sides_of_a_statement_long(int n)  {
        String additional = "";
        for (int i = 0; i < n; i++)
            additional += ("x" + i) + ',';

        TestNAR tester = test();
        tester.nar.trace();
        tester.believe("<neutralization --> reaction>",1.0f,0.9f);
        tester.ask("<(\\,neutralization," + additional + " acid,_) --> ?x>");
        tester.mustBelieve(CYCLES*10, "<(\\,neutralization," + additional + " acid,_) --> (\\,reaction," + additional + " acid,_)>", 1.0f, 0.81f);

    }

    @Test
    public void composition_on_both_sides_of_a_statement2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<neutralization --> reaction>",1.0f,0.9f); //en("Neutralization is a type of reaction.");
        tester.ask("<(\\,neutralization,acid,_) --> ?x>"); //en("What can be neutralized by acid?");
        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>", 1.0f, 0.81f); //en("What can be neutralized by acid can react with acid.");

    }

    @Test
    public void composition_on_both_sides_of_a_statement2_2() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<neutralization --> reaction>",1.0f,0.9f); //en("Neutralization is a type of reaction.");
        tester.ask("<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>");
        tester.mustBelieve(CYCLES, "<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>", 1.0f, 0.81f); //en("What can be neutralized by acid can react with acid.");

    }

    @Test
    public void composition_on_both_sides_of_a_statement3() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<soda --> base>",1.0f,0.9f); //en("Soda is a type of base.");
        tester.ask("<(/,neutralization,_,base) --> ?x>"); //en("What is something that can neutralize a base?");
        tester.mustBelieve(CYCLES, "<(/,neutralization,_,base) --> (/,neutralization,_,soda)>", 1.0f, 0.81f); //en("What can neutraliz base can react with base.");

    }

    /* NAL6
    @Test public void recursionSmall2() throws InvalidInputException {
        long time;
        final float finalConf = 0.73f;

        if (seed instanceof Solid) {
            time = 50;
        }
        else {
            if (n.nal() <= 6) {
                time = 400; //less time for the nal6 config
            } else {
                time = 800;
            }
        }

        n.believe(" <0 --> n>", 1.0f, 0.9f);
        n.believe("<<$1 --> n> ==> <(/,next,$1,_) --> n>>", 1.0f, 0.9f);
        n.ask("<(/,next,(/,next,0,_),_) --> n>");
        n.mustBelieve(time, "<(/,next,0,_) --> n>", 1.0f, 1.0f, 0.81f, 1.0f);
        n.mustBelieve(time, "<(/,next,(/,next,0,_),_) --> n>", 1.0f, 1.0f, finalConf, 1.0f);
        n.run();
    }
    */

//    @Test public void missingEdgeCase() {
//        //((<%1 --> %2>, <(|, %1, %3) --> %2>), (<%3 --> %2>,
//        //((<p1 --> p2>, <(|, p1, p3) --> p2>), (<p3 --> p2>,
//        TestNAR tester = test();
//        tester.believe("<p1 --> p2>");
//        tester.believe("<(|, p1, p3) --> p2>");
//        tester.mustBelieve(100, "<p3 --> p2>",
//                1f, 1f, 0.1f, 1f);
//        tester.run(true);
//    }
    @Test public void missingEdgeCase2() {
        //((<(%1) --> %2>, %2), (<%2 --> (/, %1, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
        //  ((<(p1) --> p2>, p2), (<p2 --> (/, p1, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
        new RuleTest(
                "<(p1) --> belief:p2>.", "belief:p2.",
                "<belief:p2 --> (/, _, p1)>.",
                1.0f, 1.0f, 0.9f, 0.9f);
    }


    @Test public void missingEdgeCase3() {
        //((<(%1) --> %2>, %1), (<%1 --> (/, %2, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
        //  ((<(p1) --> p2>, p1), (<p1 --> (/, p2, _)>, (<Identity --> Truth>, <Identity --> Desire>)))
        new RuleTest(
                "<(belief:p1) --> p2>.", "belief:p1.",
                "<belief:p1 --> (/, p2, _)>.",
                1.0f, 1.0f, 0.9f, 0.9f);
    }

    @Test public void missingEdgeCase4() {
        //((<%1 --> (%2)>, %1), (<(\, %2, _) --> %1>, (<Identity --> Truth>, <Identity --> Desire>)))
        new RuleTest(
                "<belief:p1 --> (p2)>.", "belief:p1.",
                "<(\\, _, p2) --> belief:p1>.",
                1.0f, 1.0f, 0.9f, 0.9f);
    }

    @Test public void missingEdgeCase5() {
        //((<%1 --> (%2)>, %2), (<(\, %1, _) --> %2>, (<Identity --> Truth>, <Identity --> Desire>)))
        new RuleTest(
                "<p1 --> (belief:p2)>.", "belief:p2.",
                "<(\\, p1, _) --> belief:p2>.",
                1.0f, 1.0f, 0.9f, 0.9f);
    }

}
