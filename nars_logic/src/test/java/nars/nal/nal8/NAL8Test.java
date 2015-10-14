package nars.nal.nal8;

import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.nal.nal7.Tense;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.jgroups.util.Util.assertTrue;

@RunWith(Parameterized.class)
public class NAL8Test extends AbstractNALTest {

    public NAL8Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name = "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core8;
    }

    @Test public void testQuest() throws InvalidInputException {

        String term = "<a --> b>";

        NAR nar = nar();

        //nar.stdout();

        nar.goal(nar.term(term), Tense.Eternal, 1.0f, 0.9f);

        nar.quest(term);

        AtomicBoolean valid = new AtomicBoolean(false);

        //   eventAnswer: $0.10;0.90;1.00$ <a --> b>@ {0: 2} Input:$0.60;0.90;0.95$ <a --> b>! %1.00;0.90% {0: 1} Input
        nar.answer(nar.task(term + "@"), a -> {
            //System.out.println("answer: " + a);
            if (a.toString().contains("<a --> b>!"))
                valid.set(true);
        });

        nar.frame(3);

        assertTrue(valid.get());
    }

    int cycles = 10;
    @Test
    public void subgoal_1() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<{t001} --> [opened]>. :|:");
        tester.nar.frame(10);
        tester.believe("<(&/,<(*,SELF,{t002}) --> hold>,<(*,SELF,{t001}) --> at>,<(*,{t001}) --> ^open>) =/> <{t001} --> [opened]>>.");

        //0.81 because from goal perspective it is deduction, following from the definition (A)! being equal to (A==>D).
        tester.mustDesire(cycles, "(&/,<(*,SELF,{t002}) --> hold>,<(*,SELF,{t001}) --> at>,<(*,{t001}) --> ^open>)", 1.0f, 0.81f); //TODO:  :|: TODO HOW TEST FOR OCCURENCE?
        tester.run();
    }

    @Test
    public void subgoal_2() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("(&/,<(*,SELF,{t002}) --> hold>,<(*,SELF,{t001}) --> at>,(^open,{t001}))!");

        //0.81 because from goal perspective it is deduction, following from the definition (A)! being equal to (A==>D).
        tester.mustDesire(cycles, "<(*,SELF,{t002}) --> hold>", 1.0f, 0.81f); //TODO:  :|: TODO HOW TEST FOR OCCURENCE?
        tester.run();
    }

//    protected void testGoalExecute(String condition, String action) {
//
//        //TextOutput.out(nar);
//
//        tester.nar.believe(condition, Tense.Present, 1.0f, 0.9f);
//        tester.nar.goal("(&/," + condition + ',' + action + ")", 1.0f, 0.9f);
//
//        tester.mustDesire(40, action, 1.0f, 0.42f);
//
//        assertTrue("test impl unfinished", false);
//        //tester.mustOutput(nar.memory.eventExecute, 1, 10, action, '.', 1f, 1f, Global.OPERATOR_EXECUTION_CONFIDENCE, 1.00f, 0); // :|: %1.00;0.99%"); //TODO use an ExecuteCondition instance
//
//
//        tester.run(40);
//    }
//
//    @Test public void testGoalExecute0() {
//
//        /* 8.1.14
//        ********** [24 + 12 -> 25]
//        IN: <(*,SELF,{t002}) --> reachable>. :|:
//        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
//                45
//        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
//        */
//
//
//        testGoalExecute("<a --> b>", "pick(c,SELF)");
//    }
//
//    @Test public void testGoalExecute1() {
//        /* 8.1.14
//        ********** [24 + 12 -> 25]
//        IN: <(*,SELF,{t002}) --> reachable>. :|:
//        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
//                45
//        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
//        */
//
//
//        testGoalExecute("<(*,SELF,{t002}) --> reachable>", "pick({t002},SELF)");
//    }
//
//    @Test public void testGoalExecute2() {
//        /* 8.1.14
//        ********** [24 + 12 -> 25]
//        IN: <(*,SELF,{t002}) --> reachable>. :|:
//        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
//                45
//        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
//        */
//
//
//        testGoalExecute("<a --> b>", "pick(<c --> d>,SELF)");
//    }
//
//
//    @Test public void testOperationInheritance() {
//        tester.nar.input("pick(c). :|:");
//        tester.run(6);
//        tester.nar.input("<a --> b>. :|:");
//        tester.nar.input("<a --> b>!");
//        /*nar.on(Events.TaskDerive.class, new Reaction() {
//            @Override
//            public void event(Class event, Object[] args) {
//                Task t = (Task) args[0];
//                System.out.println("Derived: " + t);
//            }
//        });
//        nar.on(Events.TaskRemove.class, new Reaction() {
//            @Override
//            public void event(Class event, Object[] args) {
//                Task t = (Task) args[0];
//                System.out.println("Remove: " + t + " " + t.getReason());
//            }
//        });*/
//        tester.mustBelieve(100, "pick(c,SELF)", 1f, 1f, 0.40f, 0.50f);//this is checking for the eternalized result, but there are non-eternalized results that occur before that
//        tester.run(100);
//
//    }

}
