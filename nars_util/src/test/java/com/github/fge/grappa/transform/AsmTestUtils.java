/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.transform;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckMethodAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;

public class AsmTestUtils {

    private static final Joiner NEWLINE = Joiner.on('\n');
    private static final Pattern PATTERN = Pattern.compile("\n");

    public static String getClassDump(final byte[] code) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(printWriter);
        final ClassVisitor checkClassAdapter = new ClassVisitor(Opcodes.ASM5, traceClassVisitor) {};
        //ClassAdapter checkClassAdapter = new CheckClassAdapter(traceClassVisitor);
        final ClassReader classReader;
        classReader = new ClassReader(code);
        classReader.accept(checkClassAdapter, 0);
        printWriter.flush();
        return stringWriter.toString();
    }

    public static String getMethodInstructionList(final MethodNode methodNode) {
        Preconditions.checkNotNull(methodNode, "methodNode");
        final Printer printer = new NonMaxTextifier();
        final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        methodNode.accept(traceMethodVisitor);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        printer.print(printWriter);
        printWriter.flush();
        final String[] lines = PATTERN.split(stringWriter.toString());
        int lineNr = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].startsWith("  @")) {
                lines[i] = String.format("%2d %s", lineNr++, lines[i]);
            }
        }
        return "Method '" + methodNode.name + "':\n"
            + NEWLINE.join(lines) + '\n';
    }

    public static void assertTraceDumpEquality(
        final MethodNode method, final String traceDump) throws Exception {
        Preconditions.checkNotNull(method, "method");
        final Printer printer = new NonMaxTextifier();
        final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        // MethodAdapter checkMethodAdapter = new MethodAdapter(traceMethodVisitor);
        final MethodVisitor checkMethodAdapter = new CheckMethodAdapter(traceMethodVisitor);
        method.accept(checkMethodAdapter);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        printer.print(printWriter);
        printWriter.flush();

        assertEquals(stringWriter.toString(), traceDump);
    }

    public static void verifyIntegrity(
        final String classInternalName, final byte[] classCode) {
        Preconditions.checkNotNull(classCode, "classCode");
        final ClassNode generatedClassNode = new ClassNode();
        final ClassReader classReader = new ClassReader(classCode);
        classReader.accept(generatedClassNode, 0);

        for (final Object methodObj : generatedClassNode.methods) {
            verifyMethodIntegrity(classInternalName, (MethodNode) methodObj);
        }
    }

    public static void verifyMethodIntegrity(final String ownerInternalName, final MethodNode method) {
        try {
            new Analyzer(new SimpleVerifier()).analyze(ownerInternalName, method);
        } catch (AnalyzerException e) {
            throw new RuntimeException(
                    "Integrity error in method '" + method.name + "' of type '" + ownerInternalName + "': ", e);
        }
    }

    private static class NonMaxTextifier extends Textifier {

        private NonMaxTextifier()
        {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitMaxs(final int maxStack, final int maxLocals) {
            // don't include max values
        }
    }

}
