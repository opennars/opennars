package org.opennars.derivation;

import javassist.*;

import java.util.HashMap;
import java.util.Map;

/**
 * compiles a derivation program to bytecode which can be loaded by JVM
 */
public class DerivationCompiler {
    public static Class compile(DerivationProcessor.Instr[] program) throws CannotCompileException {
        Map<String, Integer> labels = scanLabels(program);

        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("compiled"+classCounter++);

        StringBuilder body = new StringBuilder();

        body.append("for(;;) {\n");

        body.append("if( ctx.ip >= "+program.length+" ) { break; };\n");

        body.append("switch(ctx.ip) {\n");

        for(int ipIdx=0;ipIdx<program.length;ipIdx++) {
            body.append("case "+ipIdx+":\n");

            generateCodeForInstruction(body, labels, program[ipIdx]);

            body.append("break;\n");
        }

        body.append("}\n"); // switch

        body.append("}\n"); // for

        body.append("{ System.out.println(\"Interpret DBG: finished execution\"); }\n");

        body.append("{ return null; }\n");

        System.out.println(body);

        CtMethod m = CtNewMethod.make("public static org.opennars.entity.Sentence derive0("+INSTRUCTIONANDCTX_PATH+".Context ctx) {"+body+"}", cc);


        cc.addMethod(m);

        Class c = cc.toClass();
        return c;
    }

    private static Map<String, Integer> scanLabels(DerivationProcessor.Instr[] program) {
        Map<String, Integer> ipOfLabels = new HashMap<>();

        for(int ipIdx=0;ipIdx<program.length;ipIdx++) {
            DerivationProcessor.Instr iInst = program[ipIdx];
            if (iInst.mnemonic.equals("label")) {
                String labelname = iInst.arg0;
                ipOfLabels.put(labelname, ipIdx);
            }
        }

        return ipOfLabels;
    }

    /**
     *
     * @param body used for emitting code
     * @param instr instruction which has to get converted
     */
    private static void generateCodeForInstruction(StringBuilder body, Map<String, Integer> labels, DerivationProcessor.Instr instr) throws CannotCompileException {
        String mnemonic = instr.mnemonic;

        if (mnemonic.equals("label")) {
            // ignored
            body.append("ctx.ip++;\n");
        }
        else if (mnemonic.equals("jmp")) {
            body.append("ctx.ip+=" + instr.arg0Int+";\n");
            body.append("ctx.ip++;\n");
        }
        else if (mnemonic.equals("jmpTrue")) {
            String labelName = instr.arg0;

            int targetIp = labels.get(labelName);

            body.append("ctx.ip="+targetIp+";\n");
        }
        else {
            boolean forceReturn = false; // does the function force a return of the call?
            forceReturn = mnemonic.equals("m_writeConjunction") || mnemonic.equals("m_writeWindowedConjunction");

            if(forceReturn) {
                // insert call to static method which implements the function
                body.append("return "+INSTRUCTIONANDCTX_PATH+"." + mnemonic + "(ctx);\n");
            }
            else {
                // insert call to static method which implements the function
                body.append(INSTRUCTIONANDCTX_PATH+ "." + mnemonic + "(ctx);\n");
            }

            body.append("ctx.ip++;\n");
        }
    }

    private static long classCounter = 0;

    // full classpath to InstructionsAndContext
    private static final String INSTRUCTIONANDCTX_PATH = "org.opennars.derivation.InstructionsAndContext";
}
