package nars.guifx.graph2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.*;
import nars.guifx.JFX;
import nars.guifx.NARfx;
import nars.guifx.annotation.Range;
import nars.guifx.util.ColorMatrix;
import nars.term.Term;

/**
 * Created by me on 10/2/15.
 */
public class HexagonsVis implements VisModel<HexagonsVis.HexTerm2Node> {

    final static Font nodeFont = NARfx.mono(0.25);

    protected double minSize = 16;
    protected double maxSize = 64;

    double nodeScaleCache = 1.0;

    @Range(min=0, max=16)
    public final SimpleDoubleProperty nodeScale = new SimpleDoubleProperty(1.0);

    public HexagonsVis() {
        super();
        nodeScale.addListener((e) -> {
            nodeScaleCache = nodeScale.get();
        });
    }
    //public Function<Term,TermNode> nodeBuilder;

    @Override
    public HexTerm2Node newNode(Term term) {
        return new HexTerm2Node(term);
    }

    @Override
    public void accept(HexTerm2Node t) {
        float p, q;
        if (t == null) {
            return;
        }
        else {
//            p = t.term.cgetPriority();
//            q = t.c.getQuality();
        }

        t.update();

        t.scale(minSize + (maxSize - minSize) * t.priNorm);
    }

    final static Font mono = NARfx.mono(8);

    public static final ColorMatrix colors = new ColorMatrix(17 /* op hashcode color, hopefully prime */, 17 /* activation  */,
            (op, activation) -> {
                Color c = Color.hsb(op*360.0,
                        0.5 + 0.4 * activation,
                        0.3 + activation*0.65);
                return c;
            });


    public class HexTerm2Node extends TermNode {


        private final Canvas base;

        private GraphicsContext g;
        private boolean hover = false;
        private GraphicsContext d;

        public HexTerm2Node(Term t) {
            super(t);


            base = new Canvas();
            base.setLayoutX(-0.5f);
            base.setLayoutY(-0.5f);



            base.setOnMouseClicked(e -> {
                //System.out.println("click " + e.getClickCount());
                if (e.getClickCount() == 2) {
                    if (c != null)
                        NARfx.run((a,b) -> {
                            System.out.println("doubleclicked " + t);
                        });
                }
            });

//            EventHandler<MouseEvent> mouseActivity = e -> {
//                if (!hover) {
////                    base.setStroke(Color.ORANGE);
////                    base.setStrokeWidth(0.05);
//                    hover = true;
//                }
//            };
//            //base.setOnMouseMoved(mouseActivity);
//            base.setOnMouseEntered(mouseActivity);
//            base.setOnMouseExited(e -> {
//                if (hover) {
////                    base.setStroke(null);
////                    base.setStrokeWidth(0);
//                    hover = false;
//                }
//            });

            setPickOnBounds(false);
            setManaged(false);

            //update();

//            base.setLayoutX(-0.5f);
//            base.setLayoutY(-0.5f);

            getChildren().setAll(base);



            render(128,24); //TODO call this lazily as it is being shown


        }


        /** re-render to image buffer */
        public void render(double w, double h) {

            g = base.getGraphicsContext2D();
            d = base.getGraphicsContext2D();


            base.setWidth(w);
            base.setHeight(h);


            //TODO move nodeScaleCache elsewhere
            double s = (nodeScaleCache * 4.0) / w; //scaled to width
            base.setScaleX(s);
            base.setScaleY(s);
            base.setScaleZ(s);

            base.setLayoutX(-w/2);
            base.setLayoutY(-h/2);




            final double W = base.getWidth();
            final double H = base.getHeight();
            g.clearRect(0,0,W,H);

            g.setFill(TermNode.getTermColor(term, colors, 0.5)); /*colors.get(
                    ,
                    //c==null ? 0 : c.getPriority()) //this can work if re-rendered
                    0.5 //otherwise jus use medium
            ));*/
            g.fillRect(0,0,W,H);



            g.setFont(mono);
            g.setFill(Color.BLACK);
            g.setFontSmoothingType(FontSmoothingType.LCD);

            g.fillText(term.toStringCompact(),0,H/2);



        }

        public void update() {
//            if (c == null) return;
//
//            double v = 0.5;
//            if (c.hasBeliefs())
//                v = c.getBeliefs().top().getConfidence();
//
//            d.setFill(Color.hsb(v*360,1,1));
//            d.fillRect(0,0,10,10);
        }
    }


    /** original */
    public static class HexTermNode extends TermNode {

        private final Text label;
        public final Polygon base;
        private boolean hover = false;

        public HexTermNode(Term t) {
            super(t);

            this.label = new Text(t.toStringCompact());
            base = JFX.newPoly(6, 2.0);


            label.setFill(Color.WHITE);
            label.setBoundsType(TextBoundsType.VISUAL);

            label.setPickOnBounds(false);
            label.setMouseTransparent(true);
            label.setFont(nodeFont);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setSmooth(false);
            //titleBar.setManaged(false);
            //label.setBoundsType(TextBoundsType.VISUAL);

            base.setStrokeType(StrokeType.INSIDE);

            base.setOnMouseClicked(e -> {
                //System.out.println("click " + e.getClickCount());
                if (e.getClickCount() == 2) {
                    if (c != null)
                        NARfx.run((a,b) -> {
                            //...
                        });
                }
            });

            EventHandler<MouseEvent> mouseActivity = e -> {
                if (!hover) {
                    base.setStroke(Color.ORANGE);
                    base.setStrokeWidth(0.05);
                    hover = true;
                }
            };
            //base.setOnMouseMoved(mouseActivity);
            base.setOnMouseEntered(mouseActivity);
            base.setOnMouseExited(e -> {
                if (hover) {
                    base.setStroke(null);
                    base.setStrokeWidth(0);
                    hover = false;
                }
            });

            setPickOnBounds(false);


            getChildren().setAll(base, label);//, titleBar);


            //update();

            base.setLayoutX(-0.5f);
            base.setLayoutY(-0.5f);


            label.setLayoutX(-getLayoutBounds().getWidth() / (2) + 0.25);

            base.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            base.setCache(true);

            label.setCacheHint(CacheHint.DEFAULT);
            label.setCache(true);


        }

    }


//
//    public double getVertexScaleByPri(Concept c) {
//        return c.getPriority();
//        //return (c != null ? c.getPriority() : 0);
//    }
//
//    public double getVertexScaleByConf(Concept c) {
//        if (c.hasBeliefs()) {
//            double conf = c.getBeliefs().getConfidenceMax(0, 1);
//            if (Double.isFinite(conf)) return conf;
//        }
//        return 0;
//    }


//
//    public Color getEdgeColor(double termMean, double taskMean) {
////            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality
////
////            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
////                    0.95f,
////                    Math.min(0.75f + 0.25f * (termMean + taskMean) / 2f, 1f)
////                    //,0.5 * (termMean + taskMean)
////            );
////
//////            return new Color(
//////                    0.5f + 0.5f * termMean,
//////                    0,
//////                    0.5f + 0.5f * taskMean,
//////                    0.5f + 0.5f * (termMean + taskMean)/2f
//////            );
//        return null;
//    }

}
