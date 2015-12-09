package nars.guifx;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import nars.NAR;
import nars.Premise;
import nars.concept.Concept;
import nars.task.Task;
import nars.util.data.list.CircularArrayList;
import nars.util.event.ArraySharingList;
import nars.util.event.On;
import nars.util.event.Topic;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/15/15.
 */
public class TracePane extends LogPane {

    private final NAR nar;
    /**
     * threshold for minimum displayable priority
     */
    private final DoubleProperty volume;
    private On reg;
    private Node prev; //last node added
    ActivationTreeMap activationSet = null;
    //Pane cycleSet = null; //either displays one cycle header, or a range of cycles, including '...' waiting for next output while they queue
    boolean trace = false;
    boolean visible = false;
    ArraySharingList<Node> pending;

    final CircularArrayList<Node> toShow = new CircularArrayList<>(maxLines);

    public TracePane(NAR nar, DoubleProperty volume) {

        this.volume = volume;
        this.nar = nar;
        Topic.all(nar.memory, this::output);

//            for (Object o : enabled)
//                filter.value(o, 1);

        parentProperty().addListener(e-> {
            visible = (getParent()!=null);
            if (reg == null) {
                reg = nar.memory.eventFrameStart.on((n) -> {

                    if (!visible) {
                        reg.off();
                        reg = null;
                        return;
                    }

                    if (pending == null)
                        return;

                    Node[] p = pending.getCachedNullTerminatedArray();
                    if (p != null) {
                        pending = null;
                        //synchronized (nar) {
                        int ps = p.length;
                        int start = ps - Math.min(ps, maxLines);

                        int tr = ((ps - start) + toShow.size()) - maxLines;
                        if (tr > ps) {
                            toShow.clear();
                        } else {
                            //remove first N
                            for (int i = 0; i < tr; i++)
                                toShow.removeFirst();
                        }

                        for (int i = 0; i < ps; i++) {
                            Node v = p[i];
                            if (v == null)
                                break;
                            if (i >= start)
                                toShow.add(v);
                        }
                        //}

                        //if (queueUpdate)
                        Node[] c = toShow.toArray(new Node[toShow.size()]);
                        if (c != null) {
                            runLater(()->commit(c));
                        }
                    }
                });

            }
        });



    }



    protected void output(Object channel, Object signal) {



        //double f = filter.value(channel);

        //temporary until filter working
        if (!trace && ("eventDerived".equals(channel) ||
                "eventTaskRemoved".equals(channel) ||
                "eventConceptChange".equals(channel)
        ) )
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

    private Node getNode(Object channel, Object signal) {
        //noinspection IfStatementWithTooManyBranches
        if ("eventConceptActivated".equals(channel)) {
            boolean newn = false;
            if (activationSet == null && activationTreeMap) {
                activationSet =
                        //new ActivationSet();
                        new ActivationTreeMap((Concept) signal);

                //activationSet.prefWidth(getWidth());
                activationSet.width.set(400);
                activationSet.height.set(100);
                newn = true;
            } else if (activationSet != null) {
                activationSet.add((Concept) signal);
                newn = false;
            }

            return !newn ? null : activationSet;
        } else if ("eventCycleEnd".equals(channel)) {
            if ((prev instanceof CycleActivationBar)) {
                ((CycleActivationBar) prev).setTo(nar.time());
                return null;
            } else {
                return new CycleActivationBar(nar.time());
            }
//            if (activationSet!=null) {
//                activationSet.commit();
//                activationSet = null; //force a new one
//            }
            //return null;
            //
        } else if ("eventFrameStart".equals(channel)) {
            return null;
            //
        } else if ("eventInput".equals(channel)) {
            Task t = (Task) signal;
            return t.getPriority() >= volume.get() ? new TaskLabel(t, nar) : null;
        } else if (signal instanceof Premise) {
            //return new PremisePane((Premise)signal);
            return null;
        } else {
            return new Label(
                    //channel.toString() + ": " +
                    channel + ": " +
                            signal.toString());
        }
    }


}
