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
package nars.util.meter.session;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import nars.util.meter.data.DataSet;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.data.Field;
import nars.util.meter.key.StatsKey;
import nars.util.meter.recorder.DataRecorder;
import nars.util.meter.util.Misc;

/**
 *
 * @author The Stajistics Project
 */
public abstract class AbstractStatsSession implements StatsSession {

    private static final Logger logger = Logger.getLogger(AbstractStatsSession.class.toString());

    protected static final DataRecorder[] EMPTY_DATA_RECORDER_ARRAY = new DataRecorder[0];

    protected static final DecimalFormat DECIMAL_FORMAT;

    static {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        dfs.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.###", dfs);
        DECIMAL_FORMAT.setGroupingSize(Byte.MAX_VALUE);
    }

    protected final StatsKey key;
    

    protected final DataRecorder[] dataRecorders;

    public AbstractStatsSession(final StatsKey key, final DataRecorder... dataRecorders) {
        //assertNotNull(key, "key");
        //assertNotNull(eventManager, "eventManager");

        this.key = key;

        if (dataRecorders == null) {
            this.dataRecorders = EMPTY_DATA_RECORDER_ARRAY;
        } else {
            this.dataRecorders = dataRecorders;
        }
    }

    protected abstract void setHits(long hits);

    protected abstract void setFirstHitStamp(long firstHitStamp);

    protected abstract void setLastHitStamp(long lastHitStamp);

    protected abstract void setCommits(long commits);

    protected abstract void setFirst(double first);

    protected abstract void setLast(double last);

    protected abstract void setMin(double min);

    protected abstract void setMax(double max);

    protected abstract void setSum(double sum);

    

    @Override
    public StatsKey getKey() {
        return key;
    }

    @Override
    public List<DataRecorder> getDataRecorders() {
        return Collections.unmodifiableList(Arrays.asList(dataRecorders));
    }

    @Override
    public Object getField(String name) {
        // Intern the name to allow fast reference equality checks
        name = name.intern();

        // Check basic fields
        if (name == Field.HITS) {
            return getHits();
        }
        if (name == Field.FIRST_HIT_STAMP) {
            return getFirstHitStamp();
        }
        if (name == Field.LAST_HIT_STAMP) {
            return getLastHitStamp();
        }
        if (name == Field.COMMITS) {
            return getCommits();
        }
        if (name == Field.FIRST) {
            return getFirst();
        }
        if (name == Field.LAST) {
            return getLast();
        }
        if (name == Field.MIN) {
            return getMin();
        }
        if (name == Field.MAX) {
            return getMax();
        }
        if (name == Field.SUM) {
            return getSum();
        }
        /*if (name == DataSet.Field.STDEV) {
         return getStdev();
         }*/

        // Check DataRecorder fields
        final int dataRecorderCount = dataRecorders.length;
        for (int i = 0; i < dataRecorderCount; i++) {
            try {
                if (dataRecorders[i].getSupportedFieldNames().contains(name)) {
                    Object result = dataRecorders[i].getField(this, name);
                    if (result != null) {
                        return result;
                    }
                }
            } catch (Exception e) {
                Misc.logHandledException(logger, e, "Failed to getField({}) from {}", name, dataRecorders[i]);
                Misc.handleUncaughtException(getKey(), e);
            }
        }

        // Not found
        return null;
    }

    /**
     * A factory method for creating a DataSet instance that will be populated
     * with this session's data.
     *
     * @param drainedSession
     * @return
     */
    protected DataSet createDataSet(final boolean drainedSession) {
        DataSet dataSet = new DefaultDataSet(System.currentTimeMillis(),
                drainedSession,
                //new FastPutsLinkedMap<>()
                new HashMap()
        ) {
                    @Override
                    protected Map<String, Object> createMetaDataMap() {
                        //return new FastPutsLinkedMap<>();
                        return new HashMap();
                    }
                };

        return dataSet;
    }

    @Override
    public DataSet collectData() {
        final DataSet dataSet = createDataSet(false);
        collectData(dataSet);
        return dataSet;
    }

    protected void collectData(final DataSet dataSet) {
        dataSet.put(Field.HITS, getHits());
        dataSet.put(Field.FIRST_HIT_STAMP, getFirstHitStamp());
        dataSet.put(Field.LAST_HIT_STAMP, getLastHitStamp());
        dataSet.put(Field.COMMITS, getCommits());
        dataSet.put(Field.FIRST, getFirst());
        dataSet.put(Field.LAST, getLast());
        dataSet.put(Field.MIN, getMin());
        dataSet.put(Field.MAX, getMax());
        dataSet.put(Field.SUM, getSum());

        for (final DataRecorder dataRecorder : dataRecorders) {
            //try {
            dataRecorder.collectData(this, dataSet);
            /*} catch (Exception e) {
             Misc.logHandledException(logger, e, "Failed to collectData() from {}", dataRecorder);
             Misc.handleUncaughtException(getKey(), e);
             }*/
        }
    }

