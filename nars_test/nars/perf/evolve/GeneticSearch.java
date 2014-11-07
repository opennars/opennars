/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf.evolve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.CurveBagNARBuilder;
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
import org.encog.ml.genetic.crossover.Splice;
import org.encog.ml.genetic.genome.DoubleArrayGenome;
import org.encog.ml.genetic.genome.DoubleArrayGenomeFactory;
import org.encog.ml.genetic.mutate.MutatePerturb;
import org.encog.ml.genetic.mutate.MutateShuffle;

/**
 *
 * @see
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/ml/tsp/genetic/SolveTSP.java
 * @author me
 */
public class GeneticSearch {
    //623.0 [1, 1585, 302, 46, 33, 4, 20, 10, 4, 11]
    //607.0 [1, 656, 528, 74, 124, 5, 3, 7, 3, 13]
    //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]
    
    //0: Default Score: 2023.0
    //1473.0 [0.0, 650.0, 415.0, 5.0, 57.0, 4.806817046483262, 15.098489110914961, 9.683106815451737, 1.0, 6.0]    
    //1367.0 [0.0, 760.0, 16.0, 77.0, 126.0, 1.9526462004203577, 1.1296896689902738, 1.5503616143067935, 1.0, 16.0]
    
    int maxCycles = 256;
    int populationSize = 16;
    int generationsPerPopulation = 16;
    
    
    public static class IntegerParameter {
        private double min, max;
        private final String name;
        private boolean integer;

        public IntegerParameter(String name, double min, double max) {
            
            this.name = name;
            this.min = min;
            this.max = max;                    
            integer = true;
        }
        
        public IntegerParameter(String name, double min, double max, boolean i) {
            this(name, min, max);
            this.integer = i;
        }
        
        public double getMax() {
            return max;
        }
        public double getMin() {
            return min;
        }
        public double getRandom() {
            return acceptable( r(min,max) );
        }
        
        public boolean isInteger() {
            return integer;
        }
        public double acceptable(double i) {
            if (i < min) i = min;
            if (i > max) i = max;
            if (integer) return Math.round(i);
            return i;
        }

        @Override
        public String toString() {
            return name + "[" + min + ".." + max + "]";
        }
        
        
    }


    static final List<IntegerParameter> param = new ArrayList();
    static final Map<String,Integer> paramIndex = new HashMap();
    static {

        param.add(new IntegerParameter("builderType", 0, 0)); //result will be % number types
        param.add(new IntegerParameter("conceptMax", 900, 1100));
        param.add(new IntegerParameter("subConceptMax", 1024, 1024));
        param.add(new IntegerParameter("conceptTaskLinks", 15, 25));
        param.add(new IntegerParameter("conceptTermLinks", 90, 110));

        param.add(new IntegerParameter("conceptForgetDurations", 1.5, 2.5, false));
        param.add(new IntegerParameter("termLinkForgetDurations", 3, 5, false));
        param.add(new IntegerParameter("taskLinkForgetDurations", 9, 11, false));

        param.add(new IntegerParameter("conceptsFiredPerCycle", 1, 1));

        param.add(new IntegerParameter("cyclesPerDuration", 5, 5));

        param.add(new IntegerParameter("conceptBeliefs", 5, 10));
        param.add(new IntegerParameter("conceptGoals", 5, 10));
        param.add(new IntegerParameter("conceptQuestions", 3, 6));
        
        param.add(new IntegerParameter("contrapositionPriority", 20, 40));

        //param.add(new IntegerParameter("prologEnable", 0, 1));

        int j = 0;
        for (IntegerParameter i : param) {
            paramIndex.put(i.name, j++);
        }
    }
    
    public static class NARGenome extends DoubleArrayGenome {
        
        public NARGenome() {
            this(null);
        }
        
        public NARGenome(Genome g) {
            super(param.size());
            if (g!=null)
                System.arraycopy(((DoubleArrayGenome)g).getData(), 0, getData(), 0, param.size());
        
            //normalize to acceptable values
            for (int i = 0; i < param.size(); i++) {
                getData()[i] = param.get(i).acceptable( getData()[i] );
            }
        }
        
        public static NARGenome newRandom() {
            NARGenome g = new NARGenome();
            double[] d = g.getData();
            
            for (int i = 0; i < param.size(); i++) {
                IntegerParameter p = param.get(i);
                d[i] = r( p.getMin(), p.getMax() );
            }
            
            return g;
        }

        public int i(String name) {
            int idx = paramIndex.get(name);
            return (int)(getData()[idx] = param.get(idx).acceptable( getData()[idx] ));
        }
        public int i(String name, int defaultValue) {
            Integer idx = paramIndex.get(name);
            if (idx == null)
                return defaultValue;            
            return (int)(getData()[idx] = param.get(idx).acceptable( getData()[idx] ));
        }
        
        public double d(String name) {
            int idx = paramIndex.get(name);
            return getData()[idx] = param.get(idx).acceptable( getData()[idx] );
        }
        
