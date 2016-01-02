/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.demo.swing;


import automenta.spacegraph.demo.Demo;

/**
 *
 * @author seh
 */
public class RunAbout implements Demo {

    @Override
    public String getName() {
        return "What is Netention?";
    }

    @Override
    public String getDescription() {
        return "Introduction to the Netention system";
    }

    @Override
    public JPanel newPanel() {
        JPanel j = new JPanel();
        return j;
    }

    //explain netention
    //include all documentation
    //powered by...
    /*
     * Java
     * dANN
     * JOGL
     * etc
     */

}
