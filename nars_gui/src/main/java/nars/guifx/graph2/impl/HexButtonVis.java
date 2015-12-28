package nars.guifx.graph2.impl;

import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nars.NAR;
import nars.guifx.JFX;
import nars.guifx.NARfx;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.scene.DefaultNodeVis;
import nars.term.Termed;

/**
 * Created by me on 12/27/15.
 */
public class HexButtonVis extends DefaultNodeVis {

    private final NAR n;

    public HexButtonVis(NAR n) {
        this.n = n;
    }

    public static class HexButton<X> extends Group { //Group {

        public final X value;
        public final Node base;
        public final Node label;

        static final float sizeRatio = 6;

        public HexButton(X object) {
            super();

            setManaged(false);
            setAutoSizeChildren(false);

            this.value = object;

            getChildren().setAll(
                this.base = getBase(),
                this.label = getLabel()
            );

            setScaleX(1/sizeRatio);
            setScaleY(1/sizeRatio);


            label.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            label.setCache(true);

            base.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            base.setCache(true);


            setCache(true);


        }

        private Node getLabel() {
            Text s = new Text(value.toString());
            s.setManaged(false);

            //s.setTextAlignment(TextAlignment.CENTER);
            //s.setTextOrigin(VPos.CENTER);
            //s.setCenterShape(true);
            s.setTextAlignment(TextAlignment.CENTER);
            //s.setTranslateX(-0.5f);
            s.setFont(NARfx.mono(2f));
            s.setStroke(null);

            s.setSmooth(false);

            s.setFill(Color.WHITE);

            return s;
        }

        private Node getBase() {
            Polygon s = JFX.newPoly(6, sizeRatio);
            //s.setStrokeType(StrokeType.INSIDE);
            s.setManaged(false);

            //s.setFill(Color.GRAY);
            s.setStroke(null);
            s.setFill(Color.WHITE);
            //s.setOpacity(0.75f);
            //s.shade(1f);

            //s.setManaged(false);
            //s.setCenterShape(false);
            return s;
        }
    }

    @Override
    public TermNode newNode(Termed term) {
        return new LabeledCanvasNode(term, 32, e -> {
        }, e -> {
        }) {
            @Override
            protected Node newBase() {
                HexButton h = new HexButton(term);
                ((Polygon)h.base).setFill(
                    TermNode.getTermColor(term,
                        CanvasEdgeRenderer.colors,
                        1f/(term.term().volume()))
                );
                return h;
            }
        };
        //return new HexTermNode(term.term(), 32, e-> { }, e-> { });
        //return super.newNode(term);
    }
}
