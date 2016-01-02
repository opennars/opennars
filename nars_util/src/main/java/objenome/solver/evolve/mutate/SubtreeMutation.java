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
import objenome.problem.ProblemSTGP;
import objenome.solver.evolve.*;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;
import objenome.solver.evolve.event.OperatorEvent;
import objenome.solver.evolve.event.OperatorEvent.EndOperator;
import objenome.solver.evolve.init.Grow;

import static objenome.solver.evolve.RandomSequence.RANDOM_SEQUENCE;
import static objenome.solver.evolve.TypedOrganism.MAXIMUM_DEPTH;
import static objenome.solver.evolve.TypedOrganism.SYNTAX;

/**
 * A mutation operator for <code>STGPIndividual</code>s that replaces a subtree
 * with a new randomly generated subtree.
 *
 * <p>
 * See the {@link #setup()} method documentation for a list of configuration
 * parameters used to control this operator.
 *
 * @see PointMutation
 *
 * @since 2.0
 */
public class SubtreeMutation extends AbstractOrganismOperator implements Listener<ConfigEvent> {

    /**
     * The key for setting and retrieving the probability of this operator being
     * applied
     */
    public static final GPKey<Double> PROBABILITY = new GPKey<>();

    private final Grow grower;

    // Configuration settings
    private RandomSequence random;
    private Integer maxDepth;
    private Double probability;
    private final boolean autoConfig;

    /**
     * Constructs a <code>SubtreeMutation</code> with control parameters
     * automatically loaded from the config
     */
    public SubtreeMutation() {
        this(true);
    }

    /**
     * Constructs a <code>SubtreeMutation</code> with control parameters
     * initially loaded from the config. If the <code>autoConfig</code> argument
     * is set to <code>true</code> then the configuration will be automatically
     * updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public SubtreeMutation(boolean autoConfig) {
        grower = new Grow(autoConfig);
        this.autoConfig = autoConfig;

    }

    /**
     * Sets up this operator with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li>{@link RandomSequence#RANDOM_SEQUENCE}
     * <li>{@link TypedOrganism#SYNTAX}
     * <li>{@link TypedOrganism#MAXIMUM_DEPTH}
     * <li>{@link #PROBABILITY}
     * </ul>
     */
    @Override
    public void setConfig(GPContainer config) {
        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }

        random = config.get(RANDOM_SEQUENCE);
        maxDepth = config.get(MAXIMUM_DEPTH);
        probability = config.get(PROBABILITY);

        grower.setRandomSequence(random);
        grower.setSyntax(config.get(SYNTAX));
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
        if (event.isKindOf(ProblemSTGP.PROBLEM, RANDOM_SEQUENCE, SYNTAX, MAXIMUM_DEPTH, PROBABILITY)) {
            setConfig(event.getConfig());

        }
    }

    /**
     * Performs a subtree mutation on the given individual. A mutation point is
     * randomly selected in the program tree. Then the subtree rooted at that
     * point is replaced with a randomly generated subtree. The replacement
     * subtree is generated using a grow initialisation method.
     *
     * @param parents an array of just one individual to undergo subtree
     * mutation. It must be an instance of <code>STGPIndividual</code>.
     * @return an array containing one <code>STGPIndividual</code> that was the
     * result of mutating the parent individual
     */
    @Override
    public TypedOrganism[] perform(EndOperator event, Organism... parents) {
        TypedOrganism child = (TypedOrganism) parents[0];

        // Randomly choose a mutation point
        int length = child.size();
        int mutationPoint = random.nextInt(length);

        // Calculate available depth
        int mutationPointDepth = nodeDepth(child.getRoot(), 0, mutationPoint, 0);
        int maxSubtreeDepth = maxDepth - mutationPointDepth;

        if (maxSubtreeDepth > 0) {
            // Grow a new subtree using the GrowInitialisation
            Node originalSubtree = child.getNode(mutationPoint);
            // TODO This should be using the parent's required type not the subtree's type
            grower.setReturnType(originalSubtree.dataType());
            grower.setMaximumDepth(maxSubtreeDepth);
            Node subtree = grower.createTree();

            child.setNode(mutationPoint, subtree);

            ((SubtreeMutationEndEvent) event).setMutationPoint(mutationPoint);
            ((SubtreeMutationEndEvent) event).setSubtree(subtree);

        }
        /*else {
            return (STGPIndividual[]) parents; //TODO make AbstractOperator generic to allow returning STGPIndividual[]
        }*/

        return new TypedOrganism[]{child};

    }

    /**
     * Returns a <code>SubtreeMutationEndEvent</code> with the operator and
     * parents set
     */
    @Override
    protected EndOperator getEndEvent(Organism... parents) {
        return new SubtreeMutationEndEvent(this, parents);
    }

    /*
     * Finds what depth a node with a given index is at. Returns -1 if the index
     * is not found.
     */
    private static int nodeDepth(Node root, int currentIndex, int targetIndex, int currentDepth) {
        nodeDepth:
        while (true) {
            // TODO This should be in a utilities class
            if (currentIndex == targetIndex) {
                return currentDepth;
            }

            for (int i = 0; i < root.getArity(); i++) {
                Node subtree = root.getChild(i);
                int subtreeLength = subtree.length();
                if (targetIndex <= currentIndex + subtreeLength) {
                    // Target is in this subtree
                    root = subtree;
                    currentIndex = currentIndex + 1;
                    currentDepth = currentDepth + 1;
                    continue nodeDepth;
                }
                currentIndex += subtreeLength;
            }
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Subtree mutation operates on 1 individual.
     *
     * @return {@inheritDoc}
     */
    @Override
    public int inputSize() {
        return 1;
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

        grower.setRandomSequence(random);
    }

    /**
     * Returns the array of nodes in the available syntax. Replacement subtrees
     * are generated using the nodes in this array.
     *
     * @return an array of the nodes in the syntax
     */
    public Node[] getSyntax() {
        return grower.getSyntax();
    }

    /**
     * Sets the array of nodes to generate replacement subtrees from. If
     * automatic configuration is enabled then any value set here will be
     * overwritten by the {@link TypedOrganism#SYNTAX} configuration setting on
     * the next config event.
     *
     * @param syntax an array of nodes to generate new program trees from
     */
    public void setSyntax(Node[] syntax) {
        grower.setSyntax(syntax);
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
     * An event fired at the end of a subtree mutation
     *
     * @see SubtreeMutation
     *
     * @since 2.0
     */
    public static class SubtreeMutationEndEvent extends OperatorEvent.EndOperator {

        private Node subtree;
        private int point;

        /**
         * Constructs a <code>SubtreeMutationEndEvent</code> with the details of
         * the event
         *
         * @param operator the operator that performed the mutation
         * @param parents the individual that the operator was performed on
         */
        public SubtreeMutationEndEvent(SubtreeMutation operator, Organism... parents) {
            super(operator, parents);
        }

        /**
         * Returns the index of the mutation point in the parent program tree
         *
         * @return the mutation point
         */
        public int getMutationPoint() {
            return point;
        }

        /**
         * Returns the root node of the replacement subtree
         *
         * @return the replacement subtree
         */
        public Node getSubtree() {
            return subtree;
        }

        /**
         * Sets the index of the mutation point in the parent program tree
         *
         * @param points the mutation point
         */
        public void setMutationPoint(int point) {
            this.point = point;
        }

        /**
         * Sets the replacement subtree that was used in the mutation
         *
         * @param subtree the subtree that was inserted in to the parent program
         * tree in the mutation
         */
        public void setSubtree(Node subtree) {
            this.subtree = subtree;
        }
    }

}
