/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PostfixFunction.java". Description:
"A Function based on a mathematical expression and on other functions"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 8-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * <p>A Function based on a mathematical expression and on other functions. The expression
 * must be given as a list of list of operators, literal operands, and operand placeholders.</p>
 *
 * <p>An operate can be any Function. A literal operand must be a Float.</p>
 *
 * <p>An operand placeholder is an Integer, which points to the dimension of the input vector
 * from which the corresponding operand is to be drawn. For example the expression for a
 * PostfixFunction with 2 dimensions can include the Integers 0 and 1. When map(float[] from)
 * is called, Integer 0 will be replaced with from[0] and so on.</p>
 *
 * <p>The expression list must be given in postfix order.</p>
 *
 * TODO: need a way to manage user-defined functions that ensures they can be accessed from saved networks
 *
 * @author Bryan Tripp
 */
public class PostfixFunction implements Function {

	private static final long serialVersionUID = 1L;
	private static final Logger ourLogger = LogManager.getLogger(PostfixFunction.class);

	private List<Serializable> myExpressionList;

	/**
	 * A human-readable string representation of the function
	 */
	private String myExpression;
	private int myDimension;

	/**
	 * @param expressionList Postfix expression list (as described in class docs)
	 * @param expression String representation of the expression
	 * @param dimension Dimension of the function (must be at least as great as any operand
	 * 		placeholders that appear in the expression)
	 */
	public PostfixFunction(List<Serializable> expressionList, String expression, int dimension) {
		set(expressionList, expression, dimension);
	}

	/**
	 * @param expression String representation of the expression (infix)
	 * @param dimension Dimension of the function (must be at least as great as any operand
	 * 		placeholders that appear in the expression)
	 */
	public PostfixFunction(String expression, int dimension) {
		set(null, expression, dimension);
	}

	private void set(List<Serializable> expressionList, String expression, int dimension) {
		if (expressionList == null) {
			expressionList = DefaultFunctionInterpreter.sharedInstance().getPostfixList(expression);
		} else {
			//TODO: register user-defined functions?
		}

		int highest = findHighestDimension(expressionList);
		if (dimension <= highest) {
			dimension = highest+1;
			ourLogger.warn("Dimension adjusted to " + (highest+1) + " to satisfy expression " + expression);
		}

		myDimension = dimension;
		myExpressionList = expressionList;
		myExpression = expression;
	}

	/**
	 * @return Postfix expression list
	 */
	protected List<Serializable> getExpressionList() {
		return myExpressionList;
	}

	/**
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return myDimension;
	}

	/**
	 * @param dimension Dimension of the function (must be at least as great as any operand
	 * 		placeholders that appear in the expression)
	 */
	public void setDimension(int dimension) {
		set(myExpressionList, myExpression, dimension);
	}

	/**
	 * @return A human-readable string representation of the function
	 */
	public String getExpression() {
		if (myExpression != null && myExpression.compareTo("") != 0) {
			return myExpression;
		} else {
			return "Postfix: " + myExpressionList.toString();
		}
	}

	/**
	 * @param expression String representation of the expression (infix)
	 */
	public void setExpression(String expression) {
		set(null, expression, myDimension);
	}

	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		return doMap(myExpressionList, myDimension, from);
	}

	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];

		for (int i = 0; i < from.length; i++) {
			result[i] = doMap(myExpressionList, myDimension, from[i]);
		}

		return result;
	}

	private static float doMap(List<Serializable> expression, int dimension, float[] from) {
		if (dimension != from.length) {
			throw new IllegalArgumentException("Input dimension " + from.length + ", expected " + dimension);
		}

		float result = 0;
		int i = 0;

		try {
			Stack<Object> stack = new Stack<Object>();

			for ( ; i < expression.size(); i++) {
				Object o = expression.get(i);

				if (o instanceof Float) {
					stack.push(o);
				} else if (o instanceof Integer) {
					int index = ((Integer) o).intValue();
					stack.push(new Float(from[index]));
				} else {
					Function f = (Function) o;

					float[] args = new float[f.getDimension()];
					for (int dim = args.length-1; dim >= 0; dim--) {
						args[dim] = ((Float) stack.pop()).floatValue();
					}

					stack.push(new Float(f.map(args)));
				}
			}

			result = ((Float) stack.pop()).floatValue();

		} catch (Exception e) {
			throw new RuntimeException("Unable to evaluate expression list at index " + i, e);
		}

		return result;
	}

	//and check everything is a Float, Integer, or Function while we're at it
	private static int findHighestDimension(List<Serializable> expression) {
		int highest = -1;

		for (int i = 0; i < expression.size(); i++) {
			Object o = expression.get(i);

			if (o instanceof Integer) {
				int current = ((Integer) o).intValue();
				if (current > highest) {
					highest = current;
				}
			} else if ( !(o instanceof Float) && !(o instanceof Function) ) {
				throw new IllegalArgumentException("Expression must consist of Integers, Floats, and Functions");
			}
		}

		return highest;
	}

	@Override
	public Function clone() throws CloneNotSupportedException {
		PostfixFunction result = (PostfixFunction) super.clone();

		List<Serializable> list = new ArrayList<Serializable>(this.myExpressionList.size());
		Iterator<Serializable> it = myExpressionList.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof Float) {
				list.add(new Float(((Float) o).floatValue()));
			} else if (o instanceof Integer) {
				list.add(Integer.valueOf(((Integer) o).intValue()));
			} else if (o instanceof Function) {
				list.add(((Function) o).clone());
			} else {
				throw new RuntimeException("Expression list contains unexpected type " + o.getClass().getName());
			}
		}
		result.myExpressionList = list;

		return result;
	}

}
