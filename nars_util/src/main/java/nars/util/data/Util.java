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

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 *
 *
 *
 */
public final class Util {


    private Util() {
    }

    final static int PRIME3 = 524287;
    final static int PRIME2 = 92821;
    final static int PRIME1 = 31;

    public static String UUIDbase64() {
        long low = UUID.randomUUID().getLeastSignificantBits();
        long high = UUID.randomUUID().getMostSignificantBits();
        return new String(Base64.getEncoder().encode(
                Bytes.concat(
                        Longs.toByteArray(low),
                        Longs.toByteArray(high)
                )
        ));
    }

    public final static int hash(final int a, final int b) {
        return PRIME2 * (PRIME1 + a) + b;
    }

    public final static int hash(int a, int b, int c) {
        return PRIME3 * (31 * (PRIME2 + a) + b) + c;
    }

    public final static int hash(Object a, Object b) {
        return hash(a.hashCode(), b.hashCode());
    }

    public final static int hash(Object a, Object b, Object c) {
        return hash(a.hashCode(), b.hashCode(), c.hashCode());
    }

    public final static int hash(Object a, Object b, Object c, Object d) {
        return 31 * (PRIME3 * (31 * (PRIME2 + a.hashCode()) + b.hashCode()) + c.hashCode()) + d.hashCode();
    }

    public final static int hash(int a, int b, Object c, Object d) {
        return 31 * (PRIME3 * (31 * (PRIME2 + a) + d.hashCode()) + a) + c.hashCode();
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
        if (line.startsWith("*")) {
            line = line.substring(1);
            strLen--;
        }
        if (line.endsWith("*")) {
            line = line.substring(0, strLen - 1);
            strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : line.toCharArray()) {
            switch (currentChar) {
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
                    if (escaping) {
                        sb.append("\\\\");
                        escaping = false;
                    } else
                        escaping = true;
                    break;
                case '{':
                    if (escaping) {
                        sb.append("\\{");
                    } else {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping) {
                        sb.append(')');
                        inCurlies--;
                    } else if (escaping)
                        sb.append("\\}");
                    else
                        sb.append('}');
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping) {
                        sb.append('|');
                    } else if (escaping)
                        sb.append("\\,");
                    else
                        sb.append(',');
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return sb.toString();
    }



/*
 **************************************************************************
 *                                                                        *
 *          General Purpose Hash Function Algorithms Library              *
 *                                                                        *
 * Author: Arash Partow - 2002                                            *
 * URL: http://www.partow.net                                             *
 * URL: http://www.partow.net/programming/hashfunctions/index.html        *
 *                                                                        *
 * Copyright notice:                                                      *
 * Free use of the General Purpose Hash Function Algorithms Library is    *
 * permitted under the guidelines and in accordance with the most current *
 * version of the Common Public License.                                  *
 * http://www.opensource.org/licenses/cpl1.0.php                          *
 *                                                                        *
 **************************************************************************
*/


    /*class GeneralHashFunctionLibrary
    {*/


    public static long RSHash(String str) {
        int b = 378551;
        int a = 63689;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = hash * a + str.charAt(i);
            a = a * b;
        }

        return hash;
    }
   /* End Of RS Hash Function */


    public static long JSHash(String str) {
        long hash = 1315423911;

        for (int i = 0; i < str.length(); i++) {
            hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
        }

        return hash;
    }
   /* End Of JS Hash Function */


    public static long PJWHash(String str) {
        long BitsInUnsignedInt = (long) (4 * 8);
        long ThreeQuarters = (long) ((BitsInUnsignedInt * 3) / 4);
        long OneEighth = (long) (BitsInUnsignedInt / 8);
        long HighBits = (long) (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
        long hash = 0;
        long test = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << OneEighth) + str.charAt(i);

            if ((test = hash & HighBits) != 0) {
                hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
            }
        }

        return hash;
    }
   /* End Of  P. J. Weinberger Hash Function */


    public static long ELFHash(String str) {
        long hash = 0;
        long x = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 4) + str.charAt(i);

            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return hash;
    }

    public static long ELFHash(final byte[] str) {
        return ELFHash(str, 0);
    }



    public static long ELFHash(final byte[] str, final long seed)
    {
        long hash = seed;
        long x    = 0;

        final int len = str.length;

        for(int i = 0; i < len; i++)
        {
            hash = (hash << 4) + str[i];

            if((x = hash & 0xF0000000L) != 0)
            {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return hash;
    }



        public static long BKDRHash(String str)
        {
            long seed = 131; // 31 131 1313 13131 131313 etc..
            long hash = 0;

            for(int i = 0; i < str.length(); i++)
            {
                hash = (hash * seed) + str.charAt(i);
            }

            return hash;
        }
   /* End Of BKDR Hash Function */


        public static long SDBMHash(String str)
        {
            long hash = 0;

            for(int i = 0; i < str.length(); i++)
            {
                hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
            }

            return hash;
        }
   /* End Of SDBM Hash Function */


        public static long DJBHash(String str)
        {
            long hash = 5381;

            for(int i = 0; i < str.length(); i++)
            {
                hash = ((hash << 5) + hash) + str.charAt(i);
            }

            return hash;
        }
   /* End Of DJB Hash Function */


        public static long DEKHash(String str)
        {
            long hash = str.length();

            for(int i = 0; i < str.length(); i++)
            {
                hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
            }

            return hash;
        }
   /* End Of DEK Hash Function */


        public static long BPHash(String str)
        {
            long hash = 0;

            for(int i = 0; i < str.length(); i++)
            {
                hash = hash << 7 ^ str.charAt(i);
            }

            return hash;
        }
   /* End Of BP Hash Function */


        public static long FNVHash(String str)
        {
            long fnv_prime = 0x811C9DC5;
            long hash = 0;

            for(int i = 0; i < str.length(); i++)
            {
                hash *= fnv_prime;
                hash ^= str.charAt(i);
            }

            return hash;
        }
   /* End Of FNV Hash Function */


        public static long APHash(String str)
        {
            long hash = 0xAAAAAAAA;

            for(int i = 0; i < str.length(); i++)
            {
                if ((i & 1) == 0)
                {
                    hash ^= ((hash << 7) ^ str.charAt(i) * (hash >> 3));
                }
                else
                {
                    hash ^= (~((hash << 11) + str.charAt(i) ^ (hash >> 5)));
                }
            }

            return hash;
        }
   /* End Of AP Hash Function */

//    }
}
