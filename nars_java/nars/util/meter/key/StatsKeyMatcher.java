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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 *
 * @author The Stajistics Project
 */
public abstract class StatsKeyMatcher implements Serializable {

    /* FACTORY METHODS */
    /**
     * Create a matcher that matches all keys.
     *
     * @return A matcher that matches all keys.
     */
    public static StatsKeyMatcher all() {
        return AllMatcher.INSTANCE;
    }

    /**
     * Create a matcher that doesn't match any keys.
     *
     * @return A matcher that doesn't match any keys.
     */
    public static StatsKeyMatcher none() {
        return NoneMatcher.INSTANCE;
    }

    /**
     * Create a matcher that matches only keys that equal the given
     * <tt>key</tt>.
     *
     * @param key The key for which equal keys should be matched.
     * @return An exact match matcher.
     *
     * @see StatsKey#equals(Object)
     */
    public static StatsKeyMatcher exactMatch(final StatsKey key) {
        return new ExactMatcher(key);
    }

    /**
     * Create a matcher that matches keys that have names equal to the given
     * <tt>keyName</tt>.
     *
     * @param keyName The key name for which keys having an equal name should be
     * matched.
     *
     * @return A key name equality matcher.
     */
    public static StatsKeyMatcher nameEquals(final String keyName) {
        return new EqualsMatcher(MatchTarget.KEY_NAME, keyName);
    }

    /**
     * Create a matcher that matches keys that have an attribute name equal to
     * the given <tt>attrName</tt>.
     *
     * @param attrName The key attribute name for which keys having an equal
     * attribute name should be matched.
     *
     * @return A key attribute name equality matcher.
     */
    public static StatsKeyMatcher attrNameEquals(final String attrName) {
        return new EqualsMatcher(MatchTarget.ATTR_NAME, attrName);
    }

    /**
     * Create a matcher that matches keys that have an attribute value equal to
     * the given <tt>attrValue</tt>.
     *
     * @param attrValue The key attribute value for which keys having an equal
     * attribute value should be matched.
     *
     * @return A key attribute value equality matcher.
     */
    public static StatsKeyMatcher attrValueEquals(final Object attrValue) {
        return new EqualsMatcher(MatchTarget.ATTR_VALUE, attrValue);
    }

    public static StatsKeyMatcher prefix(final String prefix) {
        return new PrefixMatcher(MatchTarget.KEY_NAME, prefix);
    }

    public static StatsKeyMatcher descendentOf(final String keyName) {
        return new DescendentMatcher(keyName);
    }

    public static StatsKeyMatcher ancestorOf(final String keyName) {
        throw new UnsupportedOperationException("TODO");
    }

    public static StatsKeyMatcher childOf(final String keyName) {
        throw new UnsupportedOperationException("TODO");
    }

    public static StatsKeyMatcher parentOf(final String keyName) {
        throw new UnsupportedOperationException("TODO");
    }

    public static StatsKeyMatcher attrNamePrefix(final String prefix) {
        return new PrefixMatcher(MatchTarget.ATTR_NAME, prefix);
    }

    public static StatsKeyMatcher attrValuePrefix(final String prefix) {
        return new PrefixMatcher(MatchTarget.ATTR_VALUE, prefix);
    }

    public static StatsKeyMatcher suffix(final String suffix) {
        return new SuffixMatcher(MatchTarget.KEY_NAME, suffix);
    }

    public static StatsKeyMatcher attrNameSuffix(final String suffix) {
        return new SuffixMatcher(MatchTarget.ATTR_NAME, suffix);
    }

    public static StatsKeyMatcher attrValueSuffix(final String suffix) {
        return new SuffixMatcher(MatchTarget.ATTR_VALUE, suffix);
    }

    public static StatsKeyMatcher contains(final String string) {
        return new ContainsMatcher(MatchTarget.KEY_NAME, string);
    }

    public static StatsKeyMatcher attrNameContains(final String string) {
        return new ContainsMatcher(MatchTarget.ATTR_NAME, string);
    }

    public static StatsKeyMatcher attrValueContains(final String string) {
        return new ContainsMatcher(MatchTarget.ATTR_VALUE, string);
    }

    public static StatsKeyMatcher length(final int length) {
        return new LengthMatcher(MatchTarget.KEY_NAME, length);
    }

