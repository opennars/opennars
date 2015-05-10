package org.projog.api;

import org.junit.Ignore;
import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.ProjogException;
import org.projog.core.ProjogProperties;
import org.projog.core.parser.ParserException;
import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.StringReader;

import static java.lang.System.lineSeparator;
import static org.junit.Assert.*;
import static org.projog.TestUtils.*;

/**
 * Tests {@link Projog}.
 * <p>
 * The majority of tests in this class work by calling {@link #createQueryResult()} which provides a convenient way of
 * creating a {@link QueryResult}. The {@link QueryResult} uses a new {@link Projog} instance that has been populated
 * with the contents of {@link #createTestScript()}
 */
public class ProjogTest {
   @Test
   public void testSimpleQuery() {
      QueryResult r = createQueryResult();
      assertTrue(r.next());
      assertEquals("a", r.getTerm("X").toString());
      assertEquals("b", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertEquals("1", r.getTerm("X").toString());
      assertEquals("1", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertEquals("a", r.getTerm("X").toString());
      assertEquals("c", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertEquals("z", r.getTerm("X").toString());
      assertEquals("9", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertEquals("a", r.getTerm("X").toString());
      assertEquals("e", r.getTerm("Y").toString());
      assertFalse(r.next());
   }

   @Test
   public void testQueryMultiResultsAfterSetTerm() {
      QueryResult r = createQueryResult();
      Atom a = atom("a");
      assertTrue(r.setTerm("X", a));
      assertSame(a, r.getTerm("X"));
      assertTrue(r.next());
      assertSame(a, r.getTerm("X"));
      assertEquals("b", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertSame(a, r.getTerm("X"));
      assertEquals("c", r.getTerm("Y").toString());
      assertTrue(r.next());
      assertSame(a, r.getTerm("X"));
      assertEquals("e", r.getTerm("Y").toString());
      assertFalse(r.next());
   }

   @Test
   public void testQuerySingleResultsAfterSetTerm() {
      QueryResult r = createQueryResult();
      Atom z = atom("z");
      assertTrue(r.setTerm("X", z));
      assertSame(z, r.getTerm("X"));
      assertTrue(r.next());
      assertSame(z, r.getTerm("X"));
      assertEquals("9", r.getTerm("Y").toString());
      assertFalse(r.next());
   }

   @Test
   public void testQueryNoResultsAfterSetTerm() {
      QueryResult r = createQueryResult();
      Atom y = atom("y");
      assertTrue(r.setTerm("X", y));
      assertSame(y, r.getTerm("X"));
      assertFalse(r.next());
   }

   @Test
   public void testSetTermForUnknownVariable() {
      QueryResult r = createQueryResult();
      Atom x = atom("x");
      try {
         r.setTerm("Z", x);
         fail();
      } catch (ProjogException e) {
         assertEquals("Do not know about variable named: Z in query: test(X, Y)", e.getMessage());
      }
   }

   @Test
   public void testSetTermAfterNext() {
      QueryResult r = createQueryResult();
      Atom x = atom("x");
      r.next();
      try {
         r.setTerm("X", x);
         fail();
      } catch (ProjogException e) {
         assertEquals("Calling setTerm(X, x) after next() has already been called for: test(a, b)", e.getMessage());
      }
   }

   @Test
   public void testGetTermForUnknownVariable() {
      QueryResult r = createQueryResult();
      r.getTerm("X");
      r.getTerm("Y");
      try {
         r.getTerm("Z");
         fail();
      } catch (ProjogException e) {
         assertEquals("Do not know about variable named: Z in query: test(X, Y)", e.getMessage());
      }
   }

   @Test
   public void testInvalidQuery() {
      try {
         Projog p = createProjog();
         p.query("X");
         fail();
      } catch (ParserException e) {
         assertEquals("Unexpected end of stream Line: X", e.getMessage());
      }
   }

   @Test
   public void testMoreThanOneSentenceInQuery() {
      try {
         Projog p = createProjog();
         p.query("X is 1. Y is 2.");
         fail();
      } catch (ProjogException e) {
         assertEquals("org.projog.core.ProjogException caught parsing: X is 1. Y is 2.", e.getMessage());
         assertEquals("More input found after .", e.getCause().getMessage());
      }
   }

   /**
    * Tests a query that fails while it is being evaluated.
    * <p>
    * This method is a little different than the other tests in the class in that it does not use
    * {@link #createQueryResult()}.
    */
   @Test
   public void testProjogExceptionWhileEvaluatingQueries() {
      Projog p = createProjog();
      StringReader sr = new StringReader("a(A) :- b(A). b(Z) :- c(Z, 5). c(X,Y) :- Z is X + Y, Z < 9.");
      p.consultReader(sr);
      QueryStatement s = p.query("a(X).");
      QueryResult r = s.get();
      try {
         r.next();
         fail();
      } catch (ProjogException e) {
         assertSame(ProjogException.class, e.getClass()); // check it is not a sub-class
         assertEquals("Cannot get Numeric for term: X of type: NAMED_VARIABLE", e.getMessage());
      }
   }

   /**
    * Attempts to open a file that doesn't exist to see how non-ProjogException exceptions are dealt with.
    * <p>
    * NOTE: as this test actually compiles the Prolog syntax into Java bytecode at runtime, you will need the directory
    * specified by the {@link ProjogProperties#getRuntimeCompilationOutputDirectory} method implemented by
    * {@link TestUtils#COMPILATION_ENABLED_PROPERTIES} on the classpath for this test to work.
    */
   @Test @Ignore
   public void testIOExceptionWhileEvaluatingQueries() {
      Projog p = new Projog(COMPILATION_ENABLED_PROPERTIES);
      StringBuilder inputSource = new StringBuilder();
      inputSource.append("x(A) :- fail. x(A) :- y(A). x(A). ");
      inputSource.append("y(A) :- Q is 4 + 5, z(A, A, Q). ");
      inputSource.append("z(A, B, C) :- fail. z(A, B, C) :- 7<3. z(A, B, C) :- open(A,'read',Z). z(A, B, C). ");
      StringReader sr = new StringReader(inputSource.toString());
      p.consultReader(sr);
      QueryStatement s = p.query("x('a_directory_that_doesnt_exist/another_directory_that_doesnt_exist/some_file.xyz').");
      QueryResult r = s.get();
      try {
         r.next();
         fail();
      } catch (ProjogException projogException) {
         assertEquals("Unable to open input for: a_directory_that_doesnt_exist/another_directory_that_doesnt_exist/some_file.xyz", projogException.getMessage());
         FileNotFoundException fileNotFoundException = (FileNotFoundException) projogException.getCause();

         // retrieve and check stack trace elements
         ProjogStackTraceElement[] elements = p.getStackTrace(fileNotFoundException);
         assertEquals(3, elements.length);
         assertProjogStackTraceElement(elements[0], "z/3", 2, ":-(z(A, B, C), open(A, read, Z))");
         assertProjogStackTraceElement(elements[1], "y/1", 0, ":-(y(A), ,(is(Q, +(4, 5)), z(A, A, Q)))");
         assertProjogStackTraceElement(elements[2], "x/1", 1, ":-(x(A), y(A))");

         // Write stack trace to OutputStream so it can be compared against the expected result.
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         PrintStream ps = new PrintStream(bos);
         p.printProjogStackTrace(projogException, ps);
         ps.close();

         // Generate expected stack trace.
         StringBuilder expectedResult = new StringBuilder();
         expectedResult.append("z/3 rule 3 z(A, B, C) :- open(A, read, Z)");
         expectedResult.append(lineSeparator());
         expectedResult.append("y/1 rule 1 y(A) :- Q is 4 + 5 , z(A, A, Q)");
         expectedResult.append(lineSeparator());
         expectedResult.append("x/1 rule 2 x(A) :- y(A)");
         expectedResult.append(lineSeparator());

         // Confirm contents of stack trace
         assertEquals(expectedResult.toString(), bos.toString());
      }
   }

   /** Tests a query that contains a cut (i.e. !). */
   @Test
   public void testQueryContainingCut() {
      Projog p = createProjog();
      QueryStatement s = p.query("repeat, !.");
      QueryResult r = s.get();
      assertTrue(r.next());
      assertFalse(r.next());
   }

   @Test
   public void testTermToString() {
      Projog p = createProjog();
      PTerm inputTerm = TestUtils.parseSentence("X is 1 + 1 ; 3 < 5.");
      assertEquals(write(inputTerm), p.toString(inputTerm));
   }

   private void assertProjogStackTraceElement(ProjogStackTraceElement actual, String expectedKey, int expectedClauseIdx, String expectedTerm) {
      assertEquals(expectedKey, actual.getPredicateKey().toString());
      assertEquals(expectedClauseIdx, actual.getClauseIdx());
      assertEquals(expectedTerm, actual.getTerm().toString());
   }

   /** Provides a convenient way of creating a {@link QueryResult} to test with. */
   private QueryResult createQueryResult() {
      Projog p = createProjog();
      QueryStatement s = p.query("test(X,Y).");
      return s.get();
   }

   /** Returns a new {@link Projog} instance that has been populated with rules from {@link #createTestScript()}. */
   private Projog createProjog() {
      //File f = createTestScript();
      // Note: using COMPILATION_DISABLED_PROPERTIES as else 
      // code will try to generate Java bytecode at runtime as part of completing
      // tests and that will fail unless classpath contains classes output directory -
      // so disable it as we are not interested in compilation as part of these tests.
      Projog p = new Projog(COMPILATION_DISABLED_PROPERTIES);
      //p.consultFile(f);
      StringReader s = newTestScript();
      p.consultReader(s);
      return p;
   }

//   /**
//    * Returns a file containing Prolog syntax that can be used to populate the knowledge base of a {@link Projog}
//    * instance.
//    */
//   private File createTestScript() {
//      try {
//         File f = File.createTempFile(getClass().getSimpleName(), ".pl", new File("build"));
//         f.deleteOnExit();
//         try (PrintWriter pw = new PrintWriter(f)) {
//            pw.println("test(a,b).");
//            pw.println("test(A,A) :- A is 1.");
//            pw.println("test(a,c).");
//            pw.println("test(z,9).");
//            pw.println("test(a,e).");
//         }
//         return f;
//      } catch (Exception e) {
//         throw new RuntimeException("error creating test script" + e, e);
//      }
//   }
   private StringReader newTestScript() {
         PrintStream out;
         StringReader s = new StringReader(
                 ("test(a,b).") + "\n" +
            ("test(A,A) :- A is 1.") + "\n" +
            ("test(a,c).") + "\n" +
            ("test(z,9).") + "\n" +
            ("test(a,e)."));
         return s;
   }
}