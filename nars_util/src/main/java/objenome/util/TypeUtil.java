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
import org.apache.commons.lang3.ClassUtils;

/**
 * This class provides static utility methods for working with Epox data-types.
 */
public   enum TypeUtil {
    ;

    /**
     * Returns the super-type class of the given classes, or <code>null</code>
     * if none of them are a super class of all others
     *
     * @param classes the classes to check for a super-type
     * @return the super-type if there is one, or <code>null</code> otherwise
     */
    public static Class<?> getSuper(Class<?>... classes) {
        return getSuper(false, classes);
    }

    /**
     * Returns the super-type class of the given classes, or <code>null</code>
     * if none of them are a super class of all others
     *
     * @param autobox whether autoboxing should be allowed in the consideration
     * of a super-type
     * @param classes the classes to check for a super-type
     * @return the super-type if there is one, or <code>null</code> otherwise
     */
    public static Class<?> getSuper(boolean autobox, Class<?>... classes) {
        outer:
        for (Class<?> cls1 : classes) {
            for (Class<?> cls2 : classes) {
                if (!ClassUtils.isAssignable(cls2, cls1, autobox)) {
                    continue outer;
                }
            }
            return cls1;
        }

        return null;
    }

    /**
     * Returns the sub-type class of the given classes, or <code>null</code> if
     * none of them are a sub class of all others
     *
     * @param classes the classes to check for a sub-type
     * @return the sub-type if there is one, or <code>null</code> otherwise
     */
    public static Class<?> getSub(Class<?>... classes) {
        return getSuper(false, classes);
    }

    /**
     * Returns the sub-type class of the given classes, or <code>null</code> if
     * none of them are a sub class of all others
     *
     * @param autobox whether autoboxing should be allowed in the consideration
     * of a sub-type
     * @param classes the classes to check for a sub-type
     * @return the sub-type if there is one, or <code>null</code> otherwise
     */
    public static Class<?> getSub(boolean autobox, Class<?>... classes) {
        outer:
        for (Class<?> cls1 : classes) {
            for (Class<?> cls2 : classes) {
                if (!ClassUtils.isAssignable(cls1, cls2, autobox)) {
                    continue outer;
                }
            }
            return cls1;
        }

        return null;
    }

