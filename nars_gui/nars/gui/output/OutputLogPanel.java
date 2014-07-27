package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.gui.NARControls;
import nars.gui.NPanel;
import nars.gui.NSlider;
import nars.io.Output;

/**
 *
 * @author me
 */


public class OutputLogPanel extends NPanel implements Output {
    private final DefaultStyledDocument doc;
    private final JTextPane ioText;
    private final Style mainStyle;
    private final NAR nar;
    int maxIOTextSize = (int) 8E6;
    private boolean showErrors = true;
    private boolean showStamp = false;
    private boolean showQuestions = true;
    private boolean showStatements = true;

    private Collection<String> nextOutput = new ConcurrentLinkedQueue();

    public OutputLogPanel(NAR s) {
        super();
        setLayout(new BorderLayout());
        
        this.nar = s;
        
        
        StyleContext sc = new StyleContext();
        doc = new DefaultStyledDocument(sc);
        
        ioText = new JTextPane(doc);
        ioText.setEditable(false);
        

        // Create and add the main document style
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        //StyleConstants.setLeftIndent(mainStyle, 16);
        //StyleConstants.setRightIndent(mainStyle, 16);
        //StyleConstants.setFirstLineIndent(mainStyle, 16);
        //StyleConstants.setFontFamily(mainStyle, "serif");
        //StyleConstants.setFontSize(mainStyle, 12);
        doc.setLogicalStyle(0, mainStyle);

        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());
        ioTextWrap.add(ioText);
        
        add(new JScrollPane(ioTextWrap), BorderLayout.CENTER);
        
        
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        final JCheckBox showErrorBox = new JCheckBox("Errors");
        showErrorBox.setSelected(showErrors);
        showErrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
        });
        menu.add(showErrorBox);
        
        final JCheckBox showStatementsBox = new JCheckBox("Statements");
        showStatementsBox.setSelected(showStatements);
        showStatementsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatements = showStatementsBox.isSelected();
            }
        });
        menu.add(showStatementsBox);
        
        final JCheckBox showQuestionsBox = new JCheckBox("Questions");
        showQuestionsBox.setSelected(showQuestions);
        showQuestionsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQuestions = showQuestionsBox.isSelected();
            }
        });
        menu.add(showQuestionsBox);        
        
        final JCheckBox showStampBox = new JCheckBox("Show Stamp");
        showStampBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStamp = showStampBox.isSelected();
            }
        });
        menu.add(showStampBox);        
        
        final NSlider fontSlider = new NSlider(ioText.getFont().getSize(), 6, 40) {

            @Override
            public void onChange(double v) {
                ioText.setFont(ioText.getFont().deriveFont((float)v));
            } 
         
        };        
        fontSlider.setPrefix("Font size: ");
        menu.add(fontSlider);
        
        
        add(menu, BorderLayout.SOUTH);

        addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
            }
        });

    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing)
            nar.addOutput(this);        
        else
            nar.removeOutput(this);                
    }

    
    @Override
    public void output(final Class c, Object o) {

        if ((!showErrors) && (c == ERR.class)) {
            return;
        }
        if (o instanceof Sentence) {
            Sentence s = (Sentence)o;

            if (s.isQuestion() && !showQuestions)
                return;
            if (s.isJudgment() && !showStatements)
                return;        
        }

        if (o instanceof Exception) {
            o = (o.toString() + " @ " + Arrays.asList(((Exception) o).getStackTrace()));
        }
        
        nextOutput.add(c.getSimpleName() + ": " + objectString(o) + '\n');
        
        SwingUtilities.invokeLater(nextOutputRunnable);

    }

    void limitBuffer(int incomingDataSize) {
        Document doc = ioText.getDocument();
        int overLength = doc.getLength() + incomingDataSize - maxIOTextSize;

        if (overLength > 0) {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException ex) {
            }
        }
    }

    protected void print(Color c, float size, String text, boolean bold) {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        MutableAttributeSet aset = ioText.getInputAttributes();


        Font f = ioText.getFont();
        StyleConstants.setForeground(aset, c);
        //StyleConstants.setFontSize(aset, (int)(f.getSize()*size));
        StyleConstants.setBold(aset, bold);
        
        try {
            doc.insertString(doc.getLength(), text, null);

            ioText.getStyledDocument().setCharacterAttributes(doc.getLength() - text.length(), text.length(), aset, true);
        } catch (BadLocationException ex) {
            Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
    private Runnable nextOutputRunnable = new Runnable() {
        @Override
        public void run() {
            if (nextOutput.size() > 0) {

                //limitBuffer(nextOutput.baseLength());
                limitBuffer(128);

                for (String o : nextOutput) {
                    print(Color.BLACK, 1.0f, o, false);
                }
                
                nextOutput.clear();
            }
        }
    };
    
    public String objectString(Object o) {
        if ((o instanceof String) || (o instanceof Character)) {
            return o.toString();
        }
        else if (o instanceof Sentence) {
            Sentence s = (Sentence)o;

            if (s.isQuestion() && !showQuestions)
                return null;
            if (s.isJudgment() && !showStatements)
                return null;


            String r = s.getContent().toString() + s.punctuation + ' ' + s.stamp.getTense(nar.memory.getTime());

            if (s.truth!=null) {
                r += " " + s.truth.toString();
            }

            if ((showStamp) && (s.stamp!=null)) {
                r += " " + s.stamp.getTense(nar.memory.getTime()) + 
                     " " + s.stamp.toString();
                return r;
            }
        }
        
        return o.toString();
    }
    
}
