/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.prolog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;
import nars.prolog.interfaces.IParser;

/**
 * This class defines a parser of prolog terms and sentences.
 * <p/>
 * BNF part 2: Parser
 * term ::= exprA(1200)
 * exprA(n) ::= exprB(n) { op(yfx,n) exprA(n-1) |
 *                         op(yf,n) }*
 * exprB(n) ::= exprC(n-1) { op(xfx,n) exprA(n-1) |
 *                           op(xfy,n) exprA(n) |
 *                           op(xf,n) }*
 * // exprC is called parseLeftSide in the code
 * exprC(n) ::= '-' integer | '-' float |
 *              op( fx,n ) exprA(n-1) |
 *              op( fy,n ) exprA(n) |
 *              exprA(n)
 * exprA(0) ::= integer |
 *              float |
 *              atom |
 *              variable |
 *              atom'(' exprA(1200) { ',' exprA(1200) }* ')' |
 *              '[' [ exprA(1200) { ',' exprA(1200) }* [ '|' exprA(1200) ] ] ']' |
 *              '(' { exprA(1200) }* ')'
 *              '{' { exprA(1200) }* '}'
 * op(type,n) ::= atom | { symbol }+
 */
@SuppressWarnings("serial")
public class Parser implements /*Castagna 06/2011*/IParser,/**/ Serializable, Iterable<Term>
{
	private static class IdentifiedTerm {
		private int priority;
		private Term result;
		public IdentifiedTerm(int priority, Term result) {
			this.priority = priority;
			this.result = result;
		}
	}

	private static OperatorManager defaultOperatorManager = new DefaultOperatorManager();

	private Tokenizer tokenizer;
	private OperatorManager opManager = defaultOperatorManager;
	/*Castagna 06/2011*/
	private HashMap<Term, Integer> offsetsMap;		 
	private int tokenStart;
	/**/    

	/**
	 * creating a Parser specifing how to handle operators
	 * and what text to parse
	 */
	public Parser(OperatorManager op, InputStream theoryText) {
		this(theoryText);
		if (op != null)
			opManager = op;
	}
	
	/*Castagna 06/2011*/    
	/**
	 * creating a Parser specifing how to handle operators
	 * and what text to parse
	 */	
	public Parser(OperatorManager op, String theoryText, HashMap<Term, Integer> mapping) {		 
		this(theoryText, mapping);		 
		if (op != null)		 
			opManager = op;		 
	}
	/**/ 

	/**
	 * creating a Parser specifing how to handle operators
	 * and what text to parse
	 */
	public Parser(OperatorManager op, String theoryText) {
		this(theoryText);
		if (op != null)
			opManager = op;
	}
	
	/*Castagna 06/2011*/
	/**
	 * creating a parser with default operator interpretation
	 */	
	public Parser(String theoryText, HashMap<Term, Integer> mapping) {		 
		tokenizer = new Tokenizer(theoryText);		 
		offsetsMap = mapping;		 
	}
	/**/

	/**
	 * creating a parser with default operator interpretation
	 */
	public Parser(String theoryText) {
		tokenizer = new Tokenizer(theoryText);
		/*Castagna 06/2011*/
		offsetsMap = null;
		/**/        
	}

	/**
	 * creating a parser with default operator interpretation
	 */
	public Parser(InputStream theoryText) {
		tokenizer = new Tokenizer(new BufferedReader(new InputStreamReader(theoryText)));
	}

	//  user interface

	public Iterator<Term> iterator() {
		return new TermIterator(this);
	}

