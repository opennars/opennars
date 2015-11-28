package automenta.falcon;

import jurls.reinforcementlearning.domains.RLEnvironment;

public abstract class AGENT {

    public final static int RFALCON = 0;
    public final static int TDFALCON = 1;
    public final static int BPN = 2;

    public final static int QLEARNING = 0;
    public final static int SARSA = 1;

    // variable for Q learning

    //public double QAlpha = (double) 0.5;

    //  default QGamma suitable for Immediate Reward Scheme
//	To be overriden for Delayed reward 
    public double QGamma = (double) 0.1;


    public double initialQ = (double) 0.5;

    // 	default QEpsilonDecay and initialQEpsilon for TD-FALCON
// 	The values are to be override when using RFALCON, BPN and DNDP in the setParameters method     
    public double QEpsilonDecay = (double) 0.005;
    public double QEpsilon = (double) 0.50000;
    public double minQEpsilon = (double) 0.05;

    public boolean forgetting = true;

    public static boolean INTERFLAG = false;
    //   public static boolean detect_loop=false;
    //   public static boolean look_ahead =false;
    public boolean Trace = false;

    abstract public void saveAgent(String outfile);

    abstract public void checkAgent(String outfile);

    abstract public void init(int AVTYPE, boolean ImmediateReward);

    abstract public void setAction(int action);

    abstract public void initAction();

    abstract public void resetAction();



    abstract public int doSearchAction(int mode, int type);

    abstract public int act(boolean train, RLEnvironment env);

    abstract public int actDirect(RLEnvironment env, boolean train);

    abstract public int doDirectAccessAction(boolean train, RLEnvironment env);

    abstract public void doLearnACN();

    abstract public void setprev_J();

    abstract public double computeJ(RLEnvironment env);

    abstract public void setNextJ(double J);

    abstract public void turn(int d);

    abstract public void move(int d, boolean actual);

    abstract public double doSearchQValue(int mode, int type);

    abstract public double getMaxQValue(int method, boolean train, RLEnvironment env);

    abstract public void setReward(double reward);

    abstract public double getPrevReward();

    abstract public void setPrevReward(double reward);

    abstract public void init_path(int maxStep);

    abstract public void setTrace(boolean trace);

    abstract public int getNumCode();

    abstract public int getCapacity();

    abstract public void decay();

    abstract public void prune();

    abstract public void purge();

    abstract public void reinforce();

    abstract public void penalize();

    public void age() {
        if (QEpsilon > minQEpsilon)
            QEpsilon -= QEpsilonDecay;
    }

//    /*
// *	Method simulating the sense-act-learn cycle of TD-FALCON and BPN
// */
//    private void doStep(RLEnvironment env, double lastReward, double rwd, boolean last) {
//
//        final AGENT ag = this;
//
//
//        int k;
//        final int PERFORM = 0;
//        final int LEARN = 1;
//        final int INSERT = 2;
//
//        final int type = 0;  //0-fuzzART 1-ART2
//
//        double this_Q;
//        double max_Q = 0.0;
//        double new_Q = 0.0;
//
//        double[] this_Sonar = new double[5];
//        double[] that_Sonar = new double[5];
//        double[] this_AVSonar = new double[5];
//        double[] that_AVSonar = new double[5];
//
//        int x, y, px, py;
//        int action;
//        int this_bearing;
//        double this_targetRange;
//
//        if (Trace)
//            System.out.println("\nSelecting action ....");
//
//        ag.setPrevReward(rwd);
//
//
//
//        //do {
//
//
//                action = ag.doDirectAccessAction(true, env);   // action is from 0 to numAction
//            //else
//                action = ag.doSelectValidAction(true, env);        // action is from 0 to numAction
//
//
//        //} while (action == -1);
//
//        final double r;
//
//        if (action != -1) {
//            env.takeAction(action);
//            r = env.getReward();
//        }
//        else {
//            r = 0;
//        }
//
//
//        @Deprecated final boolean ImmediateReward = true;
//
//        //	Calculate new Q value from reward function if possible
//        boolean new_Q_value_assigned = true;
//        if (ImmediateReward) {
//            if (r == 1.0) new_Q = 1.0;
//            else if (r == 0.0) new_Q = 0.0;
//            else if (ImmediateReward) new_Q = r;
//            else new_Q_value_assigned = false;
//        }
////        } else
////            new_Q_value_assigned = false;
////
////        //	Estimate new Q value through TD formula
////        if (!new_Q_value_assigned) {
////
////            ag.setAction(action);
////            this_Q = ag.doSearchQValue(PERFORM, type);
////
////            double[] new_sonar = new double[5];
////            maze.getSonar(agt, new_sonar);
////            double[] new_av_sonar = new double[5];
////            maze.getAVSonar(agt, new_av_sonar);
////
////            int new_target_bearing = maze.getTargetBearing(agt);
////            int new_current_bearing = maze.getCurrentBearing(agt);
////            double new_target_range = maze.getTargetRange(agt);
////
////            ((MineAGENT)ag).setState(new_sonar, new_av_sonar, (8 + new_target_bearing - new_current_bearing) % 8, new_target_range);
////
////
////            max_Q = ag.getMaxQValue(TDMethod, true, maze);
////
////            // learn QValue for this state and action
////            if (Bound == false) {
////                new_Q = this_Q + ag.QAlpha * (r + ag.QGamma * max_Q - this_Q);//Q-FALCON or S-FALCON
////                // thresholding - limit the Q value to 0 and 1
////                // new_Q = 1.0/(double) (1.0 + (double) Math.exp (-5*(new_Q-0.5)));
////
////                if (AVTYPE == AGENT.TDFALCON) {
////                    if (new_Q < 0) new_Q = 0;
////                    if (new_Q > 1) new_Q = 1;
////                }
////            } else {
////                new_Q = this_Q + ag.QAlpha * (r + ag.QGamma * max_Q - this_Q) * (1 - this_Q);//BQ-FALCON or BS-FALCON
////
////                if (new_Q < 0 || new_Q > 1) {
////                    System.out.println("*** Bounded rule breached *** ");
////                    System.out.println("r = " + r + " this_Q = " + this_Q + " max_Q = " + max_Q + " new_Q = " + new_Q);
////                }
////
////            }
////        }
//
//        // Learning with state, action, and Q_value
//
//        if (Trace)
//            System.out.println("\nLearning state action value ....");
//
//        //((MineAGENT)ag).setState(this_Sonar, this_AVSonar, this_bearing, this_targetRange); //set back to old state
//        ag.setAction(action);
//        ag.setReward(new_Q); //new_Q = R, so has no effect
//
//        //if (ag.direct_access)
//            ag.doSearchAction(LEARN, type);
//        /*else
//            ag.doSearchQValue(LEARN, type);*/
//
//        if (Trace) System.out.println("Action = " + action + " Reward = " + r +
//                " new_Q = " + new_Q + " max_Q = " + max_Q);
//
//        ag.decay();
//    }

}
