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
package nars.util.meter.data;

import java.io.Serializable;
import java.util.Set;

/**
 * Stores a mapping of name-value pairs, where the values may be arbitrary
 * types.
 *
 * @author The Stajistics Project
 */
public interface DataContainer extends Serializable {

    /**
     * Obtain the number of fields in this DataContainer.
     *
     * @return The current number of fields.
     */
    int size();

    /**
     * Determine if this DataContainer is empty.
     *
     * @return <tt>true</tt> if there are zero elements, <tt>false</tt>
     * otherwise.
     */
    boolean isEmpty();

    /**
     * Get the value of a single field by the given <tt>name</tt>.
     *
     * @param name The name of the field to retrieve.
     * @return The field value, or <tt>null</tt> if not found or if
     * <tt>name</tt> is <tt>null</tt>.
     */
    Object get(String name);

    /**
     * Get the value of a single field by the given <tt>name</tt>. Coerce the
     * resulting value into the given type <tt>type</tt>.
     *
     * @param <T> The expected type of the value.
     * @param name The name of the field to retrieve.
     * @param type The type into which to coerce the value, if found.
     * @return The field value, or <tt>null</tt> if not found.
     * @throws ClassCastException If the existing value cannot be coerced into
     * the given <tt>type</tt>.
     */
    <T> T getField(String name, Class<T> type) throws ClassCastException;

    /**
     * Get the value of a single field by the given <tt>name</tt>.
     *
     * @param <T>
     * @param name The name of the field to retrieve.
     * @param defaultValue The value to return if the field doesn't exist or is
     * not of type <tt>T</tt>.
     * @return The field value, or <tt>defaultValue</tt> if not found or if the
     * value is not of type <tt>T</tt>.
     */
    <T> T getField(String name, T defaultValue);

    /**
     * Obtains a Set of field names that are contained in this DataContainer.
     *
     * @return The Set of field names, never <tt>null</tt>.
     */
    Set<String> keySet();

    /**
     * Store a name-value pair in this DataContainer, replacing a possibly
     * existing field having the same <tt>name</tt>.
     *
     * @param name The name of the field to add.
     * @param value The value of the field to add.
     */
    void put(String name, Object value);

    /**
     * Remove the field defined by <tt>name</tt> from this DataContainer.
     *
     * @param name The name of the field to remove.
     * @return The removed field value if found, <tt>null</tt> if not found.
     */
    Object removeField(String name);

    /**
     * Clear all stored name-value pairs.
     */
    void clear();

    /* NESTED INTERFACES */
    /**
     * A {@link DataContainer} entry which provides access to a field name and
     * it's associated value.
     */
    interface Entry {

        String getName();

        Object getValue();

        <T> T getValue(Class<T> type);

    }
}
