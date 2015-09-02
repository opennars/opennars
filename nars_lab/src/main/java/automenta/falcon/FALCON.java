package automenta.falcon;


import jurls.reinforcementlearning.domains.RLEnvironment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;

public class FALCON extends AGENT {
    final static int RFALCON = 0;
    final static int TDFALCON = 1;


    public final int FUZZYART = 0;
    public final int ART2 = 1;

    int mode = ART2;


    final int numSpace = 4; // 0-State 1-Action 2-Reward 3-New State

    final int numReward = 2;
    final int complementCoding = 1;

    final int CURSTATE = 0;
    final int ACTION = 1;
    final int REWARD = 2;
    final int NEWSTATE = 3;
    public final int PERFORM = 0;
    final int LEARN = 1;
    final int INSERT = 2;
    private final int[] numInput;
    private final double[][] activityF1;
    private final int KMax = 3;
    private final double initConfidence = 0.5;
    private final double reinforce_rate = (double) 0.5;
    private final double penalize_rate = (double) 0.2;
    private final double decay_rate = (double) 0.0005;
    private final double threshold = (double) 0.01;
    private final int capacity = 9999;
    private final double beta = (double)0.5f;
    private static final double epilson = (double) 0.000001;

    private final double[] gamma = {1.0, 1.0, 1.0, 0.0};


    private final double[] alpha = {0.1, 0.1, 0.1};
    //private final double[] b_rho = {0.2, 0.2, 0.5, 0.0}; // fuzzy ART baseline vigilances


    // Action enumeration
    private final double[] p_rho = {0.0, 0.0, 0.0, 0.0}; // fuzzy ART performance vigilances

    // Direct Access
    /*
	private double alpha[]={(double)0.001,  (double)0.001, (double)0.001, (double)0.001};
	private double b_rho[]={(double)0.25, (double)0.1, (double)0.5, (double)0.0}; // fuzzy ART baseline vigilances
	private double p_rho[]={(double)0.25, (double)0.1, (double)0.5, (double)0.0}; // fuzzy ART performance vigilances
	*/
    // ART 2 Parameter Setting

    private double b_rho[]={(double)0.5,(double)0.2,(double)0.0,(double)0.0};

    public static final NumberFormat df = NumberFormat.getInstance();
    private final RLEnvironment env;
    private int numCode;
    private double prevReward = 0;
    private double[] activityF2;
    private double[][][] weight;
    private int J;
    private boolean[] newCode;
    private double[] confidence;
    private boolean end_state;
    private int max_step;
    private int step;

    public FALCON(RLEnvironment env) {

        this.env = env;

        df.setMaximumFractionDigits(1);

        numInput = new int[numSpace];
        numInput[0] = env.numStates();
        numInput[1] = env.numActions();
        numInput[2] = numReward;
        numInput[3] = numInput[0];

        activityF1 = new double[numSpace][];
        for (int i = 0; i < numSpace; i++)
            activityF1[i] = new double[numInput[i]];

        numCode = 0;
        newCode = new boolean[numCode + 1];
        newCode[0] = true;

        confidence = new double[numCode + 1];
        confidence[0] = initConfidence;

        activityF2 = new double[numCode + 1];

        weight = new double[numCode + 1][][];
        for (int j = 0; j <= numCode; j++) {
            weight[j] = new double[numSpace][];
            for (int k = 0; k < numSpace; k++) {
                weight[j][k] = new double[numInput[k]];
                for (int i = 0; i < numInput[k]; i++)
                    weight[j][k][i] = 1.0;
            }
        }
        end_state = false;


    }

    public void init(int AVTYPE, boolean immediateReward) {

        //clear?



        if (AVTYPE == RFALCON) {
            QEpsilonDecay = 0.00000;
            QEpsilon = 0.00000;
        } else { //  QEpsilonDecay rate for TD-FALCON
            QEpsilonDecay = 0.001;
            QEpsilon = 0.50000;
            minQEpsilon = 0.01;
        }

        if (immediateReward)
            QGamma = 0.5;
        else
            QGamma = 0.9;

        initAction();

        initReward();
        setReward(1);

        //createNewCode();

    }

