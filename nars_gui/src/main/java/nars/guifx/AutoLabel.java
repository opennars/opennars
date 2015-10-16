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

import java.util.concurrent.atomic.AtomicBoolean;


public abstract class AutoLabel<T> extends Label implements ChangeListener {

    public static class TaskLabel extends AutoLabel<Task> {
        private final NAR nar;
        private TaskSummaryIcon summary;
        //private final NSlider slider;
        protected final String prefix;

        public TaskLabel(String prefix, Task task, NAR n) {
            super(task);
            this.prefix = prefix;
            this.nar = n;
            setOnMouseClicked(onMouseClick);

        }

        public TaskLabel(Task obj, NAR n) {
            this("", obj, n);
        }

        static private final EventHandler<? super MouseEvent> onMouseClick = (e) -> {
            TaskLabel a = (TaskLabel) e.getSource();
            Task t = a.obj;
            NARfx.newWindow(a.nar, t);
        };

        public void update() {

            if (summary==null)
                return;


            summary.run();

            //double sc = 0.5 + 1.0 * pri;
            //setScaleX(sc);
            //setScaleY(sc);
            //setFont(NARfx.mono((pri*12+12)));




        }

        @Override protected float getPriority(Task obj) {
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


            int iconWidth = 30;

            summary = new TaskSummaryIcon(obj, this).width(iconWidth);

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


        }

        @Override
        protected String getText(Task task) {
            return prefix + task.appendTo(new StringBuilder(), nar.memory(), true, false, false, false).toString();
        }
    }


    protected T obj;
    protected float lastPri = -1;
    public final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    protected String text;

    public AutoLabel(T obj) {
        super();

        this.obj = obj;

        setText(text = getText(obj));

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        changed(null, null, null);

        //parentProperty().addListener(this);


    }

    protected abstract String getText(T t);

    abstract public void update();

    //TODO use a DoubleProperty
    protected abstract float getPriority(T obj);

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {

        /* parent changed */
        //getChildren().clear();

        setGraphicTextGap(0);
        //getStylesheets().setAll();
        //getStyleClass().setAll();


        //setTooltip(new Tooltip().on);

        //setText(text);


        //label.getStyleClass().add("tasklabel_text");
        //setMouseTransparent(true);
        //label.setCacheHint(CacheHint.SCALE);
        //setPickOnBounds(false);
        //setSmooth(false);
        //setCache(true);


        setCenterShape(false);
        setPickOnBounds(true);

        setTextAlignment(TextAlignment.LEFT);

        float pri = getPriority(obj);
//        if (Precision.equals(lastPri, pri, 0.07)) {
//            return;
//        }
        lastPri = pri;
        setStyle(JFX.fontSize( ((1.0f + pri)*100.0f) ) );

        setTextFill(JFX.grayscale.get(pri*0.5+0.5));

        update();
        layout();
        //setCache(true);

    }
}
