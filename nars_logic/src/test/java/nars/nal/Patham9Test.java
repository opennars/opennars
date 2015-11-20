//package nars.nal;
//
//import nars.NAR;
//import org.junit.Ignore;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.util.function.Supplier;
//
////don't touch this file - patham9
//
//@Ignore
//@RunWith(Parameterized.class)
//public class Patham9Test extends AbstractNALTest {
//
//    public Patham9Test(Supplier<NAR> b) { super(b); }
//
//    @Parameterized.Parameters(name= "{0}")
//    public static Iterable configurations() {
//        return AbstractNALTest.nars(6, false);
//    }
//
///*
//    //don't touch this file - patham9
//    @Test
//    public void Linkage_NAL5_abduction() {
//        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>","<robin --> animal>");
//    }
//
//
//    @Test
//    public void Linkage_NAL5_detachment() {
//        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>","<robin --> bird>");
//    }
//
//    @Test
//    public void detachment() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<<robin --> bird> ==> <robin --> animal>>"); //.en("If robin is a type of bird then robin can fly.");
//        tester.believe("<robin --> bird>"); //.en("Robin is a type of bird.");
//        tester.mustBelieve(100,"<robin --> animal>",1.00f,0.81f); //.en("Robin is a type of animal.");
//        tester.run();
//    }
//
//    @Test
//    public void detachment2() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<<robin --> bird> ==> <robin --> animal>>",0.70f,0.90f); //.en("Usually if robin is a type of bird then robin is a type of animal.");
//        tester.believe("<robin --> animal>"); //.en("Robin is a type of animal.");
//        tester.mustBelieve(100,"<robin --> bird>",1.00f,0.36f); //.en("I guess robin is a type of bird.");
//        tester.run();
//    }
//
//
//    @Test
//    public void Linkage_NAL6() {
//        ProperlyLinkedTest("<<$x --> bird> ==> <$x --> animal>>","<tiger --> animal>");
//    }
//
//*/
///*
//    @Test
//    public void Linkage_NAL6_variable_elimination2() {
//        ProperlyLinkedTest("<<$x --> bird> ==> <$x --> animal>>","<tiger --> animal>");
//    }*/
//
//
//  /*  @Test
//    public void variable_elimination2() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<<$x --> bird> ==> <$x --> animal>>"); //en("If something is a bird, then it is an animal.");
//        tester.believe("<tiger --> animal>"); //en("A tiger is an animal.");
//        tester.mustBelieve(1000, "<tiger --> bird>", 1.00f,0.45f); //en("I guess that a tiger is a bird.");
//        tester.run();
//    }*/
///*
//    @Test
//    public void set_operations() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<planetX --> {Mars,Pluto,Venus}>",0.9f,0.9f); //.en("PlanetX is Mars, Pluto, or Venus.");
//        tester.believe("<planetX --> {Pluto,Saturn}>", 0.7f,0.9f); //.en("PlanetX is probably Pluto or Saturn.");
//        tester.mustBelieve(500, "<planetX --> {Mars,Pluto,Saturn,Venus}>", 0.97f ,0.81f); //.en("PlanetX is Mars, Pluto, Saturn, or Venus.");
//        tester.mustBelieve(500, "<planetX --> {Pluto}>", 0.63f ,0.81f); //.en("PlanetX is probably Pluto.");
//        tester.run();
//    }*/
//
//    /*@Test
//    public void variable_elimination() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<<$x --> bird> ==> <$x --> animal>>"); //en("If something is a bird, then it is an animal.");
//        tester.believe("<robin --> bird>"); //en("A robin is a bird.");
//        tester.mustBelieve(1000,"<robin --> animal>",1.00f,0.81f); //en("A robin is an animal.");
//        tester.run();
//    }*/
///*
//    @Test
//    public void multiple_variable_elimination3() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("(&&,<#x --> lock>,<<$y --> key> ==> <#x --> (/,open,$y,_)>>)"); //en("There is a lock that can be opened by every key.");
//        tester.believe("<{lock1} --> lock>"); //en("Lock-1 is a lock.");
//        tester.mustBelieve(100, "<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>", 1.00f, 0.42f); //en("I guess Lock-1 can be opened by every key.");
//        tester.run();
//    }*/
//
//   /* @Test
//    public void second_level_variable_unification() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)", 1.00f, 0.90f); //en("there is a lock which is opened by all keys");
//        tester.believe("<{key1} --> key>", 1.00f, 0.90f); //en("key1 is a key");
//        tester.mustBelieve(100, "(&&,<#1 --> lock>,<#1 --> (/,open,{key1},_)>)", 1.00f, 0.81f); //en("there is a lock which is opened by key1");
//        tester.run();
//    }*/
//
//   /* @Test
//    public void strong_unification() throws InvalidInputException {
//        TestNAR tester = test();
//        tester.believe("<<(*,$a,is,$b) --> sentence> ==> <$a --> $b>>.", 1.00f, 0.90f);
//        tester.believe("<(*,bmw,is,car) --> sentence>.", 1.00f, 0.90f);
//        tester.mustBelieve(2000, "<bmw --> car>", 1.00f, 0.81f); //en("there is a lock which is opened by key1");
//        tester.run();
//    }
//*/
//    /*
//   @Test
//   public void variable_elimination4() throws InvalidInputException {
//       TestNAR tester = test();
//       tester.believe("(&&,<#x --> bird>,<#x --> swimmer>)"); //en("Some bird can swim.");
//       tester.believe("<swan --> bird>", 0.90f, 0.9f); //en("Swan is a type of bird.");
//       tester.mustBelieve(200, "<swan --> swimmer>", 0.90f, 0.43f); //en("I guess swan can swim.");
//       tester.run();
//   }*/
///*
//    @Test
//    public void pattern_trySubs_atomic() throws Exception {
//        Default nar = new Default();
//        String s1 = "<%A =/> %B>";
//        String s2 = "<<a --> A> =/> <b --> B>>";
//        nar.input(s1+".");
//        nar.input(s2+".");
//        nar.frame(10000);
//        Term t1 = nar.concept(s1).getTerm();
//        Term t2 = nar.concept(s2).getTerm();
//
//        HashMap<Term, Term> M1 = new HashMap<Term,Term>();
//        HashMap<Term, Term> M2 = new HashMap<Term,Term>();
//        FindSubst sub = new FindSubst(Op.VAR_PATTERN,M1,M2,new Random());
//        if(!sub.next(t1,t2,99999)) {
//            throw new Exception("Unification with pattern variable failed");
//        }
//    }
//
//    @Test
//    public void pattern_trySubs_Indep_Var() throws Exception {
//        Default nar = new Default();
//        String s1 = "<%A =/> %B>";
//        String s2 = "<<$1 --> A> =/> <$1 --> B>>";
//        nar.input(s1+".");
//        nar.input(s2+".");
//        nar.frame(10000);
//        Term t1 = nar.concept(s1).getTerm();
//        Term t2 = nar.concept(s2).getTerm();
//
//        HashMap<Term, Term> M1 = new HashMap<Term,Term>();
//        HashMap<Term, Term> M2 = new HashMap<Term,Term>();
//        FindSubst sub = new FindSubst(Op.VAR_PATTERN,M1,M2,new Random());
//        if(!sub.next(t1,t2,99999)) {
//            throw new Exception("Unification with pattern variable failed");
//        }
//    }
//
//    @Test
//    public void pattern_trySubs_Dep_Var() throws Exception {
//        Default nar = new Default();
//        String s1 = "<%A =/> %B>";
//        String s2 = "<<#1 --> A> =/> <$1 --> B>>";
//        nar.input(s1+".");
//        nar.input(s2+".");
//        nar.frame(10000);
//        Term t1 = nar.concept(s1).getTerm();
//        Term t2 = nar.concept(s2).getTerm();
//
//        HashMap<Term, Term> M1 = new HashMap<Term,Term>();
//        HashMap<Term, Term> M2 = new HashMap<Term,Term>();
//        FindSubst sub = new FindSubst(Op.VAR_PATTERN,M1,M2,new Random());
//        if(!sub.next(t1,t2,99999)) {
//            throw new Exception("Unification with pattern variable failed");
//        }
//    }*/
//
//
//}


