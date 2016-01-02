package automenta.falcon.minefield;

import jurls.reinforcementlearning.domains.RLEnvironment;

class Maze implements RLEnvironment  {
    final int size = 16;
    final int numMines = 10;
    final boolean binarySonar = false;

    final int LINEAR = 0;
    final int EXP = 1;
    private final int RewardType = EXP; //LINEAR;

    private int agent_num;

    private int[][] current;
    private int[][] prev_current;
    private int[] target;
    private int[] currentBearing;
    private int[] prev_bearing;
    private int[] targetBearing;
    private double[][] sonar;
    private double[][] av_sonar;
    private int[] range;
    private int[][] mines;

    private int[][] avs;
    private boolean[] end_state;
    private boolean[] conflict_state;

    public Maze(int ag_num) {
        refreshMaze(ag_num);
    }

    public void set_conflict(int i, int j) {
        avs[current[i][0]][current[i][1]] = 0;
        end_state[i] = true;
        end_state[j] = true;
        conflict_state[i] = true;
        conflict_state[j] = true;
//        current[i][0] = -1;
//        current[i][1] = -1;
//        current[j][0] = -1;
//        current[j][1] = -1;
    }

    public boolean check_conflict(int i) {
        int k;

        if ((current[i][0] == target[0]) && (current[i][1] == target[1]))
            return (false);
        if (conflict_state[i])
            return (true);
        if ((current[i][0] < 0) || (current[i][1] < 0))
            return (false);
        for (k = 0; k < agent_num; k++) {
            if (k == i)
                continue;
            if ((current[k][0] == current[i][0]) && (current[k][1] == current[i][1])) {
                set_conflict(i, k);
                return (true);
            }
        }
        return (false);
    }

    public boolean check_conflict(int agt, int pos[], boolean actual) {
        int k;

        for (k = 0; k < agent_num; k++) {
            if (k == agt)
                continue;
            if ((current[k][0] == pos[0]) && (current[k][1] == pos[1])) {
                if (actual) {
                    set_conflict(agt, k);
                }
                return (true);
            }
        }
        return (false);
    }

    public void refreshMaze(int agt) {
        int k, w;
        int x, y;
        double d;

        // limit the agent number between 1 and 10
        if (agt < 1)
            agent_num = 1;
        else if (agt > 10)
            agent_num = 10;
        else
            agent_num = agt;
        current = new int[agent_num][];
        target = new int[2];
        prev_current = new int[agent_num][];
        currentBearing = new int[agent_num];
        prev_bearing = new int[agent_num];
        targetBearing = new int[agent_num];
        avs = new int[size][size];
        mines = new int[size][size];
        end_state = new boolean[agent_num];
        conflict_state = new boolean[agent_num];

        sonar = new double[agent_num][];
        av_sonar = new double[agent_num][];

        for (k = 0; k < agent_num; k++) {
            current[k] = new int[2];
            prev_current[k] = new int[2];
            end_state[k] = false;
            conflict_state[k] = false;
            sonar[k] = new double[5];
            av_sonar[k] = new double[5];
        }

        for (k = 0; k < 3; k++)
            d = Math.random();

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                avs[i][j] = 0;

        for (k = 0; k < agent_num; k++) {
            do {
                x = (int) (Math.random() * size);
                current[k][0] = x;
                y = (int) (Math.random() * size);
                current[k][1] = y;
            }
            while (avs[x][y] > 0);

            avs[x][y] = k + 1;

            for (w = 0; w < 2; w++)
                prev_current[k][w] = current[k][w];

            end_state[k] = false;
            conflict_state[k] = false;
        }

        do {
            x = (int) (Math.random() * size);
            target[0] = x;
            y = (int) (Math.random() * size);
            target[1] = y;
        } while (avs[x][y] > 0);

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                mines[i][j] = 0;

        for (int i = 0; i < numMines; i++) {
            do {
                x = (int) (Math.random() * size);
                y = (int) (Math.random() * size);
            }
            while ((avs[x][y] > 0) || (mines[x][y] == 1) || (x == target[0] && y == target[1]));
            mines[x][y] = 1;
        }
        for (int a = 0; a < agent_num; a++) {
            this.setCurrentBearing(a, this.adjustBearing(this.getTargetBearing(a)));
            prev_bearing[a] = this.currentBearing[a];
        }
    }

