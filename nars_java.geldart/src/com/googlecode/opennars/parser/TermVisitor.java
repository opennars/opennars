package com.googlecode.opennars.parser;

import com.googlecode.opennars.language.*;

/**
 * A visitor interface for terms in NARS to make it easier to serialise things
 * @author jgeldart
 *
 * @param <R> The return type of this visitor
 * @param <A> The argument type
 */
public interface TermVisitor<R, A> {
	
	R visit(BooleanLiteral p, A arg);
	R visit(Conjunction p, A arg);
	R visit(ConjunctionParallel p, A arg);
	R visit(ConjunctionSequence p, A arg);
	R visit(DifferenceExt p, A arg);
	R visit(DifferenceInt p, A arg);
	R visit(Disjunction p, A arg);
	R visit(Equivalence p, A arg);
	R visit(EquivalenceAfter p, A arg);
	R visit(EquivalenceWhen p, A arg);
	R visit(ImageExt p, A arg);
	R visit(ImageInt p, A arg);
	R visit(Implication p, A arg);
	R visit(ImplicationAfter p, A arg);
	R visit(ImplicationBefore p, A arg);
	R visit(Inheritance p, A arg);
	R visit(Instance p, A arg);
	R visit(InstanceProperty p, A arg);
	R visit(IntersectionExt p, A arg);
	R visit(IntersectionInt p, A arg);
	R visit(Negation p, A arg);
	R visit(NumericLiteral p, A arg);
	R visit(Product p, A arg);
	R visit(Property p, A arg);
	R visit(SetExt p, A arg);
	R visit(SetInt p, A arg);
	R visit(Similarity p, A arg);
	R visit(TenseFuture p, A arg);
	R visit(TensePast p, A arg);
	R visit(TensePresent p, A arg);
	R visit(StringLiteral p, A arg);
	R visit(URIRef p, A arg);
	R visit(Variable p, A arg);
	R visit(Term p, A arg);
	
}
