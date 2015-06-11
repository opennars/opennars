package nars.core;

import nars.NAR;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.model.impl.Default;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.term.Term;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkTemplate;
import nars.util.data.id.Identifier;
import nars.util.graph.TermLinkGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermLinkTest {

    @Test
    public void testConjunctionTermLinks() {

        Bag<Identifier, TermLink> cj0 = getTermLinks("(&&,a,b)");
        assertTrue(cj0.keySet().toString(), cj0.keySet().toString().contains("Ba"));
        assertTrue(cj0.keySet().toString(), cj0.keySet().toString().contains("Bb"));
        assertEquals(2, cj0.size());

        Bag<Identifier, TermLink> cj1 = getTermLinks("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)");
        //System.out.println(cj1.keySet());

        assertEquals(5, cj1.size());
        //NOTE: cj1.size() will equal 5 if termlinks are normalized in TermLinkBuilder
    }

    @Test
    public void testImplicatedConjunctionWithVariablesTermLinks() {
        Bag<Identifier, TermLink> cj1 = getTermLinks("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>");
        //System.out.println(cj1.keySet());
        // [Dba:<#1 --> key>, Dbb:<$1 --> (/,open,#2,_)>, Da:<$1 --> lock>, Db:(&&,<#1 --> key>,<$2 --> (/,open,#1,_)>), Dab:lock]
        assertEquals(5, cj1.size());

    }


        @Test public void testImplicationTermLinks() {
        Bag<Identifier, TermLink> cj2 = getTermLinks("<(*,c,d) ==> e>");
        assertEquals(cj2.values() + " should be: [ Da:(*,c,d) ,  Db:e ] ", 2, cj2.size());
        List<TermLinkTemplate> tj2 = getTermLinkTemplates("<(*,c,d) ==> e>");
        assertEquals(4, tj2.size()); //4 templates: [<(*,c,d) ==> e>:Ea|Da:(*,c,d), <(*,c,d) ==> e>:Iaa|Haa:c, <(*,c,d) ==> e>:Iab|Hab:d, <(*,c,d) ==> e>:Eb|Db:e]

        Bag<Identifier, TermLink> cj3 = getTermLinks("<d ==> e>");
        assertEquals(2, cj3.size());
        List<TermLinkTemplate> tj3 = getTermLinkTemplates("<d ==> e>");
        assertEquals(2, tj3.size());

        /*Bag<Identifier, TermLink> cj2 = getTermLinks("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>");
        cj2.printAll(System.out);

        System.out.println();

        Bag<Identifier, TermLink> cj3 = getTermLinks("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>");
        cj3.printAll(System.out);
        */
    }

    private static NAR nn(String term) {
        String task = term + ". %1.00;0.90%";
        NAR n = new NAR(new Default());
        n.input(task);
        n.runWhileNewInput(16);
        return n;
    }

    private List<TermLinkTemplate> getTermLinkTemplates(String term) {
        NAR n = nn(term);
        Concept c = n.concept(term);
        assertNotNull(c);

        return ((DefaultConcept)c).getTermLinkTemplates();
    }

    public static Bag<Identifier, TermLink> getTermLinks(String term) {
        NAR n = nn(term);
        //Concept c = n.conceptualize(term);
        Concept c = n.memory.conceptualize(new Budget(1f,1f,1f),
                n.term(term));
        assertNotNull(c);
        n.runWhileNewInput(2);

        return c.getTermLinks();
    }

    public Set<String> getTermLinks(Bag<Identifier, TermLink> t) {
        Set<String> s = new HashSet();
        t.forEach(l -> s.add(l.toString()));
        return s;
    }

    @Test
    public void testStatementComponent() {
        NAR n = new NAR(new Default());
        n.input("<a --> b>.");
        n.runWhileNewInput(1);

        Set<String> tl = getTermLinks(n.concept("<a --> b>").getTermLinks());
        assertEquals("[Cb:b, Ca:a]", tl.toString());
    }

    @Test
    public void testIdentifier() {
        NAR n = new NAR(new Default());
        n.input("<a --> b>.");
        n.input("<<a --> b> --> d>.");
        n.input("<<a --> b> --> e>.");
        n.input("<c --> <a --> b>>.");
        n.input("<c --> d>.");
        n.input("<f --> <a --> b>>.");
        n.runWhileNewInput(6);



        Set<String> ainhb = getTermLinks(n.concept("<a --> b>").getTermLinks());

        assertTrue( 6 <= ainhb.size());
        assertTrue(ainhb.contains("Ca:a"));
        assertTrue(ainhb.contains("Cb:b"));
        //assertTrue("not necessary to include the term's own name in comopnent links because its index will be unique within the term anyway", !ainhb.contains("Da:a"));

        Set<String> atl = getTermLinks(n.concept("a").getTermLinks());
        System.out.println(ainhb);
        System.out.println(atl);

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
        Set<String> f = getTermLinks(n.concept("f").getTermLinks());
        assertEquals(1, f.size());
        assertTrue(f.contains("Da" + Symbols.TLinkSeparator + "<f --> <a --> b>>"));


        //this compound involving f has no incoming links, all links are internal
        Set<String> fc = getTermLinks(n.concept("<f --> <a --> b>>").getTermLinks());
        assertEquals(4, fc.size());
        assertTrue(fc.contains("Ca:f"));
        assertTrue(fc.contains("Cb:<a --> b>"));
        assertTrue(fc.contains("Cba:a"));
        assertTrue(fc.contains("Cbb:b"));


    }

    @Test public void termlinkConjunctionImplicationFullyConnected() {
        //from nal6.4
        String c = "<(&&,<$x --> flyer>,<$x --> [chirping]>) ==> <$x --> bird>>.";
        String d = "<<$y --> [withwings]> ==> <$y --> flyer>>.";
        Bag<Identifier, TermLink> x = getTermLinks(d);
        for (TermLink t : x.values()) {
            assertEquals(t.type, 3); //all component_statement links
        }

        assertEquals(4, getTermLinkTemplates(d).size());



        NAR n = new NAR(new Default());

        n.input(c);
        n.input(d);
        n.frame(12);

        TermLinkGraph g = new TermLinkGraph(n);


        ConnectivityInspector<Term,TermLink> ci = new ConnectivityInspector(g);
        int set = 0;
        for (Set<Term> s : ci.connectedSets()) {
            for (Term v : s)
                System.out.println(set + ": " + v);
            set++;
        }

        assertTrue("termlinks between the two input concepts form a fully connected graph",
                ci.isGraphConnected());

    }

    @Test public void termlinksSetAndElement() {
        //from nal6.4
        String c = "<{x} --> y>.";


        NAR n = new NAR(new Default());
        n.input(c);
        n.frame(2);

        TermLinkGraph g = new TermLinkGraph(n);


        //System.out.println(g);

        ConnectivityInspector<Term,TermLink> ci = new ConnectivityInspector(g);
        assertTrue("termlinks between the two input concepts form a fully connected graph",
                ci.isGraphConnected());

        TermLinkGraph h = new TermLinkGraph().add(n.concept("{x}"), true);
        //System.out.println(h);
        String baix = "Aa:x=({x},x)";
        assertTrue(h.toString() + " must contain " + baix, h.toString().contains(baix));
        TermLinkGraph i = new TermLinkGraph().add(n.concept("x"), true);
        //System.out.println(i);

        assertTrue(i.toString(), i.toString().contains("Ba:{x}=(x,{x})"));

    }

}
