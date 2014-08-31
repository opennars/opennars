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
 * A {@link StatsKey} implementation that stores attributes in a {@link Map}. Do not
 * instantiate this class directly. Instead use the {@link StatsKeyFactory} provided by
 * {@link StatsManager#getKeyFactory()}, or {@link StatsFactory#newKey(String)}, or
 * {@link StatsFactory#buildKey(String)}.
 *
 * @author The Stajistics Project
 */
public class DefaultStatsKey extends AbstractStatsKey {

    protected final Map<String,Object> attributes;

    /**
     * Do not create instances directly; use a 
     *
     * @param name The key name. Must not be <tt>null</tt>.
     * @param keyFactory The factory that supports the creation of copies of this StatsKey instance.
     * @param attributes The Map of attributes to be associated with this StatsKey instance. 
     *                   Must not be <tt>null</tt>. This Map is not copied; it is referenced directly.
     * @throws NullPointerException If <tt>attributes</tt> is <tt>null</tt>.
     */
    public DefaultStatsKey(final String namespace,
                           final String name,
                           final Map<String,Object> attributes) {
        super(namespace, name);
        //assertNotNull(attributes, "attributes");

        this.attributes = attributes;

        setHashCode();
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String,Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public int getAttributeCount() {
        return attributes.size();
    }

    @Override
    protected void appendAttributes(final StringBuilder buf) {
        buf.append(attributes);
    }

    @Override
    protected boolean areAttributesEqual(final StatsKey other) {
        // SimpleStatsKey and SingleAttributeStatsKey are optimized for speed, 
        // so use their implementation if possible
        Class<?> keyClass = other.getClass();
        if (keyClass == SingleAttributeStatsKey.class) {
            return ((AbstractStatsKey)other).areAttributesEqual(this);
        }

        return attributes.equals(other.getAttributes());
    }

}
