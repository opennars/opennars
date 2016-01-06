//package nars.gui.output;
//
//public class SwingLogText extends SwingText {
////    final Deque<LogLine> pendingDisplay = new ArrayDeque(); //new ConcurrentLinkedDeque<>();
////    public final Runnable update = new Runnable() {
////
////        //final Rectangle bottom = new Rectangle(0,Integer.MAX_VALUE-1,1,1);
////
////        @Override
////        public void run() {
////
////            while (pendingDisplay.size() > 0) {
////                LogLine l = pendingDisplay.removeFirst();
////                print(l);
////            }
////
////            limitBuffer();
////
////            /*try {
////                //scrollRectToVisible(bottom);
////            }
////            catch (Exception e) { } */
////        }
////    };
////    private final NAR nar;
////    private final ConceptPanelBuilder cpBuilder;
////    public boolean showStamp = false;
////    private JScrollPane scroller;
////
////
////    public SwingLogText(NAR n) {
////        super();
////
////        this.nar = n;
////
////        this.cpBuilder = new ConceptPanelBuilder(n);
////    }
////
//////    @Override
//////    public void paint(Graphics g) {
//////        super.paint(g); //To change body of generated methods, choose Tools | Templates.
//////        if (isVisible()) {
//////
////////            try {
////////
////////                TextUI mapper = getUI();
////////
////////
////////                Rectangle r = mapper.modelToView(this, getCaretPosition());
////////                System.out.println("caret: " + r);
////////
////////            } catch (Exception e) {
////////
////////                System.err.println("Problem painting cursor");
////////
////////            }
//////
//////            //scrollUpdate();
//////        }
//////
//////    }
////
////    void setScroller(JScrollPane scroller) {
////        this.scroller = scroller;
////        /*scroller.getViewport().addChangeListener(new ChangeListener() {
////                @Override
////                public void stateChanged(ChangeEvent e) {
////
////                    //scrollUpdate();
////
////                }
////            });*/
////    }
////
//////    protected void scrollUpdate() {
//////        int docLen = doc.getLength();
//////        if (docLen > 0) {
//////            //JViewport viewport = (JViewport) e.getSource();
//////            Rectangle viewRect = scroller.getViewport().getViewRect();
//////
//////            Point p = viewRect.getLocation();
//////            int startIndex = viewToModel(p);
//////
//////            p.x += viewRect.width;
//////            p.y += viewRect.height;
//////            int endIndex = viewToModel(p);
//////
//////            for (int offset = endIndex; offset < startIndex;) {
//////                try {
//////                    //System.out.println(" " + offset);
//////
//////                    onLineVisible(offset);
//////
//////                    offset = Utilities.getRowStart(SwingLogText.this, offset) - 1;
//////
//////                } catch (BadLocationException ex) {
//////                    Logger.getLogger(SwingLogText.class.getName()).log(Level.SEVERE, null, ex);
//////                }
//////            }
//////            //System.out.println("< -- (" + endIndex + ", " + startIndex);
//////        }
//////    }
////
////    protected void onLineVisible(int offset) {
////    }
////
////    final StringBuilder buffer = new StringBuilder();
////
////    public void output(final Class c, final Object o) {
////        pendingDisplay.addLast(new LogLine(c, o));
////
////        if (pendingDisplay.size() == 1) {
////            //only invoke update after the first has been added
////            SwingUtilities.invokeLater(update);
////        }
////    }
////
////    protected int print(final LogLine l) {
////
////        Class c = l.c;
////        Object o = l.o;
////        float priority = 1f;
////
////
////
////        if (c != OUT.class) {
////            //pad the channel name to max 6 characters, right aligned
////
////            String n = c.getSimpleName();
////            final int nl = Math.min(n.length(), 6);
////
////            buffer.setLength(0);
////
////            switch (nl) {
////                case 0:
////                    break;
////                case 1:
////                    buffer.append("     ");
////                    break;
////                case 2:
////                    buffer.append("    ");
////                    break;
////                case 3:
////                    buffer.append("   ");
////                    break;
////                case 4:
////                    buffer.append("  ");
////                    break;
////                case 5:
////                    buffer.append(" ");
////                    break;
////            }
////
////            if (nl > 6)
////                buffer.append(n.substring(0, 6));
////            else
////                buffer.append(n);
////
////            Color chanColor = Video.getColor(c.hashCode(), 0.8f, 0.8f);
////            print(chanColor, buffer.toString());
////        } else {
////            if (o instanceof Task) {
////                Task t = (Task) o;
////                Sentence s = t;
////                if (s != null) {
////                    priority = t.getPriority();
////                    printBlock(LogPanel.getPriorityColor(priority), "  ");
////
////                    Truth tv = s.getTruth();
////                    if (tv != null) {
////                        printBlock(LogPanel.getFrequencyColor(tv.getFrequency()), "  ");
////                        printBlock(LogPanel.getConfidenceColor(tv.getConfidence()), "  ");
////                    } else if (t.getBestSolution() != null) {
////                        printBlock(LogPanel.getStatementColor('=', priority), "    ");
////                    } else {
////                        printBlock(LogPanel.getStatementColor(s.getPunctuation(), priority), "    ");
////                    }
////                }
////            }
////        }
////
////
////
////
////        buffer.setLength(0);
////
////        buffer.append(' ');
////        TextOutput.append(c, o, showStamp, nar, buffer);
////
////        if (buffer.length() == 0)
////            throw new RuntimeException("no text generated for: " + o);
////
////        if (buffer.charAt(buffer.length() - 1) == '\n') {
////            buffer.delete(buffer.length()-1, buffer.length());
////            //throw new RuntimeException(sb + " should not end in newline char");
////        }
////
////        if ((buffer.length() > maxLineWidth) && (c != Events.ERR.class))
////            buffer.setLength(maxLineWidth);
////
////
////        if (o instanceof Task) {
////            Task t = (Task) o;
////            float tc = 0.5f + 0.5f * priority;
////            Color textColor = new Color(tc, tc, tc);
////            print(textColor, buffer.toString(), new ConceptAction(t.getTerm()));
////
////            print(Color.GRAY, "\n"); //print the newline separately without the action handler
////        }
////        else {
//////        float tc = 0.75f + 0.25f * priority;
//////        Color textColor = new Color(tc,tc,tc);
////            print(Color.GRAY, buffer.append('\n').toString());
////        }
////
////
////
////        return doc.getLength();
////
////    }
////
//////    public class TaskIcon extends NCanvas {
//////
//////        public TaskIcon() {
//////            super();
//////            setMaximumSize(new Dimension(50,10));
//////            setPreferredSize(new Dimension(50,10));
//////            setSize(50,10);
//////
//////            Graphics2D g = getBufferGraphics();
//////
//////            showBuffer(g);
//////        }
//////
//////
//////
//////    }
////
////    public static class LogLine {
////        public final Class c;
////        public final Object o;
////
////        public LogLine(Class c, Object o) {
////            this.c = c;
////            this.o = o;
////        }
////
////    }
////
////    public class ConceptAction extends AbstractAction {
////        private final Term term;
////        private NWindow w = null;
////
////        public ConceptAction(Term t) {
////            super();
////            this.term = t;
////        }
////
////        @Override
////        public void actionPerformed(ActionEvent e) {
////
////
////            Concept concept = nar.concept(term);
////            if (concept != null) {
////
////
////                //Collection<ConceptPanelBuilder.ConceptPanel> panels = cpBuilder.getPanels(concept);
////
////                if (w == null) {
////                    w = new NWindow(concept.getTerm().toString(),
////                            cpBuilder.newPanel(concept, true, true, 64));
////
////                    w.pack();
////                    w.setVisible(true);
////                } else {
////                    if (w!=null) {
////                        w.setVisible(false);
////                        w.removeAll();
////                        w = null;
////                    }
////                }
////            }
//////            else {
//////                if (existing.isVisible())
//////                    existing.requestFocusInWindow();
//////            }
////
////        }
////
////    }
////
//
// }
