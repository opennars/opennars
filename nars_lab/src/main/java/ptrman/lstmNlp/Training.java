package ptrman.lstmNlp;

import nars.rl.lstm.AbstractTraining;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Training extends AbstractTraining {

    public Training(Random random) {
        super(random, observation_dimension, action_dimension);

        tests = 1500;
    }

    /***
     *
     *
     * °b - variable
     * °c - variable
     *
     * ## : #
     * #a : (^ask,
     * #b : (*,
     * #c : -->
     * #d : >)?
     * #e : u
     * #f : -->is-attribute>)?
     * #g : -->an-1-is-a-2>
     * #h : and
     * #i : -->a-1-is-a-2>
     */

    @Override
    protected List<Interaction> GenerateInteractions(int tests) {
        List<TrainingTuple> trainingTuples = new ArrayList<>();
        trainingTuples.add(new TrainingTuple("is a °b °c?", "#aask,#b\"a\",°b,°c)#f"));
        trainingTuples.add(new TrainingTuple("a °b is °c.", "#b,°b,°c)#cis>"));
        trainingTuples.add(new TrainingTuple("is the °c °b?", "#a,#b\"the\",°b,°c)#f"));

        trainingTuples.add(new TrainingTuple("is an °b °c?", "#aask,#b\"an\",°b,°c)#f"));
        trainingTuples.add(new TrainingTuple("an °b is a °c.", "<#b,°b,°c#g"));
        trainingTuples.add(new TrainingTuple("an °b is a °c and a °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));

        // "a"  "an"  "the"  "this"
        // seperated by ", " and "the"
        trainingTuples.add(new TrainingTuple("an °b is a °c and an °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c and an °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c and the °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c, the °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));



        trainingTuples.add(new TrainingTuple("this means that °c is °b.", ""));



        trainingTuples.add(new TrainingTuple("a frog is stable.", "<(*, \"frog\", \"stable\") --> is>"));
        trainingTuples.add(new TrainingTuple("a bridge is stable.", "<(*, \"bridge\", \"stable\") --> is>"));
        trainingTuples.add(new TrainingTuple("a train is stable.", "<(*, \"train\", \"stable\") --> is>"));
        trainingTuples.add(new TrainingTuple("a house is stable.", "<(*, \"house\", \"stable\") --> is>"));

        trainingTuples.add(new TrainingTuple("is the bridge stable?", "(^ask, <(*, \"a\", \"bridge\", \"stable\") --> is-attribute>)"));
        trainingTuples.add(new TrainingTuple("is a car a vehicle?", "(^ask, <(*, \"a\", \"car\", \"stable\") --> is-a>)"));
        trainingTuples.add(new TrainingTuple("is a bridge a vehicle?", "(^ask, <(*, \"a\", \"bridge\", \"vehicle\") --> is-a>)"));
        trainingTuples.add(new TrainingTuple("is a bridge a dog?", "(^ask, <(*, \"bridge\", \"dog\") --> is-a>)"));
        trainingTuples.add(new TrainingTuple("is a dog a bridge?", "(^ask, <(*, \"dog\", \"bridge\") --> is-a>)"));


        this.tests = trainingTuples.size();

        int sampleSource = 0;

        List<Interaction> result = new ArrayList<>();
        for (int test = 0; test < tests; test++) {
            //sampleSource++;
            sampleSource %= 2;

            String sampleNaturalText;
            String sampleNal;

            if( sampleSource == 0 ) {
                int testIndex = random.nextInt(6 /*trainingTuples.size()*/);

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


            int[] inputSequence = convertInputToVector(sampleNaturalText + sampleNaturalText);

            // try to improve result by repeating the answer

            int[] resultSequence = convertInputToVector(sampleNal);

            for (int t = 0; t < inputSequence.length; t++) {
                double[] input = new double[CODEBOOK.length()];
                input[inputSequence[t]] = 1.0;

                Interaction inter = new Interaction();
                if (t == 0 && true)
                    inter.do_reset = true;
                inter.observation = input;
                result.add(inter);
            }

            // stop symbol
            /*
            double[] input1 = new double[observation_dimension];
            input1[256+1 - 1] = 1.0;
            double[] target_output1 = new double[action_dimension];
            Interaction inter1 = new Interaction();
            inter1.observation = input1;
            inter1.target_output = target_output1;
            result.add(inter1);*/

            // result
            for (int t = 0; t < resultSequence.length; t++) {
                double[] input = new double[CODEBOOK.length()];
                double[] target_output2 = new double[CODEBOOK.length()];

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

    public static int[] convertInputToVector(final String input) {


        int[] result = new int[input.length()];

        final String translatedInput = input.toLowerCase();

        for( int i = 0; i < input.length(); i++ ) {
            final int codebookIndex = CODEBOOK.indexOf(translatedInput.charAt(i));

            if( codebookIndex == -1 ) {
                throw new RuntimeException("Char " + translatedInput.charAt(i) + " not in codebook!");
            }

            result[i] = codebookIndex;
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

    public final static String CODEBOOK = "abcdefghijklmnoprst°#->?. ,\")<";

    static final int observation_dimension = CODEBOOK.length();
    static final int action_dimension = CODEBOOK.length();

}
