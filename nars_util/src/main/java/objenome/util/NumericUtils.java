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
package objenome.util;

/**
 * This class provides static utility methods for working with different number
 * types.
 */
public enum NumericUtils {
    ;

    /**
     * Converts the given object to a <code>Double</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return a <code>Double</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Double asDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }

        return null;
    }

    /**
     * Converts the given object to a <code>Float</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return a <code>Float</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Float asFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }

        return null;
    }

    /**
     * Converts the given object to an <code>Integer</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return an <code>Integer</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Integer asInteger(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }

        return null;
    }

    /**
     * Converts the given object to a <code>Long</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return a <code>Long</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Long asLong(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }

        return null;
    }

    /**
     * Converts the given object to a <code>Short</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return a <code>Short</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Short asShort(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        }

        return null;
    }

    /**
     * Converts the given object to a <code>Byte</code>
     *
     * @param o an object of some <code>Number</code> type
     * @return a <code>Byte</code> that is equivalent to the given object or
     * <code>null</code> if the object is not an instance of <code>Number</code>
     */
    public static Byte asByte(Object o) {
        if (o instanceof Number) {
            return ((Number) o).byteValue();
        }

        return null;
    }
}
