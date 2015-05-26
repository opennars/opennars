package ptrman.lstmNlp;


import nars.rl.lstm.AbstractTraining;
import nars.rl.lstm.SimpleLSTM;
import nars.rl.lstm.util;
import objenome.util.random.XORShiftRandom;

import java.util.Random;

import static ptrman.lstmNlp.Training.convertInputToVector;

public class Test {


    public static void main(String[] args) throws Exception {

        System.out.println("LSTM nlp training\n");

        Random r = new XORShiftRandom(1234);
        AbstractTraining task = new Training(r);

        // 50 cells min? @ 30000 cycles @ 0.01 learning rate @ 2 samples
        // 30 cells min? @ 25000 cacles @ 0.01 learning rate @ 2 samples
        final int epochs = 120000; // 20000
        final int epochsShake = 25000000;
        int cell_blocks = 32; // 5  100
        double learningRate = 0.01; // 0.07
        SimpleLSTM slstm = new SimpleLSTM(r,
                task.getInputDimension(),
                task.getOutputDimension(),
                cell_blocks,
                learningRate);

        for (int epoch = 0; epoch < epochs; epoch++) {
            double fit = task.EvaluateFitnessSupervised(slstm);
            if (true || epoch % 10 == 0) {
                System.out.println("[" + epoch + "] error = " + (1 - fit));
            }

            if (fit == 1.0) {
                System.out.println("error = 0.0, DONE");
                break;
            }

            if (epoch > epochsShake-1) {
                if( epoch % epochsShake == 0) {
                    slstm.setLearningRate(0.15f);
                }
                if( epoch % epochsShake == 10 ) {
                    slstm.setLearningRate(learningRate);
                }
            }
        }
        System.out.println("done.");



        slstm.clear();

        int[] inputSequence = convertInputToVector("is a °b °c?");


        for (int t = 0; t < inputSequence.length; t++) {
            double[] input = new double[Training.CODEBOOK.length()];
            input[inputSequence[t]] = 1.0;

            double[] actual_output = slstm.predict(input);

            // find out the position where it is maximum
            int maxIndex = util.argmax(actual_output);

            //System.out.println(maxIndex);
        }

        // stop symbol
        /*
        {
            double[] input = new double[256+1];
            input[256+1-1] = 1.0;

            double[] actual_output = slstm.predict(input);
        }*/

        //slstm.clear();

        for( int outputSequenceIndex = 0; outputSequenceIndex < 30; outputSequenceIndex++ ) {
            double[] input = new double[Training.CODEBOOK.length()];

            double[] actual_output = slstm.predict(input);

            // find out the position where it is maximum
            int maxIndex = util.argmax(actual_output);

            System.out.print(Training.CODEBOOK.charAt(maxIndex));
        }


    }

}
