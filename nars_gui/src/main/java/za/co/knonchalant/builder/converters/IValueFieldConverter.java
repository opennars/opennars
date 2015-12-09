package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Converter responsible for transforming POJOs to/from JavaFX nodes.
 */
public interface IValueFieldConverter<T> {
    void setTag(String tag);

    Node toNode(T object, boolean readOnly, TaggedParameters parameters);

    T toValue(Node node, boolean readOnly);
}
