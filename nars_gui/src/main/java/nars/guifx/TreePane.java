package nars.guifx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import nars.NAR;
import nars.event.FrameReaction;
import nars.op.io.echo;
import nars.task.Task;
import org.infinispan.commons.util.concurrent.ConcurrentWeakKeyHashMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/10/15.
 */
public class TreePane extends BorderPane {

    private final TaskTreeItem rootNode;
    private final TreeView<Task> tree;
    private final FrameReaction onFrame;

    final Set<Task> pendingTasks = new LinkedHashSet<>(); //Global.newHashSet(1);

    final Map<Task, TaskTreeItem> tasks = new ConcurrentWeakKeyHashMap<>();


    private final NAR nar;

    public final DoubleProperty minPriority;

    final Function<Task, TaskLabel> labelBuilder;

    final AtomicBoolean ready = new AtomicBoolean(true);

    public TreePane(NAR n) {
        super();

        this.nar = n;
        this.labelBuilder = (i) -> {
            return new TaskLabel(i, nar);
        };

        rootNode = new TaskTreeItem(echo.echo("root"));
        tree = new TreeView<Task>(rootNode);
        tree.setCellFactory(new Callback<TreeView<Task>, TreeCell<Task>>() {
            @Override
            public TreeCell<Task> call(TreeView<Task> param) {
                return new TaskCell();
            }
        });

        tree.setShowRoot(false);


        setCenter(tree);

        onFrame = new FrameReaction(n) {
            @Override
            public void onFrame() {

                update();
            }
        };
        {
            NSliderFX ns;
            Pane p = new FlowPane(
                    new Label("Pri Min"),
                    ns = new NSliderFX(80, 40)
            );

            minPriority = ns.value;

            minPriority.addListener((v) -> {
                update();
            });

            setTop(p);
        }

        autosize();
    }


    public class TaskCell extends TreeCell<Task> {

        public TaskCell() {
            super();
            setEditable(false);
        }


        @Override
        public void updateItem(Task t, boolean empty) {
            super.updateItem(t, empty);
//
//            setText(null);
//
//            if (empty) {
//            }
//            else {
//                if (t == null) {
//                    //setText("?");
//                } else if (t instanceof ImmediateOperation.ImmediateTask) {
//                    //setText(t.toString());
//                } else {
//                    //.setText(t.toString(nar.memory).toString());
//
//                }
//
//            }

            if (getItem() != null) {
                setGraphic(tasks.get(t).label);
            } else {
                //this.setTextFill(Color.WHITE);
                setGraphic(null);
            }

        }


    }


    protected void update() {

        if (!ready.compareAndSet(true, false))
            return;

        synchronized (pendingTasks) {
            pendingTasks.clear();
            nar.memory.forEachTask(true, t -> {
                if (visible(t))
                    pendingTasks.add(t);
                else
                    hide(t);
            });
        }

        runLater(() -> {


            Iterator<Map.Entry<Task, TaskTreeItem>> ii = tasks.entrySet().iterator();

            synchronized (pendingTasks) {
                while (ii.hasNext()) {
                    Map.Entry<Task, TaskTreeItem> ent = ii.next();
                    Task k = ent.getKey();
                    if (!pendingTasks.remove(k) || !visible(k)) {
                        //task removed
                        hide(k);
                        ii.remove();
                    } else {
                        //existing task
                        getItem(k);
                    }
                }

                //new task
                for (Task p : pendingTasks) {
                    getItem(p);
                }
            }

            tasks.entrySet().forEach(
                    t -> update(t.getKey(), t.getValue()));

            ready.set(true);

        });
    }

    private boolean visible(Task k) {
        return k.getPriority() >= minPriority.get();
    }

    public class TaskTreeItem extends TreeItem<Task> {
        public final TaskLabel label;

        public TaskTreeItem(Task t) {
            super(t);
            label = new TaskLabel(t, nar);
            label.setVisible(false);
        }
    }

    protected TaskTreeItem getItem(final Task t) {
        if (t == null)
            return rootNode;

        TaskTreeItem i = tasks.computeIfAbsent(t, _t -> {


            return new TaskTreeItem(_t);

        });

        if (visible(t)) {
            if (!i.label.isVisible())
                reparent(i);
        }
        else {
            if (i.label.isVisible())
                hide(t);
            return null;
        }

        return i;
    }

    private TaskTreeItem reparent(TaskTreeItem ii) {

        Task t = ii.getValue();
        Task pt = t.getParentTask();
        if (pt == t)
            throw new RuntimeException(t + " is its own parent task");

        TaskTreeItem parent = getItem(pt);
        if (parent!=null) {
            parent.getChildren().add(ii);
            ii.label.setVisible(true);
        }
        else {
            hide(t);
        }

        return ii;
    }

    private void update(final Task t, final TreeItem<Task> i) {
        if (!visible(t))
            hide(t);

        final Node g = i.getGraphic();
        if (g instanceof Runnable)
            ((Runnable) g).run();
    }

    private void hide(Task t) {
        TaskTreeItem tt = tasks.get(t);
        if (tt == null)
            return;

        TaskLabel tp = tt.label;
        tp.setVisible(false);

        TreeItem<Task> pp = tt.getParent();
        if (pp != null) {
            pp.getChildren().remove(tt);
        }

    }


}
