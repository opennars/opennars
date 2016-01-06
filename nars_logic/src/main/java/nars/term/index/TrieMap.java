package nars.term.index;

/**
 * Implements very fast dictionary storage and retrieval. Only depends upon the
 * core String class.
 * 
 * @author Melinda Green - © 2010 Superliminal Software. Free for all uses with
 *         attribution.
 */
public class TrieMap {
	/*
	 * Implementation of a trie tree. (see http://en.wikipedia.org/wiki/Trie)
	 * though I made it faster and more compact for long key strings by building
	 * tree nodes only as needed to resolve collisions. Each letter of a key is
	 * the index into the following array. Values stored in the array are either
	 * a Leaf containing the user's value or another TrieMap node if more than
	 * one key shares the key prefix up to that point. Null elements indicate
	 * unused, I.E. available slots.
	 */
	private final Object[] mChars = new Object[256];
	private Object mPrefixVal; // Used only for values of prefix keys.

	// Simple container for a string-value pair.
	private static class Leaf {
		public final String mStr;
		public final Object mVal;
		public Leaf(String str, Object val) {
			mStr = str;
			mVal = val;
		}
	}

	public boolean isEmpty() {
        if(mPrefixVal != null) {
            return false;
        }
        for(Object o : mChars) {
            if(o != null) {
                return false;
            }
        }
        return true;
    }
	/**
	 * Inserts a key/value pair.
	 * 
	 * @param key
	 *            may be empty or contain low-order chars 0..255 but must not be
	 *            null.
	 * @param val
	 *            Your data. Any data class except another TrieMap. Null values
	 *            erase entries.
	 */
	public void put(String key, Object val) {
        assert key != null;
        assert !(val instanceof TrieMap); // Only we get to store TrieMap nodes. TODO: Allow it.
        if(key.isEmpty()) {
            // All of the original key's chars have been nibbled away 
            // which means this node will store this key as a prefix of other keys.
            mPrefixVal = val; // Note: possibly removes or updates an item.
            return;
        }
        char c = key.charAt(0);
        Object cObj = mChars[c];
        if(cObj == null) { // Unused slot means no collision so just store and return;
            if(val == null) {
                return; // Don't create a leaf to store a null value.
            }
            mChars[c] = new Leaf(key, val);
            return;
        }
        if(cObj instanceof TrieMap) {
            // Collided with an existing sub-branch so nibble a char and recurse.
            TrieMap childTrie = (TrieMap)cObj;
            childTrie.put(key.substring(1), val);
            if(val == null && childTrie.isEmpty()) {
                mChars[c] = null; // put() must have erased final entry so prune branch.
            }
            return;
        }
        // Collided with a leaf 
        if(val == null) {
            mChars[c] = null; // Null value means to remove any previously stored value.
            return;
        }
        assert cObj instanceof Leaf;
        // Sprout a new branch to hold the colliding items.
        Leaf cLeaf = (Leaf)cObj;
        TrieMap branch = new TrieMap();
        branch.put(key.substring(1), val); // Store new value in new subtree.
        branch.put(cLeaf.mStr.substring(1), cLeaf.mVal); // Plus the one we collided with.
        mChars[c] = branch;
    }
	/**
	 * Retrieve a value for a given key or null if not found.
	 */
	public Object get(String key) {
        TrieMap other = this;
        while (true) {
            assert key != null;
            if (key.isEmpty()) {
                // All of the original key's chars have been nibbled away
                // which means this key is a prefix of another.
                return other.mPrefixVal;
            }
            char c = key.charAt(0);
            Object cVal = other.mChars[c];
            if (cVal == null) {
                return null; // Not found.
            }
            assert cVal instanceof Leaf || cVal instanceof TrieMap;
            if (cVal instanceof TrieMap) { // Hash collision. Nibble first char, and recurse.
                key = key.substring(1);
                other = ((TrieMap) cVal);
                continue;
            }
            if (cVal instanceof Leaf) {
                // cVal contains a user datum, but does the key match its substring?
                Leaf cPair = (Leaf) cVal;
                if (key.equals(cPair.mStr)) {
                    return cPair.mVal; // Return user's data value.
                }
            }
            return null; // Not found.
        }
    }
	/**
	 * Simple example test program.
	 */
	public static void main(String[] args) {
		// Insert a bunch of key/value pairs.
		TrieMap trieMap = new TrieMap();
		trieMap.put("123", "456");
		trieMap.put("Java", "rocks");
		trieMap.put("Melinda", "too");
		trieMap.put("Moo", "cow"); // Will collide with "Melinda".
		trieMap.put("Moon", "walk"); // Collides with "Melinda" and turns "Moo"
										// into a prefix.
		trieMap.put("", "Root"); // You can store one value at the empty key if
									// you like.

		// Test for inserted, nonexistent, and deleted keys.
		System.out.println("123 = " + trieMap.get("123"));
		System.out.println("Java = " + trieMap.get("Java"));
		System.out.println("Melinda = " + trieMap.get("Melinda"));
		System.out.println("Moo = " + trieMap.get("Moo"));
		System.out.println("Moon = " + trieMap.get("Moon"));
		System.out.println("Mo = " + trieMap.get("Mo")); // Should return null.
		System.out.println("Empty key = " + trieMap.get("")); // Should return
																// "Root".
		System.out.println("Moose = " + trieMap.get("Moose")); // Never added so
																// should return
																// null.
		System.out.println("Nothing = " + trieMap.get("Nothing")); // Ditto.
		trieMap.put("123", null); // Removes this leaf entry.
		System.out.println("After removal, 123 = " + trieMap.get("123")); // Should
																			// now
																			// return
																			// null.
		trieMap.put("Moo", null); // Removes this prefix entry. (Special case to
									// test internal logic).
		System.out.println("After removal, Moo = " + trieMap.get("Moo")); // Should
																			// now
																			// return
																			// null.
		trieMap.put("Moon", null); // Internal test of branch pruning.
	}
}