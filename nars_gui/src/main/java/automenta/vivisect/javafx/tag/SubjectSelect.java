/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.tag;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

import java.util.Collection;


/**
 * Combobox for selecting subjects (ex: users)
 * @author me
 */
public class SubjectSelect extends ComboBox<Object> {

    public SubjectSelect(final Collection<Object> subjects) {
        super();
        
        setConverter(new StringConverter<Object>() {
            
            @Override public String toString(Object object) {
                if (object == null) return "?";
                
                /*if (object.name!=null)
                    return object.name;*/
                return object.toString();
            }

            @Override public Object fromString(String string) {
                Object first = null;
                for (Object n : subjects) {
                    if (first == null) first = n;
                    if (n.toString().equals(string))
                        return n;
                }
                return first;
            }
        });
        
        getItems().addAll(subjects);        
        
        setTooltip(new Tooltip("Who?"));        
        
        setEditable(true);
        

        
    }
    
    
}
