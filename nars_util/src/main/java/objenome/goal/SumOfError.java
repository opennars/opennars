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

import objenome.op.Variable;
import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.TypedOrganism;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;

import java.util.ArrayDeque;
import java.util.Deque;

import static objenome.goal.HitsCount.EXPECTED_OUTPUTS;

/**
 * A fitness function for <code>STGPIndividual</code>s that calculates and
 * assigns <code>DoubleFitness.Minimise</code> scores. The fitness scores are
 * calculated by executing the program for each of the provided sets of inputs.
 * The difference between the value returned by the program and the expected
 * outputs supplied is summed to give the fitness value.
 *
 * When using this fitness function the
 * {@link #INPUT_VARIABLES}, {@link #INPUT_VALUE_SETS} and
 * {@link #EXPECTED_OUTPUTS} config options must be set, or the same values set
 * using the mutator methods provided. The length of the
 * <code>INPUT_VALUE_SETS</code> array should match the length of the
 * <code>EXPECTED_OUTPUTS</code> array and the number of values in each set
 * should match the length of the <code>INPUT_VARIABLES</code> array.
 *
 * If the program returns <code>NaN</code> for any of the input sets then a
 * fitness score of <code>NaN</code> is assigned by default, although this can
 * be changed by overriding the <code>nanFitnessScore</code> method.
 *
 * @since 2.0
 */
public class SumOfError<I,O> extends TypedFitnessFunction implements Listener<ConfigEvent>, GPContainer.GPContainerAware {

    /**
     * The key for setting the program's input variables
     */
    public static final GPKey<Variable[]> INPUT_VARIABLES = new GPKey<>();


    public final Deque<Observation<I[],O>> obs = new ArrayDeque();

    // Configuration settings
    private Variable[] inputVariables;
    private final boolean autoConfig;

    /**
     * Constructs a <code>SumOfError</code> fitness function with control
     * parameters automatically loaded from the config.
     */
    public SumOfError() {
        this(true);
    }

    /**
     * Constructs a <code>SumOfError</code> fitness function with control
     * parameters initially loaded from the config. If the
     * <code>autoConfig</code> argument is set to <code>true</code> then the
     * configuration will be automatically updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public SumOfError(boolean autoConfig) {
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
     * </ul>
     */
    @Override
    public void setConfig(GPContainer config) {
        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }

        //inputVariables = config.get(INPUT_VARIABLES);
        inputVariables = config.get(INPUT_VARIABLES, config.getVariables());
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
        if (event.isKindOf(/*PROBLEM, */INPUT_VARIABLES, INPUT_VALUE_SETS, EXPECTED_OUTPUTS)) {
            setConfig(event.getConfig());
        }
    }

    /**
     * Calculates the fitness of the given individual. This fitness function
     * only operates on STGPIndividuals with a Double data-type. The fitness
     * returned will be an instance of DoubleFitness.Minimise. The fitness score
     * is calculated as the sum of the difference between the expected outputs
     * and the actual outputs, for each set of inputs.
     *
     * @param individual the individual to evaluate the fitness of
     * @return the fitness of the given individual
     * @throws IllegalArgumentException if the individual is not an
     * STGPIndividual or the individual's data-type is not Double.
     */
    @Override
    public DoubleFitness.Minimize evaluate(Population population, TypedOrganism individual) {

        if (!(individual instanceof TypedOrganism)) {
            throw new IllegalArgumentException("Unsupported representation");
        }

        setConfig(population.getConfig());

        //TODO validate number of inputs etc

        if (individual.dataType() != Double.class) {
            throw new IllegalArgumentException("Unsupported data-type");
        }

        
        int i = 0;
        double errorSum = 0.0;

        Variable[] inputVariables = this.inputVariables;
        for (Observation<I[], O> o : obs) {
            I[] input = o.input;
            // Update the variable values
            for (int j = 0; j < input.length; j++) {
                inputVariables[j].setValue(input[j]);
            }

            // Run the program
            Object result = individual.evaluate();

            if (o.output instanceof Double) {
                if (result instanceof Double) {
                    double d = (Double) result;

                    if (!Double.isNaN(d)) {
                        double error = (d - ((Double)o.output));
                        if (error < 0) error = -error; //abs
                        errorSum += error * o.weight;
                    } else {
                        errorSum = nanFitnessScore();
                        break;
                    }
                }
                
            }
            else {
                throw new RuntimeException("Unimplemented error evaluation for non-numeric values");
            }
        }

        onEvaluate(individual, errorSum);

        return new DoubleFitness.Minimize(errorSum);
    }

    /**
     * Returns the value to be used when an individual returns a NaN value. The
     * default value is <code>Double.NaN</code>.
     *
     * @return the value to return when an individual returns a NaN value
     */
    protected Double nanFitnessScore() {
        return Double.NaN;
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


    double minError = Double.MAX_VALUE;
    TypedOrganism best = null;

    protected void onEvaluate(TypedOrganism i, double error) {
        if (minError > error) {
            minError = error;
            best = i;
            onNextBest(i, error);
        }
    }

    protected void onNextBest(TypedOrganism i, double error) {

    }

}
