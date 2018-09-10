package org.opennars.main;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Used to override Parameters with values by name
 */
public class ParametersOverride {
    /**
     * overrides parameter values by name
     * @param parameters (overwritten) parameters of a Reasoner
     * @param overrides specific override values by parameter name
     */
    public static void override(Parameters parameters, Map<String, Object> overrides) {
        for (final Map.Entry<String, Object> iOverride : overrides.entrySet()) {
            final String propertyName = iOverride.getKey();
            final Object value = iOverride.getValue();

            try {
                final Field fieldOfProperty = Parameters.class.getField(propertyName);
                fieldOfProperty.set(parameters, value);
            } catch (NoSuchFieldException e) {
                // ignore
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }
}
