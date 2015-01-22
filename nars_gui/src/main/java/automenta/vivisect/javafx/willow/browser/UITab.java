/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.vivisect.javafx.willow.browser;

import automenta.vivisect.javafx.willow.navigation.History;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 *
 * @author me
 */
public class UITab<N extends Node> extends Tab {
    private final N contnt;
    

    public UITab(N content) {
        super();
        this.contnt = content;
        
    }
    
    public N content() {
        return contnt;
    }    
    
    protected void init() {   }
    
    public History getHistory() {
        return null;
    }
    public void go(String loc) {
        //...
    }

    public String getLocation() {
        return "about:?";
    }
    
    
}
