package automenta.falcon.jurls;

import automenta.falcon.AGENT;
import automenta.falcon.FALCON;
import automenta.vivisect.swing.NWindow;
import jurls.reinforcementlearning.domains.PoleBalancing2D;
import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.tetris.Tetris;
import jurls.reinforcementlearning.domains.wander.Curiousbot;

/**
 * Created by me on 8/5/15.
 */
public class RunEnv {

    public static void main(String[] args) throws InterruptedException {


        //RLEnvironment env = new PoleBalancing2D();
        RLEnvironment env = new Curiousbot();
        //RLEnvironment env = new Tetris(8,4);
        NWindow w = env.newWindow().show(600, 600);


        FALCON f = new FALCON(env);
        f.setTrace(true);
        f.init(AGENT.TDFALCON, true /* immediate reward */);

        while (true) {

            f.act(true);

            env.frame();
            w.repaint();

            Thread.sleep(10);

        }


    }
}
