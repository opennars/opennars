package nars.guifx.util;

import com.gs.collections.api.block.procedure.primitive.IntProcedure;
import javafx.beans.property.SimpleIntegerProperty;
import nars.util.data.MutableInteger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

/**
 * wraps various numeric data types
 */
public class MutableIntProperty extends SimpleIntegerProperty {

    private final IntSupplier getter;
    private final IntProcedure setter;


    public MutableIntProperty(MutableInteger a) {
        this(a::intValue, (v) -> a.setValue((float)v));
    }
    public MutableIntProperty(AtomicInteger a) {
        this(a::intValue, (v) -> a.set((int)v));
    }

    public MutableIntProperty(IntSupplier getter, IntProcedure setter) {
        this.getter = getter;
        this.setter = setter;
    }
    @Override
    public int get() {
        int existing = super.get();
        int actual = getter.getAsInt();
        if (existing != actual)
            super.set(actual);
        return actual;
    }

    @Override
    public void set(int newValue) {
        super.set(newValue);
        setter.value(newValue);
        //System.out.println(newValue + " "+ cycle + " activationFactor=" + cycle.activationFactor.getValue());
    }

}
