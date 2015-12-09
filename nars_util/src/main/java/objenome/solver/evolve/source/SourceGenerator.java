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
package objenome.solver.evolve.source;

import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.Organism;

/**
 * Source generators take a candidate program and produce source code from it.
 */
public interface SourceGenerator<T extends Organism> {

    /**
     * The key for setting the source generator
     */
    GPKey<SourceGenerator<?>> SOURCE_GENERATOR = new GPKey<>();

    String getSource(T individual);

}
