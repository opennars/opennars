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


    private final Concept concept;
    //final Label subLabel = new Label();
    final AtomicBoolean pendingUpdate = new AtomicBoolean(false);
    private ConceptSummaryPaneIcon icon;
    private float lastPri = -1f;

    public ConceptSummaryPane(Concept c) {
        super(c.getTerm().toStringCompact());

        this.concept = c;

        //label.getStylesheets().clear();
        setTextAlignment(TextAlignment.LEFT);

        parentProperty().addListener(e -> {

            if (getParent()!=null) {
                if (icon==null) {
                    final double iconWidth = 48f;
                    setGraphic(icon = new ConceptSummaryPaneIcon());
                    icon.size(iconWidth, iconWidth);
                }

                update(true,true);
            }
        });

        setOnMouseClicked( e -> {
            Concept cc = ((ConceptSummaryPane)e.getSource()).concept;
            NARfx.newWindow(cc);
        });
    }

    public void update(boolean priority, boolean icon) {

        if (this.icon == null) return;

        if (icon) {
            this.icon.repaint();
        }

        float pri = concept.getPriority();
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

            Color c = NARfx.hashColor(concept.getTerm().hashCode(),
                    1f, Plot2D.ca);
            g.setStroke(Color.GRAY);
            g.setLineWidth(m);
            g.strokeRect(m/2, m/2, W-m, H-m);

            concept.getBeliefs().forEach(t-> {
                plot(m, Wm, Hm, g, t, false);
            });
            concept.getGoals().forEach(t-> {
                plot(m, Wm, Hm, g, t, true);
            });

        }


    }

    static void plot(double m, double Wm, double hm, GraphicsContext g, Task t, boolean type) {

        ColorMatrix ca = type ? red: blue;

        final double w = 12;


        float freq = t.getFrequency();
        double y = (1f - freq) * Wm;
        float cnf = t.getConfidence();
        double x = cnf * hm;

        Color color = ca.get(freq, cnf);

        //g.setFill(color);
        //g.fillRect(m + x-wh, m + y-wh, w, w);


        double cx = m + x;
        double cy = m + y;

        g.setStroke(color);
        if (type)
            g.strokeLine(cx-w/2, cy-w/2, cx+w/2, cy+w/2);
        else
            g.strokeLine(cx+w/2, cy-w/2, cx-w/2, cy+w/2);
    }

    final static ColorMatrix red  = new ColorMatrix(8,8,(x,y) -> {
        return Color.hsb(360 * (x * 0.25 + 0.25), 0.67, 0.5 + 0.5 * y);
    });
    final static ColorMatrix blue = new ColorMatrix(8,8,(x,y) -> {
        return Color.hsb(360 * (x * 0.25 + 0.65), 0.67, 0.5 + 0.5 * y);
    });

}
