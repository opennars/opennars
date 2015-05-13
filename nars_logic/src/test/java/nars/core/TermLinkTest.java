package nars.core;

import nars.NAR;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.model.impl.Default;
import nars.nal.concept.Concept;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;
import nars.nal.tlink.TermLinkTemplate;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermLinkTest {

    @Test
    public void testConjunctionTermLinks() {

        Bag<TermLinkKey, TermLink> cj0 = getTermLinks("(&&,a,b)");
        assertTrue(cj0.keySet().toString(), cj0.keySet().toString().contains("Ba"));
        assertTrue(cj0.keySet().toString(), cj0.keySet().toString().contains("Bb"));
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

    public Set<String> getTermLinks(Bag<TermLinkKey, TermLink> t) {
        Set<String> s = new HashSet();
        t.forEach(l -> s.add(l.toString()));
        return s;
    }

    @Test
    public void testUnnecessaryTargetInTermlinkKey() {
        NAR n = new NAR(new Default());
        n.input("<a --> b>.");
        n.input("<<a --> b> --> d>.");
        n.input("<<a --> b> --> e>.");
        n.input("<c --> <a --> b>>.");
        n.input("<c --> d>.");
        n.input("<f --> <a --> b>>.");
        n.run(6);


        Set<String> ainhb = getTermLinks(n.concept("<a --> b>").termLinks);
        assertEquals(6, ainhb.size());
        assertTrue(ainhb.contains("Da"));
        assertTrue(ainhb.contains("Db"));
        assertTrue("not necessary to include the term's own name in comopnent links because its index will be unique within the term anyway", !ainhb.contains("Da:a"));



//        System.out.println();
//
//        n.concept("d").termLinks.forEach(x -> System.out.println(x));
//
//        System.out.println();
//
//        n.concept("c").termLinks.forEach(x -> System.out.println(x));
//
//        System.out.println();
//
        Set<String> f = getTermLinks(n.concept("f").termLinks);
        assertEquals(1, f.size());
        assertTrue(f.contains("Ea" + Symbols.TLinkSeparator + "<f --> <a --> b>>"));


        //this compound involving f has no incoming links, all links are internal
        Set<String> fc = getTermLinks(n.concept("<f --> <a --> b>>").termLinks);
        assertEquals(4, fc.size());
        assertTrue(fc.contains("Da"));
        assertTrue(fc.contains("Db"));
        assertTrue(fc.contains("Dba"));
        assertTrue(fc.contains("Dbb"));


    }

}
