package org.projog.api;

import static org.projog.core.KnowledgeBaseUtils.getOperands;
import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;
import static org.projog.core.term.TermUtils.createAnonymousVariable;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.projog.core.KnowledgeBase;
import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.ProjogProperties;
import org.projog.core.ProjogSourceReader;
import org.projog.core.ProjogSystemProperties;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;
import org.projog.core.udp.UserDefinedPredicateFactory;
import org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate;

/**
 * Provides an entry point for other Java code to interact with Projog.
 * <p>
 * Contains a single instance of {@link org.projog.core.KnowledgeBase}.
 * <p>
 * <h3>Example usage</h3> Contents of {@code ProjogExample.java}:
 * 
 * <pre>
 * package org.projog.example;
 * 
 * import java.io.File;
 * 
 * import org.projog.api.Projog;
 * import org.projog.api.QueryResult;
 * import org.projog.api.QueryStatement;
 * import org.projog.core.term.Atom;
 * 
 * public class ProjogExample {
 *    public static void main(String[] args) {
 *       Projog p = new Projog();
 *       p.consultFile(new File("test.pl"));
 *       QueryStatement s1 = p.query("test(X,Y).");
 *       QueryResult r1 = s1.getResult();
 *       while (r1.next()) {
 *          System.out.println("X = " + r1.getTerm("X") + " Y = " + r1.getTerm("Y"));
 *       }
 *       QueryResult r2 = s1.getResult();
 *       r2.setTerm("X", new Atom("d"));
 *       while (r2.next()) {
 *          System.out.println("Y = " + r2.getTerm("Y"));
 *       }
 * 		
 *       QueryStatement s2 = p.query("testRule(X).");
 *       QueryResult r3 = s2.getResult();
 *       while (r3.next()) {
 *          System.out.println("X = " + r3.getTerm("X"));
 *       }
 *
 *       QueryStatement s3 = p.query("test(X, Y), Y<3.");
 *       QueryResult r4 = s3.getResult();
 *       while (r4.next()) {
 *          System.out.println("X = " + r4.getTerm("X") + " Y = " + r4.getTerm("Y"));
 *       }
 *    }
 * }
 * </pre>
 * Contents of {@code test.pl}:
 * 
 * <pre>
 * test(a,1).
 * test(b,2).
 * test(c,3).
 * test(d,4).
 * test(e,5).
 * test(f,6).
 * test(g,7).
 * test(h,8).
 * test(i,9).
 * 
 * testRule(X) :- test(X, Y), Y mod 2 =:= 0.
 * </pre>
 * Compiling {@code ProjogExample.java}:
 * 
 * <pre>javac -cp projog-core.jar;. ProjogExample.java</pre>
 * Running {@code ProjogExample} (the directory {@code projogGeneratedClasses} must already exist):
 * 
 * <pre>java -cp projog-core.jar;projogGeneratedClasses;. ProjogExample</pre>
 * Output of running {@code ProjogExample}:
 * 
 * <pre>
 * X = a Y = 1
 * X = b Y = 2
 * X = c Y = 3
 * X = d Y = 4
 * X = e Y = 5
 * X = f Y = 6
 * X = g Y = 7
 * X = h Y = 8
 * X = i Y = 9
 * Y = 4
 * X = b
 * X = d
 * X = f
 * X = h
 * X = a Y = 1
 * X = b Y = 2
 * </pre>
 * <p>
 * <img src="doc-files/Projog.png">
 */
public class Projog {
   private final KnowledgeBase kb;
   private final TermFormatter tf;

   /**
    * Constructs a new {@code Projog} object using {@link ProjogSystemProperties} and the specified {@code Observer}s.
    */
   public Projog(Observer... observers) {
      this(new ProjogSystemProperties(), observers);
   }

   /**
    * Constructs a new {@code Projog} object with the specified {@code ProjogProperties} and {@code Observer}s.
    */
   public Projog(ProjogProperties projogProperties, Observer... observers) {
      this.kb = KnowledgeBaseUtils.createKnowledgeBase(projogProperties);
      for (Observer o : observers) {
         addObserver(o);
      }
      KnowledgeBaseUtils.bootstrap(kb);
      this.tf = new TermFormatter(getOperands(kb));
   }

   /**
    * Populates this objects {@code KnowledgeBase} with clauses read from the specified file.
    * 
    * @param prologScript source of the prolog syntax defining the clauses to add
    * @throws ProjogException if there is any problem parsing the syntax or adding the new clauses
    */
   public void consultFile(File prologScript) {
      ProjogSourceReader.parseFile(kb, prologScript);
   }

   /**
    * Populates this objects {@code KnowledgeBase} with clauses read from the specified {@code Reader}.
    * 
    * @param reader source of the prolog syntax defining the clauses to add
    * @throws ProjogException if there is any problem parsing the syntax or adding the new clauses
    */
   public void consultReader(Reader reader) {
      ProjogSourceReader.parseReader(kb, reader);
   }

   /**
    * Creates a {@link QueryStatement} for querying the Projog environment.
    * <p>
    * The newly created object represents the query parsed from the specified syntax.
    * 
    * @param prologQuery prolog syntax representing a query
    * @return representation of the query parsed from the specified syntax
    * @throws ProjogException if an error occurs parsing {@code prologQuery}
    */
   public QueryStatement query(String prologQuery) {
      return new QueryStatement(kb, prologQuery);
   }

   /**
    * Registers an {@code Observer} to receive {@link org.projog.core.event.ProjogEvent}s generated during the
    * evaluation of Prolog goals.
    * 
    * @param observer an observer to be added
    */
   public void addObserver(Observer observer) {
      getProjogEventsObservable(kb).addObserver(observer);
   }

