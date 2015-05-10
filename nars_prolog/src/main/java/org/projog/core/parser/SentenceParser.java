package org.projog.core.parser;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static org.projog.core.parser.Delimiters.isArgumentSeperator;
import static org.projog.core.parser.Delimiters.isListCloseBracket;
import static org.projog.core.parser.Delimiters.isListOpenBracket;
import static org.projog.core.parser.Delimiters.isListTail;
import static org.projog.core.parser.Delimiters.isPredicateCloseBracket;
import static org.projog.core.parser.Delimiters.isPredicateOpenBracket;
import static org.projog.core.parser.Delimiters.isSentenceTerminator;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.projog.core.Operands;
import org.projog.core.term.Atom;
import org.projog.core.term.DecimalFraction;
import org.projog.core.term.EmptyList;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.ListFactory;
import org.projog.core.term.Structure;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;

/**
 * Parses Prolog syntax representing rules including operators.
 * <p>
 * <b>Note:</b> not thread safe.
 * </p>
 * 
 * @see Operands
 */
public class SentenceParser {
   private static final String MINUS_SIGN = "-";
   private final TokenParser parser;
   private final Operands operands;
   /**
    * A collection of {@code Variable}s this parser currently knows about (key = the variable name).
    * <p>
    * The reason this information needs to be stored is so that each instance of the same variable name, in a single
    * sentence, refers to the same {@code Variable} instance.
    * <p>
    * e.g. During the parsing of the sentence <code>Y is 2, X is Y * 2.</code> two {@code Variable} instances need to be
    * created - one for the variable name <code>X</code> and one that is shared between both references to the variable
    * name <code>Y</code>.
    */
   private final HashMap<String, Variable> variables = new HashMap<>();
   /**
    * Terms created, during the parsing of the current sentence, that were represented using infix notation.
    * <p>
    * Example of infix notation: <code>X = 1</code> where the predicate name <code>=</code> is positioned between its
    * two arguments <code>X</code> and <code>1</code>.
    * <p>
    * The reason these need to be kept a record of is, when the sentence has been fully read, the individual terms can
    * be reordered to conform to operator precedence.
    * <p>
    * e.g. <code>1+2/3</code> will get ordered like: <code>+(1, /(2, 3))</code> while <code>1/2+3</code> will be ordered
    * like: <code>+(/(1, 2), 3)</code>.
    */
   private final Set<PTerm> parsedInfixTerms = new HashSet<>();
   /**
    * Terms created, during the parsing of the current sentence, that were enclosed in brackets.
    * <p>
    * The reason this information needs to be stored is so that the parser knows to <i>not</i> reorder these terms as
    * part of the reordering of infix terms that occurs once the sentence is fully read. i.e. Using brackets to
    * explicitly define the ordering of terms overrules the default operator precedence of infix terms.
    * <p>
    * e.g. Although <code>1/2+3</code> will be ordered like: <code>+(/(1, 2), 3)</code>, <code>1/(2+3)</code> (i.e.
    * where <code>2+3</code> is enclosed in brackets) will be ordered like: <code>/(1, +(2, 3))</code>.
    */
   private final Set<PTerm> bracketedTerms = new HashSet<>();

   /**
    * Returns a new {@code SentenceParser} will parse the specified {@code String} using the specified {@code Operands}.
    * 
    * @param prologSyntax the prolog syntax to be parsed
    * @param operands details of the operands to use during parsing
    * @return a new {@code SentenceParser}
    */
   public static SentenceParser getInstance(String prologSyntax, Operands operands) {
      Reader reader = new StringReader(prologSyntax);
      return getInstance(reader, operands);
   }

   /**
    * Returns a new {@code SentenceParser} that will parse Prolog syntax read from the specified {@code Reader} using
    * the specified {@code Operands}.
    * 
    * @param reader the source of the prolog syntax to be parsed
    * @param operands details of the operands to use during parsing
    * @return a new {@code SentenceParser}
    */
   public static SentenceParser getInstance(Reader reader, Operands operands) {
      BufferedReader br = new BufferedReader(reader);
      return new SentenceParser(br, operands);
   }