    public void stop() {
        end_state = true;
    }

    public void checkAgent(String outfile) {
        PrintWriter pw_agent = null;
        boolean invalid;

        try {
            pw_agent = new PrintWriter(new FileOutputStream(outfile), true);
        } catch (IOException ex) {
        }

        pw_agent.println("Number of Codes : " + numCode);
        for (int j = 0; j < numCode; j++) {
            invalid = false;
            for (int i = 0; i < numInput[ACTION]; i++)
                if (weight[j][0][i] == 1 && weight[j][ACTION][i] == 1)
                    invalid = true;

            if (invalid) {
                pw_agent.println("Code " + j);
                for (int k = 0; k < numSpace; k++) {
                    pw_agent.print("Space " + k + " : ");
                    for (int i = 0; i < numInput[k] - 1; i++)
                        pw_agent.print(weight[j][k][i] + ", ");
                    pw_agent.println(weight[j][k][numInput[k] - 1]);
                }
            }
        }
        if (pw_agent != null)
            pw_agent.close();
    }

    public void clean() {
        int numClean = 0;
        for (int j = 0; j < numCode; j++)
            for (int i = 0; i < numInput[ACTION]; i++)
                if (weight[j][0][i] == 1 && weight[j][ACTION][i] == 1) {
                    newCode[j] = true;
                    numClean++;
                }
        if (numClean > 0)
            System.out.println(numClean + " bad code(s) removed.");
    }

    public void saveAgent(String outfile) {
        PrintWriter pw_agent = null;

        try {
            pw_agent = new PrintWriter(new FileOutputStream(outfile), true);
        } catch (IOException ex) {
        }

        pw_agent.println("Number of Codes : " + numCode);
        for (int j = 0; j <= numCode; j++) {
            pw_agent.println("Code " + j);
            for (int k = 0; k < numSpace; k++) {
                pw_agent.print("Space " + k + " : ");
                for (int i = 0; i < numInput[k] - 1; i++)
                    pw_agent.print(weight[j][k][i] + ", ");
                pw_agent.println(weight[j][k][numInput[k] - 1]);
            }
        }
        if (pw_agent != null)
            pw_agent.close();
    }

    public int getNumCode() {
        return (numCode);
    }

    public int getCapacity() {
        return (capacity);
    }

    public void setTrace(boolean t) {
        Trace = t;
    }

    public double getPrevReward() {
        return (prevReward);
    }

    public void setPrevReward(double r) {
        prevReward = r;
    }

    @Override
    public void init_path(int maxStep) {

    }

    public void createNewCode() {
        numCode++;

        activityF2 = new double[numCode + 1];

        boolean[] new_newCode = new boolean[numCode + 1];
        for (int j = 0; j < numCode; j++)
            new_newCode[j] = newCode[j];
        new_newCode[numCode] = true;
        newCode = new_newCode;

        double[] new_confidence = new double[numCode + 1];
        for (int j = 0; j < numCode; j++)
            new_confidence[j] = confidence[j];
        new_confidence[numCode] = initConfidence;
        confidence = new_confidence;

        double[][][] new_weight = new double[numCode + 1][][];
        for (int j = 0; j < numCode; j++)
            new_weight[j] = weight[j];

        new_weight[numCode] = new double[numSpace][];
        for (int k = 0; k < numSpace; k++) {
            new_weight[numCode][k] = new double[numInput[k]];
            for (int i = 0; i < numInput[k]; i++)
                new_weight[numCode][k][i] = 1.0;
        }
        weight = new_weight;
    }



    public void reinforce() {
        confidence[J] += (1.0 - confidence[J]) * reinforce_rate;
    }

    public void penalize() {
        confidence[J] -= confidence[J] * penalize_rate;
    }

