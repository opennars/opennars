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

import java.util.Collections;
import java.util.Set;

/**
 * A {@link DataSet} implementation conforming to the null object pattern.
 *
 * @author The Stajistics Project
 */
public final class NullDataSet implements DataSet {

    private static final NullDataSet instance = new NullDataSet();

    private NullDataSet() {
    }

    public static NullDataSet getInstance() {
        return instance;
    }

    @Override
    public long getCollectionTimeStamp() {
        return 0;
    }

    @Override
    public boolean isSessionDrained() {
        return false;
    }

    @Override
    public boolean hasMetaData() {
        return false;
    }

    @Override
    public MetaData getMetaData() {
        return NullMetaData.getInstance();
    }

    /**
     * @return <tt>null</tt>.
     */
    @Override
    public Object get(String name) {
        return null;
    }

    /**
     * @return <tt>null</tt>.
     */
    @Override
    public <T> T getField(String name, Class<T> type) {
        return null;
    }

    /**
     * @return <tt>defaultValue</tt>.
     */
    @Override
    public <T> T getField(String name, T defaultValue) {
        return defaultValue;
    }

    /**
     * @return An empty Set.
     */
    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    /**
     * @return <tt>true</tt>.
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * @return <tt>0</tt>.
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Does nothing.
     */
    @Override
    public void clear() {
    }

    /**
     * Does nothing.
     *
     * @return <tt>null</tt>.
     */
    @Override
    public Object removeField(String name) {
        return null;
    }

    /**
     * Does nothing.
     */
    @Override
    public void put(String name, Object value) {
    }

}
