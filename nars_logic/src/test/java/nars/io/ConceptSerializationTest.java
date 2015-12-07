//package nars.io;
//
//import nars.NAR;
//import nars.concept.Concept;
//import nars.concept.util.ConceptExternalizer;
//import nars.nar.Default;
//import nars.task.Task;
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.io.*;
//import java.util.Arrays;
//import java.util.Collection;
//
//import static org.junit.Assert.assertNotNull;
//
//
//@Ignore
//@RunWith(Parameterized.class)
//public class ConceptSerializationTest  {
//
//    private final String input;
//
//    public ConceptSerializationTest(String input) {
//        this.input = input;
//    }
//
//    @Test
//    public void testConceptExternalizer() throws IOException, InterruptedException, ClassNotFoundException {
//        byte[] by;
//        Concept ac;
//        final NAR a = new Default();
//        ConceptExternalizer ae = new ConceptExternalizer(a);
//
//        Task t = a.inputTask(input);
//        a.frame(1);
//        ac = a.concept(t.getTerm());
//        assertNotNull(ac);
//
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream o = new ObjectOutputStream(baos);
//        ae.writeObject(o, ac);
//        o.flush();
//
//        by = baos.toByteArray();
//        System.out.println(ac + " externalized to " + by.length + " bytes");
//
//
//        //TEST INPUT ===================
//        final NAR b = new Default();
//        ConceptExternalizer be = new ConceptExternalizer(b);
//
//        Concept bc = be.readObject(new ObjectInputStream(new ByteArrayInputStream(by)));
//
//        assertEquals(ac, bc);
//
////
////
//////        ByteArrayInputStream bais = new ByteArrayInputStream(by);
//////        ObjectInputStream i = new ObjectInputStream(bais);
//////        i.readObject();
////
////        e.writeObject(jbossMarshaller.)
////
////            byte[] by = jbossMarshaller.objectToByteBuffer(c);
//
//
////            Object x = jbossMarshaller.objectFromByteBuffer(by);
////            if (x!=null)
////                System.out.println(c + " internalized to " + x.getClass() + " " + x);
//
//
//
//
//
//    }
//
//    @Parameterized.Parameters(name = "{0}")
//    public static Collection configurations() {
//        return Arrays.asList(new Object[][]{
//
//                //TODO add all term types here
//
//                {"<x-->y>."},
//                {"<#1-->y>."},
//                {"$0.12;0.41;0.31$ (--, x). %1.00|0.50%"},
//                {"(x, y, z)?"},
//                {"<a --> b>!"},
//                {"(x && y)."},
//                {"(x =/> y)."},
//                {"(x ==> y)."},
//                {"(x </> y)@"},
//                {"((x,y) ==> z)@"},
//                {"((x,y) =/> z)."},
//                {"<a --> (b, c)>."},
//                {"<a --> (b, c)>. :/:"},
//                {"$0.5$ (&/, a, /3, b)."},
//
//
//                //TODO images
//                //Intervals
//                //Immediate Operations (Command)
//
//
//        });
//    }
//
//    public void assertEquals(Concept a, Concept b)  {
//
//        Assert.assertEquals(a.hashCode(), b.hashCode());
//
//        Assert.assertEquals(a.getTerm(), b.getTerm());
//        Assert.assertEquals(a.getBudget(), b.getBudget());
//
//        Assert.assertEquals(a.getBeliefs(), b.getBeliefs());
//        Assert.assertEquals(a.getGoals(), b.getGoals());
//        Assert.assertEquals(a.getQuestions(), b.getQuestions());
//
//        if (!a.getTermLinks().equals(b.getTermLinks())) {
//            System.err.println("inequal termlinks: ");
//            a.getTermLinks().printAll();
//            b.getTermLinks().printAll();
//        }
//        Assert.assertEquals(a.getTermLinks(), b.getTermLinks());
//        Assert.assertEquals(a.getTaskLinks(), b.getTaskLinks());
//
//        //Assert.assertEquals(a.getTermLinkBuilder().templates(), b.getTermLinkBuilder().templates());
//
//
//        if (!a.equals(b)) {
//            System.out.println(a +  "\t\t" + b);
//
//            /*try {
//                System.out.println(JSON.omDeep.writeValueAsString(a));
//                System.out.println(JSON.omDeep.writeValueAsString(b));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }*/
//        }
//
//        a.equals(b);
//
//        Assert.assertEquals(a, b);
//
//        Assert.assertEquals(a.toString(), b.toString());
//
//    }
//
//
//}