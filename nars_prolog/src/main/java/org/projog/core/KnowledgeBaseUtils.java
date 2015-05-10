package org.projog.core;

import org.projog.core.event.ProjogEventsObservable;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;
import org.projog.core.term.TermType;

import java.util.ArrayList;
import java.util.List;

import static org.projog.core.KnowledgeBaseServiceLocator.getServiceLocator;

/**
 * Helper methods for performing common tasks on {@link KnowledgeBase} instances.
 */
public final class KnowledgeBaseUtils {
   /**
    * The functor of structures representing conjunctions ({@code ,}).
    */
   public static final String CONJUNCTION_PREDICATE_NAME = ",";
   /**
    * The functor of structures representing implications ({@code :-}).
    */
   public static final String IMPLICATION_PREDICATE_NAME = ":-";
   /**
    * The functor of structures representing questions (i.e. queries) ({@code ?-}).
    */
   public static final String QUESTION_PREDICATE_NAME = "?-";

   /**
    * Private constructor as all methods are static.
    */
   private KnowledgeBaseUtils() {
      // do nothing
   }

   /**
    * Constructs a new {@code KnowledgeBase} object using {@link ProjogSystemProperties}
    */
   public static KnowledgeBase createKnowledgeBase() {
      return createKnowledgeBase(new ProjogSystemProperties());
   }

   /**
    * Constructs a new {@code KnowledgeBase} object using the specified {@link ProjogProperties}
    */
   public static KnowledgeBase createKnowledgeBase(ProjogProperties projogProperties) {
      KnowledgeBase kb = new KnowledgeBase();
      getServiceLocator(kb).addInstance(ProjogProperties.class, projogProperties);
      return kb;
   }

   /**
    * Consults the {@link ProjogProperties#getBootstrapScript()} for the {@code KnowledgeBase}.
    * <p>
    * This is a way to configure a new {@code KnowledgeBase} (i.e. plugging in {@link Calculatable} and
    * {@link PredicateFactory} instances).
    * <p>
    * When using {@link ProjogSystemProperties} the resource parsed will be {@code projog-bootstrap.pl} (contained in
    * {@code projog-core.jar}).
    * 
    * @link ProjogSourceReader#parseResource(KnowledgeBase, String)
    */
   public static void bootstrap(KnowledgeBase kb) {
      ProjogSourceReader.parse(kb, Init.init);
   }

//      String bootstrapScript = getProjogProperties(kb).getBootstrapScript();
//      ProjogSourceReader.parseResource(kb, bootstrapScript);
//   }

   /**
    * Returns list of all user defined predicates with the specified name.
    */
   public static List<PredicateKey> getPredicateKeysByName(KnowledgeBase kb, String predicateName) {
      List<PredicateKey> matchingKeys = new ArrayList<>();
      for (PredicateKey key : kb.getUserDefinedPredicates().keySet()) {
         if (predicateName.equals(key.getName())) {
            matchingKeys.add(key);
         }
      }
      return matchingKeys;
   }

   /**
    * Returns a {@link Predicate} instance for the specified {@link PTerm}.
    */
   public static Predicate getPredicate(KnowledgeBase kb, PTerm t) {
      return kb.getPredicateFactory(t).getPredicate(t.getArgs());
   }

   /**
    * Returns {@code true} if the specified {@link PTerm} represents a question or directive, else {@code false}.
    * <p>
    * A {@link PTerm} is judged to represent a question if it is a structure a single argument and with a functor
    * {@link #QUESTION_PREDICATE_NAME} or {@link #IMPLICATION_PREDICATE_NAME}.
    */
   public static boolean isQuestionOrDirectiveFunctionCall(PTerm t) {
      return t.type() == TermType.STRUCTURE && t.args() == 1 && (QUESTION_PREDICATE_NAME.equals(t.getName()) || IMPLICATION_PREDICATE_NAME.equals(t.getName()));
   }

   /**
    * Returns {@code true} if the specified {@link PTerm} represent a {@code dynamic} function call, else {@code false}.
    * <p>
    * A {@link PTerm} is judged to represent a dynamic function call (i.e. a request to mark a user defined predicate as
    * "dynamic") if it is a structure with a functor of {@code dynamic} and a single argument.
    */
   public static boolean isDynamicFunctionCall(PTerm t) {
      return "dynamic".equals(t.getName()) && t.args() == 1;
   }

   /**
    * Returns {@code true} if the predicate represented by the specified {@link PTerm} never succeeds on re-evaluation.
    */
   public static boolean isSingleAnswer(KnowledgeBase kb, PTerm term) {
      if (term.type().isVariable()) {
         return false;
      } else if (isConjunction(term)) {
         return isConjunctionWithSingleResult(kb, term);
      } else {
         PredicateFactory ef = kb.getPredicateFactory(term);
         return ef instanceof AbstractSingletonPredicate;
      }
   }

   /**
    * Returns an array of all {@link PTerm}s that make up the conjunction represented by the specified {@link PTerm}.
    * <p>
    * If the specified {@link PTerm} does not represent a conjunction then it will be used as the only element in the
    * returned array.
    */
   public static PTerm[] toArrayOfConjunctions(PTerm t) {
      List<PTerm> l = new ArrayList<>();
      while (isConjunction(t)) {
         l.add(0, t.getArgs()[1]);
         t = t.getArgs()[0];
      }
      l.add(0, t);
      return l.toArray(new PTerm[l.size()]);
   }

   private static boolean isConjunctionWithSingleResult(KnowledgeBase kb, PTerm antecedant) {
      PTerm[] functions = toArrayOfConjunctions(antecedant);
      return isAllSingleAnswerFunctions(kb, functions);
   }

   private static boolean isAllSingleAnswerFunctions(KnowledgeBase kb, PTerm[] functions) {
      for (PTerm t : functions) {
         if (!isSingleAnswer(kb, t)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns {@code true} if the specified {@link PTerm} represent a conjunction, else {@code false}.
    * <p>
    * A {@link PTerm} is judged to represent a conjunction if is a structure with a functor of
    * {@link #CONJUNCTION_PREDICATE_NAME} and exactly two arguments.
    */
   public static boolean isConjunction(PTerm t) {
      // is relying on assumption that conjunctions are only, and always, represented by a comma
      return t.type() == TermType.STRUCTURE && CONJUNCTION_PREDICATE_NAME.equals(t.getName()) && t.getArgs().length == 2;
   }

   public static ProjogEventsObservable getProjogEventsObservable(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(ProjogEventsObservable.class);
   }

   public static ProjogProperties getProjogProperties(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(ProjogProperties.class);
   }

   public static Operands getOperands(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(Operands.class);
   }

   public static TermFormatter getTermFormatter(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(TermFormatter.class);
   }

   public static SpyPoints getSpyPoints(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(SpyPoints.class);
   }

   public static FileHandles getFileHandles(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(FileHandles.class);
   }

   public static Calculatables getCalculatables(KnowledgeBase kb) {
      return getServiceLocator(kb).getInstance(Calculatables.class);
   }
}