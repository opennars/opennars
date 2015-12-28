package nars.term;

import nars.$;
import nars.Op;
import nars.term.compound.Compound;
import org.junit.Test;

import static nars.$.*;
import static org.junit.Assert.*;

/**
 * Created by me on 12/10/15.
 */
public class TermReductionsTest {

    final Term p = $("P"), q = $("Q"), r = $("R"), s = $("S");

    @Test
    public void testIntersectExtReduction1() {
        // (&,R,(&,P,Q)) = (&,P,Q,R)
        assertEquals("(&,P,Q,R)", sect(r, sect(p, q)).toString());
        assertEquals("(&,P,Q,R)", $("(&,R,(&,P,Q))").toString());
    }
    @Test public void testIntersectExtReduction2() {
        // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
        assertEquals("(&,P,Q,R,S)", sect(sect(p, q), sect(r, s)).toString());
        assertEquals("(&,P,Q,R,S)", $("(&,(&,P,Q),(&,R,S))").toString());
    }
    @Test public void testIntersectExtReduction3() {
        // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
        assertEquals("(&,P,Q,R,S,T,U)", $("(&,(&,P,Q),(&,R,S), (&,T,U))").toString());
    }
    @Test public void testIntersectExtReduction2_1() {
        // (&,R,(&,P,Q)) = (&,P,Q,R)
        assertEquals("(&,P,Q,R)", $("(&,R,(&,P,Q))").toString());
    }
    @Test public void testIntersectExtReduction4() {
        //UNION if (term1.op(Op.SET_INT) && term2.op(Op.SET_INT)) {
        assertEquals("{P,Q,R,S}", sect(sete(p, q), sete(r, s)).toString());
        assertEquals("{P,Q,R,S}", $("(&,{P,Q},{R,S})").toString());
        assertEquals(null, sect(seti(p, q), seti(r, s)));

    }

    @Test public void testIntersectIntReduction1() {
        // (|,R,(|,P,Q)) = (|,P,Q,R)
        assertEquals("(|,P,Q,R)", sectInt(r, sectInt(p, q)).toString());
        assertEquals("(|,P,Q,R)", $("(|,R,(|,P,Q))").toString());
    }
    @Test public void testIntersectIntReduction2() {
        // (|,(|,P,Q),(|,R,S)) = (|,P,Q,R,S)
        assertEquals("(|,P,Q,R,S)", sectInt(sectInt(p, q), sectInt(r, s)).toString());
        assertEquals("(|,P,Q,R,S)", $("(|,(|,P,Q),(|,R,S))").toString());
    }
    @Test public void testIntersectIntReduction3() {
        // (|,R,(|,P,Q)) = (|,P,Q,R)
        assertEquals("(|,P,Q,R)", $("(|,R,(|,P,Q))").toString());
    }
    @Test public void testIntersectIntReduction4() {
        //UNION if (term1.op(Op.SET_INT) || term2.op(Op.SET_INT)) {
        assertEquals("[P,Q,R,S]", sectInt(seti(p, q), seti(r, s)).toString());
        assertEquals("[P,Q,R,S]", $("(|,[P,Q],[R,S])").toString());

    }
    @Test public void testIntersectIntReductionToZero() {
        assertEquals(null, $("(|,{P,Q},{R,S})"));
    }

    @Test public void testIntersectIntReduction_to_one() {
        assertEquals("<robin-->bird>", $("<robin-->(|,bird)>").toString());
        assertEquals("<robin-->bird>", $("<(|,robin)-->(|,bird)>").toString());
    }


    @Test public void testInvalidEquivalences() {
        assertEquals("<P<=>Q>", equiv(p, q).toString() );

        assertNull(equiv( impl(p, q), r) );
        assertNull(equiv( equiv(p, q), r) );
        assertNull(equiv( equivAfter(p, q), r) );
        assertNull($("<<a <=> b> <=> c>"));
    }

    @Test public void testReducedAndInvalidImplications() {
        assertNull($("<<P<=>Q> ==> R>"));
        assertNull($("<<P==>Q> ==> R>"));
        assertNull($("<R ==> <P<=>Q>>"));

        assertEquals("<(&&,P,R)==>Q>", $("<R==><P==>Q>>").toString());
        assertNull($("<R==><P==>R>>"));
        assertEquals("<R==>P>", $("<R==><R==>P>>").toString());

    }

    @Test public void testReducedAndInvalidImplicationsTemporal() {
        assertNull($("<<P<=>Q> =/> R>"));
        assertNull($("<R =/> <P<=>Q>>"));

        assertNull($("<<P==>Q> =/> R>"));
        assertNull($("<<P==>Q> =|> R>"));
        assertNull($("<<P==>Q> =|> R>"));
    }

    @Test public void testReducedAndInvalidImplicationsTemporal2() {
        assertEquals("<(&|,P,R)=|>Q>", $("<R=|><P==>Q>>").toString());
        assertEquals("<(&/,R,P)=/>Q>", $("<R=/><P==>Q>>").toString());
        assertEquals("<(&/,P,R)=\\>Q>", $("<R=\\><P==>Q>>").toString());
    }

