package nars.util.data;

import java.util.function.Supplier;

/**
 * wraps a supplier (ex: lambda) with a label appearing in toString()
 */
public class LabeledSupplier<X> implements Supplier<X> {
    private final Supplier<X> s;
    private final String label;

    public static <X> LabeledSupplier<X> supply(String x, Supplier<X> sup) {
        return new LabeledSupplier(x, sup);
    }

    LabeledSupplier(String label, Supplier<X> sup) {
        s = sup;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public X get() {
        return s.get();
    }
}
