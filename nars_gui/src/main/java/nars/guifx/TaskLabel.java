package nars.guifx;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import nars.NAR;
import nars.task.Task;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/16/15.
 */
public class TaskLabel extends AutoLabel<Task> {
    private final NAR nar;
    private TaskSummaryIcon summary;
    //private final NSlider slider;


    public TaskLabel(Task task, NAR n) {
        super(task);
        nar = n;

        int iconWidth = 30;
        summary = new TaskSummaryIcon(obj, this);
        summary.width(iconWidth);

        String selectedClass = "selected";
        selected.addListener((c, p, v) -> {
            if (v) {
                getStyleClass().add(selectedClass);
            } else {
                getStyleClass().remove(selectedClass);
            }
        });


        setOnMouseClicked(onMouseClick);

        runLater(() -> changed(null, null, null));

    }

    private static final EventHandler<? super MouseEvent> onMouseClick = (e) -> {
        TaskLabel a = (TaskLabel) e.getSource();
        Task t = a.obj;
        NARfx.newWindow(a.nar, t);
    };

    @Override
    public void update() {

        if (summary == null)
            return;


        summary.run();


        //double sc = 0.5 + 1.0 * pri;
        //setScaleX(sc);
        //setScaleY(sc);
        //setFont(NARfx.mono((pri*12+12)));


    }

    @Override
    protected float getPriority(Task obj) {
        return obj.getBudget().getPriorityIfNaNThenZero();
    }

    public void enablePopupClickHandler(NAR nar) {

        setOnMouseClicked(e -> {
            NARfx.newWindow(nar, obj);
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

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        super.changed(observable, oldValue, newValue);
        if (newValue == null) {
            //unparented
            summary = null;
            return;
        }




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


//        setAlignment(summary, Pos.CENTER_LEFT);
//        setAlignment(label, Pos.CENTER_LEFT);

    /*slider.setOpacity(0.5);
    slider.setBlendMode(BlendMode.HARD_LIGHT);*/
        summary.setMouseTransparent(false);



    /*setOnMouseEntered(e-> {
        if (e.isPrimaryButtonDown()) {
            System.out.println("dragged: " + task);
            selected.set(!selected.get());
        }
    });*/
//        AtomicBoolean dragging = new AtomicBoolean(false);
//        EventHandler<MouseEvent> onDrag = e -> {
//            if (dragging.compareAndSet(false, true)) {
//                //System.out.println("dragged: " + task);
//                selected.set(!selected.get());
//            }
//        };
//        EventHandler<MouseEvent> clearDrag = e -> {
//            //System.out.println("exited: " + task);
//            dragging.set(false);
//        };
//
//
//        setOnDragOver((e) -> onDrag.handle(null));
//        setOnDragDetected(e -> {
//            clearDrag.handle(null);
//            startFullDrag();
//        });
//        setOnMouseDragEntered(onDrag);
//        setOnMouseReleased(clearDrag);
//



    }

    @Override
    protected String getText(Task task) {
        if (nar!=null)
            return task.appendTo(new StringBuilder(), nar.memory, true, false, false, false).toString();
        return "";
    }
}
