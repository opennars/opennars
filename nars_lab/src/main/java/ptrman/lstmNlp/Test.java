package ptrman.lstmNlp;


import nars.rl.lstm.AbstractTraining;
import nars.rl.lstm.SimpleLSTM;
import objenome.util.random.XORShiftRandom;

import java.util.Random;

import static ptrman.lstmNlp.Training.convertInputToVector;

public class Test {


    public static void main(String[] args) throws Exception {

        System.out.println("LSTM nlp training\n");

        Random r = new XORShiftRandom(1234);
        AbstractTraining task = new Training(r);

        int cell_blocks = 5; // 5
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

        //slstm.do_reset = true;

        int[] inputSequence = convertInputToVector("a bridge is stable.");


        for (int t = 0; t < inputSequence.length; t++) {
            double[] input = new double[256+1];
            input[inputSequence[t]] = 1.0;

            double[] actual_output = slstm.predict(input);
        }

        // stop symbol
        {
            double[] input = new double[256+1];
            input[256+1-1] = 1.0;

            double[] actual_output = slstm.predict(input);
        }

        for( int outputSequenceIndex = 0; outputSequenceIndex < 10; outputSequenceIndex++ ) {
            double[] input = new double[256+1];

            double[] actual_output = slstm.predict(input);

            // find out the position where it is maximum
            int maxIndex = 0;
            double maxValue = actual_output[0];

            for( int i = 0; i < actual_output.length; i++ ) {
                final double currentValue = actual_output[i];
                if( currentValue > maxValue ) {
                    maxValue = currentValue;
                    maxIndex = i;
                }
            }

            System.out.println(maxIndex);
        }
    }

}
