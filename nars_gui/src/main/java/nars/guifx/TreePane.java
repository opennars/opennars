package nars.guifx;

import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import nars.Global;
import nars.NAR;
import nars.event.FrameReaction;
import nars.nal.nal8.ImmediateOperation;
import nars.op.io.Echo;
import nars.task.Task;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by me on 8/10/15.
 */
public class TreePane extends BorderPane {

    private final TreeItem<Task> rootNode;
    private final TreeView<Task> tree;
    private final FrameReaction onFrame;

    final Set<Task> pendingTasks = Global.newHashSet(1);

    final Map<Task, TreeItem<Task>> tasks = Global.newHashMap();
    private final NAR nar;

    public TreePane(NAR n) {
        super();

        this.nar = n;

        rootNode = new TreeItem<Task>(new Echo("Tasks").newTask());
        tree = new TreeView<Task>(rootNode);
        tree.setCellFactory(new Callback<TreeView<Task>, TreeCell<Task>>() {
            @Override public TreeCell<Task> call(TreeView<Task> param) {
                return new TaskCell();
            }
        });

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
                setGraphic(new TaskLabel(getItem(), nar));
            }
            else {
                this.setTextFill(Color.WHITE);
                setGraphic(null);
            }

        }

    }



    protected void update() {
        nar.memory.forEachTask(true, t -> {
            pendingTasks.add(t);
        });
        Platform.runLater(() -> {

            Iterator<Map.Entry<Task, TreeItem<Task>>> ii = tasks.entrySet().iterator();
            while (ii.hasNext()) {
                Map.Entry<Task, TreeItem<Task>> t = ii.next();
                Task k = t.getKey();
                if (!pendingTasks.remove(k)) {
                    TreeItem<Task> tt = t.getValue();
                    tt.getParent().getChildren().remove(tt);
                    ii.remove();
                }
                else {
                    getItem(k);
                }
            }

            for (Task p : pendingTasks) {
                getItem(p);
            }
            pendingTasks.clear();
        });
    }

    protected TreeItem<Task> getItem(Task t) {
        if (t == null)
            return rootNode;

        TreeItem<Task> i = tasks.get(t);
        if (i == null) {
            i = new TreeItem<Task>(t);
            tasks.put(t, i);
            getItem(t.getParentTask()).getChildren().add(i);
        }

        update(t, i);


        return i;
    }

    private void update(Task t, TreeItem<Task> i) {
    }


}
