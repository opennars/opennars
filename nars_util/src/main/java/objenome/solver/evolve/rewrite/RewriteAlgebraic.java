package objenome.solver.evolve.rewrite;

///*
// * Encog(tm) Core v3.3 - Java Version
// * http://www.heatonresearch.com/encog/
// * https://github.com/encog/encog-java-core
// 
// * Copyright 2008-2014 Heaton Research, Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *   
// * For more information on Heaton Research copyrights, licenses 
// * and trademarks visit:
// * http://www.heatonresearch.com/copyright
// */
//package org.encog.ml.prg.train.rewrite;
//
//import objenome.gene.gp.STGPIndividual;
//import objenome.gene.gp.epox.Node;
//import org.encog.ml.ea.rules.RewriteRule;
//
///**
// * This class is used to rewrite algebraic expressions into more simple forms.
// * This is by no means a complete set of rewrite rules, and will likely be
// * extended in the future.
// */
//public class RewriteAlgebraic implements RewriteRule {
//
//	/**
//	 * Has the expression been rewritten.
//	 */
//	private boolean rewritten;
//
//	/**
//	 * Create an floating point numeric constant.
//	 * @param prg The program to create the constant for.
//	 * @param v The value that the constant represents.
//	 * @return The newly created node.
//	 */
//	private Node createNumericConst(final STGPIndividual prg,
//			final double v) {
//		final Node result = prg.getFunctions().factorNode("#const",
//				prg, new Node[] {});
//		result.getData()[0] = new ExpressionValue(v);
//		return result;
//	}
//
//	/**
//	 * Create an integer numeric constant.
//	 * @param prg The program to create the constant for.
//	 * @param v The value that the constant represents.
//	 * @return The newly created node.
//	 */
//	private Node createNumericConst(final STGPIndividual prg, final int v) {
//		final Node result = prg.getFunctions().factorNode("#const",
//				prg, new Node[] {});
//		result.getData()[0] = new ExpressionValue(v);
//		return result;
//	}
//
//	/**
//	 * Attempt to rewrite the specified node.
//	 * @param parent The parent node to start from.
//	 * @return The rewritten node, or the same node if no rewrite occurs.
//	 */
//	private Node internalRewrite(final Node parent) {
//		Node rewrittenParent = parent;
//
//		rewrittenParent = tryDoubleNegative(rewrittenParent);
//		rewrittenParent = tryMinusMinus(rewrittenParent);
//		rewrittenParent = tryPlusNeg(rewrittenParent);
//		rewrittenParent = tryVarOpVar(rewrittenParent);
//		rewrittenParent = tryPowerZero(rewrittenParent);
//		rewrittenParent = tryOnePower(rewrittenParent);
//		rewrittenParent = tryZeroPlus(rewrittenParent);
//		rewrittenParent = tryZeroDiv(rewrittenParent);
//		rewrittenParent = tryZeroMul(rewrittenParent);
//		rewrittenParent = tryMinusZero(rewrittenParent);
//
//		// try children
//		for (int i = 0; i < rewrittenParent.getChildNodes().size(); i++) {
//			final Node childNode = (Node) rewrittenParent
//					.getChildNodes().get(i);
//			final Node rewriteChild = internalRewrite(childNode);
//			if (childNode != rewriteChild) {
//				rewrittenParent.getChildNodes().remove(i);
//				rewrittenParent.getChildNodes().add(i, rewriteChild);
//				this.rewritten = true;
//			}
//		}
//
//		return rewrittenParent;
//	}
//
//	/**
//	 * Determine if the specified node is constant.
//	 * @param node The node to check.
//	 * @param v The constant to compare against.
//	 * @return True if the specified node matches the specified constant.
//	 */
//	private boolean isConstValue(final Node node, final double v) {
//		if (node.getTemplate() == StandardExtensions.EXTENSION_CONST_SUPPORT) {
//			if (Math.abs(node.getData()[0].toFloatValue() - v) < Encog.DEFAULT_DOUBLE_EQUAL) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public boolean rewrite(final STGPIndividual program) {
//		this.rewritten = false;
//		
//		final Node node = program.getRoot();
//		final Node rewrittenRoot = internalRewrite(node);
//		if (rewrittenRoot != null) {
//			program.setRoot(rewrittenRoot);
//		}
//		return this.rewritten;
//	}
//
//	/**
//	 * Try to rewrite --x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryDoubleNegative(final Node parent) {
//		if (parent.getIdentifier().equals("-")) {
//			final Node child = parent.getChildren()[0];
//			if (child.getIdentifier().equals("-")) {
//				final Node grandChild = child.getChildren()[0];
//				this.rewritten = true;
//				return grandChild;
//			}
//		}
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite --x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryMinusMinus(Node parent) {
//		if (parent.getIdentifier().equals("-") && parent.getChildNodes().size() == 2) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (child2.getIdentifier().equals("#const")) {
//				final ExpressionValue v = child2.getData()[0];
//				if (v.isFloat()) {
//					final double v2 = v.toFloatValue();
//					if (v2 < 0) {
//						child2.getData()[0] = new ExpressionValue(-v2);
//						parent = parent
//								.getOwner()
//								.getContext()
//								.getFunctions()
//								.factorNode("+", parent.getOwner(),
//										new Node[] { child1, child2 });
//					}
//				} else if (v.isInt()) {
//					final long v2 = v.toIntValue();
//					if (v2 < 0) {
//						child2.getData()[0] = new ExpressionValue(-v2);
//						parent = parent
//								.getOwner()
//								.getContext()
//								.getFunctions()
//								.factorNode("+", parent.getOwner(),
//										new Node[] { child1, child2 });
//					}
//				}
//			}
//		}
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite x-0.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryMinusZero(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_SUB) {
//			final Node child2 = parent.getChildNode(1);
//			if (isConstValue(child2, 0)) {
//				final Node child1 = parent.getChildren()[0];
//				return child1;
//			}
//		}
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite x^1.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryOnePower(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_POWER
//				|| parent.getTemplate() == StandardExtensions.EXTENSION_POWFN) {
//			final Node child = parent.getChildren()[0];
//			if (child.getTemplate() == StandardExtensions.EXTENSION_CONST_SUPPORT) {
//				if (Math.abs(child.getData()[0].toFloatValue() - 1) < Encog.DEFAULT_DOUBLE_EQUAL) {
//					this.rewritten = true;
//					return createNumericConst(parent.getOwner(), 1);
//				}
//			}
//		}
//
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite x+-c.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryPlusNeg(Node parent) {
//		if (parent.getIdentifier().equals("+") && parent.getChildNodes().size() == 2) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (child2.getIdentifier().equals("-")
//					&& child2.getChildNodes().size() == 1) {
//				parent = parent
//						.getOwner()
//						.getContext()
//						.getFunctions()
//						.factorNode(
//								"-",
//								parent.getOwner(),
//								new Node[] { child1,
//										child2.getChildren()[0] });
//			} else if (child2.getIdentifier().equals("#const")) {
//				final ExpressionValue v = child2.getData()[0];
//				if (v.isFloat()) {
//					final double v2 = v.toFloatValue();
//					if (v2 < 0) {
//						child2.getData()[0] = new ExpressionValue(-v2);
//						parent = parent
//								.getOwner()
//								.getContext()
//								.getFunctions()
//								.factorNode("-", parent.getOwner(),
//										new Node[] { child1, child2 });
//					}
//				} else if (v.isInt()) {
//					final long v2 = v.toIntValue();
//					if (v2 < 0) {
//						child2.getData()[0] = new ExpressionValue(-v2);
//						parent = parent
//								.getOwner()
//								.getContext()
//								.getFunctions()
//								.factorNode("-", parent.getOwner(),
//										new Node[] { child1, child2 });
//					}
//				}
//			}
//		}
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite x^0.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryPowerZero(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_POWER
//				|| parent.getTemplate() == StandardExtensions.EXTENSION_POWFN) {
//			final Node child0 = parent.getChildren()[0];
//			final Node child1 = parent.getChildNode(1);
//			if (isConstValue(child1, 0)) {
//				return createNumericConst(parent.getOwner(), 1);
//			}
//			if (isConstValue(child0, 0)) {
//				return createNumericConst(parent.getOwner(), 0);
//			}
//		}
//
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite x+x, x-x, x*x, x/x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryVarOpVar(Node parent) {
//		if (parent.getChildNodes().size() == 2
//				&& parent.getIdentifier().length() == 1
//				&& "+-*/".indexOf(parent.getIdentifier().charAt(0)) != -1) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (child1.getIdentifier().equals("#var")
//					&& child2.getIdentifier().equals("#var")) {
//				if (child1.getData()[0].toIntValue() == child2.getData()[0]
//						.toIntValue()) {
//					switch (parent.getIdentifier().charAt(0)) {
//					case '-':
//						parent = createNumericConst(parent.getOwner(), 0);
//						break;
//					case '+':
//						parent = parent
//								.getOwner()
//								.getFunctions()
//								.factorNode(
//										"*",
//										parent.getOwner(),
//										new Node[] {
//												createNumericConst(
//														parent.getOwner(), 2),
//												child1 });
//						break;
//					case '*':
//						parent = parent
//								.getOwner()
//								.getFunctions()
//								.factorNode(
//										"^",
//										parent.getOwner(),
//										new Node[] {
//												child1,
//												createNumericConst(
//														parent.getOwner(), 2) });
//						break;
//					case '/':
//						parent = createNumericConst(parent.getOwner(), 1);
//						break;
//					}
//				}
//			}
//		}
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite 0/x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryZeroDiv(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_DIV) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (!isConstValue(child2, 0)) {
//				if (isConstValue(child1, 0)) {
//					this.rewritten = true;
//					return this.createNumericConst(parent.getOwner(), 0);
//				}
//			}
//		}
//
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite 0*x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryZeroMul(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_MUL) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (isConstValue(child1, 0) || isConstValue(child2, 0)) {
//				this.rewritten = true;
//				return this.createNumericConst(parent.getOwner(), 0);
//			}
//		}
//
//		return parent;
//	}
//
//	/**
//	 * Try to rewrite 0+x.
//	 * @param parent The parent node to attempt to rewrite.
//	 * @return The rewritten node, if it was rewritten.
//	 */
//	private Node tryZeroPlus(final Node parent) {
//		if (parent.getTemplate() == StandardExtensions.EXTENSION_ADD) {
//			final Node child1 = parent.getChildren()[0];
//			final Node child2 = parent.getChildNode(1);
//
//			if (isConstValue(child1, 0)) {
//				this.rewritten = true;
//				return child2;
//			}
//
//			if (isConstValue(child2, 0)) {
//				this.rewritten = true;
//				return child1;
//			}
//
//		}
//
//		return parent;
//	}
//
//}