    public void decay() {
        for (int j = 0; j < numCode; j++)
            confidence[j] -= confidence[j] * decay_rate;
    }

    public void prune() {
        for (int j = 0; j < numCode; j++)
            if (confidence[j] < threshold)
                newCode[j] = true;
    }

    public void purge() {
        int numPurge = 0;

        for (int j = 0; j < numCode; j++)
            if (newCode[j] == true)
                numPurge++;

        if (numPurge > 0) {
            double[][][] new_weight = new double[numCode - numPurge + 1][][];
            boolean[] new_newCode = new boolean[numCode - numPurge + 1];
            double[] new_confidence = new double[numCode - numPurge + 1];

            System.out.print("Total of " + numCode + " rule(s) created. ");
            int k = 0;
            for (int j = 0; j < numCode; j++)
                if (newCode[j] == false) {
                    new_weight[k] = weight[j];
                    new_newCode[k] = newCode[j];
                    new_confidence[k] = confidence[j];
                    k++;
                }
            new_weight[numCode - numPurge] = weight[numCode];
            new_newCode[numCode - numPurge] = newCode[numCode];
            new_confidence[numCode - numPurge] = confidence[numCode];

            weight = new_weight;
            newCode = new_newCode;
            confidence = confidence;

            numCode -= numPurge;
            activityF2 = new double[numCode + 1];
            System.out.println(numPurge + " rule(s) purged.");
        }
    }



    public void initAction() {
        for (int i = 0; i < numInput[ACTION]; i++)
            activityF1[ACTION][i] = 1;
    }


    public void resetAction() {
        for (int i = 0; i < numInput[ACTION]; i++)
            activityF1[ACTION][i] = 1 - activityF1[ACTION][i];
    }

    public void setAction(int action) {
        for (int i = 0; i < numInput[ACTION]; i++)
            activityF1[ACTION][i] = 0;
        activityF1[ACTION][action] = 1.0;
    }

    public void setReward(double r) {
        activityF1[REWARD][0] = r;
        activityF1[REWARD][1] = 1 - r;
    }

    public void initReward() {
        activityF1[REWARD][0] = 1;
        activityF1[REWARD][1] = 1;
    }

    public double[] getState() { return activityF1[0]; }


    protected double[] getNewState() {
        return activityF1[NEWSTATE];
    }

    public void computeChoice(int type, int numSpace) {
        double top, bottom;

        if (type == FUZZYART) {
            for (int j = 0; j <= numCode; j++) {
                activityF2[j] = 0.0;
                for (int k = 0; k < numSpace; k++)
                //        	if (gamma[k]>0.0)
                {
                    top = 0;
                    bottom = alpha[k];
                    for (int i = 0; i < numInput[k]; i++) {
                        top += Math.min(activityF1[k][i], weight[j][k][i]);
                        bottom += weight[j][k][i];
                    }
//                    activityF2[j] *= (double)(top/bottom);  // product rule, does not work
                    activityF2[j] += gamma[k] * top / bottom;
                }
//              System.out.println( "F["+j+"] = " + activityF2[j] );
            }
        } else if (type == ART2) {
            for (int j = 0; j <= numCode; j++) {
                activityF2[j] = 0.0;
                for (int k = 0; k < numSpace; k++) {
                    top = 0;
                    for (int i = 0; i < numInput[k]; i++)
                        top += activityF1[k][i] * weight[j][k][i];
                    top /= numInput[k];
                    activityF2[j] += top;
                }
            }
        }
    }

    public int doChoice() {
        double max_act = Double.NEGATIVE_INFINITY;
        int c = -1;

        for (int j = 0; j <= numCode; j++) {
            if (activityF2[j] > max_act) {
                max_act = activityF2[j];
                c = j;
            }
        }
        return (c);
    }

    public static boolean isNull(double[] x, int n) {
        for (int i = 0; i < n; i++)
            if (!isZero(x[i])) return (false);
        return (true);
    }

