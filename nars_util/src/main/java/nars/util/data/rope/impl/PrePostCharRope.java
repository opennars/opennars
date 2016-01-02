package nars.util.data.rope.impl;

import nars.util.data.rope.Rope;

import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;

/**
 * Nearly complete implementation of Rope that surrounds some content with a
 * character prefix and suffix. For example it can be used to represent: '(' +
 * 'text' + ')'
 */
public final class PrePostCharRope extends AbstractRope {

    public final char pre;
    public final char post;
    public final Rope content;
    public final byte depth;
    private final int localhash;

    public PrePostCharRope(char pre, char post, Rope content) {
        this.pre = pre;
        this.post = post;
        this.content = content;
        localhash = Objects.hash(pre, post);
        depth = Rope.depth(content);
    }

    @Override
    public char charAt(int index) {
        if (index == 0) {
            return pre;
        }
        if (index == content.length() + 2 - 1) {
            return post;
        }
        return content.charAt(index - 1);
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
    public int indexOf(char ch) {
        if (ch == pre) {
            return 0;
        }
        int c = content.indexOf(ch);
        if (c != -1) {
            return c + 1;
        }
        if (ch == post) {
            return content.length() + 1;
        }
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(char ch, int fromIndex) {
        if (fromIndex < 1) {
            return indexOf(ch);
        }
        if (fromIndex < content.length()) {
            int c = content.indexOf(ch, fromIndex - 1);
            if (c != -1) {
                return c + 1;
            }
        }
        if (ch == post) {
            return content.length() + 1;
        }
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(CharSequence sequence, int fromIndex) {
        return -1;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return new Iterator<Character>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return current < length();
            }

            @Override
            public Character next() {
                return charAt(current++);
            }

            @Override
            public void remove() {
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
        return content.length() + 2;
    }

    @Override
    public Rope reverse() {
        return new ReverseRope(this);
    }

    @Override
    public Iterator<Character> reverseIterator(int start) {
        return null;
    }

    @Override
    public Rope subSequence(int start, int end) {
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

    public String toString(int offset, int length) {
        //return new String(this.sequence, offset, length);
        return null;
    }

    @Override
    public void write(Writer out) {
        //this.write(out, 0, this.length());
    }

    @Override
    public void write(Writer out, int offset, int length) {
        //out.write(this.sequence, offset, length);
    }
}
