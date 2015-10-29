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

import nars.util.data.list.FasterList;

import java.util.Collection;
import java.util.List;

/**
 * A <code>Pipeline</code> applies a sequence of population processes
 * Pipelines are themselves population processes.
 */
public class Pipeline<I extends Organism> implements PopulationProcess<I> {

    /**
     * The list of components in this pipeline.
     */
    private final List<PopulationProcess<I>> pipeline;
    //private GPContainer container;

    /**
     * Constructs an empty <code>Pipeline</code>.
     */
    public Pipeline() {
        pipeline = new FasterList();
    }

    /**
     * Processes all the components of this pipeline in sequence. The provided
     * population is supplied to the first component of the sequence, with each
     * successive component given the population returned by the previous
     * component.
     *
     * @param population the <code>Population</code> to be passed to the first
     * component in the pipeline
     * @return a <code>Population</code> returned by the last component in the
     * pipeline. If this pipeline is empty then the returned
     * <code>Population</code> will be the same <code>Population</code> that was
     * provided as a parameter.
     */
    @Override
    public Population<I> process(Population<I> population) {
        //this.container = population.getConfig();
        for (PopulationProcess component : pipeline) {            
            population = component.process(population);
            if (population == null)
                throw new RuntimeException("population null");
        }

        return population;
    }

    /**
     * Appends the specified <code>Component</code> to the end of this pipeline.
     *
     * @param component the <code>Component</code> to add to this pipeline
     */
    public void add(PopulationProcess component) {
        pipeline.add(component);
    }

    /**
     * Appends all the <code>Component</code>s in the specified collection to
     * the end of this pipeline.
     *
     * @param components a list of components to be added to this pipeline
     */
    public void addAll(Collection<PopulationProcess<I>> components) {
        pipeline.addAll(components);
    }

    /**
     * Inserts the provided <code>Component</code> into this pipeline at the
     * specified index. Any components currently residing at that position, and
     * any subsequent components, are shifted backwards one index position.
     *
     * @param index the index at which the given <code>Component</code> is to be
     * inserted. The components are indexed from <code>0</code>.
     * @param component the <code>Component</code> to add to this pipeline
     * @throws IndexOutOfBoundsException if the index is out of range
	 *         <code>(index < 0 || index > size())</code>
     */
    public void add(int index, PopulationProcess component) {
        pipeline.add(index, component);
    }

    /**
     * Removes the first occurrence of the specified <code>Component</code> from
     * this pipeline if it is present. If this pipeline does not contain the
     * component then it is unchanged.
     *
     * @param component the <code>Component</code> to be removed from this
     * pipeline, if present
     * @return <code>true</code> if the component was in this pipeline, and
     * <code>false</code> otherwise
     */
    public boolean remove(PopulationProcess component) {
        return pipeline.remove(component);
    }

    /**
     * Removes the <code>Component</code> at the specified index in this
     * pipeline. Any subsequent components are shifted forwards one index
     * position. The component that was removed is returned.
     *
     * @param index the index of the component to be removed
     * @return the component previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
	 *         <code>(index < 0 || index > size())</code>
     */
    public PopulationProcess remove(int index) {
        return pipeline.remove(index);
    }

}