    private static boolean isZero(final double v) {
        return Math.abs(v) < epilson;
    }
    private static boolean isOne(final double v) {
        return isZero(v-1.0);
    }

    public double getMatch(int k, int j) {

        double m = 0.0;
        double denominator = 0.0;

        if (isNull(activityF1[k], numInput[k])) {
            return (1);
        }

        final double[] ww = weight[j][k];
        final double[] acki = activityF1[k];

        for (int i = 0; i < numInput[k]; i++) {
            final double a = Math.abs(acki[i]);
            final double w = Math.abs(ww[i]);
            m += Math.min(a, w);
            denominator += a;
        }
//      System.out.println ("Code "+j+ " match "+m/denominator);
        if (isZero(denominator))
            return (1);
        return (m / denominator);
    }

    public void doComplete(int j, int k) {
        for (int i = 0; i < numInput[k]; i++)
            activityF1[k][i] = weight[j][k][i];
    }

    public void doInhibit(int j, int k) {
        for (int i = 0; i < numInput[k]; i++)
            if (weight[j][k][i] == 1)
                activityF1[k][i] = 0;
    }

    public int doSelect(int k) {
        int winner = 0;
        double max_act = 0;

        for (int i = 0; i < numInput[k]; i++) {
            if (activityF1[k][i] > max_act) {
                max_act = activityF1[k][i];
                winner = i;
            }
        }

        for (int i = 0; i < numInput[k]; i++)
            activityF1[k][i] = 0;
        activityF1[k][winner] = 1;
        return (winner);
    }

    public void doLearn(int J, int type) {
        final double learningRate;

        if (!newCode[J] || numCode < capacity) {

            if (newCode[J]) {
                learningRate = 1;
            }
            else
                learningRate = beta; //*Math.abs(r-reward);

            for (int k = 0; k < numSpace; k++) {

                final double[] ack = activityF1[k];
                final double[] wjk = weight[J][k];

                for (int i = 0; i < numInput[k]; i++) {

                    final double w = wjk[i];

                    if (type == FUZZYART) {
                        wjk[i] = (1 - learningRate) * w +
                                learningRate * Math.min(w, ack[i]);
                    }
                    else if (type == ART2)
                        wjk[i] = (1 - learningRate) * w +
                                learningRate * ack[i];
                }
            }

            if (newCode[J]) {
                newCode[J] = false;
                //System.out.println("new code " + J + " " + numCode);
                createNewCode();
            }
        }
    }

    public void doOverwrite(int J) {

        for (int k = 0; k < numSpace; k++) {
            for (int i = 0; i < numInput[k]; i++)
                weight[J][k][i] = activityF1[k][i];
        }
    }

    public void displayActivity(int k) {
        System.out.print("Space " + k + " : ");
        for (int i = 0; i < numInput[k] - 1; i++)
            System.out.print(df.format(activityF1[k][i]) + ", ");
        System.out.println(df.format(activityF1[k][numInput[k] - 1]));
    }

    public void displayActivity2(PrintWriter pw, int k) {
        //pw.print("AV" + agentID + " Space " + k + " : ");
        for (int i = 0; i < numInput[k] - 1; i++)
            pw.print(df.format(activityF1[k][i]) + ", ");
        pw.println(df.format(activityF1[k][numInput[k] - 1]));
    }

    public static void displayVector(String s, double[] x, int n) {
        System.out.print(s + " : ");
        for (int i = 0; i < n - 1; i++)
            System.out.print(df.format(x[i]) + ", ");
        System.out.println(df.format(x[n - 1]));
    }


    public static void displayVector2(PrintWriter pw, String s, double[] x, int n) {
        //pw.print("AV" + agentID + " " + s + " : ");
        for (int i = 0; i < n - 1; i++)
            pw.print(df.format(x[i]) + ", ");
        pw.println(df.format(x[n - 1]));
    }

