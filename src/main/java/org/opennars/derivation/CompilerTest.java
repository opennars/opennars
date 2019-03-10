package org.opennars.derivation;

import javassist.CannotCompileException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompilerTest {
    public static void main(String[] args) throws CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class class_ = DerivationCompiler.compile();

        Method method = class_.getMethod("derive0", InstructionsAndContext.Context.class);
        method.invoke(null, new Object[]{new InstructionsAndContext.Context()});

        int debugHere = 5;
    }
}
