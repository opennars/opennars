package automenta.falcon.minefield;

import automenta.falcon.FALCON;
import jurls.reinforcementlearning.domains.RLEnvironment;

/**
 * Created by me on 8/4/15.
 */
public class MineFALCON extends FALCON {
    public MineFALCON(int av_num) {
        super(av_num);
    }

    @Override
    public int doSelectAction(boolean train, RLEnvironment maze) {
        return 0;
    }

    @Override
    public int doSelectValidAction(boolean train, RLEnvironment maze) {
        return 0;
    }

    @Override
    protected boolean validAction(RLEnvironment env, int selectedAction) {
        return ((Maze) env).withinField(agentID, selectedAction - 2);
    }

    @Override
    public double getMaxQValue(int method, boolean train, RLEnvironment env) {
        final Maze maze = (Maze)env;
        if (maze.isHitMine(agentID))   //case hit mine
            return 0.0;
        else if (maze.isHitTarget(agentID))
            return 1.0; //case reach target
        else {

        }
        return super.getMaxQValue(method, train, env);
    }
}
