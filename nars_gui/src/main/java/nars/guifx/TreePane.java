package nars.guifx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import nars.NAR;
import nars.event.FrameReaction;
import nars.op.io.Echo;
import nars.task.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Created by me on 8/10/15.
 */
public class TreePane extends BorderPane {

    private final TreeItem<Task> rootNode;
    private final TreeView<Task> tree;
    private final FrameReaction onFrame;

    final Set<Task> pendingTasks = new LinkedHashSet<>(); //Global.newHashSet(1);

    final Map<Task, TreeItem<Task>> tasks = new LinkedHashMap(); //Global.newHashMap();
    final Map<Task, TaskLabel> labels = new WeakHashMap();

    private final NAR nar;

    final Function<Task,TaskLabel> labelBuilder;

    private Node getLabel(final Task item) {
        return labels.computeIfAbsent(item, labelBuilder);
    }

    final AtomicBoolean ready = new AtomicBoolean(true);

    public TreePane(NAR n) {
        super();

        this.nar = n;
        this.labelBuilder = (i) -> {
            return new TaskLabel(i, nar);
        };

        rootNode = new TreeItem(new Echo(""));
        tree = new TreeView<Task>(rootNode);
        tree.setCellFactory(new Callback<TreeView<Task>, TreeCell<Task>>() {
            @Override public TreeCell<Task> call(TreeView<Task> param) {
                return new TaskCell();
            }
        });

        tree.setShowRoot(false);

        setCenter(tree);

        onFrame = new FrameReaction(n) {
            @Override public void onFrame() {

                update();
            }
        };

        autosize();
    }

    public class TaskCell extends TreeCell<Task> {

        public TaskCell() {
            super();
            setEditable(false);
        }


        @Override
        public void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
//
//            setText(null);
//
//            if (empty) {
//            }
//            else {
//                if (item == null) {
//                    //setText("?");
//                } else if (item instanceof ImmediateOperation.ImmediateTask) {
//                    //setText(item.toString());
//                } else {
//                    //.setText(item.toString(nar.memory).toString());
//
//                }
//
//            }

            if (getItem()!=null) {
                setGraphic(getLabel(getItem()));
            }
            else {
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
                pendingTasks.add(t);
            });
        }

        Platform.runLater(() -> {


            Iterator<Map.Entry<Task, TreeItem<Task>>> ii = tasks.entrySet().iterator();

            synchronized (pendingTasks) {
                while (ii.hasNext()) {
                    Map.Entry<Task, TreeItem<Task>> ent = ii.next();
                    Task k = ent.getKey();
                    if (!pendingTasks.remove(k)) {
                        //task removed
                        TreeItem<Task> tt = ent.getValue();
                        tt.getParent().getChildren().remove(tt);
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

    protected TreeItem<Task> getItem(final Task t) {
        if (t == null)
            return rootNode;

        TreeItem<Task> i = tasks.computeIfAbsent(t, _t -> {
            Task pt = _t.getParentTask();
            if (pt == _t)
                throw new RuntimeException(t + " is its own parent task");

            final TreeItem<Task> ii = new TreeItem<Task>(_t);
            getItem(pt).getChildren().add(ii);
            return ii;
        });

        return i;
    }

    private static void update(final Task t, final TreeItem<Task> i) {
        final Node g = i.getGraphic();
        if (g instanceof Runnable)
            ((Runnable)g).run();
    }


}
