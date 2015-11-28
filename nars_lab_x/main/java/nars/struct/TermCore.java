package nars.struct;

import nars.Op;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieMatch;
import org.magnos.trie.TrieSequencerByteArray;

import java.nio.ByteBuffer;

/**
 * Termcept memory block
 */
public class TermCore {

    final Trie<byte[], Integer> index = new Trie(new TrieSequencerByteArray());

    final ByteBuffer terms;

    /** size of a termcept in this core */
    final int s;

    int nextFree = 0;

    final ThreadLocal<TermCept> cept = ThreadLocal.withInitial(() -> {
       return new TermCept();
    });
    final ThreadLocal<TermSpect> spect = ThreadLocal.withInitial(() -> {
        return new TermSpect();
    });

    public final TermCept cept() { return cept.get(); }
    public final TermSpect spect() { return spect.get(); }

    public TermCore(int bytes) {
        //terms = ByteBuffer.allocateDirect(bytes);
        terms = ByteBuffer.allocate(bytes);

        s = new TermCept().size();
    }

    public int allocateNext() {
        //TODO use a bitvector map of the memory but for now just increment the address
        return nextFree++;
    }

    public int get(final byte[] name) {
        Integer a = index.get(name, TrieMatch.EXACT);
        if (a == null) {
            a = allocateNext();
            index.put(name, a);
            cept().set(this, a).name.set(name);
        }
        return a;
    }

    public TermCept cept(final byte[] name) {
        int a = get(name);
        return cept().set(this, get(name));
    }

    public TermSpect term(final byte[] name, Op op) {
        return spect().the(this, get(name), op);
    }

}
