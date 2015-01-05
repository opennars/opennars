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

import nars.util.meter.session.StatsSession;

/**
 * <p>
 * Stores statistical data and meta data as a mapping of key-value pairs known
 * as fields. Statistical data is stored as fields, while meta data that is not
 * directly related to statistics can be stored with the DataSet.</p>
 *
 * @author The Stajistics Project
 */
public interface DataSet extends DataContainer {

    


    /**
     * Standard field names.
     */
    interface Field {

        public static final String HITS = "hits";
        public static final String FIRST_HIT_STAMP = "firstHitStamp";
        public static final String LAST_HIT_STAMP = "lastHitStamp";
        public static final String COMMITS = "commits";
        public static final String FIRST = "first";
        public static final String LAST = "last";
        public static final String MIN = "min";
        public static final String MAX = "max";
        public static final String SUM = "sum";
        //public static final String STDEV = "stdDev";

        /**
         * DefaultField values for standard field names.
         */
        interface DefaultField {

            public static final Long HITS = 0L;
            public static final Long FIRST_HIT_STAMP = -1L;
            public static final Long LAST_HIT_STAMP = -1L;
            public static final Long COMMITS = 0L;
            public static final Double FIRST = Double.NaN;
            public static final Double LAST = Double.NaN;
            public static final Double MIN = Double.NaN;
            public static final Double MAX = Double.NaN;
            public static final Double SUM = 0D;
            //public static final Double STDEV = 0D;
        }
    }

    /**
     * Get the time stamp at which this DataSet was collected from a
     * {@link StatsSession}.
     *
     * @return The collection time stamp.
     */
    long getCollectionTimeStamp();

    /**
     * Determine if the collection that resulted in this DataSet drained the
     * {@link StatsSession}.
     *
     * @return <tt>true</tt> if the session was drained.
     * @see StatsSession#drainData()
     */
    boolean isSessionDrained();

    /**
     * Determine if this DataSet has any meta data. Internal meta data
     * structures are lazily initialised when {@link #getMetaData()} is called
     * to try to save resources where possible. Therefore, calling this method
     * is preferable to calling <tt>!getMetaData().isEmpty()</tt> because this
     * method will not lazily initialise the meta data structures.
     *
     * @return <tt>true</tt> if at least one meta data attribute is available.
     */
    boolean hasMetaData();

    /**
     * Obtain a {@link MetaData} instance which contains meta data related to
     * this DataSet.
     *
     * @return A {@link MetaData} instance, never <tt>null</tt>.
     */
    MetaData getMetaData();

    /** double value of a field */
    public double d(String fieldName);

    /** long integer value of a field */
    public long i(String fieldName);
    
    
    default public double sum() { return d("sum"); }
    default public double max() { return d("max"); }
    default public double min() { return d("min"); }
    default public long hits() { return i("hits"); }
    
    default public double median() { return (max() + min())/2.0; }
    default public double mean() { 
        long h = hits();
        if (h == 0) return 0;
        return (sum() / h); 
    }


}
