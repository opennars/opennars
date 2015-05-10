package org.projog.core;

import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.PTerm;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.DynamicUserDefinedPredicateFactory;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;
import org.projog.core.udp.UserDefinedPredicateFactory;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.projog.core.KnowledgeBaseUtils.*;

/**
 * Populates a {@link KnowledgeBase} with clauses parsed from Prolog syntax.
 * <p>
 * <img src="doc-files/ProjogSourceReader.png">
 */
public final class ProjogSourceReader {
   private final KnowledgeBase kb;
   private final Map<PredicateKey, UserDefinedPredicateFactory> userDefinedPredicates = new LinkedHashMap<>();

   /**
    * Populates the KnowledgeBase with clauses defined in the file.
    * 
    * @param kb the KnowledgeBase to add the clauses to
    * @param prologSourceFile source of the prolog syntax defining clauses to add to the KnowledgeBase
    * @throws ProjogException if there is any problem parsing the syntax or adding the new clauses to the KnowledgeBase
    */
   public static void parseFile(KnowledgeBase kb, File prologSourceFile) {
      notifyReadingFromFileSystem(kb, prologSourceFile);
      try (Reader reader = new FileReader(prologSourceFile)) {
         ProjogSourceReader projogSourceReader = new ProjogSourceReader(kb);
         projogSourceReader.parse(reader);
      } catch (Exception e) {
         throw new ProjogException("Could not read prolog source from file: " + prologSourceFile + " due to: " + e, e);
      }
   }

   /**
    * Populates the KnowledgeBase with clauses defined in the specified resource.
    * <p>
    * If {@code prologSourceResourceName} refers to an existing file on the file system then that file is used as the
    * source of the prolog syntax else {@code prologSourceResourceName} is read from the classpath.
    * 
    * @param kb the KnowledgeBase to add the clauses to
    * @param prologSourceResourceName source of the prolog syntax defining clauses to add to the KnowledgeBase
    * @throws ProjogException if there is any problem parsing the syntax or adding the new clauses to the KnowledgeBase
    */
   public static void parseResource(KnowledgeBase kb, String prologSourceResourceName) {
      try (Reader reader = getReader(kb, prologSourceResourceName)) {
         ProjogSourceReader projogSourceReader = new ProjogSourceReader(kb);
         projogSourceReader.parse(reader);
      } catch (Exception e) {
         throw new ProjogException("Could not read prolog source from resource: " + prologSourceResourceName, e);
      }
   }

   /**
    * Populates the KnowledgeBase with clauses read from the Reader.
    * <p>
    * Note that this method will call {@code close()} on the specified reader - regardless of whether this method
    * completes successfully or if an exception is thrown.
    * 
    * @param kb the KnowledgeBase to add the clauses to
    * @param reader source of the prolog syntax defining clauses to add to the KnowledgeBase
    * @throws ProjogException if there is any problem parsing the syntax or adding the new clauses to the KnowledgeBase
    */
   public static void parseReader(KnowledgeBase kb, Reader reader) {
      try {
         ProjogSourceReader projogSourceReader = new ProjogSourceReader(kb);
         projogSourceReader.parse(reader);
      } catch (Exception e) {
         throw new ProjogException("Could not read prolog source from java.io.Reader: " + reader, e);
      } finally {
         try {
            reader.close();
         } catch (Exception e) {
         }
      }
   }

   public static void parse(KnowledgeBase kb, String s) {
      parseReader(kb, new StringReader(s));
   }

   /**
    * Creates a new {@code Reader} for the specified resource.
    * <p>
    * If {@code resourceName} refers to an existing file on the filesystem then that file is used as the source of the
    * {@code Reader}. If there is no existing file on the filesystem matching {@code resourceName} then an attempt is
    * made to read the resource from the classpath.
    */
   private static Reader getReader(KnowledgeBase kb, String resourceName) throws IOException {
      File f = new File(resourceName);
      if (f.exists()) {
         notifyReadingFromFileSystem(kb, f);
         return new FileReader(resourceName);
      } else {
         notifyReadingFromClasspath(kb, resourceName);
         InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
         if (is == null) {
            throw new ProjogException("Cannot find resource: " + resourceName + " at " + f.getAbsolutePath());
         }
         return new InputStreamReader(is);
      }
   }

