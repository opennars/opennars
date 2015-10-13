package nars.guifx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import nars.NAR;
import nars.task.Task;
import org.apache.commons.math3.util.Precision;

import java.util.concurrent.atomic.AtomicBoolean;


public class AutoLabel extends Label implements ChangeListener {

    private final NAR nar;
    private final String prefix;

    private Task task;
    private TaskSummaryIcon summary;
    //private final NSlider slider;
    private float lastPri = -1;
    public final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private String text;

    public AutoLabel(String prefix, Task task, NAR n) {
        super();

        this.prefix = prefix;
        this.task = task;
        this.nar = n;
        text = prefix + task.toString(new StringBuilder(), nar.memory(), true, false, false, false).toString();
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        parentProperty().addListener(this);
        setOnMouseClicked(onMouseClick);
    }

    static private final EventHandler<? super MouseEvent> onMouseClick = (e) -> {
        AutoLabel a = (AutoLabel) e.getSource();
        Task t = a.task;
        NARfx.newWindow(a.nar, t);
    };

    public void enablePopupClickHandler(NAR nar) {

        setOnMouseClicked(e -> {
            NARfx.newWindow(nar, task);
//            Term t = task.getTerm();
//            if (t!=null) {
////                Concept c = nar.concept(t);
////                if (c != null) {
////                    NARfx.window(nar, c);
////                }
//
//            }
        });

    }

    public AutoLabel(Task task, NAR nar) {
        this("", task, nar);
    }

    public void update() {

        if (summary==null)
            return;

        float pri = task.getBudget().getPriorityIfNaNThenZero();
        if (Precision.equals(lastPri, pri, 0.07)) {
            return;
        }
        lastPri = pri;

        summary.run();

        //double sc = 0.5 + 1.0 * pri;
        //setScaleX(sc);
        //setScaleY(sc);
        //setFont(NARfx.mono((pri*12+12)));


        setStyle(JFX.fontSize( ((1.0f + pri)*100.0f) ) );

        setTextFill(JFX.grayscale.get(pri*0.5+0.5));

    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {

        /* parent changed */
        getChildren().clear();

        if (newValue == null) {
            //unparented
            summary = null;
            return;
        }

        setGraphicTextGap(0);
        getStylesheets().setAll();
        getStyleClass().setAll();


        //setTooltip(new Tooltip().on);

        setText(text);


        //label.getStyleClass().add("tasklabel_text");
        //setMouseTransparent(true);
        //label.setCacheHint(CacheHint.SCALE);
        //setPickOnBounds(false);
        //setSmooth(false);
        //setCache(true);


        int iconWidth = 30;
        int iconSpacing = 1;

        setCenterShape(false);
        setPickOnBounds(true);

        summary = new TaskSummaryIcon(task, this).width(iconWidth);

//        summary.hoverProperty().addListener(c -> {
//            if (summary.isHover()) {
//                Popup p = new Popup();
//
//
//                p.getContent().add(
//                        new BorderPane(
//                                new NSlider(iconWidth, 20).set(0, 0, 1)
//                        )
//                );
//
//                p.show(TaskLabel.this, 0, 0);
//
//                p.setAutoHide(true);
//                p.setHideOnEscape(true);
//            }
//        });
        /*slider = new NSlider(iconWidth, 20).set(0, 0, 1);*/




        /*getChildren().setAll(
                summary, label
        );*/
        setGraphic(summary);


        setTextAlignment(TextAlignment.LEFT);
//        setAlignment(summary, Pos.CENTER_LEFT);
//        setAlignment(label, Pos.CENTER_LEFT);

        /*slider.setOpacity(0.5);
        slider.setBlendMode(BlendMode.HARD_LIGHT);*/
        summary.setMouseTransparent(false);


        update();

        layout();



        /*setOnMouseEntered(e-> {
            if (e.isPrimaryButtonDown()) {
                System.out.println("dragged: " + task);
                selected.set(!selected.get());
            }
        });*/
        AtomicBoolean dragging= new AtomicBoolean(false);
        EventHandler<MouseEvent> onDrag = e -> {
            if (dragging.compareAndSet(false, true)) {
                //System.out.println("dragged: " + task);
                selected.set(!selected.get());
            }
        };
        EventHandler<MouseEvent> clearDrag = e -> {
            //System.out.println("exited: " + task);
            dragging.set(false);
        };


        setOnDragOver((e)->{
            onDrag.handle(null);
        });
        setOnDragDetected(e->{
            clearDrag.handle(null);
            startFullDrag();
        });
        setOnMouseDragEntered(onDrag);
        setOnMouseReleased(clearDrag);


        final String selectedClass = "selected";
        selected.addListener((c,p,v) -> {
            if (v) {
                getStyleClass().add(selectedClass);
            }
            else {
                getStyleClass().remove(selectedClass);
            }
        });

        setCache(true);

    }
}
