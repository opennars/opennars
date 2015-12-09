package nars.util.data.rope.impl;

/**
 * Variation on FlatCharSequenceRope that sacrifices the ability to compare
 * content for speed. It can only safely be compared (equals() and hashCode())
 * with other FlatCharSequenceRope. So it will behave as expected only if you do
 * not compare against other kinds of ropes.
 *
 * Be careful not to use StringBuilder or StringBuffer as the sequence; its
 * equals and hashcode are not functional.
 */
public class FastCharSequenceRope extends FlatCharSequenceRope {

    public FastCharSequenceRope(CharSequence c) {
        super(c);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FastCharSequenceRope) {
            return sequence.equals(((FastCharSequenceRope) other).sequence);
        }
        return false;
    }

}
