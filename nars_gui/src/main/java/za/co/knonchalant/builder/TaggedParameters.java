package za.co.knonchalant.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * List of tagged parameters for the Node.
 */
public class TaggedParameters {
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Add an object as a tag to the set of parameters.
     *
     * @param tag   tag name
     * @param param the object
     */
    public void addTag(String tag, Object param) {
        parameters.put(tag, param);
    }

    /**
     * Get the object for the specified tag
     *
     * @param tag tag name
     * @return the object
     */
    public Object get(String tag) {
        return parameters.get(tag);
    }
}
