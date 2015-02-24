

package automenta.vivisect.javafx.tag;


import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;

/**
 *
 * @author me
 */
abstract public class SaveObjectPane extends BorderPane {

    public SaveObjectPane() {

        //TEMP:
        ArrayList a = new ArrayList();
        a.add("Anonymous");

        SubjectSelect s = new SubjectSelect(a /*core.getUsers()*/);
        //s.setTooltip(new InfoWindow.Tooltip("Re:"));
        s.getSelectionModel().select(a.get(0) /* myself */);
        

        ScopeSelect scope = new ScopeSelect();
        
        Button cancelButton = AwesomeDude.createIconButton(AwesomeIcon.UNDO);
        cancelButton.setTooltip(new Tooltip("Cancel"));
        
        Button saveButton = AwesomeDude.createIconButton(AwesomeIcon.SAVE);
        saveButton.setTooltip(new Tooltip("Save"));
        
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) {                         
                onSave(scope.getScope(), s.getSelectionModel().getSelectedItem()); 
            }
        });
        cancelButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) { onCancel(); }
        });
        
        setLeft(s);
        
        FlowPane fp = new FlowPane(scope, cancelButton, saveButton);
        fp.setAlignment(Pos.BOTTOM_RIGHT);
        
        setCenter(fp);
        
    }
    
    public abstract void onCancel();
    public abstract void onSave(Scope scope, Object subject);
}
