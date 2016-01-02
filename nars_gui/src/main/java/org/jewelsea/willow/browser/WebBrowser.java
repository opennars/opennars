/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow.browser;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jewelsea.willow.navigation.NavTools;
import org.jewelsea.willow.util.DebugUtil;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static org.jewelsea.willow.util.ResourceUtil.getString;

public class WebBrowser extends BorderPane {
    @Deprecated
    public static final long start = System.currentTimeMillis();

    private final AnchorPane overlayLayer;
    private final BorderPane underlayLayer;

    public abstract static class Route {
        public final String url;
        public final String name;

        @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
        public Route(String url, String name) {
            this.url = url;
            this.name = name;
        }

        public abstract Object handle(Map<String,String> parameters);
    }

    public static class Router {
        public final Map<String, Route> routes = new HashMap();

        public void add(Route r) {
            routes.put(r.url, r);
        }

        public Route get(String url) {
            for (Map.Entry<String, Route> stringRouteEntry : routes.entrySet()) {
                if (stringRouteEntry.getKey().equals(url))
                    return stringRouteEntry.getValue();
            }
            return null;
        }
    }

    public final Router router = new Router();


    public static final String APPLICATION_ICON =
            "WillowTreeIcon.png";

    public static final String DEFAULT_HOME_LOCATION =
            "about:";

    @SuppressWarnings("HardcodedFileSeparator")
    public static final String STYLESHEET =
            "/resources/browser.css";

    public StringProperty homeLocationProperty = new SimpleStringProperty(DEFAULT_HOME_LOCATION);
    private static final double INITIAL_SCENE_HEIGHT = 600;
    private static final double INITIAL_SCENE_WIDTH = 1121;
    private Node sidebar;                              // sidebar for controlling the app.
    private final TabManager tabManager;                        // tab manager for managing browser tabs.
    private final BorderPane mainLayout = new BorderPane();     // layout of the browser application.
    private final TextField chromeLocField = new TextField();   // current location of the current browser or a value being updated by the user to change the current browser's location.
    // change listeners to tie the location of the current browser to the chromeLocField and vice versa (binding cannot be used because both these values must be read/write).
    private ChangeListener<String> browserLocFieldChangeListener;
    private ChangeListener<String> chromeLocFieldChangeListener;




    /*public NObject newBrowserState() {
        List<String> urls = new ArrayList();
        for (Tab t : tabManager.tabPane.getTabs()) {
            if (t instanceof BrowserTab) {
                BrowserTab bt = (BrowserTab)t;
                urls.add(bt.getLocation());
            }
        }
        NObject o = core.newObject("Web Browsing @ " + new Date().toString());
        o.add("Web");
        for (String u : urls)
            o.add(u);
        return o;
    }*/