    public double doSearchQValue(int mode, int type) {
        boolean reset = true, perfectMismatch = false;

        double[] rho = new double[4];
        double[] match = new double[4];

        if (mode == INSERT)
            for (int k = 0; k < numSpace; k++)
                rho[k] = 1;
        else if (mode == LEARN)
            for (int k = 0; k < numSpace; k++)
                rho[k] = b_rho[k];
        else if (mode == PERFORM)
            for (int k = 0; k < numSpace; k++)
                rho[k] = p_rho[k];

//        System.out.println ("Running searchQValue:");
        computeChoice(type, 2); //map from state action to reward

        int loops = 10;
        while (numCode > 0 && reset && !perfectMismatch && loops-- >= 0) {
            reset = false;
            J = doChoice();

            for (int k = 0; k < numSpace; k++)
                match[k] = getMatch(k, J);

            if (match[CURSTATE] < rho[CURSTATE] || match[ACTION] < rho[ACTION] || match[REWARD] < rho[REWARD]) {
                if (isOne(match[CURSTATE])) {
                    perfectMismatch = true;
                    if (Trace) System.out.println("Perfect mismatch. Overwrite code " + J);
                } else {
                    activityF2[J] = -1.0;
                    reset = true;

                    for (int k = 0; k < 1; k++) // raise vigilance of State
                        if (match[k] > rho[k])
                            rho[k] = Math.min(match[k] + epilson, 1);
                }
            }
        }
        if (mode == PERFORM) {
            doComplete(J, REWARD);

            final double QValue;

            if (activityF1[REWARD][0] == activityF1[REWARD][1] && activityF1[REWARD][0] == 1) { //initialize Q value
                if (INTERFLAG) QValue = initialQ;
                else QValue = initialQ;
            } else
                QValue = activityF1[REWARD][0];

            return QValue;
        } else if (mode == LEARN) {
            if (!perfectMismatch)
                doLearn(J, type);
            else {
                doOverwrite(J);
            }
        }
        return Double.NaN;
    }



    @Override
    @Deprecated public double getMaxQValue(int method, boolean train, RLEnvironment env) {
        int QLEARNING = 0;
        int SARSA = 1;
        double Q = 0.0;




            if (method == QLEARNING) {                   //q learning
                for (int i = 0; i < env.numActions(); i++) {
                    setAction(i);
                    double tmp_Q = doSearchQValue(PERFORM, mode);
                    if (tmp_Q > Q) Q = tmp_Q;
                }
            } else {                               //sarsa
                int next_a = act(train, env);
                setAction(next_a);  // set action
                Q = doSearchQValue(PERFORM, mode);
            }


        return Q;
    }

