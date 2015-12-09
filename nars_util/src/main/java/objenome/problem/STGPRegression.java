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
package objenome.problem;

import nars.util.data.random.MersenneTwisterFast;
import objenome.goal.DoubleFitness;
import objenome.goal.HitsCount;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.op.math.Add;
import objenome.op.math.DivisionProtected;
import objenome.op.math.Multiply;
import objenome.op.math.Subtract;
import objenome.solver.evolve.*;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.TournamentSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This template sets up EpochX to run the cubic regression benchmark with the
 * STGP representation. Cubic regression involves evolving an equivalent
 * function to the formula: x + x^2 + x^3
 *
 * The following configuration is used:
 *
 * <li>{@link Population#SIZE}: <code>100</code>
 * <li>{@link GenerationalStrategy#TERMINATION_CRITERIA}:
 * <code>MaximumGenerations</code>, <code>TerminationFitness(0.0)</code>
 * <li>{@link MaximumGenerations#MAXIMUM_GENERATIONS}: <code>50</code>
 * <li>{@link TypedOrganism#MAXIMUM_DEPTH}: <code>6</code>
 * <li>{@link BranchedBreeder#SELECTOR}: <code>TournamentSelector</code>
 * <li>{@link TournamentSelector#TOURNAMENT_SIZE}: <code>7</code>
 * <li>{@link Breeder#OPERATORS}: <code>SubtreeCrossover</code>,
 * <code>SubtreeMutation</code>
 * <li>{@link SubtreeMutation#PROBABILITY}: <code>0.0</code>
 * <li>{@link SubtreeCrossover#PROBABILITY}: <code>1.0</code>
 * <li>{@link Initialiser#METHOD}: <code>FullInitialisation</code>
 * <li>{@link RandomSequence#RANDOM_SEQUENCE}: <code>MersenneTwisterFast</code>
 * <li>{@link TypedOrganism#SYNTAX}: <code>AddFunction</code>,
 * <code>SubtractFunction</code>, <code>MultiplyFunction<code>,
 * <code>DivisionProtectedFunction<code>, <code>VariableNode("X", Double)<code>
 * <li>{@link TypedOrganism#RETURN_TYPE}: <code>Double</code>
 * <li>{@link FitnessEvaluator#FUNCTION}: <code>HitsCount</code>
 * <li>{@link HitsCount#POINT_ERROR}: <code>0.01</code>
 * <li>{@link HitsCount#INPUT_VARIABLES}: <code>X</code>
 * <li>{@link HitsCount#INPUT_VALUE_SETS}: [20 random values between -1.0 and
 * +1.0]
 * <li>{@link HitsCount#EXPECTED_OUTPUTS}: [correct output for input value sets]
 *
 * @since 2.0
 */
public class STGPRegression extends ProblemSTGP {

    int functionPoints;
    
    public final Variable x;
    
    /**
     * Sets up the given template with the benchmark config settings
     *
     * Function is evaluated at N points of func on -1..+1
     * 
     * @param template a map to be filled with the template config
     */    
    public STGPRegression(int functionPoints, Function<Double,Double> func) {
        this.functionPoints = functionPoints;
        
        the(Population.SIZE, 100);
        List<PopulationTermination> criteria = new ArrayList<>();
        criteria.add(new TerminationFitness(new DoubleFitness.Minimize(0.0)));
        criteria.add(new MaximumGenerations());
        the(EvolutionaryStrategy.TERMINATION_CRITERIA, criteria);
        the(MaximumGenerations.MAXIMUM_GENERATIONS, 150);
        the(TypedOrganism.MAXIMUM_DEPTH, 6);

        the(Breeder.SELECTOR, new TournamentSelector(7));

        List<OrganismOperator> operators = new ArrayList<>();
        operators.add(new SubtreeCrossover());
        operators.add(new SubtreeMutation());
        the(Breeder.OPERATORS, operators);
        the(SubtreeCrossover.PROBABILITY, 1.0);
        the(SubtreeMutation.PROBABILITY, 0.0);
        the(Initialiser.METHOD, new Full());

        RandomSequence randomSequence = new MersenneTwisterFast();
        the(RandomSequence.RANDOM_SEQUENCE, randomSequence);

        // Setup syntax        
        the(TypedOrganism.SYNTAX, new Node[]{
            new Add(),
            new Subtract(),
            new Multiply(),
            new DivisionProtected(),
            new VariableNode( x = Variable.make("X", Double.class) )
        });
        the(TypedOrganism.RETURN_TYPE, Double.class);

        // Generate inputs and expected outputs        
        Double[][] inputsGiven = new Double[functionPoints][1];
        Double[] expectedOutputs = new Double[functionPoints];
        for (int i = 0; i < functionPoints; i++) {
            // Inputs values between -1.0 and +1.0
            inputsGiven[i][0] = (randomSequence.nextDouble() * 2) - 1;
            expectedOutputs[i] = func.apply(inputsGiven[i][0]);
        }

        // Setup fitness function
        the(FitnessEvaluator.FUNCTION, new HitsCount());
        the(HitsCount.POINT_ERROR, 0.01);
        the(HitsCount.INPUT_VALUE_SETS, inputsGiven);
        the(HitsCount.EXPECTED_OUTPUTS, expectedOutputs);
    }
}