    //TODO:
        /*
            (&,(&,P,Q),R) = (&,P,Q,R)
            (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)

            // set union
            if (term1.op(Op.SET_INT) && term2.op(Op.SET_INT)) {

            // set intersection
            if (term1.op(Op.SET_EXT) && term2.op(Op.SET_EXT)) {

         */

    @Test public void testDisjunctEqual() {
        assertEquals(p, $.disj(p, p));
    }
    @Test public void testConjunctionEqual() {
        assertEquals(p, $.conj(p, p));
    }

    @Test public void testIntExtEqual() {
        assertEquals(p, $.sect(p, p));
        assertEquals(p, $.sectInt(p, p));
    }

    @Test public void testDiffIntEqual() {
        Term d = diffInt(p, p);
        assertNull(d);
    }
    @Test public void testDiffExtEqual() {
        Term d = diffExt(p, p);
        assertNull(d);
    }
    @Test public void testDifferenceSorted() {
//        assertArrayEquals(
//            new Term[] { r, s },
//            Terms.toArray(TermContainer.differenceSorted(sete(r, p, q, s), sete(p, q)))
//        );
        //check consistency with differenceSorted
        assertArrayEquals(
            new Term[] { r, s },
            Terms.toSortedSetArray(TermContainer.difference(sete(r, p, q, s), sete(p, q)))
        );
    }
    @Test public void testDifferenceSortedEmpty() {
//        assertArrayEquals(
//                new Term[] { },
//                Terms.toArray(TermContainer.differenceSorted(sete(p, q), sete(p, q)))
//        );
        //check consistency with differenceSorted
        assertArrayEquals(
                new Term[] { },
                Terms.toSortedSetArray(TermContainer.difference(sete(p, q), sete(p, q)))
        );
    }

    @Test
    public void testDifferenceImmediate() {

        Term d = diffInt(
                seti($("a"), $("b"), $("c")),
                seti($("d"), $("b")));
        assertEquals(Op.SET_INT, d.op());
        assertEquals(d.toString(), 2, d.size());
        assertEquals("[a,c]", d.toString());
    }

    @Test
    public void testDifferenceImmediate2() {


        Compound a = $.sete($("a"), $("b"), $("c"));
        Compound b = $.sete($("d"), $("b"));
        Term d = diffExt(a, b);
        assertEquals(Op.SET_EXT, d.op());
        assertEquals(d.toString(), 2, d.size());
        assertEquals("{a,c}", d.toString());

    }

    @Test
    public void testDisjunctionReduction() {
        assertEquals("(||,a,b,c,d)",
                $("(||,(||,a,b),(||,c,d))").toString());
        assertEquals("(||,b,c,d)",
                $("(||,b,(||,c,d))").toString());
    }

    @Test
    public void testConjunctionReduction() {
        assertEquals("(&&,a,b,c,d)",
                $("(&&,(&&,a,b),(&&,c,d))").toString());
        assertEquals("(&&,b,c,d)",
                $("(&&,b,(&&,c,d))").toString());
    }

    @Test
    public void testMultireduction() {
        //TODO probably works
    }

    @Test public void testConjunctionMultipleAndEmbedded() {

        assertEquals("(&&,a,b,c,d)",
                $("(&&,(&&,a,b),(&&,c,d))").toString());
        assertEquals("(&&,a,b,c,d,e,f)",
                $("(&&,(&&,a,b),(&&,c,d), (&&, e, f))").toString());
        assertEquals("(&&,a,b,c,d,e,f,g,h)",
                $("(&&,(&&,a,b, (&&, g, h)),(&&,c,d), (&&, e, f))").toString());
    }

    @Test public void testConjunctionEquality() {

        assertEquals(
            $("(&&,r,s)"),
            $("(&&,s,r)"));
        assertNotEquals(
            $("(&/,r,s)"),
            $("(&/,s,r)"));
        assertEquals(
            $("(&|,r,s)"),
            $("(&|,s,r)"));

    }

    @Test public void testImplicationInequality() {

        assertNotEquals(
                $("<r ==> s>"),
                $("<s ==> r>"));
        assertNotEquals(
                $("<r =/> s>"),
                $("<s =/> r>"));
        assertNotEquals(
                $("<r =\\> s>"),
                $("<s =\\> r>"));
        assertNotEquals(
                $("<r =|> s>"),
                $("<s =|> r>"));

    }

    @Test public void testDisjunctionMultipleAndEmbedded() {

        assertEquals("(||,a,b,c,d)",
                $("(||,(||,a,b),(||,c,d))").toString());
        assertEquals("(||,a,b,c,d,e,f)",
                $("(||,(||,a,b),(||,c,d), (||, e, f))").toString());
        assertEquals("(||,a,b,c,d,e,f,g,h)",
                $("(||,(||,a,b, (||, g, h)),(||,c,d), (||, e, f))").toString());

    }

}
