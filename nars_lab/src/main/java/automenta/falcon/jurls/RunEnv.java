package automenta.falcon.jurls;

import automenta.falcon.AGENT;
import automenta.falcon.FALCON;
import automenta.vivisect.swing.NWindow;
import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.wander.Curiousbot;

/**
 * Created by me on 8/5/15.
 */
public class RunEnv {

    public static void main(String[] args) throws InterruptedException {


        //RLEnvironment env = new PoleBalancing2D();
        RLEnvironment env = new Curiousbot();
        //RLEnvironment env = new Tetris(16,24);
        NWindow w = env.newWindow().show(800, 800);

        System.out.println(env.numActions() + " actions, " + env.numStates() + " states");

        int cyclesPerFrame = 15;
        int frame = 0;

        FALCON f = new FALCON(env);
        f.setTrace(false);
        f.init(AGENT.TDFALCON, true /* immediate reward */);

        while (true) {

            for (int i = 0; i < cyclesPerFrame; i++) {
                f.act(true);
                f.decay();
            }

            if (frame++ % 1000 == 0) {
                f.prune();
                f.purge();
            }

            w.repaint();

            Thread.sleep(50);

            //System.out.println(f.getNumCode());

        }


    }
}
