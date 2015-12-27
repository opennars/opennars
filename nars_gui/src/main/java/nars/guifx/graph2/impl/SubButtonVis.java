package nars.guifx.graph2.impl;

import javafx.scene.Node;
import nars.concept.Concept;
import nars.guifx.demo.SubButton;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.scene.DefaultNodeVis;
import nars.nar.Default;
import nars.term.Termed;

/**
 * Created by me on 12/27/15.
 */
public class SubButtonVis extends DefaultNodeVis {

    private final Default n;

    public SubButtonVis(Default n) {
        this.n = n;
    }

    @Override
    public TermNode newNode(Termed term) {
        return new LabeledCanvasNode(term, 32, e -> {
        }, e -> {
        }) {
            @Override
            protected Node newBase() {
                SubButton s = SubButton.make(
                        n, (Concept) term
                        //n, $.the(term.toString())
                );

                s.setScaleX(0.02f);
                s.setScaleY(0.02f);
                s.shade(1f);

                s.setManaged(false);
                s.setCenterShape(false);

                return s;
            }
        };
        //return new HexTermNode(term.term(), 32, e-> { }, e-> { });
        //return super.newNode(term);
    }
}
