package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Converter responsible for transforming POJOs to/from JavaFX nodes.
 */
public interface IValueFieldConverter<T> {
    public void setTag(String tag);

    public Node toNode(T object, boolean readOnly, TaggedParameters parameters);

    public T toValue(Node node, boolean readOnly);
}
