package nars.guifx.demo;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;

/**
 * Created by me on 12/13/15.
 */
public class SubButton extends HBox {


    NAR nar = null;
    Object value = null;
    public static final float paddingDefault = 0.5f;

    public void setNAR(NAR nar) {
        this.nar = nar;
    }

    public NAR getNAR() {
        return nar;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    private static final EventHandler<? super MouseEvent> onTermClicked = (e) -> {
//        System.out.println(e);
        /*
        System.out.println("clicked: " + e.getSource() + " " +
                ((SubButton)e.getSource()).getValue().getClass() );
        */
//        System.out.println(e.getTarget());
        SubButton sb = (SubButton)e.getSource();

        Object v = sb.getValue();
        if (v instanceof Concept) {
            //NARfx.newWindow(nar, (Concept)v);
        }

        e.consume();
    };

    public static Node space() {
        return new Text("  ");
    }

    public static Node makeParagraph(String s) {
        Text l = new Text(s);
        l.getStyleClass().clear();
        l.setFill(Color.WHITE);
        return l;
    }

    public static Node make(String s) {
        Text l = new Text(s);
        //l.setMaxWidth(0);
        l.getStyleClass().clear();
        //l.setWrapText(true);
        //l.setWrapText(true);

        //l.setTextOverrun(OverrunStyle.ELLIPSIS);
        //l.setAlignment(Pos.CENTER_LEFT);
        l.setTextOrigin(VPos.CENTER);
        l.setTextAlignment(TextAlignment.LEFT);
        //l.setWrapText(true);

        //l.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);
        //l.setTextFill(Color.WHITE);
        l.setFill(Color.WHITE);

        //l.setFont(NARfx.mono(20));
        //l.setTextOrigin(VPos.CENTER);


//        l.setOnMouseClicked(e-> {
//            System.out.println(s);
//            //NARfx.newWindow()
//        });
        //l.setTooltip(new Tooltip(""));

        //l.hoverProperty().addListener(TextNodeHoverListener);

        return l;
    }

    public static SubButton make(NAR nar, Concept c) {
        SubButton s = make(nar, c.term());
        s.setValue(c);

        Bag<Task> tl = c.getTaskLinks();
        if (!tl.isEmpty()) {
            s.shade(tl.getPriorityMax());
        }
        return s;
    }

    static final Insets subtermPadding = new Insets(0, 4, 0, 0);

    public static SubButton make(NAR nar, Term t) {
        SubButton sb = new SubButton(paddingDefault, t, paddingDefault);
        sb.setValue(t);

        if (t instanceof Compound) {
            //Pane ig = new FlowPane();
            //ig.setMaxWidth(0);

            if (t.op().isStatement()) {
                Compound st = (Compound)t;
                sb.getChildren().setAll(
                    make(nar, st.term(0)),
                    make(t.op().str),
                    make(nar, st.term(1))
                );
            } else {
                sb.getChildren().add(make(t.op().str));

                for (Term x : ((Compound) t).terms()) {

                    SubButton subterm = make(nar, x);
                    subterm.setPadding(subtermPadding);

                    sb.getChildren().add(subterm);
                    //
                    //                //HACK
                    //                Rectangle space = new Rectangle(4f, 4f);
                    //                space.setFill(Color.TRANSPARENT);
                    //                sb.getChildren().add(space);
                }
            }
            //sb.add(ig);
        } else {
            sb.add(make(t.toString(true)));
        }
        sb.setOnMouseClicked(onTermClicked);

        return sb;
    }

    public static SubButton make(NAR nar, Task t) {
        SubButton sb = new SubButton(paddingDefault, t.term(), t.getPriority());

        if (t.term().volume() > 16) {
            sb.add(makeParagraph(t.toString()));
        } else {
            sb.add(make(nar, t.term()));
            sb.add(make(String.valueOf(t.getPunctuation())));
            //sb.add(make(space()));
        }

        float minScale = 0.25f;
        if (!t.isQuestOrQuestion()) {
            sb.scale(minScale + 2f * 0.75f * t.getTruth().getConfidence());
        }

        sb.shade(t.getPriority());

        return sb;
    }

    public void shade(float p) {
        setOpacity(0.25f + 0.75f * p);
    }


    public void scale(float v) {
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

        //setMaxWidth(0);
        setAlignment(Pos.CENTER_LEFT);

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

    private static final InvalidationListener TextNodeHoverListener  = new InvalidationListener() {

        @Override
        public void invalidated(Observable k) {

            Text kk = (Text) ((ReadOnlyProperty) k).getBean();

            //Text kk = (Text)
            //Label kk = (Label)l;
            kk.setUnderline(kk.isHover());
            if (kk.isHover()) {
                kk.setUnderline(true);
            } else {
                kk.setUnderline(false);
            }

        }
    };

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