    /**
     * Returns <code>true</code> if the given collection contains an element
     * which represents a class which is the same as or is a sub-type of the
     * given class
     *
     * @param collection the collection to search for a class that is assignable
     * to <code>cls</code>
     * @param cls the class that is being searched against the given collection
     * @return <code>true</code> if the given collection contains a class which
     * is the same as or a sub-type of the given class parameter. It returns
     * <code>false</code> otherwise.
     */
    public static boolean containsSub(Class<?>[] collection, Class<?> cls) {
        for (Class<?> c : collection) {
            if (ClassUtils.isAssignable(c, cls)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the given collection contains an element
     * which represents a class which is the same as or is a super-type of the
     * given class
     *
     * @param collection the classes to search for one that <code>cls</code> is
     * assignable to
     * @param cls the class that is being searched against
     * <code>collection</code>
     * @return <code>true</code> if the given collection contains a class which
     * is the same as or a super-type of the given class parameter. It returns
     * <code>false</code> otherwise.
     */
    public static boolean containsSuper(Class<?>[] collection, Class<?> cls) {
        for (Class<?> c : collection) {
            if (ClassUtils.isAssignable(cls, c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if all elements of <code>collection</code> are
     * sub-types of <code>cls</code>, or the same type
     *
     * @param collection the classes that must all be sub-types of
     * <code>cls</code>
     * @param cls the type to compare against <code>collection</code>
     * @return <code>true</code> if all classes in <code>collection</code> are
     * sub-types of, or the same type as, <code>cls</code> and returns
     * <code>false</code> otherwise
     */
    public static boolean allSub(Class<?>[] collection, Class<?> cls) {
        for (Class<?> c : collection) {
            if (!ClassUtils.isAssignable(c, cls)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if all elements of <code>collection</code> are
     * super-types of <code>cls</code>, or the same type
     *
     * @param collection the classes that must all be super-types of
     * <code>cls</code>
     * @param cls the type to compare against <code>collection</code>
     * @return <code>true</code> if all classes in <code>collection</code> are
     * super-types of, or the same type as, <code>cls</code> and returns
     * <code>false</code> otherwise
     */
    public static boolean allSuper(Class<?>[] collection, Class<?> cls) {
        for (Class<?> c : collection) {
            if (!ClassUtils.isAssignable(cls, c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if all classes in the <code>collection</code>
     * array are equal to <code>cls</code>
     *
     * @param collection the classes to compare against <code>cls</code> for
     * equality
     * @param cls the type to compare against <code>collection</code>
     * @return <code>true</code> if all classes in <code>collection</code> are
     * the same type as <code>cls</code> and <code>false</code> otherwise
     */
    public static boolean allEqual(Class<?>[] collection, Class<?> cls) {
        for (Class<?> c : collection) {
            if (c != cls) {
                return false;
            }
        }

        return true;
    }

    
    /**
     * Returns the widest compatible numeric type for two primitive numeric
     * class types. Any of <code>Byte</code>, <code>Short</code>,
     * <code>Integer</code> will resolve to <code>Integer</code>.
     *
     * @param classes an array of numeric class types, for example,
     * <code>int, long, float, or double</code>.
     * @return the compatible numeric type for binary operations involving both
     * types, or <code>null</code> if there is no compatible numeric type
     */
    public static Class<?> widestNumberType(Class<?>... classes) {
        
        //check common cases first to avoid the iterative ones at the end
        if (classes.length == 1) {
            return classes[0];
        }
        if (classes.length == 2) {
            Class c = classes[0];
            if (c == classes[1]) {
                if (c == Double.class) return Double.class;
                if (c == Float.class) return Float.class;
                if (c == Long.class) return Long.class;
                if (c == Integer.class) return Integer.class;
            }
        }

        if (!isAllNumericType(classes)) {
            return null;
        }

        if (ArrayUtils.contains(classes, Double.class)) {
            return Double.class;
        } else if (ArrayUtils.contains(classes, Float.class)) {
            return Float.class;
        } else if (ArrayUtils.contains(classes, Long.class)) {
            return Long.class;
        } else {
            return Integer.class;
        }
    }

    /**
     * Tests whether the given class type is a numeric type (one of
     * <code>Byte, Short, Integer, Long, Float, Double</code>)
     *
     * @param type the type to check
     * @return <code>true</code> if it is a numeric type, <code>false</code>
     * otherwise
     */
    public static boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type);
        /*
        return ((type == Byte.class) || (type == Short.class) || (type == Integer.class) || (type == Long.class)
                || (type == Double.class) || (type == Float.class));
        */
    }

    /**
     * Tests whether all given classes are for numeric types (any of <code>Byte,
     * Short, Integer, Long, Float, Double</code>)
     *
     * @param classes the types to check for numeric types
     * @return <code>true</code> if all given classes are for numeric types,
     * <code>false</code> otherwise
     */
    public static boolean isAllNumericType(Class<?>... classes) {
        for (Class<?> c : classes) {
            if (!TypeUtil.isNumericType(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the primitive class of any numeric object class (any of <code>Byte,
     * Short, Integer, Long, Float, Double</code>). If a primitive type is
     * provided then the same type is returned.
     *
     * @param type a numeric type, which is either a primitive or an object
     * @return the equivalent primitive class type
     */
    public static Class<?> getPrimitiveType(Class<?> type) {

        //noinspection IfStatementWithTooManyBranches
        if (type.isPrimitive()) {
            return type;
        } else if (Integer.class.equals(type)) {
            return int.class;
        } else if (Long.class.equals(type)) {
            return long.class;
        } else if (Float.class.equals(type)) {
            return float.class;
        } else if (Double.class.equals(type)) {
            return double.class;
        } else if (Byte.class.equals(type)) {
            return byte.class;
        } else if (Short.class.equals(type)) {
            return short.class;
        } else {
            throw new IllegalArgumentException("Input class must be a numeric type");
        }
    }

    /**
     * Returns the object wrapper class type for a primitive class type
     *
     * @param type a numeric type, which is either a primitive or an object
     * @return the equivalent object class type
     */
    public static Class<?> getObjectType(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        } else if (int.class == type) {
            return Integer.class;
        } else if (long.class == type) {
            return Long.class;
        } else if (float.class == type) {
            return Float.class;
        } else if (double.class == type) {
            return Double.class;
        } else if (boolean.class == type) {
            return Boolean.class;
        } else if (short.class == type) {
            return Short.class;
        } else if (char.class == type) {
            return Character.class;
        } else if (byte.class == type) {
            return Byte.class;
        } else {
            throw new IllegalArgumentException();
        }
    }

}
