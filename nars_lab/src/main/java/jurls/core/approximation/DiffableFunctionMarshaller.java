/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.util.ClassFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thorsten
 */
public class DiffableFunctionMarshaller implements ParameterizedFunction {

    private final DiffableFunctionSource fs;
    private final DiffableFunction f;
    private double minOutputDebug = Double.POSITIVE_INFINITY;
    private double maxOutputDebug = Double.NEGATIVE_INFINITY;
    private final Scalar[] inputScalars;
    private final double[] inputValues;
    private final int numInputs;
    private static int COMPILED_CLASS_INDEX = 0;
    private final boolean debug = true;

    private static final class JaninoRestrictedClassLoader extends
            SecureClassLoader {

        Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length, new ProtectionDomain(null,
                    new Permissions(), this, null));
        }
    }

    public DiffableFunctionMarshaller(
            DiffableFunctionGenerator diffableFunctionGenerator,
            int numInputs
    ) {
        GeneratorContext gc = diffableFunctionGenerator.generate(numInputs);
        fs = gc.getDiffableFunctionSource();
        int n = numInputs + gc.getParameterScalars().size();
        inputScalars = new Scalar[n];
        inputValues = new double[n];
        f = compile(fs);
        this.numInputs = numInputs;

        for (int i = 0; i < numInputs; ++i) {
            inputScalars[i] = gc.getInputScalars()[i];
            inputValues[i] = 0;
        }

        for (int i = 0; i < gc.getParameterScalars().size(); ++i) {
            inputScalars[numInputs + i] = gc.getParameterScalars().get(i);
            inputValues[numInputs + i] = gc.getParameterData().get(i);
        }
    }

    private DiffableFunction compile(DiffableFunctionSource dfs) {
        SourceEnvironment se1 = new SourceEnvironment();
        String rv1 = dfs.valueToSource(se1);
        SourceEnvironment se2 = new SourceEnvironment();
        String rv2 = dfs.partialDeriveToSource(se2);

        String classPackage = getClass().getPackage().getName() + ".compiled";
        String className = "JaninoCompiledFastexpr" + COMPILED_CLASS_INDEX++;

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(classPackage).append(";\n");
        sb.append("import ").append(DiffableFunction.class.getCanonicalName()).append(";\n");
        sb.append("public class ").append(className).append(" implements DiffableFunction {\n");
        sb.append("public double value(double[] xValues) {\n");
        sb.append(se1);
        sb.append("return ").append(rv1).append(";\n");
        sb.append("}\n");
        sb.append("public double partialDerive(double[] xValues, int parameterIndex) {\n");
        sb.append(se2);
        sb.append("return ").append(rv2).append(";\n");
        sb.append("}\n");
        sb.append("}\n");

        try {
            Scanner scanner = new Scanner(null, new ByteArrayInputStream(
                    sb.toString().getBytes("UTF-8")), "UTF-8");

            JaninoRestrictedClassLoader cl = new JaninoRestrictedClassLoader();
            UnitCompiler unitCompiler = new UnitCompiler(
                    new Parser(scanner).parseCompilationUnit(),
                    new ClassLoaderIClassLoader(cl));

            ClassFile[] classFiles = unitCompiler.compileUnit(debug, debug, debug);
            Class<?> clazz = cl.defineClass(classPackage + '.' + className,
                    classFiles[0].toByteArray());

            return (DiffableFunction) clazz.newInstance();
        } catch (CompileException | IOException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DiffableFunctionMarshaller.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }


    @Override
    public double value(double[] xs) {
        for (int i = 0; i < xs.length; ++i) {
            inputScalars[i].setValue(inputValues, xs[i]);
        }

        double y = f.value(inputValues);

        if (y > maxOutputDebug) {
            maxOutputDebug = y;
        }
        if (y < minOutputDebug) {
            minOutputDebug = y;
        }

        return y;
    }

    @Override
    public void learn(double[] xs, double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public double getParameter(int i) {
        return inputValues[i];
    }

    @Override
    public int numberOfParameters() {
        return inputValues.length - numInputs;
    }

    @Override
    public int numberOfInputs() {
        return numInputs;
    }

    @Override
    public void parameterGradient(double[] output, double...xs) {
        for (int i = 0; i < xs.length; ++i) {
            inputScalars[i].setValue(inputValues, xs[i]);
        }

        for (int i = 0; i < numberOfParameters(); ++i) {
            output[i] = f.partialDerive(inputValues, numberOfInputs() + i);
        }
    }



    @Override
    public void addToParameters(double[] deltas) {
        for (int i = 0; i < numberOfParameters(); ++i) {
            Scalar p = inputScalars[numInputs + i];

            p.setValue(inputValues, p.getValue(inputValues) + deltas[i]);
        }
    }

    @Override
    public double minOutputDebug() {
        return minOutputDebug;
    }

    @Override
    public double maxOutputDebug() {
        return maxOutputDebug;
    }
}
