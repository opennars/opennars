/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import javafx.scene.control.ChoiceBox;

/**
 *
 * @author me
 */
public class ScopeSelect extends ChoiceBox<Scope> {

    static final Scope defaultScope = Scope.Public;
    
    public ScopeSelect() {
        super();
        getItems().addAll(Scope.values());
        getSelectionModel().select(defaultScope);
    }
    
    public Scope getScope() {
        return getSelectionModel().getSelectedItem();
    }
    
}
