package nars.gui.output;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import nars.Events;
import nars.Events.OUT;
import nars.NAR;
import nars.io.TextOutput;
import nars.nal.concept.Concept;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.term.Term;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Deque;


public class SwingLogText extends SwingText {
    final Deque<LogLine> pendingDisplay = new ArrayDeque(); //new ConcurrentLinkedDeque<>();
    public final Runnable update = new Runnable() {

        //final Rectangle bottom = new Rectangle(0,Integer.MAX_VALUE-1,1,1);

        @Override
        public void run() {

            while (pendingDisplay.size() > 0) {
                LogLine l = pendingDisplay.removeFirst();
                print(l);
            }

            limitBuffer();

            /*try {
                //scrollRectToVisible(bottom);
            }
            catch (Exception e) { } */
        }
    };
    private final NAR nar;
    private final ConceptPanelBuilder cpBuilder;
    public boolean showStamp = false;
    private JScrollPane scroller;


    public SwingLogText(NAR n) {
        super();

        this.nar = n;

        this.cpBuilder = new ConceptPanelBuilder(n);
    }

//    @Override
//    public void paint(Graphics g) {
//        super.paint(g); //To change body of generated methods, choose Tools | Templates.
//        if (isVisible()) {
//
////            try {
////
////                TextUI mapper = getUI();
////
////                
////                Rectangle r = mapper.modelToView(this, getCaretPosition());
////                System.out.println("caret: " + r);
////
////            } catch (Exception e) {
////
////                System.err.println("Problem painting cursor");
////
////            }
//
//            //scrollUpdate();
//        }
//
//    }

    void setScroller(JScrollPane scroller) {
        this.scroller = scroller;
        /*scroller.getViewport().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    
                    //scrollUpdate();

                }
            });*/
    }

//    protected void scrollUpdate() {
//        int docLen = doc.getLength();
//        if (docLen > 0) {
//            //JViewport viewport = (JViewport) e.getSource();
//            Rectangle viewRect = scroller.getViewport().getViewRect();
//
//            Point p = viewRect.getLocation();
//            int startIndex = viewToModel(p);
//
//            p.x += viewRect.width;
//            p.y += viewRect.height;
//            int endIndex = viewToModel(p);
//
//            for (int offset = endIndex; offset < startIndex;) {
//                try {
//                    //System.out.println(" " + offset);
//                    
//                    onLineVisible(offset);
//                    
//                    offset = Utilities.getRowStart(SwingLogText.this, offset) - 1;
//                    
//                } catch (BadLocationException ex) {
//                    Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            //System.out.println("< -- (" + endIndex + ", " + startIndex);
//        }        
//    }

    protected void onLineVisible(int offset) {
    }

    final StringBuilder buffer = new StringBuilder();

    public void output(final Class c, final Object o) {
        pendingDisplay.addLast(new LogLine(c, o));

        if (pendingDisplay.size() == 1) {
            //only invoke update after the first has been added
            SwingUtilities.invokeLater(update);
        }
    }

    protected int print(final LogLine l) {

        Class c = l.c;
        Object o = l.o;
        float priority = 1f;


        if (c != OUT.class) {
            //pad the channel name to max 6 characters, right aligned

            String n = c.getSimpleName();
            final int nl = n.length();
            if (nl > 6)
                n = n.substring(0, 6);
            switch (n.length()) {
                case 0:
                    break;
                case 1:
                    n = "     " + n;
                    break;
                case 2:
                    n = "    " + n;
                    break;
                case 3:
                    n = "   " + n;
                    break;
                case 4:
                    n = "  " + n;
                    break;
                case 5:
                    n = " " + n;
                    break;
            }
            Color chanColor = Video.getColor(c.getClass().hashCode(), 0.8f, 0.8f);
            print(chanColor, n);
        } else {
            if (o instanceof Task) {
                Task t = (Task) o;
                Sentence s = t.sentence;
                if (s != null) {
                    priority = t.budget.getPriority();
                    printBlock(LogPanel.getPriorityColor(priority), "  ");

                    TruthValue tv = s.truth;
                    if (tv != null) {
                        printBlock(LogPanel.getFrequencyColor(tv.getFrequency()), "  ");
                        printBlock(LogPanel.getConfidenceColor(tv.getConfidence()), "  ");
                    } else if (t.getBestSolution() != null) {
                        printBlock(LogPanel.getStatementColor('=', priority), "    ");
                    } else {
                        printBlock(LogPanel.getStatementColor(s.punctuation, priority), "    ");
                    }
                }
            }
        }


        CharSequence text = TextOutput.getOutputString(c, o, showStamp, nar, buffer);
        StringBuilder sb = new StringBuilder(text.length() + 2);
        sb.append(' ');
        if ((text.length() > maxLineWidth) && (c != Events.ERR.class))
            sb.append(text.subSequence(0, maxLineWidth));
        else
            sb.append(text);

        if (sb.charAt(sb.length() - 1) == '\n') {
            sb = sb.delete(sb.length()-1, sb.length());
            //throw new RuntimeException(sb + " should not end in newline char");
        }


        if (o instanceof Task) {
            Task t = (Task) o;
            float tc = 0.5f + 0.5f * priority;
            Color textColor = new Color(tc, tc, tc);
            print(textColor, sb.toString(), new ConceptAction(t.getTerm()));

            print(Color.GRAY, "\n"); //print the newline separately without the action handler
        }
        else {
//        float tc = 0.75f + 0.25f * priority;
//        Color textColor = new Color(tc,tc,tc);
            sb.append('\n');
            print(Color.GRAY, sb.toString());
        }



        return doc.getLength();

    }

//    public class TaskIcon extends NCanvas {
//
//        public TaskIcon() {
//            super();
//            setMaximumSize(new Dimension(50,10));
//            setPreferredSize(new Dimension(50,10));
//            setSize(50,10);
//            
//            Graphics2D g = getBufferGraphics();
//            
//            showBuffer(g);
//        }
//        
//        
//        
//    }

    public static class LogLine {
        public final Class c;
        public final Object o;

        public LogLine(Class c, Object o) {
            this.c = c;
            this.o = o;
        }

    }

    public class ConceptAction extends AbstractAction {
        private final Term term;
        private NWindow w = null;

        public ConceptAction(Term t) {
            super();
            this.term = t;
        }

        @Override
        public void actionPerformed(ActionEvent e) {


            Concept concept = nar.concept(term);
            if (concept != null) {


                //Collection<ConceptPanelBuilder.ConceptPanel> panels = cpBuilder.getPanels(concept);

                if (w == null) {
                    w = new NWindow(concept.term.toString(),
                            cpBuilder.newPanel(concept, true, true, 64));

                    w.pack();
                    w.setVisible(true);
                } else {
                    if (w!=null) {
                        w.setVisible(false);
                        w.removeAll();
                        w = null;
                    }
                }
            }
//            else {
//                if (existing.isVisible())
//                    existing.requestFocusInWindow();
//            }

        }

    }


}