   /**
    * Returns a string representation of the specified {@code Term}.
    * 
    * @param t the {@code Term} to represent as a string
    * @return a string representation of the specified {@code Term}
    * @see org.projog.core.term.TermFormatter#toString(PTerm)
    */
   public String toString(PTerm t) {
      return tf.toString(t);
   }

   /**
    * Prints the all clauses contained in the specified throwable's stack trace to the standard error stream.
    */
   public void printProjogStackTrace(Throwable exception) {
      printProjogStackTrace(exception, System.err);
   }

   /**
    * Prints the all clauses contained in the specified throwable's stack trace to the specified print stream.
    */
   public void printProjogStackTrace(Throwable exception, PrintStream out) {
      ProjogStackTraceElement[] stackTrace = getStackTrace(exception);
      for (ProjogStackTraceElement e : stackTrace) {
         // e.getRuleIdx() is zero-based so +1 before displaying in output
         int ruleIdx = e.getClauseIdx() + 1;
         out.println(e.getPredicateKey() + " rule " + ruleIdx + " " + toString(e.getTerm()));
      }
   }

   /**
    * Provides programmatic access to the stack trace information printed by {@link #printProjogStackTrace(Throwable)}.
    */
   public ProjogStackTraceElement[] getStackTrace(Throwable exception) {
      List<ProjogStackTraceElement> result = new ArrayList<>();
      StackTraceElement[] elements = exception.getStackTrace();
      List<InterpretedUserDefinedPredicate> interpretedUserDefinedPredicates = getInterpretedUserDefinedPredicates(exception);
      for (StackTraceElement element : elements) {
         ProjogStackTraceElement pste = createProjogStackTraceElement(element, interpretedUserDefinedPredicates);
         if (pste != null) {
            result.add(pste);
         }
      }
      return result.toArray(new ProjogStackTraceElement[result.size()]);
   }

   private List<InterpretedUserDefinedPredicate> getInterpretedUserDefinedPredicates(Throwable e) {
      if (e instanceof ProjogException) {
         ProjogException pe = (ProjogException) e;
         return pe.getInterpretedUserDefinedPredicates();
      } else {
         return new ArrayList<>();
      }
   }

   private ProjogStackTraceElement createProjogStackTraceElement(StackTraceElement element, List<InterpretedUserDefinedPredicate> interpretedUserDefinedPredicates) {
      String className = element.getClassName();
      String methodName = element.getMethodName();
      if ("org.projog.core.udp.InterpretedUserDefinedPredicate".equals(className) && "evaluate".equals(methodName)) {
         InterpretedUserDefinedPredicate p = interpretedUserDefinedPredicates.remove(0);
         return createProjogStackTraceElementFromInterpretedUserDefinedPredicate(p);
      } else if (isCompiledPredicate(className) && isRuleEvaluationMethod(methodName)) {
         return createProjogStackTraceElementFromCompiledPredicate(className, methodName);
      } else {
         return null;
      }
   }

   private ProjogStackTraceElement createProjogStackTraceElementFromInterpretedUserDefinedPredicate(InterpretedUserDefinedPredicate p) {
      PredicateKey key = p.getPredicateKey();
      int ruleIdx = p.getCurrentClauseIdx();
      PredicateFactory ef = kb.getPredicateFactory(key);
      UserDefinedPredicateFactory pf = (UserDefinedPredicateFactory) ef;
      ClauseModel cm = pf.getClauseModel(ruleIdx);
      // ClauseModel might be null for dynamic predicates where it has been retracted
      PTerm term = cm == null ? createAnonymousVariable() : cm.getOriginal();
      return new ProjogStackTraceElement(key, ruleIdx, term);
   }

   private boolean isCompiledPredicate(String className) {
      return className.startsWith("org.projog.content_generated_at_runtime.");
   }

   private boolean isRuleEvaluationMethod(String methodName) {
      return methodName.startsWith("initRule") || methodName.startsWith("retryRule");
   }

   private ProjogStackTraceElement createProjogStackTraceElementFromCompiledPredicate(String className, String methodName) {
      int ruleIdx = getRuleIndex(methodName);
      StaticUserDefinedPredicateFactory compiledPredicate = getCompiledPredicateForClass(className);
      ClauseModel clauseModel = compiledPredicate.getClauseModel(ruleIdx);
      PredicateKey key = compiledPredicate.getPredicateKey();
      PTerm term = clauseModel.getOriginal();
      return new ProjogStackTraceElement(key, ruleIdx, term);
   }

   private int getRuleIndex(String methodName) {
      int beginIndex;
      if (methodName.startsWith("initRule")) {
         beginIndex = "initRule".length();
      } else {
         beginIndex = "retryRule".length();
      }
      String suffix = methodName.substring(beginIndex);
      return Integer.parseInt(suffix);
   }

   private StaticUserDefinedPredicateFactory getCompiledPredicateForClass(String className) {
      for (Map.Entry<PredicateKey, UserDefinedPredicateFactory> e : kb.getUserDefinedPredicates().entrySet()) {
         UserDefinedPredicateFactory udp = e.getValue();
         if (isCompiledPredicateForClass(className, udp)) {
            return (StaticUserDefinedPredicateFactory) udp;
         }
      }
      return null;
   }

   private boolean isCompiledPredicateForClass(String className, UserDefinedPredicateFactory udp) {
      if (udp instanceof StaticUserDefinedPredicateFactory) {
         StaticUserDefinedPredicateFactory compiledPredicate = (StaticUserDefinedPredicateFactory) udp;
         PredicateFactory ef = compiledPredicate.getActualPredicateFactory();
         return className.equals(ef.getClass().getName());
      }
      return false;
   }
}