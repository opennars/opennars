/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author me
 */
public class ObjectCard extends VBox {
    
    public ObjectCard(Object n) {
        super();
        
        Label nameLabel = new Label(n.toString());
        nameLabel.setFont(Font.getDefault().font(24f));
        getChildren().add(nameLabel);
        
        //getChildren().add(new Label(n.getTags().toString()));
    }
}