    public int doSearchAction(int mode, int type) {
        boolean reset = true, perfectMismatch = false;

        double[] rho = new double[4];
        double[] match = new double[4];

        if (mode == INSERT)
            for (int k = 0; k < numSpace; k++)
                rho[k] = 1;
        else if (mode == LEARN)
            for (int k = 0; k < numSpace; k++)
                rho[k] = b_rho[k];
        else if (mode == PERFORM)
            for (int k = 0; k < numSpace; k++)
                rho[k] = p_rho[k];

//        System.out.println ("Running searchAction");
        if (Trace) {
            //System.out.println("\nInput activities:");
            //displayState("STATE", activityF1[CURSTATE], numInput[CURSTATE]);
            displayActivity(ACTION);
            displayActivity(REWARD);
        }

/*		if (mode==LEARN) {
	        if (Trace) System.out.println ("reward="+r+ " prev="+reward);
			if (r==0 || r<=reward) {
  				if (Trace) System.out.println ("reset action");
  				for (int i=0; i<numInput[ACTION]; i++)
					activityF1[ACTION][i] = 1-activityF1[ACTION][i];
  			}
  			reward = r;
  		}
*/
        computeChoice(type, 1);  /* 1 - choice function is computed based on state only */
								/* 3 - choice function is computed based on state, action, and value */

        int loops = 10;
        while (reset && !perfectMismatch && loops-- >= 0) {
            reset = false;
            J = doChoice();
            for (int k = 0; k < numSpace; k++)
                match[k] = getMatch(k, J);
            if (Trace) {
                System.out.println("winner = " + J);
                /*displayState("weight[J][STATE] ", weight[J][CURSTATE], numInput[CURSTATE]);
                displayVector("weight[J][ACTION]", weight[J][ACTION], numInput[ACTION]);
                displayVector("weight[J][REWARD]", weight[J][REWARD], numInput[REWARD]);*/
                System.out.println("Winner " + J + " act " + df.format(activityF2[J]) +
                        " match[State] = " + df.format(match[CURSTATE]) +
                        " match[action] = " + df.format(match[ACTION]) +
                        " match[reward] = " + df.format(match[REWARD]));
            }

            // Checking match in all channels
            if (match[CURSTATE] < rho[CURSTATE] || match[ACTION] < rho[ACTION] || match[REWARD] < rho[REWARD]) {
                if (isOne(match[CURSTATE])) {
                    perfectMismatch = true;
                    if (Trace) System.out.println("Perfect mismatch. Overwrite code " + J);
                } else {
                    activityF2[J] = (float) -1.0;
                    if (Trace) System.out.println("Reset Winner " + J + " rho[State] " + rho[CURSTATE]
                            + " rho[Action] " + rho[ACTION] + " rho[Reward] " + rho[REWARD]);
                    reset = true;

                    for (int k = 0; k < 1; k++) // raise vigilance of State only
                        if (match[k] > rho[k])
                            rho[k] = Math.min(match[k] + epilson, 1);
                }
            }


        }


        if (mode == PERFORM) {

            final int action;

            if (newCode[J]) {
                action = -1;
            }
            else {
                doComplete(J, ACTION);
                action = doSelect(ACTION);
            }

            return action;

        } else if (mode == LEARN) {

            if (!perfectMismatch)
                doLearn(J, type);
            else
                doOverwrite(J);

        }

        return -1;
    }


    public int act(boolean train, RLEnvironment env) {

        double[] qValues = new double[env.numActions()];
        int selectedAction = -1;



        //get qValues for all available actions
        for (int i = 0; i < env.numActions(); i++) {
            setAction(i);
            qValues[i] = doSearchQValue(PERFORM, mode);
        }



        if (Math.random() < QEpsilon && train == true) {

            //Explore

            if (Trace) System.out.println("exploring");

            selectedAction = -1;

        } else {

            selectedAction = doSearchAction(PERFORM, mode);

            if (selectedAction == -1) {

                double maxQ = Double.NEGATIVE_INFINITY;
                int[] doubleValues = new int[qValues.length];
                int maxDV = 0;

                for (int action = 0; action < qValues.length; action++) {
                    if (qValues[action] > maxQ) {
                        selectedAction = action;
                        maxQ = qValues[action];
                        maxDV = 0;
                        doubleValues[maxDV] = selectedAction;
                    } else if (qValues[action] == maxQ) {
                        doubleValues[++maxDV] = action;
                    }
                }

                if (maxDV > 0) {
                    int randomIndex = (int) (Math.random() * (maxDV ));
                    selectedAction = doubleValues[randomIndex];
                }
            }

        }




        // Select random action if all qValues == 0 or exploring.
        if (selectedAction == -1) {
            if (Trace)
                System.out.println("random action selected!");

            selectedAction = (int) (Math.random() * qValues.length);
        }
        else {
            if (Trace)
                System.out.println("action=" + selectedAction);
        }




        if (selectedAction == -1) {
            System.out.println("No action selected");
        }
        else {

            //System.out.println("execute: " + selectedAction);


            env.takeAction(selectedAction);
            env.frame();

            setReward(env.getReward());
            setNextState(env.observe());
            setAction(selectedAction);

            doSearchQValue(LEARN, mode);
            doSearchAction(LEARN, mode);

            age();

        }


        return selectedAction;
    }

