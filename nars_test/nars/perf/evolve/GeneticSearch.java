/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf.evolve;

import java.util.Arrays;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.DefaultNARBuilder;
import nars.core.build.NeuromorphicNARBuilder;
import nars.perf.NALTestScore;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.population.BasicPopulation;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.species.BasicSpecies;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.genetic.crossover.SpliceNoRepeat;
import org.encog.ml.genetic.genome.IntegerArrayGenome;
import org.encog.ml.genetic.genome.IntegerArrayGenomeFactory;
import org.encog.ml.genetic.mutate.MutatePerturb;

/**
 *
 * @see
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/ml/tsp/genetic/SolveTSP.java
 * @author me
 */
public class GeneticSearch {

    float parallelizationPenalizationRate = 0.5f; //scale of score divisor for parallel
    int maxCycles = 32;
    int populationSize = 8;
    
    public static class NARGenome extends IntegerArrayGenome {

        final static int S = 10;

        
                
        //TODO cycle task ordering
        
        public NARGenome() {
            super(S);                        
        }
        
        public NARGenome(Genome g) {
            super(S);
            System.arraycopy(((IntegerArrayGenome)g).getData(), 0, getData(), 0, S);
        }
        
        
        public static int r(int min, int max) {
            return (int)(Math.random() * (1+max-min) + min);
        }
        public static int minmax(int x, int min, int max) {
            return Math.min(Math.max(x, min), max);
        }
        
        public static NARGenome newRandom() {
            NARGenome g = new NARGenome();
            int[] d = g.getData();
            
            d[0] = r(0,1);
            d[1] = r(250, 1250);
            d[2] = r(4, 40);
            d[3] = r(10, 200);
            d[4] = r(0, 1000);
            
            d[5] = r(1, 5);
            d[6] = r(1, 20);
            d[7] = r(1, 10);
            
            d[8] = r(1, 8);
            
            d[9] = r(1,10);
            
            return g;
        }

        public int getConceptsFired() { return getData()[8] = minmax(getData()[8],1, 5);       }
        public int getConceptTermLinks() { return getData()[2] = minmax( getData()[2], 1, 200); };
        public int getConceptTaskLinks() { return getData()[3] = minmax( getData()[3], 1, 100); };
        
        public NAR newNAR() {
            int[] d = getData();
            
            int builderType = d[0] % 2; 
            int numConcepts = d[1];
            int numTaskLinks = getConceptTermLinks();
            int numTermLinks = getConceptTaskLinks();
            int numSubconcepts = d[4];
            int conceptForget = d[5];
            int beliefForget = d[6];
            int taskForget = d[7];
            int conceptsFired = getConceptsFired();
            int duration = Math.max(1,d[9]);
            
            DefaultNARBuilder b;
                        
            if (builderType == 0) {
                b = new DefaultNARBuilder();                
            }
            else if (builderType == 1) {
                b = new NeuromorphicNARBuilder(conceptsFired /*ants */);                
            }
            else {
                throw new RuntimeException("Invalid Builder type " + builderType);
            }
            
            b.setConceptBagSize(numConcepts);
            b.setTaskLinkBagSize(numTaskLinks);
            b.setTermLinkBagSize(numTermLinks);
            b.setSubconceptBagSize(numSubconcepts);
                        
            NAR n = b.build();
            
            n.param().duration.set(duration);
            n.param().conceptForgetDurations.set(conceptForget);
            n.param().taskForgetDurations.set(taskForget);
            n.param().beliefForgetDurations.set(beliefForget);
            
            if (builderType == 0) {
                //analogous to # of ants but in defaultbag
                n.param().cycleConceptsFired.set( conceptsFired );
            }
            
            return n;
        }


        @Override
        public String toString() {
            return "NARGenome[" + Arrays.toString(getData()) + "]";
        }
        
        
        
    }

    public double score(NAR n) {


            Parameters.DEBUG = false;

            double s;
            try {
                s = NALTestScore.score(n, maxCycles);
            }
            catch (Throwable e) { 
                if (Parameters.DEBUG)
                    e.printStackTrace();
                return 0;
            }

            return s;

    }
    
    private TrainEA genetic;

    public GeneticSearch() {


        Population pop = initPopulation(populationSize);

        CalculateScore score = new CalculateScore() {

            
            @Override
            public double calculateScore(MLMethod phenotype) {
		
                try {
                    NARGenome genome = (NARGenome) phenotype;

                    System.out.print(genome.toString());

                    final NAR n = genome.newNAR();

                    double s = score(n);
                    
                    //divide score based on degree of parallelism
                    s/=(1.0 + ( (genome.getConceptsFired()-1.0) * parallelizationPenalizationRate));
                    
                    System.out.println(" score: " + s);
                    
                    return s;
                }                
                catch (Throwable e) { 
                    if (Parameters.DEBUG)
                        e.printStackTrace();
                    return 0; 
                }
		
            }

            @Override public boolean shouldMinimize() {
                return false;
            }

            @Override public boolean requireSingleThreaded() {
                return true;
            }

        };

        System.out.println("Default Score: " + score( new DefaultNARBuilder().build() ));
        
        genetic = new TrainEA(pop, score);
        genetic.setShouldIgnoreExceptions(true);
        genetic.setThreadCount(1);

        genetic.addOperation(0.9, new SpliceNoRepeat(1));
        genetic.addOperation(0.2, new MutatePerturb(0.1f));
        

        while (true) {
            genetic.iteration();            
            
            
            NARGenome g = (NARGenome)genetic.getBestGenome();
            System.out.println(g.getScore() + " " + Arrays.toString(g.getData()) );
        }
    }

    private Population initPopulation(int populationSize) {
        Population result = new BasicPopulation(populationSize, null);

        BasicSpecies defaultSpecies = new BasicSpecies();
        defaultSpecies.setPopulation(result);
        for (int i = 0; i < populationSize; i++) {
            final IntegerArrayGenome genome = NARGenome.newRandom();            
            defaultSpecies.getMembers().add(genome);
        }
        
        result.setGenomeFactory(new IntegerArrayGenomeFactory(NARGenome.S) {
 
            @Override public Genome factor() {
                return new NARGenome(super.factor());
            }

            @Override
            public Genome factor(Genome other) {
                return new NARGenome(super.factor(other));
            }
            
            
            
        });
        
        result.getSpecies().add(defaultSpecies);

        return result;
    }


    public static void main(String[] args) {
        new GeneticSearch();
    }
}
