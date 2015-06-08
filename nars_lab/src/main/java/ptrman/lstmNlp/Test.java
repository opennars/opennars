package ptrman.lstmNlp;


import nars.rl.lstm.opencl.LSTMCL;
import nars.rl.lstm.util;
import nars.util.data.random.XORShiftRandom;

import java.util.List;
import java.util.Random;

import static ptrman.lstmNlp.Training.convertInputToVector;

public class Test {
    private Training task;

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        test.train();

    }

    public void train() throws Exception {

        System.out.println("LSTM nlp training\n");

        Random r = new XORShiftRandom(1234);
        task = new Training(r);

        //task.batchsize = 1;

        task.generateTemplates();

        // 50 cells min? @ 30000 cycles @ 0.01 learning rate @ 2 samples
        // 30 cells min? @ 25000 cacles @ 0.01 learning rate @ 2 samples
        final int epochs = 100000; // 200000
        final int epochsShake = 25000000;
        // 10 20 30 50 -70- -90 for 10- 100 200? 300(tried, not finished)
        int cell_blocks = 300; // 20 50 100 -500-
        double learningRate = 0.07; // 0.07
        LSTMCL slstm = new LSTMCL(r,
                task.getInputDimension(),
                task.getOutputDimension(),
                cell_blocks,
                learningRate);

        int noErrorCounter = 0;

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("[" + epoch + "]");

            if (epoch % 10 == 0) {
                double fit = task.EvaluateFitnessSupervised(slstm);

                System.out.println("[" + epoch + "] error = " + (1 - fit));


                if (fit == 1.0) {
                    //String decoded = getDecoded(slstm, "b a++?");

                    //System.out.println(decoded);

                    final boolean allValidated = validate(task.templateTrainingTuples, slstm);

                /*
                noErrorCounter++;

                if( noErrorCounter >= 15 ) {
                    System.out.println("error = 0.0, DONE");
                    break;
                }
                */

                    if( allValidated ) {
                        break;
                    }
                }
                else {
                    noErrorCounter = 0;
                }
            }
            else {
                task.supervised(slstm);
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



    }

    // double[] actual_output = slstm.predict(input, true);

    // returns true if validation set
    private boolean validate(final List<Training.TrainingTuple> validationSet, LSTMCL slstm) {
        int validationSuccess = 0;
        int validationCount = 0;

        System.out.println("Validation:");

        for( final Training.TrainingTuple currentValidationTuple : validationSet ) {
            String result = getDecoded(slstm, currentValidationTuple.input);

            if( result.equals(currentValidationTuple.result) ) {
                validationSuccess++;
            }

            final boolean debugMissmatch = true;

            if( debugMissmatch && !result.equals(currentValidationTuple.result) ) {
                System.out.println("   missmatch!");
                System.out.println("      correct output: " + currentValidationTuple.result);
                System.out.println("      ------- output: " + result);
            }

            validationCount++;
        }

        System.out.println("   statistics:");
        System.out.println("   successrate: " + Integer.toString(validationSuccess) + "/" + Integer.toString(validationCount));

        return validationCount == validationSuccess;
    }

    private String getDecoded(LSTMCL lstm, final String input) {
        int[] inputSequence = convertInputToVector(input, 0);



        for (int t = 0; t < inputSequence.length; t++) {
            double[] inputVector = new double[Training.CODEBOOK.length()];
            inputVector[inputSequence[t]] = 1.0;

            double[] actual_output = lstm.predict(inputVector, true);

            // find out the position where it is maximum
            int maxIndex = util.argmax(actual_output);

            //System.out.println(maxIndex);
        }

        String decoded = "";

        for( int t = 0; t < 50; t++ ) {
            double[] inputVector = new double[Training.CODEBOOK.length()];

            double[] actual_output = lstm.predict(inputVector, true);

            // find out the position where it is maximum
            int maxIndex = util.argmax(actual_output) - task.observation_dimension;

            decoded += Training.CODEBOOK.charAt(maxIndex);

            if( Training.CODEBOOK.charAt(maxIndex) == '.' || Training.CODEBOOK.charAt(maxIndex) == '!' || Training.CODEBOOK.charAt(maxIndex) == '?' ) {
                break;
            }
        }

        return decoded;
    }

}
