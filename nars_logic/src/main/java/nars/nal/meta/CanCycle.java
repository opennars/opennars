package nars.nal.meta;

import nars.truth.Truth;

import java.util.function.BinaryOperator;

/**
 * http://aleph.sagemath.org/?q=qwssnn
 * <patham9> only strong rules are allowing overlap
 * <patham9> except union and revision
 * <patham9> if you look at the graph you see why
 * <patham9> its both rules which allow the conclusion to be stronger than the premises
 */
public interface CanCycle extends BinaryOperator<Truth> {
}
