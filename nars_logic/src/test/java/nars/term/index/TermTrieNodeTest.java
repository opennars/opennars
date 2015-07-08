package nars.term.index;

import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nar.Default;
import org.junit.Test;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by me on 7/8/15.
 */
public class TermTrieNodeTest  {


    @Test
    public void testInsertionAndRemoval() {
        // Insert a bunch of key/value pairs.
        TermTrieNode<Concept> trieMap = new TermTrieNode();

        String[] terms = new String[] {
                "<a --> b>", "<a --> c>", "<a --> d>", "<b --> d>", "<b --> <a --> c>>"
        };

        NAR n = new NAR( new Default() );
        for (String t : terms)
            trieMap.put(n.memory.conceptualize(n.term(t), new Budget(1f, 1f, 1f)));

        trieMap.print(System.out);

        for (String t : terms) {
            Concept ab = trieMap.get(n.term(t).bytes());
            assertEquals(t, ab.toString());
        }



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