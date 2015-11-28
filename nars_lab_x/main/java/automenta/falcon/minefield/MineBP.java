package automenta.falcon.minefield;

import automenta.falcon.BP;
import jurls.reinforcementlearning.domains.RLEnvironment;

/**
 * Created by me on 8/5/15.
 */
public class MineBP extends BP {

    public MineBP(int av_num) {
        super(av_num);
    }

    @Override
    public double getMaxQValue(int method, boolean train, RLEnvironment env) {
        double mQ = super.getMaxQValue(method, train, env);

        Maze maze = (Maze)env;
        if (maze.isHitMine(agent_num))   //case hit mine
            return 0;
        else if (maze.isHitTarget(agent_num))
            return 1; //case reach target

        return mQ;

    }
}
