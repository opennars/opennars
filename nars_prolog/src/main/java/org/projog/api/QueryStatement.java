package org.projog.api;

import jdk.nashorn.internal.objects.Global;
import org.projog.core.KB;
import org.projog.core.PredicateFactory;
import org.projog.core.ProjogException;
import org.projog.core.parser.ParserException;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;

import java.util.HashMap;
import java.util.Map;

import static org.projog.core.KnowledgeBaseUtils.getOperands;

/**
 * Represents a query.
 */
public final class QueryStatement  {
   private final PredicateFactory predicateFactory;
   private final PTerm parsedInput;
   private final Map<String, PVar> variables;
   private final int numVariables;

   /**
    * Creates a new {@code QueryStatement} representing a query specified by {@code prologQuery}.
    *
    * @param kb the {@link KB} to query against
    * @param prologQuery prolog syntax representing a query (do not prefix with a {@code ?-})
    * @throws ProjogException if an error occurs parsing {@code prologQuery}
    * @see Projog#query(String)
    */
   QueryStatement(KB kb, String prologQuery) {
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

      Map<String, PVar> copyVariables = new HashMap(numVariables);
      Map<PVar, PVar> sharedVariables = new HashMap(numVariables);
      for (Map.Entry<String, PVar> e : variables.entrySet()) {
         String id = e.getKey();
         PVar v = new PVar(id);
         copyVariables.put(id, v);
         sharedVariables.put(e.getValue(), v);
      }
      PTerm copyParsedInput = parsedInput.copy(sharedVariables);
      return new QueryResult(predicateFactory, copyParsedInput, copyVariables);
   }


}