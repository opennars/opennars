/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DefaultFunctionInterpreter.java". Description:
"Default implementation of FunctionInterpreter"

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
import ca.nengo.math.FunctionInterpreter;

import java.io.Serializable;
import java.util.*;

/**
 * <p>Default implementation of FunctionInterpreter. This implementation produces
 * PostfixFunctions.</p>
 *
 * TODO: faster Functions could be produced by compiling expressions into Java classes.
 *
 * @author Bryan Tripp
 */
public class DefaultFunctionInterpreter implements FunctionInterpreter {

	private static DefaultFunctionInterpreter ourInstance;

	private final Map<String, Function> myFunctions;
	private final Map<String, AbstractOperator> myOperators;
	private final String myTokens;

	/**
	 * @return A singleton instance of DefaultFunctionInterpreter
	 */
	public static synchronized DefaultFunctionInterpreter sharedInstance() {
		if (ourInstance == null) {
			ourInstance = new DefaultFunctionInterpreter();
		}
		return ourInstance;
	}

	/**
	 * Initializes data structures
	 *
	 * TODO: Make this a static list or something, why are we doing this for each function
	 */
	public DefaultFunctionInterpreter() {
		myFunctions = new HashMap<String, Function>(20);
		myFunctions.put("sin", new SimpleFunctions.Sin());
		myFunctions.put("cos", new SimpleFunctions.Cos());
		myFunctions.put("tan", new SimpleFunctions.Tan());
		myFunctions.put("asin", new SimpleFunctions.Asin());
		myFunctions.put("acos", new SimpleFunctions.Acos());
		myFunctions.put("atan", new SimpleFunctions.Atan());
        myFunctions.put("InverseNormal", new SimpleFunctions.InverseNormal());
        myFunctions.put("Normal", new SimpleFunctions.Normal());

		myFunctions.put("fold", new SimpleFunctions.Fold());
		myFunctions.put("exp", new SimpleFunctions.Exp());
		myFunctions.put("log2", new SimpleFunctions.Log2());
		myFunctions.put("log10", new SimpleFunctions.Log10());
		myFunctions.put("ln", new SimpleFunctions.Ln());
		myFunctions.put("pow", new SimpleFunctions.Pow());
		myFunctions.put("sqrt", new SimpleFunctions.Sqrt());
		myFunctions.put("max", new SimpleFunctions.Max());
		myFunctions.put("min", new SimpleFunctions.Min());

		myOperators = new HashMap<String, AbstractOperator>(20);
		myOperators.put("^", new ExponentOperator());
		myOperators.put("*", new MultiplicationOperator());
		myOperators.put("/", new DivisionOperator());
		myOperators.put("%", new ModuloOperator());
		myOperators.put("+", new AdditionOperator());
		myOperators.put("-", new SubtractionOperator());
		myOperators.put("~", new NegativeOperator()); //we substitute - for ~ based on context
		myOperators.put("!", new NotOperator());
		myOperators.put("<", new LessThanOperator());
		myOperators.put(">", new GreaterThanOperator());
		myOperators.put("&", new AndOperator());
		myOperators.put("|", new OrOperator());

		StringBuilder buf = new StringBuilder();
		Iterator<String> it = myOperators.keySet().iterator();
		while (it.hasNext()) {
			buf.append(it.next());
		}
		myTokens = buf + "(), ";
	}

	/**
	 * @see ca.nengo.math.FunctionInterpreter#registerFunction(java.lang.String, ca.nengo.math.Function)
	 */
	public void registerFunction(String name, Function function) {
		if (name.matches(".*\\s.*")) {
			throw new IllegalArgumentException("Function name '" + name + "' is invalid (can not contain whitespace)");
		}

		if (myFunctions.containsKey(name)) {
			throw new IllegalArgumentException("There is already a function named " + name);
		}

		myFunctions.put(name, function);
	}

	/**
	 * @see ca.nengo.math.FunctionInterpreter#parse(java.lang.String, int)
	 */
	public Function parse(String expression, int dimension) {
		List<Serializable> postfix = getPostfixList(expression);
		return new PostfixFunction(postfix, expression, dimension);
	}

	/**
	 * @param expression Mathematical expression, as in parse(...)
	 * @return List of operators and operands in postfix order
	 */
	public List<Serializable> getPostfixList(String expression) {
		//Dijkstra's shunting yard algorithm to convert infix to postfix
		// see http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm
		// see also http://en.wikipedia.org/wiki/Reverse_Polish_notation

		StringTokenizer tok = new StringTokenizer(expression, myTokens, true);
		Stack<Serializable> stack = new Stack<Serializable>();
		List<Serializable> result = new ArrayList<Serializable>(100); //postfix operand & operate list

		boolean negativeUnary = true; //contextual flag to indicate that "-" should be treated as unary

		while (tok.hasMoreTokens()) {
			String token = tok.nextToken().trim();
			if (token.equals("-") && negativeUnary) {
                token = "~";
            }

			if (token.length() > 0) {
				if (token.equals("(")) {
					stack.push(token);
					negativeUnary = true;
				} else if (token.equals(")")) {
					Object o;
					while ( !(o = stack.pop()).equals("(") ) { //TODO: error if empty
						assert o instanceof AbstractOperator;
						result.add((AbstractOperator) o);
					}
					if ( !stack.empty() && isFunction(stack.peek()) ) {
						result.add(stack.pop());
					}
					negativeUnary = false;
				} else if (token.matches("x\\d+")) { //input placeholder in form x0, x1, ...
					int index = Integer.parseInt(token.substring(1));
					result.add(new Integer(index));
					negativeUnary = false;
				} else if (token.matches("\\d*?\\.?\\d+")) { //literal floating point number
					result.add(new Float(Float.parseFloat(token)));
					negativeUnary = false;
				} else if (token.equalsIgnoreCase("pi")) {
					result.add(new Float(Math.PI));
					negativeUnary = false;
				} else if (token.equals(",")) {
					while ( !stack.peek().equals("(") ) { //TODO: error if empty (separator misplaces or parentheses mismatched)
						result.add(stack.pop());
					}
					negativeUnary = true;
				} else if (myFunctions.get(token) != null) {
					stack.push(myFunctions.get(token));
					negativeUnary = false;
				} else if (myOperators.get(token) != null) {
					AbstractOperator op = myOperators.get(token);

					oploop: while ( !stack.isEmpty() && isOperator(stack.peek()) ) {
						AbstractOperator op2 = (AbstractOperator) stack.peek();
						if (op.getPrecedence() > op2.getPrecedence()
								|| (op.isRightAssociative() && op.getPrecedence() == op2.getPrecedence())) {
							break oploop;
						}

						result.add(stack.pop());
					}

					stack.push(op);

					negativeUnary = true;
				} else {
					throw new RuntimeException("The function '" + token + "' is not recognized");
				}
			}
		}

		while ( !stack.empty() ) {
			result.add(stack.pop());
		}

		return result;
	}


