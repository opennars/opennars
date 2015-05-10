package org.projog.example;

import java.io.File;

import org.projog.api.Projog;
import org.projog.api.QueryResult;
import org.projog.api.QueryStatement;
import org.projog.core.term.Atom;

public class ProjogExample {
   public static void main(String[] args) {
      Projog p = new Projog();
      p.consultFile(new File("test.pl"));
      QueryStatement s1 = p.query("test(X,Y).");
      QueryResult r1 = s1.get();
      while (r1.next()) {
         System.out.println("X = " + r1.getTerm("X") + " Y = " + r1.getTerm("Y"));
      }
      QueryResult r2 = s1.get();
      r2.setTerm("X", new Atom("d"));
      while (r2.next()) {
         System.out.println("Y = " + r2.getTerm("Y"));
      }

      QueryStatement s2 = p.query("testRule(X).");
      QueryResult r3 = s2.get();
      while (r3.next()) {
         System.out.println("X = " + r3.getTerm("X"));
      }

      QueryStatement s3 = p.query("test(X, Y), Y<3.");
      QueryResult r4 = s3.get();
      while (r4.next()) {
         System.out.println("X = " + r4.getTerm("X") + " Y = " + r4.getTerm("Y"));
      }
   }
}