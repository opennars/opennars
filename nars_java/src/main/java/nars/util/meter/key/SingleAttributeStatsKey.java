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
package nars.util.meter.key;

import java.util.Collections;
import java.util.Map;

/**
 * A {@link StatsKey} implementation that can store only a single attribute. Do
 * not instantiate this class directly. Instead use the {@link StatsKeyFactory}
 * provided by {@link StatsManager#getKeyFactory()}, or
 * {@link Stats#newKey(String)}, or {@link Stats#buildKey(String)}.
 *
 * @author The Stajistics Project
 */
public class SingleAttributeStatsKey extends AbstractStatsKey {

    private final String attrName;
    private final Object attrValue;

    /**
     * Create a new instance.
     *
     * @param name The key name. Must not be <tt>null</tt>.
     * @param keyFactory The factory that supports the creation of copies of
     * this StatsKey instance.
     * @param attrName The sole attribute name. Can only be <tt>null</tt> if
     * <tt>attrValue</tt> is <tt>null</tt>.
     * @param attrValue The sole attribute value. May be <tt>null</tt>.
     * @throws NullPointerException If <tt>name</tt> is <tt>null</tt>. If
     * <tt>attrName</tt> is <tt>null</tt> and <tt>attrValue</tt> is not.
     */
    public SingleAttributeStatsKey(final String namespace,
            final String name,
            final String attrName,
            final Object attrValue) {
        super(namespace, name);

        if (attrName == null && attrValue != null) {
            throw new NullPointerException("attrValue");
        }

        this.attrName = attrName;
        this.attrValue = attrValue;

        setHashCode();
    }

    @Override
    public Object getAttribute(final String name) {
        if (name.equals(attrName)) {
            return attrValue;
        }

        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (attrName == null) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(attrName, attrValue);
    }

    @Override
    public int getAttributeCount() {
        return attrName == null ? 0 : 1;
    }

    @Override
    protected boolean areAttributesEqual(final StatsKey other) {
        if (getAttributeCount() != other.getAttributeCount()) {
            return false;
        }

        if (attrName == null) {
            return true;
        }

        return attrValue.equals(other.getAttribute(attrName));
    }

    @Override
    protected void appendAttributes(final StringBuilder buf) {
        buf.append('{');
        buf.append(attrName);
        buf.append('=');
        buf.append(attrValue);
        buf.append('}');
    }
}
