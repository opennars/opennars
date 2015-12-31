package nars.guifx;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import nars.$;
import nars.NAR;
import nars.util.data.list.CircularArrayList;
import nars.util.data.list.FasterList;
import nars.util.event.Active;
import nars.util.event.ArraySharingList;
import nars.util.event.On;
import nars.util.event.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/15/15.
 */
public abstract class TracePane extends LogPane implements ChangeListener, Consumer<NAR> {

    public final NAR nar;

    static final Logger logger = LoggerFactory.getLogger(TracePane.class);

    /**
     * threshold for minimum displayable priority
     */
    public final DoubleProperty volume;
    private Active events;
    private On reg;
    protected Node prev; //last node added
    //Pane cycleSet = null; //either displays one cycle header, or a range of cycles, including '...' waiting for next output while they queue
    boolean trace = false;
    ArraySharingList<Node> pending;

    final CircularArrayList<Node> toShow = new CircularArrayList<>(maxLines);
    private UnsynchronizedAppenderBase appender;

    public TracePane(NAR nar, DoubleProperty volume) {

        this.volume = volume;
        this.nar = nar;

//            for (Object o : enabled)
//                filter.value(o, 1);

        volume.addListener((v, p, n) -> {
            if (p.floatValue()!=0 && n.floatValue()==0) {
                disappear();
            } else {
                appear();
            }
        });

        sceneProperty().addListener(this);
        //parentProperty().addListener(this);


    }


    protected void output(Object channel, Object signal) {

        //double f = filter.value(channel);

        Node n = getNode(channel, signal);

        if (n != null) {
            append(n);
        }
    }

    public void commit(String message) {
        append(message);
        commit();
    }

    public void append(String message) {
        append(new Label(message));
    }

    public void append(Node n) {
        //synchronized (toShow) {
        if (pending == null)
            pending = new ArraySharingList(Node[]::new); //Global.newArrayList();

        pending.add(n);
        prev = n;
        //}
    }


    public abstract Node getNode(Object channel, Object signal);


    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {

        if (reg==null && getParent()!=null) {
            appear();
            return;
        }

        if (reg!=null && (getParent()==null || getScene() == null)) {
            disappear();
            return;
        }

    }



    @Override
    public void accept(NAR n) {

        commit();

    }

    public void commit() {
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

    public void disappear() {
        if (events!=null || reg!=null) {
            logger.info("silence");

            if (events!=null) {
                events.off();
                events =  null;
            }

            if (reg!=null) {
                reg.off();
                reg = null;
            }

            if (appender!=null) {
                $.logRoot.detachAppender(appender);
                appender.stop();
                appender = null;
            }
        }
    }
    public void appear() {
        if (reg==null && events == null) {
            reg = nar.memory.eventFrameStart.on(this);
            events = Topic.all(nar.memory, this::output,
                    (k) ->  !k.equals("eventConceptProcess") &&
                            !k.equals("eventConceptActivated") &&
                            !k.equals("eventConceptChanged") &&
                            !k.equals("eventTaskRemoved") &&
                            !k.equals("eventDerived")
            );

            appender = new UnsynchronizedAppenderBase() {

                @Override
                public void start() {
                    super.start();
                }

                @Override
                protected void append(Object eventObject) {
                    TracePane.this.append(eventObject.toString());
                }
            };
            appender.setContext($.logRoot.getLoggerContext());
            //appender.setEncoder(encoder);
            appender.start();
            $.logRoot.addAppender(appender);

            logger.info("active");
        }
        //if reg==null || events==null WTF
    }
}
