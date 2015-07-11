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
package objenome.solver.evolve;

/**
 * An <code>AbstractIndividual</code> is a candidate solution with a settable
 * fitness value.
 *
 * @since 2.0
 */
public abstract class AbstractOrganism implements Organism {


    protected Fitness fitness;

    /**
     * Sets this individual's fitness value
     *
     * @param fitness the fitness to set
     */
    public void setFitness(Fitness fitness) {
        this.fitness = fitness;
    }

    /**
     * Returns the fitness value assigned to this individual
     *
     * @return a fitness value for this individual
     */
    @Override
    public Fitness getFitness() {
        return fitness;
    }

    /**
     * Returns a clone of this individual with a copy of its fitness assigned
     *
     * @return an individual which is a copy of this individual
     */
    @Override
    public AbstractOrganism clone() {
        try {
            AbstractOrganism clone = (AbstractOrganism) super.clone();
            if (fitness != null) {
                clone.fitness = fitness.clone();
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
