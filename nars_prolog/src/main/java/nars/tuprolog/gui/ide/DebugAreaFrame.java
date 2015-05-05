package nars.tuprolog.gui.ide;

import nars.tuprolog.event.SpyEvent;
import nars.tuprolog.event.SpyListener;
import nars.tuprolog.event.WarningEvent;
import nars.tuprolog.event.WarningListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.StringTokenizer;

    /**
     * 
     * @author Juri Castellani
     *
     * This is a class what is used to create and desplay a
     * debug area window.
     */
public class DebugAreaFrame extends GenericFrame implements SpyListener, WarningListener, ChangeListener
{
    
    private static final long serialVersionUID = 1L;
    
    private JTabbedPane debug;
    private JTextPane warningPane;
    private JTree spyTree;
    private DefaultMutableTreeNode root;
    private JButton expandAllButton;
    private JButton collapseAllButton;
    private JButton expandSelectedNodesButton;
    private JButton collapseSelectedNodesButton;

    public DebugAreaFrame()
    {
        super("Debug Information", null, 275, 400);
        initComponents();
    }
    
    private void initComponents()
    {
        Container c=this.getContentPane();
        JPanel buttonsPanel = new JPanel();
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new BorderLayout());
        toolBar.add(buttonsPanel,BorderLayout.WEST);
        JPanel otherPanel = new JPanel();
        c.setLayout(new BorderLayout());
        c.add(toolBar,BorderLayout.NORTH);
        c.add(otherPanel,BorderLayout.CENTER);

        debug=new JTabbedPane();
        debug.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); 
        warningPane=new JTextPane();
        warningPane.setEditable(false);
        debug.addTab("Warning",new JScrollPane(warningPane));

        root=new DefaultMutableTreeNode("Spy:");
        spyTree=new JTree(root);
        spyTree.setEditable(false);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)spyTree.getCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);

        JPanel spyPanel = new JPanel();
        spyPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        spyPanel.add(new JScrollPane(spyTree),constraints);
        constraints.gridy = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        debug.addTab("Spy",spyPanel);
        

        otherPanel.setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        otherPanel.add(debug,constraints);

        JButton clear=new JButton();
        URL urlImage = ToolBar.getIcon("img/Clear24.png");
        clear.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        clear.setToolTipText("Clear");
        clear.setPreferredSize(new Dimension(32,32));
        clear.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                clear();
            }
        });
        collapseAllButton = new JButton();
        urlImage = ToolBar.getIcon("img/collapseAll.png");
        collapseAllButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        collapseAllButton.setEnabled(false);
        collapseAllButton.setPreferredSize(new Dimension(32,32));
        collapseAllButton.setToolTipText("Collapse all nodes");
        collapseAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                collapseAll();
            }
        });
        expandAllButton = new JButton();
        urlImage = ToolBar.getIcon("img/expandAll.png");
        expandAllButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        expandAllButton.setEnabled(false);
        expandAllButton.setPreferredSize(new Dimension(32,32));
        expandAllButton.setToolTipText("Expand all nodes");
        expandAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                expandAll();
            }
        });
        expandSelectedNodesButton = new JButton();
        urlImage = ToolBar.getIcon("img/expandSelected.png");
        expandSelectedNodesButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        expandSelectedNodesButton.setEnabled(false);
        expandSelectedNodesButton.setPreferredSize(new Dimension(32,32));
        expandSelectedNodesButton.setToolTipText("Expand selected nodes");
        expandSelectedNodesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                expandSelectedNodes();
            }
        });
        collapseSelectedNodesButton = new JButton();
        urlImage = ToolBar.getIcon("img/collapseSelected.png");
        collapseSelectedNodesButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        collapseSelectedNodesButton.setEnabled(false);
        collapseSelectedNodesButton.setPreferredSize(new Dimension(32,32));
        collapseSelectedNodesButton.setToolTipText("Collapse selected nodes");
        collapseSelectedNodesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                collapseSelectedNodes();
            }
        });
        
        buttonsPanel.add(clear);
        buttonsPanel.add(expandAllButton);
        buttonsPanel.add(collapseAllButton);
        buttonsPanel.add(expandSelectedNodesButton);
        buttonsPanel.add(collapseSelectedNodesButton);
        debug.addChangeListener(this);
    }

    public void onSpy(SpyEvent event)
    {
        DefaultTreeModel model = (DefaultTreeModel)spyTree.getModel();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(event.getMsg());
        model.insertNodeInto(node, root, 0);
        if (event.getSnapshot()!=null)
        {
            StringTokenizer st = new StringTokenizer(event.getSnapshot().toString(),"\n");
            while(st.hasMoreTokens())
            {
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(st.nextToken()); 
                model.insertNodeInto(subNode, node, node.getChildCount());
            }
        }
        spyTree.scrollPathToVisible(new TreePath(root.getFirstLeaf().getPath()));
        /*Castagna 16/09*/
        this.collapseAll();
        /**/
    }

    public void onWarning(WarningEvent event)
    {
        warningPane.setText(warningPane.getText()+event.getMsg()+ '\n');
        warningPane.setCaretPosition(warningPane.getDocument().getLength()-1);
    }

    /**
     * Clear the debug area.
     */
    public void clear()
    {
        if(debug.getSelectedIndex()==0)
            warningPane.setText("");
        if(debug.getSelectedIndex()==1)
        {
            root.removeAllChildren();
            ((DefaultTreeModel)spyTree.getModel()).reload();
        }
            
    }

    public void expandAll()
    {
        for (int i=0;i<spyTree.getRowCount();i++)
            spyTree.expandRow(i);
    }
    public void collapseAll()
    {
        int row = spyTree.getRowCount() - 1;
        while (row >= 0)
        {
            spyTree.collapseRow(row);
            row--;
        }
        //to expand root row
        spyTree.expandRow(0);
    }
    public void expandSelectedNodes()
    {
        TreePath[] paths = spyTree.getSelectionPaths();
        if(paths!=null)
        {
            for(int i=0;i<paths.length;i++)
            {
                spyTree.expandPath(paths[i]);
            }
        }
    }
    public void collapseSelectedNodes()
    {
        TreePath[] paths = spyTree.getSelectionPaths();
        if(paths!=null)
        {
            for(int i=0;i<paths.length;i++)
            {
                spyTree.collapsePath(paths[i]);
            }
        }
    }

    public void stateChanged(ChangeEvent arg0) {
        if (debug.getSelectedIndex()==0)
        {
            collapseAllButton.setEnabled(false);
            expandAllButton.setEnabled(false);
            expandSelectedNodesButton.setEnabled(false);
            collapseSelectedNodesButton.setEnabled(false);
        }
        if (debug.getSelectedIndex()==1)
        {
            collapseAllButton.setEnabled(true);
            expandAllButton.setEnabled(true);
            expandSelectedNodesButton.setEnabled(true);
            collapseSelectedNodesButton.setEnabled(true);
        }
    }
}
