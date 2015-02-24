/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package automenta.vivisect.javafx.tag;

import automenta.vivisect.javafx.JFX;
import automenta.vivisect.javafx.WebBrowser;
import automenta.vivisect.javafx.WikiTagger;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;


/**
*
* @author me
*/
public class NodeControlPane extends BorderPane {


    public NodeControlPane() {
        super();



        TabPane tab = new TabPane();
        tab.getTabs().add(newIndexTab());
        tab.getTabs().add(newWikiTab());
        tab.getTabs().add(newSpacetimeTab());
        tab.getTabs().add(newSpaceTab());
        tab.getTabs().add(newTimeTab());
        tab.getTabs().add(newOptionsTab());

        tab.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tab.autosize();

        setCenter(tab);

        FlowPane menu = new FlowPane();
        menu.getChildren().add(newAddButton());
        menu.getChildren().add(newBrowserButton());

        setBottom(menu);
    }

    public Button newBrowserButton() {
        Button b = new Button(/**/);
        AwesomeDude.setIcon(b, AwesomeIcon.LINK);
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                JFX.popup(new WebBrowser());
            }

        });
        return b;

    }

    public Button newAddButton() {
        Button b = new Button("+");
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                //popupObjectEdit(new NObject());
            }

        });
        return b;
    }


    public Tab newWikiTab() {
        final Tab t = new Tab(/*"Wiki"*/);
        AwesomeDude.setIcon(t, AwesomeIcon.TAGS);

        t.selectedProperty().addListener(new ChangeListener<Boolean>() {
            boolean firstvisible = true;

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean b, Boolean t1) {
                if (firstvisible) {
                    t.setContent(new WikiTagger("Self"));
                    firstvisible = false;
                }
            }

        });

        return t;
    }

    public Tab newSpaceTab() {
        Tab t = new Tab(/*"Space"*/);
        AwesomeDude.setIcon(t, AwesomeIcon.MAP_MARKER);


        SwingNode swingMap = new SwingNode();
        t.selectedProperty().addListener(new ChangeListener<Boolean>() {
            boolean firstvisible = true;
            @Override
            public void changed(ObservableValue<? extends Boolean> o, Boolean a, Boolean b) {
//                if (swingMap.isVisible() && firstvisible) {
//                    swingMap.setContent(new SwingMap( new GeoPosition(40.00, -80.00)));
//                    t.setContent(swingMap);
//                    firstvisible = false;
//                }
            }
        });
        return t;
    }
    public Tab newTimeTab() {
        Tab tab = new Tab(/*"Time"*/);
        AwesomeDude.setIcon(tab, AwesomeIcon.CLOCK_ALT);
        //tab.setContent(new TimePanel());

        return tab;
    }
    public Tab newSpacetimeTab() {
        Tab t = new Tab(/*"Spacetime"*/);
        AwesomeDude.setIcon(t, AwesomeIcon.CUBES);
        return t;
    }
    public Tab newOptionsTab() {
        Tab t = new Tab(/*"Options"*/);
        AwesomeDude.setIcon(t, AwesomeIcon.COGS);

        Accordion a =new Accordion();

        a.getPanes().addAll(new TitledPane("Identity", newIdentityPanel()), new TitledPane("Network", newNetworkPanel()), /*new TitledPane("Logic", newLogicPanel()),*/ new TitledPane("Database", newDatabasePanel()));

        for (TitledPane tp : a.getPanes())
            tp.setAnimated(false);

        t.setContent(a);
        return t;
    }
    public Tab newIndexTab() {
        Tab t = new Tab(/*"Index"*/);
        AwesomeDude.setIcon(t, AwesomeIcon.LIST);
        t.setContent(new IndexTreePane( new TaggerPane.TagReceiver() {

            @Override
            public void onTagSelected(String s) {
                //NetentionJFX.popupObjectView(core, core.data.get(s));
            }
        }));
        return t;
    }

    protected Node newNetworkPanel() {
        Pane p = new Pane();

        TextArea ta = new TextArea();
//        if (core.net!=null) {
//            ta.setText(core.net.getClass().getSimpleName());
//        }
//        else {
//            ta.setText("Offline");
//        }
        p.getChildren().add(ta);

        return p;
    }
    protected Node newLogicPanel() {



        final SwingNode s = new SwingNode();


        ScrollPane x = new ScrollPane(s);


        BorderPane flow = new BorderPane(x);
        flow.setPadding(new Insets(5, 5, 5, 5));
        flow.autosize();


        return flow;
    }
    protected Node newIdentityPanel() {
        Pane p = new Pane();

        /*if (core.getMyself()!=null)
            p.getChildren().add(new ObjectCard(core.getMyself()));*/

        return p;
    }
    protected Node newDatabasePanel() {
        Pane p = new Pane();

        /*TextArea ta = new TextArea();
        ta.setText(core.db.toString() + "\n" + core.db.getEngine().toString() );
        p.getChildren().add(ta);*/

        //core.data.sizeLong()

        return p;
    }

//    public static void popupObjectEdit(NObject n) {
//        Stage st = new Stage();
//
//        /*
//        st.setTitle(n.id);
//        st.setScene(new Scene(new ObjectEditPane(core, n), 600, 400));
//        */
//
//        st.show();
//    }

}
