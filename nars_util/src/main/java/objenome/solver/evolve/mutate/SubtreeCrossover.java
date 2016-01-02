/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
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
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve.mutate;

import objenome.op.Node;
import objenome.solver.evolve.*;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;
import objenome.solver.evolve.event.OperatorEvent;
import objenome.solver.evolve.event.OperatorEvent.EndOperator;

import java.util.ArrayList;
import java.util.List;

import static objenome.solver.evolve.RandomSequence.RANDOM_SEQUENCE;
import static objenome.solver.evolve.TypedOrganism.MAXIMUM_DEPTH;

/**
 * A crossover operator for <code>STGPIndividual</code>s that exchanges subtrees
 * in two individuals. A bias can optionally be set to influence the probability
 * that a terminal or a non-terminal is selected as the crossover points.
 *
 * <p>
 * See the {@link #setup()} method documentation for a list of configuration
 * parameters used to control this operator.
 *
 * @see KozaCrossover
 * @see OnePointCrossover
 *
 * @since 2.0
 */
public class SubtreeCrossover extends AbstractOrganismOperator implements Listener<ConfigEvent> {

    /**
     * The key for setting and retrieving the probability with which a terminal
     * will be selected as the crossover point
     */
    public static final GPKey<Double> TERMINAL_PROBABILITY = new GPKey<>();

    /**
     * The key for setting and retrieving the probability of this operator being
     * applied
     */
    public static final GPKey<Double> PROBABILITY = new GPKey<>();

    // Configuration settings
    private RandomSequence random;
    private Double terminalProbability;
    private Double probability;
    private Integer maxDepth;
    private final boolean autoConfig;

    /**
     * Constructs a <code>SubtreeCrossover</code> with control parameters
     * automatically loaded from the config
     */
    public SubtreeCrossover() {
        this(true);
    }

    /**
     * Constructs a <code>SubtreeCrossover</code> with control parameters
     * initially loaded from the config. If the <code>autoConfig</code> argument
     * is set to <code>true</code> then the configuration will be automatically
     * updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public SubtreeCrossover(boolean autoConfig) {
        // Default config values
        terminalProbability = -1.0;

        this.autoConfig = autoConfig;
    }

    /**
     * Sets up this operator with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li>{@link RandomSequence#RANDOM_SEQUENCE}
     * <li>{@link #TERMINAL_PROBABILITY} (default: <code>-1.0</code>)
     * <li>{@link #PROBABILITY}
     * <li>{@link TypedOrganism#MAXIMUM_DEPTH}
     * </ul>
     */
    @Override
    public void setConfig(GPContainer config) {
        super.setConfig(config);
        
        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }
        random = config.get(RANDOM_SEQUENCE);
        terminalProbability = config.get(TERMINAL_PROBABILITY, terminalProbability);
        probability = config.get(PROBABILITY);
        
