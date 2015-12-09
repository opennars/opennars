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
import objenome.goal.HitsCount;
import objenome.goal.HitsCountAndMinified;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.op.bool.And;
import objenome.op.bool.Not;
import objenome.op.bool.Or;
import objenome.op.lang.If;
import objenome.solver.evolve.*;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.TournamentSelector;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This template sets up EpochX to run the 6-bit multiplexer benchmark with the
 * STGP representation. The 6-bit multiplexer problem involves evolving a
 * program which receives an array of 6 boolean values. The first 2 values are
 * address bits, which the program should convert into an index for which of the
 * remaining data registers to return. {a0, a1, d0, d1, d2, d3}.
 *
 * <table>
 * <tr>
 * <td>a0</td><td>a1</td><td>return value</td>
 * <td>false</td><td>false</td><td>d0</td>
 * <td>true</td><td>false</td><td>d1</td>
 * <td>false</td><td>true</td><td>d2</td>
 * <td>true</td><td>true</td><td>d3</td>
 * </tr>
 * </table>
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
 * <li>{@link TypedOrganism#SYNTAX}: <code>AndFunction</code>,
 * <code>OrFunction</code>, <code>NotFunction<code>,
 * <code>IfFunction<code>, <code>VariableNode("A0", Boolean)<code>, <code>VariableNode("A1", Boolean)<code>,
 * <code>VariableNode("D2", Boolean)<code>, <code>VariableNode("D3", Boolean)<code>, <code>VariableNode("D4", Boolean)<code>,
 * <code>VariableNode("D5", Boolean)<code>
 * <li>{@link TypedOrganism#RETURN_TYPE}: <code>Boolean</code>
 * <li>{@link FitnessEvaluator#FUNCTION}: <code>HitsCount</code>
 * <li>{@link HitsCount#INPUT_VARIABLES}: <code>A0</code>, <code>A1</code>,
 * <code>D2</code>, <code>D3</code>, <code>D4</code>, <code>D5</code>
 * <li>{@link HitsCount#INPUT_VALUE_SETS}: [all possible binary input
 * combinations]
 * <li>{@link HitsCount#EXPECTED_OUTPUTS}: [correct output for input value sets]
 *
 * @since 2.0
 */
public class STGPBoolean extends ProblemSTGP {

   
    public static class BooleanCases {
        
        public final Boolean[][] inputValues;
        public final Boolean[] expectedOutputs;

        public BooleanCases(Boolean[][] inputValues, Boolean[] expectedOutputs) {
            this.inputValues = inputValues;
            this.expectedOutputs = expectedOutputs;
        }
        
    }
 
    public STGPBoolean(BooleanCases c, int populationSize, int maxGenerations) {
        this(c.inputValues, c.expectedOutputs, populationSize, maxGenerations);
    }

    public STGPBoolean(Boolean[][] inputValues, Boolean[] expectedOutputs, int populationSize, int maxGenerations) {


        the(Population.SIZE, populationSize);
        
        
        the(EvolutionaryStrategy.TERMINATION_CRITERIA, newArrayList(new PopulationTermination[] {
            //new TerminationFitness(new DoubleFitness.Minimise(0.0)),
            new MaximumGenerations()
        }));
        
        the(MaximumGenerations.MAXIMUM_GENERATIONS, maxGenerations);
        the(TypedOrganism.MAXIMUM_DEPTH, 6);

        the(Breeder.SELECTOR, new TournamentSelector(7));

        the(SubtreeCrossover.PROBABILITY, 1.0);
        the(SubtreeMutation.PROBABILITY, 0.0);
        
        the(Breeder.OPERATORS, newArrayList(new OrganismOperator[] {
            new SubtreeCrossover(),
            new SubtreeMutation()           
        }));
        
        the(Initialiser.METHOD, new Full());        
        the(RandomSequence.RANDOM_SEQUENCE, new MersenneTwisterFast());

        // Setup syntax
        List<Node> syntaxList = new ArrayList<Node>() {{
            add(new And());
            add(new Or());
            add(new Not());
            add(new If());
        }};
        
        for (int i = 0; i < inputValues[0].length; i++) {
            syntaxList.add(new VariableNode(Variable.make("b" + i, Boolean.class)));
        }


        the(TypedOrganism.SYNTAX, syntaxList.toArray(new Node[syntaxList.size()]));
        the(TypedOrganism.RETURN_TYPE, Boolean.class);


        // Setup fitness function
        the(FitnessEvaluator.FUNCTION, new HitsCountAndMinified());
        the(HitsCount.INPUT_VALUE_SETS, inputValues);
        the(HitsCount.EXPECTED_OUTPUTS, expectedOutputs);
    }
}


/**
 * This template sets up EpochX to run the 11-bit multiplexer benchmark with the
 * STGP representation. The 11-bit multiplexer problem involves evolving a
 * program which receives an array of 11 boolean values. The first 3 values are
 * address bits, which the program should convert into an index for which of the
 * remaining data registers to return. {a0, a1, a2, d0, d1, d2, d3, d4, d5, d6,
 * d7}.
 *
 * <table>
 * <tr>
 * <td>a0</td><td>a1</td><td>a2</td><td>return value</td>
 * <td>false</td><td>false</td><td>false</td><td>d0</td>
 * <td>true</td><td>false</td><td>false</td><td>d1</td>
 * <td>false</td><td>true</td><td>false</td><td>d2</td>
 * <td>true</td><td>true</td><td>false</td><td>d3</td>
 * <td>false</td><td>false</td><td>true</td><td>d4</td>
 * <td>true</td><td>false</td><td>true</td><td>d5</td>
 * <td>false</td><td>true</td><td>true</td><td>d6</td>
 * <td>true</td><td>true</td><td>true</td><td>d7</td>
 * </tr>
 * </table>
 *
 * The following configuration is used:
 *
 * <li>{@link Population#SIZE}: <code>100</code>
 * <li>{@link GenerationalStrategy#TERMINATION_CRITERIA}:
 * <code>MaximumGenerations</code>, <code>TerminationFitness(0.0)</code>
 * <li>{@link MaximumGenerations#MAXIMUM_GENERATIONS}: <code>50</code>
 * <li>{@link STGPIndividual#MAXIMUM_DEPTH}: <code>6</code>
 * <li>{@link BranchedBreeder#SELECTOR}: <code>TournamentSelector</code>
 * <li>{@link TournamentSelector#TOURNAMENT_SIZE}: <code>7</code>
 * <li>{@link Breeder#OPERATORS}: <code>SubtreeCrossover</code>,
 * <code>SubtreeMutation</code>
 * <li>{@link SubtreeMutation#PROBABILITY}: <code>0.0</code>
 * <li>{@link SubtreeCrossover#PROBABILITY}: <code>1.0</code>
 * <li>{@link Initialiser#METHOD}: <code>FullInitialisation</code>
 * <li>{@link RandomSequence#RANDOM_SEQUENCE}: <code>MersenneTwisterFast</code>
 * <li>{@link STGPIndividual#SYNTAX}: <code>AndFunction</code>,
 * <code>OrFunction</code>, <code>NotFunction<code>,
 * <code>IfFunction<code>, <code>VariableNode("A0", Boolean)<code>, <code>VariableNode("A1", Boolean)<code>,
 * <code>VariableNode("A2", Boolean)<code>, <code>VariableNode("D3", Boolean)<code>, <code>VariableNode("D4", Boolean)<code>,
 * <code>VariableNode("D5", Boolean)<code>, <code>VariableNode("D6", Boolean)<code>, <code>VariableNode("D7", Boolean)<code>,
 * <code>VariableNode("D8", Boolean)<code>, <code>VariableNode("D9", Boolean)<code>, <code>VariableNode("D10", Boolean)<code>
 * <li>{@link STGPIndividual#RETURN_TYPE}: <code>Boolean</code>
 * <li>{@link FitnessEvaluator#FUNCTION}: <code>HitsCount</code>
 * <li>{@link HitsCount#INPUT_VARIABLES}: <code>A0</code>, <code>A1</code>,
 * <code>A2</code>, <code>D3</code>, <code>D4</code>, <code>D5</code>,
 * <code>D6</code>, <code>D7</code>, <code>D8</code>, <code>D9</code>,
 * <code>D10</code>
 * <li>{@link HitsCount#INPUT_VALUE_SETS}: [all possible binary input
 * combinations]
 * <li>{@link HitsCount#EXPECTED_OUTPUTS}: [correct output for input value sets]
 *
 * @since 2.0
 */