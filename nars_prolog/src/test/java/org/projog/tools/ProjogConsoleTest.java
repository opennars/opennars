package org.projog.tools;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static java.lang.System.lineSeparator;
import static org.junit.Assert.assertEquals;
import static org.projog.TestUtils.COMPILATION_ENABLED_PROPERTIES;
import static org.projog.TestUtils.writeToTempFile;

@Ignore
public class ProjogConsoleTest {
   private static final String ERROR_MESSAGE = "Invalid. Enter ; to continue or q to quit. ";
   private static final String PROMPT = "?- ";
   private static final String EXPECTED_HEADER = concatenate("[31966667] INFO Reading prolog source in: projog-bootstrap.pl from classpath", "Projog Console", "www.projog.org", "");
   private static final String EXPECTED_FOOTER = "" + lineSeparator() + PROMPT;
   private static final String QUIT_COMMAND = concatenate("quit.");
   private static final String YES = "yes (0 ms)";
   private static final String NO = "no (0 ms)";

   @BeforeClass
   public static void setUp() {
      // this will ensure the required output directory exists when the tests are run
      COMPILATION_ENABLED_PROPERTIES.getRuntimeCompilationOutputDirectory();
   }

   @Test
   public void testTrue() throws IOException {
      String input = createInput("true.");
      String expected = createExpectedOutput(PROMPT, YES);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   @Test
   public void testFail() throws IOException {
      String input = createInput("fail.");
      String expected = createExpectedOutput(PROMPT, NO);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   @Test
   public void testSingleVariable() throws IOException {
      String input = createInput("X = y.");
      String expected = createExpectedOutput(PROMPT, "X = y", "", YES);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   @Test
   public void testMultipleVariables() throws IOException {
      String input = createInput("W=X, X=1+1, Y is W, Z is -W.");
      String expected = createExpectedOutput(PROMPT, "W = 1 + 1", "X = 1 + 1", "Y = 2", "Z = -2", "", YES);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   @Test
   public void testInvalidSyntax() throws IOException {
      String input = createInput("X is 1 + 1");
      String expected = createExpectedOutput(PROMPT, "Error parsing query:", "Unexpected end of stream", "X is 1 + 1", "          ^");
      String actual = getOutput(input);
      compare(expected, actual);
   }

   /** Test inputting {@code ;} to continue evaluation and {@code q} to quit, plus validation of invalid input. */
   @Test
   public void testRepeat() throws IOException {
      String input = createInput("repeat.", ";", ";", "z", "", "qwerty", "q");
      String expected = createExpectedOutput(PROMPT, YES, YES, YES + ERROR_MESSAGE + ERROR_MESSAGE + ERROR_MESSAGE);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   /** Tests {@code trace} functionality using query against terms input using {@code consult}>. */
   @Test
   public void testConsultAndTrace() throws IOException {
      File tempFile = createFileToConsult("test(a).", "test(b).", "test(c).");
      String path = tempFile.getPath();
      String input = createInput("consult('" + path + "').", "trace.", "test(X).", ";", ";");
      String expected = createExpectedOutput(PROMPT + "[THREAD-ID] INFO Reading prolog source in: " + path + " from file system", "", YES, "", PROMPT, YES, "", PROMPT + "[THREAD-ID] CALL test( X )", "[THREAD-ID] EXIT test( a )", "", "X = a", "", YES + "[THREAD-ID] REDO test( a )", "[THREAD-ID] EXIT test( b )", "", "X = b", "", YES + "[THREAD-ID] REDO test( b )", "[THREAD-ID] EXIT test( c )", "", "X = c", "", YES);
      String actual = getOutput(input);
      compare(expected, actual);
   }

   private File createFileToConsult(String... lines) throws IOException {
      return writeToTempFile(this.getClass(), concatenate(lines));
   }

   private String createInput(String... lines) {
      return concatenate(lines) + QUIT_COMMAND;
   }

   private String createExpectedOutput(String... lines) {
      return EXPECTED_HEADER + concatenate(lines) + EXPECTED_FOOTER;
   }

   private String getOutput(String input) throws IOException {
      try (ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes()); ByteArrayOutputStream os = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(os)) {
         final ProjogConsole c = new ProjogConsole(is, ps);
         c.run(new ArrayList<String>());
         return os.toString();
      }
   }

   private void compare(String expected, String actual) {
      String tidiedExpected = makeSuitableForComparison(expected);
      String tidiedActual = makeSuitableForComparison(actual);
      assertEquals(tidiedExpected, tidiedActual);
   }

   /**
    * Output from the console application is unpredictable - some information returned (that is incidental to the core
    * functionality) will vary between multiple executions of the same query against the same knowledge base. In order
    * to check the actual input meets our expectations we first need to "tidy it" to remove inconsistencies (i.e. thread
    * IDs and timings).
    */
   private String makeSuitableForComparison(String in) {
      return replaceTimings(replaceThreadId(in));
   }

   /**
    * Return a version of the input with the thread IDs removed.
    * <p>
    * Output sometimes includes thread IDs contained in square brackets. e.g.: <code>[31966667]</code>
    */
   private String replaceThreadId(String in) {
      return in.replaceAll("\\[\\d*\\]", "[THREAD-ID]");
   }

   /**
    * Return a version of the input with the timings removed.
    * <p>
    * Output sometimes contains info on how long a query took to execute. e.g.: <code>(15 ms)</code>
    */
   private String replaceTimings(String in) {
      return in.replaceAll("\\(\\d* ms\\)", "(n ms)");
   }

   private static String concatenate(String... lines) {
      final StringBuilder result = new StringBuilder();
      for (final String line : lines) {
         result.append(line);
         result.append(lineSeparator());
      }
      return result.toString();
   }
}
