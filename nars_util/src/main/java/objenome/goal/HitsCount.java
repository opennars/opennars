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
package objenome.goal;


import objenome.op.Variable;
import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.TypedOrganism;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;

/**
 * A fitness function for <code>STGPIndividual</code>s that calculates and
 * assigns <code>DoubleFitness.Minimise</code> scores. The fitness scores are
 * calculated by executing the program for each of the provided sets of inputs.
 * The results are compared to the expected outputs and a count of the number of
 * correct results is given as the fitness value. Can work with doubles or other
 * object types. If doubles are used then the point error option is used,
 * otherwise the objects are just compared for equality.
 *
 * When using this fitness function the
 * {@link #INPUT_VARIABLES}, {@link #INPUT_VALUE_SETS} and
 * {@link #EXPECTED_OUTPUTS} config options must be set, or the same values set
 * using the mutator methods provided. The length of the INPUT_VALUE_SETS array
 * should match the length of the EXPECTED_OUTPUTS array and the number of
 * values in each set should match the length of the INPUT_VARIABLES array.
 *
 * @since 2.0
 */
public class HitsCount extends TypedFitnessFunction implements Listener<ConfigEvent>, GPContainerAware {

    /**
     * The key for setting the expected output values from the programs being
     * evaluated
     */
    public static final GPKey<Double[]> EXPECTED_OUTPUTS = new GPKey<>();

    /**
     * The key for setting the acceptable error for each point to count as a hit
     */
    public static final GPKey<Double> POINT_ERROR = new GPKey<>();

    // Configuration settings

    private Variable<Object>[] inputVariables;

    private Object[][] inputValueSets;
    private Object[] expectedOutputs;

    private Double pointError;
    private final boolean autoConfig;


    /**
     * Constructs a <code>HitsCount</code> fitness function with control
     * parameters automatically loaded from the config.
     */
    public HitsCount() {
        this(true);
    }

    /**
     * Constructs a <code>HitsCount</code> fitness function with control
     * parameters initially loaded from the config. If the
     * <code>autoConfig</code> argument is set to <code>true</code> then the
     * configuration will be automatically updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public HitsCount(boolean autoConfig) {
        this.autoConfig = autoConfig;

    }

    /**
     * Sets up this operator with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li>{@link #INPUT_VARIABLES}
     * <li>{@link #INPUT_VALUE_SETS}
     * <li>{@link #EXPECTED_OUTPUTS}
     * <li>{@link #POINT_ERROR}
     * </ul>
     */
    @Override
    public void setConfig(GPContainer config) {
        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }

