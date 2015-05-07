/* 
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http:/www.epochx.org
 */
package objenome.goal;

import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.op.math.*;
import objenome.op.trig.Sine;
import objenome.op.trig.Tangent;
import objenome.solver.evolve.*;
import objenome.solver.evolve.fitness.DoubleFitness;
import objenome.solver.evolve.fitness.SumOfError;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.PointMutation;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.TournamentSelector;
import objenome.util.random.MersenneTwisterFast;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Evolves a function that minimizes the total error of an expression
 * evaluated according to a set of sampled points.
 * @since 2.0
 */
public class STGPFunctionApproximation extends ProblemSTGP {

    
    public final Variable x;
    public final Deque<Observation<Double[], Double>> samples;

    final SumOfError<Double,Double> fitness = new SumOfError() {
        @Override public void onNextBest(STGPIndividual s, double error) {
            nextBest = s;
            nextBestError = error;
        }
    };
 
    public STGPFunctionApproximation(int populationSize, int expressionDepth, boolean arith, boolean trig, boolean exp, boolean piecewise) {        
        super();
        
        
        double elitismRate = 0.5 * populationSize;
        
        the(Population.SIZE, populationSize);
        List<TerminationCriteria> criteria = new ArrayList<>();
        criteria.add(new TerminationFitness(new DoubleFitness.Minimise(0.0)));
        criteria.add(new MaximumGenerations());
        the(EvolutionaryStrategy.TERMINATION_CRITERIA, criteria);
        the(MaximumGenerations.MAXIMUM_GENERATIONS, 150);
        the(STGPIndividual.MAXIMUM_DEPTH, expressionDepth);

        the(Breeder.SELECTOR, new TournamentSelector());
        the(TournamentSelector.TOURNAMENT_SIZE, 7);
        List<Operator> operators = new ArrayList<>();
        operators.add(new PointMutation());
        operators.add(new SubtreeCrossover());
        operators.add(new SubtreeMutation());
        the(Breeder.OPERATORS, operators);
        the(BranchedBreeder.ELITISM, (int)(populationSize * elitismRate));
        the(PointMutation.PROBABILITY, 0.3);
        the(SubtreeCrossover.PROBABILITY, 0.3);
        the(SubtreeMutation.PROBABILITY, 0.3);
        the(Initialiser.METHOD, new Full());
        //the(Initialiser.METHOD, new RampedHalfAndHalf());

        RandomSequence randomSequence = new MersenneTwisterFast();
        the(RandomSequence.RANDOM_SEQUENCE, randomSequence);

        List<Node> syntax = new ArrayList();
        
        //+2.0 allows it to grow
        syntax.add( new DoubleERC(randomSequence, -1.0, 2.0, 4));
        
        if (arith) {
            syntax.add(new Add());
            syntax.add(new Subtract());
            syntax.add(new Multiply());
            syntax.add(new DivisionProtected());            
        }
        if (trig) {
            syntax.add(new Sine());
            //syntax.add(new Tangent());
        }
        if (exp) {
            //syntax.add(new LogNatural());
            //syntax.add(new Exp());
            syntax.add(new Power());
        }
        if (piecewise) {
            syntax.add(new Min2());
        }
        
        syntax.add( new VariableNode( x = new Variable("X", Double.class) ) );
                
        // Setup syntax        
        the(STGPIndividual.SYNTAX, syntax.toArray(new Node[syntax.size()]));
            
        
        the(STGPIndividual.RETURN_TYPE, Double.class);


        // Setup fitness function
        the(FitnessEvaluator.FUNCTION, fitness);

        samples = fitness.obs;
    }

    STGPIndividual nextBest = null;
    double nextBestError = Double.NaN;

    public double getBestError() {
        return nextBestError;
    }

    public STGPIndividual getBest() {
        return nextBest;
    }
}
