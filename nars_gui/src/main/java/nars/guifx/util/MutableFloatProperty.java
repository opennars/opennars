package nars.guifx.util;

import com.google.common.util.concurrent.AtomicDouble;
import com.gs.collections.api.block.procedure.primitive.DoubleProcedure;
import javafx.beans.property.SimpleDoubleProperty;
import nars.util.data.MutableDouble;
import nars.util.data.MutableInteger;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

/**
 * wraps various numeric data types
 */
public class MutableFloatProperty extends SimpleDoubleProperty {

    private final DoubleSupplier getter;
    private final DoubleProcedure setter;


    public MutableFloatProperty(MutableFloat a) {
        this(a::getValue, (v) -> a.setValue((float)v));
    }
    public MutableFloatProperty(MutableDouble a) {
        this(a::doubleValue, a::setValue);
    }
    public MutableFloatProperty(AtomicDouble a) {
        this(a::doubleValue, a::set);
    }
    public MutableFloatProperty(AtomicInteger a) {
        this(a::doubleValue, (v) -> a.set((int)Math.round(v)));
    }
    public MutableFloatProperty(MutableInteger a) {
        this(a::doubleValue, (v) -> a.set(Math.round(v)));
    }

    public MutableFloatProperty(DoubleSupplier getter, DoubleProcedure setter) {
        this.getter = getter;
        this.setter = setter;
    }
    @Override
    public double get() {
        double existing = super.get();
        double actual = getter.getAsDouble();
        if (existing != actual)
            super.set(actual);
        return actual;
    }

    @Override
    public void set(double newValue) {
        super.set(newValue);
        setter.value(newValue);
        //System.out.println(newValue + " "+ cycle + " activationFactor=" + cycle.activationFactor.getValue());
    }

}
