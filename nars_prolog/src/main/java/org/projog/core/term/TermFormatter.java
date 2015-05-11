package org.projog.core.term;

import static org.projog.core.KnowledgeBaseUtils.getOperands;

import org.projog.core.KB;
import org.projog.core.Operands;

/**
 * Produces {@code String} representations of {@link PTerm} instances.
 * <p>
 * Does take account of operator precedence.
 * 
 * @see #toString(PTerm)
 */
public class TermFormatter {
   private final Operands operands;

   public TermFormatter(KB kb) {
      this(getOperands(kb));
   }

   public TermFormatter(Operands operands) {
      this.operands = operands;
   }

   /**
    * Returns a string representation of the specified {@code Term}.
    * <p>
    * This method does take account of current operator declarations - thus an infix operator will be printed out
    * between it's arguments. This method represents lists as a comma separated sequence of elements enclosed in square
    * brackets.
    * <p>
    * For example:
    * 
    * <pre>
    * Term structure = Structure.createStructure("+", new IntegerNumber(1), new IntegerNumber(2));
    * Term list = ListFactory.create(new Term[]{new Atom("a"), Atom("b"), Atom("c")});
    * System.out.println("Structure.toString():      "+structure.toString());
    * System.out.println("Write.toString(structure): "+write.toString(structure));
    * System.out.println("List.toString():           "+list.toString());
    * System.out.println("Write.toString(list):      "+write.toString(list));
    * </pre>
    * would print out:
    * 
    * <pre>
    * Structure.toString():      +(1, 2)
    * Write.toString(structure): 1 + 2
    * List.toString():           .(a, .(b, .(c, [])))
    * Write.toString(list):      [a,b,c]
    * </pre>
    * 
    * @param t the {@code Term} to represent as a string
    * @return a string representation of the specified {@code Term}
    */
   public String toString(PTerm t) {
      StringBuilder sb = new StringBuilder();
      write(t, sb);
      return sb.toString();
   }

   private void write(PTerm t, StringBuilder sb) {
      switch (t.type()) {
         case STRUCTURE:
            writePredicate(t, sb);
            break;
         case LIST:
            writeList(t, sb);
            break;
         case EMPTY_LIST:
            sb.append("[]");
            break;
         case NAMED_VARIABLE:
            sb.append(((PVar) t).getId());
            break;
         default:
            sb.append(t.toString());
      }
   }

   private void writeList(PTerm p, StringBuilder sb) {
      sb.append('[');
      PTerm head = p.term(0);
      PTerm tail = p.term(1);
      write(head, sb);
      PTerm list;
      while ((list = getList(tail)) != null) {
         sb.append(',');
         write(list.term(0), sb);
         tail = list.term(1);
      }

      if (tail.type() != PrologOperator.EMPTY_LIST) {
         sb.append('|');
         write(tail, sb);
      }
      sb.append(']');
   }

   private static PTerm getList(PTerm t) {
      if (t.type() == PrologOperator.LIST) {
         return t;
      } else {
         return null;
      }
   }

   private void writePredicate(PTerm p, StringBuilder sb) {
      if (isInfixOperator(p)) {
         writeInfixOperator(p, sb);
      } else if (isPrefixOperator(p)) {
         writePrefixOperator(p, sb);
      } else if (isPostfixOperator(p)) {
         writePostfixOperator(p, sb);
      } else {
         writeNonOperatorPredicate(p, sb);
      }
   }

   private boolean isInfixOperator(PTerm t) {
      return t.type() == PrologOperator.STRUCTURE && t.terms().length == 2 && operands.infix(t.getName());
   }

   private void writeInfixOperator(PTerm p, StringBuilder sb) {
      PTerm[] args = p.terms();
      write(args[0], sb);
      sb.append(' ').append(p.getName()).append(' ');
      // if second argument is an infix operand then add brackets around it so:
      //  ?-(,(fail, ;(fail, true)))
      // appears as:
      //  ?- fail , (fail ; true)
      // not:
      //  ?- fail , fail ; true
      if (isInfixOperator(args[1]) && isEqualOrLowerPriority(p, args[1])) {
         sb.append('(');
         writeInfixOperator(args[1], sb);
         sb.append(')');
      } else {
         write(args[1], sb);
      }
   }

   private boolean isEqualOrLowerPriority(PTerm p1, PTerm p2) {
      return operands.getInfixPriority(p1.getName()) <= operands.getInfixPriority(p2.getName());
   }

   private boolean isPrefixOperator(PTerm t) {
      return t.type() == PrologOperator.STRUCTURE && t.terms().length == 1 && operands.prefix(t.getName());
   }

   private void writePrefixOperator(PTerm p, StringBuilder sb) {
      sb.append(p.getName()).append(' ');
      write(p.terms()[0], sb);
   }

   private boolean isPostfixOperator(PTerm t) {
      return t.type() == PrologOperator.STRUCTURE && t.terms().length == 1 && operands.postfix(t.getName());
   }

   private void writePostfixOperator(PTerm p, StringBuilder sb) {
      write(p.terms()[0], sb);
      sb.append(' ').append(p.getName());
   }

   private void writeNonOperatorPredicate(PTerm p, StringBuilder sb) {
      String name = p.getName();
      PTerm[] args = p.terms();
      sb.append(name);
      sb.append("(");
      for (int i = 0; i < args.length; i++) {
         if (i != 0) {
            sb.append(", ");
         }
         write(args[i], sb);
      }
      sb.append(")");
   }
}
