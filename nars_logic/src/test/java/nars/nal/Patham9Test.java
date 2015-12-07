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

import nars.$;
import nars.NAR;
import nars.Narsese;
import nars.concept.Concept;
import nars.nal.nal8.operator.TermFunction;
import nars.nar.Default2;
import nars.op.mental.Anticipate;
import nars.term.Term;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class Patham9Test extends AbstractNALTester {


    //final int cycles = 2000;

//    public Patham9Test(Supplier<NAR> b) {
//        super(b);
//    }
//
//    @Parameterized.Parameters(name = "{0}")
//    public static Iterable configurations() {
//        return AbstractNALTester.nars(8, false, true);
//    }




    /*@Test
    public void noise_simple() throws Narsese.NarseseException {
        NAR nar =new Default2(1000, 1, 1, 3);
        //NAR nar = nar();

        Anticipate.teststring = "";
        Anticipate.testing = true;
        nar.input("<a --> A>. :|:");
        nar.frame(30);
        nar.input("<b --> B>. :|:");
        nar.frame(30);
        nar.input("<a --> A>. :|:");
        nar.frame(30);
        nar.input("<b --> B>. :|:");
        nar.frame(30);
        nar.input("<a --> A>. :|:");
        nar.frame(4000);

        String a ="anticipating: <b --> B>\ndid not happen: <b --> B>\n";
        String b = Anticipate.teststring;
        assertEquals(a, b);

        //Concept c = nar.concept("<(&/, <b --> B>, /25) =/> <a --> A>>");
      // nar.forEachConcept(h -> {
      //      if(!h.getBeliefs().isEmpty()) {
       //         System.out.println(h.toString()+" "+h.getBeliefs().topTruth().toString());
        //    }
       // });
    }


    @Test
    public void noisetest() throws Narsese.NarseseException {
        NAR nar =new Default2(1000, 1, 1, 3);
        //NAR nar = nar();

        Anticipate.teststring = "";
        Anticipate.testing = true;
        nar.input("<a --> A>. :|:");
        nar.frame(20);
        nar.input("<x --> X>. :|:");
        nar.frame(20);
        nar.input("<c --> C>. :|:");
        nar.frame(300);
        nar.input("<a --> A>. :|:");
        nar.frame(20);
        nar.input("<m --> M>. :|:");
        nar.frame(20);
        nar.input("<c --> C>. :|:");
        nar.frame(300);
        nar.input("<a --> A>. :|:");
        nar.frame(4000);

        String a ="anticipating: <c --> C>\ndid not happen: <c --> C>\n";
        String b = Anticipate.teststring;
        assertTrue(b.contains(a));
    }*/

    final int cycles = 500;
    int exeCount = 0;
    private TermFunction exeFunc;

    public Patham9Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(8, false);
    }

    @Override
    public NAR nar() {
        NAR n = super.nar();

        final Term v = $.the("a");
        exeFunc = n.onExecTerm("exe", (Term[] t) -> {
            exeCount++;
            return v;
        });

        return n;
    }

    public float priority_safe(Concept c) {
        if(c == null) {
            return 0;
        }
        return c.getPriority();
    }

    @Test
    public void repeated_sequence_test() throws Narsese.NarseseException {
        NAR nar = new Default2(1000, 1, 1, 3);

        for(int i=0;i<1000;i++) {
            nar.input("<a --> A>. :|:");
            nar.frame(20);
            nar.input("<b --> B>. :|:");
            nar.frame(20);
            nar.input("<c --> C>. :|:");
            nar.frame(100);
        }

        Concept seq_a_b = nar.concept("(&/,<a --> A>,<b --> B>)");
        Concept seq_b_c = nar.concept("(&/,<b --> B>,<c --> C>)");
        Concept seq_a_c = nar.concept("(&/,<a --> A>,<c --> C>)");
        Concept seq_a_b_c = nar.concept("(&/,<a --> A>,<b --> B>,<c --> C>)");

        Concept imp_a_b = nar.concept("<(&/,<a --> A>,/1) =/> <b --> B>>");
        Concept imp_a_c = nar.concept("<(&/,<a --> A>,/1) =/> <c --> C>>");
        Concept imp_b_c = nar.concept("<(&/,<b --> B>,/1) =/> <c --> C>>");
        Concept imp_s = nar.concept("<(&/,<a --> A>,<b --> B>) =/> <c --> C>>");

        Concept c_a = nar.concept("<a --> A>");
        Concept c_b = nar.concept("<b --> B>");
        Concept c_c = nar.concept("<c --> C>");

        float priority_seq_a_b = priority_safe(seq_a_b);
        float priority_seq_b_c = priority_safe(seq_b_c);
        float priority_seq_a_c = priority_safe(seq_a_c);
        float priority_seq_a_b_c = priority_safe(seq_a_b_c);

        float priority_imp_a_b = priority_safe(imp_a_b);
        float priority_imp_a_c = priority_safe(imp_a_c);
        float priority_imp_b_c = priority_safe(imp_b_c);

        float priority_imp_a = priority_safe(c_a);
        float priority_imp_b = priority_safe(c_b);
        float priority_imp_c = priority_safe(c_c);
    }
/*
    @Test
    public void subsent_1() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(&/,<a --> b>,/10,<b --> c>,/100,<x --> y>,/20) =/> <d --> D>>. :|:");
        tester.inputAt(10, "(&/,<a --> b>,<b --> c>). :|:");

        tester.mustBelieve(cycles, "<(&/,<x --> y>,/20) =/> <d --> D>>",
                1.0f, 0.81f,
                120); // :|:
        tester.run();
    }*/

}
