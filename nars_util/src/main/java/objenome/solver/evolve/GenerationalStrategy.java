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

import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;
import objenome.solver.evolve.event.GenerationEvent.StartGeneration;
import objenome.solver.evolve.event.Listener;

import java.util.List;

/**
 * A <code>GenerationalStrategy</code> is an evolutionary strategy with clearly
 * defined generations. It consists of a main loop that is executed until a
 * termination criteria is met. At each iteration of the loop, a new population
 * is created using the pipeline's components.
 *
 * The main loop can be illustrated as:
 *
 * <pre>
 *  while (!terminate) {
 *  	generate (and optionally evaluate) new population
 *  }
 * </pre>
 *
 * The {@link PopulationTermination} is obtained from the {@link GPContainer}, using
 * the appropriate <code>ConfigKey</code>. A new population is generated using
 * the pipeline's components, which typically will include a {@link Breeder} and
 * {@link FitnessEvaluator} instances.
 *
 * @see Breeder
 * @see FitnessEvaluator
 * @see PopulationTermination
 */
public class GenerationalStrategy extends Pipeline implements EvolutionaryStrategy, Listener<ConfigEvent> {

    /**
     * The list of termination criteria.
     */
    private List<PopulationTermination> criteria;
    private GPContainer config;

    /**
     * Constructs a <code>GenerationalStrategy</code> with the provided
     * components. One of those components would typically be a {@link Breeder}.
     *
     * @param components
     */
    public GenerationalStrategy(PopulationProcess... components) {
        for (PopulationProcess component : components) {
            add(component);
        }

    }

    /**
     * Evolves the population until the termination criteria is met. A
     * {@link StartGeneration} event is fired at the start of the generation and
     * an {@link EndGeneration} event at the end of the generation.
     *
     * @param population the population to be evolved
     *
     * @return the evolved population.
     */
    @Override
    public Population process(Population population) {
        config = population.getConfig();

        setup();

        config.on(ConfigEvent.class, this);

        int generation = 1;
        do {
            config.fire(new StartGeneration(generation, population));

            population = super.process(population);

            config.fire(new EndGeneration(generation, population));
            generation++;
        } while (!terminate());

        return population;
    }

    /**
     * Returns <code>true</code> if any of the termination criteria is met.
     *
     * @return <code>true</code> if any of the termination criteria is met;
     * <code>false</code> otherwise.
     */
    protected boolean terminate() {
        if (criteria!=null)
            for (PopulationTermination tc : criteria) {
                if (tc.terminate(config)) {
                    return true;
                }
            }
        return true;
    }

    /**
     * Looks up the {@link PopulationTermination} and the {@link FitnessEvaluator}
     * in the {@link GPContainer}.
     */
    protected void setup() {
        criteria = config.get(EvolutionaryStrategy.TERMINATION_CRITERIA);
        if (criteria!=null)
            for (PopulationTermination x : criteria) {
                GPContainer.setContainerAware(config, x);
            }
    }

    /**
     * Receives {@link ConfigEvent} event notifications.
     *
     * @param event the fired event.
     */
    @Override
    public void onEvent(ConfigEvent event) {
        if (event.isKindOf(/*ProblemSTGP.PROBLEM, */EvolutionaryStrategy.TERMINATION_CRITERIA)) {
            setup();
        }
    }

}