    public int act(boolean train) {
        return act(train, this.env);
    }

    @Deprecated public int actDirect(RLEnvironment env, boolean train) {
        double d;
        int selectedAction = -1;
        int except_action = -1;
        int k;

        initAction();
        setReward( env.getReward() );
        setState( env.observe() );

//        if (detect_loop)
//        except_action = get_except_action();

        //get qValues for all available actions

        int validActionCount = 0;
        int[] validActions = new int[env.numActions()];
        double[] qValues = new double[env.numActions()];

        for (int i = 0; i < validActionCount; i++) {
            if (!validAction(env, i)) {
                qValues[i] = -1.0;
//      			System.out.println ( "action " + i + " invalid");
            } else {
                setAction(i);
                qValues[i] = doSearchQValue(PERFORM, FUZZYART);
                validActions[validActionCount++] = i;
            }
        }

        if (validActionCount == 0) {
// 	        System.out.println ( "current = ("+current[0]+","+current[1]+")  Bearing = " + currentBearing);
// 	        System.out.println ( "*** No valid action *** ");

            return (-1);
        }

        // Explore
        if (Math.random() < QEpsilon && train == true) {
            // Select random action if all qValues == 0 or exploring.
            if (Trace)
                System.out.println("random action selected!");
            int randomIndex = (int) (Math.random() * validActionCount);
            selectedAction = validActions[randomIndex];
            ;
        } else {
            double maxQ = -Double.MAX_VALUE;
            int[] doubleValues = new int[qValues.length];
            int maxDV = 0;

            for (int vAction = 0; vAction < validActionCount; vAction++) {
                int action = validActions[vAction];
//            	System.out.print ( "action[" + action + "] = " + qValues[action]);
/*				if (detect_loop)
                	if( except_action == action )
                    	continue;

//           	System.out.println ( "   nextReward[" + action + "] = " + maze.nextReward (agentID, action-2));
           	    if (look_ahead)
	               	if( maze.nextReward (agentID, action-2) > 0.5) { //add in rules
                    	selectedAction = action;
                        maxDV=0;
                        break;
                	}
*/
                if (qValues[action] > maxQ) {
                    selectedAction = action;
                    maxQ = qValues[action];
                    doubleValues[maxDV] = selectedAction;
                    maxDV = 1;
                } else if (qValues[action] == maxQ) {
                    doubleValues[maxDV] = action;
                    maxDV++;
                }
            }

            if (maxDV > 1) {   // more than 1 with max value
                int randomIndex = (int) (Math.random() * maxDV);
                selectedAction = doubleValues[randomIndex];
            }
//        	System.out.println ( "Best valid action is " + selectedAction + " with maxQ =" + maxQ);
        }

        if (selectedAction == -1) {
            System.out.println("No action selected");
        }
        else {
            env.takeAction(selectedAction);
            age();
        }

        return selectedAction;
    }

    private void setState(double[] observe) {
        System.arraycopy(observe, 0, getState(), 0, observe.length);
    }
    private void setNextState(double[] observe) {
        System.arraycopy(observe, 0, getNewState(), 0, observe.length);
    }

//    private void flipstate() {
//        System.arraycopy(getNewState(), 0, getState(), 0, getState().length);
//    }