    public static StatsKeyMatcher attrNameLength(final int length) {
        return new LengthMatcher(MatchTarget.ATTR_NAME, length);
    }

    public static StatsKeyMatcher attrValueLength(final int length) {
        return new LengthMatcher(MatchTarget.ATTR_VALUE, length);
    }

    public static StatsKeyMatcher matchesRegEx(final String regEx) {
        return matchesRegEx(Pattern.compile(regEx));
    }

    public static StatsKeyMatcher matchesRegEx(final Pattern pattern) {
        return new RegExMatcher(MatchTarget.KEY_NAME, pattern);
    }

    public static StatsKeyMatcher attrNameMatchesRegEx(final String regEx) {
        return attrNameMatchesRegEx(Pattern.compile(regEx));
    }

    public static StatsKeyMatcher attrNameMatchesRegEx(final Pattern pattern) {
        return new RegExMatcher(MatchTarget.ATTR_NAME, pattern);
    }

    public static StatsKeyMatcher attrValueMatchesRegEx(final String regEx) {
        return attrValueMatchesRegEx(Pattern.compile(regEx));
    }

    public static StatsKeyMatcher attrValueMatchesRegEx(final Pattern pattern) {
        return new RegExMatcher(MatchTarget.ATTR_VALUE, pattern);
    }

    public static StatsKeyMatcher depth(final int depth) {
        return new DepthMatcher(depth);
    }

    public static StatsKeyMatcher attributeCount(final int count) {
        return new AttrCountMatcher(count);
    }

    /* COMPOSITION METHODS */
    /**
     * Create a new matcher that negates the result of calls to
     * {@link #matches(StatsKey)} on this matcher.
     *
     * @return The inverse matcher of this matcher.
     */
    public StatsKeyMatcher not() {
        return new NegationMatcher(this);
    }

    /**
     * Create a new composite matcher for which the {@link #matches(StatsKey)}
     * method returns the conjunction of the results of the same method called
     * on this and the passed <tt>matcher</tt>.
     *
     * @param matcher The matcher to conjunct with this matcher.
     *
     * @return A matcher that ANDs this and the given matcher.
     */
    public StatsKeyMatcher and(final StatsKeyMatcher matcher) {
        return new CompositeMatcher(CompositeMatcher.Op.AND, this, matcher);
    }

    /**
     * Create a new composite matcher for which the {@link #matches(StatsKey)}
     * method returns the disjunction of the results of the same method called
     * on this and the passed <tt>matcher</tt>.
     *
     * @param matcher The matcher to disjunct with this matcher.
     *
     * @return A matcher that ORs this and the given matcher.
     */
    public StatsKeyMatcher or(final StatsKeyMatcher matcher) {
        return new CompositeMatcher(CompositeMatcher.Op.OR, this, matcher);
    }

    /**
     * Create a new composite matcher for which the {@link #matches(StatsKey)}
     * method returns the exclusive disjunction of the results of the same
     * method called on this and the passed <tt>matcher</tt>.
     *
     * @param matcher The matcher to exclusively disjunct with this matcher.
     *
     * @return A matcher that XORs this and the given matcher.
     */
    public StatsKeyMatcher xor(final StatsKeyMatcher matcher) {
        return new CompositeMatcher(CompositeMatcher.Op.XOR, this, matcher);
    }

    /* FILTER METHODS */
    public void filter(final Collection<StatsKey> keys) {
        Iterator<StatsKey> itr = keys.iterator();
        while (itr.hasNext()) {
            if (!matches(itr.next())) {
                itr.remove();
            }
        }
    }

    public Collection<StatsKey> filterCopy(final Collection<StatsKey> keys) {
        List<StatsKey> filteredList = new ArrayList<>(keys.size());
        for (StatsKey key : keys) {
            if (matches(key)) {
                filteredList.add(key);
            }
        }
        return Collections.unmodifiableCollection(filteredList);
    }

    public Set<StatsKey> filterCopy(final Set<StatsKey> keys) {
        Set<StatsKey> filteredSet = new HashSet<>(keys.size());
        for (StatsKey key : keys) {
            if (matches(key)) {
                filteredSet.add(key);
            }
        }
        return Collections.unmodifiableSet(filteredSet);
    }

