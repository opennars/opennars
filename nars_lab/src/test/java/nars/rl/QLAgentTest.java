//package nars.rl;
//
//import jurls.reinforcementlearning.domains.RLEnvironment;
//import jurls.reinforcementlearning.domains.follow.Follow1D;
//import nars.Global;
//import nars.NAR;
//import nars.concept.Concept;
//import nars.nar.Default;
//import org.junit.Test;
//
//import static junit.framework.TestCase.assertNotNull;
//import static junit.framework.TestCase.assertTrue;
//
///**
// * Created by me on 5/7/15.
// */
//public class QLAgentTest {
//
//
//
//    @Test
//    public void testQLAgents() {
//        testQLAgent(4);
//        testQLAgent(16);
//        testMatrixState(testQLAgent(128));
//    }
//
//    private NAR testMatrixState(NAR n) {
//        //n.memory.concepts.forEach(x -> System.out.println(x));
//
//
//        return n;
//    }
//
//    public NAR testQLAgent(int conceptCapacity) {
//
//        Global.DEBUG = true;
//
//        RLEnvironment env = new Follow1D();
//
//        NAR n = new NAR( new Default(conceptCapacity, 1, 1) );
//
//        QLAgent a = new QLAgent(n, "act", "<good --> be>", env,
//                new RawPerception.BipolarDirectPerception("s", 0.5f));
//
//        a.ql.brain.setEpsilon(0.5f);
//
//        //TODO fluent api to define perceptual hierarchy:
//        //new QLAgent(n).in(env).in(new RawPerception, new SOM, ..)
//
//        //allow the concept memory to reach capacity
//        for (int i = 0; i < conceptCapacity / 4; i++)
//            n.frame();
//
//
//        if (conceptCapacity < 20) {
//            //TODO ideally we want the capacity and max size to be equal, but for now it's 1 less than capacity
//            assertTrue(n.memory.getCycleProcess().size() + " concepts for capacity=" + conceptCapacity, Math.abs(conceptCapacity - n.memory.getCycleProcess().size()) <= 1);
//        }
//        else {
//
//            a.ql.spontaneous(0.5f);
//
//            //TextOutput.out(n);
//
//            for (int i = 0; i < 32; i++)
//                n.frame();
//
//
////            //a.getOperatorConcept().termLinks.printAll(System.out);
////            //a.getActionConcept(0).termLinks.printAll(System.out);
//            Concept actionConcept = a.getActionConcept(0);
//            assertNotNull(actionConcept);
////            actionConcept.print(System.out);
////
////            //check that the agent knows all the actions
////            assertEquals(a.ql.cols.toString(), a.ql.cols.size(), env.numActions());
////
////
////            assertTrue(a.ql.rows.toString(), a.ql.rows.size() > 0);
//        }
//
//        return n;
//    }
// }