   private SentenceParser(Reader reader, Operands operands) {
      this.parser = new TokenParser(reader, operands);
      this.operands = operands;
   }

   /**
    * Creates a {@link PTerm} from Prolog syntax, terminated by a {@code .}, read from this object's
    * {@link CharacterParser}.
    * 
    * @return a {@link PTerm} created from Prolog syntax read from this object's {@link CharacterParser} or {@code null}
    * if the end of the underlying stream being parsed has been reached
    * @throws ParserException if an error parsing the Prolog syntax occurs
    */
   public PTerm parseSentence() {
      final PTerm t = parseTerm();
      if (t == null) {
         return null;
      }

      Token trailingToken = popValue();
      if (!isSentenceTerminator(trailingToken)) {
         throw newParserException("Expected . after: " + t + " but got: " + trailingToken);
      }

      return t;
   }

   /**
    * Creates a {@link PTerm} from Prolog syntax read from this object's {@link CharacterParser}.
    * 
    * @return a {@link PTerm} created from Prolog syntax read from this object's {@link CharacterParser} or {@code null}
    * if the end of the underlying stream being parsed has been reached
    * @throws ParserException if an error parsing the Prolog syntax occurs
    * @see SentenceParser#parseSentence()
    */
   public PTerm parseTerm() {
      if (parser.hasNext()) {
         resetState();
         return getTerm(Integer.MAX_VALUE);
      } else {
         return null;
      }
   }

   private void resetState() {
      parsedInfixTerms.clear();
      variables.clear();
      bracketedTerms.clear();
   }

   /**
    * Returns collection of {@link Variable} instances created by this {@code SentenceParser}.
    * <p>
    * Returns all {@link Variable}s created by this {@code SentenceParser} either since it was created or since the last
    * execution of {@link #parseTerm()}.
    * 
    * @return collection of {@link Variable} instances created by this {@code SentenceParser}
    */
   @SuppressWarnings("unchecked")
   public Map<String, Variable> getParsedTermVariables() {
      return (Map<String, Variable>) variables.clone();
   }

   /**
    * Creates a {@link PTerm} from Prolog syntax read from this object's {@link CharacterParser}.
    */
   private PTerm getTerm(int maxLevel) {
      final PTerm firstArg = getPossiblePrefixArgument(maxLevel);
      if (parser.hasNext()) {
         return getTerm(firstArg, maxLevel, maxLevel, true);
      } else {
         return firstArg;
      }
   }