   private static void notifyReadingFromFileSystem(KnowledgeBase kb, File file) {
      ProjogEvent event = new ProjogEvent(ProjogEventType.INFO, "Reading prolog source in: " + file + " from file system", ProjogSourceReader.class);
      getProjogEventsObservable(kb).notifyObservers(event);
   }

   private static void notifyReadingFromClasspath(KnowledgeBase kb, String resourceName) {
      ProjogEvent event = new ProjogEvent(ProjogEventType.INFO, "Reading prolog source in: " + resourceName + " from classpath", ProjogSourceReader.class);
      getProjogEventsObservable(kb).notifyObservers(event);
   }

   private ProjogSourceReader(KnowledgeBase kb) {
      this.kb = kb;
   }

   private void parse(Reader reader) {
      try {
         parseTerms(reader);
         addUserDefinedPredicatesToKnowledgeBase();
      } finally {
         try {
            reader.close();
         } catch (Exception e) {
         }
      }
   }

   private void parseTerms(Reader reader) {
      SentenceParser sp = SentenceParser.getInstance(reader, getOperands(kb));
      PTerm t;
      while ((t = sp.parseSentence()) != null) {
         if (isQuestionOrDirectiveFunctionCall(t)) {
            processQuestion(t);
         } else {
            storeParsedTerm(t);
         }
      }
   }

   /**
    * @param t structure with name of {@code ?-} and a single argument.
    */
   private void processQuestion(PTerm t) {
      PTerm query = t.arg(0);
      if (isDynamicFunctionCall(query)) {
         declareDynamicPredicate(query.arg(0));
      } else {
         Predicate e = KnowledgeBaseUtils.getPredicate(kb, query);
         while (e.evaluate(query.getArgs()) && e.isRetryable()) {
            // keep re-evaluating until fail
         }
      }
   }

   /**
    * Declare a user defined predicate as "dynamic".
    * <p>
    * Only user defined predicates declared as "dynamic" can be used with the "asserta", "assertz" and "retract"
    * commands.
    * 
    * @param query structure named {@code /} where first argument is name and second is arity.
    * @throws ProjogException if the user defined predicate has already been read
    */
   private void declareDynamicPredicate(PTerm t) {
      PredicateKey key = PredicateKey.createFromNameAndArity(t);
      UserDefinedPredicateFactory userDefinedPredicate = userDefinedPredicates.get(key);
      if (userDefinedPredicate == null) {
         userDefinedPredicate = new DynamicUserDefinedPredicateFactory(kb, key);
         userDefinedPredicates.put(key, userDefinedPredicate);
      } else {
         throw new ProjogException("Cannot declare: " + key + " as a dynamic predicate when it has already been used. Query: " + t);
      }
   }

   private void storeParsedTerm(PTerm parsedTerm) {
      ClauseModel clauseModel = ClauseModel.createClauseModel(parsedTerm);
      PTerm parsedTermConsequent = clauseModel.getConsequent();
      UserDefinedPredicateFactory userDefinedPredicate = createOrReturnUserDefinedPredicate(parsedTermConsequent);
      userDefinedPredicate.addLast(clauseModel);
   }

   private UserDefinedPredicateFactory createOrReturnUserDefinedPredicate(PTerm t) {
      PredicateKey key = PredicateKey.createForTerm(t);
      UserDefinedPredicateFactory userDefinedPredicate = userDefinedPredicates.get(key);
      if (userDefinedPredicate == null) {
         userDefinedPredicate = new StaticUserDefinedPredicateFactory(key);
         userDefinedPredicate.setKnowledgeBase(kb);
         userDefinedPredicates.put(key, userDefinedPredicate);
      }
      return userDefinedPredicate;
   }

   private void addUserDefinedPredicatesToKnowledgeBase() {
      for (UserDefinedPredicateFactory userDefinedPredicate : userDefinedPredicates.values()) {
         kb.addUserDefinedPredicate(userDefinedPredicate);
      }
      for (UserDefinedPredicateFactory userDefinedPredicate : userDefinedPredicates.values()) {
         if (userDefinedPredicate instanceof StaticUserDefinedPredicateFactory) {
            ((StaticUserDefinedPredicateFactory) userDefinedPredicate).compile();
         }
      }
   }
}