    // Direct Access
    @Deprecated public int doDirectAccessAction(boolean train, RLEnvironment env) {
        int selectedAction; // from 0 to 4

        // first try to select an action
        //setState (maze.getSonar(), (maze.getTargetBearing()-maze.getCurrentBearing()+10)%8);
        initAction();   // initialize action to all 1's
        setReward(1);  // search for actions with good reward


        if (Math.random() < QEpsilon ||
                (selectedAction = doSearchAction(PERFORM, FUZZYART)) == -1 || // no close match
                !validAction(env, selectedAction)) { // not valid action

            if (Trace)
                System.out.println("random action selected!");

            int[] validActions = new int[env.numActions()];
            int maxVA = 0;

//            System.out.print ("Valid actions :");
            for (int i = 0; i < env.numActions(); i++)
                if (validAction(env, i - 2)) {   // valid action
//					System.out.print (" " + i);
                    validActions[maxVA] = i;
                    maxVA++;
                }
//            System.out.println (" ");

            if (maxVA > 0) {
                int randomIndex = (int) (Math.random() * maxVA);
                selectedAction = validActions[randomIndex];
            } else
                selectedAction = -1;
        }
        //     	else
        // 	        System.out.println ( "Chosen valid action is " + selectedAction);


//        if (selectedAction==-1)
//   	        System.out.println ( "No action selected");   	    
//   	    if (maze.withinField (agt, selectedAction-2)==false)
//  	        System.out.println("WARNING: selectedaction " + selectedAction + " out of field");

        return selectedAction;
    }

    /**
     * allows domain-specific subclasses to override known invalid actions
     * to preven their selection
     */
    protected boolean validAction(RLEnvironment env, int selectedAction) {
        return true;
    }

    public int[] findKMax(double[] v, int n, int K) {
        int temp;
        double tempf;
        int[] maxIndex = new int[K];
        int[] index = new int[n];

        for (int i = 0; i < n; i++)
            index[i] = i;

        for (int k = 0; k < K; k++) {
            for (int i = n - 1; i > k; i--)
                if (v[i - 1] < v[i]) {
                    tempf = v[i];
                    v[i] = v[i - 1];
                    v[i - 1] = tempf;
                    temp = index[i];
                    index[i] = index[i - 1];
                    index[i - 1] = temp;
                }
            maxIndex[k] = index[k];
        }
        return (maxIndex);
    }

    public boolean doCompleteKMax(int[] k_max) {
        int actualK, j;
        boolean predict = false;

        if (numCode < KMax)
            actualK = numCode;
        else
            actualK = KMax;

        for (int i = 0; i < numInput[ACTION]; i++) {
            activityF1[ACTION][i] = 0;
            for (int k = 0; k < actualK; k++) {
                j = k_max[k];
                if (activityF2[j] > 0.9) {     // threshold of activity for predicting
                    activityF1[ACTION][i] += activityF2[j] *
                            (weight[j][REWARD][0] * weight[j][ACTION][i]                  //good move
                                    - (1 - weight[j][REWARD][0]) * weight[j][ACTION][i]); //bad move
                    predict = true;
                }
            }
        }
        return (predict);
    }

    public int doSelectDualAction(int type) {
        boolean reset = true;
        int action = 0;
        double[] rho = new double[4];
        double[] match = new double[4];
        int[] k_max;

        for (int k = 0; k < numSpace; k++)
            rho[k] = p_rho[k];

        if (Trace) {
            System.out.println("Input activities");
            displayActivity(CURSTATE);
            displayActivity(ACTION);
            displayActivity(REWARD);
        }

        computeChoice(type, 1);
//      for (int j=0; j<numCode; j++)
//          System.out.println ("F2["+j+"]= "+activityF2[j]);

        if (numCode > KMax) {
            k_max = findKMax(activityF2, numCode, KMax);
//          for (int j=0; j<K; j++)
//              System.out.println ("k_max["+j+"]= "+k_max[j]);
            if (!doCompleteKMax(k_max))
                J = doChoice();
        } else
            J = doChoice();

        action = doSelect(ACTION);
        return (action);
    }



    // dummy methods required by abstract AGENT class

    public void doLearnACN() {
    }

    ;

    public void setprev_J() {
    }

    ;

    public double computeJ(RLEnvironment env) {
        return 0.0;
    }

    public void setNextJ(double J) {
    }

    @Override
    public void turn(int d) {

    }

    @Override
    public void move(int d, boolean actual) {

    }


} 
                                
