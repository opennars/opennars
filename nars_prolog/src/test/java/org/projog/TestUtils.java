package org.projog;

import org.projog.core.*;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.*;
import org.projog.core.udp.ClauseModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.projog.core.KnowledgeBaseUtils.getOperands;

/**
 * Helper methods for performing unit tests.
 */
public class TestUtils {
   public static final PredicateKey ADD_PREDICATE_KEY = new PredicateKey("pj_add_predicate", 2);
   public static final PredicateKey ADD_CALCULATABLE_KEY = new PredicateKey("pj_add_calculatable", 2);
   public static final File BOOTSTRAP_FILE = new File("projog-bootstrap.pl");
   public static final ProjogProperties COMPILATION_DISABLED_PROPERTIES = new ProjogSystemProperties() {
      @Override
      public boolean isRuntimeCompilationEnabled() {
         return false;
      }
   };
   public static final ProjogProperties COMPILATION_ENABLED_PROPERTIES = new ProjogSystemProperties() {
      @Override
      public boolean isRuntimeCompilationEnabled() {
         return true;
      }

      @Override
      public String getRuntimeCompilationOutputDirectory() {
         String dir = "projogGeneratedClasses";
         new File(dir).mkdirs();
         return dir;
      }
   };

   private static final File TEMP_DIR = new File("build");

   private static final Operands OPERANDS = getOperands(createKnowledgeBase());

   /**
    * Private constructor as all methods are static.
    */
   private TestUtils() {
      // do nothing
   }

   public static File writeToTempFile(Class<?> c, String contents) {
      try {
         File tempFile = createTempFile(c.getClass());
         try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(contents);
         }
         return tempFile;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static File createTempFile(Class<?> c) throws IOException {
      TEMP_DIR.mkdir();
      File tempFile = File.createTempFile(c.getName(), ".tmp", TEMP_DIR);
      tempFile.deleteOnExit();
      return tempFile;
   }

   public static KB createKnowledgeBase() {
      try {
         KB kb = KnowledgeBaseUtils.createKnowledgeBase();
         KnowledgeBaseUtils.bootstrap(kb);
         return kb;
      } catch (Throwable t) {
         t.printStackTrace();
         throw new RuntimeException(t);
      }
   }

   public static KB createKnowledgeBase(ProjogProperties projogProperties) {
      try {
         KB kb = KnowledgeBaseUtils.createKnowledgeBase(projogProperties);
         KnowledgeBaseUtils.bootstrap(kb);
         return kb;
      } catch (Throwable t) {
         t.printStackTrace();
         throw new RuntimeException(t);
      }
   }

   public static PAtom atom() {
      return atom("test");
   }

   public static PAtom atom(String name) {
      return new PAtom(name);
   }

   public static PStruct structure() {
      return structure("test", new PTerm[] {atom()});
   }

   public static PStruct structure(String name, PTerm... args) {
      return (PStruct) PStruct.make(name, args);
   }

   public static PList list(PTerm... args) {
      return (PList) ListFactory.createList(args);
   }

   public static IntegerNumber integerNumber() {
      return integerNumber(1);
   }

   public static IntegerNumber integerNumber(long i) {
      return new IntegerNumber(i);
   }

   public static DecimalFraction decimalFraction() {
      return decimalFraction(1.0);
   }

   public static DecimalFraction decimalFraction(double d) {
      return new DecimalFraction(d);
   }

   public static PVar variable() {
      return variable("X");
   }

   public static PVar variable(String name) {
      return new PVar(name);
   }

   public static PTerm[] createArgs(int numberOfArguments) {
      return createArgs(numberOfArguments, atom());
   }

   public static PTerm[] createArgs(int numberOfArguments, PTerm term) {
      PTerm[] args = new PTerm[numberOfArguments];
      Arrays.fill(args, term);
      return args;
   }

   public static SentenceParser createSentenceParser(String prologSyntax) {
      return SentenceParser.getInstance(prologSyntax, OPERANDS);
   }

   public static PTerm parseSentence(String prologSyntax) {
      SentenceParser sp = createSentenceParser(prologSyntax);
      return sp.parseSentence();
   }

   public static PTerm parseTerm(String source) {
      SentenceParser sp = createSentenceParser(source);
      return sp.parseTerm();
   }

   public static ClauseModel createClauseModel(String prologSentenceSytax) {
      PTerm t = parseSentence(prologSentenceSytax);
      return ClauseModel.createClauseModel(t);
   }

   public static String write(PTerm t) {
      return new TermFormatter(OPERANDS).toString(t);
   }

   public static PTerm[] parseTermsFromFile(File f) {
      try (FileReader fr = new FileReader(f)) {
         SentenceParser sp = SentenceParser.getInstance(fr, OPERANDS);

         ArrayList<PTerm> result = new ArrayList<>();
         PTerm next;
         while ((next = sp.parseSentence()) != null) {
            result.add(next);
         }
         return result.toArray(new PTerm[result.size()]);
      } catch (IOException e) {
         throw new RuntimeException("Could not parse: " + f, e);
      }
   }

   public static void assertStrictEquality(PTerm t1, PTerm t2, boolean expectedResult) {
      assertTrue(t1.strictEquals(t2) == expectedResult);
      assertTrue(t2.strictEquals(t1) == expectedResult);
   }
}