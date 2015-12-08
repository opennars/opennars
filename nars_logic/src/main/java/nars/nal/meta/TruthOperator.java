package nars.nal.meta;

import nars.Memory;
import nars.truth.Truth;


@FunctionalInterface
public interface TruthOperator {
    Truth apply(Truth task, Truth belief, Memory m);
}
