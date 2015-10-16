package nars.guifx;

import javafx.scene.Node;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import org.infinispan.commons.util.WeakValueHashMap;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/15/15.
 */
public class LogPane2 extends LogPane {

    LinkedHashSet<Concept> display = new LinkedHashSet();
    int maxShown = 64;

    final AtomicBoolean pendingShown = new AtomicBoolean(false);

    public LogPane2(NAR n) {
        super();

        n.memory.eventConceptChange.on((Concept c) -> {
            //TODO more efficient:
            display.remove(c);
            display.add(c);

            //if (!pendingUpdate) ..
            //  runLater(update);
            if (pendingShown.compareAndSet(false, true)) {

                List<Node> displayed = Global.newArrayList();
                Iterator<Concept> ii = display.iterator();
                int toSkip = display.size() - maxShown;
                while (ii.hasNext()) {

                    Concept cc = ii.next();

                    if (toSkip > 0) {
                        ii.remove();
                        toSkip--;
                        continue;
                    }

                    displayed.add( node(cc) );
                }

                runLater(() -> {
                    pendingShown.set(false);
                    commit(displayed);
                });
            }
        });

    }

    WeakValueHashMap<Concept,ConceptSummaryPane> concepts = new WeakValueHashMap();

    Node node(Concept cc) {
        ConceptSummaryPane cp = concepts.computeIfAbsent(cc, koncept -> {
            return new ConceptSummaryPane(koncept);
        });
        cp.update();
        return cp;
    }

}
