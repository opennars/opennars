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

import org.apache.commons.lang3.ArrayUtils;

/**
 * This class provides static utility methods for working with
 * <code>Strings</code>.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Tests whether the given string contains any of the <code>char</code>s in
     * the provided array.
     *
     * @param str an input string to test for specific <code>char</code>s
     * @param chrs an array of characters to look for in <code>str</code>
     * @return <code>true</code> if <code>str</code> contains one or more
     * characters from the <code>chrs</code> array, and <code>false</code> if it
     * contains none
     */
    public static boolean containsAny(String str, char[] chrs) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (ArrayUtils.contains(chrs, c)) {
                return true;
            }
        }
        return false;
    }

}
