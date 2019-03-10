package org.opennars.derivation;

import javassist.*;

/**
 * compiles a derivation program to bytecode which can be loaded by JVM
 */
public class DerivationCompiler {
    public static Class compile(DerivationProcessor.Instr[] program) throws CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("compiled"+classCounter++);

        StringBuilder body = new StringBuilder();

        body.append("for(;;) {\n");

        body.append("if( ctx.ip >= "+program.length+" ) { break; };\n");

        body.append("switch(ctx.ip) {\n");

        for(int ipIdx=0;ipIdx<program.length;ipIdx++) {
            body.append("case "+ipIdx+":\n");

            generateCodeForInstruction(body, program[ipIdx]);

            body.append("break;\n");
        }

        body.append("}\n"); // switch

        body.append("ctx.ip++;\n"); // need to increment ip for program execution

        body.append("}\n"); // for

        body.append("{ System.out.println(\"Interpret DBG: finished execution\"); }\n");

        body.append("{ return null; }\n");

        //System.out.println(body);

        CtMethod m = CtNewMethod.make("public static org.opennars.entity.Sentence derive0("+INSTRUCTIONANDCTX_PATH+".Context ctx) {"+body+"}", cc);


        cc.addMethod(m);

        Class c = cc.toClass();
        return c;
    }

    /**
     *
     * @param body used for emitting code
     * @param instr instruction which has to get converted
     */
    private static void generateCodeForInstruction(StringBuilder body, DerivationProcessor.Instr instr) throws CannotCompileException {
        String mnemonic = instr.mnemonic;

        if (mnemonic.equals("label")) {
            // ignored
        }
        else if (mnemonic.equals("jmp")) {
            body.append("ctx.ip+=" + instr.arg0Int);
        }
        else if (mnemonic.equals("jmpTrue")) {
            // TODO TODO TODO TODO TODO< implement >
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
        }
    }

    private static long classCounter = 0;

    // full classpath to InstructionsAndContext
    private static final String INSTRUCTIONANDCTX_PATH = "org.opennars.derivation.InstructionsAndContext";
}
