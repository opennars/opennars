package nars.core;

import nars.NAR;
import nars.model.impl.Default;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;
import nars.nal.tlink.TermLinkTemplate;
import nars.bag.Bag;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermLinkTest {

    @Test
    public void testConjunctionTermLinks() {

        Bag<TermLinkKey, TermLink> cj0 = getTermLinks("(&&,a,b)");
        assertTrue(cj0.keySet().toString().contains("Ba" + Symbols.TLinkSeparator + "a"));
        assertTrue(cj0.keySet().toString().contains("Bb" + Symbols.TLinkSeparator + "b"));
        assertEquals(2, cj0.size());

        Bag<TermLinkKey, TermLink> cj1 = getTermLinks("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)");
        //System.out.println(cj1.keySet());

        assertEquals(5, cj1.size());
        //NOTE: cj1.size() will equal 5 if termlinks are normalized in TermLinkBuilder
    }

    @Test
    public void testImplicatedConjunctionWithVariablesTermLinks() {
        Bag<TermLinkKey, TermLink> cj1 = getTermLinks("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>");
        //System.out.println(cj1.keySet());
        // [Dba:<#1 --> key>, Dbb:<$1 --> (/,open,#2,_)>, Da:<$1 --> lock>, Db:(&&,<#1 --> key>,<$2 --> (/,open,#1,_)>), Dab:lock]
        assertEquals(5, cj1.size());

    }


        @Test public void testImplicationTermLinks() {
        Bag<TermLinkKey, TermLink> cj2 = getTermLinks("<(*,c,d) ==> e>");
        assertEquals(2, cj2.size()); //Da:(*,c,d)  and Db:e
        List<TermLinkTemplate> tj2 = getTermLinkTemplates("<(*,c,d) ==> e>");
        assertEquals(4, tj2.size()); //4 templates: [<(*,c,d) ==> e>:Ea|Da:(*,c,d), <(*,c,d) ==> e>:Iaa|Haa:c, <(*,c,d) ==> e>:Iab|Hab:d, <(*,c,d) ==> e>:Eb|Db:e]

        Bag<TermLinkKey, TermLink> cj3 = getTermLinks("<d ==> e>");
        assertEquals(2, cj3.size());
        List<TermLinkTemplate> tj3 = getTermLinkTemplates("<d ==> e>");
        assertEquals(2, tj3.size());

        /*Bag<TermLinkKey, TermLink> cj2 = getTermLinks("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>");
        cj2.printAll(System.out);

        System.out.println();

        Bag<TermLinkKey, TermLink> cj3 = getTermLinks("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
        cj3.printAll(System.out);
        */
    }

    private static NAR nn(String term) {
        String task = term + ". %1.00;0.90%";
        NAR n = new NAR(new Default());
        n.input(task);
        n.run(16);
        return n;
    }

    private List<TermLinkTemplate> getTermLinkTemplates(String term) {
        NAR n = nn(term);
        Concept c = n.concept(term);
        assertNotNull(c);

        return c.getTermLinkTemplates();
    }

    public static Bag<TermLinkKey, TermLink> getTermLinks(String term) {
        NAR n = nn(term);
        //Concept c = n.conceptualize(term);
        Concept c = n.memory.conceptualize(new Budget(1f,1f,1f),
                n.term(term));
        assertNotNull(c);
        n.run(2);

        return c.termLinks;
    }

}