    public WebBrowser() {

        System.out.println("WebBrowser.start()" + (System.currentTimeMillis() - start)/1000.0);


        /*
        try {
            new SchemaOrg(core);
        } catch (IOException ex) {
            Logger.getLogger(WebBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
        */

        initRoutes();

        // set the title bar to the title of the web page (if there is one).
        //stage.setTitle(getString("browser.name"));

        // initialize the stuff which can't be initialized in the init method due to stupid threading issues.
        tabManager = new TabManager(chromeLocField);


        System.out.println("created tabs" + (System.currentTimeMillis() - start)/1000.0);


        //sidebar = SideBar.createSidebar(this);


        //System.out.println("created sidebar" + (System.currentTimeMillis() - start)/1000.0);

        // initialize the location field in the Chrome.
        chromeLocField.setPromptText(getString("location.prompt"));
        chromeLocField.setTooltip(new Tooltip(getString("location.tooltip")));
        chromeLocField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                go(chromeLocField.getText());
            }
        });

        // setup the main layout.
        HBox.setHgrow(chromeLocField, Priority.ALWAYS);
        Pane navPane = NavTools.createNavPane(this);
        mainLayout.setTop(navPane);

        //System.out.println("navpane added " + (System.currentTimeMillis() - start)/1000.0);




        // add an overlay layer over the main layout for effects and status messages.
        overlayLayer = new AnchorPane();
        underlayLayer = new BorderPane();
        StackPane overlaidLayout = new StackPane();
        overlaidLayout.getChildren().addAll(underlayLayer, mainLayout, overlayLayer);
        overlayLayer.setPickOnBounds(false);

        underlayLayer.setPrefWidth(Double.MAX_VALUE);
        underlayLayer.setPrefHeight(Double.MAX_VALUE);
        underlayLayer.setFocusTraversable(false);




        // monitor the tab manager for a change in the browser window and update the display appropriately.
        tabManager.browserProperty().addListener((observableValue, oldBrowser, newBrowser) ->
                browserChanged(oldBrowser, newBrowser, null, overlayLayer)
        );

        //System.out.println("creating scene " + (System.currentTimeMillis() - start)/1000.0);


        // create the scene.
        Scene scene = new Scene(
                overlaidLayout,
                INITIAL_SCENE_WIDTH,
                INITIAL_SCENE_HEIGHT
        );
        scene.getStylesheets().add(STYLESHEET);
        //overlaidLayout.setStyle("-fx-background: rgba(100, 0, 0, 0)");

        // set some sizing constraints on the scene.
        overlayLayer.prefHeightProperty().bind(scene.heightProperty());
        overlayLayer.prefWidthProperty().bind(scene.widthProperty());



        addOverlayLog();

        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToHeight(true);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setMinWidth(250);
        sidebarScroll.setMaxWidth(250);
        //mainLayout.setLeft(sidebarScroll);


        //System.out.println("sidebar added " + (System.currentTimeMillis() - start)/1000.0);

        // highlight the entire text if we click on the chromeLocField so that it can be easily changed.
        chromeLocField.focusedProperty().addListener((observableValue, from, to) -> {
            if (to) {
                // run later used here to override the default selection rules for the textfield.
                Platform.runLater(chromeLocField::selectAll);
            }
        });

        // make the chrome location field draggable.
        chromeLocField.getStyleClass().add("location-field");
        chromeLocField.setOnDragDetected(mouseEvent -> {
            Dragboard db = chromeLocField.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(chromeLocField.getText());
            db.setContent(content);
        });

        // automatically hide and show the sidebar and navbar as we transition in and out of fullscreen.
        /*
        final Button navPaneButton = createNavPaneButton(navPane);
        stage.fullScreenProperty().addListener((observableValue, oldValue, newValue) -> {
            if ((stage.isFullScreen() && getSidebar().getScroll().isVisible()) ||
                    (!stage.isFullScreen() && !getSidebar().getScroll().isVisible())) {
                ((Button) scene.lookup("#sidebarButton")).fire();
            }
            if ((stage.isFullScreen() && navPane.isVisible()) ||
                    (!stage.isFullScreen() && !navPane.isVisible())) {
                navPaneButton.fire();
            }
        });
        */

        // create a new tab when the user presses Ctrl+T
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.T)) {
                tabManager.getNewTabButton().fire();
            }
        });

        //getSidebarDisplay().setMaxWidth(getSidebarDisplay().getWidth());

        // add an icon for the application.
        //stage.getIcons().add(ResourceUtil.getImage(APPLICATION_ICON));

        //sidebar.getScroll().setPrefViewportWidth(sidebar.getBarDisplay().getWidth());

        // debugging routine.
        //debug(scene);

        // nav to the home location

        go(homeLocationProperty.get());

        // we need to manually handle the change from no browser at all to an initial browser.
        //browserChanged(null, getBrowser(), stage, overlayLayer);

        System.out.println("WebBrowser.start() finished " + (System.currentTimeMillis() - start)/1000.0);

        // show the scene.
        //stage.setScene(scene);
        //stage.show();


        /*
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent e) {
                //core.publish(newBrowserState());
            }
        });*/

        setCenter(overlaidLayout);



    }

    private void debug(Scene scene) {
        System.getProperties().list(System.out);
        //ScenicView.show(scene);
        Platform.runLater(() -> DebugUtil.dump(scene.getRoot()));
    }

    // creates a button to hide and show the navigation pane.
    private Button createNavPaneButton(Pane navPane) {
        Button navPaneButton = new Button();

        DoubleProperty startHeight = new SimpleDoubleProperty();

        // todo java 8 has a weird background issue on resize.
        // hide sidebar.
        Animation hideNavPane = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            @Override
            protected void interpolate(double frac) {
                double curHeight = startHeight.get() * (1.0 - frac);
                navPane.setPrefHeight(curHeight);   // todo resize a spacing underlay to allow the scene to adjust.
                navPane.setTranslateY(-startHeight.get() + curHeight);
            }
        };
        hideNavPane.onFinishedProperty().set(actionEvent -> navPane.setVisible(false));

        // show sidebar.
        Animation showNavPane = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            @Override
            protected void interpolate(double frac) {
                double curHeight = startHeight.get() * frac;
                navPane.setPrefHeight(curHeight);
                navPane.setTranslateY(-startHeight.get() + curHeight);
            }
        };

        navPaneButton.setOnAction(actionEvent -> {
            navPane.setMinHeight(Control.USE_PREF_SIZE);

            if (showNavPane.statusProperty().get().equals(Animation.Status.STOPPED) && hideNavPane.statusProperty().get().equals(Animation.Status.STOPPED)) {
                if (navPane.isVisible()) {
                    startHeight.set(navPane.getHeight());
                    hideNavPane.play();
                } else {
                    navPane.setVisible(true);
                    showNavPane.play();
                }
            }
        });

        return navPaneButton;
    }

    /**
     * Handler for when a new browser is switched into the chrome.
     *
     * @param oldBrowser   the old browser we were to displaying (or none if there is no such thing).
     * @param newBrowser   the new browser we are to display.
     * @param stage        the stage displaying the chrome.
     * @param overlayLayer the overlay layer for status and other information in the chrome.
     */
    private void browserChanged(UITab oldTab, UITab newTab, Stage stage, AnchorPane overlayLayer) {


        if (oldTab instanceof BrowserTab) {
            BrowserTab oldBrowserTab = (BrowserTab)oldTab;
            BrowserWindow oldBrowser = oldBrowserTab.getBrowser();
            // cleanup the links between the chrome's location field and the old browser's location field.
            if (oldBrowser != null && browserLocFieldChangeListener != null) {
                oldBrowser.getLocField().textProperty().removeListener(browserLocFieldChangeListener);
            }
            if (chromeLocFieldChangeListener != null) {
                chromeLocField.textProperty().removeListener(chromeLocFieldChangeListener);
            }
        }


        if (newTab instanceof BrowserTab) {
            BrowserTab newBrowserTab = (BrowserTab)newTab;
            BrowserWindow newBrowser = newBrowserTab.getBrowser();

            // update the stage title to monitor the page displayed in the selected browser.
            // todo hmm I wonder how the listeners ever get removed...
            newBrowser.getView().getEngine().titleProperty().addListener((observableValue, oldTitle, newTitle) -> {
                if (newTitle != null && !newTitle.isEmpty()) {
                    //stage.setTitle(getString("browser.name") + " - " + newTitle);
                } else {
                    // necessary because when the browser is in the process of loading a new page, the title will be empty.  todo I wonder if the title would be reset correctly if the page has no title.
                    if (!newBrowser.getView().getEngine().getLoadWorker().isRunning()) {
                        //stage.setTitle(getString("browser.name") );
                    }
                }
            });

            // monitor the status of the selected browser.
            //overlayLayer.getChildren().clear();

            StatusDisplay statusDisplay = new StatusDisplay(newBrowser.statusProperty());

            //statusDisplay.translateXProperty().bind(getSidebarDisplay().widthProperty().add(20).add(getSidebarDisplay().translateXProperty()));
            //statusDisplay.translateYProperty().bind(overlayLayer.heightProperty().subtract(50));
            //overlayLayer.getChildren().add(statusDisplay);

            // monitor the loading progress of the selected browser.

            statusDisplay.setLoadControl(
                new LoadingProgressDisplay(
                    newBrowser.getView().getEngine().getLoadWorker()
                )
            );

            // make the chrome's location field respond to changes in the new browser's location.
            browserLocFieldChangeListener = (observableValue, oldLoc, newLoc) -> {
                if (!chromeLocField.getText().equals(newLoc)) {
                    chromeLocField.setText(newLoc);
                }
            };
            newBrowser.getLocField().textProperty().addListener(browserLocFieldChangeListener);

            // make the new browser respond to changes the user makes to the chrome's location.
            chromeLocFieldChangeListener = (observableValue, oldLoc, newLoc) -> {
                if (!newBrowser.getLocField().getText().equals(newLoc)) {
                    newBrowser.getLocField().setText(newLoc);
                }
            };
            chromeLocField.textProperty().addListener(chromeLocFieldChangeListener);
            chromeLocField.setText(newBrowser.getLocField().getText());

            // enable forward and backward buttons as appropriate.
            Button forwardButton = (Button) mainLayout.lookup("#forwardButton");
            if (forwardButton != null) {
                forwardButton.disableProperty().unbind();
                forwardButton.disableProperty().bind(newBrowser.getHistory().canNavForwardProperty().not());
            }
            Button backButton = (Button)mainLayout.lookup("#backButton");


            if (forwardButton != null) {
                backButton.disableProperty().unbind();
                backButton.disableProperty().bind(newBrowser.getHistory().canNavBackwardProperty().not());
            }

            // display the selected browser.
            mainLayout.setCenter(newBrowser.getView());
            mainLayout.setBottom(statusDisplay);
        }
        else if (newTab instanceof UITab) {
            mainLayout.setCenter(newTab.content());
        }
    }

    public UITab getBrowser() {
        return tabManager.getBrowser();
    }

    public Node getSidebar() {
        return sidebar;
    }

    public Node getSidebarDisplay() {
        return sidebar;
    }

    public TextField getChromeLocField() {
        return chromeLocField;
    }

    public TabManager getTabManager() {
        return tabManager;
    }



    /** URL router */
    private void go(String url) {
        Route rr = router.get(url);
        if (rr!=null) {
            Object r = rr.handle(new HashMap());
            if (r!=null) {
                Node c;
                //noinspection IfStatementWithTooManyBranches
                if (r instanceof String) {
                    //html
                    c = new WebView();
                    ((WebView)c).setFontSmoothingType(FontSmoothingType.GRAY);
                    ((WebView)c).getEngine().loadContent((String)r);
                }
                else if (r instanceof Node) {
                    //javafx component
                    c = (Node)r;
                }
                else if (r instanceof JComponent) {
                    //swing
                    c = new SwingNode();
                    ((SwingNode)c).setContent((JComponent)r);
                }
                else {
                    c = new Label(r.toString());
                }

                UITab u = new UITab(c);
                u.setText(rr.name);
                getTabManager().addTab(u);
                return;
            }
        }


        //Load URL in BrowserTab
        if (getBrowser() instanceof BrowserTab)
            ((BrowserTab)getBrowser()).getBrowser().go(url);
        else {
            //create a new tab because we were in a  UITab (not BrowserTab)
            BrowserTab bt = new BrowserTab(tabManager);
            getTabManager().addTab(bt);
            bt.getBrowser().go(url);
        }
    }


    protected void initRoutes() {
//        router.add(new Route("about:ontology", "Ontology") {
//            @Override public Object handle(Map<String, String> parameters) {
//                return new IndexTreePane(core, null);
//            }
//        });

        router.add(new Route("about:", "System") {
            @Override public Object handle(Map<String, String> parameters) {
                String html = "";
                html += router.routes.toString();
                return html;
            }
        });

        /*
        router.add(new Route("about:logic/memory", "Logic Memory") {
            @Override public Object handle(Map<String, String> parameters) {
                return new MemoryView(core.logic);
            }
        });
        */
        /*router.add(new Route("about:logic", "Logic") {
            @Override public Object handle(Map<String, String> parameters) {
                return new NARControls(core.logic);
            }
        });*/

    }

    protected void addOverlayLog() {
        /*TextArea logoutput = new TextArea();
        logoutput.setPrefRowCount(10);

        logoutput.setEditable(false);

        new TextOutput(core.logic) {

            @Override
            protected void outputString(String s) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        logoutput.appendText(s+"\n");
                    }
                });
            }

        };

        mainLayout.setOpacity(0.9);
        underlayLayer.setCenter(logoutput);*/
    }

    public static void main(String[] args) {
        Application.launch();
    }

}