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

import objenome.solver.evolve.GPContainer.GPKey;

/**
 * <code>Initialiser</code> components are responsible for creating individuals,
 * delegating the creation to an {@link OrganismBuilder} instance.
 */
public class Initialiser<I extends Organism> extends ProxyComponent<OrganismBuilder<I>> implements PopulationProcess<I> {

    /**
     * The key for setting and retrieving the <code>InitialisationMethod</code>
     * used by this component.
     */
    public static final GPKey<OrganismBuilder> METHOD = new GPKey<>();

    /**
     * Constructs a <code>Initialiser</code>.
     */
    public Initialiser() {        
        super(METHOD);

    }

    /**
     * Delegates the initialisation of the population to the
     * <code>InitialisationMethod</code> object.
     */
    @Override
    public Population<I> process(Population<I> population) {
        if (getConfig()==null)
            setConfig(population.getConfig());
        
        OrganismBuilder handler = getHandler();
        if (handler == null) {
            throw new IllegalStateException("The initialisation method has not been set.");
        }

        return handler.createPopulation(population, population.getConfig());
    }

}
