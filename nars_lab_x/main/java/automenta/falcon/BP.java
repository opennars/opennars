package automenta.falcon; /******************************************************************************
 * ====================================================
 * Network:      Backpropagation Network with Bias Terms and Momentum
 * ====================================================
 * <p>
 * Author:       Originally written by Karsten Kutza for Time Series Forecasting
 * Adpated for reinforcement learning and minefield navigation.
 * <p>
 * <p>
 * Reference:    D.E. Rumelhart, G.E. Hinton, R.J. Williams
 * Learning Internal Representations by Error Propagation
 * in:
 * D.E. Rumelhart, J.L. McClelland (Eds.)
 * Parallel Distributed Processing, Volume 1
 * MIT Press, Cambridge, MA, pp. 318-362, 1986
 ******************************************************************************/



import jurls.reinforcementlearning.domains.RLEnvironment;

import java.io.PrintWriter;



public class BP extends AGENT {
    public static final int NUM_LAYERS = 3;
    public static final int N = 18;
    public static final int H = 36;
    public static final int M = 1;
    final int numSpace = 4; // 0-State 1-Action 2-Reward 3-New State
    final int numSonarInput = 5;
    final int numAVSonarInput = 0;
    final int numBearingInput = 8;
    final int numRangeInput = 0;
    final int numAction = 5;
    final int numReward = 2;
    final int complementCoding = 1;
    final int PERFORM = 0;
    final int LEARN = 1;
    final int INSERT = 2;
    protected final int agent_num;
    private final int[] current;
    public boolean detect_loop = false;
    public boolean look_ahead = false;
    double[] Input;        /* - Input of ith unit                  */
    double[] Output;       /* - Output of ith unit                 */
    double[] action;
    LAYER[] Layer;         /* - layers of this net                 */

//    public static boolean INTERFLAG=false;

	/* A bpn_xor */
    double Alpha = 0.5;       /* - momentum factor                    */
    double Eta = 0.25;      /* - learning rate                      */
    double Gain = 1.0;       /* - gain of sigmoid function           */
    double Error;           /* - total net error                    */
    double MinTestError;
    boolean train_flag = true;
    double[] Target;
    double Mean;
    double TrainError;
    double TrainErrorPredictingMean;
    double TestError;
    double TestErrorPredictingMean;
    private int targetBearing;
    private double prevReward = 0;
    private boolean Trace = false;
    private boolean end_state;
    private int max_step;
    private int step;
    private int currentBearing;
    private int[][] path;

    public BP(int av_num) {
        agent_num = av_num;
        MinTestError = 999999.99;
        Input = new double[numSonarInput + numAVSonarInput + numBearingInput + numAction];
        action = new double[numAction];
        Output = new double[M];
        Target = new double[M];
        GenerateNetwork();
        RandomWeights();
        current = new int[2];
    }

    ;

    public int doDirectAccessAction(boolean train, RLEnvironment env) {
        return 0;
    }

    ;

    public void checkAgent(String outfile) {
    }

    ;

    public void saveAgent(String outfile) {
    }

    public void init(int AVTYPE, boolean immediateReward) {
        QEpsilonDecay = 0.00001;
        QEpsilon = 0.50000;

        if (immediateReward)
            QGamma = 0.9;
        else
            QGamma = 0.9;
    }

    public void setState(double[] sonar, double[] av_sonar, int bearing, double range) {
        int index;

        for (int i = 0; i < numSonarInput; i++)
            Input[i] = sonar[i];

        index = numSonarInput;
        for (int i = 0; i < numAVSonarInput; i++)
            Input[index + i] = av_sonar[i];

        index += numAVSonarInput;
        for (int i = 0; i < numBearingInput; i++)
            Input[index + i] = 0.0;
        Input[index + bearing] = 1.0;
    }

    public void initAction() {
        for (int i = 0; i < numAction; i++)
            action[i] = 1;
    }

    public void init_path(int maxstep) {
        int k;

        max_step = maxstep;
        step = 0;
        currentBearing = 0;
        path = new int[max_step + 1][2];
        for (k = 0; k < 2; k++) {
            current[k] = 0;
            path[step][k] = 0;
        }
    }

