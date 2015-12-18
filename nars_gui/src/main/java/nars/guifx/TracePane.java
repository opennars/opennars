package nars.guifx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import nars.NAR;
import nars.util.data.list.CircularArrayList;
import nars.util.data.list.FasterList;
import nars.util.event.ArraySharingList;
import nars.util.event.On;
import nars.util.event.Topic;

import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/15/15.
 */
abstract public class TracePane extends LogPane implements ChangeListener<Parent>, Consumer<NAR> {

    public final NAR nar;
    /**
     * threshold for minimum displayable priority
     */
    public final DoubleProperty volume;
    private On reg;
    protected Node prev; //last node added
    ActivationTreeMap activationSet = null;
    //Pane cycleSet = null; //either displays one cycle header, or a range of cycles, including '...' waiting for next output while they queue
    boolean trace = false;
    boolean visible = false;
    ArraySharingList<Node> pending;

    final CircularArrayList<Node> toShow = new CircularArrayList<>(maxLines);

    public TracePane(NAR nar, DoubleProperty volume) {

        this.volume = volume;
        this.nar = nar;
        Topic.all(nar.memory, this::output,
            (k) -> !k.equals("eventConceptProcess") &&
                    !k.equals("eventConceptActivated")
        );

//            for (Object o : enabled)
//                filter.value(o, 1);

        parentProperty().addListener(this);


    }


    protected void output(Object channel, Object signal) {


        //double f = filter.value(channel);

        //temporary until filter working
        if (!trace && ("eventDerived".equals(channel) ||
                "eventTaskRemoved".equals(channel) ||
                "eventConceptChange".equals(channel)
        ))
            return;

        Node n = getNode(channel, signal);
        if (n != null) {
            //synchronized (toShow) {
            if (pending == null)
                pending = new ArraySharingList(Node[]::new); //Global.newArrayList();

            pending.add(n);
            prev = n;
            //}
        }
    }

    boolean activationTreeMap = false;

    abstract public Node getNode(Object channel, Object signal);




    @Override
    public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
        visible = (getParent() != null);
        if (reg == null) {
            reg = nar.memory.eventFrameStart.on(this);

        }

    }

    @Override
    public void accept(NAR n) {

        if (!visible) {
            reg.off();
            reg = null;
            return;
        }

        if (pending == null)
            return;

        Node[] p = pending.getCachedNullTerminatedArray();
        if (p == null) return;

        pending = null;
        //synchronized (nar) {
        int ps = p.length;
        int start = ps - Math.min(ps, maxLines);

        CircularArrayList<Node> s = this.toShow;
        int tr = ((ps - start) + s.size()) - maxLines;
        if (tr > ps) {
            s.clear();
        } else {
            s.removeFirst(tr);
        }

        for (int i = 0; i < ps; i++) {
            Node v = p[i];
            if (v == null)
                break;
            if (i >= start)
                s.add(v);
        }
        //}

        //if (queueUpdate)
        //Node[] c = s.toArray(new Node[s.size()]);
        if (!s.isEmpty()) {
            //if (c != null) {
            runLater(() -> commit(new FasterList(s)));
            //this.toShow = new CircularArrayList<>(maxLines);
        }
        //}

    }
}
