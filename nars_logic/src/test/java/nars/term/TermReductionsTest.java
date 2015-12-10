package nars.term;

import nars.$;
import org.junit.Test;

import static nars.$.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        // (&,R,(&,P,Q)) = (&,P,Q,R)
        assertEquals("(&,P,Q,R)", $("(&,R,(&,P,Q))").toString());
    }
    @Test public void testIntersectExtReduction4() {
        //UNION if (term1.op(Op.SET_INT) && term2.op(Op.SET_INT)) {
        assertEquals("{P,Q,R,S}", sect($.set(p, q), $.set(r, s)).toString());
        assertEquals("{P,Q,R,S}", $("(&,{P,Q},{R,S})").toString());
        assertEquals(null, sect($.setInt(p, q), $.setInt(r, s)));

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
        assertEquals("[P,Q,R,S]", sectInt($.setInt(p, q), $.setInt(r, s)).toString());
        assertEquals("[P,Q,R,S]", $("(|,[P,Q],[R,S])").toString());
        assertEquals(null, $("(|,{P,Q},{R,S})"));
    }
    @Test public void testIntersectIntReduction_to_one() {
        assertEquals("<robin-->bird>", $("<robin-->(|,bird)>").toString());
        assertEquals("<robin-->bird>", $("<(|,robin)-->(|,bird)>").toString());
    }


    @Test public void testInvalidEquivalences() {
        assertEquals("<P<=>Q>", $.equiv(p, q).toString() );

        assertNull($.equiv( $.impl(p, q), r) );
        assertNull($.equiv( $.equiv(p, q), r) );
        assertNull($.equiv( $.equivAfter(p, q), r) );
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


}
