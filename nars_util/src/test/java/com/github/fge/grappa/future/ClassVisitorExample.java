package com.github.fge.grappa.future;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassVisitorExample
    extends ClassVisitor
{
    private String className;

    public ClassVisitorExample(final ClassVisitor cv)
    {
        super(Opcodes.ASM5, cv);
    }

    public static void findAreturnBlocks(final Path path)
        throws Exception
    {
        final ClassReader cr = new ClassReader(Files.readAllBytes(path));
        cr.accept(new ClassVisitorExample(null), ClassReader.EXPAND_FRAMES);
    }

    @Override
    public void visit(final int version, final int access, final String name,
        final String signature, final String superName,
        final String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
        final String desc, final String signature, final String[] exceptions)
    {
        // Unused?
        /*
        final MethodVisitor mv = super.visitMethod(access, name, desc,
            signature, exceptions);
        */

        return new MethodNode(Opcodes.ASM5, access, name, desc, signature,
            exceptions)
        {
            @Override
            public void visitEnd()
            {
                super.visitEnd();

                try {
                    final BasicInterpreter basicInterpreter
                        = new BasicInterpreter();
                    final Analyzer<BasicValue> analyzer
                        = new Analyzer<>(basicInterpreter);
                    final AbstractInsnNode[] nodes = instructions.toArray();
                    final Frame<BasicValue>[] frames
                        = analyzer.analyze(className, this);
                    int areturn = -1;
                    for (int i = nodes.length -1; i >= 0; i--)
                    {
                        if (nodes[i].getOpcode() == Opcodes.ARETURN) {
                            areturn = i;
                            System.out.println(className + "." + name + desc);
                            System.out.println("Found areturn at: " + i);
                        } else if (areturn != -1
                            && nodes[i].getOpcode() != -1
                            && frames[i].getStackSize() == 0) {
                            System.out.println("Found start of block at: " + i);

                            final InsnList list = new InsnList();
                            for (int j = i; j <= areturn; j++)
                                list.add(nodes[j]);
                            final Textifier textifier = new Textifier();
                            final PrintWriter pw = new PrintWriter(System.out);
                            list.accept(new TraceMethodVisitor(textifier));
                            textifier.print(pw);
                            pw.flush();
                            System.out.println("\n\n");
                            areturn = -1;
                        }
                    }
                }
                catch (AnalyzerException e) {
                    e.printStackTrace();
                }

                if (mv != null)
                    accept(mv);
            }
        };
    }
}
