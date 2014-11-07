package nars.test.multistep;

import nars.core.NAR;
import nars.core.build.Default;
import nars.io.TextOutput;

/**
I played a lot with 1.6.0 version for long time now. I'm am pleased that it works that nicely.

However I found one thing which I think is crucial to make it more effective.

Lets say we have a sequence of actions which leads to a goal, like "(&/,a,b,c) =/> d" with the goal "d!".
We currently don't create such statements by temporal induction, which currently means it will be like that:

a =/> b
b =/> c
c =/> d

with d being the goal,
it will spawn c,
then spawn b,
then spawn a with even less priority,
now it will eventually execute a.

However, this is very ineffective, because it has to do every step "mentally", with a ton of reasoning-overhead,
which leads, altough working in principial, to failing even for relatively simple examples.

Clearly with expressions like (&/,a,b,c), the execution could be achieved with much less reasoning overhead for the system,
it may work much better then.

Any ideas of a strategy to generate statements like "(&/,a,b,c) =/> d" with temporal induction in an effective way?
*/
public class GoalSequence {

    public static void main(String[] args) {
        NAR n = new Default().build();
        new TextOutput(n, System.out);
        n.addInput("<a =/> b>. \n <b =/> c>. \n <c =/> d>.");
        n.finish(500);
    }
}
