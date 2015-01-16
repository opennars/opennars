/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.cfg.callgraph;

import nars.cfg.callgraph.model.CGClass;
import nars.cfg.callgraph.model.CGMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nmalik
 */
public class MethodCallGraph extends DirectedMultigraph<CGMethod, Instruction> {

    private final Map<String, CGClass> cacheClass = new HashMap<>();
    private final Map<String, CGMethod> cacheMethod = new HashMap<>();

    public MethodCallGraph() {
        super(new EdgeFactory<CGMethod, Instruction>() {
            @Override
            public Instruction createEdge(CGMethod cgMethod, CGMethod v1) {
                return null;
            }
        });

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

    protected void register(CGMethod cgm) {
        if (cacheMethod.containsKey(cgm.key())) {
            return;
        }

        cgm.clazz = cacheClass.get(getKeyForClass(cgm.className));
        cacheMethod.put(cgm.key(), cgm);
    }

    public void register(JavaClass jc, MethodGen mg) {
//        System.out.println("register(JavaClass, MethodGen): " + jc.getClassName() + ", " + mg.getName());

        register(CGClass.create(jc));
        register(CGMethod.create(jc, mg));
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
        addVertex(targetMethod);
        addEdge(callerMethod, targetMethod, ii);

        // add caller / target references
        callerMethod.invoking.add(targetMethod);
        targetMethod.invokedBy.add(callerMethod);

//        System.out.println("class key = " + callerClass.key());
//        System.out.println("caller key = " + callerMethod.key());
//        System.out.println("target key = " + targetMethod.key());
    }

    public void register(JavaClass jc, Method m) {
//        System.out.println("register(JavaClass, Method): " + jc.getClassName() + ", " + m.getName());
        register(CGClass.create(jc));
        register(CGMethod.create(jc, m));
    }

    public List<CGClass> getClasses(String rootRegex) {
        List<CGClass> output = new ArrayList<>();

        // find root classes
        CGClass current;
        for (CGClass root : cacheClass.values()) {
            if (root.className.matches(rootRegex)) {
                output.add(root);
            }
        }

        return output;
    }
}
