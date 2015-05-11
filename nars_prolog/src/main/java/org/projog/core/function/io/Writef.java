package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;
import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;
import static org.projog.core.term.EmptyList.EMPTY_LIST;
import static org.projog.core.term.ListUtils.toJavaUtilList;
import static org.projog.core.term.TermUtils.toLong;

import java.util.List;

import org.projog.core.Calculatables;
import org.projog.core.FileHandles;
import org.projog.core.KB;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.ListUtils;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;
import org.projog.core.term.TermUtils;

/* TEST
 %QUERY writef('%s%n %t%r', [[h,e,l,l,o], 44, world, !, 3])
 %OUTPUT hello, world!!!
 %ANSWER/
 
 %QUERY writef('.%7l.\n.%7l.\n.%7l.\n.%7l.\n.%7l.', [a, abc, abcd, abcdefg, abcdefgh])
 %OUTPUT
 % .a      .
 % .abc    .
 % .abcd   .
 % .abcdefg.
 % .abcdefgh.
 %OUTPUT
 %ANSWER/ 

 %QUERY writef('.%7r.\n.%7r.\n.%7r.\n.%7r.\n.%7r.', [a, abc, abcd, abcdefg, abcdefgh])
 %OUTPUT
 % .      a.
 % .    abc.
 % .   abcd.
 % .abcdefg.
 % .abcdefgh.
 %OUTPUT
 %ANSWER/ 

 %QUERY writef('.%7c.\n.%7c.\n.%7c.\n.%7c.\n.%7c.', [a, abc, abcd, abcdefg, abcdefgh])
 %OUTPUT
 % .   a   .
 % .  abc  .
 % . abcd  .
 % .abcdefg.
 % .abcdefgh.
 %OUTPUT
 %ANSWER/ 

 %QUERY writef('%w %d', [1+1, 1+1])
 %OUTPUT 1 + 1 +(1, 1)
 %ANSWER/
 
 %QUERY writef('\%\%%q\\\\\r\n\u0048',[abc])
 %OUTPUT
 % %%abc\\
 % H
 %OUTPUT
 %ANSWER/
 
 % Note: calling writef with only 1 argument is the same as calling it with an empty list for the second argument:
 %QUERY writef('\u0048\u0065\u006C\u006c\u006F', [])
 %OUTPUT Hello
 %ANSWER/
 %QUERY writef('\u0048\u0065\u006C\u006c\u006F')
 %OUTPUT Hello
 %ANSWER/ 
 */
/**
 * <code>writef(X,Y)</code> - writes formatted text to the output stream.
 * <p>
 * The first argument is an atom representing the text to be output. The text can contain special character sequences
 * which specify formatting and substitution rules.
 * </p>
 * <p>
 * Supported special character sequences are:
 * <table>
 * <tr>
 * <th>Sequence</th>
 * <th>Action</th>
 * </tr>
 * <tr>
 * <td>\n</td>
 * <td>Output a 'new line' character (ASCII code 10).</td>
 * </tr>
 * <tr>
 * <td>\l</td>
 * <td>Same as <code>\n</code>.</td>
 * </tr>
 * <tr>
 * <td>\r</td>
 * <td>Output a 'carriage return' character (ASCII code 13).</td>
 * </tr>
 * <tr>
 * <td>\t</td>
 * <td>Output a tab character (ASCII code 9).</td>
 * </tr>
 * <tr>
 * <td>\\</td>
 * <td>Output the <code>\</code> character.</td>
 * </tr>
 * <tr>
 * <td>\%</td>
 * <td>Output the <code>%</code> character.</td>
 * </tr>
 * <tr>
 * <td>\\u<i>NNNN</i></td>
 * <td>Output the unicode character represented by the hex digits <i>NNNN</i>.</td>
 * </tr>
 * <tr>
 * <td>%t</td>
 * <td>Output the next term - in same format as <code>write/1</code>.</td>
 * </tr>
 * <tr>
 * <td>%w</td>
 * <td>Same as <code>\t</code>.</td>
 * </tr>
 * <tr>
 * <td>%q</td>
 * <td>Same as <code>\t</code>.</td>
 * </tr>
 * <tr>
 * <td>%p</td>
 * <td>Same as <code>\t</code>.</td>
 * </tr>
 * <tr>
 * <td>%d</td>
 * <td>Output the next term - in same format as <code>write_canonical/1</code>.</td>
 * </tr>
 * <tr>
 * <td>%f</td>
 * <td>Ignored (only included to support compatibility with other Prologs).</td>
 * </tr>
 * <tr>
 * <td>%s</td>
 * <td>Output the elements contained in the list represented by the next term.</td>
 * </tr>
 * <tr>
 * <td>%n</td>
 * <td>Output the character code of the next term.</td>
 * </tr>
 * <tr>
 * <td>%r</td>
 * <td>Write the next term <i>N</i> times, where <i>N</i> is the value of the second term.</td>
 * </tr>
 * <tr>
 * <td>%<i>N</i>c</td>
 * <td>Write the next term centered in <i>N</i> columns.</td>
 * </tr>
 * <tr>
 * <td>%<i>N</i>l</td>
 * <td>Write the next term left-aligned in <i>N</i> columns.</td>
 * </tr>
 * <tr>
 * <td>%<i>N</i>r</td>
 * <td>Write the next term right-aligned in <i>N</i> columns.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * <code>writef(X)</code> produces the same results as <code>writef(X, [])</code>.
 * </p>
 */
public final class Writef extends AbstractSingletonPredicate {
   private TermFormatter termFormatter;
   private FileHandles fileHandles;
   private Calculatables calculatables;

