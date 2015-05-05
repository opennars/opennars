package nars.tuprolog.gui.ide;

import nars.tuprolog.Prolog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class TheoryTabbedPane
    extends JTabbedPane
    implements MouseListener, IDE, ChangeListener, PropertyChangeListener, FontDimensionHandler
{
    private static final long serialVersionUID = 1L;

    private Prolog engine;

    private ToolBar toolBar;
    private TheoryEditor editor;
    private JavaInputField inputField;
    private ConsoleDialog consoleDialog;
    private StatusBar statusBar;

    private ArrayList<FileIDE> theoryFileNames;
    
    public TheoryTabbedPane()
    {
        super();
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addMouseListener(this);
        theoryFileNames = new ArrayList<>();
    }

    public FileIDE getTheoryTitleNamesAt(int index)
    {
        return theoryFileNames.get(index);
    }

    public void addTab(String FileName, Component component)
    {
        this.addTab(FileName, component, null);
    }
    public void addTab(String FileName, Component component, Icon extraIcon)
    {
        super.addTab(FileName, new CloseTabIcon(extraIcon), component);
    }

    public void setEngine(Prolog engine)
    {
        this.engine = engine;
    }
    public void setToolBar(ToolBar toolBar)
    {
        this.toolBar = toolBar;
    }
    public void setTheoryEditor(TheoryEditor editor)
    {
        this.editor = editor;
    }
    public void setInputField(JavaInputField inputField)
    {
        this.inputField = inputField;
    }
    public void setConsoleDialog(ConsoleDialog consoleDialog)
    {
        this.consoleDialog = consoleDialog;
    }
    public void setStatusBar(StatusBar statusBar)
    {
        this.statusBar = statusBar;
    }

    //ProperyChangeListener interface method
    public void propertyChange(PropertyChangeEvent event)
    {
        String propertyName = event.getPropertyName();
        if (propertyName.equals("saved"))
        {
            if (event.getNewValue().equals(true))
            {
                if (getTitleAt(getSelectedIndex()).charAt(0)=='*')
                {
                    setTitleAt(getSelectedIndex(),getTitleAt(getSelectedIndex()).substring(1));
                }
            }
            if (event.getNewValue().equals(false))
            {
                if (getTitleAt(getSelectedIndex()).charAt(0)!='*')
                {
                    String newTitle = "*";
                    newTitle=newTitle.concat(getTitleAt(getSelectedIndex()));
                    setTitleAt(getSelectedIndex(),newTitle);
                }
            }
        }
        if (propertyName.equals("caretLine"))
        {
            editor.setCaretLine(Integer.parseInt(event.getNewValue().toString()));
        }
    }

    public JavaEditArea getJavaEditAreaAt(int index)
    {
        return (JavaEditArea)this.getComponentAt(index);
    }
    public JavaEditArea getSelectedJavaEditArea()
    {
        return (JavaEditArea)this.getSelectedComponent();
    }
    public TheoryEditArea getSelectedTheoryEditArea()
    {
        return (TheoryEditArea)this.getSelectedComponent();
    }
    public FileEditArea    getSelectedFileEditArea()
    {
        return (FileEditArea)this.getSelectedComponent();
    }

    //MouseListener interface methods
    public void mouseClicked(MouseEvent e)
    {

        int tabNumber=getUI().tabForCoordinate(this, e.getX(), e.getY());
        if (tabNumber < 0)
            return;
        Rectangle rect=((CloseTabIcon)getIconAt(tabNumber)).getBounds();
        if (rect.contains(e.getX(), e.getY()))
        {
            //the tab is being closed, but if it is the last one another void tab will opened
            if (isClosable(getSelectedIndex()))
            {
                theoryFileNames.remove(tabNumber);
                this.removeTabAt(tabNumber);
                if (this.getTabCount()==0)
                    newTheory();
            }
        }
        stateChanged();
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    //ChangeListener interface method
    public void stateChanged(ChangeEvent event)
    {
        stateChanged();
    }

    private void stateChanged()
    {
        if (getSelectedIndex()>=0 && theoryFileNames.size()>0)
        {
            toolBar.setFileIDE(theoryFileNames.get(getSelectedIndex()));
            editor.setEditArea(getSelectedTheoryEditArea());
            if (getSelectedTheoryEditArea().getCaretLine()==0)
                editor.setCaretLine(1);
            else
                editor.setCaretLine(getSelectedTheoryEditArea().getCaretLine());
        }
    }

    //IDE intarface methods
    public void enableTheoryCommands(boolean flag) {
        editor.enableTheoryCommands(flag);
        toolBar.enableTheoryCommands(flag);
    }
    public boolean isFeededTheory() {
        return !getSelectedTheoryEditArea().isDirty();
    }
    public void setFeededTheory(boolean flag) {
        getSelectedTheoryEditArea().setDirty(!flag);
    }
    public String getEditorContent() {
        return getSelectedTheoryEditArea().getTheory();
    }
    public void setEditorContent(String text) {
        getSelectedTheoryEditArea().setTheory(text);
    }
    public String getEditorContentTabName()
    {
        return theoryFileNames.get(getSelectedIndex()).getFileName();
    }
    public void newTheory() {
        JavaEditArea editArea = new JavaEditArea();
        addTab("untitled", editArea);
        theoryFileNames.add(new FileIDE("",null));
        setSelectedIndex(getTabCount()-1);
        setEditorContent("");
        toolBar.setFileIDE(theoryFileNames.get(getSelectedIndex()));
        setFontDimension(getFontDimension());
        getSelectedJavaEditArea().setSaved(true);
        editArea.addPropertyChangeListener(this);
        editArea.setCaretLine(1);
    }
    public void loadTheory() {
        FileIDE fileIDE = toolBar.getFileIDE();
        boolean found = false;
        int index = -1;
        for (int i=0 ;i<theoryFileNames.size() && !found;i++)
        {
            if(fileIDE.getFileName().equals(theoryFileNames.get(i).getFileName()) && fileIDE.getFilePath().equals(theoryFileNames.get(i).getFilePath()))
            {
                found = true;
                index = i;
            }
        }
        if (!found)
        {
            theoryFileNames.add(fileIDE);
            JavaEditArea editArea = new JavaEditArea();
            addTab(fileIDE.getFileName(), editArea);
            setSelectedIndex(getTabCount()-1);
            setEditorContent(fileIDE.getContent());
            editArea.addPropertyChangeListener(this);
            getSelectedJavaEditArea().setSaved(true);
        }
        else//if (found)
        {
            setSelectedIndex(index);
        }
    }
    public void saveTheory()
    {
        theoryFileNames.set(getSelectedIndex(), toolBar.getFileIDE());
        setTitleAt(getSelectedIndex(),toolBar.getFileIDE().getFileName());
        getSelectedJavaEditArea().setSaved(true);
    }
    public void getTheory()
    {
        FileIDE fileIDE = new FileIDE(engine.getDynamicTheoryCopy().toString(),null);
        JavaEditArea editArea = new JavaEditArea();
        addTab("Theory loaded", editArea);
        theoryFileNames.add(fileIDE);
        setSelectedIndex(getTabCount()-1);
        toolBar.setFileIDE(fileIDE);
        setFontDimension(getFontDimension());

        editArea.setTheory(fileIDE.getContent());
        editArea.addPropertyChangeListener(this);
        getSelectedJavaEditArea().setSaved(true);
    }

    //FontDimensionHandler interface methods
    public void incFontDimension()
    {
        setFontDimension(getFontDimension()+1);
    }
    public void decFontDimension()
    {
        setFontDimension(getFontDimension()-1);
    }
    public void setFontDimension(int dimension)
    {
        for (int i=0;i<getTabCount();i++)
        {
            getJavaEditAreaAt(i).setFontDimension(dimension);
        }
        inputField.setFontDimension(dimension);
        consoleDialog.setFontDimension(dimension);
        statusBar.setFontDimension(dimension);
    }
    public int getFontDimension()
    {
        if (statusBar!=null)
            return statusBar.getFont().getSize();
        else
            return 12;
    }


    public boolean isClosable(int index)
    {
        boolean isClosable = false;
        if (!getJavaEditAreaAt(index).isSaved())
        {
            FileIDE fileIDE = theoryFileNames.get(index);
            Object[] options = {"Yes", "No", "Cancel"};
            int result = JOptionPane.showOptionDialog(this,
                "The file '"
                + getTitleAt(index).substring(1)
                + "' has been modified.\r\n\r\nDo you want to save the changes?", "Confirm",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

            if (result == 0)
            {
                   // save the changes and close the tab
                FileIDE oldValue = toolBar.getFileIDE();
                toolBar.setFileIDE(fileIDE);
                      toolBar.saveTheory();//update theoryFileNames
                toolBar.setFileIDE(oldValue);
                      if (fileIDE.getFileName() != null)
                      {
                          setTitleAt(index,fileIDE.getFileName());
                      }
                      isClosable = true;
            }
            else
                if (result == 2)
                {
                    // don't save changes and don't close the tab
                    isClosable = false;
                }
                else
                    if (result == 1)
                    {
                        // don't save changes and close the tab
                        isClosable = true;
                    }
        }
        else
            isClosable = true;
        return isClosable;
    }

    /**
     * The class which generates the 'X' icon for the tabs. The constructor
     * accepts an icon which is extra to the 'X' icon, so you can have tabs
     * like in JBuilder. This value is null if no extra icon is required.
     */
    class CloseTabIcon implements Icon
    {
        private int x_pos;
        private int y_pos;
        private int width;
        private int height;
        private Icon fileIcon;
         
        public CloseTabIcon(Icon fileIcon)
        {
            this.fileIcon=fileIcon;
            width=16;
            height=16;
        }
         
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            this.x_pos=x;
            this.y_pos=y;
            Color col=g.getColor();
            g.setColor(Color.black);
            int y_p=y+2;
            g.drawLine(x+1, y_p, x+12, y_p);
            g.drawLine(x+1, y_p+13, x+12, y_p+13);
            g.drawLine(x, y_p+1, x, y_p+12);
            g.drawLine(x+13, y_p+1, x+13, y_p+12);
            g.drawLine(x+3, y_p+3, x+10, y_p+10);
            g.drawLine(x+3, y_p+4, x+9, y_p+10);
            g.drawLine(x+4, y_p+3, x+10, y_p+9);
            g.drawLine(x+10, y_p+3, x+3, y_p+10);
            g.drawLine(x+10, y_p+4, x+4, y_p+10);
            g.drawLine(x+9, y_p+3, x+3, y_p+9);
            g.setColor(col);
            if (fileIcon != null)
            {
                fileIcon.paintIcon(c, g, x+width, y_p);
            }
        }

        public int getIconWidth()
        {
            return width + (fileIcon != null? fileIcon.getIconWidth() : 0);
        }
         
        public int getIconHeight()
        {
            return height;
        }
         
        public Rectangle getBounds()
        {
            return new Rectangle(x_pos, y_pos, width, height);
        }
    }
}
