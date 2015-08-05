package automenta.falcon.minefield;

import automenta.falcon.DNDP;
import jurls.reinforcementlearning.domains.RLEnvironment;

/**
 * Created by me on 8/5/15.
 */
public class MineDNDP extends DNDP {
    public MineDNDP(int av_num) {
        super(av_num);
    }

    @Override
    public boolean validAction(int i, RLEnvironment maze) {
        return ((Maze) maze).withinField(agent_num, i - 2);
    }


    @Override
    public int findMax(double[] u, int numActions, RLEnvironment m) {
        int i = super.findMax(u, numActions, m);



        if (agent_num == -1 && Trace) {
            displayVector("No Valid Action u", u, numActions);
            for (int k = 0; k < m.numActions(); k++) {
                if (validAction(k, m)) System.out.print("true ");
                else System.out.print("false ");
            }
            System.out.println("");
        }

        return i;

    }
}
