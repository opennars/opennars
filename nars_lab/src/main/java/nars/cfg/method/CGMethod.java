/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.cfg.method;

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
    public InvokeInstruction ii;
    public JavaClass jc;
    public MethodGen mg;
    private Method m;

    public CGClass clazz;
    public String className;
    public String methodName;
    public List<String> argumentTypes = new ArrayList<>();
    public String returnType;
    public List<String> throwing = new ArrayList<>();
    //public final List<CGMethod> invoking = new ArrayList<>();
    //public final List<CGMethod> invokedBy = new ArrayList<>();
    public CGMethod overriding;
    private String key;



    public String key() {
        return key;
    }

    @Override
    public int hashCode() {
        return key().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return key().equals( ((CGMethod)obj).key() );
    }

    public CGMethod(JavaClass jc, MethodGen mg, InvokeInstruction ii) {
        this.jc = jc;
        this.mg = mg;
        this.ii = ii;
        pre();
    }
    public CGMethod(JavaClass jc, MethodGen mg) {
        this.jc = jc;
        this.mg = mg;
        pre();
    }
    public CGMethod(JavaClass jc, Method m) {
        this.jc = jc;
        this.m = m;
        pre();
    }

    protected void pre() {
        if (ii !=null) {
            ConstantPoolGen cpg = mg.getConstantPool();
            className = ii.getReferenceType(cpg).toString();
            methodName = ii.getMethodName(cpg);
            for (Type type : ii.getArgumentTypes(cpg)) {
                argumentTypes.add(type.toString());
            }
        }
        else if (mg !=null) {
            className = jc.getClassName();
            methodName = mg.getName();
            for (Type type : mg.getArgumentTypes()) {
                argumentTypes.add(type.toString());
            }
        }
        else if (m != null) {
            className = jc.getClassName();
            methodName = m.getName();

            for (Type type : m.getArgumentTypes()) {
                argumentTypes.add(type.toString());
            }
        }
        key = className + '#' + methodName + '(' + argumentTypes.toString() + ')';

    }
    protected void post() {

        if (ii !=null) {
            ConstantPoolGen cpg = mg.getConstantPool();
            returnType = ii.getReturnType(cpg).toString();
            for (Class c : ii.getExceptions())
                throwing.add(c.getName().toString());
        }
        else if (mg !=null) {
            returnType = mg.getReturnType().toString();
            throwing.addAll(Arrays.asList(mg.getExceptions()));
        }
        else if (m != null) {
            returnType = m.getReturnType().toString();
            throwing.addAll(Arrays.asList(m.getExceptionTable().getExceptionNames()));
        }

    }

    public static CGMethod create(JavaClass jc, Method mg) {
        CGMethod cgm = new CGMethod(jc, mg);
        return cgm;
    }

    public static CGMethod create(JavaClass jc, MethodGen mg) {
        CGMethod cgm = new CGMethod(jc, mg);
        return cgm;
    }

    public static CGMethod create(JavaClass jc, MethodGen mg, InvokeInstruction ii) {
        CGMethod cgm = new CGMethod(jc, mg, ii);
        return cgm;
    }

    public String toString() {
        return key;
    }


}