    /*
        private int get_except_action()
        {
            int rep_step;
            int a;
            int [] new_pos;

            new_pos = new int[2];
            rep_step = loop_path();
            if( rep_step < 0 )
                return( -1 );
            for( a = 0; a < numAction; a++ )
            {
                virtual_move( a - 2, new_pos );
                if( ( new_pos[0] == path[rep_step+1][0] ) && ( new_pos[1] == path[rep_step+1][1] ) )
                    return( a );
            }
            return( -1 );
        }
    */
    public void resetAction() {
        for (int i = 0; i < numAction; i++)
            action[i] = 1 - action[i];
    }

    public void setAction(int action) {
        int index = numSonarInput + numAVSonarInput + numBearingInput;
        for (int i = 0; i < numAction; i++)
            Input[index + i] = 0;
        Input[index + action] = 1.0;
    }

    public void setReward(double r) {
        Target[0] = r;
    }

    public void initReward() {
        Target[0] = 1;
    }

    public void setNewState(double[] sonar, double[] av_sonar, int bearing, double range) {
        for (int i = 0; i < numSonarInput; i++) {
            Input[i] = sonar[i];
        }

        int index = numSonarInput;
        for (int i = 0; i < numAVSonarInput; i++) {
            Input[index + i] = av_sonar[i];
        }

        index += numAVSonarInput;
        for (int i = 0; i < numBearingInput; i++)
            Input[index + i] = 0.0;
        Input[index + bearing] = 1.0;
    }

    public double doSearchQValue(int mode, int type) {
        double QValue = 0.0;

        if (mode == LEARN)
            SimulateNet(Input, Output, Target, true);
        else if (mode == PERFORM)
            SimulateNet(Input, Output, Target, false);

        QValue = Output[0];

        return (QValue);
    }

    public double getMaxQValue(int method, boolean train, RLEnvironment env) {
        int QLEARNING = 0;
        int SARSA = 1;
        double return_Q = 0.0;

        if (method == QLEARNING) {                   //q learning
            for (int i = 0; i < numAction; i++) {
                setAction(i);
                double tmp_Q = doSearchQValue(PERFORM, 0);

                if (tmp_Q > return_Q)
                    return_Q = tmp_Q;
            }
        } else {                               //sarsa
            int next_a = act(train, env);
            setAction(next_a);  // set action
            return_Q = doSearchQValue(PERFORM, 0);
        }

        return return_Q;
    }

    /*    private int loop_path()
        {
            int k;

            for( k = ( step - 1 ); k >= 0; k-- )
                if( ( current[0] == path[k][0] ) && ( current[1] == path[k][1] ) )
                    return( k );
            return( -1 );
        }
    */
    public int actDirect(RLEnvironment env, boolean train) {
        double d;
        double[] qValues = new double[numAction];
        int selectedAction = -1;
        int except_action = -1;
        int k;

        //get qValues for all available actions

        int[] validActions = new int[qValues.length];
        int maxVA = 0;

        for (int i = 0; i < numAction; i++) {
            /*if (maze.withinField(agent_num, i - 2) == false) {
                qValues[i] = -1.0;
            } else */{
                setAction(i);
                qValues[i] = doSearchQValue(PERFORM, 0);
                validActions[maxVA] = i;
                maxVA++;
            }
        }

        if (maxVA == 0)
            return (-1);

        // Explore
        if (Math.random() < QEpsilon && train == true) {
            // Select random action if all qValues == 0 or exploring.
            if (Trace)
                System.out.println("random action selected!");
            int randomIndex = (int) (Math.random() * maxVA);
            selectedAction = validActions[randomIndex];
            ;
        } else {
            double maxQ = -Double.MAX_VALUE;
            int[] doubleValues = new int[qValues.length];
            int maxDV = 0;

            for (int vAction = 0; vAction < maxVA; vAction++) {
                int action = validActions[vAction];

                if (qValues[action] > maxQ) {
                    selectedAction = action;
                    maxQ = qValues[action];
                    maxDV = 0;
                    doubleValues[maxDV] = selectedAction;
                } else if (qValues[action] == maxQ) {
                    maxDV++;
                    doubleValues[maxDV] = action;
                }
            }

            if (maxDV > 0) {
                int randomIndex = (int) (Math.random() * (maxDV + 1));
                selectedAction = doubleValues[randomIndex];
            }
        }

        if (selectedAction == -1)
            System.out.println("No action selected");

        return selectedAction;
    }

