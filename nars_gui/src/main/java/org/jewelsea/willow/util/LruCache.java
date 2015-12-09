/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<A, B> extends LinkedHashMap<A, B> {
    private final int maxEntries;

    public LruCache(int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
        return super.size() > maxEntries;
    }
} 
