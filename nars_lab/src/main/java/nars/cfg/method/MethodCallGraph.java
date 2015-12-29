/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.cfg.method;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import nars.util.data.PackageUtility;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nmalik
 */
public class MethodCallGraph extends DirectedMultigraph<CGMethod, Object> {

    private final Map<String, CGClass> cacheClass = new HashMap<>();
    private final Map<String, CGMethod> cacheMethod = new HashMap<>();
    private final boolean includeMethodCalls;

    public MethodCallGraph() {
        this(false);
    }

    public MethodCallGraph(boolean includeMethodCalls) {
        super((cgMethod, v1) -> null);
        this.includeMethodCalls = includeMethodCalls;
    }


    public MethodCallGraph addClass(String rootClass) throws ClassNotFoundException  {

        JavaClass c = Repository.lookupClass(rootClass);

        ClassVisitor visitor = new ClassVisitor(this, c);
        visitor.start();

        return this;
    }

    private String getKeyForClass(String className) {
        return className;
    }

    protected void register(CGClass cgc) {
        if (cacheClass.containsKey(cgc.key())) {
            return;
        }

        cacheClass.put(cgc.key(), cgc);
    }

    protected CGMethod register(CGMethod cgm) {
        CGMethod existing = cacheMethod.get(cgm.key());
        if (existing!=null) {
            return existing;
        }

        cgm.post();
        cgm.clazz = cacheClass.get(getKeyForClass(cgm.className));
        cacheMethod.put(cgm.key(), cgm);
        return cgm;
    }

    public void register(JavaClass jc, MethodGen mg) {
//        System.out.println("register(JavaClass, MethodGen): " + jc.getClassName() + ", " + mg.getName());

        register(CGClass.create(jc));
        register(CGMethod.create(jc, mg));
    }

    public void registerPreceding(JavaClass jc, MethodGen mg, InvokeInstruction pp, InvokeInstruction nn) {
        CGClass theClass = CGClass.create(jc, mg, pp);
        register(theClass);
        CGMethod sourceMethodCall = new CGMethodCall( CGMethod.create(jc, mg, pp), pp);
        sourceMethodCall = register(sourceMethodCall);

        CGMethod targetMethodCall = new CGMethodCall( CGMethod.create(jc, mg, nn), nn);
        targetMethodCall = register(targetMethodCall);

        if (includeMethodCalls) {
            addVertex(sourceMethodCall);
            addVertex(targetMethodCall);
            addEdge(sourceMethodCall, targetMethodCall, jc.getClassName() + pp + "pre" + nn);
        }

    }

    public void register(JavaClass jc, MethodGen mg, CGMethodCall methodCall, InvokeInstruction ii) {
        CGClass callerClass = CGClass.create(jc);
        register(callerClass);
        CGMethod callingMethod = register(methodCall.method);
        methodCall = (CGMethodCall) register(methodCall);
        callerClass.methods.add(methodCall.method);


        CGClass targetClass = CGClass.create(jc, mg, ii);
        register(targetClass);
        CGMethod targetMethod = CGMethod.create(jc, mg, ii);
        targetMethod = register(targetMethod);
        targetClass.methods.add(targetMethod);

        if (includeMethodCalls) {
            addVertex(methodCall);
            addVertex(targetMethod);
            addEdge(methodCall, targetMethod, ii.toString());

            addVertex(callingMethod);
            addEdge(callingMethod, methodCall, callingMethod.key() + '@' + ii);
        }
        else {
            addVertex(callingMethod);
            if (!callingMethod.equals(targetMethod)) {
                addVertex(targetMethod);
                addEdge(callingMethod, targetMethod, methodCall.key() + '@' + ii);
            }
        }
    }

    public void register(JavaClass jc, MethodGen mg, InvokeInstruction ii) {
//        System.out.println("register(JavaClass, MethodGen, InvokeInstruction): " + jc.getClassName() + ", " + mg.getName());
        // register caller class and method
        CGClass callerClass = CGClass.create(jc);
        register(callerClass);
        CGMethod callerMethod = CGMethod.create(jc, mg);
        register(callerMethod);
        callerClass.methods.add(callerMethod);

        // register target class and method
        CGClass targetClass = CGClass.create(jc, mg, ii);
        register(targetClass);
        CGMethod targetMethod = CGMethod.create(jc, mg, ii);
        register(targetMethod);
        targetClass.methods.add(targetMethod);

        addVertex(callerMethod);

        if (!callerMethod.equals(targetMethod)) {
            addVertex(targetMethod);

            //between methods directly
            addEdge(callerMethod, targetMethod, ii);
        }

    }

    public void register(JavaClass jc, Method m) {
//        System.out.println("register(JavaClass, Method): " + jc.getClassName() + ", " + m.getName());
        register(CGClass.create(jc));
        register(CGMethod.create(jc, m));
    }

    public List<CGClass> getClasses(String rootRegex) {
        List<CGClass> output = new ArrayList<>(cacheClass.values().stream().filter(root -> root.className.matches(rootRegex)).collect(Collectors.toList()));

        // find root classes
        CGClass current;

        return output;
    }

    public Iterable<CGMethod> getEntryPoints() {
        return Iterables.filter( vertexSet(), new EntryMethodFilter());
    }

    public Iterable<CGMethod> getExitPoints() {
        return Iterables.filter( vertexSet(), new ExitMethodFilter());
    }

    public CGMethod method(String s) {
        return cacheMethod.get(s);
    }

    public boolean isInstructionLevel() {
        return true;
    }

    public MethodCallGraph addClasses(List<Class> classes) throws ClassNotFoundException {
        for (Class c : classes) {
            System.out.println("CLASS " + c);
            addClass(c.getName());
        }
        return this;
    }

    public MethodCallGraph addClasses(String pkg, boolean inner) throws ClassNotFoundException {
        return addClasses(PackageUtility.getClasses(pkg, inner));
    }


    private class EntryMethodFilter implements Predicate<CGMethod> {
        @Override public boolean apply(CGMethod m) {
            return inDegreeOf(m) == 0;
        }
    }
    private class ExitMethodFilter implements Predicate<CGMethod> {
        @Override public boolean apply(CGMethod m) {
            return outDegreeOf(m) == 0;
        }
    }
}
