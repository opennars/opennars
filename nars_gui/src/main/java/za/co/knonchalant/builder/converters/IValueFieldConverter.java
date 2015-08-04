package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import javafx.stage.Window;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Converter responsible for transforming POJOs to/from JavaFX nodes.
 */
public interface IValueFieldConverter<T> {
    public void setTag(String tag);

    public Node convert(T object, boolean readOnly, TaggedParameters parameters);

    public T parse(Node node, boolean readOnly);
}
