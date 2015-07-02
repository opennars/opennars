/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.solution.dependency.Builder;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Container representing dynamically loaded .jar files and all they provide
 */
public class Jartainer extends Container {

    //TODO store as String or something
    final Map<URL,ClassLoader> classLoaders = new HashMap();
    final Map<Class, ClassLoader> classes = new HashMap();
    
    public Jartainer(String... jarURLS) throws MalformedURLException {
        this();
        for (String s : jarURLS) {
            addJar( new URL(s) );
        }
    }

    
    public Jartainer(URL... jarURLS) {
        this();
        
        for (URL u : jarURLS) {
            addJar(u);
        }
    }

    public Jartainer() {
        super(false);
        
    }
    
    
    public void addJar(URL u) {
        URLClassLoader classes = new URLClassLoader (new URL[] { u });
        classLoaders.put(u, classes);
    }
    
    public Class getClass(String className) {
        for (ClassLoader cl : classLoaders.values()) {
            Class found = null;
            try {
                found = Class.forName(className, true, cl);
            }
            catch (Exception e) { }
            if (found!=null) {
                classes.put(found, cl);
                return found;
            }
        }
        return null;
    }
    
    public Builder usable(String className) throws ClassNotFoundException {
        /*if (classes.containsKey(c))
            return super.usable(c);*/
        
        //1. check all class loaders for c
        Class found = getClass(className);
        if (found!=null) {
            super.usable(className, found); 
            return super.usable(found);                    
        }
        else
            throw new ClassNotFoundException(className);
    }

    public Builder use(String className) throws ClassNotFoundException {
        Class found = getClass(className);
        if (found!=null) {
            
            //index by key...
            super.use(className, found);
            
            //...and also by its class
            return super.use( found );
            
        }
        else
            throw new ClassNotFoundException(className);
    }
 
    public <T> T get(String c) throws ClassNotFoundException {
        Class i = getClass(c);
        if (i!=null)
            return (T)super.get(i);
        else
            throw new ClassNotFoundException(c);
    }
    
    
    public Collection<Class> getClasses() {
        List<Class> lc = new ArrayList();
        try {
            //http://stackoverflow.com/a/10261850
            Field f = ClassLoader.class.getDeclaredField("classes");
            for (ClassLoader cl : classLoaders.values()) {                
                f.setAccessible(true);
                Vector<Class> cls = (Vector<Class>) f.get(cl);        
                lc.addAll(cls);
            }
        } catch (Exception ex) {
            //Logger.getLogger(Jartainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lc;
    }
}
