package nars.guifx.util;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

/**
 * Created by me on 8/13/15.
 */
public class LerpDoubleProperty extends SimpleDoubleProperty {

    double epsilon = 0.01;

    double rate = 0.25;

    public double target;
    boolean stable;

    public LerpDoubleProperty(double v) {
        super(v);
        target = v;
        stable = true;
    }

    public double getTarget() {
        return target;
    }

    @Override
    public void bind(ObservableValue<? extends Number> rawObservable) {
        super.bind(rawObservable);
    }

    @Override
    public void bindBidirectional(Property<Number> other) {
        super.bindBidirectional(other);
    }

    @Override
    public void setValue(Number v) {
        set(v.doubleValue());
    }

    @Override
    public void set(double v) {
        target = v;
        updateStability();
    }

    /**
     * call each animation frame
     */
    public void update() {
        if (stable) return;

        double v = getValue() * (1.0d - rate) + target * rate;

        super.set(v);

        updateStability();
    }


    private void updateStability() {
        stable = (Math.abs(target - get()) <= epsilon);
    }

    public void setTargetPlus(double d) {
        set(getTarget() + d);
    }
}
