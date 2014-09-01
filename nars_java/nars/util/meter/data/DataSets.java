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

import java.util.Set;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public class DataSets {

    private DataSets() {
    }

    public static DataSet unmodifiable(final DataSet dataSet) {
        if (dataSet.getClass() == ImmutableDataSetDecorator.class) {
            return dataSet;
        }

        return new ImmutableDataSetDecorator(dataSet);
    }

    /* NESTED CLASSES */
    private static final class ImmutableDataSetDecorator implements DataSet {

        private final DataSet delegate;
        private final MetaData delegateMetaData;

        ImmutableDataSetDecorator(final DataSet delegate) {
            //assertNotNull(delegate, "delegate");
            this.delegate = delegate;

            if (delegate.hasMetaData()) {
                this.delegateMetaData = new ImmutableMetaDataDecorator(delegate.getMetaData());
            } else {
                this.delegateMetaData = NullMetaData.getInstance();
            }
        }

        @Override
        public boolean hasMetaData() {
            return !delegateMetaData.isEmpty();
        }

        @Override
        public MetaData getMetaData() {
            return delegateMetaData;
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
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(final String name) {
            return delegate.get(name);
        }

        @Override
        public <T> T getField(final String name, final Class<T> type) throws ClassCastException {
            return delegate.getField(name, type);
        }

        @Override
        public <T> T getField(final String name, final T defaultValue) {
            return delegate.getField(name, defaultValue);
        }

        @Override
        public Set<String> keySet() {
            // Already unmodifiable
            return delegate.keySet();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Object removeField(final String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(final String name, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return delegate.size();
        }

    }

    private static final class ImmutableMetaDataDecorator implements MetaData {

        private final MetaData delegate;

        ImmutableMetaDataDecorator(final MetaData delegate) {
            //assertNotNull(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(final String name) {
            return delegate.get(name);
        }

        @Override
        public <T> T getField(final String name, final Class<T> type) throws ClassCastException {
            return delegate.getField(name, type);
        }

        @Override
        public <T> T getField(final String name, final T defaultValue) {
            return delegate.getField(name, defaultValue);
        }

        @Override
        public Set<String> keySet() {
            // Already unmodifiable
            return delegate.keySet();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Object removeField(final String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(final String name, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return delegate.size();
        }
    }

}
