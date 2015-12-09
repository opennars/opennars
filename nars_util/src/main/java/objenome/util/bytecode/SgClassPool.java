/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package objenome.util.bytecode;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores references of all known model classes.
 */
public final class SgClassPool {

    private final Map<String, SgClass> cache = new HashMap<>();

    /**
     * Default constructor.
     */
    public SgClassPool() {
        cache.put(void.class.getName(), SgClass.VOID);
        cache.put(boolean.class.getName(), SgClass.BOOLEAN);
        cache.put(byte.class.getName(), SgClass.BYTE);
        cache.put(char.class.getName(), SgClass.CHAR);
        cache.put(short.class.getName(), SgClass.SHORT);
        cache.put(int.class.getName(), SgClass.INT);
        cache.put(long.class.getName(), SgClass.LONG);
        cache.put(float.class.getName(), SgClass.FLOAT);
        cache.put(double.class.getName(), SgClass.DOUBLE);
        cache.put(Object.class.getName(), SgClass.OBJECT);
    }

    /**
     * Returns a class from the internal cache.
     * 
     * @param className
     *            Class to find - Cannot be null.
     * 
     * @return Class or null if it's not found.
     */
    public SgClass get(String className) {
        if (className == null) {
            throw new IllegalArgumentException("The argument 'className' cannot be null!");
        }
        return cache.get(className);
    }

    /**
     * Adds a class to the internal cache.
     * 
     * @param clasz
     *            Class to add - Cannot be null.
     */
    public void put(SgClass clasz) {
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }
        cache.put(clasz.getName(), clasz);
    }

    /**
     * Adds a class to the internal cache by it's name. Helper method to add
     * array classes that have a strange name. The name cannot be reconstructed
     * by the model class. TODO Handle array classes better
     * 
     * @param className
     *            Name of the class - Cannot be null.
     * @param clasz
     *            Class to add - Cannot be null.
     */
    void put(String className, SgClass clasz) {
        if (className == null) {
            throw new IllegalArgumentException("The argument 'className' cannot be null!");
        }
        if (clasz == null) {
            throw new IllegalArgumentException("The argument 'clasz' cannot be null!");
        }
        cache.put(className, clasz);
    }

}
