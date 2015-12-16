package nars.guifx.demo;

import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;

/**
 * Created by me on 12/13/15.
 */
public class SubButton extends TextFlow {


    public static Node space() {
        return new Text("  ");
    }

    public static Node make(String s) {
        Text l = new Text(s);
        l.getStyleClass().clear();
        //l.setWrapText(true);
        l.setTextAlignment(TextAlignment.LEFT);
        //l.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        l.setFill(Color.WHITE);

        l.setFont(NARfx.mono(20));
        l.setTextOrigin(VPos.CENTER);

//        l.hoverProperty().addListener(k -> {
//
//            Label kk = (Label)k;
//            if (kk.isHover()) {
//                kk.setTextFill(Color.WHITE);
//            } else {
//                kk.setTextFill(kk.getColor());
//            }
//
//        });
        return l;
    }

    public static SubButton make(NAR nar, Term t) {
        SubButton sb = new SubButton(6, t, 0.5f);
        if (t instanceof Compound) {
            sb.add(make(t.op().str));
            for (Term x : ((Compound)t).terms()) {
                sb.add(make(nar, x));
            }
        } else {
            sb.add(make(t.toString(true)));
        }
        return sb;
    }

    public static SubButton make(NAR nar, Task t) {
        SubButton sb = new SubButton(6, t.term(), t.getPriority());
        sb.add(make(nar, t.get()));
        if (t.getTruth()!=null)
            sb.add(make(t.getTruth().toString()));
        sb.add(make(String.valueOf(t.getPunctuation())));
        //sb.add(make(space()));
        return sb;
    }

    public void add(Node n) {
        getChildren().add(n);
    }


    public SubButton() {
        this(6, "#262");
    }

    public SubButton(int padding, Term t, float p) {
        this(padding,
                TermNode.getTermColor(t.term(),
                        CanvasEdgeRenderer.colors,
                        p)
                        .interpolate(Color.TRANSPARENT, 1f-p)
                        .toString().replace("0x", "#")
        );

    }
    public SubButton(int padding, String bg) {
        super();

        getStyleClass().clear();


        setCursor(Cursor.CROSSHAIR);


        setStyle("-fx-border-color: #666; -fx-border-width: 2px; -fx-background-color: " + bg + "; -fx-padding:" + padding + "px;");

        setTextAlignment(TextAlignment.LEFT);




//        setOnMouseClicked(c -> {
//            //setSelected(false);
//            setFocused(false);
//
//            Popup p = new Popup();
//            p.getContent().add(new TaskPane(nar, task));
//            p.getContent().add(new NSlider("pri", 100, 25, 0.5f));
//            Button b1 = new Button("+");
//            p.getContent().add(b1);
//            p.setOpacity(0.75f);
//            p.setAutoHide(true);
//            p.setAutoFix(true);
//
//            p.show(getScene().getWindow(), c.getSceneX(), c.getSceneY());
//        });

        //runLater(this::update);

    }

    private static String labelize(String s) {
        //https://en.wikipedia.org/wiki/List_of_logic_symbols
        return s.replace("-->","→")
                .replace("==>","⇒")
                .replace("<=>","⇄")
                .replace("<->","↔")
                .replace("||", "⇵")
                ;
        //↔ ⇔ ⇒ ⇄ ⇾ ⇥ ⇵
    }

//    public void update() {
//        float pri = task.getPriority();
//        float priToFontSize = pri * 40f;
////            getStyleClass().clear();
////            getStylesheets().clear();
////            //setStyle("-fx-background-color: #FFFFFF !important;");
////            setStyle("-fx-base: #FFFFFF !important;");
////            setStyle("-fx-padding: 5px !important;");
////            //setStyle("-fx-border-radius: 20;");
//        setFont(NARfx.mono(priToFontSize));
//
//        Color c = getColor();
////            setBackground(new Background(
////                    new BackgroundFill(
////                        c,
////                        CornerRadii.EMPTY,
////                        Insets.EMPTY)));
//        setTextFill(c);
//
//
//    }
//
//    @NotNull
//    private Color getColor() {
//        float pri = task.getPriority();
//        return hsb(
//                (task.getTerm().op().ordinal() / 64f) * 360.0,
//                0.4, 0.7, 0.75f + pri * 0.25f
//        );
//    }

}