   /**
    * Recursively called to combine individual terms into a composite term.
    * <p>
    * While the parsing of the individual terms is performed, priority of the operands they represent needs to be
    * considered to make sure the terms are ordered correctly (due to different operand precedence it is not always the
    * case that the terms will be ordered in the resulting composite term in the same order they were parsed from the
    * input stream).
    * 
    * @param currentTerm represents the current state of the process to parse a complete term
    * @param currentLevel the current priority/precedence/level of terms being parsed - if an operand represented by a
    * term retrieved by this method has a higher priority then reordering needs to take place to position the term in
    * the right position in relation to the other terms that exist within the {@code currentTerm} structure (in order to
    * maintain the correct priority)
    * @param maxLevel the maximum priority/precedence/level of operands to parse - if an operand represented by the next
    * term retrieved by this method has a higher priority then it is ignored for now ({@code currentTerm} is returned
    * "as-is"}.
    * @param {@code true} if this method is being called by another method, {@code false} if it is being called
    * recursively by itself.
    */
   private PTerm getTerm(final PTerm currentTerm, final int currentLevel, final int maxLevel, final boolean isFirst) {
      final Token nextToken = popValue();
      final String next = nextToken.value;
      if (operands.postfix(next) && operands.getPostfixPriority(next) <= currentLevel) {
         PTerm postfixTerm = addPostfixOperand(next, currentTerm);
         return getTerm(postfixTerm, currentLevel, maxLevel, false);
      } else if (!operands.infix(next)) {
         // could be '.' if end of sentence 
         // or ',', '|', ']' or ')' if parsing list or predicate
         // or could be an error
         parser.rewind(nextToken);
         return currentTerm;
      }

      final int level = operands.getInfixPriority(next);
      if (level > maxLevel) {
         parser.rewind(nextToken);
         return currentTerm;
      }

      final PTerm secondArg = getPossiblePrefixArgument(level);

      if (isFirst) {
         final PTerm t = Structure.createStructure(next, new PTerm[] {currentTerm, secondArg});
         return getTerm(t, level, maxLevel, false);
      } else if (level < currentLevel) {
         // compare previous.getArgs()[1] to level -
         // keep going until find right level to add this term to
         PTerm t = currentTerm;
         while (isParsedInfixTerms(t.getArgs()[1]) && getInfixLevel(t.getArgs()[1]) > level) {
            if (bracketedTerms.contains(t.getArgs()[1])) {
               break;
            }
            t = t.getArgs()[1];
         }
         PTerm predicate = Structure.createStructure(next, new PTerm[] {t.getArgs()[1], secondArg});
         parsedInfixTerms.add(predicate);
         t.getArgs()[1] = predicate;
         return getTerm(currentTerm, currentLevel, maxLevel, false);
      } else {
         if (level == currentLevel) {
            if (operands.xfx(next)) {
               throw newParserException("Operand " + next + " has same precedence level as preceding operand: " + currentTerm);
            }
         }
         PTerm predicate = Structure.createStructure(next, new PTerm[] {currentTerm, secondArg});
         parsedInfixTerms.add(predicate);
         return getTerm(predicate, level, maxLevel, false);
      }
   }

   /**
    * Parses and returns a {@code Term}.
    * <p>
    * If the parsed {@code Term} represents a prefix operand, then the subsequent term is also parsed so it can be used
    * as an argument in the returned structure.
    * 
    * @param currentLevel the current priority level of terms being parsed (if the parsed term represents a prefix
    * operand, then the operand cannot have a higher priority than {@code currentLevel} (a {@code ParserException} will
    * be thrown if does).
    */
   private PTerm getPossiblePrefixArgument(int currentLevel) {
      final Token token = popValue();
      final String value = token.value;
      if (operands.prefix(value) && parser.isFollowedByTerm()) {
         if (value.equals(MINUS_SIGN) && isFollowedByNumber()) {
            return getNegativeNumber();
         }

         int prefixLevel = operands.getPrefixPriority(value);
         if (prefixLevel > currentLevel) {
            throw newParserException("Invalid prefix: " + value + " level: " + prefixLevel + " greater than current level: " + currentLevel);
         }

         // The difference between "fy" and "fx" associativity is that a "y" means that the argument
         // can contain operators of <i>the same</i> or lower level of priority
         // while a "x" means that the argument can <i>only</i> contain operators of a lower priority.
         if (operands.fx(value)) {
            // -1 to only parse terms of a lower priority than the current prefix operator.
            prefixLevel--;
         }

         PTerm argument = getTerm(prefixLevel);
         return createPrefixTerm(value, argument);
      } else {
         parser.rewind(token);
         return getDiscreteTerm();
      }
   }

   private PTerm getNegativeNumber() {
      final Token token = popValue();
      final String value = "-" + token.value;
      if (token.type == TokenType.INTEGER) {
         return toIntegerNumber(value);
      } else {
         return toDecimalFraction(value);
      }
   }

   /**
    * Returns a new {@code Term} representing the specified prefix operand and argument.
    */
   private PTerm createPrefixTerm(String prefixOperandName, PTerm argument) {
      return Structure.createStructure(prefixOperandName, new PTerm[] {argument});
   }

