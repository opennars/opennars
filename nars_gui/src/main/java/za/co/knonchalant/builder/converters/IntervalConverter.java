package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import javafx.scene.control.Slider;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Created by me on 8/5/15.
 */
public class IntervalConverter extends BaseConverter<Interval> {

    @Override
    public Node toNode(Interval object, boolean readOnly, TaggedParameters parameters) {
        return new Slider();
    }

    @Override
    public Interval toValue(Node node, boolean readOnly) {
        return new Interval(0,0);
    }
}
