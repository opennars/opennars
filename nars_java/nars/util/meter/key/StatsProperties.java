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

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import static nars.util.meter.util.Util.assertNotEmpty;

/**
 * Provides access to configuration properties that are used by Stajistics
 * internals.
 *
 * @author The Stajistics Project
 */
public abstract class StatsProperties {

    private static final Logger logger = Logger.getLogger(StatsProperties.class.toString());

    private static StatsProperties instance;

    public StatsProperties() {
    }

    /**
     * Obtain the singleton instance of {@link StatsProperties}.
     *
     * @return A {@link StatsProperties} instance, never <tt>null</tt>.
     */
    public static StatsProperties getInstance() {
        if (instance == null) {
            //synchronized (StatsProperties.class) {
                if (instance == null) {
                    instance = new SystemStatsProperties();
                }
            //}
        }

        return instance;
    }

    /**
     * Set the given StatsConfig <tt>instance</tt> as the singleton instance.
     * After this call, calls to {@link #getInstance()} will return
     * <tt>instance</tt>.
     *
     * @param instance The {@link StatsProperties} instance to use, or
     * <tt>null</tt> to have the default implementation loaded upon the next
     * call to {@link #getInstance()}.
     */
    public static void load(final StatsProperties instance) {
        StatsProperties.instance = instance;
    }

    protected abstract Object getPropertyImpl(String key, Object defaultValue);

    /* STRING */
    /**
     * Get a String property.
     *
     * @param key The key for which to return a value.
     *
     * @return The String value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static String getProperty(final String key) {
        return getProperty(key, null);
    }

    /**
     * Get a String property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The String value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static String getProperty(final String key,
            final String defaultValue) {
        Object value = getInstance().getPropertyImpl(key, defaultValue);
        return (value != null) ? value.toString() : null;
    }

    /* BOOLEAN */
    /**
     * Get a Boolean property.
     *
     * @param key The key for which to return a value.
     *
     * @return The Boolean value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static Boolean getBooleanProperty(final String key) {
        return getBooleanProperty(key, null);
    }

    /**
     * Get a Boolean property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The Boolean value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static Boolean getBooleanProperty(final String key,
            final Boolean defaultValue) {
        Boolean value = defaultValue;
        Object objectValue = getInstance().getPropertyImpl(key, defaultValue);

        if (objectValue != null) {
            if (objectValue instanceof Boolean) {
                value = (Boolean) objectValue;
            } else if (objectValue instanceof String) {
                value = Boolean.valueOf((String) objectValue);
            } else {
                logger.severe("Failed to coerce property {}={} into a boolean " + key + " " + objectValue);
            }
        }

        return value;
    }

    /* INTEGER */
    /**
     * Get an Integer property.
     *
     * @param key The key for which to return a value.
     *
     * @return The Integer value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static Integer getIntegerProperty(final String key) {
        return getIntegerProperty(key, null);
    }

    /**
     * Get an Integer property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The Integer value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static Integer getIntegerProperty(final String key,
            final Integer defaultValue) {
        Integer value = defaultValue;
        Object objectValue = getInstance().getPropertyImpl(key, defaultValue);

        if (objectValue != null) {
            if (objectValue instanceof Number) {
                value = ((Number) objectValue).intValue();
            } else if (objectValue instanceof String) {
                try {
                    value = Integer.parseInt((String) objectValue);
                } catch (NumberFormatException nfe) {
                    logger.severe("Failed to parse string property {}={} into an integer " + key + ' ' + objectValue);
                }
            } else {
                logger.severe("Failed to coerce property {}={} into an integer " + key + ' ' + objectValue);
            }
        }

        return value;
    }

    /* LONG */
    /**
     * Get a Long property.
     *
     * @param key The key for which to return a value.
     *
     * @return The Long value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static Long getLongProperty(final String key) {
        return getLongProperty(key, null);
    }

    /**
     * Get a Long property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The Long value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static Long getLongProperty(final String key,
            final Long defaultValue) {
        Long value = defaultValue;
        Object objectValue = getInstance().getPropertyImpl(key, defaultValue);

        if (objectValue != null) {
            if (objectValue instanceof Number) {
                value = ((Number) objectValue).longValue();
            } else if (objectValue instanceof String) {
                try {
                    value = Long.parseLong((String) objectValue);
                } catch (NumberFormatException nfe) {
                    logger.severe("Failed to parse string property {}={} into a long" + key + ' ' + objectValue);
                }
            } else {
                logger.severe("Failed to coerce property {}={} into a long" + key + ' ' + objectValue);
            }
        }

        return value;
    }

    /* FLOAT */
    /**
     * Get a Float property.
     *
     * @param key The key for which to return a value.
     *
     * @return The Float value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static Double getFloatProperty(final String key) {
        return getDoubleProperty(key, null);
    }

    /**
     * Get a Float property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The Float value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static Float getFloatProperty(final String key,
            final Float defaultValue) {
        Float value = defaultValue;
        Object objectValue = getInstance().getPropertyImpl(key, defaultValue);

        if (objectValue != null) {
            if (objectValue instanceof Number) {
                value = ((Number) objectValue).floatValue();
            } else if (objectValue instanceof String) {
                try {
                    value = Float.parseFloat((String) objectValue);
                } catch (NumberFormatException nfe) {
                    logger.severe("Failed to parse string property {}={} into a float" + key + ' ' + objectValue);
                }
            } else {
                logger.severe("Failed to coerce property {}={} into a float" + key + ' ' + objectValue);
            }
        }

        return value;
    }

    /* DOUBLE */
    /**
     * Get a Double property.
     *
     * @param key The key for which to return a value.
     *
     * @return The Double value for the given <tt>key</tt>, or <tt>null</tt> if
     * not found.
     */
    public static Double getDoubleProperty(final String key) {
        return getDoubleProperty(key, null);
    }

