package org.projog.tools;

import static org.projog.core.KnowledgeBaseUtils.QUESTION_PREDICATE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.Set;

import org.projog.api.Projog;
import org.projog.api.QueryResult;
import org.projog.api.QueryStatement;
import org.projog.core.ProjogException;
import org.projog.core.event.ProjogEvent;
import org.projog.core.parser.ParserException;
import org.projog.core.term.PTerm;

/**
 * Command line interface to Prolog.
 * <p>
 * Provides a mechanism for users to interact with Projog via a read-evaluate-print loop (REPL).
 * <p>
 * <img src="doc-files/ProjogConsole.png">
 */
public class ProjogConsole implements Observer {
   /** Command user can enter to exit the console application. */
   private static final String QUIT_COMMAND = "quit.";
   private static final String CONTINUE_EVALUATING = ";";
   private static final String STOP_EVALUATING = "q";

   private final Scanner in;
   private final PrintStream out;
   private final Projog projog;

   ProjogConsole(InputStream in, PrintStream out) {
      this.in = new Scanner(in);
      this.out = out;
      this.projog = new Projog(this);
   }

   void run(List<String> startupScriptFilenames) throws IOException {
      out.println("Projog Console");
      out.println("www.projog.org");

      consultScripts(startupScriptFilenames);

      while (true) {
         printPrompt();

         String inputSyntax = in.nextLine();
         if (QUIT_COMMAND.equals(inputSyntax)) {
            return;
         } else if (isNotEmpty(inputSyntax)) {
            parseAndExecute(inputSyntax);
         }
      }
   }

   private void printPrompt() {
      out.println();
      out.print(QUESTION_PREDICATE_NAME + " ");
   }

   private static boolean isNotEmpty(String input) {
      return input.trim().length() > 0;
   }

   /**
    * Observer method that informs user of events generated during the evaluation of goals.
    */
   @Override
   public void update(Observable o, Object arg) {
      ProjogEvent event = (ProjogEvent) arg;
      Object source = event.getSource();
      String id = source == null ? "?" : Integer.toString(source.hashCode());
      out.println("[" + id + "] " + event.getType() + " " + event.getMessage());
   }

   private void consultScripts(List<String> scriptFilenames) {
      for (String startupScriptName : scriptFilenames) {
         consultScript(startupScriptName);
      }
   }

   private void consultScript(String startupScriptName) {
      try {
         File startupScriptFile = new File(startupScriptName);
         projog.consultFile(startupScriptFile);
      } catch (Throwable e) {
         out.println();
         processThrowable(e);
      }
   }

   private void parseAndExecute(String inputSyntax) {
      try {
         QueryStatement s = projog.query(inputSyntax);
         QueryResult r = s.get();
         Set<String> variableIds = r.getVariableIds();
         while (evaluateOnce(r, variableIds) && shouldContinue()) {
            // keep evaluating the query
         }
         out.println();
      } catch (ParserException pe) {
         out.println();
         out.println("Error parsing query:");
         pe.getDescription(out);
      } catch (Throwable e) {
         out.println();
         processThrowable(e);
         projog.printProjogStackTrace(e);
      }
   }

   private boolean shouldContinue() {
      while (true) {
         String input = in.nextLine();
         if (CONTINUE_EVALUATING.equals(input)) {
            return true;
         } else if (STOP_EVALUATING.equals(input)) {
            return false;
         } else {
            out.print("Invalid. Enter ; to continue or q to quit. ");
         }
      }
   }

   private void processThrowable(Throwable e) {
      if (e instanceof ParserException) {
         ParserException pe = (ParserException) e;
         out.println("ParserException at line: " + pe.getLineNumber());
         pe.getDescription(out);
      } else if (e instanceof ProjogException) {
         out.println(e.getMessage());
         Throwable cause = e.getCause();
         if (cause != null) {
            processThrowable(cause);
         }
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append("Caught: ");
         sb.append(e.getClass().getName());
         StackTraceElement ste = e.getStackTrace()[0];
         sb.append(" from class: ");
         sb.append(ste.getClassName());
         sb.append(" method: ");
         sb.append(ste.getMethodName());
         sb.append(" line: ");
         sb.append(ste.getLineNumber());
         out.println(sb);
         String message = e.getMessage();
         if (message != null) {
            out.println("Description: " + message);
         }
      }
   }

   /** Returns {@code true} if {@code QueryResult} can be re-tried */
   private boolean evaluateOnce(QueryResult r, Set<String> variableIds) {
      long start = System.currentTimeMillis();
      boolean success = r.next();
      if (success) {
         printVariableAssignments(r, variableIds);
      }
      printOutcome(success, System.currentTimeMillis() - start);
      return success && !r.isExhausted();
   }

   private void printVariableAssignments(QueryResult r, Set<String> variableIds) {
      if (!variableIds.isEmpty()) {
         out.println();
         for (String variableId : variableIds) {
            PTerm answer = r.getTerm(variableId);
            String s = projog.toString(answer);
            out.println(variableId + " = " + s);
         }
      }
   }

   private void printOutcome(boolean success, long timing) {
      out.println();
      out.print(success ? "yes" : "no");
      out.print(" (");
      out.print(timing);
      out.print(" ms)");
   }

   public static void main(String[] args) throws IOException {
      ArrayList<String> startupScriptFilenames = new ArrayList<>();
      for (String arg : args) {
         if (arg.startsWith("-")) {
            System.out.println();
            System.out.println("don't know about argument: " + arg);
            System.exit(-1);
         }
         startupScriptFilenames.add(arg);
      }

      ProjogConsole console = new ProjogConsole(System.in, System.out);
      console.run(startupScriptFilenames);
   }
}