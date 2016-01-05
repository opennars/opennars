/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import objenome.util.bytecode.SgClass;
import objenome.util.bytecode.SgClassPool;
import objenome.util.bytecode.SgMethod;

import java.lang.reflect.Modifier;

/**
 * Uses another class's static method (with same return type) to implement an abstract or interface method
 * TODO use something other than javassist, like bytebuddy or procyon?
 */
public enum SetAbstractMethodWithExistingStaticMethod {
    ;

    //TODO harvest static methods from certain classes like Math. , Double., Character, etc..

    public static void main(String[] args) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get("java.lang.Math");
        SgClass ss = SgClass.create(new SgClassPool(), "java.lang.Math");


        for (SgMethod xs : ss.getMethods()) {
            //System.out.println(ss.toString());
        }

        for (CtMethod em : cc.getMethods()) {
            //Method em = m.getExistingMethod();
            //TODO allow referencing the particular method in case it's overloaded with different param signatures

            SgMethod sm = ss.findMethod(em);
            if (sm == null) {
                continue;
            }



            int mod = em.getModifiers();
            if (!Modifier.isStatic( mod ) )
                continue;

            System.out.println(sm);
            System.out.println(em.getMethodInfo());
            //System.out.println(sm.getBody());


        }
    }
}