	/**
	 * Parses next term from the stream built on string.
	 * @param endNeeded <tt>true</tt> if it is required to parse the end token
	 * (a period), <tt>false</tt> otherwise.
	 * @throws InvalidTermException if a syntax error is found. 
	 */
	public Term nextTerm(boolean endNeeded) throws InvalidTermException {
		try {
			Token t = tokenizer.readToken();
			if (t.isEOF())
				return null;

			tokenizer.unreadToken(t);
			Term term = expr(false);
			if (term == null)
				/*Castagna 06/2011*/
	            //throw new InvalidTermException("The parser is unable to finish");
	            throw new InvalidTermException("The parser is unable to finish.",
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
				/**/

			if (endNeeded && tokenizer.readToken().getType() != Tokenizer.END)
				/*Castagna 06/2011*/
	            //throw new InvalidTermException("The term " + term + " is not ended with a period.");
				throw new InvalidTermException("The term '" + term + "' is not ended with a period.",
						tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            		tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
				/**/

			term.resolveTerm();
			return term;
		} catch (IOException ex) {
			/*Castagna 06/2011*/
            //throw new InvalidTermException("An I/O error occured.");
			throw new InvalidTermException("An I/O error occured.",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
		            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);					
			/**/
		}
	}

	/**
	 * Static service to get a term from its string representation
	 */
	public static Term parseSingleTerm(String st) throws InvalidTermException {
		return parseSingleTerm(st, null);
	}

	/**
	 * Static service to get a term from its string representation,
	 * providing a specific operator manager
	 */
	public static Term parseSingleTerm(String st, OperatorManager op) throws InvalidTermException {
		try {
			Parser p = new Parser(op, st);
			Token t = p.tokenizer.readToken();
			if (t.isEOF())
	            throw new InvalidTermException("Term starts with EOF");

			p.tokenizer.unreadToken(t);
			Term term = p.expr(false);
			if (term == null)
				throw new InvalidTermException("Term is null");
			if (!p.tokenizer.readToken().isEOF())
				throw new InvalidTermException("The entire string could not be read as one term");
			term.resolveTerm();
			return term;
		} catch (IOException ex) {
			throw new InvalidTermException("An I/O error occured");
		}
	}

	// internal parsing procedures

	private Term expr(boolean commaIsEndMarker) throws InvalidTermException, IOException {
		return exprA(OperatorManager.OP_HIGH, commaIsEndMarker).result;
	}

	private IdentifiedTerm exprA(int maxPriority, boolean commaIsEndMarker) throws InvalidTermException, IOException {

		IdentifiedTerm leftSide = exprB(maxPriority, commaIsEndMarker);
		//if (leftSide == null)
			//return null;

		//{op(yfx,n) exprA(n-1) | op(yf,n)}*
		Token t = tokenizer.readToken();
		for (; t.isOperator(commaIsEndMarker); t = tokenizer.readToken()) {

			int YFX = opManager.opPrio(t.seq, "yfx");
			int YF = opManager.opPrio(t.seq, "yf");

			//YF and YFX has a higher priority than the left side expr and less then top limit
			// if (YF < leftSide.priority && YF > OperatorManager.OP_HIGH) YF = -1;
			if (YF < leftSide.priority || YF > maxPriority) YF = -1;
			// if (YFX < leftSide.priority && YFX > OperatorManager.OP_HIGH) YFX = -1;
			if (YFX < leftSide.priority || YFX > maxPriority) YFX = -1;

			//YFX has priority over YF
			if (YFX >= YF && YFX >= OperatorManager.OP_LOW){
				IdentifiedTerm ta = exprA(YFX-1, commaIsEndMarker);
				if (ta != null) {
					/*Castagna 06/2011*/
					//leftSide = new IdentifiedTerm(YFX, new Struct(t.seq, leftSide.result, ta.result));
					 leftSide = identifyTerm(YFX, new Struct(t.seq, leftSide.result, ta.result), tokenStart);
					/**/
					continue;
				}
			}
			//either YF has priority over YFX or YFX failed
			if (YF >= OperatorManager.OP_LOW) {
				/*Castagna 06/2011*/
				//leftSide = new IdentifiedTerm(YF, new Struct(t.seq, leftSide.result));
				 leftSide = identifyTerm(YF, new Struct(t.seq, leftSide.result), tokenStart);
				/**/
				continue;
			}
			break;
		}
		tokenizer.unreadToken(t);
		return leftSide;
	}

	private IdentifiedTerm exprB(int maxPriority, boolean commaIsEndMarker) throws InvalidTermException, IOException {

		//1. op(fx,n) exprA(n-1) | op(fy,n) exprA(n) | expr0
		IdentifiedTerm left = parseLeftSide(commaIsEndMarker, maxPriority);

		//2.left is followed by either xfx, xfy or xf operators, parse these
		Token operator = tokenizer.readToken();
		for (; operator.isOperator(commaIsEndMarker); operator = tokenizer.readToken()) {
			int XFX = opManager.opPrio(operator.seq, "xfx");
			int XFY = opManager.opPrio(operator.seq, "xfy");
			int XF = opManager.opPrio(operator.seq, "xf");

			//check that no operator has a priority higher than permitted
			//or a lower priority than the left side expression
			if (XFX > maxPriority || XFX < OperatorManager.OP_LOW) XFX = -1;
			if (XFY > maxPriority || XFY < OperatorManager.OP_LOW) XFY = -1;
			if (XF > maxPriority || XF < OperatorManager.OP_LOW) XF = -1;

			//XFX
			boolean haveAttemptedXFX = false;
			if (XFX >= XFY && XFX >= XF && XFX >= left.priority) {     //XFX has priority
				IdentifiedTerm found = exprA(XFX - 1, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					//Struct xfx = new Struct(operator.seq, left.result, found.result);
					//left = new IdentifiedTerm(XFX, xfx);
					left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				} else
					haveAttemptedXFX = true;
			}
			//XFY
			if (XFY >= XF && XFY >= left.priority){           //XFY has priority, or XFX has failed
				IdentifiedTerm found = exprA(XFY, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					//Struct xfy = new Struct(operator.seq, left.result, found.result);
					//left = new IdentifiedTerm(XFY, xfy);
					left = identifyTerm(XFY, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				}
			}
			//XF
			if (XF >= left.priority)                   //XF has priority, or XFX and/or XFY has failed
				/*Castagna 06/2011*/
				//return new IdentifiedTerm(XF, new Struct(operator.seq, left.result));
				return identifyTerm(XF, new Struct(operator.seq, left.result), tokenStart);
				/**/

			//XFX did not have top priority, but XFY failed
			if (!haveAttemptedXFX && XFX >= left.priority) {
				IdentifiedTerm found = exprA(XFX - 1, commaIsEndMarker);
				if (found != null) {
					/*Castagna 06/2011*/
					//Struct xfx = new Struct(operator.seq, left.result, found.result);
					//left = new IdentifiedTerm(XFX, xfx);
					left = identifyTerm(XFX, new Struct(operator.seq, left.result, found.result), tokenStart);
					/**/
					continue;
				}
			}
			break;
		}
		tokenizer.unreadToken(operator);
		return left;
	}

	/**
	 * Parses and returns a valid 'leftside' of an expression.
	 * If the left side starts with a prefix, it consumes other expressions with a lower priority than itself.
	 * If the left side does not have a prefix it must be an expr0.
	 *
	 * @param commaIsEndMarker used when the leftside is part of and argument list of expressions
	 * @param maxPriority operators with a higher priority than this will effectivly end the expression
	 * @return a wrapper of: 1. term correctly structured and 2. the priority of its root operator
	 * @throws InvalidTermException
	 */
	private IdentifiedTerm parseLeftSide(boolean commaIsEndMarker, int maxPriority) throws InvalidTermException, IOException {
		//1. prefix expression
		Token f = tokenizer.readToken();
		if (f.isOperator(commaIsEndMarker)) {
			int FX = opManager.opPrio(f.seq, "fx");
			int FY = opManager.opPrio(f.seq, "fy");

			if (f.seq.equals("-")) {
				Token t = tokenizer.readToken();
				if (t.isNumber())
					/*Michele Castagna 06/2011*/
					//return new IdentifiedTerm(0, Parser.createNumber("-" + t.seq));
					return identifyTerm(0, Parser.createNumber("-" + t.seq), tokenStart);
					/**/
				else
					tokenizer.unreadToken(t);
			}

			//check that no operator has a priority higher than permitted
			if (FY > maxPriority) FY = -1;
			if (FX > maxPriority) FX = -1;


			//FX has priority over FY
			boolean haveAttemptedFX = false;
			if (FX >= FY && FX >= OperatorManager.OP_LOW){
				IdentifiedTerm found = exprA(FX-1, commaIsEndMarker);    //op(fx, n) exprA(n - 1)
				if (found != null)
					/*Castagna 06/2011*/
					//return new IdentifiedTerm(FX, new Struct(f.seq, found.result));
					return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart);
					/**/
				else
					haveAttemptedFX = true;
			}
			//FY has priority over FX, or FX has failed
			if (FY >= OperatorManager.OP_LOW) {
				IdentifiedTerm found = exprA(FY, commaIsEndMarker); //op(fy,n) exprA(1200)  or   op(fy,n) exprA(n)
				if (found != null)
					/*Castagna 06/2011*/
					//return new IdentifiedTerm(FY, new Struct(f.seq, found.result));
					return identifyTerm(FY, new Struct(f.seq, found.result), tokenStart);
				/**/
			}
			//FY has priority over FX, but FY failed
			if (!haveAttemptedFX && FX >= OperatorManager.OP_LOW) {
				IdentifiedTerm found = exprA(FX-1, commaIsEndMarker);    //op(fx, n) exprA(n - 1)
				if (found != null)
					/*Castagna 06/2011*/
					//return new IdentifiedTerm(FX, new Struct(f.seq, found.result));
					return identifyTerm(FX, new Struct(f.seq, found.result), tokenStart);
					/**/
			}
		}
		tokenizer.unreadToken(f);
		//2. expr0
		return new IdentifiedTerm(0, expr0());
	}

	/**
	 * exprA(0) ::= integer |
	 *              float |
	 *              variable |
	 *              atom |
	 *              atom( exprA(1200) { , exprA(1200) }* ) |
	 *              '[' exprA(1200) { , exprA(1200) }* [ | exprA(1200) ] ']' |
	 *              '{' [ exprA(1200) ] '}' |
	 *              '(' exprA(1200) ')'
	 */
	private Term expr0() throws InvalidTermException, IOException {
		Token t1 = tokenizer.readToken();

		/*Castagna 06/2011*/
		/*
		if (t1.isType(Tokenizer.INTEGER))
			return Parser.parseInteger(t1.seq); //todo moved method to Number

		if (t1.isType(Tokenizer.FLOAT))
			return Parser.parseFloat(t1.seq);   //todo moved method to Number

		if (t1.isType(Tokenizer.VARIABLE))
			return new Var(t1.seq);             //todo switched to use the internal check for "_" in Var(String)
		*/
		
		int tempStart = tokenizer.tokenStart();

        if (t1.isType(Tokenizer.INTEGER)) {
        	Term i = Parser.parseInteger(t1.seq);
        	map(i, tokenizer.tokenStart());
            return i; //todo moved method to Number
        }

        if (t1.isType(Tokenizer.FLOAT)) {
        	Term f = Parser.parseFloat(t1.seq);
        	map(f, tokenizer.tokenStart());
            return f;   //todo moved method to Number
        }

        if (t1.isType(Tokenizer.VARIABLE)) {
        	Term v = new Var(t1.seq);
        	map(v, tokenizer.tokenStart());
            return v;             //todo switched to use the internal check for "_" in Var(String)
        }
		/**/
		
		if (t1.isType(Tokenizer.ATOM) || t1.isType(Tokenizer.SQ_SEQUENCE) || t1.isType(Tokenizer.DQ_SEQUENCE)) {
			if (!t1.isFunctor())
			/*Castagna 06/2011*/
			{
				//return new Struct(t1.seq);
				Term f = new Struct(t1.seq);
        		map(f, tokenizer.tokenStart());
        		return f;
			}
			/**/

			String functor = t1.seq;
			Token t2 = tokenizer.readToken();   //reading left par
			if (!t2.isType(Tokenizer.LPAR))
				throw new InvalidTermException("Something identified as functor misses its first left parenthesis");//todo check can be skipped
			LinkedList<Term> a = expr0_arglist();     //reading arguments
			Token t3 = tokenizer.readToken();
			if (t3.isType(Tokenizer.RPAR))      //reading right par
			/*Castagna 06/2011*/
			{
				//return new Struct(functor, a);
				Term c = new Struct(functor, a);
            	map(c, tempStart);                
            	return c;
			}
			/**/
			/*Castagna 06/2011*/
            //throw new InvalidTermException("Missing right parenthesis: ("+a + " -> here <-");
			throw new InvalidTermException("Missing right parenthesis '("+a + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LPAR)) {
			Term term = expr(false);
			if (tokenizer.readToken().isType(Tokenizer.RPAR))
				return term;
			/*Castagna 06/2011*/
            //throw new InvalidTermException("Missing right parenthesis: ("+term + " -> here <-");
			throw new InvalidTermException("Missing right parenthesis '("+term + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LBRA)) {
			Token t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA))
				return new Struct();

			tokenizer.unreadToken(t2);
			Term term = expr0_list();
			if (tokenizer.readToken().isType(Tokenizer.RBRA))
				return term;
			/*Castagna 06/2011*/
            //throw new InvalidTermException("Missing right bracket: ["+term + " -> here <-");
			throw new InvalidTermException("Missing right bracket '["+term + " ->' here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}

		if (t1.isType(Tokenizer.LBRA2)) {
			Token t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA2))
			/*Castagna 06/2011*/
			{
				//return new Struct("{}");
				Term b = new Struct("{}");
            	map(b, tempStart);
                return b;
			}
			/**/
			tokenizer.unreadToken(t2);
			Term arg = expr(false);
			t2 = tokenizer.readToken();
			if (t2.isType(Tokenizer.RBRA2))
			/*Castagna 06/2011*/
			{
				//return new Struct("{}", arg);
				Term b = new Struct("{}", arg);
				map(b, tempStart);
				return b;
			}
			/*Castagna 06/2011*/
            //throw new InvalidTermException("Missing right braces: {"+arg + " -> here <-");
			throw new InvalidTermException("Missing right braces '{"+arg + "' -> here <-",
					tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            	tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
			/**/
		}
		/*Castagna 06/2011*/
		//throw new InvalidTermException("The following token could not be identified: "+t1.seq);
		throw new InvalidTermException("Unexpected token '" + t1.seq + "'",
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/
	}

	//todo make non-recursive?
	private Term expr0_list() throws InvalidTermException, IOException {
		Term head = expr(true);
		Token t = tokenizer.readToken();
		if (",".equals(t.seq))
			return new Struct(head, expr0_list());
		if ("|".equals(t.seq))
			return new Struct(head, expr(true));
		if ("]".equals(t.seq)) {
			tokenizer.unreadToken(t);
			return new Struct(head, new Struct());
		}
		/*Castagna 06/2011*/
        //throw new InvalidTermException("The expression: " + head + " is not followed by either a ',' or '|'  or ']'.");
		throw new InvalidTermException("The expression '" + head + "' is not followed by either a ',' or '|'  or ']'.",
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/	
	
	}

	//todo make non-recursive
	private LinkedList<Term> expr0_arglist() throws InvalidTermException, IOException {
		Term head = expr(true);
		Token t = tokenizer.readToken();
		if (",".equals(t.seq)) {
			LinkedList<Term> l = expr0_arglist();
			l.addFirst(head);
			return l;
		}
		if (")".equals(t.seq)) {
			tokenizer.unreadToken(t);
			LinkedList<Term> l = new LinkedList<>();
			l.add(head);
			return l;
		}
		/*Castagna 06/2011*/
		//throw new InvalidTermException("The argument: " + head + " is not followed by either a ',' or ')'.\nline: " + tokenizer.lineno());
		/*Castagna 06/2011*/
        //throw new InvalidTermException("The argument: " + head + " is not followed by either a ',' or ')'.");
		throw new InvalidTermException("The argument '" + head + "' is not followed by either a ',' or ')'.",
				tokenizer.offsetToRowColumn(getCurrentOffset())[0],
	            tokenizer.offsetToRowColumn(getCurrentOffset())[1] - 1);
		/**/
	}

	// commodity methods to parse numbers

	static Number parseInteger(String s) {
		long num = java.lang.Long.parseLong(s);
		if (num > Integer.MIN_VALUE && num < Integer.MAX_VALUE)
			return new Int((int) num);
		else
			return new Long(num);
	}

	static Double parseFloat(String s) {
		double num = java.lang.Double.parseDouble(s);
		return new Double(num);
	}

	static Number createNumber(String s){
		try {
			return parseInteger(s);
		} catch (Exception e) {
			return parseFloat(s);
		}
	}

	/*Castagna 06/2011*/
	/*
     * Francesco Fabbri
     * 18/04/2011
     * Mapping terms on text
     */
    
    private IdentifiedTerm identifyTerm(int priority, Term term, int offset) {
    	map(term, offset);
    	return new IdentifiedTerm(priority, term);
    }
    
    private void map(Term term, int offset) {
    	if (offsetsMap != null)
    		offsetsMap.put(term, offset);
    }
    
    public HashMap<Term, Integer> getTextMapping() {
    	return offsetsMap;
    }
    
    /*
     * Francesco Fabbri
     * 19/04/2011
     * Offset / line tracking
     */

    @Override
	/**/
	public int getCurrentLine() {
		return tokenizer.lineno();
	}
    
    /*Castagna 06/2011*/
    @Override
	public int getCurrentOffset() {
		return tokenizer.tokenOffset();
	}
	
	public int[] offsetToRowColumn(int offset) {
    	return tokenizer.offsetToRowColumn(offset);
	}
    /**/

	/**
	 * @return true if the String could be a prolog atom
	 */
	 public static boolean isAtom(final CharSequence s) {
		 return atom.matcher(s).matches();
	 }

	 static private final Pattern atom = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)");

}