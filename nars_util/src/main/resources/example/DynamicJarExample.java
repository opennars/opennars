/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

import objenome.Multitainer;
import objenome.Jartainer;
import objenome.Objenome;
import static objenome.solution.dependency.Builder.of;

/**
 *
 * @author me
 */
public class DynamicJarExample {
    
    public static void main(String[] args) throws Exception {
        
        Jartainer j = new Jartainer("file:///home/me/share/opennars/dist/OpenNARS_GUI.jar");
        j.usable("nars.core.build.Default");
        j.usable("nars.core.build.Neuromorphic");
        j.usable("nars.core.Build");
        Object o = j.get("nars.core.NAR");
        
        System.out.println(o.getClass());
        System.out.println(o);
        
        System.out.println(j.getClasses());

        Multitainer g = new Multitainer(j);
        g.any(j.getClass("nars.core.Build"), 
                of(j.getClass("nars.core.build.Default"), j.getClass("nars.core.build.Neuromorphic")));
        
        Objenome objenome = g.random(j.getClass("nars.core.Build"));        
        
        System.out.println(objenome);
        System.out.println(objenome.getSolutions());
        
        Object b = objenome.get(j.getClass("nars.core.Build"));
        System.out.println(b);
        System.out.println(b.getClass());
        
        //g.(o.getClass(), o);
//        URL u = new URL("file:///home/me/share/opennars/dist/OpenNARS_GUI.jar");
//        URLClassLoader classes = new URLClassLoader (new URL[] { u });
//
//  
//        Class nar = Class.forName ("nars.core.NAR", true, classes);
//        Class build = Class.forName ("nars.core.Build", true, classes);
//        Class defaultNAR = Class.forName ("nars.core.build.Default", true, classes);
//        
//        Container c = new Container();
//        c.use(build, defaultNAR);
//        Object x = c.get(nar);
//        
//        System.out.println(x);
//    
    }
}
