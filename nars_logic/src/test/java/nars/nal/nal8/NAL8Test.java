package nars.nal.nal8;

import nars.Global;
import nars.NAR;
import nars.nal.JavaNALTest;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL8Test extends JavaNALTest {

    public NAL8Test(NAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default()},
                //{new DefaultMicro() },
                //{new Classic() }
                //{new Discretinuous() },
        });
    }

    @Test public void testQuest() throws InvalidInputException {

        String goal = "<a --> b>";

        tester.nar.goal(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY, goal, 1.0f, 0.9f);

        tester.run(50);

        tester.mustDesire(60, goal, 1.0f, 0.9f);
        tester.nar.quest(goal);

        tester.run(10);
    }

    protected void testGoalExecute(String condition, String action) {

        //TextOutput.out(nar);

        tester.nar.believe(condition, Tense.Present, 1.0f, 0.9f);
        tester.nar.goal("(&/," + condition + ',' + action + ")", 1.0f, 0.9f);

        tester.mustDesire(40, action, 1.0f, 0.42f);

        tester.mustOutput(1, 10, action, '.', 1f, 1f, Global.OPERATOR_EXECUTION_CONFIDENCE, 1.00f, 0); // :|: %1.00;0.99%"); //TODO use an ExecuteCondition instance


        tester.run(40);
    }

    @Test public void testGoalExecute0() {

        /* 8.1.14
        ********** [24 + 12 -> 25]
        IN: <(*,SELF,{t002}) --> reachable>. :|:
        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
                45
        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
        */


        testGoalExecute("<a --> b>", "pick(c,SELF)");
    }

    @Test public void testGoalExecute1() {
        /* 8.1.14
        ********** [24 + 12 -> 25]
        IN: <(*,SELF,{t002}) --> reachable>. :|:
        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
                45
        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
        */


        testGoalExecute("<(*,SELF,{t002}) --> reachable>", "pick({t002},SELF)");
    }

    @Test public void testGoalExecute2() {
        /* 8.1.14
        ********** [24 + 12 -> 25]
        IN: <(*,SELF,{t002}) --> reachable>. :|:
        IN: (&/,<(*,SELF,{t002}) --> reachable>,(^pick,{t002}))!
                45
        ''outputMustContain('(^pick,{t002},SELF)! %1.00;0.43%')
        */


        testGoalExecute("<a --> b>", "pick(<c --> d>,SELF)");
    }


    @Test public void testOperationInheritance() {
        tester.nar.input("pick(c). :|:");
        tester.run(6);
        tester.nar.input("<a --> b>. :|:");
        tester.nar.input("<a --> b>!");
        /*nar.on(Events.TaskDerive.class, new Reaction() {
            @Override
            public void event(Class event, Object[] args) {
                Task t = (Task) args[0];
                System.out.println("Derived: " + t);
            }
        });
        nar.on(Events.TaskRemove.class, new Reaction() {
            @Override
            public void event(Class event, Object[] args) {
                Task t = (Task) args[0];
                System.out.println("Remove: " + t + " " + t.getReason());
            }
        });*/
        tester.mustBelieve(100, "pick(c,SELF)", 1f, 1f, 0.40f, 0.50f);//this is checking for the eternalized result, but there are non-eternalized results that occur before that
        tester.run(100);

    }

}
