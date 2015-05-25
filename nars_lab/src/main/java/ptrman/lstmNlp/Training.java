package ptrman.lstmNlp;

import nars.rl.lstm.AbstractTraining;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Training extends AbstractTraining {
    static final int observation_dimension = 256+1; // +1 for stop symbol
    static final int action_dimension = 256;

    public Training(Random random) {
        super(random, observation_dimension, action_dimension);

        tests = 1000;
    }

    @Override
    protected List<Interaction> GenerateInteractions(int tests) {
        List<TrainingTuple> trainingTuples = new ArrayList<>();
        trainingTuples.add(new TrainingTuple("is a bridge stable?", "(^ask, <(*, \"a\", \"bridge\", \"stable\") -- >is-attribute>)"));
        trainingTuples.add(new TrainingTuple("is the bridge stable?", "(^ask, <(*, \"a\", \"bridge\", \"stable\") -- >is-attribute>)"));
        trainingTuples.add(new TrainingTuple("is a car a vehicle?", "(^ask, <(*, \"a\", \"car\", \"stable\") -- >is-a>)"));
        trainingTuples.add(new TrainingTuple("is a bridge a vehicle?", "(^ask, <(*, \"a\", \"bridge\", \"vehicle\") -- >is-a>)"));
        trainingTuples.add(new TrainingTuple("is a bridge a dog?", "(^ask, <(*, \"bridge\", \"dog\") -- >is-a>)"));
        trainingTuples.add(new TrainingTuple("is a dog a bride?", "(^ask, <(*, \"dog\", \"bridge\") -- >is-a>)"));

        trainingTuples.add(new TrainingTuple("a bridge is stable.", "<(*, \"bridge\", \"stable\") -- >is-a>"));

        this.tests = trainingTuples.size();

        int sampleSource = 0;

        List<Interaction> result = new ArrayList<>();
        for (int test = 0; test < tests; test++) {
            sampleSource++;
            sampleSource %= 2;

            String sampleNaturalText;
            String sampleNal;

            if( sampleSource == 0 ) {
                int testIndex = random.nextInt(trainingTuples.size());

                sampleNaturalText = trainingTuples.get(testIndex).input;
                sampleNal = trainingTuples.get(testIndex).result;
            }
            else {
                TrainingExampleGenerators.Pattern1 generator = new TrainingExampleGenerators.Pattern1();

                TrainingExampleGenerators.Tuple trainingTuple = generator.generate(random);

                sampleNaturalText = trainingTuple.naturalText;
                sampleNal = trainingTuple.nalText;

                //System.out.println(sampleNal);
            }


            int[] inputSequence = convertInputToVector(sampleNaturalText);
            int[] resultSequence = convertInputToVector(sampleNal);

            for (int t = 0; t < inputSequence.length; t++) {
                double[] input = new double[observation_dimension];
                input[inputSequence[t]] = 1.0;

                Interaction inter = new Interaction();
                if (t == 0 && true)
                    inter.do_reset = true;
                inter.observation = input;
                result.add(inter);
            }

            // stop symbol

            double[] input1 = new double[observation_dimension];
            input1[256+1 - 1] = 1.0;
            double[] target_output1 = new double[action_dimension];
            Interaction inter1 = new Interaction();
            inter1.observation = input1;
            inter1.target_output = target_output1;
            result.add(inter1);

            // result
            for (int t = 0; t < resultSequence.length; t++) {
                double[] input = new double[observation_dimension];
                double[] target_output2 = new double[action_dimension];

                target_output2[resultSequence[t]] = 1.0;

                Interaction inter = new Interaction();
                if (t == 0 && false)
                    inter.do_reset = true;
                inter.observation = input;
                inter.target_output = target_output2;
                result.add(inter);
            }
        }

        return result;
    }

    private static int[] convertInputToVector(final String input) {
        int[] result = new int[input.length()];

        final String translatedInput = input.toLowerCase();

        for( int i = 0; i < input.length(); i++ ) {
            result[i] = (int)translatedInput.charAt(0);
        }

        return result;
    }

    private static class TrainingTuple {
        public TrainingTuple(String input, String result) {
            this.input = input;
            this.result = result;
        }

        public String input;
        public String result;
    }
}
