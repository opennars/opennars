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
package objenome.solver.evolve.init;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import objenome.op.Node;
import objenome.solver.evolve.*;
import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.InitialisationEvent;
import objenome.solver.evolve.event.Listener;
import org.apache.commons.lang3.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

import static objenome.solver.evolve.Population.SIZE;
import static objenome.solver.evolve.RandomSequence.RANDOM_SEQUENCE;
import static objenome.solver.evolve.TypedOrganism.*;

/**
 * Initialisation method which produces <code>STGPIndividual</code>s with full
 * program trees to a specified depth. Program trees are constructed randomly
 * from the nodes in the syntax, with each node's data-type constraints
 * enforced.
 *
 * <p>
 * See the {@link #setup()} method documentation for a list of configuration
 * parameters used to control this operator.
 *
 * @see Grow
 * @see RampedHalfAndHalf
 *
 * @since 2.0
 */
public class Full implements TypedInitialization, Listener<ConfigEvent>, GPContainerAware {

    // Configuration settings
    private Node[] syntax; // TODO We don't really need to store this
    private RandomSequence random;
    private Class<?> returnType;
    private Integer populationSize;
    private Integer maxDepth;
    private Boolean allowDuplicates;

    // The contents of the syntax split
    private List<Node> terminals;
    private List<Node> nonTerminals;

    // Lookup table of the return types valid at each depth level
    private List<Class<?>>[] dataTypesTable;
    private final boolean autoConfig;

    /**
     * Constructs a <code>FullInitialisation</code> with control parameters
     * automatically loaded from the config
     */
    public Full() {
        this(true);
    }

    /**
     * Constructs a <code>FullInitialisation</code> with control parameters
     * initially loaded from the config. If the <code>autoConfig</code> argument
     * is set to <code>true</code> then the configuration will be automatically
     * updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public Full(boolean autoConfig) {
        // Default config values
        allowDuplicates = false;

        this.autoConfig = autoConfig;
    }

    public static boolean containsSub(Iterable<Class<?>> collection, Class<?> cls) {
        for (Class<?> c : collection)
            if (ClassUtils.isAssignable(c, cls))
                return true;
        return false;
    }


    /**
     * Sets up this operator with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li>{@link RandomSequence#RANDOM_SEQUENCE}
     * <li>{@link Population#SIZE}
     * <li>{@link TypedOrganism#SYNTAX}
     * <li>{@link TypedOrganism#RETURN_TYPE}
     * <li>{@link TypedOrganism#MAXIMUM_DEPTH}
     * <li>{@link TypedInitialization#MAXIMUM_INITIAL_DEPTH}
     * <li>{@link OrganismBuilder#ALLOW_DUPLICATES} (default:
     * <code>true</code>)
     * </ul>
     */
    protected void setup(GPContainer config) {
        random = config.get(RANDOM_SEQUENCE);
        populationSize = config.get(SIZE);
        syntax = config.get(SYNTAX);
        returnType = config.get(RETURN_TYPE);
        allowDuplicates = config.get(ALLOW_DUPLICATES, false);

        Integer maxDepth = config.get(MAXIMUM_DEPTH, this.maxDepth);
        Integer maxInitialDepth = config.get(MAXIMUM_INITIAL_DEPTH);

        // Use max initial depth if possible, unless it is greater than max depth
        if (maxInitialDepth != null && (maxDepth == null || maxInitialDepth < maxDepth)) {
            this.maxDepth = maxInitialDepth;
        } else {
            this.maxDepth = (maxDepth == null) ? -1 : maxDepth;
        }

        updateSyntax();
    }

    /*
     * Splits the syntax in to terminals and nonTerminals
     */
    private void updateSyntax() {
        terminals = new ArrayList<>();
        nonTerminals = new ArrayList<>();

        if (syntax != null) {
            for (Node n : syntax) {
                if (n.getArity() == 0) {
                    terminals.add(n);
                } else {
                    nonTerminals.add(n);
                }
            }
        }

        // Lookup table will need recreating
        dataTypesTable = null;
    }

