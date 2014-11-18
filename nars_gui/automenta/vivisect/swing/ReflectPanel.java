/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.swing;

/**
 * L2FProd Common v9.2 License.
 *
 * Copyright 2005 - 2009 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import automenta.vivisect.swing.property.propertysheet.PropertySheet;
import automenta.vivisect.swing.property.propertysheet.PropertySheetPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.SimpleBeanInfo;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Auto-reflect an object into a Swing PropertySheetPage
 */
public class ReflectPanel<O> extends JPanel {


    public static void main(String[] args) throws Exception {
        
        //Example
        JFrame frame = new JFrame("PropertySheet");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add("Center", new ReflectPanel(frame));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

    public ReflectPanel(O instance) {
        super(new BorderLayout());
        
        BeanInfo beanInfo = new SimpleBeanInfo();
        try {
            beanInfo = Introspector.getBeanInfo(instance.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        final PropertySheetPanel sheet = new PropertySheetPanel();
        sheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        sheet.setToolBarVisible(false);
        sheet.setDescriptionVisible(false);
        sheet.setBeanInfo(beanInfo);

        sheet.setPreferredSize(new Dimension(100,100));
        
        // initialize the properties with the value from the object
        // one can use sheet.readFromObject(button)
        // but I encountered some issues with Java Web Start. The method
        // getLocationOnScreen on the button is throwing an exception, it
        // does not happen when not using Web Start. Load properties one
        // by one as follow will do the trick
        automenta.vivisect.swing.property.propertysheet.Property[] properties = sheet.getProperties();
        for (int i = 0, c = properties.length; i < c; i++) {
            try {
                properties[i].readFromObject(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // everytime a property change, update the button with it
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                automenta.vivisect.swing.property.propertysheet.Property prop = (automenta.vivisect.swing.property.propertysheet.Property) evt.getSource();
                prop.writeToObject(instance);
                
                
                //button.repaint();
            }
        };
        sheet.addPropertySheetChangeListener(listener);

        add(sheet, BorderLayout.CENTER);
    }

}