        maxDepth = config.get(MAXIMUM_DEPTH);
    }

    /**
     * Receives configuration events and triggers this operator to configure its
     * parameters if the <code>ConfigEvent</code> is for one of its required
     * parameters.
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void onEvent(ConfigEvent event) {
        if (event.isKindOf(RANDOM_SEQUENCE, TERMINAL_PROBABILITY, PROBABILITY, MAXIMUM_DEPTH)) {
            setConfig(event.getConfig());

        }
    }

    /**
     * Performs a subtree crossover on the given individuals. A crossover point
     * is randomly chosen in both programs and the subtrees at these points are
     * exchanged.
     *
     * @param parents an array of two individuals to undergo subtree crossover.
     * Both individuals must be instances of <code>STGPIndividual</code>.
     * @return an array containing two <code>STGPIndividual</code>s that are the
     * result of the crossover
     */
    @Override
    public TypedOrganism[] perform(EndOperator event, Organism... parents) {
        TypedOrganism program1 = (TypedOrganism) parents[0].clone();
        TypedOrganism program2 = (TypedOrganism) parents[1].clone();

        // Select first swap point
        int swapPoint1 = crossoverPoint(program1);
        Node subtree1 = program1.getNode(swapPoint1);// .clone();

        // Find which nodes in program2 have a matching return type to subtree1
        Class<?> subtree1Type = subtree1.dataType();
        List<Node> matchingNodes = new ArrayList<>();
        List<Integer> matchingIndexes = new ArrayList<>();
        nodesOfType(program2.getRoot(), subtree1Type, 0, matchingNodes, matchingIndexes);

        if (!matchingNodes.isEmpty()) {
            // Select second swap point with the same data-type
            int index = selectNodeIndex(matchingNodes);
            Node subtree2 = matchingNodes.get(index);
            int swapPoint2 = matchingIndexes.get(index);

            if (swapPoint2 < program2.size()) {
                program1.setNode(swapPoint1, subtree2);
                program2.setNode(swapPoint2, subtree1);

                // Check the depths are valid
                int depth1 = program1.depth();
                int depth2 = program2.depth();

                TypedOrganism[] children = new TypedOrganism[2];

                //noinspection IfStatementWithTooManyBranches
                if (depth1 <= maxDepth && depth2 <= maxDepth) {
                    children = new TypedOrganism[]{program1, program2};
                } else if (depth1 <= maxDepth) {
                    children  = new TypedOrganism[]{program1};
                } else if (depth2 <= maxDepth) {
                    children  = new TypedOrganism[]{program2};
                } else {
                    children  = new TypedOrganism[0];
                }

                int[] swapPoints = {swapPoint1, swapPoint2};
                Node[] subtrees = {subtree1, subtree2};

                ((EndEvent) event).setCrossoverPoints(swapPoints);
                ((EndEvent) event).setSubtrees(subtrees);

                return children;

            }
        }

        return new TypedOrganism[] { program1, program2 };

    }

    /**
     * Returns a <code>SubtreeCrossoverEndEvent</code> with the operator and
     * parents set
     */
    @Override
    protected EndEvent getEndEvent(Organism... parents) {
        return new EndEvent(this, parents);
    }

    /*
     * Fills the 'matching' list argument with all the nodes in the tree rooted
     * at 'root' that have a data-type that equals the 'type' argument. The
     * 'indexes' list is filled with the index of each of those nodes.
     */
    private static int nodesOfType(Node root, Class<?> type, int current, List<Node> matching, List<Integer> indexes) {
        if (root.dataType() == type) {
            matching.add(root);
            indexes.add(current);
        }

        for (int i = 0; i < root.getArity(); i++) {
            current = nodesOfType(root.getChild(i), type, current + 1, matching, indexes);
        }

        return current;
    }

    /**
     * Chooses a crossover point at random in the program tree of the given
     * individual. The probability that a terminal is selected is equal to the
     * result of the <code>getTerminalProbability()</code> method. If the
     * terminal probability is -1.0 then all nodes will be selected from at
     * random.
     *
     * @param individual the individual to choose a crossover point in
     * @return the index of the crossover point selected in the given
     * individual's program tree.
     */
    protected int crossoverPoint(TypedOrganism individual) {
        double terminalProbability = getTerminalProbability();

        int length = individual.size();
        if (terminalProbability == -1.0) {
            return random.nextInt(length);
        }

        int noTerminals = individual.getRoot().countTerminals();
        int noNonTerminals = length - noTerminals;

        if ((noNonTerminals > 0) && (random.nextDouble() >= terminalProbability)) {
            int f = random.nextInt(noNonTerminals);
            return individual.getRoot().nthNonTerminalIndex(f);
        } else {
            int t = random.nextInt(noTerminals);
            return individual.getRoot().nthTerminalIndex(t);
        }
    }

    /**
     * Selects a node in the list of nodes given and returns the index. The node
     * is selected at random from the list, but the probability that a terminal
     * will be selected is equal to the result of the
     * <code>getTerminalProbability()</code> method. If the terminal probability
     * is set to -1.0 then all nodes are selected from with equal probability.
     *
     * @param nodes the list of nodes to select from
     * @return the index of the node that was selected
     */
    protected int selectNodeIndex(List<Node> nodes) {
        double terminalProbability = getTerminalProbability();

        if (terminalProbability == -1.0) {
            return random.nextInt(nodes.size());
        } else {
            List<Integer> terminalIndexes = new ArrayList<>();
            List<Integer> nonTerminalIndexes = new ArrayList<>();

            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getArity() == 0) {
                    terminalIndexes.add(i);
                } else {
                    nonTerminalIndexes.add(i);
                }
            }

            return (!nonTerminalIndexes.isEmpty()) && (random.nextDouble() >= terminalProbability) ? nonTerminalIndexes.get(random.nextInt(nonTerminalIndexes.size())) : terminalIndexes.get(random.nextInt(terminalIndexes.size()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Subtree crossover operates on 2 individuals.
     *
     * @return {@inheritDoc}
     */
    @Override
    public int inputSize() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double probability() {
        return probability;
    }

    /**
     * Sets the probability of this operator being selected
     *
     * @param probability the new probability to set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the random number sequence in use
     *
     * @return the currently set random sequence
     */
    public RandomSequence getRandomSequence() {
        return random;
    }

    /**
     * Sets the random number sequence to use. If automatic configuration is
     * enabled then any value set here will be overwritten by the
     * {@link RandomSequence#RANDOM_SEQUENCE} configuration setting on the next
     * config event.
     *
     * @param random the random number generator to set
     */
    public void setRandomSequence(RandomSequence random) {
        this.random = random;
    }

    /**
     * Returns the probability that a terminal will be selected as the crossover
     * point
     *
     * @return the probability of a terminal being selected or -1.0 to indicate
     * equal probability for all nodes
     */
    public double getTerminalProbability() {
        return terminalProbability;
    }

    /**
     * Sets the probability that a terminal should be selected as the crossover
     * point. The value should be between 0.0 and 1.0, or 1.0 to indicate that
     * an equal probability should be assigned to all nodes. If automatic
     * configuration is enabled then any value set here will be overwritten by
     * the {@link SubtreeCrossover#TERMINAL_PROBABILITY} configuration setting
     * on the next config event.
     *
     * @param terminalProbability the probability that a terminal should be
     * selected
     */
    public void setTerminalProbability(double terminalProbability) {
        this.terminalProbability = terminalProbability;
    }

    /**
     * Returns the maximum depth for program trees that are returned from this
     * operator
     *
     * @return the maximum depth for program trees
     */
    public int getMaximumDepth() {
        return maxDepth;
    }

    /**
     * Sets the maximum depth for program trees returned from this operator. If
     * automatic configuration is enabled then any value set here will be
     * overwritten by the {@link TypedOrganism#MAXIMUM_DEPTH} configuration
     * setting on the next config event.
     *
     * @param maxDepth the maximum depth for program trees
     */
    public void setMaximumDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * An event fired at the end of a subtree crossover
     *
     * @see SubtreeCrossover
     *
     * @since 2.0
     */
    public static class EndEvent extends OperatorEvent.EndOperator {

        private Node[] subtrees;
        private int[] points;

        /**
         * Constructs a <code>SubtreeCrossoverEndEvent</code> with the details
         * of the event
         *
         * @param operator the operator that performed the crossover
         * @param parents an array of two individuals that the operator was
         * performed on
         */
        public EndEvent(SubtreeCrossover operator, Organism[] parents) {
            super(operator, parents);
        }

        /**
         * Returns an array of the two crossover points in the parent program
         * trees
         *
         * @return an array containing two indices which are the crossover
         * points
         */
        public int[] getCrossoverPoints() {
            return points;
        }

        /**
         * Returns an array of nodes which are the root nodes of the subtrees
         * that were exchanged. The nodes are given in the same order as the
         * parents they were taken from
         *
         * @return the subtrees
         */
        public Node[] getSubtrees() {
            return subtrees;
        }

        /**
         * Sets the indices of the crossover points in the parent program trees
         *
         * @param points an array of the crossover points
         */
        public void setCrossoverPoints(int[] points) {
            this.points = points;
        }

        /**
         * Sets the subtrees that were exchanged in the crossover. These should
         * be in the same order as the parents from which they were taken.
         *
         * @param subtrees an array of nodes which are the root nodes of the
         * subtrees that were exchanged
         */
        public void setSubtrees(Node[] subtrees) {
            this.subtrees = subtrees;
        }
    }

}
