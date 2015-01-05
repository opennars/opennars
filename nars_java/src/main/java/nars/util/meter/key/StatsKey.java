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

import java.io.Serializable;
import java.util.Map;
import nars.util.meter.session.StatsSession;

/**
 * A StatsKey acts as a handle for a single target for which statistics can be
 * collected.
 *
 * <p>
 * StatsKeys are composed of a name and, optionally, a set of attributes. One
 * StatsKey is equal to another if the names are equal and the number, names,
 * and values of their attributes are equal. A StatsKey of unique equality
 * represents a single target to be tracked. StatsKeys are also used to address
 * various entities in statistics collection that are associated with the keys
 * target, such as, configuration
 * ({@link org.stajistics.configuration.StatsConfig}) and collected data
 * ({@link StatsSession}). As StatsKeys are immutable, copies can be made using
 * a {@link StatsKeyBuilder} which is obtained with the {@link #buildCopy()}
 * method.</p>
 *
 * <dt>Naming and the Key Hierarchy</dt>
 * <dd>
 * <p>
 * A key name is defined statically in the sense that it is not composed of
 * runtime data. In other words, any key name must be able to be referenced or
 * declared in a static configuration file. For example, it wouldn't make sense
 * for a key name to contain a user ID because a user ID is runtime data, and it
 * would be extremely awkward to manually maintain a secondary list of active
 * user IDs in a static configuration file. Key names should be predictable for
 * the purposes of easily querying the Stajistics API for data related to a
 * key.</p>
 *
 * <p>
 * A valid key name must be greater than zero length. There are no restrictions
 * on which characters can appear in a key name. Dot (.) characters, however,
 * are interpreted as hierarchy delimiters.</p>
 *
 * <p>
 * StatsKeys are organised into a hierarchy through the following naming
 * convention. A key is the ancestor of another key if its name followed by a
 * dot is a prefix of the descendant key name. A key is a parent of a child key
 * if there are no ancestors between it and the descendant key.</p>
 *
 * <p>
 * For example, a key named <tt>"foo"</tt> is the ancestor and parent of a key
 * named
 * <tt>"foo.bar"</tt> and the ancestor of a key named <tt>"foo.bar.baz"</tt>. A
 * key named
 * <tt>"foo.bar.baz"</tt> is the descendant and child of a key named
 * <tt>"foo.bar"</tt> and the descendant of a key named <tt>"foo"</tt>.</p>
 *
 * <p>
 * The primary purpose of the key hierarchy is to discourage key naming clashes
 * that may occur when separate modules using Stajistics, potentially created by
 * different parties, are deployed to the same JVM. As such, it is recommended
 * that key names be defined using the same conventions as for
 * <a
 * href="http://java.sun.com/docs/codeconv/html/CodeConventions.doc8.html">naming
 * Java packages</a>.</p>
 *
 * <p>
 * The secondary purpose of the key hierarchy is to allow more robust control
 * over the configuration of groups of related keys. See
 * {@link org.stajistics.configuration.StatsConfig} and
 * {@link org.stajistics.configuration.StatsConfigManager} for details.</p>
 * </dd>
 *
 * <dt>Attributes and Sub-Keys</dt>
 * <dd>
 * <p>
 * Beyond the key hierarchy described above, there is the notion of sub-keys. A
 * sub-key is just a StatsKey instance like any other key, but by convention, a
 * key can only be called a sub-key if it has one or more associated
 * attributes.</p>
 *
 * <p>
 * The purpose of sub-keys is to allow the client to conveniently refine or
 * narrow the scope of a target for which statistics are collected. Key
 * attributes are intended to hold the runtime data that should not be
 * statically defined in the key name, such as a worker thread name, a URL, or a
 * file name, for example.</p>
 *
 * <p>
 * A key with no attributes is known as a super-key. A key is a sub-key when it
 * has one or more associated attributes. When a key has attributes, it is said
 * to be the sub-key of another key with the same name that does not have any
 * attributes (a super-key). As an example, Key Y is a sub-key of key X if the
 * two keys share the same name and key X has no attributes. Since key X and Y
 * are not equal, they can be assigned to different tracking targets.</p>
 *
 * <p>
 * While the sub-key mechanism (and the key hierarchy mechanism) can be used in
 * any way to support various organizations of statistics tracking, the
 * following is a best-practice example usage of sub-keys:
 * <blockquote>
 * A key named "com.acme.AnvilApp.http.sessions" is defined to track the total
 * number and life span of active HTTP sessions in a web application. A sub-key
 * of this key that has an attribute "user" is set to the ID of the logged in
 * user for the request. Using this configuration allows the collection of
 * statistics for all HTTP sessions, and separately for HTTP sessions per user.
 * </blockquote>
 * </p>
 *
 * </dd>
 *
 * @see StatsFactory
 * @see StatsKeyBuilder
 * @see StatsKeyFactory
 *
 * @author The Stajistics Project
 */
public interface StatsKey extends Comparable<StatsKey>, Serializable {

    String getNamespace();

    /**
     * Get the key name.
     *
     * @return The name of this key.
     */
    String getName();

    /**
     * Obtain the value of an attribute associated with this key by name.
     *
     * @param name The name of the attribute.
     * @return The attribute value or <tt>null</tt> if the attribute does not
     * exist.
     */
    Object getAttribute(String name);

    /**
     * Obtain a {@link Map} of all attributes associated with this key.
     *
     * @return The {@link Map} of attribute names to values. The Map may be
     * empty but never <tt>null</tt>.
     */
    Map<String, Object> getAttributes();

    /**
     * Get the number of attributes associated with this key.
     *
     * @return The number of attributes associated with this key.
     */
    int getAttributeCount();

}
