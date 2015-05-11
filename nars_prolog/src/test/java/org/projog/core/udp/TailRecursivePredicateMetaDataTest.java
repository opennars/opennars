package org.projog.core.udp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.KB;
import org.projog.core.term.PTerm;

public class TailRecursivePredicateMetaDataTest {
   private final KB kb = TestUtils.createKnowledgeBase();
   private List<ClauseModel> clauses;

   @Before
   public void setUp() {
      clauses = null;
   }

   @Test
   public void testPrefix() {
      setClauses(":- prefix([],Ys).", "prefix([X|Xs],[X|Ys]) :- prefix(Xs,Ys).");
      assertSingleResultTailRecursive("prefix([a],[a,b,c]).");
      assertMultipleResultsTailRecursive("prefix(X,[a,b,c]).");
   }

   @Test
   public void testAppend() {
      setClauses(":- append([],Ys,Ys).", "append([X|Xs],Ys,[X|Zs]) :- append(Xs,Ys,Zs).");
      assertSingleResultTailRecursive("append([a,b,c],[d,e,f],Zs).");
      assertMultipleResultsTailRecursive("append(As,[X,Y|Ys],Zs)."); // query finds adjacent terms
   }

   @Test
   public void testMember() {
      setClauses(":- member(X,[X|Xs]).", "member(X,[Y|Ys]) :- member(X,Ys).");
      assertMultipleResultsTailRecursive("member(a,[a,b,c]).");
      assertMultipleResultsTailRecursive("member(X,[a,b,c]).");
   }

   @Test
   public void testRepeat() {
      setClauses("repeat(N).", "repeat(N) :- N > 1, N1 is N-1, repeat(N1).");
      assertMultipleResultsTailRecursive("repeat(10000).");
   }

   @Test
   public void testRepeatWithWrite() {
      setClauses("writeAndRepeat(N) :- write(N).", "writeAndRepeat(N) :- N > 1, N1 is N-1, writeAndRepeat(N1).");
      assertMultipleResultsTailRecursive("writeAndRepeat(10000).");
   }

   @Test
   public void testRepeatWithSingleResultConjunction() {
      setClauses("writeNewLineAndRepeat(N) :- write(N), nl.", "writeNewLineAndRepeat(N) :- N > 1, N1 is N-1, writeNewLineAndRepeat(N1).");
      assertMultipleResultsTailRecursive("writeNewLineAndRepeat(10000).");
   }

   @Test
   public void testNonTailRecursivePredicates() {
      assertNotTailRecursive("t(Y) :- t(Y).");

      assertNotTailRecursive("repeat(N).", "repeat(N) :- N > 1, N1 is N-1, repeat(N1), write(N1).");

      assertNotTailRecursive("repeat(N).", "repeat(N) :- N > 1, N1 is N-1, repeat(N1).", "repeat(N) :- N < 1.");
   }

   private void assertSingleResultTailRecursive(String input) {
      PTerm parsedSentence = TestUtils.parseSentence(input);
      assertTrue(isSingleResultTailRecursive(copyClauses(), parsedSentence.terms()));
   }

   private void assertMultipleResultsTailRecursive(String input) {
      PTerm parsedSentence = TestUtils.parseSentence(input);
      assertFalse(isSingleResultTailRecursive(copyClauses(), parsedSentence.terms()));
   }

   private boolean isSingleResultTailRecursive(List<ClauseModel> facts, PTerm[] args) {
      TailRecursivePredicateMetaData metaData = TailRecursivePredicateMetaData.create(kb, facts);
      assertNotNull(metaData);
      for (int i = 0; i < args.length; i++) {
         if (metaData.isSingleResultIfArgumentImmutable(i) && args[i].type().isVariable() == false) {
            return true;
         }
      }
      return false;
   }

   private List<ClauseModel> setClauses(String... sentences) {
      clauses = new ArrayList<>();

      for (String sentence : sentences) {
         ClauseModel clause = TestUtils.createClauseModel(sentence);
         clauses.add(clause);
      }

      return clauses;
   }

   private List<ClauseModel> copyClauses() {
      List<ClauseModel> copy = new ArrayList<>(clauses.size());
      for (ClauseModel clause : clauses) {
         copy.add(clause.copy());
      }
      return copy;
   }

   private void assertNotTailRecursive(String... prologClauses) {
      setClauses(prologClauses);
      TailRecursivePredicateMetaData metaData = TailRecursivePredicateMetaData.create(kb, clauses);
      assertNull(metaData);
   }
}