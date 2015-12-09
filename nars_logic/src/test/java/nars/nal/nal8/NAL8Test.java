package nars.nal.nal8;

import nars.$;
import nars.NAR;
import nars.Narsese;
import nars.nal.AbstractNALTester;
import nars.nal.nal7.Tense;
import nars.nal.nal8.operator.TermFunction;
import nars.nar.SingleStepNAR;
import nars.term.Term;
import nars.util.meter.TestNAR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class NAL8Test extends AbstractNALTester {

    final int cycles = 600;
    int exeCount = 0;
    private TermFunction exeFunc;

    public NAL8Test(Supplier<NAR> b) { super(b); }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(8, false);
    }

    @Override
    public NAR nar() {
        NAR n = super.nar();

        Term v = $.the("a");
        exeFunc = n.onExecTerm("exe", (Term[] t) -> {
            exeCount++;
            return v;
        });

        return n;
    }

    @Test public void testQuest() throws Narsese.NarseseException {

        String term = "<a --> b>";

        NAR nar = nar();

        //nar.stdout();

        nar.goal(nar.term(term), Tense.Eternal, 1.0f, 0.9f);

        nar.should(term);

        AtomicBoolean valid = new AtomicBoolean(false);

        //   eventAnswer: $0.10;0.90;1.00$ <a --> b>@ {0: 2} Input:$0.60;0.90;0.95$ <a --> b>! %1.00;0.90% {0: 1} Input
        nar.answer(nar.task(term + '@'), a -> {
            //System.out.println("answer: " + a);
            if (a.toString().contains("<a --> b>!"))
                valid.set(true);
        });

        nar.frame(3);

        assertTrue(valid.get());
    }

    @Test
    public void subsent_1() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<{t001} --> [opened]>. :|:");
        tester.inputAt(10, "<(&/, <(SELF,{t002}) --> hold>, <(SELF,{t001}) --> at>, <({t001}) --> ^open>) =/> <{t001} --> [opened]>>.");

        tester.mustBelieve(cycles, "(&/, <(SELF, {t002}) --> hold>, <(SELF, {t001}) --> at>, open({t001}))",
                1.0f, 0.45f,
                -5); // :|:

    }

    @Test
    public void subgoal_1_abd() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<{t001} --> [opened]>. :|:");
        tester.inputAt(10, "<(&/, <(SELF,{t002}) --> hold>, <(SELF,{t001}) --> at>, <({t001}) --> ^open>) =/> <{t001} --> [opened]>>.");

        tester.mustBelieve(cycles, "(&/, <(SELF, {t002}) --> hold>, <(SELF, {t001}) --> at>, open({t001}))",
                1.0f, 0.45f,
                -5); // :|:

    }

    @Test
    public void subgoal_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("(&/,<(SELF,{t002}) --> hold>, <(SELF,{t001}) --> at>, open({t001}))!");

        tester.mustDesire(cycles, "<(SELF,{t002}) --> hold>",
                1.0f, 0.81f); // :|:

    }

    @Test
    public void further_detachment() throws Narsese.NarseseException {
        TestNAR tester = test();


        tester.input("<(SELF,{t002}) --> reachable>. :|:");
        tester.inputAt(10, "(&/, <(SELF,{t002}) --> reachable>, pick({t002}))!");

        tester.mustDesire(cycles, "pick({t002})", 1.0f, 0.81f); // :|:

    }


    @Test
    public void further_detachment_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(SELF,{t002}) --> reachable>. :|:");
        tester.inputAt(10, "<(&/,<(SELF,{t002}) --> reachable>,pick({t002}))=/><(SELF,{t002}) --> hold>>.");

        tester.mustBelieve(cycles, "<pick({t002}) =/> <(SELF, {t002}) --> hold>>", 1.0f, 0.81f, 0); // :|:

    }

    @Test
    public void temporal_deduction_1() throws Narsese.NarseseException {
        TestNAR tester = test();


        tester.input("pick({t002}). :\\: ");
        tester.inputAt(10, "<pick({t002}) =/> <(SELF,{t002}) --> hold>>. :\\: ");

        tester.mustBelieve(cycles, "<(SELF,{t002}) --> hold>", 1.0f, 0.81f, 0); // :|:

    }

    @Test
    public void temporal_goal_detachment_1() throws Narsese.NarseseException {
        TestNAR tester = test();


        tester.input("<(SELF,{t002}) --> hold>.");
        tester.inputAt(10, "(&/, <(SELF,{t002}) --> hold>, <(SELF,{t001}) --> at>, open({t001}) )!");

        tester.mustDesire(cycles, "(&/,<(SELF,{t001}) --> at>,open({t001}))", 1.0f, 0.81f); // :|:

    }

    @Test
    public void temporal_deduction_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(&/, <(SELF,{t002}) --> hold>, <(SELF,{t001}) --> at>, open({t001})) =/> <{t001} --> [opened]>>.");
        tester.inputAt(10, "<(SELF,{t002}) --> hold>. :|: ");

        //mustBelieve?
        tester.mustBelieve(cycles, "<(&/, <(SELF,{t001}) --> at>, open({t001})) =/> <{t001} --> [opened]>>", 1.0f, 0.81f, 10); // :|:

    }

    @Test
    public void detaching_condition() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(&/, <(SELF,{t002}) --> hold>, <(SELF, {t001}) --> at>, open({t001})) =/> <{t001} --> [opened]>>.");
        tester.inputAt(10, "<(SELF,{t002}) --> hold>. :|:");

        tester.mustBelieve(cycles, "<(&/, <(SELF,{t001}) --> at>, open({t001})) =/> <{t001} --> [opened]>>", 1.0f, 0.81f, 10); // :|:

    }

    @Test
    public void detaching_single_premise() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("(&/,<(*,SELF,{t002}) --> reachable>,pick({t002}))!");


        tester.mustDesire(cycles, "<(*,SELF,{t002}) --> reachable>", 1.0f, 0.81f); // :|:

    }

    @Test
    public void detaching_single_premise2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("(&/, <(SELF,{t001}) --> at>, open({t001}) )!");


        tester.mustDesire(cycles, "<(SELF,{t001}) --> at>", 1.0f, 0.81f); // :|:

    }

    @Test
    public void goal_deduction() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(SELF,{t001}) --> at>!");
        tester.inputAt(10, "<goto($1)=/><(SELF,$1) --> at>>.");

        tester.mustDesire(cycles, "goto({t001})", 1.0f, 0.81f); // :|:

    }

    @Test
    public void goal_deduction_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("(^goto,{t001}). :\\: ");
        tester.inputAt(10, "<(^goto,$1)=/><(*,SELF,$1) --> at>>. ");

        tester.mustBelieve(cycles, "<(*,SELF,{t001}) --> at>", 1.0f, 0.81f, 0); // :|:

    }

    @Test
    public void detaching_condition_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t001}) --> at>. :|: ");
        tester.inputAt(10, "<(&/,<(*,SELF,{t001}) --> at>,(^open,{t001}))=/><{t001} --> [opened]>>. :|:");

        tester.mustBelieve(cycles, "<(^open, {t001}) =/> <{t001} --> [opened]>>", 1.0f, 0.81f, 0); // :|:

    }

    @Test
    public void goal_ded_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t001}) --> at>. :|:");
        tester.inputAt(10, "(&/,<(*,SELF,{t001}) --> at>,(^open,{t001}))!");

        tester.mustDesire(cycles, "(^open,{t001})", 1.0f, 0.81f); // :|:

    }

    @Test
    public void belief_deduction_by_condition() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<open({t001})=/><{t001} --> [opened]>>. :|: ");
        tester.inputAt(10, "open({t001}). :|:");

        tester.mustBelieve(cycles, "<{t001} --> [opened]>", 1.0f, 0.81f, 15); // :|:

    }

    @Test
    public void condition_goal_deduction() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t002}) --> reachable>! ");
        tester.inputAt(10, "<(&|,<(*,$1,#2) --> on>,<(*,SELF,#2) --> at>)=|><(*,SELF,$1) --> reachable>>.");

        tester.mustDesire(cycles, "(&|,<(*,SELF,#1) --> at>,<(*,{t002},#1) --> on>)", 1.0f, 0.81f); // :|:

    }

    @Test
    public void condition_goal_deduction_2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,{t002},{t003}) --> on>. :|:");
        tester.inputAt(10, "(&|,<(*,{t002},#1) --> on>,<(*,SELF,#1) --> at>)!");

        tester.mustDesire(cycles, "<(*,SELF,{t003}) --> at>", 1.0f, 0.81f); // :|:

    }

    @Test
    public void condition_goal_deduction_3() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t003}) --> at>!");
        tester.inputAt(10, "<goto($1)=/><(*,SELF,$1) --> at>>.");

        tester.mustDesire(cycles, "goto({t003})", 1.0f, 0.81f); // :|:

    }

    @Test
    public void condition_goal_deduction_3_() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t003}) --> at>! :|:");
        tester.inputAt(10, "<(&/,goto($1),/1) =/> <(*,SELF,$1) --> at>>.");

        tester.mustDesire(cycles, "goto({t003})", 1.0f, 0.81f, -6); // :|:

    }

    @Test
    public void condition_goal_deduction_3__() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<(*,SELF,{t003}) --> at>! :|:");
        tester.inputAt(10, "<goto($1) =/> <(*,SELF,$1) --> at>>.");

        tester.mustDesire(cycles, "goto({t003})", 1.0f, 0.81f, -5); // :|:

    }

    @Test
    public void conditional_abduction_test() throws Narsese.NarseseException { //maybe to nal7 lets see how we will split these in the future
        TestNAR tester = test();

        tester.input("<(*,SELF,{t003}) --> at>. :|:");
        tester.inputAt(10, "<(&/,goto($1),/1) =/> <(*,SELF,$1) --> at>>.");

        tester.mustBelieve(cycles, "goto({t003})", 1.0f, 0.45f, -6); // :|:

    }

    @Test
    public void ded_with_var_temporal() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<({t003}) --> ^goto>. :|: ");
        tester.inputAt(10, "<<($1) --> ^goto> =/> <(SELF,$1) --> at>>. ");

        tester.mustBelieve(cycles, "<(SELF, {t003}) --> at>", 1.0f, 0.81f, 5); // :|:

    }

    @Test
    public void ded_with_var_temporal2() throws Narsese.NarseseException {
        TestNAR tester = test();

        tester.input("<({t003}) --> ^goto>. :|: ");
        tester.inputAt(10, "<<($1) --> ^goto> =/> <(SELF,$1) --> at>>. ");

        tester.mustBelieve(cycles, "<(SELF, {t003}) --> at>", 1.0f, 0.81f,5); // :|:

    }

    @Test public void desiredFeedbackReversedIntoGoalEternal()  {
        TestNAR tester = test();
        tester.input("<y --> (/,^exe,x,_)>!");
        tester.mustDesire(5, "exe(x, y)", 1.0f, 0.9f);
    }
    @Test public void desiredFeedbackReversedIntoGoalNow()  {
        TestNAR tester = test();
        tester.input("<y --> (/,^exe,x,_)>! :|:");
        tester.mustDesire(5, "exe(x, y)", 1.0f, 0.9f, 0);
    }

    @Test public void testExecutionResult()  {
        TestNAR tester = test();


        //tester.nar.log();
        tester.input("<#y --> (/,^exe,x,_)>! :|:");
        tester.mustDesire(4, "exe(x, #y)", 1.0f, 0.9f, 0);

        if (!(tester.nar instanceof SingleStepNAR)) {
            //tester.nar.log();
            tester.mustBelieve(250, "exe(x, a)", 1.0f, 0.99f, 10);
            //        tester.mustBelieve(26, "<a --> (/, ^exe, x, _)>",
            //                exeFunc.getResultFrequency(),
            //                exeFunc.getResultConfidence(),
            //                exeFunc.getResultFrequency(),
            //                exeFunc.getResultConfidence(),
            //                6);
            tester.nar.onEachFrame(n -> {
                if (n.time() > 8)
                    assertEquals(1, exeCount);
            });
        }

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
