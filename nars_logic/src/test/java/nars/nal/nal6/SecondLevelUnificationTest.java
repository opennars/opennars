package nars.nal.nal6;

import nars.NAR;
import nars.Narsese;
import nars.nar.Default;
import org.junit.Test;

/**
 * Created by me on 11/6/15.
 */
public class SecondLevelUnificationTest {


    @Test
    public void test1() throws Narsese.NarseseException {
        NAR n = new Default(512, 1, 2, 3);

        //n.log();
        n.memory.eventTaskRemoved.on(t -> {
           //System.err.println("rm: " + t + " " + t.getLogLast());
        });

        n.believe("<<$1 --> x> ==> (&&,<#2 --> y>,<$1 --> (/,open,#2,_)>)>", 1.00f, 0.90f); //en("all xs are opened by some y");
        n.believe("<{z} --> y>", 1.00f, 0.90f); //en("z is a y");
        //tester.mustBelieve(cycles, "<<$1 --> x> ==> <$1 --> (/,open,{z},_)>>", 1.00f, 0.42f); //en("maybe all xs are opened by z");
        n.frame(250);
    }
    @Test
    public void test2() throws Narsese.NarseseException {
        NAR n = new Default(512, 1, 2, 3);

        //n.log();
        n.memory.eventTaskRemoved.on(t -> {
            //System.err.println("rm: " + t + " " + t.getLogLast());
        });

        n.believe("<<$1 --> x> ==> (&&,<#2 --> y>,<$1 --> #2>)>", 1.00f, 0.90f); //en("all xs are opened by some y");
        n.believe("<{z} --> y>", 1.00f, 0.90f); //en("z is a y");
        //tester.mustBelieve(cycles, "<<$1 --> x> ==> <$1 --> {z}>>", 1.00f, 0.42f); //en("maybe all xs are opened by z");
        n.frame(250);
    }


//    @Test public void termlinks1() throws IOException {
//        String s = "<<{z} --> y> ==> (&&, <$1 --> (/, open, #2, _)>, <#2 --> y>)>";
//        NAR n = new Default2(512, 3, 2, 1);
//        n.believe(s);
//        n.frame(1);
//        Concept c = n.concept(s);
//        assertNotNull(c);
//
//        System.out.println(c);
//        System.out.println(c.getTermLinkTemplates());
//        c.getTermLinks().printAll();
//
//        TermLinkGraph g = new TermLinkGraph(n);
//        System.out.println(g.isConnected());
//        System.out.println(g);
//
////        GmlExporter gme = new GmlExporter(
////                new IntegerNameProvider(), new StringNameProvider(),
////                new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
//        DOTExporter gme = new DOTExporter(
//                new IntegerNameProvider(), new StringNameProvider(),
//                new StringEdgeNameProvider<>()
//        );
//        gme.export(new FileWriter("/tmp/t.dot"), g);
//
//    }

}
