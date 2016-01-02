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

import objenome.solver.evolve.*;
import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;

/**
 * A fitness function for that calculates and assigns adjusted fitness scores.
 * The fitness scores are calculated by passing evaluation to a delegate fitness
 * function, which must return an instance of
 * <code>DoubleFitness.Minimise</code>. This value is then converted to
 * standardised fitness by removing any offset of the values (so the minimum
 * possible score is 0.0) and then converted to adjusted fitness using the
 * following formula:
 *
 * <code><blockquote>
 * adjusted-fitness = 1 / (1 + standardised-fitness)
 * </blockquote></code>
 *
 * When using this fitness function the {@link #MINIMUM_FITNESS_SCORE} config
 * option can optionally be set, or the same value set using the mutator method
 * provided. If set, the MINIMUM_FITNESS_SCORE is used to convert the delegate's
 * calculated values to standardised fitness. For example, if the minimum
 * fitness score is 10.0, and the delegate assigns a fitness value of 11.0, then
 * the standardised fitness will be 1.0. In the same example, the adjusted
 * fitness would then be calculated as 0.5 using the formula above.
 *
 * If the MINIMUM_FITNESS_SCORE config option is not set, then it is assumed the
 * delegates calculation is already standardised.
 *
 * @since 2.0
 */
public class AdjustedFitnessFunction extends AbstractFitnessFunction implements Listener<ConfigEvent>, GPContainerAware {

    /**
     * The key for setting the minimum fitness score possible, used when
     * calculating the adjusted fitness score.
     */
    public static final GPKey<Double> MINIMUM_FITNESS_SCORE = new GPKey<>();

    // The delegate that fitness calculations will be passed to
    private final AbstractFitnessFunction delegate;

    // Configuration settings
    private double minFitnessScore;
    private final boolean autoConfig;

    /**
     * Constructs a <code>AdjustedFitnessFunction</code> fitness function with
     * control parameters automatically loaded from the config.
     */
    public AdjustedFitnessFunction(AbstractFitnessFunction delegate) {
        this(delegate, true);
    }

    /**
     * Constructs a <code>AdjustedFitnessFunction</code> fitness function with
     * control parameters initially loaded from the config. If the
     * <code>autoConfig</code> argument is set to <code>true</code> then the
     * configuration will be automatically updated when the config is modified.
     *
     * @param autoConfig whether this operator should automatically update its
     * configuration settings from the config
     */
    public AdjustedFitnessFunction(AbstractFitnessFunction delegate, boolean autoConfig) {

        this.delegate = delegate;
        this.autoConfig = autoConfig;

    }

    /**
     * Sets up this operator with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li>{@link #MINIMUM_FITNESS_SCORE}
     * </ul>
     */
    @Override
    public void setConfig(GPContainer config) {
        if (autoConfig) {
            config.on(ConfigEvent.class, this);
        }

        minFitnessScore = config.get(MINIMUM_FITNESS_SCORE, minFitnessScore);
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
        if (event.isKindOf(MINIMUM_FITNESS_SCORE)) {
            throw new UnsupportedOperationException("Unsupported yet"); //setup()
        }
    }

    /**
     * Calculates the adjusted fitness of the given individual. The adjusted
     * fitness is a double value between 0.0 and 1.0, where 1.0 is a correct
     * solution. The evaluation of individuals is delegated to the
     * <code>AbstractFitnessFunction</code> supplied to the constructor as the
     * delegate. The values returned by the delegate are converted to adjusted
     * fitness before being returned. The fitness returned will be an instance
     * of DoubleFitness.Maximise.
     *
     * @param individual the program to evaluate
     * @return a DoubleFitness.Maximise which is the adjusted fitness of the
     * given individual
     * @throws IllegalStateException if the delegate returns anything other than
     * an instance of DoubleFitness.Minimise
     */
    @Override
    public DoubleFitness.Maximise evaluate(Population population, Organism individual) {
        setConfig(population.getConfig());

        Fitness fitness = delegate.evaluate(population, individual);

        if (!(fitness instanceof DoubleFitness.Minimize)) {
            throw new IllegalStateException("Delegate must return instances of DoubleFitness.Minimise");
        }

        DoubleFitness.Minimize minimised = (DoubleFitness.Minimize) fitness;

        // Convert to standardised fitness (minimised with 0.0 as lowest value)
        double standardised = minimised.getValue() - minFitnessScore;

        return new DoubleFitness.Maximise(adjustedFitness(standardised));
    }

    /**
     * Converts the given standardised fitness score to an adjusted fitness
     * score, between 0.0 and 1.0. This is calculated as
     * <code>1.0 / (1.0 + standardised)</code> as defined by Koza in Genetic
     * Programming.
     *
     * @param standardised the standardised fitness score - this must be 0.0 or
     * greater
     * @return a double which is the adjusted fitness score between 0.0 and 1.0,
     * where 1.0 is more fit than 0.0
     */
    protected static double adjustedFitness(double standardised) {
        if (standardised < 0.0) {
            throw new IllegalArgumentException("Standardised fitness must be 0.0 or greater");
        }

        return 1.0 / (1.0 + standardised);
    }

    /**
     * Returns the minimum fitness score that an individual can obtain using the
     * delegate. This is used to convert the delegate's fitness scores to
     * standardised fitness by removing the offset.
     *
     * @return the minimum fitness score that the delegate can assign
     */
    public double getMinimumFitnessScore() {
        return minFitnessScore;
    }

    /**
     * Sets the minimum fitness score than an individual can be assigned by the
     * delegate. this is used to convert the delegate's fitness score to
     * standardised fitness by removing the amount the scores are offset from
     * 0.0.
     *
     * If automatic configuration is enabled then any value set here will be
     * overwritten by the {@link #MINIMUM_FITNESS_SCORE} configuration setting
     * on the next config event.
     *
     * @param minFitnessScore the minimum fitness score that the delegate can
     * assign
     */
    public void setMinimumFitnessScore(double minFitnessScore) {
        this.minFitnessScore = minFitnessScore;
    }
}
