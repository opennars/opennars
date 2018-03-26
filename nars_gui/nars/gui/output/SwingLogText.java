package nars.gui.output;

import automenta.vivisect.Video;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import nars.main.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.TruthFunctions;
import nars.inference.UtilityFunctions;
import nars.io.handlers.OutputHandler.OUT;


public class SwingLogText extends SwingText  {
    private final NAR nar;
    public boolean showStamp = false;
    final Deque<LogLine> pendingDisplay = new ConcurrentLinkedDeque<>();
    private JScrollPane scroller;


    
    public static class LogLine {
        public final Class c;
        public final Object o;

        public LogLine(Class c, Object o) {
            this.c = c;
            this.o = o;
        }
        
    }
    
    

    public SwingLogText(NAR n) {        
        super();
        
        this.nar = n;
        
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
    
    protected void onLineVisible(int offset) { }
    
    
    public void output(final Class c, final Object o) {                
        pendingDisplay.addLast(new LogLine(c, o));
                
        if (pendingDisplay.size() == 1) {
            //only invoke update after the first has been added
            SwingUtilities.invokeLater(update);
        }
    }
    
    public final Runnable update = new Runnable() {
        
        //final Rectangle bottom = new Rectangle(0,Integer.MAX_VALUE-1,1,1);        
        
        @Override public void run() {
            
            while (pendingDisplay.size() > 0) {
                LogLine l = pendingDisplay.removeFirst();
                print(l.c, l.o);
            
            }
                        
            limitBuffer();                        

            /*try {
                //scrollRectToVisible(bottom);
            }
            catch (Exception e) { } */
        }
    };
    
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
    
    
    protected int print(Class c, Object o)  {        

        float priority = 1f;

        
        
        if (c!=OUT.class) {
            //pad the channel name to max 6 characters, right aligned
            
            String n = c.getSimpleName();
            //n = n.substring(0,Math.min(4, n.length()));
            switch (n.length()) {
                case 0: break;
                case 1: n = "   " + n; break;
                case 2: n = "  " + n; break;
                case 3: n = " " + n; break;           
            }
            Color chanColor = Video.getColor(c.getClass().hashCode(), 0.8f, 0.8f);
            print(chanColor, n);
        }
        
        else {
            if (o instanceof Task) {
                Task t = (Task)o;
                Sentence s = t.sentence;
                if (s!=null) {
                    priority = t.budget.getPriority();
                    //printColorBlock(LogPanel.getPriorityColor(priority), "  ");
                
                    TruthValue tv = s.truth;
                    if (tv!=null) {          
                        float evidence = TruthFunctions.c2w(tv.getConfidence());
                        float pos_2 = tv.getConfidence()*tv.getFrequency();
                        float positive_evidence_in_0_1 = TruthFunctions.w2c(evidence*tv.getFrequency());
                        float negative_evidence_in_0_1 = TruthFunctions.w2c(evidence*(1.0f-tv.getFrequency()));
                        printColorBlock(LogPanel.getPositiveEvidenceColor(positive_evidence_in_0_1), "  ");
                        printColorBlock(LogPanel.getNegativeEvidenceColor(negative_evidence_in_0_1), "  ");                        
                    }
                    else if ( t.getBestSolution()!=null) {
                        float evidence = TruthFunctions.c2w(t.getBestSolution().truth.getConfidence());
                        float pos_2 = t.getBestSolution().truth.getConfidence()*t.getBestSolution().truth.getFrequency();
                        float positive_evidence_in_0_1 = TruthFunctions.w2c(evidence*t.getBestSolution().truth.getFrequency());
                        float negative_evidence_in_0_1 = TruthFunctions.w2c(evidence*(1.0f-t.getBestSolution().truth.getFrequency()));
                        //printColorBlock(LogPanel.getStatementColor('=', priority, t.getBestSolution().truth.get), "    ");
                        printColorBlock(LogPanel.getPositiveEvidenceColor(positive_evidence_in_0_1), "  ");
                        printColorBlock(LogPanel.getNegativeEvidenceColor(negative_evidence_in_0_1), "  ");  
                    }
                    else {                        
                        printColorBlock(LogPanel.getStatementColor(s.punctuation, priority), "    ");                   
                    }
                }
            }
        }        
        
        
        CharSequence text = LogPanel.getText(c, o, showStamp, nar);
        StringBuilder sb = new StringBuilder(text.length()+2);
        sb.append(' ');
        if (text.length() > maxLineWidth)
            sb.append(text.subSequence(0,maxLineWidth));
        else
            sb.append(text);

        if (sb.charAt(sb.length()-1)!='\n')
            sb.append('\n');
                
        if (o instanceof Task) {
            Task t = (Task)o;
            int color = 128;
            Concept cc = nar.memory.concept(t.getTerm()); 
            color = (int) Math.min(255.0f,80.0f+t.getPriority()*255.0f);
            Color col = new Color(color,color,color);
            if (cc!=null) {
                color = (int)(128.0f+cc.getPriority()*128.0f);
                print(col, sb.toString(), new ConceptAction(cc));
                return doc.getLength();
            } 
            
            print(col, sb.toString());
            return doc.getLength();
        }
        
//        float tc = 0.75f + 0.25f * priority;
//        Color textColor = new Color(tc,tc,tc);   
        String s = sb.toString();
        print(Color.GRAY, sb.toString());        
        return doc.getLength();
        
    }
    
    public class ConceptAction extends AbstractAction {
        private final Concept concept;

        public ConceptAction(Concept c) {
            super();
            this.concept = c;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {            
            ConceptButton.popup(nar, concept);
        }
        
    }
    

}
