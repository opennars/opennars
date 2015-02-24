/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import automenta.vivisect.javafx.WikiBrowser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

/**
 *
 * @author me
 */
public class TaggerPane extends BorderPane  {
    private final ChoiceBox<String> mode;
    private final TagReceiver receiver;

    
    public static interface TagReceiver {
        public void onTagSelected(String s);
    }
    
    public TaggerPane(TagReceiver receiver) {
        super();
        
        this.receiver = receiver;
        
        mode = new ChoiceBox<String>();
        mode.getItems().addAll("Wiki", "Index");
        mode.getSelectionModel().select(0);
        
        mode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateMode(newValue);
            }            
        });
        setTop(new FlowPane(mode));

        
        
        updateMode(mode.getSelectionModel().getSelectedItem());
        
    }
    protected void updateMode(String m) {
        Node c = null;
        if (m.equals("Wiki")) {
            c = new WikiBrowser("Life") {
                @Override public void onTagClicked(String id) {
                    receiver.onTagSelected(id);
                }                
            };
        }
        else if (m.equals("Index")) {
            c = new IndexTreePane(receiver);
        }
                
        setCenter(c);
    }

    
}
