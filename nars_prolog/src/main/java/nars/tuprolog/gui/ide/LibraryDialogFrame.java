package nars.tuprolog.gui.ide;

import nars.tuprolog.InvalidLibraryException;
import nars.tuprolog.Libraries;
import nars.tuprolog.event.LibraryEvent;
import nars.tuprolog.event.LibraryListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LibraryDialogFrame extends GenericFrame implements LibraryListener
{
    
    private static final long serialVersionUID = 1L;
    /**
	 * The library manager associated with the interface.
	 */
    private LibraryManager libraryManager;
    /**
	 * The statusbar associated with the dialog
	 */
    private StatusBar sb;
    /**
	 * The text field where it's possible digit library's classname
	 */
    private JTextField libraryClassnameField;
    /**
	 * The panel that contains components that reguard the displayed libraries
	 */
    private JPanel librariesDisplayPanel;
    
    private JPanel addLibraryPanel;
    
    private JButton browseButton;
    
    private IOFileOperations fileManager;

    public LibraryDialogFrame(LibraryManager libraryManager, JFrame mainWindow)
    {
        super("Library Manager", mainWindow, 395, 390, true);
        initComponents();

        this.libraryManager = libraryManager;
        displayLibraryManagerStatus();
        setStatusMessage("Ready.");
        onClose();
    }

    private void initComponents()
    {
        Container c = this.getContentPane();
        JPanel northPanel = new JPanel();
        JPanel loadSavePanel = new JPanel();
        JPanel libraryLoadedPanel = new JPanel();
        addLibraryPanel = new JPanel();
        librariesDisplayPanel = new JPanel();
        JPanel okCancelStatusBarPanel = new JPanel();
        JPanel okCancelPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(addLibraryPanel,BorderLayout.NORTH);
        northPanel.add(libraryLoadedPanel,BorderLayout.SOUTH);
        c.setLayout(new BorderLayout());
        c.add(northPanel,BorderLayout.NORTH);
        c.add(librariesDisplayPanel,BorderLayout.CENTER);
        c.add(okCancelStatusBarPanel,BorderLayout.SOUTH);

        // Display Library Panel 
        libraryClassnameField = new JTextField();
        Document textFieldDoc = libraryClassnameField.getDocument();
        textFieldDoc.addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updated(e);
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updated(e);
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updated(e);
			}
			private void updated(DocumentEvent e) {
		        boolean enable = e.getDocument().getLength() > 0;
		        browseButton.setEnabled(enable);
		        }
		      });
       
        displayLibraryPanel();
        
        JButton bOpen=new JButton();
        URL urlImage = ToolBar.getIcon("img/Open24.png");
        bOpen.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bOpen.setToolTipText("Load preferences");
        bOpen.setPreferredSize(new Dimension(32,32));
        bOpen.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                loadPreferences();
            }
        });
        JButton bSave=new JButton();
        urlImage = ToolBar.getIcon("img/Save24.png");
        bSave.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        bSave.setToolTipText("Save preferences");
        bSave.setPreferredSize(new Dimension(32,32));
        bSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                savePreferences();
            }
        });
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(bOpen);
        buttonsPanel.add(bSave);
        loadSavePanel.setLayout(new BorderLayout());
        loadSavePanel.add(new JLabel("Load preferences:"), BorderLayout.NORTH);
        loadSavePanel.add(buttonsPanel,BorderLayout.WEST);
      
        libraryLoadedPanel.setLayout(new BorderLayout());
        JLabel loadedLibrariesLabel = new JLabel("Currently loaded libraries:");
        libraryLoadedPanel.add(loadedLibrariesLabel);
        librariesDisplayPanel.setLayout(new GridBagLayout());

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                setLibraryManagerStatus();
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                closeLibraryDialog();
            }
        });
        sb = new StatusBar();
        okCancelStatusBarPanel.setLayout(new BorderLayout());
        okCancelStatusBarPanel.add(okCancelPanel,BorderLayout.NORTH);
        okCancelStatusBarPanel.add(loadSavePanel,BorderLayout.CENTER);
        okCancelStatusBarPanel.add(sb,BorderLayout.SOUTH);
        okCancelPanel.add(ok);
        okCancelPanel.add(cancel);
    }

    /**
     * Build the library panel used to add by full name or by path a library
     */
    
    private void displayLibraryPanel()
    {
    	browseButton = new JButton("Browse");
    	browseButton.setEnabled(false);
        JLabel libraryFileLabel = new JLabel("Library file:");
        JLabel libraryFullNameLabel = new JLabel("Library full name:");
        JButton add = new JButton("add");
        
        add.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                   addLibrary(libraryClassnameField.getText());
            }
        });
        
        browseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
            	JFileChooser fc = new JFileChooser();
            	fc.showOpenDialog(LibraryDialogFrame.this);
            	File file = fc.getSelectedFile();
            	try
            	{
            		addLibrary(libraryClassnameField.getText(), file);
            	}catch(Exception e)
            	{
            		setStatusMessage("Loading library error!");
            	}
            }
        });
        
        javax.swing.GroupLayout addLibraryPanelLayout = new javax.swing.GroupLayout(addLibraryPanel);
        addLibraryPanel.setLayout(addLibraryPanelLayout);
        addLibraryPanelLayout.setHorizontalGroup(
            addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addLibraryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(add)
                        .addComponent(libraryClassnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(libraryFullNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(libraryFileLabel)
                    .addGroup(addLibraryPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(browseButton)))
                .addGap(35, 35, 35))
        );
        addLibraryPanelLayout.setVerticalGroup(
            addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addLibraryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(libraryFullNameLabel)
                    .addComponent(libraryFileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addLibraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(libraryClassnameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(add)
                .addContainerGap(19, Short.MAX_VALUE))
        );
        
    }
    /**
     * Build the dialog displaying the name of the managed libraries and
     * the status for each of the managed libraries.
     */
    private void displayLibraryManagerStatus()
    {
        Object[] libraries = libraryManager.getLibraries();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 2;
        librariesDisplayPanel.add(new JLabel(" "),constraints);
        constraints.gridy++;
        for (int i = 0; i < libraries.length; i++)
        {
            constraints.gridx++;constraints.weightx=2;
            constraints.anchor=GridBagConstraints.WEST;
            librariesDisplayPanel.add(createLabel(libraries[i]),constraints);
            constraints.gridx++;constraints.gridx++;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            librariesDisplayPanel.add(createComboBox(libraries[i]),constraints);
            constraints.gridx++;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            librariesDisplayPanel.add(createButton(libraries[i]),constraints);
            constraints.gridx = 0;
            constraints.gridy++;
        }
        librariesDisplayPanel.add(new JLabel(" "),constraints);
    }
        
    /**
     * Create a label to display the name of a
     * library managed by the Library Manager.
     *
     * @param library A library in the Library Manager.
     */
    private JLabel createLabel(Object library)
    {
        String libraryClassname = library.toString();
        //String libraryName = libraryClassname.substring(libraryClassname.lastIndexOf('.') + 1, libraryClassname.length());
        return new JLabel(libraryClassname);
    }

    /**
     * Create a combobox to display the status of a
     * library managed by the Library Manager.
     *
     * @param library A library in the Library Manager.
     */
    private JComboBox<String> createComboBox(Object library)
    {
        JComboBox<String> cb = new JComboBox<>();
        cb.setEditable(false);
        cb.addItem("Loaded");
        cb.addItem("Unloaded");
        boolean selected = libraryManager.isLibraryLoaded(library.toString());
        if (selected)
            cb.setSelectedIndex(0);
        else
            cb.setSelectedIndex(1);
        return cb;
    }

    /**
     * Create a button to allow to remove the corrispondent
     * library managed by the Library Manager.
     *
     * @param library A library in the Library Manager.
     */
   private JButton createButton(Object library)
    {
       final String libraryClassname = library.toString();
       JButton remove = new JButton("remove");
       remove.addActionListener(new ActionListener()
       {
           public void actionPerformed(ActionEvent event)
           {
               removeLibrary(libraryClassname);
               pack();
               setSize(395,getSize().height);
           }
       });
       return remove;
    }

    /**
     * Set the status message in the status bar of this dialog.
     *
     * @param message The message to set in the status bar.
     */
    protected void setStatusMessage(String message)
    {
        sb.setStatusMessage(message);
    }

    /**
     * Add a library from the dialog to the manager.
     *
     * @param libraryClassname The name of the .class of the library to be added.
     */
    public void addLibrary(String libraryClassname)
    {
        try
        {
            /** check to avod that it's possible add more times the same library */
        	if(libraryManager.contains(libraryClassname))
        			setStatusMessage(libraryClassname + ": Library already loaded");
            else
            {
                libraryManager.addLibrary(libraryClassname);
                libraryManager.loadLibrary(libraryClassname);
                librariesDisplayPanel.removeAll();
                displayLibraryManagerStatus();
                setStatusMessage("Ready.");
            }
            pack();
            setSize(395,getSize().height);
        }
        catch (ClassNotFoundException e)
        {
            setStatusMessage(libraryClassname + ": Class Not Found");
        }
        catch (InvalidLibraryException e)
        {
            setStatusMessage(libraryClassname + ": Not a Library");
        }
    }

    /**
     * Add a library from file chooser path.
     *
     * @param libraryClassname The name of the .class of the library to be added.
     * @param path from file chooser.
     */
    public void addLibrary(String libraryClassname, File file)
    {
        try
        {
            /** check to avod that it's possible add more times the same library */
        	String libName = libraryClassname;
        	if(System.getProperty("java.vm.name").equals("IKVM.NET"))
        	{
        		libName = "cli." + libraryClassname.substring(0, 
        				libraryClassname.indexOf(',')).trim();
        	}
            if (libraryManager.contains(libName))
            {
                setStatusMessage(libraryClassname + ": Library already loaded");
            }
            else
            {
            	libraryManager.addLibrary(libraryClassname, file);
                libraryManager.loadLibrary(libraryClassname, file);
                librariesDisplayPanel.removeAll();
                displayLibraryManagerStatus();
                setStatusMessage("Ready.");
            }
            pack();
            setSize(395,getSize().height);
        }
        catch (ClassNotFoundException e)
        {
            setStatusMessage(libraryClassname + ": Class Not Found");
        }
        catch (InvalidLibraryException e)
        {
            setStatusMessage(libraryClassname + ": Not a Library");
        }
    }
    
    /**
     * Remove a library from the dialog to the manager.
     *
     * @param libraryClassname The name of the .class of the library to be removed.
     */
    public void removeLibrary(String libraryClassname)
    {
        try
        {
            /**
             * without this "if" if a library is unloaded
             * an InvalidLibraryException is always catched 
             */
        	if(libraryManager.isExternalLibrary(libraryClassname))
        		libraryManager.unloadExternalLibrary(libraryClassname);
        	if (libraryManager.isLibraryLoaded(libraryClassname))
                libraryManager.unloadLibrary(libraryClassname);
            libraryManager.removeLibrary(libraryClassname);
            setStatusMessage("Ready.");
        }
        catch (InvalidLibraryException e)
        {
            setStatusMessage(e.getMessage());
        }
        librariesDisplayPanel.removeAll();
        displayLibraryManagerStatus();
    }

    /**
     * Load and unload the managed libraries following the indications given
     * by the user through the dialog interface.
     */
    @SuppressWarnings("unchecked")
	public void setLibraryManagerStatus()
    {
        Object[] libraries = libraryManager.getLibraries();
        /** in the dialog there are 2 void JLabel and 3 Component
         *  (JLabel, JComboBox and JButton) for each library showed
         */
        int rows=(librariesDisplayPanel.getComponentCount()-2)/3;
        for(int i=0;i<rows;i++)
        {
            String libraryClassname = libraries[i].toString();
            try
            {
                /** debug to verify that i-th library into libraries correspond 
                 *  with i-th library displayed in the dialog
                 */
                //JOptionPane.showMessageDialog(this,((JComboBox)librariesDisplayPanel.getComponent(3*i+2)).getSelectedItem());
                //JOptionPane.showMessageDialog(this,((JLabel)librariesDisplayPanel.getComponent(3*(i)+1)).getText());
                //JOptionPane.showMessageDialog(this, libraryClassname);

                //if (((JComboBox)librariesDisplayPanel.getComponent(3*i+2)).getSelectedItem().equals("Loaded")) // ED 2013-05-21
                if (((JComboBox<String>) librariesDisplayPanel.getComponent(3*i+2)).getSelectedItem().equals("Loaded"))
                {
                	if(libraryManager.isExternalLibrary(libraryClassname))
                	{
                		try {
                			URL url = libraryManager.getExternalLibraryURL(libraryClassname);
                    		if(url.getProtocol().equals("jar"))
                    		{
        	            		JarURLConnection connection =
        	            		        (JarURLConnection) url.openConnection();
        	            		    url = connection.getJarFileURL();
                    		}
                    		    
                    		libraryManager.loadLibrary(libraryClassname, new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                		}
                		catch (InvalidLibraryException e){
                        	setStatusMessage(libraryClassname + ": Not a Library");
                        }
                		catch (Exception e) {
                			setStatusMessage(libraryClassname + ": Class Not Found");
						}
                	}
                	else
                		libraryManager.loadLibrary(libraryClassname);
                }
                else
                	libraryManager.unloadLibrary(libraryClassname);
                
            }
            catch (InvalidLibraryException e)
            {
                setStatusMessage(e.getMessage());
            }
        }
        closeLibraryDialog();
        librariesDisplayPanel.removeAll();
        displayLibraryManagerStatus();
    }

    /** @see alice.tuprologx.ide.SwingFrame#onClose()*/
    public void closeLibraryDialog()
    {
        onClose();
    }

    /** @see alice.tuprolog.event.LibraryListener#libraryLoaded(alice.tuprolog.event.LibraryEvent) */
    public void libraryLoaded(LibraryEvent event)
    {
        String libraryName = event.getLibraryName();
        if (!libraryManager.contains(libraryName))
        {
            try
            {
                Libraries mainLibraries = libraryManager.getEngine().getLibraries();
            	if(mainLibraries.isExternalLibrary(libraryName))
            	{
            		URL url = mainLibraries.getExternalLibraryURL(libraryName);
            		if(url.getProtocol().equals("jar"))
            		{
	            		JarURLConnection connection =
	            		        (JarURLConnection) url.openConnection();
	            		    url = connection.getJarFileURL();
            		}
            		    
            		libraryManager.addLibrary(libraryName, new File(URLDecoder.decode(url.getFile(), "UTF-8")));
            		pack();
                    setSize(395,getSize().height);
            	}
            	else
            		libraryManager.addLibrary(libraryName);
            }
            catch (ClassNotFoundException e)
            {
                setStatusMessage(libraryName + ": Class Not Found");
            }
            catch (InvalidLibraryException e)
            {
                setStatusMessage(libraryName + ": Not a Library");
            } catch (IOException e) {
            	e.printStackTrace();
			}
        }
        librariesDisplayPanel.removeAll();
        displayLibraryManagerStatus();
    }

    /** @see alice.tuprolog.event.LibraryListener#libraryUnloaded(alice.tuprolog.event.LibraryEvent) */
    public void libraryUnloaded(LibraryEvent event)
    {
        librariesDisplayPanel.removeAll();
        displayLibraryManagerStatus();
    }

    /**
	 * Set the file manager referenced by the library dialog for use in Perceive/Output tasks.
	 * @param fileManager  The file manager we want the toolbar to use.
	 */
    public void setFileManager(IOFileOperations fileManager) {
        this.fileManager = fileManager;
    }

    public void setFileManagerType(String type)
    {
        this.fileManager.setTypeFileFilter(type);
    }

    public void loadPreferences()
    {
        setStatusMessage("Load preferences...");
        try {
            FileIDE fileIDE = fileManager.loadFile();
            if (fileIDE.getFilePath() != null)
            {
                librariesDisplayPanel.removeAll();
                libraryManager.resetLibraries();
                Object[] libraries = getLibrariesFromFile(fileIDE);
                for (int i=0;i<libraries.length;i++)
                {
                    addLibrary(libraries[i].toString());
                }
                pack();
                setSize(395,getSize().height);
            }
            setStatusMessage("Ready.");
        } catch (Exception e) {
            setStatusMessage("Error loading preferences.");
        }
    }

    public void savePreferences()
    {
        setStatusMessage("Save preferences...");
        try
        {
            FileIDE fileIDE = new FileIDE("",null);
            setLibrariesIntoFile(fileIDE);
            fileIDE=fileManager.saveFile(fileIDE);
            if (fileIDE.getFilePath() != null)
            {
                setStatusMessage("Preferences saved to " + fileIDE.getFileName() + '.');
            }
            else
                setStatusMessage("Ready.");
        }
        catch (Exception e)
        {
            setStatusMessage("Error saving preferences."+e.getMessage());
        }
    }

    private Object[] getLibrariesFromFile(FileIDE file)
    {
        ArrayList<String> resultList = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(file.getContent(),"\n");
        while (st.hasMoreTokens())
        {
            resultList.add(st.nextToken());
        }
        return resultList.toArray();
    }

    private void setLibrariesIntoFile(FileIDE file)
    {
        String fileContent = libraryManager.toString();
        file.setContent(fileContent);
    }
}
