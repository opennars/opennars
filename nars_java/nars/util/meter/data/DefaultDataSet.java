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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class DefaultDataSet extends AbstractDataContainer implements DataSet {

    private MetaData metaData = null;

    private long collectionTimeStamp;
    private boolean drainedSession;

    public DefaultDataSet(final Map<String, Object> dataMap) {
        this(0, false, dataMap);
    }

    public DefaultDataSet(final long collectionTimeStamp,
            final boolean drainedSession) {
        this(collectionTimeStamp, drainedSession, new ConcurrentHashMap<String, Object>());
    }

    public DefaultDataSet(final long collectionTimeStamp,
            final boolean drainedSession,
            final Map<String, Object> dataMap) {
        super(dataMap);
        this.collectionTimeStamp = collectionTimeStamp;
        this.drainedSession = drainedSession;
    }

    @Override
    public long getCollectionTimeStamp() {
        return collectionTimeStamp;
    }

    @Override
    public boolean isSessionDrained() {
        return drainedSession;
    }

    @Override
    public boolean hasMetaData() {
        return metaData != null && !metaData.isEmpty();
    }

    protected Map<String, Object> createMetaDataMap() {
        return new HashMap<String, Object>();
    }

    @Override
    public MetaData getMetaData() {
        if (metaData == null) {
            metaData = new DefaultMetaData(createMetaDataMap());
        }
        return metaData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (collectionTimeStamp ^ (collectionTimeStamp >>> 32));
        result = prime * result + (drainedSession ? 1231 : 1237);
        result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }

        DataSet other;
        try {
            other = (DataSet) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        if (collectionTimeStamp != other.getCollectionTimeStamp()) {
            return false;
        }
        if (drainedSession != other.isSessionDrained()) {
            return false;
        }

        if (hasMetaData()) {
            if (!other.hasMetaData()) {
                return false;
            }

            return (metaData.equals(other.getMetaData()));

        } else {
            if (other.hasMetaData()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return dataMap.toString();
    }

    @Override
    public double d(final String fieldName) {
        return getField(fieldName, Double.class);
    }

    @Override
    public long i(final String fieldName) {
        return getField(fieldName, Long.class);
    }

    
    public double n(final String fieldName) {        
        Object o = dataMap.get(fieldName);
        if (o instanceof Double)
            return ((Double)o).doubleValue();
        if (o instanceof Float)
            return ((Float)o).doubleValue();
        if (o instanceof Integer)
            return ((Integer)o).doubleValue();
        return 0;
    }
     
    public double[] toArray(double... extraFields) {
        Set<String> k = keySet();
        int s = k.size();
        double d[] = new double[s + extraFields.length];
        int i = 0;
        for (String field : k) {
            d[i++] = n(field);
        }
        for (double e : extraFields) {
            d[i++] = e;
        }
        return d;
    }
}