    /**
     * Receives configuration events and triggers this fitness function to
     * configure its parameters if the <code>ConfigEvent</code> is for one of
     * its required parameters.
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void onEvent(ConfigEvent event) {
//        if (event.isKindOf(ProblemSTGP.PROBLEM, RANDOM_SEQUENCE, SIZE, SYNTAX, RETURN_TYPE, MAXIMUM_INITIAL_DEPTH, MAXIMUM_DEPTH, ALLOW_DUPLICATES)) {
//            //setup();
//            throw new UnsupportedOperationException("Unsupported yet");
//        }
//
//        // These will be expensive so only do them when we really have to
//        if (event.isKindOf(ProblemSTGP.PROBLEM, RETURN_TYPE)) {
//            dataTypesTable = null;
//        }
//        if (event.isKindOf(ProblemSTGP.PROBLEM, SYNTAX)) {
//            updateSyntax();
//        }
    }

    /**
     * Creates a population of new <code>STGPIndividuals</code> from the
     * <code>Node</code>s provided by the {@link TypedOrganism#SYNTAX} config
     * parameter. Each individual is created by a call to the
     * <code>createIndividual</code> method. The size of the population will be
     * equal to the {@link Population#SIZE} config parameter. If the
     * {@link OrganismBuilder#ALLOW_DUPLICATES} config parameter is set to
     * <code>false</code> then the individuals in the population will be unique
     * according to their <code>equals</code> methods. By default, duplicates
     * are allowed.
     *
     * @return a population of <code>STGPIndividual</code> objects
     */
    @Override
    public Population<TypedOrganism> createPopulation(Population<TypedOrganism> survivors, GPContainer config) {
        setup(config);


        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }

        config.fire(new InitialisationEvent.StartInitialisation());

        Population<TypedOrganism> population = new Population<>(config);
        
        //is it necessary to create a new population?
        for (TypedOrganism s : survivors) {
            population.add(s);
        }
        

        populate(population, (populationSize - survivors.size()));


        config.fire(new InitialisationEvent.EndInitialisation(population));

