/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DataListView.java". Description:
"Do nothing"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.ui.data;

import ca.nengo.io.DelimitedFileExporter;
import ca.nengo.model.Network;
import ca.nengo.ui.action.PropertiesAction;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.ReversableAction;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.util.FileExtensionFilter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * TODO
 * 
 * @author TODO
 */
public class DataListView extends JPanel implements TreeSelectionListener {

    private static final long serialVersionUID = 1L;

    /**
     * TODO
     */
    public static final String DATA_FILE_EXTENSION = "txt";

    /**
     * TODO
     */
    public static final String MATLAB_FILE_EXTENSION = "mat";

    private final SimulatorDataModel dataModel;
    private final JTree tree;


    /**
     * @param data TODO
     */
    public DataListView(SimulatorDataModel data) {
        super(new GridLayout(1, 0));

        this.dataModel = data;

        // Create a tree that allows one selection at a time.
        tree = new JTree(dataModel);
        tree.setRootVisible(false);
        NengoStyle.applyStyle(tree);
        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        tree.setCellRenderer(treeRenderer);
        // treeRenderer.setBackground(style.COLOR_BACKGROUND);
        NengoStyle.applyStyle(treeRenderer);

        // tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        tree.addMouseListener(new MyTreeMouseListener());

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);
        add(scrollPane);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        /*
         * Do nothing
         */
    }

    /**
     * @param network TODO
     */
    public void captureSimulationData(Network network) {
        final SortableMutableTreeNode newNode = dataModel.captureData(network);

        if (newNode != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TreePath path = new TreePath(newNode.getPath());
                    tree.scrollPathToVisible(path);
                    tree.setSelectionPath(path);
                    tree.expandPath(path);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private class MyTreeMouseListener implements MouseListener {

        private void ContextMenuEvent(MouseEvent e) {

            JPopupMenu menu = null;

            TreePath[] paths = getTreePaths(e);
            List<MutableTreeNode> leafNodes = getLeafNodes(paths);

            // Have no idea how many data nodes there are going to be. Might as
            // well make it proportional to the number of nodes.
            HashSet<DataTreeNode> dataNodes = new HashSet<DataTreeNode>(leafNodes.size());

            // Find data nodes in tree nodes
            for (MutableTreeNode treeNode : leafNodes) {
                RecursiveFindDataNodes(treeNode, dataNodes);
            }

            PopupMenuBuilder menuBuilder;

            if (dataNodes.size() > 0) {
                if (leafNodes.size() == 1) {
                    MutableTreeNode leafNode = leafNodes.get(0);

                    menuBuilder = new PopupMenuBuilder(leafNode.toString());

                    if(leafNode instanceof DefaultMutableTreeNode &&
                            ((DefaultMutableTreeNode)leafNode).getUserObject() instanceof String) {
                        menuBuilder.addAction(new RenameAction((DefaultMutableTreeNode)leafNode));
                    }

                    menuBuilder.addAction(new ExportDelimitedFileAction(DataListView.this, leafNode));
                    //menuBuilder.addAction(new ExportMatlabAction(DataListView.this, leafNode));


                    if (leafNode instanceof NengoTreeNode) {
                        NengoTreeNode neoTreeNode = (NengoTreeNode) leafNode;

                        if (neoTreeNode.getNeoNode() != null) {
                            menuBuilder.addAction(new PropertiesAction("Inspector",
                                    neoTreeNode.getNeoNode()));
                        }
                    }

                    if (leafNode instanceof DataTreeNode) {
                        ((DataTreeNode) leafNode).constructPopupMenu(menuBuilder, dataModel);
                    }

                } else {
                    menuBuilder = new PopupMenuBuilder(dataNodes.size() + " data nodes selected");
                    menuBuilder.addAction(new PlotNodesAction(dataNodes));
                }
            } else if (leafNodes.size() == 1) {
                menuBuilder = new PopupMenuBuilder(leafNodes.get(0).toString());
            } else {
                menuBuilder = new PopupMenuBuilder(leafNodes.size() + " nodes selected");
            }

            // make sure we don't remove the node representing the network
            List<MutableTreeNode> removeNodes = new ArrayList<MutableTreeNode>();
            TreeNode root = (TreeNode) dataModel.getRoot();
            for (MutableTreeNode node : leafNodes) {
                if (node.getParent() == root) {
                    Enumeration<MutableTreeNode> childEnumerator = node.children();
                    while (childEnumerator.hasMoreElements()) {
                        removeNodes.add(childEnumerator.nextElement());
                    }
                } else {
                    removeNodes.add(node);
                }
            }

            menuBuilder.addAction(new RemoveTreeNodes(removeNodes));
            menu = menuBuilder.toJPopupMenu();

            if (menu != null) {
                menu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
                menu.setVisible(true);
            }

        }

        private void DoubleClickEvent(MouseEvent e) {
            TreePath[] paths = getTreePaths(e);
            List<MutableTreeNode> leafNodes = getLeafNodes(paths);

            if (leafNodes.size() == 1 && leafNodes.get(0) instanceof DataTreeNode) {
                DataTreeNode dataNode = (DataTreeNode) (leafNodes.get(0));

                if (dataNode != null) {
                    dataNode.getDefaultAction().doAction();
                }
            }
        }

        private List<MutableTreeNode> getLeafNodes(TreePath[] treePaths) {

            ArrayList<MutableTreeNode> treeNodes = new ArrayList<MutableTreeNode>(treePaths.length);

            Object treeRoot = dataModel.getRoot();

            for (TreePath path : treePaths) {
                Object obj = path.getLastPathComponent();
                if (obj != treeRoot && obj instanceof MutableTreeNode) {
                    treeNodes.add((MutableTreeNode) obj);
                }
            }
            return treeNodes;
        }

        private TreePath[] getTreePaths(MouseEvent e) {
            Object source = e.getSource();
            TreePath[] paths = null;
            if (source instanceof JTree) {
                JTree tree = (JTree) source;

                paths = tree.getSelectionPaths();
            }
            if (paths == null) {
                paths = new TreePath[0];
            }

            return paths;
        }

        private void RecursiveFindDataNodes(TreeNode topNode, HashSet<DataTreeNode> dataTreeNodes) {
            Enumeration<TreeNode> childEnumerator = topNode.children();
            while (childEnumerator.hasMoreElements()) {
                RecursiveFindDataNodes(childEnumerator.nextElement(), dataTreeNodes);
            }
            if (topNode instanceof DataTreeNode) {
                if (!dataTreeNodes.contains(topNode)) {
                    dataTreeNodes.add((DataTreeNode) topNode);
                }
            }

        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                ContextMenuEvent(e);
            } else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                DoubleClickEvent(e);
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                TreePath[] paths = getTreePaths(e);
                List<MutableTreeNode> leafNodes = getLeafNodes(paths);

                if (leafNodes.size() == 1 && leafNodes.get(0) instanceof TimeSeriesNode) {
                    TimeSeriesNode dataNode = (TimeSeriesNode) (leafNodes.get(0));

                    //if (dataNode != null && scriptConsole!=null) {
                        //scriptConsole.setCurrentData(dataNode.getUserObject());
                    //}
                }
            }


        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

    }

    private class RemoveTreeNodes extends ReversableAction {

        private static final long serialVersionUID = 1L;

        private final List<MutableTreeNode> nodesToRemove;
        private final List<MutableTreeNode> nodesRemoved;

        private Hashtable<MutableTreeNode, UndoInfo> undoLUT;

        public RemoveTreeNodes(List<MutableTreeNode> nodesToRemove) {
            super("Remove data");

            this.nodesToRemove = nodesToRemove;
            this.nodesRemoved = new ArrayList<MutableTreeNode>((int) (nodesToRemove.size() * 1.2f));
        }

        @Override
        protected void action() throws ActionException {
            undoLUT = new Hashtable<MutableTreeNode, UndoInfo>(nodesToRemove.size());

            for (MutableTreeNode nodeToRemove : nodesToRemove) {
                TreeNode parentNode = nodeToRemove.getParent();
                removeNodeFromParent(nodeToRemove);
                ensureNoOrphans(parentNode);
            }
        }

        /**
         * Removes the node from the tree
         * 
         * @param nodeToRemove
         */
        private void removeNodeFromParent(MutableTreeNode nodeToRemove) {
            TreeNode nodeParent = nodeToRemove.getParent();

            if (nodeParent != null) {
                int nodeIndex = nodeParent.getIndex(nodeToRemove);

                undoLUT.put(nodeToRemove, new UndoInfo(nodeParent, nodeIndex));
                dataModel.removeNodeFromParent(nodeToRemove);
                nodesRemoved.add(nodeToRemove);
            }
        }

        /**
         * Walks up the node tree ensuring no orphan nodes are left
         * 
         * @param node
         */
        private void ensureNoOrphans(TreeNode node) {
            if (node instanceof MutableTreeNode && node.getChildCount() == 0) {
                MutableTreeNode nodeToRemove = (MutableTreeNode) node;
                TreeNode nodeParent = nodeToRemove.getParent();

                removeNodeFromParent(nodeToRemove);

                if (nodeParent != null) {
                    ensureNoOrphans(nodeParent);
                }
            }
        }

        @Override
        protected void undo() throws ActionException {
            int numOfFailures = 0;

            /*
             * To maintain same node order as before the removal. we add back in
             * the reverse order we remove them.
             */
            for (int i = nodesRemoved.size() - 1; i >= 0; i--) {
                MutableTreeNode nodeToRemove = nodesRemoved.get(i);

                UndoInfo undoInfo = undoLUT.get(nodeToRemove);
                if (undoInfo != null) {
                    if (undoInfo.nodeParent instanceof MutableTreeNode) {
                        dataModel.insertNodeInto(nodeToRemove,
                                (MutableTreeNode) undoInfo.nodeParent,
                                undoInfo.nodeIndex);
                    } else {
                        numOfFailures++;
                    }

                } else {
                    numOfFailures++;
                }

            }

            if (numOfFailures > 0) {
                UserMessages.showWarning("Undo clear failed for " + numOfFailures + " nodes");
            }

        }
    }

    private static class UndoInfo {
        final int nodeIndex;
        final TreeNode nodeParent;

        public UndoInfo(TreeNode nodeParent, int nodeIndex) {
            super();
            this.nodeParent = nodeParent;
            this.nodeIndex = nodeIndex;
        }
    }

}

