/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import automenta.vivisect.javafx.WikiBrowser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author me
 */
public abstract class OperatorTagPane extends BorderPane {
    private final WikiBrowser outer;

    public OperatorTagPane(String tag, final WikiBrowser outer) {
        super();
        this.outer = outer;
        autosize();
        
        setPadding(new Insets(4,4,4,4));
        
        Label label = new Label(tag);
        label.setFont(label.getFont().font( Font.getDefault().getSize() * 1.4f ));
        label.setTextOverrun(OverrunStyle.CLIP);
        setTop(label);
        
        final ToggleGroup gk = new ToggleGroup();

        ToggleButton k1 = new ToggleButton("Learn");
        k1.setToggleGroup(gk);
        ToggleButton k2 = new ToggleButton("Do");
        k2.setToggleGroup(gk);
        ToggleButton k3 = new ToggleButton("Teach");
        k3.setToggleGroup(gk);
        
        TilePane k = new TilePane(k1,k2,k3);
        
        final ToggleGroup gn = new ToggleGroup();
        
        ToggleButton n1 = new ToggleButton("Can");
        n1.setToggleGroup(gn);
        ToggleButton n2 = new ToggleButton("Need");
        n2.setToggleGroup(gn);
        ToggleButton n3 = new ToggleButton("Not");
        n3.setToggleGroup(gn);
        
        TilePane n = new TilePane(n1,n2,n3);
        
        
        VBox c = new VBox(k, n);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(4,4,8,4));
        setCenter(c);
        
        
        setBottom(new SaveObjectPane() {

            @Override
            public void onCancel() {
                //onFinished(false, null, null);
            }

            @Override
            public void onSave(Scope scope, Object subject) {
                List<String> tags = new ArrayList();
                
                ToggleButton selectedK = (ToggleButton)gk.getSelectedToggle();
                ToggleButton selectedN = (ToggleButton)gn.getSelectedToggle();
                if (selectedK!=null)
                    tags.add(selectedK.getText());
                if (selectedN!=null)
                    tags.add(selectedN.getText());
                
                //onFinished(true, subject, tags);
            }
            
        });
    }
       
    //public abstract void onFinished(boolean save, NObject subject, Collection<String>values);
        
}
