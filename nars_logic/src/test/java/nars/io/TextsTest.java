package nars.io;

import nars.util.Texts;
import nars.util.data.rope.Rope;
import nars.util.data.rope.impl.FastConcatenationRope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TextsTest {


    /**
     * Half-way between a String and a Rope; concatenates a list of strings into an immutable CharSequence which is either:
     * If a component is null, it is ignored.
     * if total non-null components is 0, returns null
     * if total non-null components is 1, returns that component.
     * if the combined length <= maxLen, creates a StringBuilder appending them all.
     * if the combined length > maxLen, creates a Rope appending them all.
     * <p>
     * TODO do not allow a StringBuilder to appear in output, instead wrap in CharArrayRope
     */
    public static CharSequence yarn(int maxLen, CharSequence... components) {
        int totalLen = 0;
        int total = 0;
        CharSequence lastNonNull = null;
        for (CharSequence s : components) {
            if (s != null) {
                totalLen += s.length();
                total++;
                lastNonNull = s;
            }
        }
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            return lastNonNull.toString();
        }

        if ((totalLen <= maxLen) || (maxLen == -1)) {
            /*
            final CharBuffer n = CharBuffer.allocate(totalLen);

            for (final CharSequence s : components) {
                n.append(s);
            }

            return n.compact();
            */


            StringBuilder sb = new StringBuilder(totalLen);
            for (CharSequence s : components) {
                if (s != null) {
                    sb.append(s);
                }
            }
            return Rope.sequence(sb);
        } else {
            Rope r = Rope.catFast(components);
            return r;
        }
    }

    @Test
    public void testN2() {
        
        assertEquals("1.0", Texts.n2(1.00f).toString());
        assertEquals(".50", Texts.n2(0.5f).toString());
        assertEquals(".09", Texts.n2(0.09f).toString());
        assertEquals(".10", Texts.n2(0.1f).toString());
        assertEquals(".01", Texts.n2(0.009f).toString());
        assertEquals("0.0", Texts.n2(0.001f).toString());
        assertEquals(".01", Texts.n2(0.01f).toString());
        assertEquals("0.0", Texts.n2(0.0f).toString());
        
        
    }

    
//    public static CharSequence toString(Term term) {
//        if (term instanceof Statement) {
//            Statement s = (Statement)term;
//            /*
//            Rope r = cat(
//                valueOf(STATEMENT_OPENER.ch),
//                toString(s.getSubject()),
//                valueOf(' '),
//                s.operate().toString(),
//                valueOf(' '),
//                toString(s.getPredicate()),
//                valueOf(STATEMENT_CLOSER.ch));
//            */
//
//            return new PrePostCharRope(STATEMENT_OPENER, STATEMENT_CLOSER, Rope.cat(
//                toString(s.getSubject()),
//                valueOf(' '),
//                s.op().toString(),
//                valueOf(' '),
//                toString(s.getPredicate())
//            ));
//        }
//        else if (term instanceof Compound) {
//            Compound ct = (Compound)term;
//
//            Rope[] tt = new Rope[ct.length()];
//            int i = 0;
//            for (final Term t : ct.term) {
//                tt[i++] = Rope.cat(String.valueOf(Symbols.ARGUMENT_SEPARATOR), toString(t));
//            }
//
//            Rope ttt = Rope.cat(tt);
//            return Rope.cat(String.valueOf(COMPOUND_TERM_OPENER), ct.op().toString(), ttt, String.valueOf(COMPOUND_TERM_CLOSER));
//
//        }
//        else
//            return term.toString();
//    }
//
//    @Test
//    public void testRope() throws Narsese.NarseseException {
//        NAR n = new Default();
//
//        String term1String ="<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
//        Term term1 = n.term(term1String);
//
//        Rope tr = (Rope)toString(term1);
//
//        //visualize(tr, System.out);
//
//        //Sentence s = new Sentence(term1, '.', new DefaultTruth(1,1), new Stamper(n.memory, Tense.Eternal));
//
//    }
    
    @Test
    public void testRopeStringEqual() {
        //careful: String <-> Rope comparisons are not commutive because String only 
        //equals other String
        //so do not mix String and FastCharSequenceRope in the same collection

        String s = "x";
        Rope r = (Rope)Rope.rope("x");        
        assertTrue(!s.equals(r));        
        assertTrue(!r.equals(s));
        
        Rope rF = (Rope)Rope.rope("x");
        assertTrue(!s.equals(rF));
        assertTrue(!rF.equals(s));
        
        assertTrue(rF.equals(r));
    }
    
    
    @Test
    public void testFastConcat() {
        String t1 = "abcdefg";
        String t2 = "|vjxkcjvlcjxkv";
        String t3 = "nvksdlfksjdkf";
        
        CharSequence r12 = yarn(1, t1, t2);
        CharSequence s12 = yarn(1, t1, t2);
        Rope s123 = (Rope) yarn(1, t1, t2, t3);
        Rope r123 = (Rope) yarn(1, t1, t2, t3);
        Rope s13 = (Rope) yarn(1, t1, null, t3);
        Rope r13 = (Rope) yarn(1, t1, null, t3);
        
        assertTrue(r12 instanceof FastConcatenationRope);
        
        assertTrue(r12!=s12);
        assertEquals(r12,s12);
        assertEquals(r12.hashCode(), s12.hashCode());
        
        assertTrue(r123!=s123);
        assertEquals(r123,s123);
        assertEquals(r123.hashCode(), s123.hashCode());
        assertTrue(r123.compareTo(s123)==0);
        
        assertTrue(r13!=s13);
        assertEquals(r13,s13);
        assertEquals(r13.hashCode(), s13.hashCode());
        assertTrue(r13.compareTo(s13)==0);
        assertTrue(r13.compareTo(s123)!=0);
        
        assertTrue( ! yarn(1,"aa", "bb").equals( yarn(1, "aabb","") ) );
        assertTrue( ! yarn(1,"aa", "bb", null).equals( yarn(1, "aa","bb","cc") ) );
        assertTrue(   yarn(1,"aa", "bb", null).equals( yarn(1, "aa","bb",null) ) );
        assertTrue( ! yarn(1,"aa", null, "bb").equals( yarn(1, "aa","bb") ) );
        assertTrue( ! yarn(1,"aa", null, "bb").equals( yarn(1, "aa","bb") ) );
        assertTrue( ! yarn(1,"aa", "x", "bb").equals( yarn(1, "aa", null, "bb") ) );

    }    

    
}