    public int act(boolean train, RLEnvironment env) {
        double d;
        double[] qValues = new double[numAction];
        int selectedAction = -1;
        int except_action;
        int k;

//        except_action = get_except_action();
        //get qValues for all available actions
        for (int i = 0; i < numAction; i++) {
            setAction(i);
            qValues[i] = doSearchQValue(PERFORM, 0);
        }

        double maxQ = -Double.MAX_VALUE;
        int[] doubleValues = new int[qValues.length];
        int maxDV = 0;

        // Explore
        if (Math.random() < QEpsilon && train == true) {
            selectedAction = -1;
        } else {
            for (int action = 0; action < qValues.length; action++) {
                if (qValues[action] > maxQ) {
                    selectedAction = action;
                    maxQ = qValues[action];
                    maxDV = 0;
                    doubleValues[maxDV] = selectedAction;
                } else if (qValues[action] == maxQ) {
                    maxDV++;
                    doubleValues[maxDV] = action;
                }
            }

            if (maxDV > 0) {
                int randomIndex = (int) (Math.random() * (maxDV + 1));
                selectedAction = doubleValues[randomIndex];
            }
        }
        // Select random action if all qValues == 0 or exploring.
        if (selectedAction == -1) {
            if (Trace)
                System.out.println("random action selected!");
/*            do 
            {
                selectedAction = (int) (Math.random() * qValues.length);
            }
            while( selectedAction == except_action );*/
        }

        return selectedAction;
    }

    public int doSearchAction(int mode, int type) {
        return (-1);
    }

    public void virtual_move(int a, int[] res) {
        int k;
        int bearing = (currentBearing + a + 8) % 8;

        res[0] = current[0];
        res[1] = current[1];

        switch (bearing) {
            case 0:
                res[1]--;
                break;
            case 1:
                res[0]++;
                res[1]--;
                break;
            case 2:
                res[0]++;
                break;
            case 3:
                res[0]++;
                res[1]++;
                break;
            case 4:
                res[1]++;
                break;
            case 5:
                res[0]--;
                res[1]++;
                break;
            case 6:
                res[0]--;
                break;
            case 7:
                res[0]--;
                res[1]--;
                break;
            default:
                break;
        }
        return;
    }

    public void turn(int d) {
        currentBearing = (currentBearing + d + 8) % 8;
    }

