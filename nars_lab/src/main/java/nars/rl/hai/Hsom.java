package nars.rl.hai;

import nars.util.data.random.XORShiftRandom;

import java.util.Random;

public class Hsom {

    final Random random = new XORShiftRandom(1);

    private double[][][] links;
    private double[] inputs;
    private double[][] coords1;
    private double[][] coords2;
    private double[][][] vis;
    private int numInputs;
    private int SomSize;
    private double gamma = 5.0f;
    private double eta = 0.1f;
    
    public int winnerx = 0; //winner coordinates
    public int winnery = 0;

    public double Leak = 0.1;
    
    public double InMul = 1.0;
    
    boolean Leaky = true;

    public Hsom(int SomSize, int numInputs) {
        links = new double[SomSize][SomSize][numInputs];
        vis = new double[SomSize][SomSize][numInputs];
        inputs = new double[numInputs];
        coords1 = new double[SomSize][SomSize];
        coords2 = new double[SomSize][SomSize];
        this.numInputs = numInputs;
        this.SomSize = SomSize;
        for (int i1 = 0; i1 < SomSize; i1++) {
            for (int i2 = 0; i2 < SomSize; i2++) {
                coords1[i1][i2] = i1 * 1.0; //Kartenkoords
                coords2[i1][i2] = i2 * 1.0;
            }
        }
        for (int x = 0; x < SomSize; x++) {
            for (int y = 0; y < SomSize; y++) {
                for (int z = 0; z < numInputs; z++) {
                    links[x][y][z] = (random.nextFloat() * 1/**
                             * 2.0-1.0
                             */
                            ) * 0.1;
                }
            }
        }
    }

    void input(double[] input) {
        int i1, i2, j;
        double summe;
        double minv = 100000.0f;

        for (j = 0; j < numInputs; j++) {
            if (!Leaky) {
                this.inputs[j] = input[j] * InMul;
            } else {
                this.inputs[j] += -Leak * this.inputs[j] + input[j];
            }
        }
        for (i1 = 0; i1 < SomSize; i1++) {
            for (i2 = 0; i2 < SomSize; i2++) {
                summe = 0.0f;
                for (j = 0; j < numInputs; j++) {
                    double val = (links[i1][i2][j] - inputs[j]) * (links[i1][i2][j] - inputs[j]);
                    vis[i1][i2][j] = val;
                    summe += val;
                }
                if (summe <= minv) //get winner
                {
                    minv = summe;
                    winnerx = i1;
                    winnery = i2;
                }
            }
        }
    }

    void output(final double[] outarr) {
        final int x = winnerx;
        final int y = winnery;
        System.arraycopy(links[x][y], 0, outarr, 0, numInputs);
    }

    double hsit(int i1, int i2) {   //neighboorhood-function
        double diff1 = (coords1[i1][i2] - coords1[winnerx][winnery]) 
                        * (coords1[i1][i2] - coords1[winnerx][winnery]);
        double diff2 = (coords2[i1][i2] - coords2[winnerx][winnery]) 
                        * (coords2[i1][i2] - coords2[winnerx][winnery]);
        return 1.0f / Math.sqrt(2 * Math.PI * gamma * gamma)
                        * Math.exp((diff1 + diff2) / (-2 * gamma * gamma));
    }

    public void learn(final double[] input) {
        int i1, i2, j;
        input(input);
        if (eta != 0.0f) {
            for (i1 = 0; i1 < SomSize; i1++) {
                for (i2 = 0; i2 < SomSize; i2++) {
                    double h = hsit(i1, i2);
                    for (j = 0; j < numInputs; j++) { //adaption
                        links[i1][i2][j] += eta * h * (inputs[j] - links[i1][i2][j]);
                    }
                }
            }
        }
    }

    public void setParams(double AdaptionStrenght, double AdaptioRadius) {
        eta = AdaptionStrenght;
        gamma = AdaptioRadius;
    }

    public int width() {
        return SomSize;
    }
}