        public void set(String name, double value) {
            int idx = paramIndex.get(name);
            getData()[idx] = param.get(idx).acceptable( value );
        }
        
        public void random(int index) {
            IntegerParameter p = param.get(index);
            getData()[index] = p.getRandom();
        }
        
        public NAR newNAR() {
            
            
            int builderType = i("builderType") % 3;
            int numConcepts = i("conceptMax");
            int numTaskLinks = i("conceptTaskLinks");
            int numTermLinks = i("conceptTermLinks");
            int numSubconcepts = i("subConceptMax");
            float conceptForget = (float)d("conceptForgetDurations");
            float beliefForget = (float)d("termLinkForgetDurations");
            float taskForget = (float)d("taskLinkForgetDurations");
            int conceptsFired = i("conceptsFiredPerCycle");
            int duration = i("cyclesPerDuration");
            
                    
            //int prolog = get("prologEnable");
            
            DefaultNARBuilder b;
                        
            if (builderType == 0) {
                b = new DefaultNARBuilder();                
            }
            else if (builderType == 1) {
                b = new NeuromorphicNARBuilder(conceptsFired /*ants */);                
            }
            else if (builderType == 2) {
                b = new CurveBagNARBuilder(true);
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

            
            n.param().conceptBeliefsMax.set(i("conceptBeliefs"));
            n.param().conceptGoalsMax.set(i("conceptGoals"));
            n.param().conceptQuestionsMax.set(i("conceptQuestions"));
            
            
            if (builderType != 1) {
                //analogous to # of ants but in defaultbag
                n.param().cycleConceptsFired.set( conceptsFired );
            }
            
            /*if (prolog == 1) {
                new NARPrologMirror(n, 0.75f, true);
            }*/
            
            return n;
        }


        @Override
        public String toString() {
            return "NARGenome[" + Arrays.toString(getData()) + "]";
        }
        
        
        
    }


    
    public static class CalculateNALTestScore implements CalculateScore {
        private final int maxCycles;

        final float parallelizationPenalizationRate = 0f; //scale of score divisor for parallel
        

        public CalculateNALTestScore(int maxCycles) {
            this.maxCycles = maxCycles;
        }

        
    public static double score(int maxCycles, NAR n) {


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
    
            @Override
            public double calculateScore(MLMethod phenotype) {
		
                try {
                    NARGenome genome = (NARGenome) phenotype;

                    System.out.print("    " + genome.toString());

                    final NAR n = genome.newNAR();

                    double s = score(maxCycles, n);
                    
                    //divide score based on degree of parallelism
                    s/=(1.0 + ( (genome.i("conceptsFiredPerCycle")-1.0) * parallelizationPenalizationRate));
                    
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
                return true;
            }

            @Override public boolean requireSingleThreaded() {
                return true;
            }
        
    }
    
    private TrainEA genetic;

    public GeneticSearch() throws Exception {


        File file = new File("/home/me/Downloads/default_nar_param_genetic." + new Date().toString() + ".txt");
        
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        
        
        
        System.setOut(ps);
        
        
        System.out.println(param);
        

        System.out.println("Default Score: " + CalculateNALTestScore.score(maxCycles, new DefaultNARBuilder().build() ));
        
        while (true) {
    
            Population pop = initPopulation(populationSize);

            genetic = new TrainEA(pop, new CalculateNALTestScore(maxCycles));
            genetic.setShouldIgnoreExceptions(true);
            genetic.setThreadCount(1);

            genetic.addOperation(0.1, new Splice(3));
            genetic.addOperation(0.1, new Splice(2));
            genetic.addOperation(0.1, new Splice(1));
            genetic.addOperation(0.1, new MutateShuffle());            
            genetic.addOperation(0.5, new MutatePerturb(0.2f));


            for (int i = 0; i < generationsPerPopulation; i++) {
                genetic.iteration();            

                NARGenome g = (NARGenome)genetic.getBestGenome();
                
                System.out.println("  BEST: " + g.getScore() + " " + Arrays.toString(g.getData()) );
            }
        }
    }

    private Population initPopulation(int populationSize) {        
        Population result = new BasicPopulation(populationSize, null);

        BasicSpecies defaultSpecies = new BasicSpecies();
        defaultSpecies.setPopulation(result);
        
        for (int i = 0; i < populationSize; i++) {
            final DoubleArrayGenome genome = NARGenome.newRandom();            
            defaultSpecies.getMembers().add(genome);
        }
        
        result.setGenomeFactory(new DoubleArrayGenomeFactory(param.size()) {
 
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


    public static void main(String[] args) throws Exception {
        new GeneticSearch();
    }

    public static double  r(double  min, double max) {
        return (Math.random() * (1+max-min) + min);
    }
    public static double  minmax(double  x, double min, double  max) {
        return Math.min(Math.max(x, min), max);
    }
        
}
