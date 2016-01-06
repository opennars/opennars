package jurls.core.approximation;///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.core.approximation;
//
//import java.util.ArrayList;
//import java.util.Deque;
//import java.util.List;
//import objenome.op.Node;
//import objenome.op.Variable;
//import objenome.op.VariableNode;
//import objenome.op.math.Add;
//import objenome.op.math.DivisionProtected;
//import objenome.op.math.DoubleERC;
//import objenome.op.math.Max2;
//import objenome.op.math.Min2;
//import objenome.op.math.ModuloProtected;
//import objenome.op.math.Multiply;
//import objenome.op.math.Power;
//import objenome.op.math.Subtract;
//import objenome.op.trig.Tangent;
//import objenome.problem.Observation;
//import objenome.problem.ProblemSTGP;
//import objenome.solver.evolve.BranchedBreeder;
//import objenome.solver.evolve.Breeder;
//import objenome.solver.evolve.EvolutionaryStrategy;
//import objenome.solver.evolve.FitnessEvaluator;
//import objenome.solver.evolve.Initialiser;
//import objenome.solver.evolve.MaximumGenerations;
//import objenome.solver.evolve.Operator;
//import objenome.solver.evolve.Population;
//import objenome.solver.evolve.RandomSequence;
//import objenome.solver.evolve.STGPIndividual;
//import objenome.solver.evolve.TerminationCriteria;
//import objenome.solver.evolve.TerminationFitness;
//import objenome.solver.evolve.fitness.DoubleFitness;
//import objenome.solver.evolve.fitness.SumOfError;
//import objenome.solver.evolve.init.Full;
//import objenome.solver.evolve.mutate.PointMutation;
//import objenome.solver.evolve.mutate.SubtreeCrossover;
//import objenome.solver.evolve.mutate.SubtreeMutation;
//import objenome.solver.evolve.selection.TournamentSelector;
//import objenome.util.random.MersenneTwisterFast;
//
///**
// *
// * @author me
// */
//public class GeneticParameterizedFunction extends AbstractParameterizedFunction {
//
//    final double xOffset = -0.5; //maps 0..1 to -0.5..+0.5
//
//    ProblemSTGP funcProblem = null;
//    private final STGPFunctionApproximation2 problem;
//    private Population<STGPIndividual> population;
//    private final int numPoints;
//    private float learningRate;
//    private STGPIndividual fittest;
//    private STGPIndividual prevFittest;
//
//    public GeneticParameterizedFunction(int numInputs, int numPoints, int populationSize, float learningRate, int expressionDepth, boolean arith, boolean trig, boolean exp, boolean piecewise) {
//        super(numInputs);
//
//        this.numPoints = numPoints;
//        this.learningRate = learningRate;
//
//        problem = new STGPFunctionApproximation2(populationSize, expressionDepth, arith, trig, exp, piecewise);
//
//    }
//
//
//    @Override
//    public int numberOfParameters() {
//        return numPoints;
//    }
//
//
//
//
//    public double valueExpression(double x) {
//        if (population == null) return 0;
//
//        problem.x.setValue(x);
//
//        return (Double)fittest.evaluate();
//    }
//
//    /** function approximation evaluated by best evolved expression */
//    @Override public double value(double[] xs) {
//        if (xs.length > 1)
//            throw new RuntimeException("Only one input variable supported currently");
//
//        double x = xs[0];
//
//        return valueExpression(x - xOffset);
//    }
//
//    @Override
//    public void learn(double[] X, double y) {
//
//        if (population!=null) {
//            population.cullThis(learningRate);
//        }
//
//        //System.out.println(X[0] + " " + y);
//
//        if (problem.samples.size() >= numPoints)
//            problem.samples.removeFirst();
//
//        problem.samples.add(new Observation(
//                new Double[] { X[0] - xOffset },
//                y,
//                /* weight */ 1.0)
//        );
//
//        population = problem.run();
//
//        prevFittest = fittest;
//        fittest = population.fittest();
//
//        if (!fittest.equals(prevFittest))
//            System.out.println(fittest);
//
//    }
//
//    @Override
//    public double getParameter(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void setParameter(int i, double v) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    /** variation of objenome's STGPFunctionApproximation */
//    public static class STGPFunctionApproximation2 extends ProblemSTGP {
//
//
//        public final Variable x;
//        public final Deque<Observation<Double[], Double>> samples;
//
//
//        public STGPFunctionApproximation2(int populationSize, int expressionDepth, boolean arith, boolean trig, boolean exp, boolean piecewise) {
//            super();
//
//
//            double elitismRate = 0.05;
//            int generationsPerIteration = 4;
//
//            the(Population.SIZE, populationSize);
//            List<TerminationCriteria> criteria = new ArrayList<>();
//            criteria.add(new TerminationFitness(new DoubleFitness.Minimise(0.0)));
//            criteria.add(new MaximumGenerations());
//            the(EvolutionaryStrategy.TERMINATION_CRITERIA, criteria);
//            the(MaximumGenerations.MAXIMUM_GENERATIONS, generationsPerIteration);
//            the(STGPIndividual.MAXIMUM_DEPTH, expressionDepth);
//
//            the(Breeder.SELECTOR, new TournamentSelector());
//            the(TournamentSelector.TOURNAMENT_SIZE, 7);
//            List<Operator> operators = new ArrayList<>();
//            operators.add(new PointMutation() {
//                /*
//                @Override
//                protected List<Node> validReplacements(Node n) {
//                    System.out.println("valid repl: " + n);
//                    if (n instanceof DoubleERC) {
//                        DoubleERC d = (DoubleERC)n;
//                        DoubleERC e = d.mutated(0.1);
//                        System.out.println("point mutation: " + d + " -> " + e);
//                        return Lists.newArrayList(
//                                e
//                        );
//                    }
//                    return super.validReplacements(n);
//                }*/
//
//            });
//            operators.add(new SubtreeCrossover());
//            operators.add(new SubtreeMutation());
//            the(Breeder.OPERATORS, operators);
//            the(BranchedBreeder.ELITISM, (int)(populationSize * elitismRate));
//            the(PointMutation.PROBABILITY, 0.33);
//            the(SubtreeCrossover.PROBABILITY, 0.33);
//            the(SubtreeMutation.PROBABILITY, 0.33);
//            the(Initialiser.METHOD, new Full());
//            //the(Initialiser.METHOD, new RampedHalfAndHalf());
//
//            RandomSequence randomSequence = new MersenneTwisterFast();
//            the(RandomSequence.RANDOM_SEQUENCE, randomSequence);
//
//            List<Node> syntax = new ArrayList();
//
//            //+2.0 allows it to grow
//            syntax.add( new DoubleERC(randomSequence, -1.0, 4.0, 4));
//
//            if (arith) {
//                syntax.add(new Add());
//                syntax.add(new Subtract());
//                syntax.add(new Multiply());
//                syntax.add(new DivisionProtected());
//                syntax.add(new ModuloProtected());
//            }
//            if (trig) {
//                syntax.add(new objenome.op.trig.Sine());
//                syntax.add(new Tangent());
//            }
//            if (exp) {
//                //syntax.add(new LogNatural());
//                //syntax.add(new Exp());
//                syntax.add(new Power());
//            }
//            if (piecewise) {
//                syntax.add(new Max2());
//                syntax.add(new Min2());
//            }
//
//            syntax.add( new VariableNode( x = new Variable("X", Double.class) ) );
//
//            // Setup syntax
//            the(STGPIndividual.SYNTAX, syntax.toArray(new Node[syntax.size()]));
//
//
//            the(STGPIndividual.RETURN_TYPE, Double.class);
//
//            SumOfError<Double,Double> fitness;
//
//
//            // Setup fitness function
//            the(FitnessEvaluator.FUNCTION, fitness = new SumOfError<Double,Double>());
//
//            samples = fitness.obs;
//        }
//
//    }
//
//
//
// }
