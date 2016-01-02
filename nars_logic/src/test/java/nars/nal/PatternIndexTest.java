package nars.nal;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.$;
import nars.Global;
import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import org.junit.Test;
import org.zhz.dfargx.MatchedText;
import org.zhz.dfargx.RegexSearcher;

import java.util.List;

/**
 * Created by me on 12/25/15.
 */
public class PatternIndexTest {

    public static class CompoundRegex {
        final String regex;
        final Compound term;
        final ObjectIntHashMap<Term> atomics = new ObjectIntHashMap<>();
        final Term[] index;
        private final RegexSearcher matcher;

        /** sequence in which matchable variables are encountered.
         * may contain duplicates in which case a discrepency
         * can fail the match.
         */
        public final List<Variable> variableSequence =
                Global.newArrayList();

        public CompoundRegex(Compound c) {
            StringBuilder sb = new StringBuilder(c.volume() * 2 /* estimate */);

            append(sb, c);

            this.term = c;
            this.regex = sb.toString();

            index = new Term[atomics.size()];
            atomics.forEachKeyValue( (k, v) -> {
                index[v] = k;
            });

            matcher = new RegexSearcher(regex);
        }

        public void print() {
            char[] c = regex.toCharArray();
            System.out.print("term: " + term + ", regex(" + regex.length() + "): ");
            for (char x : c) {
                //System.out.print((int)x + " ");
                System.out.print(x);
            }
            System.out.println("\n");
            for (int i = 0; i < index.length; i++) {
                System.out.println("\t\t" + i + " = " + index[i]);
            }
            //System.out.println("\t" + matcher);
        }

        /** appends regex for terms, recursively, to a buffer */
        public void append(StringBuilder sb, Compound c) {
            //structure hash
            //sb.append( (char)c.structure()  ) //TODO needs special regex opcode to compare since it's not just equals

            sb.append( (char)c.op().ordinal() );
            if (c.hasEllipsis()) {
                sb.append('?'); //any size , TODO min bounds
            } else {
                sb.append( 'a' + c.size() ); //specific size
            }
            if (c.op().isImage()) {
                sb.append( '0' + c.relation());
            }

            sb.append(' ');

            for (Term x : c.terms()) {

                if (x.op() == Op.VAR_PATTERN) {
                    appendVariable(sb, (Variable)x);
                }
                else if (x instanceof Compound) {
                    append(sb, (Compound)x);
                } else {
                    appendAtom(sb, x);
                }
            }

            sb.append(' ');
        }

        private void appendVariable(StringBuilder sb, Variable x) {
            int id = atomics.getIfAbsentPut(x, atomics.size());
            //sb.append((char)x.op().ordinal());
            //sb.append((char)id);
            //sb.append("\\s(\\S+)\\s"); //non-whitespace surrounded by one whitespace chars
            sb.append("\\W*(\\w+)\\W*"); //non-whitespace surrounded by one whitespace chars
            //sb.append(' ');
            variableSequence.add(x);
        }

        private void appendAtom(StringBuilder sb, Term x) {
            int id = atomics.getIfAbsentPut(x, atomics.size());
            sb.append('a'+ x.op().ordinal());
            sb.append('A' + id);
            sb.append(' ');
        }
    }

    @Test
    public void testCompoundRegex() {
        CompoundRegex X = new CompoundRegex($.$("<%1-->%2>"));
        CompoundRegex Y = new CompoundRegex($.$("<a-->b>"));

        //TreePrinter.getInstance().printTree(new SyntaxTree(X.regex).getRoot());


        X.print();
        Y.print();
        X.matcher.search(Y.regex);
        while (X.matcher.hasMoreElements()) {
            MatchedText e = X.matcher.nextElement();
            System.out.println(e);
        }

    }

}