/**
 * Describes how to build a folder structure for a wrapped data node when it is
 * being exported
 * 
 * @author Shu Wu
 */
class DataPath {
    private final DataTreeNode dataNode;
    private final Collection<String> position;

    public DataPath(DataTreeNode dataNode, Collection<String> position) {
        super();
        this.dataNode = dataNode;
        this.position = position;
    }

    public DataTreeNode getDataNode() {
        return dataNode;
    }

    public Collection<String> getPath() {
        return position;
    }

}

class RenameAction extends StandardAction {
    private static final long serialVersionUID = 1L;

    final DefaultMutableTreeNode myNode;

    public RenameAction(DefaultMutableTreeNode node)
    {
        super("Change label");
        myNode = node;
    }

    protected void action()
    {
        String newName = (String)myNode.getUserObject();
        newName = JOptionPane.showInputDialog(UIEnvironment.getInstance(),
                "Enter new label", newName);
        if(newName != null) {
            myNode.setUserObject(newName);
        }
    }
}

abstract class ExportAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    private static final ExportFileChooser fileChooser = new ExportFileChooser();

    public static void findDataItemsRecursive(MutableTreeNode node,
            ArrayList<String> position,
            Collection<DataPath> dataItemsPaths) {

        if (node instanceof DataTreeNode) {
            DataTreeNode dataNode = (DataTreeNode) node;

            if (dataNode.includeInExport()) {
                DataPath dataP = new DataPath(dataNode, position);
                dataItemsPaths.add(dataP);
            }
        }

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            Object obj = children.nextElement();
            if (obj instanceof MutableTreeNode) {
                MutableTreeNode childNode = (MutableTreeNode) obj;

                @SuppressWarnings("unchecked")
                ArrayList<String> childStack = (ArrayList<String>) position.clone();

                String childName = childNode.toString();
                childStack.add(childName);

                findDataItemsRecursive(childNode, childStack, dataItemsPaths);

            }
        }
    }

    private final Component parent;

    private final MutableTreeNode rootNode;

    public ExportAction(Component parent, MutableTreeNode rootNodeToExport, String description) {
        super(description);
        this.rootNode = rootNodeToExport;
        this.parent = parent;
    }

    protected File getUserSelectedFile(ExtensionFileFilter filter) throws UserCancelledException {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("unnamed." + filter.getExtension()));

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().exists()) {
                int response = JOptionPane.showConfirmDialog(fileChooser,
                        "File already exists, replace?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION);

                if (response != JOptionPane.YES_OPTION) {
                    throw new UserCancelledException();
                }
            }

            return fileChooser.getSelectedFile();
        } else {
            throw new UserCancelledException();
        }

    }

    protected File getUserSelectedFolder() throws UserCancelledException {
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = showSaveDialog();

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            throw new UserCancelledException();
        }

    }

    public MutableTreeNode getRootNode() {
        return rootNode;
    }

    public int showSaveDialog() throws HeadlessException {
        return fileChooser.showSaveDialog(parent);
    }

}