    public <T> void filter(final Map<StatsKey, T> map) {
        Iterator<Map.Entry<StatsKey, T>> itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<StatsKey, T> entry = itr.next();
            if (!matches(entry.getKey())) {
                itr.remove();
            }
        }
    }

    public <T> Map<StatsKey, T> filterCopy(final Map<StatsKey, T> map) {
        Map<StatsKey, T> filteredMap = new HashMap<>(map.size() / 2);
        for (Map.Entry<StatsKey, T> entry : map.entrySet()) {
            if (matches(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(filteredMap);
    }

    public <T> Collection<T> filterToCollection(final Map<StatsKey, T> map) {
        List<T> filteredList = new ArrayList<>(map.size() / 2);
        for (Map.Entry<StatsKey, T> entry : map.entrySet()) {
            if (matches(entry.getKey())) {
                filteredList.add(entry.getValue());
            }
        }
        return Collections.unmodifiableCollection(filteredList);
    }

    public <T> Collection<T> filterToCollection(final Collection<? extends StatsKeyAssociation<T>> associations) {
        List<T> filteredList = new ArrayList<>(associations.size() / 2);
        StatsKey key;
        for (StatsKeyAssociation<T> ka : associations) {
            key = ka.getKey();
            if (matches(key)) {
                filteredList.add(ka.getValue());
            }
        }
        return Collections.unmodifiableCollection(filteredList);
    }

    public <T> Map<StatsKey, T> filterToMap(final Collection<? extends StatsKeyAssociation<T>> associations) {
        Map<StatsKey, T> filteredMap = new HashMap<>(associations.size() / 2);
        StatsKey key;
        for (StatsKeyAssociation<T> ka : associations) {
            key = ka.getKey();
            if (matches(key)) {
                filteredMap.put(key, ka.getValue());
            }
        }
        return Collections.unmodifiableMap(filteredMap);
    }

    public abstract boolean matches(StatsKey key);

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        return (obj instanceof StatsKeyMatcher) && equals((StatsKeyMatcher) obj);
    }

    public abstract boolean equals(StatsKeyMatcher matcher);

    @Override
    public abstract int hashCode();

    /* NESTED CLASSES */
    enum MatchTarget {

        KEY_NAME,
        ATTR_NAME,
        ATTR_VALUE
    }

    private static class NegationMatcher extends StatsKeyMatcher {

        private final StatsKeyMatcher delegate;

        NegationMatcher(final StatsKeyMatcher delegate) {
            //assertNotNull(delegate, "delegate");
            this.delegate = delegate;
        }

        @Override
        public boolean matches(final StatsKey key) {
            return !delegate.matches(key);
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            return delegate.equals(((NegationMatcher) other).delegate);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * delegate.hashCode();
        }
    }

    private static class CompositeMatcher extends StatsKeyMatcher {

        enum Op {

            AND,
            OR,
            XOR
        }

        private final Op op;
        private final StatsKeyMatcher matcher1;
        private final StatsKeyMatcher matcher2;

        CompositeMatcher(final Op op,
                final StatsKeyMatcher matcher1,
                final StatsKeyMatcher matcher2) {
            //assertNotNull(op, "op");
            //assertNotNull(matcher1, "matcher1");
            //assertNotNull(matcher2, "matcher2");

            this.op = op;
            this.matcher1 = matcher1;
            this.matcher2 = matcher2;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (op) {
                case AND:
                    return matcher1.matches(key) && matcher2.matches(key);

                case OR:
                    return matcher1.matches(key) || matcher2.matches(key);

                case XOR:
                    return matcher1.matches(key) ^ matcher2.matches(key);
            }

            throw new Error();
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            CompositeMatcher compositeMatcher = (CompositeMatcher) other;
            return matcher1.equals(compositeMatcher.matcher1)
                    && matcher2.equals(compositeMatcher.matcher2);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * matcher1.hashCode()
                    + 31 * matcher2.hashCode();
        }
    }

    private static class AllMatcher extends StatsKeyMatcher {

        private static final StatsKeyMatcher INSTANCE = new AllMatcher();

        @Override
        public boolean matches(final StatsKey key) {
            return true;
        }

        @Override
        public StatsKeyMatcher not() {
            return NoneMatcher.INSTANCE;
        }

        @Override
        public void filter(final Collection<StatsKey> keys) {
            // Do nothing
        }

        @Override
        public <T> void filter(final Map<StatsKey, T> map) {
            // Do nothing
        }

        @Override
        public Collection<StatsKey> filterCopy(final Collection<StatsKey> keys) {
            return Collections.unmodifiableCollection(new ArrayList<StatsKey>(keys));
        }

        @Override
        public <T> Map<StatsKey, T> filterCopy(final Map<StatsKey, T> map) {
            return Collections.unmodifiableMap(map);
        }

        @Override
        public <T> Collection<T> filterToCollection(final Map<StatsKey, T> map) {
            return Collections.unmodifiableCollection(new ArrayList<T>(map.values()));
        }

        @Override
        public <T> Collection<T> filterToCollection(Collection<? extends StatsKeyAssociation<T>> associations) {
            List<T> filteredList = new ArrayList<>(associations.size());
            for (StatsKeyAssociation<T> ka : associations) {
                filteredList.add(ka.getValue());
            }
            return Collections.unmodifiableCollection(filteredList);
        }

        @Override
        public <T> Map<StatsKey, T> filterToMap(Collection<? extends StatsKeyAssociation<T>> associations) {
            Map<StatsKey, T> filteredMap = new HashMap<>(associations.size());
            for (StatsKeyAssociation<T> ka : associations) {
                filteredMap.put(ka.getKey(), ka.getValue());
            }
            return Collections.unmodifiableMap(filteredMap);
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            return other == INSTANCE;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    private static class NoneMatcher extends StatsKeyMatcher {

        private static final StatsKeyMatcher INSTANCE = new NoneMatcher();

        @Override
        public boolean matches(final StatsKey other) {
            return false;
        }

        @Override
        public StatsKeyMatcher not() {
            return AllMatcher.INSTANCE;
        }

        @Override
        public void filter(final Collection<StatsKey> keys) {
            keys.clear();
        }

        @Override
        public <T> void filter(final Map<StatsKey, T> map) {
            map.clear();
        }

        @Override
        public Collection<StatsKey> filterCopy(final Collection<StatsKey> keys) {
            return Collections.emptyList();
        }

        @Override
        public <T> Map<StatsKey, T> filterCopy(final Map<StatsKey, T> map) {
            return Collections.emptyMap();
        }

        @Override
        public <T> Collection<T> filterToCollection(final Map<StatsKey, T> map) {
            return Collections.emptyList();
        }

        @Override
        public <T> Collection<T> filterToCollection(final Collection<? extends StatsKeyAssociation<T>> associations) {
            return Collections.emptyList();
        }

        @Override
        public <T> Map<StatsKey, T> filterToMap(final Collection<? extends StatsKeyAssociation<T>> associations) {
            return Collections.emptyMap();
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            return other == INSTANCE;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    private static class ExactMatcher extends StatsKeyMatcher {

        private final StatsKey testKey;

        ExactMatcher(final StatsKey testKey) {
            //assertNotNull(testKey, "testKey");

            this.testKey = testKey;
        }

        @Override
        public boolean matches(final StatsKey key) {
            return key.equals(testKey);
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            return testKey.equals(((ExactMatcher) other).testKey);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * testKey.hashCode();
        }
    }

    private static class EqualsMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final Object test;

        EqualsMatcher(final MatchTarget target, final Object test) {
            //assertNotNull(target, "target");
            //assertNotNull(test, "test");

            this.target = target;
            this.test = test;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return key.getName().equals(test);
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (attrName.equals(test)) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (attrValue.equals(test)) {
                            return true;
                        }
                    }
                    break;
            }

            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            EqualsMatcher equalsMatcher = (EqualsMatcher) other;
            return target == equalsMatcher.target
                    && test.equals(equalsMatcher.test);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * test.hashCode();
        }
    }

    private static class PrefixMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final String prefix;

        PrefixMatcher(final MatchTarget target, final String prefix) {
            //assertNotNull(target, "target");
            //assertNotNull(prefix, "prefix");

            this.target = target;
            this.prefix = prefix;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return key.getName().startsWith(prefix);
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (attrName.startsWith(prefix)) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (attrValue.toString().startsWith(prefix)) {
                            return true;
                        }
                    }
                    break;
            }

            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            PrefixMatcher prefixMatcher = (PrefixMatcher) other;
            return target == prefixMatcher.target
                    && prefix.equals(prefixMatcher.prefix);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * prefix.hashCode();
        }
    }

    private static class DescendentMatcher extends PrefixMatcher {

        public DescendentMatcher(final String keyName) {
            super(MatchTarget.KEY_NAME, formatKeyName(keyName));
        }

        private static String formatKeyName(String keyName) {
            //assertNotNull(keyName, "keyName");

            if (!keyName.endsWith(".")) {
                keyName += ".";
            }
            return keyName;
        }
    }

    private static class SuffixMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final String suffix;

        SuffixMatcher(final MatchTarget target, final String suffix) {
            //assertNotNull(target, "target");
            //assertNotNull(suffix, "suffix");

            this.target = target;
            this.suffix = suffix;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return key.getName().endsWith(suffix);
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (attrName.endsWith(suffix)) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (attrValue.toString().endsWith(suffix)) {
                            return true;
                        }
                    }
                    break;
            }

            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            SuffixMatcher suffixMatcher = (SuffixMatcher) other;
            return target == suffixMatcher.target
                    && suffix.equals(suffixMatcher.suffix);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * suffix.hashCode();
        }
    }

    private static class ContainsMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final String string;

        ContainsMatcher(final MatchTarget target, final String string) {
            //assertNotNull(target, "target");
            //assertNotNull(string, "string");

            this.target = target;
            this.string = string;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return key.getName().contains(string);
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (attrName.contains(string)) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (attrValue.toString().contains(string)) {
                            return true;
                        }
                    }
                    break;
            }

            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            ContainsMatcher containsMatcher = (ContainsMatcher) other;
            return target == containsMatcher.target
                    && string.equals(containsMatcher.string);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * string.hashCode();
        }
    }

    private static class DepthMatcher extends StatsKeyMatcher {

        private final int depth;

        DepthMatcher(int depth) {
            if (depth < 1) {
                depth = 1;
            }
            this.depth = depth;
        }

        @Override
        public boolean matches(final StatsKey key) {
            int count = countHeirarchyDelimiters(key.getName()) + 1;
            return depth == count;
        }

        private int countHeirarchyDelimiters(final String name) {
            int count = 0;
            final char[] chars = name.toCharArray();

            for (char c : chars) {
                if (c == StatsConstants.KEY_HIERARCHY_DELIMITER) {
                    count++;
                }
            }

            return count;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            return depth == ((DepthMatcher) other).depth;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * depth;
        }
    }

    private static class AttrCountMatcher extends StatsKeyMatcher {

        private final int count;

        public AttrCountMatcher(int count) {
            if (count < 0) {
                count = 0;
            }

            this.count = count;
        }

        @Override
        public boolean matches(final StatsKey key) {
            return count == key.getAttributeCount();
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            if (!(other instanceof AttrCountMatcher)) {
                return false;
            }

            return count == ((AttrCountMatcher) other).count;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * count;
        }
    }

    private static class LengthMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final int length;

        LengthMatcher(final MatchTarget target,
                int length) {
            //assertNotNull(target, "target");
            if (length < 0) {
                length = 0;
            }

            this.target = target;
            this.length = length;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return key.getName().length() == length;
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (attrName.length() == length) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (attrValue.toString().length() == length) {
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            LengthMatcher lengthMatcher = (LengthMatcher) other;
            return target == lengthMatcher.target
                    && length == lengthMatcher.length;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * length;
        }
    }

    private static class RegExMatcher extends StatsKeyMatcher {

        private final MatchTarget target;
        private final Pattern pattern;

        public RegExMatcher(final MatchTarget target,
                final Pattern pattern) {
            //assertNotNull(target, "target");
            //assertNotNull(pattern, "pattern");

            this.target = target;
            this.pattern = pattern;
        }

        @Override
        public boolean matches(final StatsKey key) {
            switch (target) {
                case KEY_NAME:
                    return pattern.matcher(key.getName()).matches();
                case ATTR_NAME:
                    for (String attrName : key.getAttributes().keySet()) {
                        if (pattern.matcher(attrName).matches()) {
                            return true;
                        }
                    }
                    break;
                case ATTR_VALUE:
                    for (Object attrValue : key.getAttributes().values()) {
                        if (pattern.matcher(attrValue.toString()).matches()) {
                            return true;
                        }
                    }
                    break;
            }

            return false;
        }

        @Override
        public boolean equals(final StatsKeyMatcher other) {
            RegExMatcher regExMatcher = (RegExMatcher) other;
            return target == regExMatcher.target
                    && pattern.equals(regExMatcher.pattern);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode()
                    + 31 * target.hashCode()
                    + 31 * pattern.hashCode();
        }
    }
}
