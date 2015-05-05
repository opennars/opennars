/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.data;

import java.util.Collection;
import java.util.Map;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public final class Util {

    private Util() {
    }

    public static void assertNotNull(final Object test, final String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
    }

    public static void assertNotEmpty(final Object[] test, final String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.length == 0) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static void assertNotEmpty(final CharSequence test, final String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.length() == 0) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static void assertNotBlank(CharSequence test, final String varName) {
        if (test != null) {
            test = test.toString().trim();
        }
        assertNotEmpty(test, varName);
    }

    public static <E> void assertNotEmpty(final Collection<E> test, final String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.isEmpty()) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static <K, V> void assertNotEmpty(final Map<K, V> test, final String varName) {
        if (test == null) {
            throw new NullPointerException(varName);
        }
        if (test.isEmpty()) {
            throw new IllegalArgumentException("empty " + varName);
        }
    }

    public static boolean equalsNullAware(final Object obj1, final Object obj2) {
        if (obj1 == null) {
            return obj2 == null;

        } else if (obj2 == null) {
            return false;
        }

        return obj1.equals(obj2);
    }

}