class ExportDelimitedFileAction extends ExportAction {
    private static final ExtensionFileFilter DATA_FILE_FILTER = new ExtensionFileFilter(
            "Delimited Text File", DataListView.DATA_FILE_EXTENSION);

    private static final long serialVersionUID = 1L;

    private static String getActionDescription(MutableTreeNode node) {
        if (isSingleFileExport(node)) {
            return "Export (Text) to file";
        } else {
            return "Export (Text) to folder";
        }
    }

    private static boolean isSingleFileExport(MutableTreeNode node) {
        if (node.getChildCount() == 0 && node instanceof DataTreeNode) {
            return true;
        }
        return false;
    }

    private final DelimitedFileExporter fileExporter;

    public ExportDelimitedFileAction(Component parent, MutableTreeNode nodeToExport) {
        super(parent, nodeToExport, getActionDescription(nodeToExport));

        fileExporter = new DelimitedFileExporter();
    }

    private void exportAllDataNodes() throws IOException, ActionException {

        /*
         * Use recursion to find data items
         */
        File folder = getUserSelectedFolder();

        folder.mkdir();

        ArrayList<DataPath> dataFilePaths = new ArrayList<DataPath>();
        findDataItemsRecursive(getRootNode(), new ArrayList<String>(), dataFilePaths);

        if (dataFilePaths.size() == 0) {
            throw new ActionException("Nothing to export");
        } else {
            for (DataPath dataPath : dataFilePaths) {

                Collection<String> folderPath = dataPath.getPath();
                DataTreeNode dataNode = dataPath.getDataNode();

                File dataFile = folder;
                for (String folderName : folderPath) {
                    dataFile = new File(dataFile, folderName);
                    dataFile.mkdir();

                    if (!dataFile.exists()) {
                        throw new ActionException("Problem creating folder: " + dataFile.toString());
                    }
                }

                dataFile = new File(dataFile, dataNode.toString() + '.'
                        + DataListView.DATA_FILE_EXTENSION);

                exportNode(dataNode, dataFile);
            }

        }

    }

