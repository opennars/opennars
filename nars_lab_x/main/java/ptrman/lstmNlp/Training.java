package ptrman.lstmNlp;

import nars.rl.lstm.AbstractTraining;

// Comingling of consciousness and computer technology is a prevalent dream.
// + of + and + + is + + +.

public class Training extends AbstractTraining {
    private static class NalCodebooks {
        //private List<String> book = new ArrayList<>();

        private Map<String, Integer> bookHas = new HashMap<>();

        public int getIndexOf(String nal) {
            if( bookHas.containsKey(nal) ) {
                return bookHas.get(nal);
            }
            else {
                bookHas.put(nal, counter);
                counter++;

                return counter-1;
            }
        }

        public String getCodeOf(String nal) {
            final int codeInteger = getIndexOf(nal);

            final String codebookRaw = "abcdefghijklmnoprst";
            // we use this encoding to save some neurons (hopefully)
            final String codebook = codebookRaw.substring(0, 5);

            final List<Integer> convertedToBase = getNumberAsFixedLengthBase(codeInteger, codebook.length(), 6);

            String temporaryResult = "";

            for( final int baseNumber : convertedToBase ) {
                temporaryResult += codebook.charAt(baseNumber);
            }

            return "#" + temporaryResult;
        }

        private int counter = 0;
    }

    NalCodebooks nalCodebooks = new NalCodebooks();



    public Training(Random random) {
        super(random, observation_dimension, action_dimension);

        tests = 1500;
    }


    public void generateTemplates() {
        // a : a
        // b : is
        // c : an
        // d : the
        // e : this
        // f : and

        templateTrainingTuples = new ArrayList<>();

        // check if something is broken
        // input and output codes don't overlap
        //templateTrainingTuples.add(new TrainingTuple("b?", "a?"));


         //still valid

        templateTrainingTuples.add(new TrainingTuple("ba+a+?", "ba+a+?"));
        templateTrainingTuples.add(new TrainingTuple("bd+a+?", "bd+a+?"));
        templateTrainingTuples.add(new TrainingTuple("bd+c+?", "bd+c+?"));

        /*
        templateTrainingTuples.add(new TrainingTuple("bc+c+?", "bc+c+?"));
        templateTrainingTuples.add(new TrainingTuple("ba+c+?", "ba+c+?"));
           */

        //templateTrainingTuples.add(new TrainingTuple("ba++?", "ba++?"));


/*
        templateTrainingTuples.add(new TrainingTuple("a+b+.", "a+b+."));

        // is the duck red?
        // is an duck red?
        // is a duck blue?
        templateTrainingTuples.add(new TrainingTuple("bd++?", "bd++?"));
        templateTrainingTuples.add(new TrainingTuple("bc++?", "bc++?"));
        templateTrainingTuples.add(new TrainingTuple("ba++?", "ba++?"));

        templateTrainingTuples.add(new TrainingTuple("c+ba+.", "c+ba+."));



        templateTrainingTuples.add(new TrainingTuple("ba+a+?", "ba+a+?"));
        templateTrainingTuples.add(new TrainingTuple("bd+a+?", "bd+a+?"));
        templateTrainingTuples.add(new TrainingTuple("bd+c+?", "bd+c+?"));
        templateTrainingTuples.add(new TrainingTuple("bc+c+?", "bc+c+?"));
        templateTrainingTuples.add(new TrainingTuple("ba+c+?", "ba+c+?"));

        templateTrainingTuples.add(new TrainingTuple("a+b+.", "a+b+."));
        */


        /*
        // is the duck red?
        // is an duck red?
        // is a duck blue?
        templateTrainingTuples.add(new TrainingTuple("is the++?", "?" + nalCodebooks.getCodeOf("<(*,\"the\",+,+)-->is-1-2-3>") + "."));
        templateTrainingTuples.add(new TrainingTuple("is an++?", "?" + nalCodebooks.getCodeOf("<(*,\"an\",+,+)-->is-1-2-3") + "."));
        templateTrainingTuples.add(new TrainingTuple("is a++?", "?" + nalCodebooks.getCodeOf("<(*,\"a\",+,+)-->is-1-2-3") + "."));

        templateTrainingTuples.add(new TrainingTuple("an+is a+.", "!" + nalCodebooks.getCodeOf("<(*,\"an\",+,\"a\",+)-->1-2-is-3-4") + "."));
        */

        //trainingTuples.add(new TrainingTuple("is an °b °c?"

        //templateTrainingTuples.add(new TrainingTuple("an + is a + and a + is a +.", "!" + nalCodebooks.getCodeOf("<(*,\"an\",+,\"a\",+)-->1-2-is-3-4") + "," + nalCodebooks.getCodeOf("<(*,\"a\",+,\"a\",+)-->1-2-is-3-4") + "."));
        //templateTrainingTuples.add(new TrainingTuple("a + is an + and a + is a +.", "!" + nalCodebooks.getCodeOf("<(*,\"a\",+,\"an\",+)-->1-2-is-3-4") + "," + nalCodebooks.getCodeOf("<(*,\"a\",+,\"a\",+)-->1-2-is-3-4") + "."));

        // we enumerate all variations
        for( int comma = 0; comma < 2; comma++ ) {
            for( int i = 0; i < 256/* 10 256*/; i++ ) {
                TupleOfNaturalAndNlp naturalAndNlp = generate_var1_word_is_var1_word__manyParts(i, comma == 0, nalCodebooks);

                System.out.println(naturalAndNlp.natural);
                System.out.println(naturalAndNlp.nal);
                System.out.println("======");

                templateTrainingTuples.add(new TrainingTuple(naturalAndNlp.natural + ".", "" + naturalAndNlp.nal + "."));
            }
        }

    }

