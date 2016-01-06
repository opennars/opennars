package nars.guifx;

import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import nars.NAR;

/**
 * displays a tree representation of NARS components, including: Tasks Judgments
 * Questions Goals
 * 
 * Plugins
 * 
 * ..
 * 
 */
public class NARTree extends TreeView {

	private final NAR nar;

	public NARTree(NAR n) {
        nar = n;
        CheckBoxTreeItem<String> rootItem =
                new CheckBoxTreeItem<>("View Source Files");

        rootItem.setExpanded(true);


        setRoot(rootItem);

        setEditable(true);

        //setCellFactory(CheckBoxTreeCell.<String>forTreeView());



        for (int i = 0; i < 8; i++) {
            TreeItem<String> checkBoxTreeItem =
                    new TreeItem<>("Sample" + (i + 1));

            rootItem.getChildren().add(checkBoxTreeItem);
        }

        setRoot(rootItem);
        setShowRoot(true);

    }
}
