package nars.util.data.id;

import nars.util.utf8.Utf8;

import java.io.*;

import static nars.util.utf8.Utf8.trim;


/**
 * Generic abstract identifier for symbols, tags, and other identifiables.
 * It is responsible for maintaining efficient time and space equality,
 * lexicographic ordering, and hashing of representations of
 * tokens / symbols / identifiers.
 *
 * can generate the following representations:
 *      --pretty String output ( default behavior of .toString() )
 *      --compact String output (ex: does not include spaces for readability, newlines, etc.)
 *      --internal byte[] - most compact and does not need to be human readable
 *
 * reducing the size of internal representations does not only save
 * memory but it is faster to compare and hash, and may hash with
 * better entropy.
 */
abstract class Identifier implements Serializable {

    /** produces the character array by invoking the append()
     *  method that certain subclasses use to form their
     *  data lazily/dynamically.  this is guaranteed to work for all
     *  but may not be the most efficient - subclasses
     *  are free to provide more efficient ones for their cases.
     *  @param pretty - whether to include spaces and other whitespace-like formatting
     *  @return character array which may need trimmed for trailing zero '\0' characters
     */
    public char[] charsFromWriter(boolean pretty) {
        CharArrayWriter caw = new EfficientCharArrayWriter(charsEstimated());
        try {
            append(caw, pretty);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trim(caw.toCharArray());
    }

    /** use this if the input is constant and already known (static).
     * avoids calling the writer and just decodes the utf8 representation directly
     * this is why it does not use the 'pretty' parameter because it would have
     * no effect
     * */
    public char[] charsByName() {
        return Utf8.fromUtf8ToChars(bytes());
    }


    /** use this when this class must generate an output by a writer
     *  this is the default general implementation.  */
    public char[] chars(boolean pretty) {
        return charsFromWriter(pretty);
    }


    public void append(OutputStream o, boolean pretty) throws IOException {
        append(new PrintWriter(o), pretty);
    }

    /**
     * implementations should call Writer.append() sometimes instead of Writer.write()
     * to avoid allocating a temporary buffer
     */
    public abstract void append(Appendable p, boolean pretty) throws IOException;


    @Override
    public abstract boolean equals(Object x);


     /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public String toString(boolean pretty) {
        char[] c = chars(pretty);
        return new String(c);
    }



    /** inefficient and potentially circularly recursive
     *  override either bytes() or chars() in subclasses please */
    public byte[] bytes() {
        System.err.println(this + " wasteful String generation");
        return Utf8.toUtf8(String.valueOf(chars(false)));
    }




    @Override
    public String toString() {
        return Utf8.fromUtf8toString(bytes());

    }

    abstract int charsEstimated();

    /** frees all associated memory */
    public abstract void delete();

    /** WARNING the toCharArray() result may need trimmed for trailing zero's */
    private static class EfficientCharArrayWriter extends CharArrayWriter {

        public EfficientCharArrayWriter(int initialSize) {
            super(initialSize);
        }

        @Override
        public char[] toCharArray() {
            if (size() == buf.length)
                return buf;
            return super.toCharArray();
        }
    }
}