   /**
    * Add a term, representing a post-fix operand, in the appropriate point of a composite term.
    * <p>
    * The correct position of the post-fix operand within the composite term (and so what the post-fix operands actual
    * argument will be) is determined by operand priority.
    * 
    * @param original a composite term representing the current state of parsing the current sentence
    * @param postfixOperand a term which represents a post-fix operand
    */
   private PTerm addPostfixOperand(String postfixOperand, PTerm original) {
      int level = operands.getPostfixPriority(postfixOperand);
      if (original.args() == 2) {
         boolean higherLevelInfixOperand = operands.infix(original.getName()) && getInfixLevel(original) > level;
         if (higherLevelInfixOperand) {
            String name = original.getName();
            PTerm firstArg = original.arg(0);
            PTerm newSecondArg = addPostfixOperand(postfixOperand, original.arg(1));
            return Structure.createStructure(name, new PTerm[] {firstArg, newSecondArg});
         }
      } else if (original.args() == 1) {
         if (operands.prefix(original.getName())) {
            if (getPrefixLevel(original) > level) {
               String name = original.getName();
               PTerm newFirstArg = addPostfixOperand(postfixOperand, original.arg(0));
               return Structure.createStructure(name, new PTerm[] {newFirstArg});
            }
         } else if (operands.postfix(original.getName())) {
            int levelToCompareTo = getPostfixLevel(original);
            // "x" in "xf" means that the argument can <i>only</i> contain operators of a lower priority.
            if (levelToCompareTo > level || (operands.xf(postfixOperand) && levelToCompareTo == level)) {
               throw newParserException("Invalid postfix: " + postfixOperand + " " + level + " and term: " + original + " " + levelToCompareTo);
            }
         }
      }
      return Structure.createStructure(postfixOperand, new PTerm[] {original});
   }

   private PTerm getDiscreteTerm() {
      final Token token = popValue();
      if (isListOpenBracket(token)) {
         return parseList();
      } else if (isPredicateOpenBracket(token)) {
         return getTermInBrackets();
      } else {
         switch (token.type) {
            case ATOM:
            case QUOTED_ATOM:
            case SYMBOL:
               return getAtomOrStructure(token.value);
            case INTEGER:
               return toIntegerNumber(token.value);
            case FLOAT:
               return toDecimalFraction(token.value);
            case VARIABLE:
               return getVariable(token.value);
            case ANONYMOUS_VARIABLE:
               return getAnonymousVariable(token.value);
            default:
               throw new IllegalArgumentException();
         }
      }
   }

   private IntegerNumber toIntegerNumber(final String value) {
      return new IntegerNumber(parseLong(value));
   }

   private DecimalFraction toDecimalFraction(final String value) {
      return new DecimalFraction(parseDouble(value));
   }

   /**
    * Returns either an {@code Atom} or {@code Structure} with the specified name.
    * <p>
    * If the next character read from the parser is a {@code (} then a newly created {@code Structure} is returned else
    * a newly created {@code Atom} is returned.
    */
   private PTerm getAtomOrStructure(String name) {
      Token token = parser.hasNext() ? peekValue() : null;
      if (isPredicateOpenBracket(token)) {
         popValue(); //skip opening bracket
         if (isPredicateCloseBracket(peekValue())) {
            throw newParserException("No arguments specified for structure: " + name);
         }

         ArrayList<PTerm> args = new ArrayList<PTerm>();

         PTerm t = getCommaSeparatedArgument();
         args.add(t);

         do {
            token = popValue();
            if (isPredicateCloseBracket(token)) {
               return Structure.createStructure(name, toArray(args));
            } else if (isArgumentSeperator(token)) {
               args.add(getCommaSeparatedArgument());
            } else {
               throw newParserException("While parsing arguments of " + name + " expected ) or , but got: " + token);
            }
         } while (true);
      } else {
         return new Atom(name);
      }
   }

