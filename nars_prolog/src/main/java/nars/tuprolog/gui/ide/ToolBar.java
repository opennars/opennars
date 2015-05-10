package nars.tuprolog.gui.ide;

import nars.tuprolog.Prolog;
import nars.tuprolog.PTerm;
import nars.tuprolog.Theory;
import nars.tuprolog.gui.spyframe.SpyFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class ToolBar extends JPanel
{
    
    private static final long serialVersionUID = 1L;

    /**
	 * The Prolog engine referenced by the toolbar.  
	 */
    private Prolog engine;

    private LibraryManager libraryManager;
    /**
	 * The Library dialog launched by the toolbar. 
	 */
    private LibraryDialogFrame libraryDialog;
    /**
	 * The debug area launched by the toolbar.
	 */
    private DebugAreaFrame debugArea;
    /**
	 * The tuProlog config dialog launched by the toolbar.
	 */
    private PrologConfigFrame configFrame;
    /**
	 * The About window launched by the toolbar.
	 */
    private AboutFrame about;

    /**
	 * The file manager launched by the toolbar.
	 */
    private IOFileOperations fileManager;
    /**
	 * A message describing the status of the console.
	 */
    private String statusMessage;
    /**
	 * Used for components interested in changes of console's properties.
	 */
    private PropertyChangeSupport propertyChangeSupport;
    /**
	 * The IDE the toolbar belongs to, necessary to manage editor-related commands such as saving its content to the filesystem.
	 */
    static private String path;
    
    private IDE ide;
    
    private JFrame parent;
    
    private JButton bNew;
    private JButton bOpen;
    private JButton bSave;
    private JButton bSaveAs;
    private JButton bLibMan;
    private JButton bDebug;
    private JButton bConfigure;
    private JButton bAbout;
    private JButton bSpy;

    protected FileIDE fileIDE;

    
    public ToolBar(IDE ide, JFrame parent)
    {
        this.parent = parent;
        this.ide = ide;
        propertyChangeSupport = new PropertyChangeSupport(this);
        initComponents();
        fileIDE=new FileIDE("",null);
    }

    public static URL getIcon(String p) {
        //getIcon(p);
        try {
            return new File("nars_prolog/src/main/java/nars/prolog/gui/ide/" + p).toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
    private void initComponents()
    {
        bNew=new JButton();
        URL urlImage = getIcon("img/New24.png");
        bNew.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bNew.setToolTipText("New Theory");
        bNew.setPreferredSize(new Dimension(32,32));
        bNew.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                newTheory();
            }
        });
        bOpen=new JButton();
        urlImage = getIcon("img/Open24.png");
        bOpen.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bOpen.setToolTipText("Load Theory");
        bOpen.setPreferredSize(new Dimension(32,32));
        bOpen.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                loadTheory();
            }
        });
        bSave=new JButton();
        urlImage = getIcon("img/Save24.png");
        bSave.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bSave.setToolTipText("Save Theory");
        bSave.setPreferredSize(new Dimension(32,32));
        bSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                saveTheory();
            }
        });
        bSaveAs=new JButton();
        urlImage = getIcon("img/SaveAs24.png");
        bSaveAs.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bSaveAs.setToolTipText("Save Theory As...");
        bSaveAs.setPreferredSize(new Dimension(32,32));
        bSaveAs.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                saveTheoryAs();
            }
        });
        bLibMan=new JButton();
        urlImage = getIcon("img/Library24.png");
        bLibMan.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bLibMan.setToolTipText("Open Library Manager");
        bLibMan.setPreferredSize(new Dimension(32,32));
        bLibMan.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                openLibraryManager();
            }
        });
        bDebug=new JButton();
        urlImage = getIcon("img/Debugger24.png");
        bDebug.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bDebug.setToolTipText("View Debug Information");
        bDebug.setPreferredSize(new Dimension(32,32));
        bDebug.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                viewDebugInformation();
            }
        });
        bConfigure=new JButton();
        urlImage = getIcon("img/Configure24.png");
        bConfigure.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bConfigure.setToolTipText("Configure tuProlog");
        bConfigure.setPreferredSize(new Dimension(32,32));
        bConfigure.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                configure();
            }
        });
        bAbout=new JButton();
        urlImage = getIcon("img/About24.png");
        bAbout.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bAbout.setToolTipText("About...");
        bAbout.setPreferredSize(new Dimension(32,32));
        bAbout.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                viewAboutInformation();
            }
        });

        bSpy = new JButton();
        urlImage = getIcon("img/AlberoBinario.png");
        bSpy.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bSpy.setToolTipText("Spy Frame");
        bSpy.setPreferredSize(new Dimension(32,32));
        bSpy.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent event)
            {
        		ConsoleManager consoleManager = JavaIDE.getConsoleManager();
                Theory theory = engine.getDynamicTheoryCopy();
                PTerm rich = engine.termSolve(consoleManager.getGoal());
                try {
					new SpyFrame(theory, rich);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });


        JPanel bottonsPanel = new JPanel();
        bottonsPanel.add(bNew);
        bottonsPanel.add(bOpen);
        bottonsPanel.add(bSave);
        bottonsPanel.add(bSaveAs);
        bottonsPanel.add(bLibMan);
        bottonsPanel.add(bDebug);
        bottonsPanel.add(bConfigure);
        bottonsPanel.add(bAbout);
        bottonsPanel.add(bSpy);
        
        
        setLayout(new BorderLayout());
        add(bottonsPanel,BorderLayout.WEST);
        add(new JSeparator(),BorderLayout.SOUTH);
    }

    /**
	 * Get the Prolog engine referenced by the toolbar.
	 * @return  The Prolog engine referenced by the toolbar.
	 */
    public Prolog getEngine() {
        return engine;
    }

    /**
	 * Set the Prolog engine referenced by the toolbar.
	 * @param engine  an <code>alice.tuprolog.Prolog</code> engine.
	 */
    public void setEngine(Prolog engine) {
        this.engine = engine;
        debugArea = new DebugAreaFrame();
        libraryDialog = new LibraryDialogFrame(libraryManager,parent);
        libraryDialog.setFileManager(new JavaIOManager(libraryDialog));
        libraryDialog.setFileManagerType("preferences");
        this.engine.addSpyListener(debugArea);
        this.engine.addWarningListener(debugArea);
        this.engine.addLibraryListener(libraryDialog);
        //this.engine.addPropertyChangeListener(debugArea);
    }

    /**
	 * Get the library manager 
	 * @return  the current library manager of the toolbar
	 */
    public LibraryManager getLibraryManager() {
        return libraryManager;
    }

    /**
	 * Set the library manager 
	 * @param libraryManager  a libraryManager to associate to the libraryDialog
	 */
    public void setLibraryManager(LibraryManager libraryManager)
    {
        this.libraryManager = libraryManager;
    }

    /**
     * Set the tuProlog config frame
     *
     * @param configFrame a libraryManager to associate to the libraryDialog
     */
    public void setPrologConfig(PrologConfigFrame configFrame)
    {
        this.configFrame = configFrame;
    }

    /**
	 * Get the toolbar status as a <code>java.lang.String</code> message.
	 * @return  the current status of the toolbar as a <code>java.lang.String</code>  message.
	 */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
	 * Set the toolbar status.
	 * @param message  The message describing the new status of the toolbar.
	 */
    public void setStatusMessage(String message) {
        String oldStatusMessage = getStatusMessage();
        statusMessage = message;
        propertyChangeSupport.firePropertyChange("StatusMessage", oldStatusMessage, statusMessage);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
	 * Set the file manager referenced by the toolbar for use in Perceive/Output tasks.
	 * @param fileManager  The file manager we want the toolbar to use.
	 */
    public void setFileManager(IOFileOperations fileManager) {
        this.fileManager = fileManager;
    }

    public void setFileManagerType(String type)
    {
        this.fileManager.setTypeFileFilter(type);
    }

    /**
	 * Get the file manager used by the toolbar.
	 * @return  The file manager used by the toolbar.
	 */
    public IOFileOperations getFileManager() {
        return fileManager;
    }

    /**
     * Enable or disable theory-related buttons.
     *
     * @param flag true if the buttons have to be enabled, false otherwise.
     */
    protected void enableTheoryCommands(boolean flag)
    {
        bNew.setEnabled(flag);
        bOpen.setEnabled(flag);
        bSave.setEnabled(flag);
        bSaveAs.setEnabled(flag);
        bLibMan.setEnabled(flag);
    }
  
    /**
     * Reset the engine's theory to a new blank theory.
     */
    public void newTheory() {
        engine.clearTheory();
        ide.newTheory();
        setStatusMessage("Ready (new theory accepted).");
//        theoryIDE=new TheoryIDE(null,null);
    }

    /**
     * Load the engine with a theory previously stored in a file.
     */
    public void loadTheory() {
        setStatusMessage("Load Theory...");
        try {
            fileIDE = fileManager.loadFile();
            if (fileIDE.getFilePath() != null) {
            	path = fileIDE.getFilePath();
                engine.resetDirectoryList(fileIDE.getFilePath());
                engine.setCurrentDirectory(path);

                /** this isn't correct with the tabbed theory visualization*/
                //engine.setTheory(theory);

                //ide.setEditorContent(theoryIDE.getTheory().toString());
                ide.loadTheory();
            }
            setStatusMessage("Ready.");
        } catch (Exception e) {
            setStatusMessage("Error consulting theory.");
        }
    }

    /**
     * Save the engine's current theory to a default file.
     */
    public void saveTheory() {
        FileIDE oldValue = fileIDE;
        try {
            fileIDE.setContent(ide.getEditorContent());
            fileIDE=fileManager.saveFile(fileIDE);
            if (fileIDE.getFilePath() != null)
            {
                setStatusMessage("Theory saved to " + fileIDE.getFileName() + '.');
                ide.saveTheory();
            }
            else
                setStatusMessage("Ready.");
        } catch (Exception e){
            setStatusMessage("Error saving theory.");
            fileIDE = oldValue;
        }
    }

    /**
     * Save the engine's current theory to a file chosen by the user.
     */
    public void saveTheoryAs() {
        setStatusMessage("Save Theory As...");
        FileIDE oldValue = fileIDE;
        try {
            fileIDE.setContent(ide.getEditorContent());
            fileIDE = fileManager.saveFileAs(fileIDE);
            if (fileIDE.getFilePath() != null)
            {
                setStatusMessage("Theory saved to " + fileIDE.getFileName() + '.');
                ide.saveTheory();
            }
            else
                setStatusMessage("Ready.");
        } catch (Exception e) {
            setStatusMessage("Error saving theory.");
            fileIDE = oldValue;
        }
    }

    /**
     * Opens the Library Manager dialog.
     */
    public void openLibraryManager() {
        parent.setEnabled(false);
        libraryDialog.setVisible(true);
        libraryDialog.setStatusMessage("Ready.");
    }

    /**
     * Show an instance of the debug area referenced by the toolbar in order to
     * display debug informations.
     */
    public void viewDebugInformation() {
        debugArea.setVisible(true);
    }

    public void configure()
    {
        parent.setEnabled(false);
        configFrame.setVisible(true);
        configFrame.reload();
    }

    /**
     * Display an About dialog with information on the system and its crafters.
     */
    public void viewAboutInformation() {
        about = new AboutFrame(parent);
        parent.setEnabled(false);
        about.pack();
        about.setVisible(true);
    }


    public void setFileIDE(FileIDE fileIDE)
    {
        this.fileIDE = fileIDE;
    }
    public FileIDE getFileIDE()
    {
        return fileIDE;
    }
    
    public static String getPath()
    {
    	return path;
    }

}
