package nars.nal.nal4;

import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL4Test extends AbstractNALTest {


    public NAL4Test(Supplier<NAR> b) { super(b);  }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.fullTest;
//        return Arrays.asList(new Supplier[][]{
//                {() -> new Default()},
//                //{new DefaultDeep()},
//                //{new NewDefault()},
//                //{new NewDefault().setInternalExperience(null)},
//                //{new Default().setInternalExperience(null) },
//                {() -> new Default().nal(5)},
//                //{new Classic().setInternalExperience(null) },
//
//                //{new Solid(1, 128, 1, 1, 1, 2).level(5)}
//
//
//        });
    }

    @Test
    public void structural_transformation() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(*,acid,base) --> reaction>",1.0f,0.9f); //en("An acid and a base can have a reaction.");
        tester.mustBelieve(100, "<acid --> (/,reaction,_,base)>", 1.0f, 0.9f); //en("Acid can react with base.");
        tester.mustBelieve(100, "<base --> (/,reaction,acid,_)>", 1.0f, 0.9f); //en("A base is something that has a reaction with an acid.");
        tester.run();
    }

    @Test
     public void structural_transformation2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<acid --> (/,reaction,_,base)>",1.0f,0.9f); //en("Acid can react with base.");
        tester.mustBelieve(100, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("Acid can react with base.");
        tester.mustBelieve(100, "<base --> (/,reaction,acid,_)>", 1.0f, 0.9f); //en("A base is something that has a reaction with an acid.");
        tester.run();
    }

    @Test
    public void structural_transformation3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<base --> (/,reaction,acid,_)>",1.0f,0.9f); //en("A base is something that has a reaction with an acid.");
        tester.mustBelieve(100, "<acid --> (/,reaction,_,base)>", 1.0f, 0.9f); //en("Acid can react with base.");
        tester.mustBelieve(100, "<(acid,base) --> reaction>", 1.0f, 0.9f); //en("An acid and a base can have a reaction.");
        tester.run();
    }

    @Test
    public void structural_transformation4() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<neutralization --> (acid,base)>",1.0f,0.9f); //en("Neutralization is a relation between an acid and a base. ");
        tester.mustBelieve(100, "<(\\,neutralization,_,base) --> acid>.", 1.0f, 0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(100, "<(\\,neutralization,acid,_) --> base>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");
        tester.run();
    }

    @Test
    public void structural_transformation5() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,_,base) --> acid>",1.0f,0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(100, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Neutralization is a relation between an acid and a base.");
        tester.mustBelieve(100, "<(\\,neutralization,acid,_) --> base>", 1.0f, 0.9f); //en("Something that can be neutralized by an acid is a base.");
        tester.run();
    }

    @Test
    public void structural_transformation6() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<(\\,neutralization,acid,_) --> base>",1.0f,0.9f); //en("Something that can be neutralized by an acid is a base.");
        tester.mustBelieve(100, "<(\\,neutralization,_,base) --> acid>", 1.0f, 0.9f); //en("Something that can neutralize a base is an acid.");
        tester.mustBelieve(100, "<neutralization --> (acid,base)>", 1.0f, 0.9f); //en("Neutralization is a relation between an acid and a base.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<bird --> animal>",1.0f,0.9f); //en("Bird is a type of animal.");
        tester.ask("<(*,bird,plant) --> ?x>"); //en("What is the relation between a bird and a plant?");
        tester.mustBelieve(100, "<(*,bird,plant) --> (*,animal,plant)>", 1.0f, 0.81f); //en("The relation between bird and plant is a type of relation between animal and plant.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement2() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<neutralization --> reaction>",1.0f,0.9f); //en("Neutralization is a type of reaction.");
        tester.ask("<(\\,neutralization,acid,_) --> ?x>"); //en("What can be neutralized by acid?");
        tester.mustBelieve(100, "<(\\,neutralization,acid,_) --> (\\,reaction,acid,_)>", 1.0f, 0.81f); //en("What can be neutralized by acid can react with acid.");
        tester.run();
    }

    @Test
    public void composition_on_both_sides_of_a_statement3() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<soda --> base>",1.0f,0.9f); //en("Soda is a type of base.");
        tester.ask("<(/,neutralization,_,base) --> ?x>"); //en("What is something that can neutralize a base?");
        tester.mustBelieve(100, "<(/,neutralization,_,base) --> (/,neutralization,_,soda)>", 1.0f, 0.81f); //en("What can neutraliz base can react with base.");
        tester.run();
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

}
