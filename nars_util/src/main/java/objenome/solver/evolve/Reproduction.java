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
 * This class implements the reproduction (copy) operator.
 */
public class Reproduction extends AbstractOrganismOperator {

    /**
     * The property key under which the reproduction probability is stored.
     */
    public static final GPKey<Double> PROBABILITY = new GPKey<>();

    /**
     * The default reproduction probability.
     */
    public static final double DEFAULT_PROBABILITY = 0.90;

    @Override
    public int inputSize() {
        return 1;
    }

    @Override
    public Organism[] perform(Organism... individuals) {
        return individuals;
    }

    @Override
    public double probability() {
        return getConfig().get(PROBABILITY, DEFAULT_PROBABILITY);
    }

}
