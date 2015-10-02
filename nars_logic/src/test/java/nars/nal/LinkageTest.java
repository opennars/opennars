package nars.nal;

import nars.NAR;
import nars.Op;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import nars.term.Atom;
import nars.term.Term;
import nars.term.transform.FindSubst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

//don't touch this file - patham9

@RunWith(Parameterized.class)
public class LinkageTest extends AbstractNALTest {

    public LinkageTest(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core8;
    }

    public void ProperlyLinkedTest(String premise1, String premise2) throws Exception {
        TestNAR tester = test();
        tester.believe(premise1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(premise2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(premise1);
        boolean passed = false;
        if(ret!=null && ret.getTermLinks()!=null) {
            for (TermLink entry : ret.getTermLinks()) {
                Term w = entry.getTerm();
                if (w.toString().equals(premise2)) {
                    passed = true;
                }
            }
        }

        Concept ret2 = tester.nar.concept(premise2);
        boolean passed2 = false;
        if(ret2!=null && ret2.getTermLinks()!=null) {
            for (TermLink entry : ret2.getTermLinks()) {
                Term w = entry.getTerm();
                if (w.toString().equals(premise1)) {
                    passed2 = true;
                }
            }
        }

        if(passed && passed2) { //dummy to pass the test:
            tester.believe("<a --> b>");
        } else {
            throw new Exception("failed");
        }
        tester.mustBelieve(10,"<a --> b>",0.9f);
    }


    //interlinked with an intermediate concept, this is needed in order to select one as task and the other as belief
    public void ProperlyLinkedIndirectlyTest(String premise1, String premise2) throws Exception {
        TestNAR tester = test();
        tester.believe(premise1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(premise2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(premise1);
        boolean passed = false;
        if(ret!=null && ret.getTermLinks()!=null) {
            for (TermLink entry : ret.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise2)) {
                    passed = true;
                    break;
                }
                Term w = entry.getTerm();
                Concept Wc = tester.nar.concept(w);
                if(Wc != null) {
                    for (TermLink entry2 : Wc.getTermLinks()) {
                        if(entry2.getTerm().toString().equals(premise2)) {
                            passed = true;
                            break;
                        }
                    }
                }
            }
        }

        Concept ret2 = tester.nar.concept(premise2);
        boolean passed2 = false;
        if(ret2!=null && ret2.getTermLinks()!=null) {
            for (TermLink entry : ret2.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise1)) {
                    passed2 = true;
                    break;
                }
                Term w = entry.getTerm();
                Concept Wc = tester.nar.concept(w);
                if(Wc != null) {
                    for (TermLink entry2 : Wc.getTermLinks()) {
                        if(entry2.getTerm().toString().equals(premise1)) {
                            passed2 = true;
                            break;
                        }
                    }
                }
            }
        }

        if(passed && passed2) { //dummy to pass the test:
            tester.believe("<a --> b>");
        } else {
            throw new Exception("failed");
        }
        tester.mustBelieve(10,"<a --> b>",0.9f);
    }


    //interlinked with an intermediate concept, this is needed in order to select one as task and the other as belief
    public void ProperlyLinkedIndirectlyLayer2Test(String premise1, String premise2) throws Exception {
        TestNAR tester = test();
        tester.believe(premise1); //.en("If robin is a type of bird then robin can fly.");
        tester.believe(premise2); //.en("Robin is a type of bird.");
        tester.run(10);

        Concept ret = tester.nar.concept(premise1);
        boolean passed = false;
        if(ret!=null && ret.getTermLinks()!=null) {
            for (TermLink entry : ret.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise2)) {
                    passed = true;
                    break;
                }
                Term w = entry.getTerm();
                Concept Wc = tester.nar.concept(w);
                if(Wc != null) {
                    for (TermLink entry2 : Wc.getTermLinks()) {
                        if(entry2.getTerm().toString().equals(premise2)) {
                            passed = true;
                            break;
                        }
                        Term w2 = entry2.getTerm();
                        Concept Wc2 = tester.nar.concept(w2);
                        if(Wc2 != null) {
                            for (TermLink entry3 : Wc2.getTermLinks()) {
                                if(entry3.getTerm().toString().equals(premise2)) {
                                    passed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                /*if (w.toString().equals(premise2)) {
                    passed = true;
                }*/
            }
        }

        Concept ret2 = tester.nar.concept(premise2);
        boolean passed2 = false;
        if(ret2!=null && ret2.getTermLinks()!=null) {
            for (TermLink entry : ret2.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise1)) {
                    passed2 = true;
                    break;
                }
                Term w = entry.getTerm();
                Concept Wc = tester.nar.concept(w);
                if(Wc != null) {
                    for (TermLink entry2 : Wc.getTermLinks()) {
                        if(entry2.getTerm().toString().equals(premise1)) {
                            passed2 = true;
                            break;
                        }
                        Term w2 = entry2.getTerm();
                        Concept Wc2 = tester.nar.concept(w2);
                        if(Wc2 != null) {
                            for (TermLink entry3 : Wc2.getTermLinks()) {
                                if(entry3.getTerm().toString().equals(premise1)) {
                                    passed2 = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if(passed && passed2) { //dummy to pass the test:
            tester.believe("<a --> b>");
        } else {
            throw new Exception("failed");
        }
        tester.mustBelieve(10,"<a --> b>",0.9f);
    }


    @Test
    public void Linkage_NAL5_abduction() throws Exception {
        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>","<robin --> animal>");
    }


    @Test
    public void Linkage_NAL5_detachment() throws Exception {
        ProperlyLinkedTest("<<robin --> bird> ==> <robin --> animal>>", "<robin --> bird>");
    }

    @Test
    public void Linkage_NAL6_variable_elimination2() throws Exception {
        ProperlyLinkedTest("<<$x --> bird> ==> <$x --> animal>>", "<tiger --> animal>");
    }

    //here the problem is: they should be interlinked by lock
    @Test
    public void Part_Indirect_Linkage_NAL6_multiple_variable_elimination4() throws Exception {
        ProperlyLinkedIndirectlyTest("<#x --> lock>","<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_multiple_variable_elimination4() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("(&&,<#x --> (/,open,#y,_)>,<#x --> lock>,<#y --> key>)","<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_abduction_with_variable_elimination_abduction() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>", "<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_variable_introduction_induction() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>", "<lock1 --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_multiple_variable_elimination() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<(&&,<$x --> key>,<$y --> lock>) ==> <$y --> (/,open,$x,_)>>", "<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_level_variable_unification2() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>", "<{key1} --> key>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_elimination_deduction() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<lock1 --> lock>", "<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_unification7() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<(&&,<$x --> flyer>,<($x,worms) --> food>) ==> <$x --> bird>>", "<<$y --> flyer> ==> <$y --> [withWings]>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_unification6() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("<(&&,<$x --> flyer>,<$x --> [chirping]>, <($x, worms) --> food>) ==> <$x --> bird>>", "<(&&,<$y --> [chirping]>,<$y --> [withWings]>) ==> <$y --> bird>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_level_variable_unification() throws Exception {
        ProperlyLinkedIndirectlyLayer2Test("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)", "<{key1} --> key>");
    }

    @Test
    public void Indirect_Linkage_Basic() throws Exception {
        ProperlyLinkedIndirectlyTest("<a --> b>", "<b --> c>");
    }


}
