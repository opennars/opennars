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
 */
public final class Util {

    private Util() {
    }

    public final static int hash(Object a, Object b) {
        return 31 * (31 + a.hashCode() ) + b.hashCode() ;
    }
    public final static int hash(Object a, Object b, Object c) {
        return 31 * (31 * (31 + a.hashCode() ) + b.hashCode() ) + c.hashCode() ;
    }
    public final static int hash(Object a, Object b, Object c, Object d) {
        return 31 * (31 * (31 * (31 + a.hashCode() ) + b.hashCode() ) + c.hashCode() ) + d.hashCode() ;
    }
    public final static int hash(int a, int b, Object c, Object d) {
        return 31 * (31 * (31 * (31 + a ) + b ) + c.hashCode() ) + d.hashCode() ;
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

    public static String globToRegEx(String line) {

        line = line.trim();
        int strLen = line.length();
        StringBuilder sb = new StringBuilder(strLen);
        // Remove beginning and ending * globs because they're useless
        if (line.startsWith("*"))
        {
            line = line.substring(1);
            strLen--;
        }
        if (line.endsWith("*"))
        {
            line = line.substring(0, strLen-1);
            strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : line.toCharArray())
        {
            switch (currentChar)
            {
                case '*':
                    if (escaping)
                        sb.append("\\*");
                    else
                        sb.append(".*");
                    escaping = false;
                    break;
                case '?':
                    if (escaping)
                        sb.append("\\?");
                    else
                        sb.append('.');
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping)
                    {
                        sb.append("\\\\");
                        escaping = false;
                    }
                    else
                        escaping = true;
                    break;
                case '{':
                    if (escaping)
                    {
                        sb.append("\\{");
                    }
                    else
                    {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping)
                    {
                        sb.append(')');
                        inCurlies--;
                    }
                    else if (escaping)
                        sb.append("\\}");
                    else
                        sb.append("}");
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping)
                    {
                        sb.append('|');
                    }
                    else if (escaping)
                        sb.append("\\,");
                    else
                        sb.append(",");
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return sb.toString();
    }

}
