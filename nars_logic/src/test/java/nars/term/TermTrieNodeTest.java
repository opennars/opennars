//package nars.term;
//
//import nars.NAR;
//import nars.nar.Default;
//import org.junit.Test;
//import org.magnos.trie.Trie;
//import org.magnos.trie.TrieMatch;
//import org.magnos.trie.Tries;
//
//import static org.junit.Assert.assertEquals;
//
//
//
///**
// * Created by me on 7/8/15.
// */
//public class TermTrieNodeTest  {
//
//    static final NAR n = new Default();
//
//    static Term t(String s) { return n.term(s); }
//
//    @Test public void testByteTrie() {
//        Trie<byte[], String> mapper = Tries.forBytes();
//        mapper.setDefaultMatch( TrieMatch.EXACT );
//
//
//
//
//// Given an IP, get the host name
//
//
//        String[] terms = {
//                "<a --> b>", "<a --> c>", "<a --> d>", "<b --> d>", "<b --> <a --> c>>"
//        };
//        int p = 0;
//        for (String s : terms)
//                mapper.put( t(s).bytes(), Integer.toString(p++) );
//
//
//        System.out.println(mapper.values());
//        System.out.println(mapper.nodes);
//
//        //mapper.put( t.getAddress(), "goodfsdfgle.com" );
//
//        assertEquals("0", mapper.get(t(terms[0]).bytes()));
//    }
////
////    @Test
////    public void testPatriciaTree() {
////        //https://code.google.com/p/patricia-trie/wiki/Examples
////        // Trie of First Name -> Person
////        PatriciaTrie<String, Integer> trie = new PatriciaTrie<String, Integer>(StringKeyAnalyzer.INSTANCE);
////        trie.put("<a --> b>", 1);
////        trie.put("<a --> c>", 2);
////        trie.put("<a --> <b --> d>>", 3);
////        trie.put("<a --> <b --> c>>", 4);
////
////
////// Returns Alex
////        TrieEntry<String, Integer> entry = trie.select("Al");
////        //System.out.println(entry);
////
////        //System.out.println(trie.toString());
////
////        //trie.print(System.out);
////    }
//
////    @Test @Ignore
////    public void testInsertionAndRemoval() {
////        // Insert a bunch of key/value pairs.
////        TermTrieNode<Concept> trieMap = new TermTrieNode();
////
////        String[] terms = new String[] {
////                "<a --> b>", "<a --> c>", "<a --> d>", "<b --> d>", "<b --> <a --> c>>"
////        };
////
////
////        for (String t : terms)
////            trieMap.put(n.memory.conceptualize(n.term(t), new Budget(1f, 1f, 1f)));
////
////        System.out.println(trieMap);
////
////        for (String t : terms) {
////            Concept ab = trieMap.get(n.term(t).bytes());
////            assertEquals(t, ab.toString());
////        }
////
////        final Term ab = n.term(terms[0]);
////        Object removed = trieMap.remove(ab.bytes());
////        assertNotNull(removed);
////
////        System.out.println(trieMap);
////
////
////        assertNull( "should be empty, having just removed it",
////                trieMap.put(n.memory.conceptualize(ab, new Budget(1f, 1f, 1f)) ));
////
////        System.out.println(trieMap);
////
////        //assertNotNull("reinsert",
////                trieMap.put(n.memory.conceptualize(ab, new Budget(1f, 1f, 1f)));
////
////        System.out.println(trieMap);
////
////        assertTrue( trieMap.contains(ab.bytes()) );
////
////
//////        trieMap.put("123", "456");
//////        trieMap.put("Java", "rocks");
//////        trieMap.put("Melinda", "too");
//////        trieMap.put("Moo", "cow"); // Will collide with "Melinda".
//////        trieMap.put("Moon", "walk"); // Collides with "Melinda" and turns "Moo" into a prefix.
//////        trieMap.put("", "Root"); // You can store one value at the empty key if you like.
//////
//////        // Test for inserted, nonexistent, and deleted keys.
//////        System.out.println("123 = " + trieMap.get("123"));
//////        System.out.println("Java = " + trieMap.get("Java"));
//////        System.out.println("Melinda = " + trieMap.get("Melinda"));
//////        System.out.println("Moo = " + trieMap.get("Moo"));
//////        System.out.println("Moon = " + trieMap.get("Moon"));
//////        System.out.println("Mo = " + trieMap.get("Mo")); // Should return null.
//////        System.out.println("Empty key = " + trieMap.get("")); // Should return "Root".
//////        System.out.println("Moose = " + trieMap.get("Moose")); // Never added so should return null.
//////        System.out.println("Nothing = " + trieMap.get("Nothing")); // Ditto.
//////        trieMap.put("123", null); // Removes this leaf entry.
//////        System.out.println("After removal, 123 = " + trieMap.get("123")); // Should now return null.
//////        trieMap.put("Moo", null); // Removes this prefix entry. (Special case to test internal logic).
//////        System.out.println("After removal, Moo = " + trieMap.get("Moo")); // Should now return null.
//////        trieMap.put("Moon", null); // Internal test of branch pruning.
////    }
//
// }