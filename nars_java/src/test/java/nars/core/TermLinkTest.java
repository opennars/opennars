package nars.core;

import nars.build.Default;
import nars.logic.entity.Concept;
import nars.logic.entity.TermLink;
import nars.logic.entity.tlink.TermLinkKey;
import nars.util.bag.Bag;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermLinkTest {

    @Test
    public void testConjunctionTermLinks() {

        Bag<TermLinkKey, TermLink> cj0 = getTermLinks("(&&,a,b)");
        assertTrue(cj0.keySet().toString().contains("Ba:a"));
        assertTrue(cj0.keySet().toString().contains("Bb:b"));
        assertEquals(2, cj0.size());

        Bag<TermLinkKey, TermLink> cj1 = getTermLinks("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)");
        assertEquals(5, cj1.size());

        Bag<TermLinkKey, TermLink> cj2 = getTermLinks("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>");
        cj2.printAll(System.out);

        System.out.println();

        Bag<TermLinkKey, TermLink> cj3 = getTermLinks("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
        cj3.printAll(System.out);
    }

    public static Bag<TermLinkKey, TermLink> getTermLinks(String term) {
        String task = term + ". %1.00;0.90%";
        NAR n = new NAR(new Default());
        n.input(task);
        n.run(4);

        Concept c = n.concept(term);
        assertNotNull(c);

        return c.termLinks;
    }

}