    public int adjustBearing(int old_bearing) {
        if ((old_bearing == 1) || (old_bearing == 7))
            return (0);
        if ((old_bearing == 3) || (old_bearing == 5))
            return (4);
        return (old_bearing);
    }

    public int getTargetBearing(int i) {
        if ((current[i][0] < 0) || (current[i][1] < 0))
            return (0);
        int[] d = new int[agent_num];

        d = new int[2];
        d[0] = target[0] - current[i][0];
        d[1] = target[1] - current[i][1];

        if (d[0] == 0 && d[1] < 0)
            return (0);
        if (d[0] > 0 && d[1] < 0)
            return (1);
        if (d[0] > 0 && d[1] == 0)
            return (2);
        if (d[0] > 0 && d[1] > 0)
            return (3);
        if (d[0] == 0 && d[1] > 0)
            return (4);
        if (d[0] < 0 && d[1] > 0)
            return (5);
        if (d[0] < 0 && d[1] == 0)
            return (6);
        if (d[0] < 0 && d[1] < 0)
            return (7);
        return (0);
    }

    public int[] getTargetBearing() {
        int[] ret = new int[agent_num];
        int k;

        for (k = 0; k < agent_num; k++)
            ret[k] = getTargetBearing(k);
        return (ret);
    }

    public int getCurrentBearing(int i) {
        return (currentBearing[i]);
    }

    public int[] getCurrentBearing() {
        return (currentBearing);
    }

    public void setCurrentBearing(int[] b) {
        currentBearing = b;
    }

    public void setCurrentBearing(int i, int b) {
        currentBearing[i] = b;
    }

    public double getRewards(int agt, int[] pos, boolean actual, boolean immediate) {
        int x = pos[0];
        int y = pos[1];

        if ((x == target[0]) && (y == target[1])) // reach target
        {
            end_state[agt] = true;
            avs[x][y] = 0;
            return (1);
        }
        if ((x < 0) || (y < 0)) // out of field
            return (-1);

        if (mines[x][y] == 1)       // hit mines
            return (0);

//        if( check_conflict( agt, pos, actual ) )
//            return( 0 );

        if (immediate) {
            if (RewardType == LINEAR) {
                int r = getRange(agt);
                if (r > 10) r = 10;
                return (1.0 - (double) r / 10.0); //adjust intermediate reward
            } else
                return (1.0 / (double) (1 + getRange(agt))); //adjust intermediate reward
        }
        return 0.0; //no intermediate reward
    }

    public double getRewards(int i, boolean immediate) {
        return (getRewards(i, current[i], true, immediate));
    }

    public double[] getRewards(boolean immediate) {
        int k;
        double[] r;

        r = new double[agent_num];
        for (k = 0; k < agent_num; k++) {
            r[k] = getRewards(k, immediate);
        }
        return (r);
    }

    public int getRange(int[] a, int[] b) {
        int range;
        int[] d = new int[2];

        d[0] = Math.abs(a[0] - b[0]);
        d[1] = Math.abs(a[1] - b[1]);
        range = Math.max(d[0], d[1]);
        return (range);
    }

    public int getRange(int i) {
        return (getRange(current[i], target));
    }

    public int getRange(int i, int j) {
        return (getRange(current[i], current[j]));
    }

    public int[] getRange() {
        int k;
        int[] range;

        range = new int[agent_num];
        for (k = 0; k < agent_num; k++) {
            range[k] = getRange(k);
        }
        return (range);
    }