package nars.nal;

import nars.NAR;
import nars.Narsese;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class Patham9Test extends AbstractNALTester {


    final int cycles = 100;

    public Patham9Test(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(7, false);
    }
/*
    @Test
    public void temporalOrder() throws Narsese.NarseseException {
        TestNAR tester = test();
        tester.believe("<<m --> M> =/> <p --> P>>");
        tester.believe("<<s --> S> <|> <m --> M>>", 0.90f, 0.9f);
        tester.mustBelieve(cycles, "<<s --> S> =/> <p --> P>>", 0.90f, 0.43f);
        tester.run();

        //(M =/> P), (S <|> M), not_equal(S,P) |- (S =/> P), (Truth:Analogy, Derive:AllowBackward)
    }
*/


   /* @Test
    public void induction_on_events2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,John,door) --> open>. :|:");
        tester.inputAt(6, "<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<<(John, door) --> open> =|> <(John, room) --> enter>>",
                1.00f, 0.45f,
                11);
        tester.run();
    }*/

    @Test
    public void induction_on_events_composition() throws Narsese.NarseseException {
        TestNAR tester = test();


        tester.input("<(*,John,key) --> hold>. :|:");
        tester.inputAt(10, "<<(*,John,door) --> open> =/> <(*,John,room) --> enter>>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,key) --> hold>,<(*,John,door) --> open>) =/> <(*,John,room) --> enter>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

   /* @Test
    public void temporalOrder() throws Narsese.NarseseException {
        SingleStepNAR tester = new SingleStepNAR();
        tester.input("<<m --> M> =/> <p --> P>>.");
        tester.input("<<s --> S> <|> <m --> M>>. %0.9;0.9%");

        tester.frame(100);

        DefaultConcept c1 = (DefaultConcept) tester.concept("<<s --> S> <|> <m --> M>>");

        BeliefTable tbl = c1.getBeliefs();



        //(M =/> P), (S <|> M), not_equal(S,P) |- (S =/> P), (Truth:Analogy, Derive:AllowBackward)
    }*/
}
