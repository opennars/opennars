package nars.guifx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import nars.concept.Concept;
import nars.guifx.util.ColorMatrix;
import nars.task.Task;
import nars.util.data.Util;

import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.application.Platform.runLater;


public class ConceptSummaryPane extends Button {


    private final Concept obj;
    //final Label subLabel = new Label();
    final AtomicBoolean pendingUpdate = new AtomicBoolean(false);
    private final ConceptSummaryPaneIcon icon;
    private float lastPri = -1f;

    public ConceptSummaryPane(Concept c) {
        super(c.getTerm().toStringCompact());

        this.obj = c;

        //label.getStylesheets().clear();
        setTextAlignment(TextAlignment.LEFT);

        final double iconWidth = 48f;
        setGraphic(icon = new ConceptSummaryPaneIcon());
        icon.size(iconWidth, iconWidth);

        //setAlignment(this, Pos.CENTER_LEFT);

//        setAlignment(subLabel, Pos.CENTER_LEFT);
//        subLabel.setTextAlignment(TextAlignment.LEFT);
//
////        subLabel.setScaleX(0.5f);
////        subLabel.setScaleY(0.5f);
//        setAlignment(subLabel, Pos.CENTER_LEFT);
//        subLabel.getStyleClass().add("sublabel");
//
//        setBottom(subLabel);

        update(true,true);
    }

    public void update(boolean priority, boolean icon) {

        if (icon) {
            this.icon.repaint();
        }

        float pri = obj.getPriority();
        if (Util.equal(lastPri, pri, 0.01)) {
            priority = false;
        }


        if (priority && pendingUpdate.compareAndSet(false, true)) {

            runLater(() -> {
                pendingUpdate.set(false);

                this.lastPri = pri;
                setStyle(JFX.fontSize(((1.0f + pri) * 100.0f)));

                //setTextFill(color);

                /*setBackground(new Background(
                        new BackgroundFill(
                            Color.BLUE, new CornerRadii(0), new Insets(0,0,0,0)
                )));*/

//                StringBuilder sb = new StringBuilder();
//
//                if (obj.hasBeliefs()) {
//                    Task topBelief = obj.getBeliefs().top();
//                    topBelief.appendTo(sb, obj.getMemory(), false);
//                }
//                if (obj.hasGoals()) {
//                    Task topGoal = obj.getGoals().top();
//                    topGoal.appendTo(sb, obj.getMemory(), false);
//                }
//
//                subLabel.setText(sb.toString());

                //layout();
            });
        }
    }

    class ConceptSummaryPaneIcon extends SummaryIcon {
        public ConceptSummaryPaneIcon() {
            super();
            repaint();
        }




        @Override
        protected void repaint() {

            double m = 2;

            double W = getWidth();
            final double Wm = W -m*2;
            double H = getHeight();
            final double Hm = H -m*2;
            if (W*H == 0) return;

            GraphicsContext g = getGraphicsContext2D();

            Color c = NARfx.hashColor(obj.getTerm().hashCode(),
                    1f, Plot2D.ca);
            g.setStroke(Color.GRAY);
            g.setLineWidth(m);
            g.strokeRect(m/2, m/2, W-m, H-m);

            obj.getBeliefs().forEach(t-> {
                plot(m, Wm, Hm, g, t, red);
            });
            obj.getGoals().forEach(t-> {
                plot(m, Wm, Hm, g, t, blue);
            });

        }


    }

    static void plot(double m, double Wm, double hm, GraphicsContext g, Task t, ColorMatrix ca) {
        final double w = 8;
        final double wh = w/2.0;

        float freq = t.getFrequency();
        double y = (1f - freq) * Wm;
        float cnf = t.getConfidence();
        double x = cnf * hm;

        g.setFill(ca.get(freq, cnf));

        g.fillRect(m + x-wh, m + y-wh, w, w);
    }

    final static ColorMatrix red  = new ColorMatrix(8,8,(x,y) -> {
        double py = y * 0.5 + 0.5;
        double ness = x * 0.9 + 0.1;
        return new Color(1f-ness, ness, 0, py);
    });
    final static ColorMatrix blue = new ColorMatrix(8,8,(x,y) -> {
        double py = y * 0.5 + 0.5;
        double ness = x * 0.9 + 0.1;
        return new Color(0, 1f-ness, ness, py);
    });

}