    /**
     * Get a Double property.
     *
     * @param key The key for which to return a value.
     * @param defaultValue The value to return when a value cannot be found for
     * the given <tt>key</tt>.
     *
     * @return The Double value for the given <tt>key</tt>, or
     * <tt>defaultValue</tt> if not found.
     */
    public static Double getDoubleProperty(final String key,
            final Double defaultValue) {
        Double value = defaultValue;
        Object objectValue = getInstance().getPropertyImpl(key, defaultValue);

        if (objectValue != null) {
            if (objectValue instanceof Number) {
                value = ((Number) objectValue).doubleValue();
            } else if (objectValue instanceof String) {
                try {
                    value = Double.parseDouble((String) objectValue);
                } catch (NumberFormatException nfe) {
                    logger.severe("Failed to parse string property {}={} into a double" + key + ' ' + objectValue);
                }
            } else {
                logger.severe("Failed to coerce property {}={} into a double" + key + ' ' + objectValue);
            }
        }

        return value;
    }

    public StatsProperties asChildOf(final StatsProperties parent) {
        return new MultiStatsProperties(this, parent);
    }

    /* NESTED CLASSES */
    /**
     * A {@link StatsProperties} implementation that retrieves properties from
     * System properties (a.k.a. JVM arguments).
     *
     * @see System#getProperties()
     */
    public static final class SystemStatsProperties extends StatsProperties {

        @Override
        public Object getPropertyImpl(final String key,
                final Object defaultValue) {
            String strDefaultValue = (defaultValue == null) ? null : defaultValue.toString();
            return System.getProperty(key, strDefaultValue);
        }
    }

    /**
     * A {@link StatsProperties} implementation that retrieves properties from a
     * supplied {@link Map}.
     */
    public static final class MapStatsProperties extends StatsProperties {

        @SuppressWarnings("unchecked")
        private final Map propertyMap;

        @SuppressWarnings("unchecked")
        public MapStatsProperties(final Map propertyMap) {
            //assertNotNull(propertyMap, "propertyMap");
            this.propertyMap = propertyMap;
        }

        @Override
        protected Object getPropertyImpl(final String key,
                final Object defaultValue) {
            Object value = propertyMap.get(key);
            if (value == null) {
                value = defaultValue;
            }

            return value;
        }
    }

    public static class MultiStatsProperties extends StatsProperties {

        private StatsProperties[] properties;

        public MultiStatsProperties(final StatsProperties... properties) {
            assertNotEmpty(properties, "properties");
            this.properties = Arrays.copyOf(properties, properties.length);
        }

        @Override
        protected Object getPropertyImpl(final String key, final Object defaultValue) {
            Object value = null;

            for (StatsProperties p : properties) {
                value = p.getPropertyImpl(key, null);
                if (value != null) {
                    break;
                }
            }

            if (value == null) {
                value = defaultValue;
            }

            return value;
        }
    }
}