    @Override
    protected void action() throws ActionException {
        try {
            if (isSingleFileExport(getRootNode())) {
                File file = getUserSelectedFile(DATA_FILE_FILTER);

                exportNode((DataTreeNode) getRootNode(), file);

                /*
                 * Export one data item
                 */
            } else {
                exportAllDataNodes();
            }

        } catch (IOException e) {
            e.printStackTrace();
            UserMessages.showWarning("Could not export: " + e.getMessage());
        }
    }

    protected void exportNode(DataTreeNode node, File file) throws IOException, ActionException {
        if (node instanceof SpikePatternNode) {
            fileExporter.export((((SpikePatternNode) node).getUserObject()), file);
        } else if (node instanceof TimeSeriesNode) {
            fileExporter.export((((TimeSeriesNode) node).getUserObject()), file);
        } else {
            throw new ActionException("Could not export node type: "
                    + node.getClass().getSimpleName());
        }
    }

}

class ExportFileChooser extends JFileChooser {

    private static final long serialVersionUID = 1L;

    public ExportFileChooser() {
        super();
        setAcceptAllFileFilterUsed(false);
    }

}

//class ExportMatlabAction extends ExportAction {
//    private static final ExtensionFileFilter MATLAB_FILE_FILTER = new ExtensionFileFilter(
//            "Matlab File", DataListView.MATLAB_FILE_EXTENSION);
//    private static final long serialVersionUID = 1L;
//    private MatlabExporter matlabExporter;
//
//    public ExportMatlabAction(Component parent, MutableTreeNode nodeToExport) {
//        super(parent, nodeToExport, "Export (Matlab) to file");
//        matlabExporter = new MatlabExporter();
//    }
//
//    @Override
//    protected void action() throws ActionException {
//        File file = getUserSelectedFile(MATLAB_FILE_FILTER);
//
//        ArrayList<DataPath> dataPaths = new ArrayList<DataPath>();
//        findDataItemsRecursive(getRootNode(), new ArrayList<String>(), dataPaths);
//
//        if (dataPaths.size() == 0) {
//            throw new ActionException("Nothing to export");
//        } else {
//            for (DataPath dataPath : dataPaths) {
//
//                Collection<String> path = dataPath.getPath();
//                DataTreeNode dataNode = dataPath.getDataNode();
//
//                StringBuilder name = new StringBuilder(200);
//                boolean first = true;
//                for (String nodeName : path) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        name.append(".");
//                    }
//
//                    name.append(nodeName);
//                }
//
//                addNode(dataNode, name.toString());
//            }
//
//            try {
//                matlabExporter.write(file);
//            } catch (IOException e) {
//                throw new ActionException("Error writing file: " + e.getMessage(), e);
//            }
//        }
//
//    }
//
//    protected void addNode(DataTreeNode node, String name) throws ActionException {
//
//        if (node instanceof SpikePatternNode) {
//            SpikePattern spikePattern = ((SpikePatternNode) node).getUserObject();
//            matlabExporter.add(name, spikePattern);
//        } else if (node instanceof TimeSeriesNode) {
//            TimeSeries spikePattern = ((TimeSeriesNode) node).getUserObject();
//            matlabExporter.add(name, spikePattern);
//        } else {
//            throw new ActionException("Could not export node type: "
//                    + node.getClass().getSimpleName());
//        }
//    }
//}

class ExtensionFileFilter extends FileExtensionFilter {

    private final String description;
    private final String extension;

    public ExtensionFileFilter(String description, String extension) {
        super();
        this.description = description;
        this.extension = extension;
    }

    @Override
    public boolean acceptExtension(String extension) {
        if (extension.compareTo(extension) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }

}