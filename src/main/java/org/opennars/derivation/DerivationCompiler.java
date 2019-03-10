package org.opennars.derivation;

import javassist.*;

import java.util.Random;

/**
 * compiles a derivation program to bytecode which can be loaded by JVM
 */
public class DerivationCompiler {
    public static Class compile() throws CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("compiled"+classCounter++);

        CtMethod m = CtNewMethod.make("public static void derive0(org.opennars.derivation.InstructionsAndContext.Context ctx) { System.out.println(\"test ok\"); }", cc);

        m.insertAfter("{ System.out.println(\"Here!\"); }");

        cc.addMethod(m);

        Class c = cc.toClass();
        return c;
    }

    private static long classCounter = 0;
}