    public double getTargetRange(int i) {
        return 1.0 / (double) (1 + getRange(i));
    }

    public double[] getTargetRange() {
        int k;
        double[] range;

        range = new double[agent_num];
        for (k = 0; k < agent_num; k++) {
            range[k] = getTargetRange(k);
        }
        return (range);
    }

    public void getSonar(int agt, double[] new_sonar) {
        int r;
        int x = current[agt][0];
        int y = current[agt][1];

        if ((x < 0) || (y < 0)) {
            for (int k = 0; k < 5; k++)
                new_sonar[k] = 0;
            return;
        }

        double[] aSonar = new double[8];

        r = 0;
        while (y - r >= 0 && mines[x][y - r] != 1)
            r++;
        if (r == 0)// || y-r<0)
            aSonar[0] = 0.0;
        else
            aSonar[0] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && y - r >= 0 && mines[x + r][y - r] != 1)
            r++;
        if (r == 0)
            aSonar[1] = 0.0;
        else
            aSonar[1] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && mines[x + r][y] != 1)
            r++;
        if (r == 0)
            aSonar[2] = 0.0;
        else
            aSonar[2] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && y + r <= size - 1 && mines[x + r][y + r] != 1)
            r++;
        if (r == 0)
            aSonar[3] = 0.0;
        else
            aSonar[3] = 1.0 / (double) r;

        r = 0;
        while (y + r <= size - 1 && mines[x][y + r] != 1)
            r++;
        if (r == 0)
            aSonar[4] = 0.0;
        else
            aSonar[4] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && y + r <= size - 1 && mines[x - r][y + r] != 1)
            r++;
        if (r == 0)
            aSonar[5] = 0.0;
        else
            aSonar[5] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && mines[x - r][y] != 1)
            r++;
        if (r == 0)
            aSonar[6] = 0.0;
        else
            aSonar[6] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && y - r >= 0 && mines[x - r][y - r] != 1)
            r++;
        if (r == 0)
            aSonar[7] = 0.0;
        else
            aSonar[7] = 1.0 / (double) r;

        currentBearing = getCurrentBearing();

        for (int k = 0; k < 5; k++) {
            new_sonar[k] = aSonar[(currentBearing[agt] + 6 + k) % 8];
            if (binarySonar)
                if (new_sonar[k] < 1)
                    new_sonar[k] = 0; // binary sonar signal
        }
        return;
    }

    public void getAVSonar(int agt, double[] new_av_sonar) {
        int r;
        int x = current[agt][0];
        int y = current[agt][1];

        if ((x < 0) || (y < 0)) {
            for (int k = 0; k < 5; k++)
                new_av_sonar[k] = 0;
            return;
        }

        double[] aSonar = new double[8];

        r = 0;
        while (y - r >= 0 && (avs[x][y - r] == (agt + 1) || avs[x][y - r] == 0))
            r++;
        if (r == 0)
            aSonar[0] = 0.0;
        else
            aSonar[0] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && y - r >= 0 && (avs[x + r][y - r] == (agt + 1) || avs[x + r][y - r] == 0))
            r++;
        if (r == 0)
            aSonar[1] = 0.0;
        else
            aSonar[1] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && (avs[x + r][y] == (agt + 1) || avs[x + r][y] == 0))
            r++;
        if (r == 0)
            aSonar[2] = 0.0;
        else
            aSonar[2] = 1.0 / (double) r;

        r = 0;
        while (x + r <= size - 1 && y + r <= size - 1 && (avs[x + r][y + r] == (agt + 1) || avs[x + r][y + r] == 0))
            r++;
        if (r == 0)
            aSonar[3] = 0.0;
        else
            aSonar[3] = 1.0 / (double) r;

        r = 0;
        while (y + r <= size - 1 && (avs[x][y + r] == (agt + 1) || avs[x][y + r] == 0))
            r++;
        if (r == 0)
            aSonar[4] = 0.0;
        else
            aSonar[4] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && y + r <= size - 1 && (avs[x - r][y + r] == (agt + 1) || avs[x - r][y + r] == 0))
            r++;
        if (r == 0)
            aSonar[5] = 0.0;
        else
            aSonar[5] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && (avs[x - r][y] == (agt + 1) || avs[x - r][y] == 0))
            r++;
        if (r == 0)
            aSonar[6] = 0.0;
        else
            aSonar[6] = 1.0 / (double) r;

        r = 0;
        while (x - r >= 0 && y - r >= 0 && (avs[x - r][y - r] == (agt + 1) || avs[x - r][y - r] == 0))
            r++;
        if (r == 0)
            aSonar[7] = 0.0;
        else
            aSonar[7] = 1.0 / (double) r;

        currentBearing = getCurrentBearing();

        for (int k = 0; k < 5; k++) {
            new_av_sonar[k] = aSonar[(currentBearing[agt] + 6 + k) % 8];
            if (binarySonar)
                if (new_av_sonar[k] < 1)
                    new_av_sonar[k] = 0; // binary sonar signal
        }
        return;
    }

    public void virtual_move(int agt, int d, int[] res) {
        int k;
        int bearing = (currentBearing[agt] + d + 8) % 8;

        res[0] = current[agt][0];
        res[1] = current[agt][1];

        switch (bearing) {
            case 0:
                if (res[1] > 0)
                    res[1]--;
                break;
            case 1:
                if ((res[0] < size - 1) && (res[1] > 0)) {
                    res[0]++;
                    res[1]--;
                }
                break;
            case 2:
                if (res[0] < size - 1)
                    res[0]++;
                break;
            case 3:
                if ((res[0] < size - 1) && (res[1] < size - 1)) {
                    res[0]++;
                    res[1]++;
                }
                break;
            case 4:
                if (res[1] < size - 1)
                    res[1]++;
                break;
            case 5:
                if ((res[0] > 0) && (res[1] < size - 1)) {
                    res[0]--;
                    res[1]++;
                }
                break;
            case 6:
                if (res[0] > 0)
                    res[0]--;
                break;
            case 7:
                if ((res[0] > 0) && (res[1] > 0)) {
                    res[0]--;
                    res[1]--;
                }
                break;
            default:
                break;
        }
        return;
    }

    public void turn(int i, int d) {
        int bearing = getCurrentBearing(i);
        bearing = (bearing + d) % 8;
        setCurrentBearing(i, bearing);
    }

    public int move(int i, int d) {
        int k;

        if ((current[i][0] < 0) || (current[i][1] < 0))
            return (-1);

        for (k = 0; k < 2; k++)
            prev_current[i][k] = current[i][k];

        prev_bearing[i] = currentBearing[i];

        currentBearing[i] = (currentBearing[i] + d + 8) % 8;

        switch (currentBearing[i]) {
            case 0:
                if (current[i][1] > 0) current[i][1]--;
                else {
                    //                   turn( i );
                    return (-1);
                }
                break;
            case 1:
                if (current[i][0] < size - 1 && current[i][1] > 0) {
                    current[i][0]++;
                    current[i][1]--;
                } else {
                    //                   turn( i );
                    return (-1);
                }
                break;
            case 2:
                if (current[i][0] < size - 1) current[i][0]++;
                else {
//                    turn( i );
                    return (-1);
                }
                break;
            case 3:
                if (current[i][0] < size - 1 && current[i][1] < size - 1) {
                    current[i][0]++;
                    current[i][1]++;
                } else {
                    //                   turn( i );
                    return (-1);
                }
                break;
            case 4:
                if (current[i][1] < size - 1)
                    current[i][1]++;
                else {
                    //                   turn( i );
                    return (-1);
                }
                break;
            case 5:
                if (current[i][0] > 0 && current[i][1] < size - 1) {
                    current[i][0]--;
                    current[i][1]++;
                } else {
//                    turn( i );
                    return (-1);
                }
                break;
            case 6:
                if (current[i][0] > 0) current[i][0]--;
                else {
//                    turn( i );
                    return (-1);
                }
                break;
            case 7:
                if (current[i][0] > 0 && current[i][1] > 0) {
                    current[i][0]--;
                    current[i][1]--;
                } else {
//                    turn( i );
                    return (-1);
                }
                break;
            default:
                break;
        }
        avs[prev_current[i][0]][prev_current[i][1]] = 0;
        avs[current[i][0]][current[i][1]] = i + 1;

        return (1);
    }


    // return true if the move still keeps the agent within the field
    public boolean withinField(int i, int d) {
        int testBearing;

        testBearing = (currentBearing[i] + d + 8) % 8;
        switch (testBearing) {
            case 0:
                if (current[i][1] > 0)
                    return (true);
                break;
            case 1:
                if (current[i][0] < size - 1 && current[i][1] > 0)
                    return (true);
                break;
            case 2:
                if (current[i][0] < size - 1) return (true);
                break;
            case 3:
                if (current[i][0] < size - 1 && current[i][1] < size - 1)
                    return (true);
                break;
            case 4:
                if (current[i][1] < size - 1)
                    return (true);
                break;
            case 5:
                if (current[i][0] > 0 && current[i][1] < size - 1)
                    return (true);
                break;
            case 6:
                if (current[i][0] > 0)
                    return (true);
                break;
            case 7:
                if (current[i][0] > 0 && current[i][1] > 0)
                    return (true);
                break;
            default:
                break;
        }
//	    System.out.println ( "OutOfField: current = ("+current[i][0]+","+current[i][1]+")  testBearing = " + testBearing);
        return (false);
    }

    public int[] move(int[] d) {
        int k;
        int[] res;

        res = new int[agent_num];
        for (k = 0; k < agent_num; k++)
            res[k] = move(k, d[k]);
        return res;
    }

    public void undoMove() {
        this.currentBearing = this.prev_bearing;
        current[0] = prev_current[0];
        current[1] = prev_current[1];
    }

    public double nextReward(int agt, int d, boolean immediate) {
        double r;
        int[] next_pos = new int[2];

        virtual_move(agt, d, next_pos);
        r = this.getRewards(agt, next_pos, false, immediate); //consider revise
        // this.undoMove();
        return r;
    }

    public boolean endState(int agt) {
        int x = current[agt][0];
        int y = current[agt][1];

        if (conflict_state[agt]) {
            end_state[agt] = true;
            return (end_state[agt]);
        }
        if ((x < 0) || (y < 0)) {
            end_state[agt] = true;
            return (end_state[agt]);
        }
        if ((x == target[0]) && (y == target[1])) {
            end_state[agt] = true;
            avs[x][y] = 0;
            return (end_state[agt]);
        }
        if ((mines[x][y] == 1) || (check_conflict(agt)) || (end_state[agt])) {
            avs[x][y] = 0;
            end_state[agt] = true;
        } else
            end_state[agt] = false;
        return (end_state[agt]);
    }

    public boolean endState(boolean target_moving) {
        int k;
        boolean bl = true;

        for (k = 0; k < agent_num; k++) {
            if (target_moving) {
                if (isHitTarget(k))
                    return (true);
                if (!endState(k))
                    bl = false;
            } else {
                if (!endState(k))
                    return (false);
            }
        }
        if (target_moving)
            return (bl);
        else
            return (true);
    }

    public boolean endState() {
        int k;

        for (k = 0; k < agent_num; k++) {
            if (!endState(k))
                return (false);
        }
        return (true);
    }

    public boolean isHitMine(int i) {
        if ((current[i][0] < 0) || (current[i][1] < 0))
            return (false);
        if (mines[current[i][0]][current[i][1]] == 1)
            return true;
        else
            return false;
    }

    public boolean isConflict(int i) {
        return (conflict_state[i]);
    }

    public boolean isHitTarget(int i) {
        if ((current[i][0] == target[0]) && (current[i][1] == target[1]))
            return true;
        else
            return false;
    }

    public boolean test_mines(int i, int j) {
        if (mines[i][j] == 1)
            return (true);
        else
            return (false);
    }

    public boolean test_current(int agt, int i, int j) {
        if (current[agt][0] == i && current[agt][1] == j)
            return (true);
        else
            return (false);
    }

    public boolean test_target(int i, int j) {
        if ((target[0] == i) && (target[1] == j))
            return (true);
        else
            return (false);
    }

    public int getMines(int i, int j) {
        return (mines[i][j]);
    }

    public int[] getCurrent(int agt) {
        return (current[agt]);
    }

    public int[][] getCurrent() {
        return (current);
    }

    public void getCurrent(int agt, int[] path) {
        int k;

        for (k = 0; k < 2; k++)
            path[k] = current[agt][k];
        return;
    }

    public void getCurrent(int[][] path) {
        int i, j;

        for (i = 0; i < agent_num; i++)
            for (j = 0; j < 2; j++)
                path[i][j] = current[i][j];
        return;
    }

    public int[] getPrevCurrent(int agt) {
        return (prev_current[agt]);
    }

    public int[][] getPrevCurrent() {
        return (prev_current);
    }

    public int[] getTarget() {
        return (target);
    }

    public void go_target() {
        int[] new_pos = new int[2];
        int b;
        int k;
        double d;

        for (k = 0; k < 3; k++)
            d = Math.random();
        do {
            b = (int) (Math.random() * size);
            virtual_move_target(b, new_pos);
        }
        while (!valid_target_pos(new_pos));
        move_target(b);
        return;
    }

    public boolean valid_target_pos(int[] new_pos) {
        int x = new_pos[0];
        int y = new_pos[1];

        if ((x < 0) || (x >= size))
            return (false);
        if ((y < 0) || (y >= size))
            return (false);
        if (avs[x][y] > 0)
            return (false);
        if (mines[x][y] == 1)
            return (false);
        return (true);
    }

    public void virtual_move_target(int d, int[] new_pos) {
        new_pos[0] = target[0];
        new_pos[1] = target[1];
        switch (d) {
            case 0:
                new_pos[1]--;
                break;
            case 1:
                new_pos[0]++;
                new_pos[1]--;
                break;
            case 2:
                new_pos[0]++;
                break;
            case 3:
                new_pos[0]++;
                new_pos[1]++;
                break;
            case 4:
                new_pos[1]++;
                break;
            case 5:
                new_pos[0]--;
                new_pos[1]++;
                break;
            case 6:
                new_pos[0]--;
                break;
            case 7:
                new_pos[0]--;
                new_pos[1]--;
                break;
            default:
                break;
        }
    }

    public void move_target(int d) {
        switch (d) {
            case 0:
                target[1]--;
                break;
            case 1:
                target[0]++;
                target[1]--;
                break;
            case 2:
                target[0]++;
                break;
            case 3:
                target[0]++;
                target[1]++;
                break;
            case 4:
                target[1]++;
                break;
            case 5:
                target[0]--;
                target[1]++;
                break;
            case 6:
                target[0]--;
                break;
            case 7:
                target[0]--;
                target[1]--;
                break;
            default:
                break;
        }
    }

    @Override
    public double[] observe() {
        return new double[0];
    }

    @Override
    public double getReward() {
        return 0;
    }

    @Override
    public boolean takeAction(int action) {
        return false;
    }

    @Override
    public void frame() {

    }

    @Override
    public int numActions() {
        return 0;
    }

    @Override
    public Component component() {
        return null;
    }
}