   /**
    * Returns a variable with the specified id.
    * <p>
    * If this object already has an instance of {@code Variable} with the specified id then it will be returned else a
    * new {@code Variable} will be created.
    */
   private Variable getVariable(String id) {
      Variable v = variables.get(id);
      if (v == null) {
         v = new Variable(id);
         variables.put(id, v);
      }
      return v;
   }

   private Variable getAnonymousVariable(String id) {
      return new Variable(id);
   }

   /** Returns a newly created {@code List} with elements read from the parser. */
   private PTerm parseList() {
      ArrayList<PTerm> args = new ArrayList<>();
      PTerm tail = EmptyList.EMPTY_LIST;

      while (true) {
         Token token = popValue();
         if (isListCloseBracket(token)) {
            break;
         }
         parser.rewind(token);
         PTerm arg = getCommaSeparatedArgument();
         args.add(arg);

         token = popValue(); // | ] or ,
         if (isListCloseBracket(token)) {
            break;
         } else if (isListTail(token)) {
            tail = getCommaSeparatedArgument();
            token = popValue();
            if (!isListCloseBracket(token)) {
               throw newParserException("Expected ] to mark end of list after tail but got: " + token);
            }
            break;
         } else if (!isArgumentSeperator(token)) {
            throw newParserException("While parsing list expected ] | or , but got: " + token);
         }
      }
      return ListFactory.createList(toArray(args), tail);
   }

   /**
    * Parses and returns the next argument of a list or structure.
    * <p>
    * As a comma would indicate a delimiter in a sequence of arguments, we only want to continue parsing up to the point
    * of any comma. i.e. Any parsed comma should not be considered as part of the argument currently being parsed.
    */
   private PTerm getCommaSeparatedArgument() {
      // Call getArgument with a priority/precedence/level of one less than the priority of a comma - 
      // as we only want to continue parsing terms that have a lower priority level than that.
      // The reason this is slightly complicated is because of the overloaded use of a comma in Prolog -  
      // as well as acting as a delimiter in a sequence of arguments for a list or structure,
      // a comma is also a predicate in it's own right (as a conjunction).
      if (operands.infix(",")) {
         return getTerm(operands.getInfixPriority(",") - 1);
      } else {
         return getTerm(Integer.MAX_VALUE);
      }
   }

   private PTerm getTermInBrackets() {
      // As we are at the starting point for parsing a term contained in brackets
      // (and as it being in brackets means we can parse it in isolation without 
      // considering the priority of any surrounding terms outside the brackets)
      // we call getArgument with the highest possible priority.
      PTerm t = getTerm(Integer.MAX_VALUE);
      final Token token = popValue();
      if (!isPredicateCloseBracket(token)) {
         throw newParserException("Expected ) but got: " + token + " after " + t);
      }
      bracketedTerms.add(t);
      return t;
   }

   private Token popValue() {
      return parser.next();
   }

   private Token peekValue() {
      Token token = popValue();
      parser.rewind(token);
      return token;
   }

   private boolean isFollowedByNumber() {
      Token token = popValue();
      TokenType tt = token.type;
      parser.rewind(token);
      return tt == TokenType.INTEGER || tt == TokenType.FLOAT;
   }

   /**
    * Has the specified term already been parsed, and included as an argument in an infix operand, as part of parsing
    * the current sentence?
    */
   private boolean isParsedInfixTerms(PTerm t) {
      return parsedInfixTerms.contains(t);
   }

   private int getPrefixLevel(PTerm t) {
      return operands.getPrefixPriority(t.getName());
   }

   private int getInfixLevel(PTerm t) {
      return operands.getInfixPriority(t.getName());
   }

   private int getPostfixLevel(PTerm t) {
      return operands.getPostfixPriority(t.getName());
   }

   private PTerm[] toArray(ArrayList<PTerm> al) {
      return al.toArray(TermUtils.EMPTY_ARRAY);
   }

   /** Returns a new {@link ParserException} with the specified message. */
   private ParserException newParserException(String message) {
      return parser.newParserException(message);
   }
}