    private static class TupleOfNaturalAndNlp {
        public TupleOfNaturalAndNlp(final String natural, final String nlp) {
            this.natural = natural;
            this.nal = nlp;
        }

        public final String natural;
        public final String nal;
    }

    // index from 0 to 255
    private static TupleOfNaturalAndNlp generate_var1_word_is_var1_word__manyParts(int index, boolean comma, NalCodebooks nalCodebooks) {
        final int part1Index = index / (4*4);
        final int part2Index = index % (4*4);

        TupleOfNaturalAndNlp x = generate_var1_word_is_var1_word(part1Index / 4, part1Index % 4, nalCodebooks);
        TupleOfNaturalAndNlp y = generate_var1_word_is_var1_word(part2Index / 4, part2Index % 4, nalCodebooks);

        String natural = x.natural;
        String nal = x.nal;

        if( comma ) {
            natural += ",";
        }
        else {
            natural += "f";
        }

        nal += ",";

        natural += y.natural;
        nal += y.nal;

        return new TupleOfNaturalAndNlp(natural, nal);
    }

    // "an + is a +"
    // "the + is an +"
    // and so on
    private static TupleOfNaturalAndNlp generate_var1_word_is_var1_word(int var1, int var2, NalCodebooks nalCodebooks) {
        final String[] VARS = {"a", "c", "d", "e"};

        final String natural = VARS[var1] + "+" + "b" + VARS[var2] + "+";
        final String nal = VARS[var1] + "+" + "b" + VARS[var2] + "+";

        return new TupleOfNaturalAndNlp(natural, nal);
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
     *
     *
     * #g : -->an-1-is-a-2>
     * #h : and
     * #i : -->a-1-is-a-2>
     *
     *
     *
     * #j : -->1-2-is-3-4
     *         an     an
     *         a      a
     * #k : <(*,
     * #f : -->is-1-2-3
     *            a
     *            an
     *            the
     *
     * #g : -->1-2-is-3
     *         a
     *         an
     *
     *
     * . stands for end
     *
     *
     * ?  : (^ask,~content~)
     * #a : <(*,"a",°b,°c)-->is-1-2-3>
     * #b : <(*,"a",°b,°c)-->1-2-is-3>
     * #c : <(*,"the",°c,°d)-->is-1-2-3>
     */

    @Override
    protected List<Interaction> GenerateInteractions(int tests) {
        List<TrainingTuple> trainingTuples = new ArrayList<>();
        /*
        trainingTuples.add(new TrainingTuple("is a °b °c?", "is a °b °c?"));

        //trainingTuples.add(new TrainingTuple("a °b is °c.", "#b!"));
        //trainingTuples.add(new TrainingTuple("is the °c °b?", "#c?"));




        trainingTuples.add(new TrainingTuple("is an °b °c?", "#aask,#k\"is\",\"an\",°b,°c)#f"));


        trainingTuples.add(new TrainingTuple("an°b is a°c.", "<#b,°b,°c#g"));
        trainingTuples.add(new TrainingTuple("an°b is a°c and a°d is a°e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));


        // "a"  "an"  "the"  "this"
        // seperated by ", " and "the"
        trainingTuples.add(new TrainingTuple("an °b is a °c and an °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c and an °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c and the °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));
        trainingTuples.add(new TrainingTuple("a °b is a °c, the °d is a °e.", "<#b,°b,°c#g#h<#b,°d,°e#i"));

        // marray had a little lambd
        // "had" "has" "have"
        // "a" "an" "the"
        // TODO< generator >

        // an andromeda council atack on an underground US govt / reptilian base
        // (he/it/ ) (was/were/ ) (an/a) °b °c °d on (an/the/a) °e °f °g
        // (he/it/ ) (was/were/ ) (an/a) °b °c °d on (an/the/a) °e °f °g / °h °i

        // °b had a °c °d
        // °b had an °c °d
        // (the/ ) °b had (a/an) °c °d




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
*/
        trainingTuples = templateTrainingTuples;

        //templateTrainingTuples = trainingTuples;

        this.tests = trainingTuples.size();

        int sampleSource = 0;

        List<Interaction> result = new ArrayList<>();
        for (int test = 0; test < tests; test++) {
            //sampleSource++;
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


            int[] inputSequence = convertInputToVector(sampleNaturalText, 0);

            // try to improve result by repeating the answer

            int[] resultSequence = convertInputToVector(sampleNal, observation_dimension);

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

    public static int[] convertInputToVector(final String input, int offset) {


        int[] result = new int[input.length()];

        final String translatedInput = input.toLowerCase();

        for( int i = 0; i < input.length(); i++ ) {
            final int codebookIndex = CODEBOOK.indexOf(translatedInput.charAt(i));

            if( codebookIndex == -1 ) {
                throw new RuntimeException("Char " + translatedInput.charAt(i) + " not in codebook!");
            }

            result[i] = codebookIndex + offset;
        }

        return result;
    }

    public static class TrainingTuple {
        public TrainingTuple(String input, String result) {
            this.input = input;
            this.result = result;
        }

        public String input;
        public String result;
    }

    private static List<Integer> getNumberAsFixedLengthBase(final int value, final int base, final int length) {
        List<Integer> result = getNumberAsVariableLengthBase(value, base);

        if( result.size() > length ) {
            throw new RuntimeException("length out of range!");
        }

        while( result.size() < length ) {
            result.add(0);
        }

        return result;
    }

    private static List<Integer> getNumberAsVariableLengthBase(final int value, final int base) {
        final String CODEBOOK = "0123456789abcdefghijklmnopqrstuvwxyz";

        List<Integer> result = new ArrayList<>();
        final String resultAsString = Integer.toString(value, base);

        for( int i = resultAsString.length() - 1; i >= 0; i-- ) {
            result.add(CODEBOOK.indexOf(resultAsString.charAt(i)));
        }

        return result;
    }

    public final static String CODEBOOK = "abcdef+?! .,";
    //public final static String CODEBOOK = "abcdefghijklmopqrstuvwxyz+?! .,°#";

    static final int observation_dimension = CODEBOOK.length();
    static final int action_dimension = CODEBOOK.length()*2;


    public List<TrainingTuple> templateTrainingTuples;
}