        return population;
    }

    @Override
    public void populate(Population<TypedOrganism> population, int num) {
        for (int i = 0; i < num; i++) {
            TypedOrganism individual;

            do {
                individual = newOrganism();
            } while (!allowDuplicates && population.contains(individual));

            population.add(individual);
        }
    }

    /**
     * Constructs a new <code>STGPIndividual</code> instance with a full program
     * tree composed of nodes provided by the {@link TypedOrganism#SYNTAX}
     * config parameter. Each node in the tree is randomly chosen from those
     * nodes with a valid data-type. If the maximum depth has not been reached
     * then the node is only selected from the valid non-terminals and at one
     * less than the maximum depth only the terminals are chosen from.
     *
     * @return a new individual with a full program tree
     */
    @Override
    public TypedOrganism newOrganism() {
        TypedOrganism t = new TypedOrganism(createTree());
        t.normalize();
        return t;
    }

    /**
     * Creates a full program tree to the maximum depth as specified by the
     * <code>getDepth</code> method. The nodes in the tree are randomly chosen
     * from those nodes in the syntax with a data-type that matches the
     * requirements of their parent (or the problem for the root node).
     *
     * @return the root node of the generated program tree
     * @throws IllegalStateException if the method is unable to create valid
     * program trees from the settings supplied
     */
    public Node createTree() {
        if (random == null) {
            throw new IllegalStateException("No random number generator has been set");
        }
        //noinspection IfStatementWithTooManyBranches
        if (returnType == null) {
            throw new IllegalStateException("No return type has been set");
        }
        if (maxDepth < 0) {
            throw new IllegalStateException("Depth must be 0 or greater");
        }
        if (terminals.isEmpty()) {
            throw new IllegalStateException("Syntax must include nodes with arity of 0");
        }
        if ((maxDepth > 0) && nonTerminals.isEmpty()) {
            throw new IllegalStateException("Syntax must include nodes with arity of >=1 if a depth >0 is used");
        }

        if (dataTypesTable == null) {
            updateDataTypesTable();
        }

        if (!containsSub(dataTypesTable[maxDepth], returnType)) {
            throw new IllegalStateException("Syntax is not able to produce full trees with the given return type (" + returnType + ") at depth " + maxDepth + " =" + Arrays.toString(dataTypesTable));
        }

        return createTree(returnType);
    }

    private Node createTree(Class<?> requiredType) {
        return createTree(requiredType, 0, null);
    }

    /*
     * Helper method for the createTree method. Recursively fills the children
     * of a node, to construct a full tree down to depth
     */
    private Node createTree(Class<?> requiredType, int currentDepth, List<Node> validNodeTemporary) {
        if (validNodeTemporary == null)
            validNodeTemporary = new ArrayList();
        else
            validNodeTemporary.clear();

        List<Node> validNodes = listValidNodes(maxDepth - currentDepth, requiredType, validNodeTemporary);

        if (validNodes.isEmpty()) {
            throw new IllegalStateException("Syntax is unable to create full node trees of given depth.");
        }

        int randomIndex = random.nextInt(validNodes.size());
        Node root = validNodes.get(randomIndex).newInstance();
        int arity = root.getArity();

        if (arity > 0) {
            // Construct list of arg sets that produce the right return type
            // TODO Surely we can cut down the number of calls to this?!
            Class<?>[][] argTypeSets = dataTypeCombinations(arity, dataTypesTable[maxDepth - currentDepth - 1]);
            List<Class<?>[]> validArgTypeSets = new ArrayList<>();
            for (Class<?>[] argTypes : argTypeSets) {
                Class<?> type = root.dataType(argTypes);
                if ((type != null) && requiredType.isAssignableFrom(type)) {
                    validArgTypeSets.add(argTypes);
                }
            }

            if (validArgTypeSets.isEmpty()) {
                throw new IllegalStateException("Syntax is unable to create full node trees of given depth.");
            }

            Class<?>[] argTypes = validArgTypeSets.get(random.nextInt(validArgTypeSets.size()));

            for (int i = 0; i < arity; i++) {
                root.setChild(i, createTree(argTypes[i], currentDepth + 1, validNodeTemporary));
            }
        }

        return root.normalize();
    }

    /*
     * Returns a list of nodes that are considered valid according to the
     * data-types lookup table, given the available depth and required
     * data-type.
     * 
     * We could make this protected to be overridden, but it only really makes
     * sense to do so if we allow the update of the data-types table to be
     * overridden too.
     */
    private List<Node> listValidNodes(int remainingDepth, Class<?> requiredType, List<Node> validNodeTemporary) {

        List<Node> nonTerminals = this.nonTerminals;

        validNodeTemporary.clear();

        if (remainingDepth > 0) {
            int nts = nonTerminals.size();
            for (Node n : nonTerminals) {
                Class<?>[][] argTypeSets = dataTypeCombinations(n.getArity(), dataTypesTable[remainingDepth - 1]);

                for (Class<?>[] argTypes : argTypeSets) {
                    Class<?> type = n.dataType(argTypes);
                    if ((type != null) && requiredType.isAssignableFrom(type)) {
                        validNodeTemporary.add(n);
                        break;
                    }
                }
            }
        } else {
            int nts = terminals.size();
            validNodeTemporary.addAll(terminals.stream().filter(n -> n.dataType().isAssignableFrom(requiredType)).collect(Collectors.toList()));
        }
        return validNodeTemporary;
    }

    public final Comparator<Class<?>> classNameComparator = new ClassComparator();
    
    /*
     * Generates the "type possibilities table" from the syntax and return
     * type, as described by Montana
     */
    private void updateDataTypesTable() {
        dataTypesTable = new List[maxDepth +1];//new Class<?>[depth + 1][];

        // Trees of depth 0 must be single terminal element
        Set<Class<?>> types = new TreeSet<>(classNameComparator);
        for (Node n : terminals) {
            types.add(n.dataType());
        }
        dataTypesTable[0] = new ArrayList(types);

        // Handle depths above 1
        for (int i = 1; i <= maxDepth; i++) {
            types = new TreeSet<>(classNameComparator); //sorted
            for (Node n : nonTerminals) {
                Class<?>[][] argTypeSets = dataTypeCombinations(n.getArity(), dataTypesTable[i - 1]);

                // Test each possible set of arguments
                for (Class<?>[] argTypes : argTypeSets) {
                    Class<?> returnType = n.dataType(argTypes);
                    if (returnType != null) {
                        types.add(returnType);
                    }
                }
            }
            dataTypesTable[i] = new ArrayList(types);
        }
    }
    
    public final Table<Integer, List<Class<?>>, Class<?>[][]> combinations = HashBasedTable.create();

    /*
     * Generates all possible combinations of the given data-types, with arity
     * positions
     * 
     * TODO We should only do this once at each depth for a particular arity
     */
    private Class<?>[][] dataTypeCombinations(int arity, List<Class<?>>dataTypes) {
        
        Class<?>[][] possibleTypes = combinations.get(arity, dataTypes);
        if (possibleTypes!=null) {
            return possibleTypes;
        }
        
        int noTypes = dataTypes.size();
        int noCombinations = (int) Math.pow(noTypes, arity);
        possibleTypes = new Class<?>[noCombinations][arity];

        for (int i = 0; i < arity; i++) {
            int period = (int) Math.pow(noTypes, i);

            for (int j = 0; j < noCombinations; j++) {
                int group = j / period;
                possibleTypes[j][i] = dataTypes.get(group % noTypes);
            }
        }
        
        combinations.put(arity, dataTypes, possibleTypes);

        return possibleTypes;
    }

    /**
     * Returns whether or not duplicates are currently allowed in generated
     * populations
     *
     * @return <code>true</code> if duplicates are currently allowed in
     * populations generated by the <code>createPopulation</code> method and
     * <code>false</code> otherwise
     */
    public boolean isDuplicatesEnabled() {
        return allowDuplicates;
    }

    /**
     * Sets whether duplicates should be allowed in populations that are
     * generated. If automatic configuration is enabled then any value set here
     * will be overwritten by the {@link OrganismBuilder#ALLOW_DUPLICATES}
     * configuration setting on the next config event.
     *
     * @param allowDuplicates whether duplicates should be allowed in
     * populations that are generated
     */
    public void setDuplicatesEnabled(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    /**
     * Returns the array of nodes in the available syntax. Program trees are
     * generated using the nodes in this array.
     *
     * @return an array of the nodes in the syntax
     */
    public Node[] getSyntax() {
        return syntax;
    }

    /**
     * Sets the array of nodes to generate program trees from. If automatic
     * configuration is enabled then any value set here will be overwritten by
     * the {@link TypedOrganism#SYNTAX} configuration setting on the next
     * config event.
     *
     * @param syntax an array of nodes to generate new program trees from
     */
    public void setSyntax(Node[] syntax) {
        this.syntax = syntax;

        updateSyntax();
    }

    /**
     * Returns the required data-type of the return for program trees generated
     * with this initialisation method
     *
     * @return the return type of the program trees generated
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Sets the required data-type of the program trees generated. If automatic
     * configuration is enabled then any value set here will be overwritten by
     * the {@link TypedOrganism#RETURN_TYPE} configuration setting on the next
     * config event.
     *
     * @param returnType the data-type of the generated programs
     */
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;

        // Lookup table will need regenerating
        dataTypesTable = null;
    }

    /**
     * Returns the number of individuals to be generated in a population created
     * by the <code>createPopulation</code> method
     *
     * @return the size of the populations generated
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * Sets the number of individuals to be generated in a population created by
     * the <code>createPopulation</code> method. If automatic configuration is
     * enabled then any value set here will be overwritten by the
     * {@link Population#SIZE} configuration setting on the next config event.
     *
     * @param size the size of the populations generated
     */
    public void setPopulationSize(int size) {
        populationSize = size;
    }

    /**
     * Returns the depth of the program trees generated with this initialisation
     * method
     *
     * @return the depth of the program trees constructed
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Sets the depth of the program trees created by the
     * <code>createIndividual</code> method. If automatic configuration is
     * enabled then any value set here will be overwritten by the
     * {@link TypedInitialization#MAXIMUM_INITIAL_DEPTH} configuration setting on
     * the next config event, or the {@link TypedOrganism#MAXIMUM_DEPTH}
     * setting if no initial maximum depth is set.
     *
     * @param maxDepth the depth of all program trees generated
     */
    public void setMaxDepth(int maxDepth) {
        if (dataTypesTable != null && maxDepth >= dataTypesTable.length) {
            // Types possibilities table needs extending
            // TODO No need to regenerate the whole table, just extend it
            dataTypesTable = null;
        }

        this.maxDepth = maxDepth;
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

    @Override
    public void setConfig(GPContainer c) {
        setup(c);
    }

    private static class ClassComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
            //return String.compare(o1.getName(), o2.getName());
        }

    }
}
