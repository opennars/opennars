package nars.nal;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.meter.TestNAR;
import nars.nar.Default;
import nars.term.Compound;
import nars.term.Term;
import nars.util.graph.TermLinkGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//don't touch this file - patham9

@RunWith(Parameterized.class)
public class LinkageTest extends AbstractNALTest {

    private int cycles = 250;

    public LinkageTest(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core6;
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

        assertTrue(passed && passed2);
//        if(passed && passed2) { //dummy to pass the test:
//            tester.believe("<a --> b>");
//        } else {
//            throw new Exception("failed");
//        }
//        tester.mustBelieve(10,"<a --> b>",0.9f);
    }


    //interlinked with an intermediate concept, this is needed in order to select one as task and the other as belief
    public void ProperlyLinkedIndirectlyTest(String spremise1, String spremise2) throws Exception {


        Default nar = new Default();

        Compound premise1 = nar.term(spremise1);
        assertNotNull(premise1);
        assertEquals(spremise1, premise1.toString());

        Compound premise2 = nar.term(spremise2);
        assertNotNull(premise2);
        assertEquals(spremise2, premise2.toString());

        nar.believe(premise1,1.0f,0.9f); //.en("If robin is a type of bird then robin can fly.");
        nar.believe(premise2,1.0f,0.9f); //.en("Robin is a type of bird.");
        nar.frame(cycles); //TODO: why does it take 30 cycles till premise1="<<$1 --> bird> ==> <$1 --> animal>>", premise2="<tiger --> animal>" is conceptualized?

        List<String> fails = new ArrayList();

        Concept ret = nar.concept(premise1);
        boolean passed = false;
        if(ret!=null && ret.getTermLinks()!=null) {
            for (TermLink entry : ret.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise2)) {
                    passed = true;
                    break;
                }

                Term w = entry.getTerm();
                Concept Wc = nar.concept(w);
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

        Concept ret2 = nar.concept(premise2);
        boolean passed2 = false;
        if(ret2!=null && ret2.getTermLinks()!=null) {
            for (TermLink entry : ret2.getTermLinks()) {
                if(entry.getTerm().toString().equals(premise1)) {
                    passed2 = true;
                    break;
                }
                Term w = entry.getTerm();
                Concept Wc = nar.concept(w);
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

        System.err.println(premise1 + " not linked with " + premise2);
        TermLinkGraph g = new TermLinkGraph(nar);
        g.print(System.out);

        assertTrue(passed && passed2);

//        if(passed && passed2) { //dummy to pass the test:
//            tester.believe("<a --> b>");
//        } else {
//
//        }
//        tester.mustBelieve(10,"<a --> b>",0.9f);
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
        ProperlyLinkedIndirectlyTest("<<$1 --> bird> ==> <$1 --> animal>>", "<tiger --> animal>");
    }

    //here the problem is: they should be interlinked by lock
    @Test
    public void Part_Indirect_Linkage_NAL6_multiple_variable_elimination4() throws Exception {
        ProperlyLinkedIndirectlyTest("<#1 --> lock>","<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_multiple_variable_elimination4() throws Exception {
        ProperlyLinkedIndirectlyTest("(&&,<#1 --> (/,open,#2,_)>,<#1 --> lock>,<#2 --> key>)", "<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_abduction_with_variable_elimination_abduction() throws Exception {
        ProperlyLinkedIndirectlyTest("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>", "<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_variable_introduction_induction() throws Exception {
        ProperlyLinkedIndirectlyTest("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>", "<lock1 --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_multiple_variable_elimination() throws Exception {
        ProperlyLinkedIndirectlyTest("<(&&,<$x --> key>,<$y --> lock>) ==> <$y --> (/,open,$x,_)>>", "<{lock1} --> lock>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_level_variable_unification2() throws Exception {
        ProperlyLinkedIndirectlyTest("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>", "<{key1} --> key>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_elimination_deduction() throws Exception {
        ProperlyLinkedIndirectlyTest("<lock1 --> lock>", "<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_unification7() throws Exception {
        ProperlyLinkedIndirectlyTest("<(&&,<$x --> flyer>,<($x,worms) --> food>) ==> <$x --> bird>>", "<<$y --> flyer> ==> <$y --> [withWings]>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_variable_unification6() throws Exception {
        ProperlyLinkedIndirectlyTest("<(&&,<$x --> flyer>,<$x --> [chirping]>, <($x, worms) --> food>) ==> <$x --> bird>>", "<(&&,<$y --> [chirping]>,<$y --> [withWings]>) ==> <$y --> bird>>");
    }

    @Test
    public void Indirect_Linkage_NAL6_second_level_variable_unification() throws Exception {
        ProperlyLinkedIndirectlyTest("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)", "<{key1} --> key>");
    }

    @Test
    public void Indirect_Linkage_Basic() throws Exception {
        ProperlyLinkedIndirectlyTest("<a --> b>", "<b --> c>");
    }

    @Test
    public void Indirect_Linkage_Layer2_Basic() throws Exception {
        ProperlyLinkedIndirectlyTest("<a --> <b --> <k --> x>>>", "<k --> x>");
    }

    @Test
    public void Indirect_Linkage_Layer2_Basic_WithVar() throws Exception {
        ProperlyLinkedIndirectlyTest("<#1 --> <b --> <k --> x>>>", "<k --> x>");
    }

    @Test
    public void Indirect_Linkage_Layer2_Basic_WithVar2() throws Exception {
        ProperlyLinkedIndirectlyTest("<a --> <b --> <#1 --> x>>>", "<k --> x>");
    }

    public void ConceptFormationTest(String s) throws Exception {
        TestNAR tester = test();
        tester.believe(s,1.0f,0.9f);
        tester.run(10);
        Concept ret = tester.nar.concept(s);

        if(ret == null) {
            tester.nar.forEachConcept(System.out::println);
        }

        assertNotNull("Failed to create a concept for "+s, ret);



    }

    @Test
    public void Basic_Concept_Formation_Test() throws Exception {
        ConceptFormationTest("<a --> b>");
    }

    @Test
    public void Advanced_Concept_Formation_Test() throws Exception {
        ConceptFormationTest("<#1 --> b>");
    }

    @Test
     public void Advanced_Concept_Formation_Test2() throws Exception {
        ConceptFormationTest("<<$1 --> a> ==> <$1 --> b>>");
    }

    @Test
    public void Advanced_Concept_Formation_Test2_2() throws Exception {
        ConceptFormationTest("<<$1 --> bird> ==> <$1 --> animal>>");
    }

    @Test
    public void Advanced_Concept_Formation_Test3() throws Exception {
        ConceptFormationTest("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)");
    }

    @Test
    public void Advanced_Concept_Formation_Test4() throws Exception {
        ConceptFormationTest("(&&,<#1 --> (/,open,#2,_)>,<#1 --> lock>,<#2 --> key>)");
    }


    @Test
    public void Variable_Normalization_1() throws Exception {
        Default tester = new Default();
        String nonsense = "<(&&,<#1 --> M>,<#1 --> M>) ==> <#1 --> nonsense>>";
        tester.believe(nonsense); //.en("If robin is a type of bird then robin can fly.");
        tester.frame(10);
        Concept c = tester.concept(nonsense);
        if(c.toString().equals("<<#1 --> M> ==> <#2 --> nonsense>>")) {
            throw new Exception("NAR fundamentally broken");
        }
        if(c==null) {
            throw new Exception("Normalization went wrong");
        }
    }

}
