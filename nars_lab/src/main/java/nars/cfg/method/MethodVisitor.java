/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nars.cfg.method;


import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * The simplest of method visitors, prints any invoked method signature for all method invocations.
 *
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

    private final JavaClass visitedClass;
    private final MethodGen mg;
    private final MethodCallGraph graph;
    private final ArrayDeque<Object> stack = new ArrayDeque();

    public MethodVisitor(MethodCallGraph graph, MethodGen m, JavaClass jc) {
        this.graph = graph;
        visitedClass = jc;
        mg = m;
    }

    public void start() {
        if (mg.isAbstract() || mg.isNative()) {
            return;
        }
        stack.clear();
        for (InstructionHandle ih = mg.getInstructionList().getStart();
             ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();

            if (stack.isEmpty())
                stack.addLast(ih.getInstruction());

            //if (!visitInstruction(i)) {
                i.accept(this);
            //}
        }
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return ((InstructionConstants.INSTRUCTIONS[opcode] != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    /*
    @Override
    public void visitBranchInstruction(BranchInstruction obj) {
        super.visitBranchInstruction(obj);
    }
    */

    @Override
    public void visitStackProducer(StackProducer obj) {
        if (obj instanceof Instruction)
            stack.addLast(obj);
    }

    @Override
    public void visitStackConsumer(StackConsumer obj) {
        if (obj instanceof Instruction)
            stack.removeLast();
    }



    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        call(visitedClass, mg, i);
    }


//    @Override
//    public void visitRET(RET obj) {
//        super.visitRET(obj);
//        stack.removeLast();
//    }


    public InvokeInstruction getLastInvocation(Instruction p) {
        Iterator<Object> i = stack.descendingIterator();
        while (i.hasNext()) {
            Object n = i.next();
            if ((n instanceof InvokeInstruction) && (!n.equals(p)))
                return ((InvokeInstruction)n);
        }
        return null;
    }

    private void call(JavaClass cl, MethodGen mg, InvokeInstruction i) {
        if (!graph.isInstructionLevel()) {
            graph.register(cl, mg, i);
        }
        else {
            CGMethodCall target = new CGMethodCall(new CGMethod(cl, mg), i);

            InvokeInstruction p = getLastInvocation(i);
            if ((p!=null) && (!p.equals(i)))
                graph.registerPreceding(cl, mg, p, i);
            else
                graph.register(cl, mg, i);

            graph.register(cl, mg, target, i);
        }
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        graph.register(visitedClass, mg, i);
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        graph.register(visitedClass, mg, i);
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        graph.register(visitedClass, mg, i);
    }
}