    public void move(int a, boolean succ) {
        int k;

        currentBearing = (currentBearing + a + 8) % 8;
        ++step;
        if (!succ) {
            path[step][0] = current[0];
            path[step][1] = current[1];
            return;
        }
        switch (currentBearing) {
            case 0:
                current[1]--;
                break;
            case 1:
                current[0]++;
                current[1]--;
                break;
            case 2:
                current[0]++;
                break;
            case 3:
                current[0]++;
                current[1]++;
                break;
            case 4:
                current[1]++;
                break;
            case 5:
                current[0]--;
                current[1]++;
                break;
            case 6:
                current[0]--;
                break;
            case 7:
                current[0]--;
                current[1]--;
                break;
            default:
                break;
        }
        path[step][0] = current[0];
        path[step][1] = current[1];
        return;
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

    double RandomEqualREAL(double Low, double High) {
        return (Math.random() * (High - Low) + Low);
    }

    void RandomWeights() {
        int l, i, j;

        /* set all weights randomly */
        for (l = 1; l < NUM_LAYERS; l++) {
            for (i = 1; i <= Layer[l].Units; i++) {
                for (j = 0; j <= Layer[l - 1].Units; j++) {
                    Layer[l].Weight[i][j] = RandomEqualREAL(-0.5, 0.5);
                }
            }
        }
    }

    void SetInput(double[] Input) {
        for (int i = 1; i <= Layer[0].Units; i++) {
            Layer[0].Output[i] = Input[i - 1];
        }
    }

    void GenerateNetwork() {
        Layer = new LAYER[NUM_LAYERS];
        Layer[0] = new LAYER(N, 0);
        Layer[1] = new LAYER(H, N);
        Layer[2] = new LAYER(M, H);
    }

    public void GetOutput(double[] Output) {
        for (int i = 1; i <= Layer[2].Units; i++) {
            Output[i - 1] = Layer[2].Output[i];
        }
    }

    public void BackpropagateLayer(int Upper, int Lower) {
        int i, j;
        double Out, Err;

        for (i = 1; i <= Layer[Lower].Units; i++) {
            Out = Layer[Lower].Output[i];
            Err = 0;
            for (j = 1; j <= Layer[Upper].Units; j++) {
                Err += Layer[Upper].Weight[j][i] * Layer[Upper].Error[j];
            }
            Layer[Lower].Error[i] = this.Gain * Out * (1 - Out) * Err;
        }
    }

    public void BackpropagateNet() {
        for (int l = NUM_LAYERS - 1; l > 1; l--) {
            BackpropagateLayer(l, l - 1);
        }
    }

    public void AdjustWeights() {
        int l, i, j;
        double Out, Err, dWeight;

        for (l = 1; l < NUM_LAYERS; l++) {
            for (i = 1; i <= Layer[l].Units; i++) {
                for (j = 0; j <= Layer[l - 1].Units; j++) {
                    Out = Layer[l - 1].Output[j];
                    Err = Layer[l].Error[i];
                    dWeight = Layer[l].dWeight[i][j];
                    Layer[l].Weight[i][j] += this.Eta * Err * Out + this.Alpha * dWeight;
                    Layer[l].dWeight[i][j] = this.Eta * Err * Out;
                }
            }
        }
    }

    public void print_weights(PrintWriter pw_code) {
        int l, i, j;

        for (l = 1; l < NUM_LAYERS; l++) {
            for (i = 1; i <= Layer[l].Units; i++) {
                for (j = 0; j <= Layer[l - 1].Units; j++) {
                    pw_code.println("Layer[" + l + "].Weight[" + i + "][" + j + "] = " + Layer[l].Weight[i][j]);
                }
            }
        }
        pw_code.println("TestError = " + TestError + ", MinTestError = " + MinTestError + ", train_flag = " + train_flag);
        for (int k = 1; k <= Layer[0].Units; k++)
            pw_code.println("Input[" + (k - 1) + "] = " + Layer[0].Output[k]);
        pw_code.println("Output[0] = " + Layer[2].Output[1] + ", Target[0] = " + this.Target[0]);
    }

    int RandomEqualINT(int Low, int High) {
        return ((int) (Math.random() * (High - Low) + Low));
    }

    public void ComputeOutputError(double[] Target) {
        int i;
        double Out, Err;

        this.Error = 0.0;
        for (i = 1; i <= Layer[2].Units; i++) {
            Out = Layer[2].Output[i];
            Err = Target[i - 1] - Out;
            Layer[2].Error[i] = Gain * Out * (1 - Out) * Err;
            this.Error += 0.5 * Math.pow(Err, 2.0);
        }
    }

    void SimulateNet(double[] Input, double[] Output, double[] Target, boolean Training) {
        SetInput(Input);
        PropagateNet();
        GetOutput(Output);

        ComputeOutputError(Target);
        if (Training) {
            BackpropagateNet();
            AdjustWeights();
        }
    }

    public void decay() {
    }

    public void prune() {
    }

    public void purge() {
    }

    public void penalize() {
    }

    public void reinforce() {
    }

    public int getNumCode() {
        return (H);
    }

    public int getCapacity() {
        return (H);
    }

    public void PropagateLayer(int Lower, int Upper) {
        int i, j;
        double Sum;

        for (i = 1; i <= Layer[Upper].Units; i++) {
            Sum = 0;
            for (j = 0; j <= Layer[Lower].Units; j++)
                Sum += Layer[Upper].Weight[i][j] * Layer[Lower].Output[j];
            if (Upper >= 1)  // was Upper==2
                Layer[Upper].Output[i] = 1 / (1 + Math.exp(-this.Gain * Sum));
            else
                Layer[Upper].Output[i] = Sum;
        }
    }

    public void PropagateNet() {
        for (int l = 0; l < NUM_LAYERS - 1; l++) {
            PropagateLayer(l, l + 1);
        }
    }

    public void SaveWeights() {
        int l, i, j;

        for (l = 1; l < NUM_LAYERS; l++) {
            for (i = 1; i <= Layer[l].Units; i++) {
                for (j = 0; j <= Layer[l - 1].Units; j++) {
                    Layer[l].WeightSave[i][j] = Layer[l].Weight[i][j];
                }
            }
        }
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

    ;
}