	//true if Function but not AbstractOperator
	private static boolean isFunction(Object o) {
		return (o instanceof Function) && !(o instanceof AbstractOperator);
	}

	//true if AbstractOperator
	private static boolean isOperator(Object o) {
		return (o instanceof AbstractOperator);
	}


	/************ PRIVATE OPERATOR CLASSES *********************************/

	private abstract static class AbstractOperator implements Function {

		private static final long serialVersionUID = 1L;

		private final int myDimension;
		private final boolean myRightAssociative;
		private final int myPrecendence;

		/**
		 * @param dimension Dimension of the space that the Function maps from
		 * @param rightAssociative Evaluated from the right (eg exponent operate)
		 * @param precedence A code indicating operate precedence relative to other operators
		 */
		public AbstractOperator(int dimension, boolean rightAssociative, int precedence) {
			myDimension = dimension;
			myRightAssociative = rightAssociative;
			myPrecendence = precedence;
		}

		/**
		 * @see ca.nengo.math.Function#getDimension()
		 */
		public int getDimension() {
			return myDimension;
		}

		/**
		 * @return True if right-associative (evaluated from the right; eg exponent operate)
		 */
		public boolean isRightAssociative() {
			return myRightAssociative;
		}

		/**
		 * @return A code indicating operate precedence relative to other operators
		 */
		public int getPrecedence() {
			return myPrecendence;
		}

		/**
		 * @see ca.nengo.math.Function#multiMap(float[][])
		 */
		public float[] multiMap(float[][] from) {
			float[] result = new float[from.length];

			for (int i = 0; i < result.length; i++) {
				result[i] = this.map(from[i]);
			}

			return result;
		}

		@Override
		public AbstractOperator clone() throws CloneNotSupportedException {
			return (AbstractOperator) super.clone();
		}

	}

	private static class ExponentOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public ExponentOperator() {
			super(2, true, 4);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return (float) Math.pow(from[0], from[1]);
		}

		public String toString() {
			return "^";
		}
	}

	private static class MultiplicationOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public MultiplicationOperator() {
			super(2, false, 3);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] * from[1];
		}

		public String toString() {
			return "*";
		}
	}

	private static class DivisionOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public DivisionOperator() {
			super(2, false, 3);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] / from[1];
		}

		public String toString() {
			return "/";
		}
	}
	
	private static class ModuloOperator extends AbstractOperator {
		
		private static final long serialVersionUID = 1L;

		public ModuloOperator() {
			super(2, false, 3);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] % from[1];
		}

		public String toString() {
			return "%";
		}
	}

	private static class AdditionOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public AdditionOperator() {
			super(2, false, 2);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] + from[1];
		}

		public String toString() {
			return "+";
		}
	}

	private static class SubtractionOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public SubtractionOperator() {
			super(2, false, 2);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] - from[1];
		}

		public String toString() {
			return "-";
		}
	}

	private static class NegativeOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public NegativeOperator() {
			super(1, false, 5);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return -from[0];
		}

		public String toString() {
			return "~";
		}
	}

	private static class NotOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public NotOperator() {
			super(1, false, 5);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return (from[0] > .5) ? 0f : 1f;
		}

		public String toString() {
			return "!";
		}
	}

	private static class LessThanOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public LessThanOperator() {
			super(2, false, 1);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] < from[1] ? 1f : 0f;
		}

		public String toString() {
			return "<";
		}
	}

	private static class GreaterThanOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public GreaterThanOperator() {
			super(2, false, 1);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return from[0] > from[1] ? 1f : 0f;
		}

		public String toString() {
			return ">";
		}
	}

	private static class AndOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public AndOperator() {
			super(2, false, 0);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return (from[0] > .5 && from[1] > .5) ? 1f : 0f;
		}

		public String toString() {
			return "&";
		}
	}

	private static class OrOperator extends AbstractOperator {

		private static final long serialVersionUID = 1L;

		public OrOperator() {
			super(2, false, 0);
		}

		public float map(float[] from) {
			assert from.length == getDimension();
			return (from[0] > .5 || from[1] > .5) ? 1f : 0f;
		}

		public String toString() {
			return "|";
		}
	}

	public Map<String, Function> getRegisteredFunctions() {
		return myFunctions;
	}

	public void removeRegisteredFunction(String name) {
		myFunctions.remove(name);

	}

}
