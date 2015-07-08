package nars.term.index;

import com.gs.collections.impl.map.mutable.primitive.ByteObjectHashMap;
import nars.term.Termed;
import nars.util.utf8.Utf8;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * from: http://www.superliminal.com/sources/TrieMap.java.html
 */
public class TermTrieNode<V extends Termed> extends ByteObjectHashMap<TermTrieNode<V>> {
    /*
     * Implementation of a trie tree. (see http://en.wikipedia.org/wiki/Trie)
     * though I made it faster and more compact for long key strings 
     * by building tree nodes only as needed to resolve collisions.
     * Each letter of a key is the index into the following array.
     * Values stored in the array are either a Leaf containing the user's value or
     * another TrieMap node if more than one key shares the key prefix up to that point.
     * Null elements indicate unused, I.E. available slots.
     */
    private V value; // Used only for values of prefix keys.
    byte[] prefix;


    public TermTrieNode() {
        this(4);
    }

    public TermTrieNode(int branch) {
        super(branch);
    }

    public TermTrieNode(byte[] prefix, V v) {
        this();
        value = v;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return (prefix != null ? Utf8.fromUtf8(prefix) : "<>") + value + ":" + super.toString();
    }

    @Override
    public boolean isEmpty() {
        if (value != null) {
            return false;
        }
        return super.isEmpty();
    }

/*    public void put(byte[] key, Object val) {
        put(key, val, 0);
    }*/

    public void put(final V v) {
        put(v.getTerm().bytes(), v);
    }

    /**
     * Inserts a key/value pair.
     *
     * @param key may be empty or contain low-order chars 0..255 but must not be null.
     * @param val Your data. Any data class except another TrieMap. Null values erase entries.
     */
    public void put(byte[] key, Object val) {
        assert key != null;
        assert !(val instanceof TermTrieNode); // Only we get to store TrieMap nodes. TODO: Allow it.
        if (key.length == 0) {
            // All of the original key's chars have been nibbled away 
            // which means this node will store this key as a prefix of other keys.
            value = (V) val; // Note: possibly removes or updates an item.
            return;
        }
        final byte c = key[0];
        Object cObj = get(c);
        if (cObj == null) { // Unused slot means no collision so just store and return;
            if (val == null) {
                return; // Don't create a leaf to store a null value.
            }
            put(c, new TermTrieNode(key, (V) val));
            return;
        }
        if (cObj instanceof TermTrieNode) {
            // Collided with an existing sub-branch so nibble a char and recurse.
            TermTrieNode childTrie = (TermTrieNode) cObj;
            childTrie.put(suffix(key, 1), val);
            if (val == null && childTrie.isEmpty()) {
                // put() must have erased final entry so prune branch.
                remove(c);
            }
            return;
        }
        // Collided with a leaf 
        if (val == null) {
            remove(c);
            return;
        }
        //assert cObj instanceof Leaf;

        // Sprout a new branch to hold the colliding items.

        V cLeaf = (V) cObj;
        TermTrieNode branch = new TermTrieNode();
        branch.put(suffix(key, 1), val); // Store new value in new subtree.

        final byte[] cleafname = cLeaf.getTerm().bytes();
        branch.put(suffix(cleafname, 1), cLeaf); // Plus the one we collided with.

        put(c, branch);
    }

    public static byte[] suffix(final byte[] x, int n) {
        return Arrays.copyOfRange(x, 1, x.length);
    }



    /*public V get(byte[] key) {
        return (V)get(key, 0);
    }*/

    /**
     * Retrieve a value for a given key or null if not found.
     */
    public V get(byte[] key) {
        assert key != null;
        if (key.length == 0) {
            // All of the original key's chars have been nibbled away 
            // which means this key is a prefix of another.
            return value;
        }

        Object cVal = get(key[0]);
        if (cVal == null) {
            return null; // Not found.
        }
        //assert cVal instanceof Leaf || cVal instanceof TrieMapNode;
        // cVal contains a user datum, but does the key match its substring?
        TermTrieNode cPair = (TermTrieNode) cVal;
        if (nars.util.utf8.Utf8.equals2(key, cPair.prefix)) {
            return (V) cPair.value; // Return user's data value.
        }

        if (cVal instanceof TermTrieNode) { // Hash collision. Nibble first char, and recurse.
            return (V)((TermTrieNode) cVal).get(suffix(key, 1));
        }


        return null; // Not found.
    }

    public void print(PrintStream out) {
        out.println(value);
        out.println("\t" + this);

    }


}