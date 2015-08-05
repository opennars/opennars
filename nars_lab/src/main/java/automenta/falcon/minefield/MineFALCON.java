//package automenta.falcon.minefield;
//
//import automenta.falcon.FALCON;
//import jurls.reinforcementlearning.domains.RLEnvironment;
//
///**
// * Created by me on 8/4/15.
// */
//public class MineFALCON extends FALCON implements MazeRun.MineAGENT {
//
//    protected final int agentID;
//    final int numSonarInput = 10;
//    final int numAVSonarInput = 0;
//    final int numBearingInput = 8;
//    final int numRangeInput = 0;
//
//    public MineFALCON(RLEnvironment env, int av_num) {
//        super(env);
//        agentID = av_num;
//    }
//
//
//    public void setState(double[] sonar, double[] av_sonar, int bearing, double range) {
//        int index;
//
//        final double[] ii = getState();
//
//        setInput(sonar, av_sonar, bearing, range, ii);
//    }
//
//    public void setNewState(double[] sonar, double[] av_sonar, int bearing, double range) {
//
//        final double[] ii = getNewState();
//
//        setInput(sonar, av_sonar, bearing, range, ii);
//    }
//
//
//    protected void setInput(double[] sonar, double[] av_sonar, int bearing, double range, double[] ii) {
//        int index;
//
//        for (int i = 0; i < numSonarInput / 2; i++) {
//
//            ii[i] = sonar[i];
//            ii[i + (numSonarInput / 2)] = 1 - sonar[i];
//        }
//        index = numSonarInput;
//
//        for (int i = 0; i < (numAVSonarInput / 2); i++) {
//
//            ii[i] = av_sonar[i];
//            ii[i + numSonarInput / 2] = 1 - av_sonar[i];
//        }
//        index += numAVSonarInput;
//
//        for (int i = 0; i < numBearingInput; i++)
//            ii[index + i] = 0.0;
//        ii[index + bearing] = 1.0;
//        index += numBearingInput;
//
//        for (int i = 0; i < (numRangeInput / 2); i++) {
//            ii[index + i] = range;
//            ii[index + i + (numRangeInput / 2)] = 1 - range;
//        }
//    }
//
//
//    @Override
//    public int doSelectAction(boolean train, RLEnvironment env) {
//        return 0;
//    }
//
//    @Override
//    public int doSelectValidAction(boolean train, RLEnvironment env) {
//        return 0;
//    }
//
//    @Override
//    protected boolean validAction(RLEnvironment env, int selectedAction) {
//        return ((Maze) env).withinField(agentID, selectedAction - 2);
//    }
//
//    @Override
//    public double getMaxQValue(int method, boolean train, RLEnvironment env) {
//        final Maze maze = (Maze)env;
//        if (maze.isHitMine(agentID))   //case hit mine
//            return 0.0;
//        else if (maze.isHitTarget(agentID))
//            return 1.0; //case reach target
//        else {
//
//        }
//        return super.getMaxQValue(method, train, env);
//    }
//
//    public void displayState(String s, double[] x, int n) {
//        System.out.print(s + "   Sonar: [");
//        int index = 0;
//        for (int i = 0; i < numSonarInput; i++)
//            System.out.print(df.format(x[index + i]) + ", ");
//        System.out.print(df.format(x[index + numSonarInput - 1]));
//
//        System.out.println("]");
//        System.out.print("TargetBearing: [");
//        index = numSonarInput;
//        for (int i = 0; i < numBearingInput; i++)
//            System.out.print(df.format(x[index + i]) + ", ");
//        System.out.println(df.format(x[index + numBearingInput - 1]) + "]");
//
//    }
//
//}
