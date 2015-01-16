/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.cfg.callgraph.model;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nmalik
 */
public class CGMethod {
    public CGClass clazz;
    public String className;
    public String methodName;
    public final List<String> argumentTypes = new ArrayList<>();
    public String returnType;
    public final List<String> throwing = new ArrayList<>();
    public final List<CGMethod> invoking = new ArrayList<>();
    public final List<CGMethod> invokedBy = new ArrayList<>();
    public CGMethod overriding;

    public String key() {
        return className + "#" + methodName + "(" + argumentTypes.toString() + ")";
    }

    public static CGMethod create(JavaClass jc, Method m) {

        CGMethod cgm = new CGMethod();
        cgm.className = jc.getClassName();
        cgm.methodName = m.getName();
        cgm.returnType = m.getReturnType().toString();
        cgm.throwing.addAll(Arrays.asList(m.getExceptionTable().getExceptionNames()));

        for (Type type : m.getArgumentTypes()) {
            cgm.argumentTypes.add(type.toString());
        }

        return cgm;
    }

    public static CGMethod create(JavaClass jc, MethodGen mg) {
        CGMethod cgm = new CGMethod();
        cgm.className = jc.getClassName();
        cgm.methodName = mg.getName();
        cgm.returnType = mg.getReturnType().toString();
        cgm.throwing.addAll(Arrays.asList(mg.getExceptions()));

        for (Type type : mg.getArgumentTypes()) {
            cgm.argumentTypes.add(type.toString());
        }
        return cgm;
    }

    public static CGMethod create(JavaClass jc, MethodGen mg, InvokeInstruction ii) {
        ConstantPoolGen cpg = mg.getConstantPool();

        CGMethod cgm = new CGMethod();
        cgm.className = ii.getReferenceType(cpg).toString();
        cgm.methodName = ii.getMethodName(cpg);
        cgm.returnType = ii.getReturnType(cpg).toString();
        for (Class c : ii.getExceptions()) {
            cgm.throwing.add(c.getName().toString());
        }

        for (Type type : ii.getArgumentTypes(cpg)) {
            cgm.argumentTypes.add(type.toString());
        }

        return cgm;
    }

    public String toString() {
        return methodName + '('+argumentTypes + ") ->"+ returnType;
    }
}
