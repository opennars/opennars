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
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve;

import java.util.List;
import objenome.solver.evolve.GPContainer.GPKey;
import static objenome.solver.evolve.RandomSequence.RANDOM_SEQUENCE;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;

/**
 * A <code>BranchedBreeder</code> produces a new <code>Population</code> by
 * applying a single genetic operator per individual. The operator that is
 * applied at each step is selected at random. The designated
 * <code>IndividualSelector</code> is used to choose the individuals that
 * undergo the selected operator. The result is that each individual in the new
 * population that is produced is the product of just one genetic operator.
 */
public class BranchedBreeder implements Breeder, Listener<ConfigEvent> {

    /**
     * The key for setting and retrieving the size of the elite.
     */
    public static final GPKey<Integer> ELITISM = new GPKey<>();

    /**
     * The list of operators to be used to generate new individuals.
     */
    private List<Operator> operators;

    /**
     * The selection strategy used to select the individual that will survive a
     * generation.
     */
    private IndividualSelector selector;

    /**
     * The random number generator.
     */
    private RandomSequence random;

    /**
     * The number of individuals to copy to the next generation by elitism.
     */
    private int elitism;

    /**
     * Constructs a <code>BranchedBreeder</code> that configures itself upon
     * construction and firing of appropriate <code>ConfigEvents</code>.
     */
    public BranchedBreeder() {
    }

    /**
     * Applies genetic operators to produce a new population. The available
     * operators are repeatedly selected from at random and applied to produce
     * new individuals that are added to the next population. The configured
     * <code>IndividualSelector</code> is used to select individuals from the
     * existing population to supply as inputs to the operator. The number of
     * individuals required by the operator is determined by its
     * <code>inputSize</code> method. The individuals returned by an operator
     * are inserted into the next population. This process is repeated until the
     * next population has been filled. If the new population has insufficient
     * space for all the new individuals then only those that there is space for
     * will be added and all others will be discarded.
     *
     * @param population the current population of individuals that a new
     * population will be produced from
     * @return a newly constructed population filled with individuals produced
     * by the application of genetic operators
     */
    @Override
    public Population process(Population population) {

        setup(population.getConfig());

        population.getConfig().on(ConfigEvent.class, this);

        selector.init(population);

        Population newPopulation = new Population(population.getConfig());
        int size = population.size();

        double[] probabilities = new double[operators.size()];
        double cumulative = 0.0;
        for (int i = 0; i < operators.size(); i++) {
            cumulative += operators.get(i).probability();
            probabilities[i] = cumulative;
        }

        if (elitism > 0) {
            Individual[] elite = population.elites(elitism);

            for (Individual individual : elite) {
                newPopulation.add(individual);
                size--;
            }
        }

        while (size > 0) {
            double r = random.nextDouble() * cumulative;
            Operator operator = null;
            for (int i = 0; i < probabilities.length; i++) {
                if (r <= probabilities[i]) {
                    operator = operators.get(i);
                    break;
                }
            }

            Individual[] parents = null;

            do {
                parents = new Individual[operator.inputSize()];

                for (int i = 0; i < parents.length; i++) {
                    parents[i] = selector.select();
                }
                
                parents = operator.apply(population, parents);
            } while (parents == null);

            for (int i = 0; (i < parents.length) && (size > 0); i++) {
                newPopulation.add(parents[i]);
                size--;
            }
        }

        return newPopulation;
    }

    /**
     * Sets up this breeder with the appropriate configuration settings. This
     * method is called whenever a <code>ConfigEvent</code> occurs for a change
     * in any of the following configuration parameters:
     * <ul>
     * <li><code>Breeder.OPERATORS</code>
     * <li><code>Breeder.SELECTOR</code>
     * <li><code>RandomSequence.RANDOM_SEQUENCE</code>
     * <li><code>BranchedBreeder.ELITISM</code>
     * </ul>
     */
    protected void setup(GPContainer config) {
        operators = config.get(OPERATORS);
        selector = config.get(SELECTOR);
        random = config.get(RANDOM_SEQUENCE);
        elitism = config.get(ELITISM, 0);
        
        for (Operator o : operators) {
            o.setConfig(config);
        }
        
    }

    /**
     * Receives configuration events and triggers this breeder to configure its
     * parameters if the <code>ConfigEvent</code> is for one of its required
     * parameters.
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void onEvent(ConfigEvent event) {
        if (event.isKindOf(OPERATORS, SELECTOR, RANDOM_SEQUENCE, ELITISM)) {
            //setup();
            throw new UnsupportedOperationException("Implementable soon");
        }
    }

}