    protected void restoreState(final DataSet dataSet) {
        //assertNotNull(dataSet, "dataSet");

        if (!dataSet.isEmpty()) {

            Long restoredHits = dataSet.getField(Field.HITS,
                    Field.DefaultField.HITS);
            Long restoredFirstHitStamp = dataSet.getField(Field.FIRST_HIT_STAMP,
                    Field.DefaultField.FIRST_HIT_STAMP);
            Long restoredLastHitStamp = dataSet.getField(Field.LAST_HIT_STAMP,
                    Field.DefaultField.LAST_HIT_STAMP);

            // Only restore if hits, firstHitStamp, and lastHitStamp are defined
            if (restoredHits > Field.DefaultField.HITS
                    && restoredFirstHitStamp > Field.DefaultField.FIRST_HIT_STAMP
                    && restoredLastHitStamp > Field.DefaultField.LAST_HIT_STAMP) {

                setHits(restoredHits);
                setFirstHitStamp(restoredFirstHitStamp);
                setLastHitStamp(restoredLastHitStamp);

                Long restoredCommits = dataSet.getField(Field.COMMITS,
                        Field.DefaultField.COMMITS);
                Double restoredFirst = dataSet.getField(Field.FIRST, Double.class);
                Double restoredLast = dataSet.getField(Field.LAST, Double.class);

                // Only restore "update()" data if commits, first, and last are defined
                if (restoredCommits > Field.DefaultField.COMMITS
                        && restoredFirst != null
                        && restoredLast != null) {

                    setCommits(restoredCommits);
                    setFirst(restoredFirst);
                    setLast(restoredLast);
                    setMin(dataSet.getField(Field.MIN, Double.POSITIVE_INFINITY));
                    setMax(dataSet.getField(Field.MAX, Double.NEGATIVE_INFINITY));
                    setSum(dataSet.getField(Field.SUM, Field.DefaultField.SUM));

                    // Restore DataRecorders
                    for (DataRecorder dataRecorder : dataRecorders) {
                        try {
                            dataRecorder.restore(dataSet);
                        } catch (Exception e) {
                            Misc.logHandledException(logger, e, "Failed to restore {}", dataRecorder);
                            Misc.handleUncaughtException(getKey(), e);
                        }
                    }
                }
            }
        }
    }

    protected void clearState() {
        setHits(Field.DefaultField.HITS);
        setFirstHitStamp(Field.DefaultField.FIRST_HIT_STAMP);
        setLastHitStamp(Field.DefaultField.LAST_HIT_STAMP);
        setCommits(Field.DefaultField.COMMITS);
        setFirst(Double.NEGATIVE_INFINITY); // The proper default is taken care of in getFirst()
        setLast(Field.DefaultField.LAST);
        setMin(Double.POSITIVE_INFINITY);
        setMax(Double.NEGATIVE_INFINITY);
        setSum(Field.DefaultField.SUM);

        for (DataRecorder dataRecorder : dataRecorders) {
            try {
                dataRecorder.clear();
            } catch (Exception e) {
                Misc.logHandledException(logger, e, "Failed to clear {}", dataRecorder);
                Misc.handleUncaughtException(getKey(), e);
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder buf = new StringBuilder(512);

        buf.append(StatsSession.class.getSimpleName());
        buf.append("[key=");
        buf.append(key);
        buf.append(",hits=");
        buf.append(getHits());
        buf.append(",firstHitStamp=");
        buf.append(getFirstHitStamp());
        buf.append(",lastHitStamp=");
        buf.append(getLastHitStamp());
        buf.append(",commits=");
        buf.append(getCommits());
        buf.append(",first=");
        buf.append(DECIMAL_FORMAT.format(getFirst()));
        buf.append(",last=");
        buf.append(DECIMAL_FORMAT.format(getLast()));
        buf.append(",min=");
        buf.append(DECIMAL_FORMAT.format(getMin()));
        buf.append(",max=");
        buf.append(DECIMAL_FORMAT.format(getMax()));
        buf.append(",sum=");
        buf.append(DECIMAL_FORMAT.format(getSum()));
        buf.append(']');

        return buf.toString();
    }

}
