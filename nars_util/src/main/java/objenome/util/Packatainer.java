/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.ClassPath;
import objenome.AbstractPrototainer;
import objenome.solution.dependency.Builder;
import objenome.solution.dependency.DecideImplementationClass;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Package container - contains classes from entire (cloass loader) packages - Type graph analsys
 */
public class Packatainer extends AbstractPrototainer {
    
    public final Set<Class> classes;
    private final Set<String> classNames;
    private final ConfigurationBuilder configuration;

    /** all classes are available */
//    public Packatainer(Class... classes) {
//        this(new String[] { "" }, classes);
//    }
//
    /** filters results to contain only what is in a given set of packages */
    public Packatainer(String[] packages, Class... classes) {
        this(new ConfigurationBuilder()
            .forPackages(packages)
            //.filterInputsBy(theClasses)
            //.setUrls(ClasspathHelper.forPackage("my.project.prefix"))
            .setScanners(new TypeElementsScanner().publicOnly().includeAnnotations(false).includeFields(false).includeMethods(false), new SubTypesScanner()), classes);
    }
    
    
    public Packatainer(ConfigurationBuilder cb, Class... classes) {
        super(false);
        configuration = cb;
        this.classes = new HashSet();
        
        for (Class c : classes)
            usable(c);

        classNames = this.classes.stream().map(Class::getName).collect(toSet());
    }

    @Override
    public Builder usable(Class c) {
        classes.add(c);
        return super.usable(c);
    }

    
    
    /*final Predicate<String> theClasses = new Predicate<String>() {
        @Override public boolean apply(final String t) {
            return classNames.contains(t);
        }
    };*/
    
    public SetMultimap<Class, Class> getAncestorImplementations() {        
        
        HashMultimap<Class, Class> r = HashMultimap.create();
        
        //1. get common ancestors
        //2. map each ancestor to implementations
        Set<Class> superTypes = new HashSet<>();
        for (Class c : classes) {
            superTypes.addAll(ReflectionUtils.getAllSuperTypes(c).stream().collect(Collectors.toList()));
        }
        
        Reflections s = new Reflections(configuration);
        
        for (Class c : superTypes) {
            r.putAll(c, s.getSubTypesOf(c));
        }
        
        return r;
    }

    public SetMultimap<Class, Class> includeAncestorImplementations() {        
        SetMultimap<Class, Class> r = getAncestorImplementations();
        for (Class c : r.keySet()) {
            usable(c, new DecideImplementationClass(c, new ArrayList( r.get(c) ) ));
        }
        return r;
    }
    
    public static Set<ClassPath.ClassInfo> getPackageClasses(String packege) throws Exception {
        //https://code.google.com/p/guava-libraries/wiki/ReflectionExplained#ClassPath
        ClassPath classpath = ClassPath.from(Packatainer.class.getClassLoader()); 

        return classpath.getTopLevelClasses(packege);
    }


    public Set<Class> getImplementable() {
        HashSet imp = classes.stream().filter(c -> (!Modifier.isAbstract(c.getModifiers())) && (!Modifier.isInterface(c.getModifiers()))).collect(Collectors.toCollection(HashSet::new));
        return imp;
    }

    
}
