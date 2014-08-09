package nars.test.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import static java.lang.String.valueOf;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.TruthValue;
import nars.io.Symbols;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import static nars.io.Symbols.NativeOperator.STATEMENT_CLOSER;
import static nars.io.Symbols.NativeOperator.STATEMENT_OPENER;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;
import org.ahmadsoft.ropes.Rope;
import org.ahmadsoft.ropes.impl.AbstractRope;
import org.ahmadsoft.ropes.impl.ConcatenationRope;
import org.ahmadsoft.ropes.impl.ReverseRope;
import org.ahmadsoft.ropes.impl.SubstringRope;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestRope {
 
    public final static class PrePostCharRope extends AbstractRope {

	private final char pre, post;
        private final Rope content;
        byte depth;
        private final int localhash;

        public PrePostCharRope(char pre, char post, Rope content) {
            this.pre = pre;
            this.post = post;
            this.content = content;
            this.localhash = Objects.hash(pre,post);
            this.depth = TestRope.depth(content);
        }
        
	@Override
	public char charAt(final int index) {
            if (index == 0) return pre;
            if (index == content.length()+2-1) return post;
            return content.charAt(index-1);
	}

	@Override
	public byte depth() {
            return depth;
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	@Override
	public int indexOf(final char ch) {
            if (ch == pre) return 0;
            
            int c = content.indexOf(ch);
            if (c!=-1)
                return c+1;
            
            if (ch == post) 
                return content.length()+1;
            
            return -1;            
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	@Override
	public int indexOf(final char ch, final int fromIndex) {
            if (fromIndex < 1) return indexOf(ch);
            if (fromIndex < content.length()) {
                int c = content.indexOf(ch, fromIndex-1);
                if (c!=-1)
                    return c+1;
            }
            
            if (ch == post) 
                return content.length()+1;
            
            return -1;
	}

	/*
	 * Implementation Note: This is a reproduction of the AbstractRope
	 * indexOf implementation. Calls to charAt have been replaced
	 * with direct array access to improve speed.
	 */
	@Override
	public int indexOf(final CharSequence sequence, final int fromIndex) {
            return -1;
	}

	@Override
	public Iterator<Character> iterator(final int start) {
            if (start < 0 || start > this.length())
                    throw new IndexOutOfBoundsException("Rope index out of range: " + start);

            return new Iterator<Character>() {
                int current = start;
                @Override public boolean hasNext() {
                    return this.current < length();
                }

                @Override public Character next() {
                    return charAt(current++);
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException("Rope iterator is read-only.");
                }
            };
	}

        @Override
        public int hashCode() {
            return content.hashCode() + localhash;
        }        
        
	@Override
	public int length() {
            return content.length()+2;
	}

	@Override
	public Rope reverse() {
            return new ReverseRope(this);
	}

	@Override
	public Iterator<Character> reverseIterator(final int start) {
            return null;
	}

	@Override
	public Rope subSequence(final int start, final int end) {
//		if (start == 0 && end == this.length())
//			return this;
//		if (end - start < 16) {
//			return new FlatCharArrayRope(this.sequence, start, end-start);
//		} else {
//			return new SubstringRope(this, start, end-start);
//		}
            return null;
	}

	@Override
	public String toString() {
            return pre + content.toString() + post;
	}


	public String toString(final int offset, final int length) {
		//return new String(this.sequence, offset, length);
            return null;
	}

	@Override
	public void write(final Writer out) throws IOException {
		//this.write(out, 0, this.length());
            return;
	}

	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException {
		//out.write(this.sequence, offset, length);
            return;
	}
    }

    
    public static Rope cat(CharSequence... c) {
        Rope r = null;
        for (CharSequence a : c) {
            if (!(a instanceof Rope))
                a = Rope.BUILDER.build(a);
                
            if (r == null) {
                r = (Rope)a;
            }
            else {                
                r = new ConcatenationRope(r, (Rope)a);
            }
        }
        return r;
    }
    
    public static CharSequence toString(Term term) {
        if (term instanceof Statement) {
            Statement s = (Statement)term;
            /*
            Rope r = cat(
                valueOf(STATEMENT_OPENER.ch),
                toString(s.getSubject()),
                valueOf(' '),
                s.operator().toString(),
                valueOf(' '),
                toString(s.getPredicate()),
                valueOf(STATEMENT_CLOSER.ch));
            */
            
            return new PrePostCharRope(STATEMENT_OPENER.ch, STATEMENT_CLOSER.ch, cat(
                toString(s.getSubject()),
                valueOf(' '),
                s.operator().toString(),
                valueOf(' '),
                toString(s.getPredicate())
            ));
        }
        else if (term instanceof CompoundTerm) {
            CompoundTerm ct = (CompoundTerm)term;
            
            Rope[] tt = new Rope[ct.term.length];
            int i = 0;
            for (final Term t : ct.term) {
                tt[i++] = cat(String.valueOf(Symbols.ARGUMENT_SEPARATOR), toString(t));
            }
            
            Rope ttt = cat(tt);
            return cat(String.valueOf(COMPOUND_TERM_OPENER.ch), ct.operator().toString(), ttt, String.valueOf(COMPOUND_TERM_CLOSER.ch));

        }
        else
            return term.toString();
    }
    
    @Test
    public void testRope() {
        NAR n = new DefaultNARBuilder().build();
        
        String term1String ="<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
        Term term1 = n.term(term1String);

        Rope tr = (Rope)toString(term1);
        
        //visualize(tr, System.out);
        
        Sentence s = new Sentence(term1, '.', new TruthValue(1,1), new Stamp(n.memory));
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	private static final short MAX_ROPE_DEPTH = 96;
	private static final String SPACES = "                                                                                                                                                                                                        ";

	

	/**
	 * Returns the depth of the specified rope.
	 * @param r the rope.
	 * @return the depth of the specified rope.
	 */
	public static byte depth(final Rope r) {
		if (r instanceof AbstractRope) {
			return ((AbstractRope)r).depth();
		} else {
			return 0;
			//throw new IllegalArgumentException("Bad rope");
		}
	}

	

	/**
	 * Visualize a rope.
	 * @param r
	 * @param out
	 */
	void visualize(final Rope r, final PrintStream out) {
		this.visualize(r, out, (byte) 0);
	}

	public void visualize(final Rope r, final PrintStream out, final int depth) {
		if (r instanceof SubstringRope) {
			out.print(SPACES.substring(0,depth*2));
			out.println("substring " + r.length() + " \"" + r + "\"");
//			this.visualize(((SubstringRope)r).getRope(), out, depth+1);
		}
                else if (r instanceof ConcatenationRope) {
			out.print(SPACES.substring(0,depth*2));
			out.println("<");
			this.visualize(((ConcatenationRope)r).getLeft(), out, depth+1);
			out.print(SPACES.substring(0,depth*2));
			out.println(">");
			this.visualize(((ConcatenationRope)r).getRight(), out, depth+1);
		}
                else if (r instanceof PrePostCharRope) {
                    PrePostCharRope p = (PrePostCharRope)r;
			out.print(SPACES.substring(0,depth*2));
			out.println("\'" + p.pre + "\'");
                        this.visualize(p.content, out, depth+1);
                        out.println("\'" + p.post + "\'");
                }
                else  {
			out.print(SPACES.substring(0,depth*2));
			out.println("\"" + r + "\""); // (" + r.getClass());
//			out.println(r.length());
		}
	}
	
	public void stats(final Rope r, final PrintStream out) {
		int nonLeaf=0;
		final ArrayList<Rope> leafNodes = new ArrayList<Rope>();
		final ArrayDeque<Rope> toExamine = new ArrayDeque<Rope>();
		// begin a depth first loop.
		toExamine.add(r);
		while (toExamine.size() > 0) {
			final Rope x = toExamine.pop();
			if (x instanceof ConcatenationRope) {
				++nonLeaf;
				toExamine.push(((ConcatenationRope) x).getRight());
				toExamine.push(((ConcatenationRope) x).getLeft());
			} else {
				leafNodes.add(x);
			}
		}
		out.println("rope(length=" + r.length() + ", leaf nodes=" + leafNodes.size() + ", non-leaf nodes=" + nonLeaf + ", depth=" + depth(r) + ")");
	}
    
}
