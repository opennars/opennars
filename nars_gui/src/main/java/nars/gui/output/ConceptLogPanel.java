package nars.gui.output;

import automenta.vivisect.Video;
import nars.Events;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.io.TextOutput;
import nars.nal.Task;
import nars.nal.concept.Concept;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by me on 4/19/15.
 */
public class ConceptLogPanel extends LogPanel implements Runnable {


    private final AbstractReaction conceptReaction;
    ConceptPanelBuilder b;
    VerticalPanel content = new VerticalPanel();

    final Deque<JComponent> pendingDisplay = new ArrayDeque(); //new ConcurrentLinkedDeque<>();

    int y = 0;

    public ConceptLogPanel(NAR c) {
        super(c);

        conceptReaction = new AbstractReaction(nar, Events.ConceptNew.class) {

            @Override
            public void event(Class event, Object[] args) {
                if (event == Events.ConceptNew.class) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateConcept((Concept)args[0],1.0f,"Conceptualized");
                        }
                    });
                }
            }
        };
        b = new ConceptPanelBuilder(nar);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public void setFontSize(float v) {

    }

    @Override
    protected void clearLog() {

        pendingDisplay.clear();
        content.removeAll();
        b.off();

        b = new ConceptPanelBuilder(nar);
        y = 0;
        remove(content);
        content = new VerticalPanel();
        add(content, BorderLayout.CENTER);

        doLayout();
        updateUI();
    }

    public void applyPriority(JComponent p, float priority) {
        //setFont(Video.monofont.deriveFont(12.0f + priority * 4f));

        p.setOpaque(true);

        final float hue = 0.3f + 0.5f * priority;

        Color c = Color.getHSBColor(hue, 0.4f, 0.2f + priority * 0.2f);

        p.setBackground(c);

        Color c2 = Color.getHSBColor(hue, 0.6f, 0.5f + priority * 0.5f);

        p.setBorder(BorderFactory.createMatteBorder(0, 14, 0, 0, c2));



        //updateUI();
    }

    @Override
    void print(Class channel, Object o) {

        float priority = 0;

        if (o instanceof Task) {

            Task t = (Task)o;

            priority = t.getPriority();


            Concept c = nar.concept(t.getTerm());
            if (c !=null) {
                updateConcept(c, priority, t.toString(nar.memory).toString());
                return;
            }
        }

        CharSequence s = TextOutput.getOutputString(channel, o, showStamp, nar, new StringBuilder());
        JLabel jl = new JLabel(s.toString());
        jl.setFont(Video.monofont);
        applyPriority(jl, priority);
        append(jl);

    }


    protected void updateConcept(Concept c, float priority, String status) {
        ConceptPanelBuilder.ConceptPanel cp = b.getFirstPanelOrCreateNew(c, true, false, 64);
        //cp.setMessage(...)
        applyPriority(cp, priority);
        append(cp);
    }

    protected synchronized void append(JComponent j) {

        boolean queueUpdate = false;
        if (pendingDisplay.isEmpty()) {
            queueUpdate = true;
        }

        pendingDisplay.add(j);

        if (queueUpdate)
            SwingUtilities.invokeLater(this);

    }

        @Override
        public void run() {

            while (!pendingDisplay.isEmpty()) {

                JComponent j = pendingDisplay.removeFirst();

                if (j instanceof ConceptPanelBuilder.ConceptPanel)
                    content.remove(j);

                content.addPanel(y++, j);
                content.scrollBottom();
                doLayout();
                updateUI();
            }
        }

    @Override
    void limitBuffer(int incomingDataSize) {

    }
}
