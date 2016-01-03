package nars.guifx.graph2.source;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.guifx.graph2.ConceptsSource;
import nars.term.Termed;

import java.util.ArrayList;
import java.util.Set;

/**
 * Includes the termlinked and tasklinked concepts of a set of
 * root concepts
 */
public class ConceptNeighborhoodSource extends ConceptsSource {

    private final ArrayList<Termed> roots;
    int termLinkNeighbors = 16;

    public ConceptNeighborhoodSource(NAR nar, Concept... c) {
        super(nar);
        this.roots = Lists.newArrayList(c);
    }

    final Set<Termed> conceptsSet = Global.newHashSet(1);

    @Override
    public void commit() {

        roots.forEach(r -> {
            conceptsSet.add(r);
            if (!(r instanceof Concept)) return;

            Concept c = (Concept) r;
            c.getTaskLinks().forEach(termLinkNeighbors, n -> {
                Termed tn = n;
                if (tn instanceof Concept) {
                    conceptsSet.add(tn);
                } else {
                    //System.out.println("non-Concept TaskLink target: " + tn + " " + tn.getClass());
                    conceptsSet.add(nar.concept(tn));
                }
            });
            c.getTermLinks().forEach(termLinkNeighbors, n -> {
                if (n instanceof Concept) {
                    conceptsSet.add(n);
                } else {
                    //System.out.println("non-Concept TermLink target: " + n + " " + n.getClass());
                    conceptsSet.add(nar.concept(n.term()));
                }
            });
            //concepts::add);
        });

        commit(conceptsSet);

        //System.out.println(concepts);

        conceptsSet.clear();

    }
}
