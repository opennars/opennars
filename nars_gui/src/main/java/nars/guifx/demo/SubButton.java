package nars.guifx.demo;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nars.NAR;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;

/**
 * Created by me on 12/13/15.
 */
public class SubButton extends FlowPane {


    public static final float paddingDefault = 0.5f;

    public static Node space() {
        return new Text("  ");
    }

    public static Node make(String s) {
        Label l = new Label(s);
        l.getStyleClass().clear();
        //l.setWrapText(true);
        l.setWrapText(true);
        l.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

        l.setTextAlignment(TextAlignment.LEFT);
        //l.setWrapText(true);

        //l.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        l.setTextFill(Color.WHITE);

        //l.setFont(NARfx.mono(20));
        //l.setTextOrigin(VPos.CENTER);


        l.hoverProperty().addListener(k -> {

            //Text kk = (Text)l;
            Label kk = (Label)l;
            if (kk.isHover()) {
                kk.setUnderline(true);
            } else {
                kk.setUnderline(false);
            }

        });
        return l;
    }

    public static SubButton make(NAR nar, Term t) {
        SubButton sb = new SubButton(paddingDefault, t, paddingDefault);
        if (t instanceof Compound) {
            HBox ig = new HBox();
            ig.setMaxWidth(0);

            ig.getChildren().add(make(t.op().str));


            for (Term x : ((Compound)t).terms()) {
                ig.getChildren().add(make(nar, x));

                //HACK
                Rectangle space = new Rectangle(4f, 4f);
                space.setFill(Color.TRANSPARENT);
                ig.getChildren().add(space);
            }
            sb.add(ig);
        } else {
            sb.add(make(t.toString(true)));
        }
        sb.layout();
        return sb;
    }

    public static SubButton make(NAR nar, Task t) {
        SubButton sb = new SubButton(paddingDefault, t.term(), t.getPriority());
        sb.add(make(nar, t.get()));
        sb.add(make(String.valueOf(t.getPunctuation())));
        if ( !t.isQuestOrQuestion()) {
            sb.scale( 0.5f + 0.5f * t.getTruth().getConfidence() );
        }
        //sb.add(make(space()));
        return sb;
    }

    private void scale(float v) {
        setStyle("-fx-font-size:" + (100f * v) + "%;");
        //setScaleX(v);
        //setScaleY(v);
    }

    public void add(Node n) {
        getChildren().add(n);
    }

//
//    public SubButton() {
//        this(paddingDefault, "#262");
//    }

    public SubButton(float padding, Term t, float p) {
        this(padding,
                TermNode.getTermColor(t.term(),
                        CanvasEdgeRenderer.colors,
                        p)
                        .interpolate(Color.TRANSPARENT, 1f-p)
                        .toString().replace("0x", "#")
        );

    }
    public SubButton(float paddingEM, String bg) {
        super();

        getStyleClass().clear();

        setMaxWidth(0);

        setCursor(Cursor.CROSSHAIR);


        setStyle(
                //"-fx-border-color: #666; -fx-border-width: 2px; " +
                "-fx-background-color: " + bg + "; -fx-padding:" + paddingEM + "em; " +
                "-fx-padding-left: 0; -fx-border-insets-left: 0;");


        //setTextAlignment(TextAlignment.LEFT);




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
