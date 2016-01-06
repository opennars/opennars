//package nars.gui.output;
//
//import javolution.util.FastSet;
//import nars.NAR;
//import nars.Video;
//import nars.concept.Concept;
//import nars.event.ConceptReaction;
//import nars.io.out.TextOutput;
//import nars.task.Task;
//import nars.util.data.id.Named;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.function.Consumer;
//
///**
// * Created by me on 4/19/15.
// */
//public class ConceptLogPanel extends LogPanel implements Runnable {
//
//
//    private final ConceptReaction conceptReaction;
//    ConceptPanelBuilder b;
//    VerticalPanel content = new VerticalPanel();
//
//    Set<JComponent> pendingDisplay = new FastSet().atomic(); //new ConcurrentLinkedDeque<>();
//
//    int y = 0;
//
//    final int maxItems = 256;
//    private String filter = null;
//
//    public ConceptLogPanel(NAR c) {
//        super(c);
//
//        conceptReaction = new ConceptReaction(nar.memory) {
//
//            @Override
//            public void event(Class event, Object[] args) {
//                //TODO prevent this from even being called by correctly removing the event handler
//                if (!isShowing())
//                    return;
//
//            }
//
//            @Override
//            public void onConceptActive(Concept c) {
//                updateConcept(c, 1.0f, "Conceptualized");
//            }
//
//            @Override
//            public void onConceptForget(Concept c) {
//
//            }
//
//        };
//        b = newBuilder();
//        add(content, BorderLayout.CENTER);
//
//        JTextField quickfilter = new JTextField(16);
//        quickfilter.setToolTipText("Quick filter");
//        quickfilter.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent e) {
//                String f = quickfilter.getText();
//                setFilter(f);
//            }
//        });
//        menu.add(quickfilter);
//    }
//
//
//    protected void setFilter(String f) {
//        if (f.isEmpty())
//            this.filter = null;
//        else
//            this.filter = f;
//
//        content.forEach(filterComponent);
//    }
//
//    final Consumer<Component> filterComponent = new Consumer<Component>() {
//
//        @Override
//        public void accept(Component x) {
//            boolean vis = true;
//
//            if (filter != null) {
//                if (x instanceof Named) {
//                    String s = ((Named) x).name().toString();
//                    vis = s.contains(filter);
//                } else {
//                    vis = false;
//                }
//            }
//
//            x.setVisible(vis);
//        }
//    };
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        super.visibility(appearedOrDisappeared);
//        if (!appearedOrDisappeared) {
//            off();
//        }
//    }
//
//    @Override
//    public void setFontSize(float v) {
//
//    }
//
//    protected void off() {
//        pendingDisplay = new FastSet().atomic();
//        conceptReaction.off();
//        content.removeAllVertically();
//        b.off();
//    }
//
//    @Override
//    protected void clearLog() {
//
//        off();
//
//        y = 0;
//        b = newBuilder();
//        content.removeAllVertically();
//
//    }
//
//    private ConceptPanelBuilder newBuilder() {
//         return new ConceptPanelBuilder(nar) {
//
//             @Override
//             public boolean isAutoRemove() {
//                 return false; //concept log panel will manage removals
//             }
//         };
//    }
//
//    public void applyPriority(Class channel, JComponent p, float priority) {
//        //setFont(Video.monofont.deriveFont(12.0f + priority * 4f));
//
//        p.setOpaque(true);
//
//        final float hue = Video.hashFloat(channel.hashCode());
//
//        Color c = Color.getHSBColor(hue, 0.4f + priority * 0.2f, 0.2f + priority * 0.2f);
//
//        p.setBackground(c);
//
//        //Color c2 = Color.getHSBColor(hue, 0.6f, 0.5f + priority * 0.5f);
//
//        p.setBorder(BorderFactory.createMatteBorder(4, 8, 4, 0, c));
//
//
//        //updateUI();
//    }
//
//
//    final StringBuilder buffer = new StringBuilder();
//
//    void print(Class channel, Object o) {
//        float priority = 0;
//
//
//        if (o instanceof Task) {
//
//            Task t = (Task) o;
//
//            priority = t.getPriority();
//
//
//            Concept c = nar.concept(t.getTerm());
//            if (c != null) {
//                updateConcept(c, priority, t.toString(nar.memory).toString());
//                return;
//            }
//        }
//
//        JLabel jl;
//
//        synchronized (buffer) {
//
//            buffer.setLength(0);
//            CharSequence s = TextOutput.append(buffer, channel, o, true, showStamp, 0f, nar);
//            if (s == null)
//                return; //ex: too low priority
//
//            jl = new JLabel(s.toString());
//            jl.setDoubleBuffered(true);
//            jl.setIgnoreRepaint(true);
//
//            applyPriority(channel, jl, priority);
//            append(jl);
//
//            //throw new RuntimeException("ConceptLogPanel: " + channel + " " + o + " NULL");
//        }
//
//
//
//    }
//
//
//    protected void updateConcept(Concept c, float priority, String status) {
//        //TODO execute at the end of the cycle, not in swing thread
//        //if it executes in-line with the cycle it can miss details that should be shown
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                ConceptPanelBuilder.ConceptPanel cp = b.getFirstPanelOrCreateNew(c, true, false, 64);
//                //cp.setMessage(...)
//                applyPriority(c.getTerm().getClass(), cp, priority);
//                append(cp);
//            }
//        });
//    }
//
//
//    protected synchronized void append(JComponent j) {
//        if (pendingDisplay.add(j))
//            SwingUtilities.invokeLater(this);
//    }
//
//    @Override
//    public void run() {
//
//        if (pendingDisplay.size() == 0) return;
//
//        Iterator<JComponent> i = pendingDisplay.iterator();
//        while (i.hasNext()) {
//
//            JComponent j = i.next();
//
//            if (j instanceof ConceptPanelBuilder.ConceptPanel) {
//                content.removeVertically(j);
//            }
//
//            filterComponent.accept(j);
//
//            content.addVertically(j);
//
//            i.remove();
//        }
//
//
//        for (Component c : content.limit(maxItems)) {
//            if (c instanceof ConceptPanelBuilder.ConceptPanel) {
//                b.remove((ConceptPanelBuilder.ConceptPanel) c);
//                ((ConceptPanelBuilder.ConceptPanel)c).closed = true;
//            }
//        }
//
//
//        content.updateUI();
//
//        SwingUtilities.invokeLater(content::scrollBottom);
//    }
//
//    @Override
//    void limitBuffer(int incomingDataSize) {
//
//    }
//
// }
