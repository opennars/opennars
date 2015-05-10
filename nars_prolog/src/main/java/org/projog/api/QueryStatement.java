package org.projog.api;

import nars.Global;
import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.ProjogException;
import org.projog.core.parser.ParserException;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;

import java.util.Map;

import static org.projog.core.KnowledgeBaseUtils.getOperands;

/**
 * Represents a query.
 */
public final class QueryStatement  {
   private final PredicateFactory predicateFactory;
   private final PTerm parsedInput;
   private final Map<String, Variable> variables;
   private final int numVariables;

   /**
    * Creates a new {@code QueryStatement} representing a query specified by {@code prologQuery}.
    * 
    * @param kb the {@link org.projog.core.KnowledgeBase} to query against
    * @param prologQuery prolog syntax representing a query (do not prefix with a {@code ?-})
    * @throws ProjogException if an error occurs parsing {@code prologQuery}
    * @see Projog#query(String)
    */
   QueryStatement(KnowledgeBase kb, String prologQuery) {
      try {
         SentenceParser sp = SentenceParser.getInstance(prologQuery, getOperands(kb));

         this.parsedInput = sp.parseSentence();
         this.predicateFactory = kb.getPredicateFactory(parsedInput);
         this.variables = sp.getParsedTermVariables();
         this.numVariables = variables.size();

         if (sp.parseSentence() != null) {
            throw new ProjogException("More input found after .");
         }
      } catch (ParserException pe) {
         throw pe;
      } catch (Exception e) {
         throw new ProjogException(e.getClass().getName() + " caught parsing: " + prologQuery, e);
      }
   }

   /**
    * Returns a new {@link QueryResult} for the query represented by this object.
    * <p>
    * Note that the query is not evaluated as part of a call to {@code getResult()}. It is on the first call of
    * {@link QueryResult#next()} that the first attempt to evaluate the query will be made.
    * <p>
    * {@code getResult()} can be called multiple times on the same {@code QueryStatement} instance.
    * 
    * @return a new {@link QueryResult} for the query represented by this object.
    */
   public QueryResult get() {
      if (numVariables == 0) {
         return new QueryResult(predicateFactory, parsedInput, variables);
      }

      Map<String, Variable> copyVariables = Global.newHashMap(numVariables);
      Map<Variable, Variable> sharedVariables = Global.newHashMap(numVariables);
      for (Map.Entry<String, Variable> e : variables.entrySet()) {
         String id = e.getKey();
         Variable v = new Variable(id);
         copyVariables.put(id, v);
         sharedVariables.put(e.getValue(), v);
      }
      PTerm copyParsedInput = parsedInput.copy(sharedVariables);
      return new QueryResult(predicateFactory, copyParsedInput, copyVariables);
   }


}