   @Override
   protected void init() {
      KB KB = getKB();
      termFormatter = getTermFormatter(KB);
      fileHandles = getFileHandles(KB);
      calculatables = getCalculatables(KB);
   }

   @Override
   public boolean evaluate(PTerm atom) {
      return evaluate(atom, EMPTY_LIST);
   }

   @Override
   public boolean evaluate(PTerm atom, PTerm list) {
      final String text = TermUtils.getAtomName(atom);
      final List<PTerm> args = toJavaUtilList(list);
      if (args == null) {
         return false;
      }

      final StringBuilder sb = format(text, args);
      print(sb);

      return true;
   }

   private StringBuilder format(final String text, final List<PTerm> args) {
      final Formatter f = new Formatter(text, args, termFormatter);
      while (f.hasMore()) {
         final int c = f.pop();
         if (c == '%') {
            parsePercentEscapeSequence(f);
         } else if (c == '\\') {
            parseSlashEscapeSequence(f);
         } else {
            f.writeChar(c);
         }
      }
      return f.output;
   }

   private void parsePercentEscapeSequence(final Formatter f) {
      final int next = f.pop();
      if (next == 'f') {
         // flush - not supported, so ignore
         return;
      }

      final PTerm arg = f.nextArg();
      final String output;
      switch (next) {
         case 't':
         case 'w':
         case 'q':
         case 'p':
            output = f.format(arg);
            break;
         case 'n':
            long charCode = toLong(calculatables, arg);
            output = Character.toString((char) charCode);
            break;
         case 'r':
            long timesToRepeat = toLong(calculatables, f.nextArg());
            output = repeat(f.format(arg), timesToRepeat);
            break;
         case 's':
            output = concat(f, arg);
            break;
         case 'd':
            // Write the term, ignoring operators.
            output = arg.toString();
            break;
         default:
            f.rewind();
            output = align(f, arg);
      }
      f.writeString(output);
   }

   private String repeat(final String text, long timesToRepeat) {
      StringBuilder sb = new StringBuilder();
      for (long i = 0; i < timesToRepeat; i++) {
         sb.append(text);
      }
      return sb.toString();
   }

   private String concat(final Formatter f, final PTerm t) {
      List<PTerm> l = ListUtils.toJavaUtilList(t);
      if (l == null) {
         throw new IllegalArgumentException("Expected list but got: " + t);
      }
      StringBuilder sb = new StringBuilder();
      for (PTerm e : l) {
         sb.append(f.format(e));
      }
      return sb.toString();
   }

   private String align(final Formatter f, final PTerm t) {
      String s = f.format(t);
      int actualWidth = s.length();
      int requiredWidth = parseNumber(f);
      int diff = Math.max(0, requiredWidth - actualWidth);
      int alignmentChar = f.pop();
      switch (alignmentChar) {
         case 'l':
            return s + getWhitespace(diff);
         case 'r':
            return getWhitespace(diff) + s;
         case 'c':
            String prefix = getWhitespace(diff / 2);
            String suffix = diff % 2 == 0 ? prefix : prefix + " ";
            return prefix + s + suffix;
         default:
            throw new IllegalArgumentException("? " + alignmentChar);
      }
   }

   private String getWhitespace(int diff) {
      String s = "";
      for (int i = 0; i < diff; i++) {
         s += " ";
      }
      return s;
   }

   private int parseNumber(Formatter f) {
      int next = 0;
      while (isNumber(f.peek())) {
         next = (next * 10) + (f.pop() - '0');
      }
      return next;
   }

   private void parseSlashEscapeSequence(final Formatter f) {
      final int next = f.pop();
      final int output;
      switch (next) {
         case 'l':
         case 'n':
            output = '\n';
            break;
         case 'r':
            output = '\r';
            break;
         case 't':
            output = '\t';
            break;
         case '\\':
            output = '\\';
            break;
         case '%':
            output = '%';
            break;
         case 'u':
            output = parseUnicode(f);
            break;
         default:
            throw new IllegalArgumentException("? " + next);
      }
      f.writeChar(output);
   }

   private int parseUnicode(final Formatter f) {
      final StringBuilder sb = new StringBuilder();
      sb.append((char) f.pop());
      sb.append((char) f.pop());
      sb.append((char) f.pop());
      sb.append((char) f.pop());
      return Integer.parseInt(sb.toString(), 16);
   }

   private boolean isNumber(int c) {
      return c >= '0' && c <= '9';
   }

   private void print(final StringBuilder sb) {
      fileHandles.getCurrentOutputStream().print(sb);
   }

   private static class Formatter {
      final StringBuilder output = new StringBuilder();
      final char[] chars;
      final List<PTerm> args;
      final TermFormatter termFormatter;
      int charIdx;
      int argIdx;

      Formatter(String text, List<PTerm> args, TermFormatter termFormatter) {
         this.chars = text.toCharArray();
         this.args = args;
         this.termFormatter = termFormatter;
      }

      public void rewind() {
         charIdx--;
      }

      PTerm nextArg() {
         return args.get(argIdx++);
      }

      String format(PTerm t) {
         return termFormatter.toString(t);
      }

      int peek() {
         if (hasMore()) {
            return chars[charIdx];
         } else {
            return -1;
         }
      }

      int pop() {
         int c = peek();
         charIdx++;
         return c;
      }

      boolean hasMore() {
         return charIdx < chars.length;
      }

      void writeChar(int c) {
         output.append((char) c);
      }

      void writeString(String s) {
         output.append(s);
      }
   }
}
