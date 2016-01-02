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

import objenome.solver.evolve.event.Event;
import objenome.solver.evolve.event.EventManager;
import objenome.solver.evolve.event.Listener;
import objenome.solver.evolve.event.OperatorEvent;
import objenome.solver.evolve.event.OperatorEvent.EndOperator;
import objenome.solver.evolve.event.OperatorEvent.StartOperator;

/**
 * A skeletal implementation of the {@link OrganismOperator} interface than fires events
 * at the start (before) and end (after) the operator is performed. Typically,
 * subclasses will override one of the following methods:
 *
 * <ul>
 *
 * <li>{@link #perform(Organism...)}: when no custom end event is needed;
 *
 * <li>{@link #perform(OperatorEvent.EndOperator, Organism...)}: when a custom
 * end event is used, this method should be overridden to set the additional
 * information.
 *
 * </ul>
 *
 * @see Event
 * @see EventManager
 * @see Listener
 */
public abstract class AbstractOrganismOperator implements OrganismOperator {
    private GPContainer config;

    /**
     * override in subclasses
     */
    @Override
    public void setConfig(GPContainer config) {

    }

    public GPContainer getConfig() {
        return config;
    }

    
    @Override
    public final Organism[] apply(Population population, Organism... individuals) {
        config = population.getConfig();
        setConfig(population.getConfig());

        // fires the start event
        StartOperator start = getStartEvent(individuals);
        population.getConfig().fire(start);

        EndOperator end = getEndEvent(individuals);
        Organism[] newParents = perform(end, individuals);

        for (Organism o : newParents)
            o.normalize();

        // fires the end event only if the operator was successful
        if (individuals != null) {
            end.setChildren(newParents);
            population.getConfig().fire(end);
        }

        return individuals;
    }

    /**
     * Performs the operator on the specified individuals. If the operator is
     * not successful, the specified individuals will not be changed and
     * <code>null</code> is returned. The default implementation calls the
     * {@link #perform(Organism...)} method.
     * <p>
     * When overriding this method, the specified <code>EndOperator</code> event
     * can be used to provide more information about the operator. In order to
     * do so, the {@link #getEndEvent(Organism...)} method must return a
     * custom event instance, enabling this method to set its properties.
     * </p>
     *
     * @param event the end event object to be fired after this operator is
     * performed.
     * @param individuals the individuals undergoing the operator.
     *
     * @return the indivuals produced by this operator.
     *
     * @see #getEndEvent(Organism...)
     */
    public Organism[] perform(EndOperator event, Organism... individuals) {
        return perform(individuals);
    }

    /**
     * Performs the operator on the specified individuals. If the operator is
     * not successful, the specified individuals will not be changed and
     * <code>null</code> is returned. The default implementation just returns
     * the same individuals.
     *
     * @param individuals the individuals undergoing the operator.
     *
     * @return the indivduals produced by this operator; <code>null</code> when
     * the operator could not be applied.
     */
    public Organism[] perform(Organism... individuals) {
        return individuals;
    }

    /**
     * Returns the operator's start event. The default implementation returns a
     * <code>StartOperator</code> instance.
     *
     * @param parents the individuals undergoing the operator.
     *
     * @return the operator's start event.
     */
    protected StartOperator getStartEvent(Organism... parents) {
        return new StartOperator(this, parents);
    }

    /**
     * Returns the operator's end event. The default implementation returns a
     * <code>EndOperator</code> instance. The end event is passed to the
     * {@link #perform(Organism...)} method to allow the operator to add
     * additional information.
     *
     * @param parents the individuals undergoing the operator.
     *
     * @return the operator's end event.
     */
    protected EndOperator getEndEvent(Organism... parents) {
        return new EndOperator(this, parents);
    }

    /**
     * Returns a (deep) clone copy of the specified array of individuals.
     *
     * @param individuals the array of individuals to be cloned.
     *
     * @return a (deep) clone copy of the specified array of individuals.
     */
    private static Organism[] clone(Organism[] individuals) {
        Organism[] clone = new Organism[individuals.length];

        for (int i = 0; i < clone.length; i++) {
            if (individuals[i] != null) {
                clone[i] = individuals[i].clone();
            }
        }

        return clone;
    }

}
