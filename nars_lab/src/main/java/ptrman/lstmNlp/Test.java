package ptrman.lstmNlp;


import nars.rl.lstm.AbstractTraining;
import nars.rl.lstm.SimpleLSTM;
import objenome.util.random.XORShiftRandom;

import java.util.Random;

public class Test {


    public static void main(String[] args) throws Exception {

        System.out.println("LSTM nlp training\n");

        Random r = new XORShiftRandom(1234);
        AbstractTraining task = new Training(r);

        int cell_blocks = 5;
        double learningRate = 0.07;
        SimpleLSTM slstm = new SimpleLSTM(r,
                task.getInputDimension(),
                task.getOutputDimension(),
                cell_blocks,
                learningRate);

        for (int epoch = 0; epoch < 5000; epoch++) {
            double fit = task.EvaluateFitnessSupervised(slstm);
            if (epoch % 10 == 0)
                System.out.println("["+epoch+"] error = " + (1 - fit));
        }
        System.out.println("done.");
    }

}
