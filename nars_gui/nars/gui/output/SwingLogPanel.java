package nars.gui.output;

import automenta.vivisect.Video;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import nars.gui.NARControls;
import nars.gui.output.SwingLogText.LogLine;
import nars.io.handlers.TextOutputHandler;

public class SwingLogPanel extends LogPanel {

    public final SwingLogText ioText;

    static int defaultFontSize = 19;
    public HashMap<Integer, LogLine> lines = new HashMap();
    
    public SwingLogPanel(NARControls narControls) {
        this(narControls, LogPanel.outputEvents);
    }
    
    public SwingLogPanel(NARControls narControls, Class... events) {
        super(narControls, events);

        ioText = new SwingLogText(narControls.nar) {

            @Override
            protected void onLineVisible(final int offset) {                
                //System.out.println(offset + " " + lines.get(offset));
            }
            
        };
        
        

        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(ioText, BorderLayout.CENTER);
        JScrollPane ioTextScroll = new JScrollPane(ioTextWrap);
        ioText.setScroller(ioTextScroll);
        add(ioTextScroll, BorderLayout.CENTER);
	
                    
        addPopupMenu();

        setConsoleFont(ioText);

        setBackground(Color.BLACK);
    }

    @Override
    public void setShowStamp(boolean showStamp) {
        ioText.showStamp = showStamp;
    }
    

    @Override
    void print(Class c, Object o) {
        
        if(!filter.equals("")) { //ok its filtered we can afford the ToStrings for this case as we also want the logfile to be affected
            if(!o.toString().contains(filter)) {
                return;
            }
        }
        ioText.output(c, o);//, showStamp, nar);

        CharSequence s = TextOutputHandler.getOutputString(c, o, true, showStamp, nar);
        if (logFile != null) {
            logFile.append(s).append('\n');
        }
    }
    
    @Override
    public void setFontSize(float v) {
        ioText.setFontSize(v);
    }
    
    @Override
    protected void clearLog() {
        ioText.setText("");
    }

    public static void setConsoleFont(JTextComponent c) {
        setConsoleFont(c, defaultFontSize);
    }

    public static void setConsoleFont(JTextComponent c, int fontSize) {
        /*if (invert) {
            c.setForeground(Color.WHITE);
            c.setCaretColor(Color.WHITE);
            c.setBackground(Color.BLACK);
        } else {
            c.setForeground(Color.BLACK);
            c.setCaretColor(Color.BLACK);
            c.setBackground(Color.WHITE);
        }*/
        c.setBorder(new EmptyBorder(0, 0, 0, 0));
        //c.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        c.setFont(Video.monofont.deriveFont(1f*fontSize));
    }

    final static String COPY = "Copy";
    final static String CUT = "Cut";
    final static String PASTE = "Paste";
    final static String SELECTALL = "Select All";

    /**
     * @see:
     * http://www.objectdefinitions.com/odblog/2007/jtextarea-with-popup-menu/
     */
    private void addPopupMenu() {

        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem copyItem = new JMenuItem();
        copyItem.setAction(ioText.getActionMap().get(DefaultEditorKit.copyAction));
        copyItem.setText(COPY);

        final JMenuItem cutItem = new JMenuItem();
        cutItem.setAction(ioText.getActionMap().get(DefaultEditorKit.cutAction));
        cutItem.setText(CUT);

        final JMenuItem pasteItem = new JMenuItem(PASTE);
        pasteItem.setAction(ioText.getActionMap().get(DefaultEditorKit.pasteAction));
        pasteItem.setText(PASTE);

        final JMenuItem selectAllItem = new JMenuItem(SELECTALL);
        selectAllItem.setAction(ioText.getActionMap().get(DefaultEditorKit.selectAllAction));
        selectAllItem.setText(SELECTALL);

        menu.add(copyItem);
        menu.add(cutItem);
        menu.add(pasteItem);
        menu.add(new JSeparator());
        menu.add(selectAllItem);

        ioText.add(menu);
        ioText.addMouseListener(new PopupTriggerMouseListener(menu, ioText));

    }

    public static class PopupTriggerMouseListener extends MouseAdapter {

        private JPopupMenu popup;
        private JComponent component;

        public PopupTriggerMouseListener(JPopupMenu popup, JComponent component) {
            this.popup = popup;
            this.component = component;
        }

        //some systems trigger popup on mouse press, others on mouse release, we want to cater for both
        private void showMenuIfPopupTrigger(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(component, e.getX() + 3, e.getY() + 3);
            }
        }

        //according to the javadocs on isPopupTrigger, checking for popup trigger on mousePressed and mouseReleased 
        //should be all  that is required
        //public void mouseClicked(MouseEvent e)  
        //{
        //    showMenuIfPopupTrigger(e);
        //}
        public void mousePressed(MouseEvent e) {
            showMenuIfPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e) {
            showMenuIfPopupTrigger(e);
        }

    }

    @Override
    void limitBuffer(int incomingDataSize) {
        ioText.limitBuffer();
    }

}