        inputVariables = config.get(INPUT_VARIABLES, config.getVariables());
        inputValueSets = config.get(INPUT_VALUE_SETS);
        expectedOutputs = config.get(EXPECTED_OUTPUTS);
        pointError = config.get(POINT_ERROR, pointError);
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
        if (event.isKindOf(INPUT_VARIABLES, INPUT_VALUE_SETS, EXPECTED_OUTPUTS, POINT_ERROR)) {
            setConfig(event.getConfig());
        }
    }

    /**
     * Calculates the fitness of the given individual. This fitness function
     * only operates on <code>STGPIndividual</code>s. The fitness returned will
     * be an instance of <code>DoubleFitness.Minimise</code>. The fitness score
     * is a count of the number of sets of inputs that produce a correct result
     * (or 'hit'). For double types a hit can have an error range, specified by
     * the {@link HitsCount#POINT_ERROR} config key.
     *
     * @param individual the program to evaluate
     * @return the fitness of the given individual
     * @throws IllegalArgumentException if the individual is not an
     * STGPIndividual
     */
    @Override
    public DoubleFitness.Minimize evaluate(Population population, TypedOrganism individual) {
        setConfig(population.getConfig());

//        if (!(individual instanceof TypedOrganism)) {
//            throw new IllegalArgumentException("Unsupported representation");
//        }
//
//        //TODO validate number of inputs etc
//        TypedOrganism program = (TypedOrganism) individual;

        double cost = getCost(individual);

        return new DoubleFitness.Minimize(cost);
    }


    public double getCost(TypedOrganism program) {

        double numWrong = 0;

        for (int i = 0; i < inputValueSets.length; i++) {
            // Update the variable values
            for (int j = 0; j < inputVariables.length; j++) {
                inputVariables[j].setValue(inputValueSets[i][j]);
            }

            // Run the program
            Object output = program.evaluate();

            if (!isHit(output, expectedOutputs[i])) {
                numWrong++;
            }
        }

        return numWrong;
    }

    /**
     * Decides whether a value returned by a program is considered to be a hit
     * or not, when compared to the expected result.
     *
     * @param result the result returned by the program
     * @param expectedResult the correct result
     * @return true if the result is considered to be a hit and false otherwise
     */
    protected boolean isHit(Object result, Object expectedResult) {
        if (result instanceof Double && expectedResult instanceof Double) {
            Double dblResult = (Double) result;
            Double dblExpectedResult = (Double) expectedResult;

            if (Double.isNaN(dblResult) && Double.isNaN(dblExpectedResult)) {
                return true;
            } else {
                double error = Math.abs(dblResult - dblExpectedResult);
                return (!Double.isNaN(error) && error <= pointError);
            }
        } else {
            return result.equals(expectedResult);
        }
    }

    /**
     * Returns the point error which defines the range allowable for double
     * values to be considered a hit.
     *
     * @return the point error for a double value to be considered a hit
     */
    public double getPointError() {
        return pointError;
    }

    /**
     * Sets the point error which defines the range allowable for double values
     * to be considered a hit. This is unused for non-Double fitness values.
     *
     * If automatic configuration is enabled then any value set here will be
     * overwritten by the {@link #POINT_ERROR} configuration setting on the next
     * config event.
     *
     * @param pointError the point error for a double value to be considered a
     * hit
     */
    public void setPointError(double pointError) {
        this.pointError = pointError;
    }

    /**
     * Gets the input variables that are currently set
     *
     * @return the current input variables
     */
    public Variable[] getInputVariables() {
        return inputVariables;
    }

    /**
     * Sets the input variables. These should be the variables used in the
     * terminal set, which will have the input values assigned to them.
     *
     * If automatic configuration is enabled then any value set here will be
     * overwritten by the {@link #INPUT_VARIABLES} configuration setting on the
     * next config event.
     *
     * @param inputVariables the input variables
     */
    public void setInputVariables(Variable[] inputVariables) {
        this.inputVariables = inputVariables;
    }

    /**
     * Returns the sets of input values.
     *
     * @return the sets of input values
     */
    public Object[][] getInputValueSets() {
        return inputValueSets;
    }

    /**
     * Sets the sets of input values. The length of the array should match the
     * length of the expected outputs array. Each set of values should have the
     * same number of values, equal to the length of the input variables array.
     *
     * If automatic configuration is enabled then any value set here will be
     * overwritten by the {@link #INPUT_VALUE_SETS} configuration setting on the
     * next config event.
     *
     * @param inputValueSets the sets of input values
     */
    public void setInputValueSets(Object[][] inputValueSets) {
        this.inputValueSets = inputValueSets;
    }

    /**
     * Returns the expected outputs that the actual outputs will be compared
     * against
     *
     * @return the expected outputs for the input sets
     */
    public Object[] getExpectedOutputs() {
        return expectedOutputs;
    }

    /**
     * Sets the expected outputs to compare against. The length of the array
     * should match the length of the input values array.
     *
     * If automatic configuration is enabled then any value set here will be
     * overwritten by the {@link #EXPECTED_OUTPUTS} configuration setting on the
     * next config event.
     *
     * @param expectedOutputs the expected outputs to compare against
     */
    public void setExpectedOutputs(Object[] expectedOutputs) {
        this.expectedOutputs = expectedOutputs;
    }
}
