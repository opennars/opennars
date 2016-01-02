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

import objenome.solver.evolve.Organism;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CachedSourceGenerator<T extends Organism> implements SourceGenerator<T> {

    // The cache of fitness scores
    private final Map<Object, String> cache;

    private final SourceGenerator<T> delegate;

    public CachedSourceGenerator(SourceGenerator<T> delegate) {
        this.delegate = delegate;

        cache = new HashMap<>();
    }

    @Override
    public String getSource(T individual) {
        Object key = key(individual);

        //TODO Use source generator if one is set
        String source = cache.get(key);
        if (source == null) {
            source = delegate.getSource(individual);
            cache.put(key, source);
        }

        return source;
    }

    protected Object key(T individual) {
        return individual.hashCode();
    }

}
