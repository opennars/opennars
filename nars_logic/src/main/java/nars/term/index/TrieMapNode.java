package nars.term.index;

import com.gs.collections.impl.map.mutable.primitive.ByteObjectHashMap;
import nars.NAR;
import nars.nar.Default;
import nars.term.Term;
import nars.util.data.id.Named;

import java.io.PrintStream;
import java.util.Arrays;

/** from: http://www.superliminal.com/sources/TrieMap.java.html */
public class TrieMapNode<V extends Named<byte[]>> extends ByteObjectHashMap {
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


    public TrieMapNode() {
        this(4);
    }

    public TrieMapNode(int branch) {
        super(branch);
    }

    @Override
    public boolean isEmpty() {
        if(value != null) {
            return false;
        }
        return super.isEmpty();
    }

    public void put(byte[] key, Object val) {
        put(key, val, 0);
    }

    public void put(final V v) {
        put(v.name(), v);
    }

    /**
     * Inserts a key/value pair.
     * 
     * @param key may be empty or contain low-order chars 0..255 but must not be null.
     * @param val Your data. Any data class except another TrieMap. Null values erase entries.
     */
    protected void put(byte[] key, Object val, int offset) {
        assert key != null;
        assert !(val instanceof TrieMapNode); // Only we get to store TrieMap nodes. TODO: Allow it.
        if(key.length-offset == 0) {
            // All of the original key's chars have been nibbled away 
            // which means this node will store this key as a prefix of other keys.
            value = (V)val; // Note: possibly removes or updates an item.
            return;
        }
        final byte c = key[offset];
        Object cObj = get(c);
        if(cObj == null) { // Unused slot means no collision so just store and return;
            if(val == null) {
                return; // Don't create a leaf to store a null value.
            }
            put(c, val);
            return;
        }
        if(cObj instanceof TrieMapNode) {
            // Collided with an existing sub-branch so nibble a char and recurse.
            TrieMapNode childTrie = (TrieMapNode)cObj;
            childTrie.put(key, val, offset+1);
            if(val == null && childTrie.isEmpty()) {
                // put() must have erased final entry so prune branch.
                remove(c);
            }
            return;
        }
        // Collided with a leaf 
        if(val == null) {
            remove(c);
            return;
        }
        //assert cObj instanceof Leaf;

        // Sprout a new branch to hold the colliding items.

        V cLeaf = (V)cObj;
        TrieMapNode branch = new TrieMapNode();
        branch.put(suffix(key, offset+1), val); // Store new value in new subtree.

        final byte[] cleafname = cLeaf.name();
        branch.put(suffix(cleafname, offset+1), cLeaf); // Plus the one we collided with.

        put(c, branch);
    }

    public static byte[] suffix(final byte[] x, int n) {
        return Arrays.copyOfRange(x, 1, x.length);
    }


    public V get(byte[] key) {
        return (V)get(key, 0);
    }
    /**
     * Retrieve a value for a given key or null if not found.
     */
    protected Object get(byte[] key, int offset) {
        assert key != null;
        if(key.length-offset == 0) {
            // All of the original key's chars have been nibbled away 
            // which means this key is a prefix of another.
            return value;
        }

        Object cVal = get(key[offset]);
        if(cVal == null) {
            return null; // Not found.
        }
        //assert cVal instanceof Leaf || cVal instanceof TrieMapNode;
        if(cVal instanceof TrieMapNode) { // Hash collision. Nibble first char, and recurse.
            return ((TrieMapNode)cVal).get(key, offset+1);
        }
        else {
            // cVal contains a user datum, but does the key match its substring?
            V cPair = (V)cVal;
            if(nars.util.utf8.Utf8.equals2(key, cPair.name())) {
                return cPair; // Return user's data value.
            }
        }
        return null; // Not found.
    }

    public void print(PrintStream out) {
        out.println(value);
        out.println("\t" + this);

    }
    
    /**
     * Simple example test program.
     */
    public static void main(String[] args) {
        // Insert a bunch of key/value pairs.
        TrieMapNode<Term> trieMap = new TrieMapNode();

        NAR n = new NAR( new Default() );
        trieMap.put(n.term("<a --> b>"));
        trieMap.put(n.term("<a --> c>"));

        trieMap.print(System.out);

//        trieMap.put("123", "456");
//        trieMap.put("Java", "rocks");
//        trieMap.put("Melinda", "too");
//        trieMap.put("Moo", "cow"); // Will collide with "Melinda".
//        trieMap.put("Moon", "walk"); // Collides with "Melinda" and turns "Moo" into a prefix.
//        trieMap.put("", "Root"); // You can store one value at the empty key if you like.
//
//        // Test for inserted, nonexistent, and deleted keys.
//        System.out.println("123 = " + trieMap.get("123"));
//        System.out.println("Java = " + trieMap.get("Java"));
//        System.out.println("Melinda = " + trieMap.get("Melinda"));
//        System.out.println("Moo = " + trieMap.get("Moo"));
//        System.out.println("Moon = " + trieMap.get("Moon"));
//        System.out.println("Mo = " + trieMap.get("Mo")); // Should return null.
//        System.out.println("Empty key = " + trieMap.get("")); // Should return "Root".
//        System.out.println("Moose = " + trieMap.get("Moose")); // Never added so should return null.
//        System.out.println("Nothing = " + trieMap.get("Nothing")); // Ditto.
//        trieMap.put("123", null); // Removes this leaf entry.
//        System.out.println("After removal, 123 = " + trieMap.get("123")); // Should now return null.
//        trieMap.put("Moo", null); // Removes this prefix entry. (Special case to test internal logic).
//        System.out.println("After removal, Moo = " + trieMap.get("Moo")); // Should now return null.
//        trieMap.put("Moon", null); // Internal test of branch pruning.
    }


}