//package nars.analyze.experimental;
//
//import nars.Global;
//import nars.Symbols;
//import nars.nal.Task;
//import nars.nal.filter.ConstantDerivationLeak;
//import nars.model.impl.Default;
//import org.apache.commons.math3.genetics.*;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import static nars.io.Texts.n4;
//
///**
// * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
// */
//public class OptimizeLeakGenetic {
//
//
//    final static int cycles = 1500;
//
//    // parameters for the GA
//    private static final int POPULATION_SIZE = 50;
//    private static final int NUM_GENERATIONS = 250;
//    private static final double ELITISM_RATE = 0.25;
//    private static final double CROSSOVER_RATE = 0.5;
//    private static final double MUTATION_RATE = 0.15;
//    private static final int TOURNAMENT_ARITY = 2;
//
//    final static int values = 6;
//    final static int bitsPerValue = 5;
//
//
//    public static void optimize() {
//        // to test a stochastic algorithm is hard, so this will rather be an usage example
//
//        // initialize a new genetic algorithm
//        GeneticAlgorithm ga = new GeneticAlgorithm(
//                new OnePointCrossover<Integer>(),
//                CROSSOVER_RATE, // all selected chromosomes will be recombined (=crosssover)
//                new BinaryMutation(),
//                MUTATION_RATE,
//                new TournamentSelection(TOURNAMENT_ARITY)
//        );
//
//        //Assert.assertEquals(0, ga.getGenerationsEvolved());
//
//        // initial population
//        Population initial = randomPopulation(bitsPerValue, values);
//        // stopping conditions
//        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);
//
//        // best initial chromosome
//        Chromosome bestInitial = initial.getFittestChromosome();
//
//        // run the algorithm
//        Population finalPopulation = ga.evolve(initial, stopCond);
//
//        // best chromosome from the final population
//        Chromosome bestFinal = finalPopulation.getFittestChromosome();
//
//        // the only thing we can test is whether the final solution is not worse than the initial one
//        // however, for some implementations of GA, this need not be true :)
//
//        //Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);
//        //Assert.assertEquals(NUM_GENERATIONS, ga.getGenerationsEvolved());
//
//    }
//
//
//
//
//    /**
//     * Initializes a random population.
//     */
//    static ElitisticListPopulation randomPopulation(int bits, int values) {
//        List<Chromosome> popList = new LinkedList<Chromosome>();
//
//        for (int i=0; i<POPULATION_SIZE; i++) {
//            BinaryChromosome randChrom = new FixedPointChromosome(bits, values);
//            popList.add(randChrom);
//        }
//        return new ElitisticListPopulation(popList, popList.size(), ELITISM_RATE);
//    }
//
//    public static double score(FixedPointChromosome f) {
//        final float jLeakPri = f.v(0);
//        final float jLeakDur = f.v(1);
//        final float gLeakPri = f.v(2);
//        final float gLeakDur = f.v(3);
//        final float qLeakPri = f.v(4);
//        final float qLeakDur = f.v(5);
//
//        Default b = new Default() {
//            @Override
//            protected void initDerivationFilters() {
//
//                getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(0, 0) {
//                    @Override
//                    protected boolean leak(Task derived) {
//                        switch (derived.getPunctuation()) {
//
//                            case Symbols.JUDGMENT:
//                                derived.mulPriority(jLeakPri);
//                                derived.mulDurability(jLeakDur);
//                                break;
//
//                            case Symbols.GOAL:
//                                derived.mulPriority(gLeakPri);
//                                derived.mulDurability(gLeakDur);
//                                break;
//
//                            case '?':
//                            case Symbols.QUEST:
//                                derived.mulPriority(qLeakPri);
//                                derived.mulDurability(qLeakDur);
//                                break;
//                        }
//
//                        return true;
//                    }
//                });
//            }
//        };
//        b.setInternalExperience(null);
//
//        ExampleScores e = new ExampleScores(b, cycles);
//        System.out.println(
//                n4(jLeakPri) + ", " + n4(jLeakDur) + ",   " +
//                        n4(gLeakPri) + ", " + n4(gLeakDur) + ",   " +
//                        n4(qLeakPri) + ", " + n4(qLeakDur) + ",   " +
//                        "   " + e.totalCost);
//
//        return 1.0 / e.totalCost;
//    }
//
//    /**
//     * Chromosomes represented by a binary chromosome.
//     *
//     * The goal is to set all bits (genes) to 1.
//     */
//    static class FixedPointChromosome extends BinaryChromosome {
//
//        int bits;
//
//        public FixedPointChromosome(int bitsPerValue, int values) {
//            super(BinaryChromosome.randomBinaryRepresentation(bitsPerValue * values));
//            this.bits = bitsPerValue;
//        }
//
//        public FixedPointChromosome(List<Integer> representation, int bitsPerValue) {
//            super(representation);
//            this.bits = bitsPerValue;
//        }
//
//        public float v(int v) {
//            double x = 0;
//            double f = 1;
//            for (int i = 0; i < bits; i++) {
//                x += f * this.getRepresentation().get(bits * v + i);
//                f *= 2;
//            }
//            x /= Math.pow(2, bits);
//            return (float)x;
//        }
//        /**
//         * Returns number of elements != 0
//         */
//        public double fitness() {
//            return score(this);
//        }
//
//        @Override
//        public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> chromosomeRepresentation) {
//            return new FixedPointChromosome(chromosomeRepresentation, bits);
//        }
//
//    }
//
//    public static void main(String[] args) {
//        Global.DEBUG = false;
//
//        optimize();
//    }
//}
