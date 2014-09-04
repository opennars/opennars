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

import java.util.Map;
import java.util.Set;

/**
 * A convenience base implementation of {@link DataContainer}.
 *
 * @author The Stajistics Project
 */
public abstract class AbstractDataContainer implements DataContainer {

    public final Map<String, Object> dataMap;

    protected AbstractDataContainer(final Map<String, Object> dataMap) {
        //assertNotNull(dataMap, "dataMap");
        this.dataMap = dataMap;
    }

    @Override
    public Object get(final String name) {
        return dataMap.get(name);
    }

    @Override
    public Set<String> keySet() {
        return dataMap.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getField(final String name, final Class<T> type) {
        Object value = get(name);
        if (value == null) {
            return null;
        }

        if (type == null) {
            return (T) value;
        }

        return type.cast(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getField(final String name,
            final T defaultValue) {
        if (name == null) {
            return defaultValue;
        }

        T result = defaultValue;
        Object value = get(name);
        if (value != null) {
            try {
                if (defaultValue == null) {
                    result = (T) value;
                } else {
                    result = (T) defaultValue.getClass()
                            .cast(value);
                }
            } catch (ClassCastException cce) {
            }
        }

        return result;
    }

    @Override
    public void put(final String name, final Object value) {
        //assertNotEmpty(name, "name");
        //assertNotNull(value, "value");        
        dataMap.put(name, value);
    }

    @Override
    public Object removeField(final String name) {
        return dataMap.remove(name);
    }

    @Override
    public void clear() {
        dataMap.clear();
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @Override
    public int hashCode() {
        return dataMap.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        AbstractDataContainer other;

        try {
            // This is optimised for the success case (i.e. to avoid instanceof)
            other = (AbstractDataContainer) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        return dataMap.equals(other.dataMap);
    }

}
