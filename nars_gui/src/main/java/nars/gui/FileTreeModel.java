
package nars.gui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.Arrays;

/**
 *http://java.dzone.com/news/taking-new-swing-tree-table-a-
 */
public class FileTreeModel implements TreeModel {

    private final File root;

    public FileTreeModel(File root) {
        this.root = root;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        //do nothing
    }

    @Override
    public Object getChild(Object parent, int index) {
        File f = (File) parent;
        return f.listFiles()[index];
    }

    @Override
    public int getChildCount(Object parent) {
        File f = (File) parent;
        return !f.isDirectory() ? 0 : f.list().length;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        File par = (File) parent;
        File ch = (File) child;
        return Arrays.asList(par.listFiles()).indexOf(ch);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
        File f = (File) node;
        return !f.isDirectory();
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        //do nothing
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        //do nothing
    }

}