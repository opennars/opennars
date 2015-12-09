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

/**
 * An implementation of the <code>Fitness</code> interface provides a measure of
 * individual quality. Implementations may represent the fitness score in any
 * form, explicit or otherwise. The only requirement is that a natural ordering
 * exists, defined by the implementation's <code>compareTo</code> method.
 */
public interface Fitness extends Cloneable, Comparable<Fitness> {

    /**
     * Creates an returns a copy of this fitness object.
     *
     * @return a clone of this fitness object.
     */
    Fitness clone();

}
