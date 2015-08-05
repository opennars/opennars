package automenta.falcon;

import jurls.reinforcementlearning.domains.RLEnvironment;

public abstract class AGENT {
    // variable for Q learning

    public static double QAlpha = (double) 0.5;

    //  default QGamma suitable for Immediate Reward Scheme
//	To be overriden for Delayed reward 
    public static double QGamma = (double) 0.1;

    public static double minQEpsilon = (double) 0.00500;
    public static double initialQ = (double) 0.5;

    // 	default QEpsilonDecay and initialQEpsilon for TD-FALCON
// 	The values are to be override when using RFALCON, BPN and DNDP in the setParameters method     
    public static double QEpsilonDecay = (double) 0.00050;
    public static double QEpsilon = (double) 0.50000;

    public static boolean direct_access = false;
    public static boolean forgetting = false;
    public static boolean INTERFLAG = false;
    //   public static boolean detect_loop=false;
    //   public static boolean look_ahead =false;
    public static boolean Trace = true;

    abstract public void saveAgent(String outfile);

    abstract public void checkAgent(String outfile);

    abstract public void setParameters(int AVTYPE, boolean ImmediateReward);

    abstract public void setAction(int action);

    abstract public void initAction();

    abstract public void resetAction();

    abstract public void setState(double[] sonar, double[] av_sonar, int bearing, double range);

    abstract public void setNewState(double[] sonar, double[] av_sonar, int bearing, double range);

    abstract public int doSearchAction(int mode, int type);

    abstract public int doSelectAction(boolean train, RLEnvironment maze);

    abstract public int doSelectValidAction(boolean train, RLEnvironment maze);

    abstract public int doDirectAccessAction(int agt, boolean train, RLEnvironment maze);

    abstract public void doLearnACN();

    abstract public void setprev_J();

    abstract public double computeJ(RLEnvironment maze);

    abstract public void setNextJ(double J);

    abstract public void turn(int d);

    abstract public void move(int d, boolean actual);

    abstract public double doSearchQValue(int mode, int type);

    abstract public double getMaxQValue(int method, boolean train, RLEnvironment maze);

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
}
