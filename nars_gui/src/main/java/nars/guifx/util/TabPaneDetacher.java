package nars.guifx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;

public class TabPaneDetacher {

    private TabPane tabPane;
    private Tab currentTab;
    private final List<Tab> originalTabs;
    private final Map<Integer, Tab> tapTransferMap;
    private String[] stylesheets;
    private final BooleanProperty alwaysOnTop;

    public TabPaneDetacher() {
        originalTabs = new ArrayList<>();
        stylesheets = new String[]{};
        tapTransferMap = new HashMap<>();
        alwaysOnTop = new SimpleBooleanProperty();
    }

    /**
     * Creates a new instance of the TabPaneDetacher
     *
     * @return The new instance of the TabPaneDetacher.
     */

    public BooleanProperty alwaysOnTopProperty() {
        return alwaysOnTop;
    }

    public Boolean isAlwaysOnTop() {
        return alwaysOnTop.get();
    }

    /**
     * 
     * Sets whether detached Tabs should be always on top.
     * 
     * @param alwaysOnTop The state to be set.
     * @return The current TabPaneDetacher instance.
     */
    public TabPaneDetacher alwaysOnTop(boolean alwaysOnTop){
        alwaysOnTopProperty().set(alwaysOnTop);
        return this;
    }
    
    /**
     * Sets the stylesheets that should be assigend to the new created {@link Stage}.
     *
     * @param stylesheets The stylesheets to be set.
     * @return The current TabPaneDetacher instance.
     */
    public TabPaneDetacher stylesheets(String... stylesheets) {
        this.stylesheets = stylesheets;
        return this;
    }

    /**
     * Make all added {@link Tab}s of the given {@link TabPane} detachable.
     *
     * @param tabPane The {@link TabPane} to take over.
     * @return The current TabPaneDetacher instance.
     */
    public TabPaneDetacher makeTabsDetachable(TabPane tabPane) {
        this.tabPane = tabPane;
        originalTabs.addAll(tabPane.getTabs());
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            tapTransferMap.put(i, tabPane.getTabs().get(i));
        }

        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (change.next()) {
                change.getAddedSubList().forEach(x -> {
                    Button p = new Button("^");
                    p.setOnMouseClicked(en -> popout(x));
                    x.setGraphic(p);
                });
            }
        });

//        tabPane.getTabs().forEach(x -> {
//
//
//
//        });

//        tabPane.setOnDragDetected(
//                (MouseEvent event) -> {
//
//                    //only apply to dragging tab labels
//                    {
//                        Node where = event.getPickResult().getIntersectedNode();
//                        if (where == null) return;
//                        Node parent = where.getParent();
//
//                        if (parent == null) return;
//                        if (parent.getStyleClass().size() == 0) return;
//                        if (!parent.getStyleClass().get(0).equals("tab-label")) {
//                            return;
//                        }
//                    }
//
//                    if (event.getSource() instanceof TabPane) {
//                        Pane rootPane = (Pane) tabPane.getScene().getRoot();
//                        rootPane.setOnDragOver((DragEvent event1) -> {
//                            event1.acceptTransferModes(TransferMode.ANY);
//                            event1.consume();
//                        });
//                        currentTab = tabPane.getSelectionModel().getSelectedItem();
//                        SnapshotParameters snapshotParams = new SnapshotParameters();
//                        snapshotParams.setTransform(Transform.scale(0.4, 0.4));
//                        WritableImage snapshot = currentTab.getContent().snapshot(snapshotParams, null);
//                        Dragboard db = tabPane.startDragAndDrop(TransferMode.MOVE);
//                        ClipboardContent clipboardContent = new ClipboardContent();
//                        clipboardContent.put(DataFormat.PLAIN_TEXT, "detach");
//                        db.setDragView(snapshot, 40, 40);
//                        db.setContent(clipboardContent);
//                    }
//                    event.consume();
//                }
//        );
//        tabPane.setOnDragDone(
//                (DragEvent event) -> {
//                    popout(currentTab);
//                    tabPane.setCursor(Cursor.DEFAULT);
//                    event.consume();
//                }
//        );
        return this;
    }

    /**
     * Opens the content of the given {@link Tab} in a separate Stage. While the content is removed from the {@link Tab} it is
     * added to the root of a new {@link Stage}. The Window title is set to the name of the {@link Tab};
     *
     * @param tab The {@link Tab} to get the content from.
     */
    public void popout(Tab tab) {
        if(tab == null){
            return;
        }
        int originalTab = originalTabs.indexOf(tab);
        tapTransferMap.remove(originalTab);
        Parent content = (Parent)tab.getContent();
        if (content == null) {
            throw new IllegalArgumentException("Can not detach Tab '" + tab.getText() + "': content is empty (null).");
        }
        tab.setContent(null);

        double W = content.getLayoutBounds().getWidth();
        double H = content.getLayoutBounds().getHeight();
        Scene scene = new Scene(content, W, H);

        scene.getStylesheets().addAll(stylesheets);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(tab.getText());
        stage.setAlwaysOnTop(isAlwaysOnTop());
        //Point2D p = MouseRobot.getMousePosition();
        //stage.setX(p.getX());
        //stage.setY(p.getY());
        stage.setOnCloseRequest((WindowEvent t) -> {
            stage.close();
            tab.setContent(content);
            int originalTabIndex = originalTabs.indexOf(tab);
            tapTransferMap.put(originalTabIndex, tab);
            int index = 0;
            SortedSet<Integer> keys = new TreeSet<>(tapTransferMap.keySet());
            for (Integer key : keys) {
                Tab value = tapTransferMap.get(key);
                if(!tabPane.getTabs().contains(value)){
                    tabPane.getTabs().add(index, value);
                }
                index++;
            }
            tabPane.getSelectionModel().select(tab);
        });
        stage.setOnShown((WindowEvent t) -> tab.getTabPane().getTabs().remove(tab));
        stage.show();
    }

}
