/*
 * Copyright (C) 2015 Francis Galiegue <fgaliegue@gmail.com>
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

import me.qmx.jitescript.VisibleAnnotation;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static me.qmx.jitescript.util.CodegenUtils.*;

public final class CodeBlock
    implements Opcodes
{
    private final InsnList instructionList = new InsnList();
    private final List<TryCatchBlockNode> tryCatchBlockList = new ArrayList<>();
    private final List<LocalVariableNode> localVariableList = new ArrayList<>();
    private final List<VisibleAnnotation> annotations = new ArrayList<>();
    private int arity = 0;
    private boolean returns = false;

    public CodeBlock()
    {
    }

    public CodeBlock(CodeBlock block)
    {
        arity = block.arity();
        prepend(block);
    }

    public CodeBlock(int arity)
    {
        this.arity = arity;
    }

    public static CodeBlock newCodeBlock()
    {
        return new CodeBlock();
    }

    public static CodeBlock newCodeBlock(int arity)
    {
        return new CodeBlock(arity);
    }

    public static CodeBlock newCodeBlock(CodeBlock block)
    {
        return new CodeBlock(block);
    }

    /**
     * Short-hand for specifying a set of aloads
     *
     * @param indices list of aloads you want
     */
    public CodeBlock aloadMany(int... indices)
    {
        for (int index: indices)
            aload(index);

        return this;
    }

    public CodeBlock aload(int index)
    {
        instructionList.add(new VarInsnNode(ALOAD, index));
        return this;
    }

    public CodeBlock iload(int index)
    {
        instructionList.add(new VarInsnNode(ILOAD, index));
        return this;
    }

    public CodeBlock lload(int index)
    {
        instructionList.add(new VarInsnNode(LLOAD, index));
        return this;
    }

    public CodeBlock fload(int index)
    {
        instructionList.add(new VarInsnNode(FLOAD, index));
        return this;
    }

    public CodeBlock dload(int index)
    {
        instructionList.add(new VarInsnNode(DLOAD, index));
        return this;
    }

    public CodeBlock astore(int index)
    {
        instructionList.add(new VarInsnNode(ASTORE, index));
        return this;
    }

    public CodeBlock istore(int index)
    {
        instructionList.add(new VarInsnNode(ISTORE, index));
        return this;
    }

    public CodeBlock lstore(int index)
    {
        instructionList.add(new VarInsnNode(LSTORE, index));
        return this;
    }

    public CodeBlock fstore(int index)
    {
        instructionList.add(new VarInsnNode(FSTORE, index));
        return this;
    }

    public CodeBlock dstore(int index)
    {
        instructionList.add(new VarInsnNode(DSTORE, index));
        return this;
    }

    public CodeBlock ldc(Object value)
    {
        instructionList.add(new LdcInsnNode(value));
        return this;
    }

    public CodeBlock bipush(int byteValue)
    {
        instructionList.add(new IntInsnNode(BIPUSH, byteValue));
        return this;
    }

    public CodeBlock sipush(int shortValue)
    {
        instructionList.add(new IntInsnNode(SIPUSH, shortValue));
        return this;
    }

    public CodeBlock pushInt(int value)
    {
        if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
            switch (value) {
                case -1:
                    iconst_m1();
                    break;
                case 0:
                    iconst_0();
                    break;
                case 1:
                    iconst_1();
                    break;
                case 2:
                    iconst_2();
                    break;
                case 3:
                    iconst_3();
                    break;
                case 4:
                    iconst_4();
                    break;
                case 5:
                    iconst_5();
                    break;
                default:
                    bipush(value);
                    break;
            }
        } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
            sipush(value);
        } else {
            ldc(value);
        }
        return this;
    }

    public CodeBlock pushBoolean(boolean bool)
    {
        if (bool)
            iconst_1();
        else
            iconst_0();

        return this;
    }

    public CodeBlock invokestatic(String className,
                                  String methodName, String methodDesc)
    {
        instructionList.add(new MethodInsnNode(INVOKESTATIC, className,
            methodName, methodDesc, false));
        return this;
    }

    public CodeBlock invokespecial(String className,
                                   String methodName, String methodDesc)
    {
        instructionList.add(new MethodInsnNode(INVOKESPECIAL, className,
            methodName, methodDesc, false));
        return this;
    }

    public CodeBlock invokevirtual(String className,
                                   String methodName, String methodDesc)
    {
        instructionList.add(new MethodInsnNode(INVOKEVIRTUAL, className,
            methodName, methodDesc, false));
        return this;
    }

    public CodeBlock invokeinterface(String className,
                                     String methodName, String methodDesc)
    {
        instructionList.add(new MethodInsnNode(INVOKEINTERFACE, className,
            methodName, methodDesc, true));
        return this;
    }

    public CodeBlock invokedynamic(String name, String descriptor,
                                   Handle bootstrapMethod, Object... bootstrapArguments)
    {
        instructionList.add(new InvokeDynamicInsnNode(name, descriptor,
            bootstrapMethod, bootstrapArguments));
        return this;
    }

    public CodeBlock aprintln()
    {
        dup();
        getstatic(p(System.class), "out", ci(PrintStream.class));
        swap();
        invokevirtual(p(PrintStream.class), "println", sig(void.class, params(
            Object.class)));
        return this;
    }

    public CodeBlock iprintln()
    {
        dup();
        getstatic(p(System.class), "out", ci(PrintStream.class));
        swap();
        invokevirtual(p(PrintStream.class), "println", sig(void.class, params(
            int.class)));
        return this;
    }

    public CodeBlock rawReturn()
    {
        returns = true;
        instructionList.add(new InsnNode(RETURN));
        return this;
    }

    public CodeBlock areturn()
    {
        returns = true;
        instructionList.add(new InsnNode(ARETURN));
        return this;
    }

    public CodeBlock ireturn()
    {
        instructionList.add(new InsnNode(IRETURN));
        return this;
    }

    public CodeBlock freturn()
    {
        instructionList.add(new InsnNode(FRETURN));
        return this;
    }

    public CodeBlock lreturn()
    {
        instructionList.add(new InsnNode(LRETURN));
        return this;
    }

    public CodeBlock dreturn()
    {
        instructionList.add(new InsnNode(DRETURN));
        return this;
    }

    public CodeBlock newobj(String desc)
    {
        instructionList.add(new TypeInsnNode(NEW, desc));
        return this;
    }

    public CodeBlock dup()
    {
        instructionList.add(new InsnNode(DUP));
        return this;
    }

    public CodeBlock swap()
    {
        instructionList.add(new InsnNode(SWAP));
        return this;
    }

    public CodeBlock swap2()
    {
        dup2_x2();
        pop2();
        return this;
    }

    public CodeBlock getstatic(String className, String fieldName,
                               String fieldDesc)
    {
        instructionList.add(new FieldInsnNode(GETSTATIC, className, fieldName,
            fieldDesc));
        return this;
    }

    public CodeBlock putstatic(String className, String fieldName,
                               String fieldDesc)
    {
        instructionList.add(new FieldInsnNode(PUTSTATIC, className, fieldName,
            fieldDesc));
        return this;
    }

    public CodeBlock getfield(String className, String fieldName,
                              String fieldDesc)
    {
        instructionList.add(new FieldInsnNode(GETFIELD, className, fieldName,
            fieldDesc));
        return this;
    }

    public CodeBlock putfield(String className, String fieldName,
                              String fieldDesc)
    {
        instructionList.add(new FieldInsnNode(PUTFIELD, className, fieldName,
            fieldDesc));
        return this;
    }

    public CodeBlock voidreturn()
    {
        instructionList.add(new InsnNode(RETURN));
        return this;
    }

    public CodeBlock anewarray(String arrayDesc)
    {
        instructionList.add(new TypeInsnNode(ANEWARRAY, arrayDesc));
        return this;
    }

    public CodeBlock multianewarray(String arrayDesc, int dims)
    {
        instructionList.add(new MultiANewArrayInsnNode(arrayDesc, dims));
        return this;
    }

    public CodeBlock newarray(int size)
    {
        instructionList.add(new IntInsnNode(NEWARRAY, size));
        return this;
    }

    public CodeBlock iconst_m1()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_M1));
        return this;
    }

    public CodeBlock iconst_0()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_0));
        return this;
    }

    public CodeBlock iconst_1()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_1));
        return this;
    }

    public CodeBlock iconst_2()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_2));
        return this;
    }

    public CodeBlock iconst_3()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_3));
        return this;
    }

    public CodeBlock iconst_4()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_4));
        return this;
    }

    public CodeBlock iconst_5()
    {
        instructionList.add(new InsnNode(Opcodes.ICONST_5));
        return this;
    }

    public CodeBlock lconst_0()
    {
        instructionList.add(new InsnNode(Opcodes.LCONST_0));
        return this;
    }

    public CodeBlock aconst_null()
    {
        instructionList.add(new InsnNode(Opcodes.ACONST_NULL));
        return this;
    }

    public CodeBlock label(LabelNode labelNode)
    {
        instructionList.add(labelNode);
        return this;
    }

    public CodeBlock nop()
    {
        instructionList.add(new InsnNode(Opcodes.NOP));
        return this;
    }

    public CodeBlock pop()
    {
        instructionList.add(new InsnNode(POP));
        return this;
    }

    public CodeBlock pop2()
    {
        instructionList.add(new InsnNode(POP2));
        return this;
    }

    public CodeBlock arrayload()
    {
        instructionList.add(new InsnNode(AALOAD));
        return this;
    }

    public CodeBlock arraystore()
    {
        instructionList.add(new InsnNode(AASTORE));
        return this;
    }

    public CodeBlock iarrayload()
    {
        instructionList.add(new InsnNode(IALOAD));
        return this;
    }

    public CodeBlock barrayload()
    {
        instructionList.add(new InsnNode(BALOAD));
        return this;
    }

    public CodeBlock barraystore()
    {
        instructionList.add(new InsnNode(BASTORE));
        return this;
    }

    public CodeBlock aaload()
    {
        instructionList.add(new InsnNode(AALOAD));
        return this;
    }

    public CodeBlock aastore()
    {
        instructionList.add(new InsnNode(AASTORE));
        return this;
    }

    public CodeBlock iaload()
    {
        instructionList.add(new InsnNode(IALOAD));
        return this;
    }

    public CodeBlock iastore()
    {
        instructionList.add(new InsnNode(IASTORE));
        return this;
    }

    public CodeBlock laload()
    {
        instructionList.add(new InsnNode(LALOAD));
        return this;
    }

    public CodeBlock lastore()
    {
        instructionList.add(new InsnNode(LASTORE));
        return this;
    }

    public CodeBlock baload()
    {
        instructionList.add(new InsnNode(BALOAD));
        return this;
    }

    public CodeBlock bastore()
    {
        instructionList.add(new InsnNode(BASTORE));
        return this;
    }

    public CodeBlock saload()
    {
        instructionList.add(new InsnNode(SALOAD));
        return this;
    }

    public CodeBlock sastore()
    {
        instructionList.add(new InsnNode(SASTORE));
        return this;
    }

    public CodeBlock caload()
    {
        instructionList.add(new InsnNode(CALOAD));
        return this;
    }

    public CodeBlock castore()
    {
        instructionList.add(new InsnNode(CASTORE));
        return this;
    }

    public CodeBlock faload()
    {
        instructionList.add(new InsnNode(FALOAD));
        return this;
    }

    public CodeBlock fastore()
    {
        instructionList.add(new InsnNode(FASTORE));
        return this;
    }

    public CodeBlock daload()
    {
        instructionList.add(new InsnNode(DALOAD));
        return this;
    }

    public CodeBlock dastore()
    {
        instructionList.add(new InsnNode(DASTORE));
        return this;
    }

    public CodeBlock fcmpl()
    {
        instructionList.add(new InsnNode(FCMPL));
        return this;
    }

    public CodeBlock fcmpg()
    {
        instructionList.add(new InsnNode(FCMPG));
        return this;
    }

    public CodeBlock dcmpl()
    {
        instructionList.add(new InsnNode(DCMPL));
        return this;
    }

    public CodeBlock dcmpg()
    {
        instructionList.add(new InsnNode(DCMPG));
        return this;
    }

    public CodeBlock dup_x2()
    {
        instructionList.add(new InsnNode(DUP_X2));
        return this;
    }

    public CodeBlock dup_x1()
    {
        instructionList.add(new InsnNode(DUP_X1));
        return this;
    }

    public CodeBlock dup2_x2()
    {
        instructionList.add(new InsnNode(DUP2_X2));
        return this;
    }

    public CodeBlock dup2_x1()
    {
        instructionList.add(new InsnNode(DUP2_X1));
        return this;
    }

    public CodeBlock dup2()
    {
        instructionList.add(new InsnNode(DUP2));
        return this;
    }

    public CodeBlock trycatch(LabelNode scopeStart,
                              LabelNode scopeEnd, LabelNode handler,
                              @Nullable String exceptionType)
    {
        tryCatchBlockList.add(new TryCatchBlockNode(scopeStart, scopeEnd,
            handler, exceptionType));
        return this;
    }

    public CodeBlock trycatch(String exceptionType, Runnable body,
                              Runnable catchBody)
    {
        LabelNode before = new LabelNode();
        LabelNode after = new LabelNode();
        LabelNode catchStart = new LabelNode();
        LabelNode done = new LabelNode();

        trycatch(before, after, catchStart, exceptionType);
        label(before);
        body.run();
        label(after);
        go_to(done);
        if (catchBody != null) {
            label(catchStart);
            catchBody.run();
        }
        label(done);
        return this;
    }

    public CodeBlock go_to(LabelNode label)
    {
        instructionList.add(new JumpInsnNode(GOTO, label));
        return this;
    }

    public CodeBlock lookupswitch(LabelNode defaultHandler,
                                  int[] keys,
                                  LabelNode[] handlers)
    {
        instructionList.add(new LookupSwitchInsnNode(defaultHandler, keys,
            handlers));
        return this;
    }

    public CodeBlock athrow()
    {
        instructionList.add(new InsnNode(ATHROW));
        return this;
    }

    public CodeBlock instance_of(String typeDesc)
    {
        instructionList.add(new TypeInsnNode(INSTANCEOF, typeDesc));
        return this;
    }

    public CodeBlock ifeq(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFEQ, jumpLabel));
        return this;
    }

    public CodeBlock iffalse(LabelNode jumpLabel)
    {
        ifeq(jumpLabel);
        return this;
    }

    public CodeBlock ifne(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFNE, jumpLabel));
        return this;
    }

    public CodeBlock iftrue(LabelNode jumpLabel)
    {
        ifne(jumpLabel);
        return this;
    }

    public CodeBlock if_acmpne(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ACMPNE, jumpLabel));
        return this;
    }

    public CodeBlock if_acmpeq(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ACMPEQ, jumpLabel));
        return this;
    }

    public CodeBlock if_icmple(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPLE, jumpLabel));
        return this;
    }

    public CodeBlock if_icmpgt(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPGT, jumpLabel));
        return this;
    }

    public CodeBlock if_icmplt(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPLT, jumpLabel));
        return this;
    }

    public CodeBlock if_icmpne(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPNE, jumpLabel));
        return this;
    }

    public CodeBlock if_icmpeq(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPEQ, jumpLabel));
        return this;
    }

    public CodeBlock if_icmpge(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IF_ICMPGE, jumpLabel));
        return this;
    }

    public CodeBlock checkcast(String typeDesc)
    {
        instructionList.add(new TypeInsnNode(CHECKCAST, typeDesc));
        return this;
    }

    public CodeBlock line(int line)
    {
        visitLineNumber(line, new LabelNode());
        return this;
    }

    public CodeBlock line(int line, LabelNode label)
    {
        visitLineNumber(line, label);
        return this;
    }

    public CodeBlock ifnonnull(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFNONNULL, jumpLabel));
        return this;
    }

    public CodeBlock ifnull(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFNULL, jumpLabel));
        return this;
    }

    public CodeBlock iflt(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFLT, jumpLabel));
        return this;
    }

    public CodeBlock ifle(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFLE, jumpLabel));
        return this;
    }

    public CodeBlock ifgt(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFGT, jumpLabel));
        return this;
    }

    public CodeBlock ifge(LabelNode jumpLabel)
    {
        instructionList.add(new JumpInsnNode(IFGE, jumpLabel));
        return this;
    }

    public CodeBlock arraylength()
    {
        instructionList.add(new InsnNode(ARRAYLENGTH));
        return this;
    }

    public CodeBlock ishr()
    {
        instructionList.add(new InsnNode(ISHR));
        return this;
    }

    public CodeBlock ishl()
    {
        instructionList.add(new InsnNode(ISHL));
        return this;
    }

    public CodeBlock iushr()
    {
        instructionList.add(new InsnNode(IUSHR));
        return this;
    }

    public CodeBlock lshr()
    {
        instructionList.add(new InsnNode(LSHR));
        return this;
    }

    public CodeBlock lshl()
    {
        instructionList.add(new InsnNode(LSHL));
        return this;
    }

    public CodeBlock lushr()
    {
        instructionList.add(new InsnNode(LUSHR));
        return this;
    }

    public CodeBlock lcmp()
    {
        instructionList.add(new InsnNode(LCMP));
        return this;
    }

    public CodeBlock iand()
    {
        instructionList.add(new InsnNode(IAND));
        return this;
    }

    public CodeBlock ior()
    {
        instructionList.add(new InsnNode(IOR));
        return this;
    }

    public CodeBlock ixor()
    {
        instructionList.add(new InsnNode(IXOR));
        return this;
    }

    public CodeBlock land()
    {
        instructionList.add(new InsnNode(LAND));
        return this;
    }

    public CodeBlock lor()
    {
        instructionList.add(new InsnNode(LOR));
        return this;
    }

    public CodeBlock lxor()
    {
        instructionList.add(new InsnNode(LXOR));
        return this;
    }

    public CodeBlock iadd()
    {
        instructionList.add(new InsnNode(IADD));
        return this;
    }

    public CodeBlock ladd()
    {
        instructionList.add(new InsnNode(LADD));
        return this;
    }

    public CodeBlock fadd()
    {
        instructionList.add(new InsnNode(FADD));
        return this;
    }

    public CodeBlock dadd()
    {
        instructionList.add(new InsnNode(DADD));
        return this;
    }

    public CodeBlock isub()
    {
        instructionList.add(new InsnNode(ISUB));
        return this;
    }

    public CodeBlock lsub()
    {
        instructionList.add(new InsnNode(LSUB));
        return this;
    }

    public CodeBlock fsub()
    {
        instructionList.add(new InsnNode(FSUB));
        return this;
    }

    public CodeBlock dsub()
    {
        instructionList.add(new InsnNode(DSUB));
        return this;
    }

    public CodeBlock idiv()
    {
        instructionList.add(new InsnNode(IDIV));
        return this;
    }

    public CodeBlock irem()
    {
        instructionList.add(new InsnNode(IREM));
        return this;
    }

    public CodeBlock ineg()
    {
        instructionList.add(new InsnNode(INEG));
        return this;
    }

    public CodeBlock i2d()
    {
        instructionList.add(new InsnNode(I2D));
        return this;
    }

    public CodeBlock i2l()
    {
        instructionList.add(new InsnNode(I2L));
        return this;
    }

    public CodeBlock i2f()
    {
        instructionList.add(new InsnNode(I2F));
        return this;
    }

    public CodeBlock i2s()
    {
        instructionList.add(new InsnNode(I2S));
        return this;
    }

    public CodeBlock i2c()
    {
        instructionList.add(new InsnNode(I2C));
        return this;
    }

    public CodeBlock i2b()
    {
        instructionList.add(new InsnNode(I2B));
        return this;
    }

    public CodeBlock ldiv()
    {
        instructionList.add(new InsnNode(LDIV));
        return this;
    }

    public CodeBlock lrem()
    {
        instructionList.add(new InsnNode(LREM));
        return this;
    }

    public CodeBlock lneg()
    {
        instructionList.add(new InsnNode(LNEG));
        return this;
    }

    public CodeBlock l2d()
    {
        instructionList.add(new InsnNode(L2D));
        return this;
    }

    public CodeBlock l2i()
    {
        instructionList.add(new InsnNode(L2I));
        return this;
    }

    public CodeBlock l2f()
    {
        instructionList.add(new InsnNode(L2F));
        return this;
    }

    public CodeBlock fdiv()
    {
        instructionList.add(new InsnNode(FDIV));
        return this;
    }

    public CodeBlock frem()
    {
        instructionList.add(new InsnNode(FREM));
        return this;
    }

    public CodeBlock fneg()
    {
        instructionList.add(new InsnNode(FNEG));
        return this;
    }

    public CodeBlock f2d()
    {
        instructionList.add(new InsnNode(F2D));
        return this;
    }

    public CodeBlock f2i()
    {
        instructionList.add(new InsnNode(F2D));
        return this;
    }

    public CodeBlock f2l()
    {
        instructionList.add(new InsnNode(F2L));
        return this;
    }

    public CodeBlock ddiv()
    {
        instructionList.add(new InsnNode(DDIV));
        return this;
    }

    public CodeBlock drem()
    {
        instructionList.add(new InsnNode(DREM));
        return this;
    }

    public CodeBlock dneg()
    {
        instructionList.add(new InsnNode(DNEG));
        return this;
    }

    public CodeBlock d2f()
    {
        instructionList.add(new InsnNode(D2F));
        return this;
    }

    public CodeBlock d2i()
    {
        instructionList.add(new InsnNode(D2I));
        return this;
    }

    public CodeBlock d2l()
    {
        instructionList.add(new InsnNode(D2L));
        return this;
    }

    public CodeBlock imul()
    {
        instructionList.add(new InsnNode(IMUL));
        return this;
    }

    public CodeBlock lmul()
    {
        instructionList.add(new InsnNode(LMUL));
        return this;
    }

    public CodeBlock fmul()
    {
        instructionList.add(new InsnNode(FMUL));
        return this;
    }

    public CodeBlock dmul()
    {
        instructionList.add(new InsnNode(DMUL));
        return this;
    }

    public CodeBlock iinc(int varIndex, int increment)
    {
        instructionList.add(new IincInsnNode(varIndex, increment));
        return this;
    }

    public CodeBlock monitorenter()
    {
        instructionList.add(new InsnNode(MONITORENTER));
        return this;
    }

    public CodeBlock monitorexit()
    {
        instructionList.add(new InsnNode(MONITOREXIT));
        return this;
    }

    public CodeBlock jsr(LabelNode branch)
    {
        instructionList.add(new JumpInsnNode(JSR, branch));
        return this;
    }

    public CodeBlock ret(int value)
    {
        instructionList.add(new IntInsnNode(RET, value));
        return this;
    }

    public CodeBlock visitInsn(int opcode)
    {
        instructionList.add(new InsnNode(opcode));
        return this;
    }

    public CodeBlock visitIntInsn(int opcode, int operand)
    {
        instructionList.add(new IntInsnNode(opcode, operand));
        return this;
    }

    public CodeBlock visitInsnNode(int opcode, int operand)
    {
        instructionList.add(new IntInsnNode(opcode, operand));
        return this;
    }

    public CodeBlock visitTypeInsn(int opcode, String desc)
    {
        instructionList.add(new TypeInsnNode(opcode, desc));
        return this;
    }

    public CodeBlock visitFieldInsn(int opcode, String className,
                                    String fieldName, String fieldDesc)
    {
        instructionList.add(new FieldInsnNode(opcode, className, fieldName,
            fieldDesc));
        return this;
    }

    public CodeBlock visitMethodInsn(int opcode, String className,
                                     String methodName, String methodDesc)
    {
        boolean intf = opcode == INVOKEINTERFACE;
        instructionList.add(new MethodInsnNode(opcode, className, methodName,
            methodDesc, intf));
        return this;
    }

    public CodeBlock visitInvokeDynamicInsn(String name,
                                            String desc, Handle bootstrapMethod,
                                            Object... bootstrapArguments)
    {
        instructionList.add(new InvokeDynamicInsnNode(name, desc,
            bootstrapMethod, bootstrapArguments));
        return this;
    }

    public CodeBlock visitJumpInsn(int opcode, LabelNode node)
    {
        instructionList.add(new JumpInsnNode(opcode, node));
        return this;
    }

    public CodeBlock visitLabel(Label label)
    {
        instructionList.add(new LabelNode(label));
        return this;
    }

    public CodeBlock visitLdcInsn(Object value)
    {
        instructionList.add(new LdcInsnNode(value));
        return this;
    }

    public CodeBlock visitIincInsn(int varIndex, int increment)
    {
        instructionList.add(new IincInsnNode(varIndex, increment));
        return this;
    }

    public CodeBlock visitTableSwitchInsn(int min, int max,
                                          LabelNode defaultHandler, LabelNode[] handlers)
    {
        instructionList.add(new TableSwitchInsnNode(min, max, defaultHandler,
            handlers));
        return this;
    }

    public CodeBlock visitLookupSwitchInsn(LabelNode defaultHandler,
                                           int[] keys, LabelNode[] handlers)
    {
        instructionList.add(new LookupSwitchInsnNode(defaultHandler, keys,
            handlers));
        return this;
    }

    public CodeBlock visitMultiANewArrayInsn(String desc, int dims)
    {
        instructionList.add(new MultiANewArrayInsnNode(desc, dims));
        return this;
    }

    public CodeBlock visitTryCatchBlock(LabelNode scopeStart,
                                        LabelNode scopeEnd, LabelNode handler,
                                        @Nullable String exceptionType)
    {
        tryCatchBlockList.add(new TryCatchBlockNode(scopeStart, scopeEnd,
            handler, exceptionType));
        return this;
    }

    public CodeBlock visitLocalVariable(String varName,
                                        String varDesc, @Nullable String signature,
                                        LabelNode scopeStart, LabelNode scopeEnd,
                                        int varIndex)
    {
        localVariableList.add(new LocalVariableNode(varName, varDesc, signature,
            scopeStart, scopeEnd, varIndex));
        return this;
    }

    public CodeBlock visitLineNumber(int lineNumber,
                                     LabelNode start)
    {
        instructionList.add(new LineNumberNode(lineNumber, start));
        return this;
    }

    public CodeBlock tableswitch(int min, int max,
                                 LabelNode defaultLabel, LabelNode[] cases)
    {
        instructionList.add(new TableSwitchInsnNode(min, max, defaultLabel,
            cases));
        return this;
    }

    public CodeBlock visitFrame(int opcode, int nrLocals,
                                Object[] localTypes, int nrStackElements,
                                Object[] stackElements)
    {
        instructionList.add(new FrameNode(opcode, nrLocals, localTypes,
            nrStackElements, stackElements));
        return this;
    }

    public InsnList getInstructionList()
    {
        return instructionList;
    }

    public List<TryCatchBlockNode> getTryCatchBlockList()
    {
        return tryCatchBlockList;
    }

    public List<LocalVariableNode> getLocalVariableList()
    {
        return localVariableList;
    }

    public List<VisibleAnnotation> getAnnotations()
    {
        return annotations;
    }

    /**
     * adds a compressed frame to the stack
     *
     * @param stackArguments the argument types on the stack, represented as
     * "class path names" e.g java/lang/RuntimeException
     */
    public CodeBlock frame_same(Object... stackArguments)
    {
        int type;

        switch (stackArguments.length) {
            case 0:
                type = Opcodes.F_SAME;
                break;
            case 1:
                type = Opcodes.F_SAME1;
                break;
            default:
                throw new IllegalArgumentException("same frame should have 0"
                    + " or 1 arguments on stack");
        }

        instructionList.add(new FrameNode(type, 0, null, stackArguments.length,
            stackArguments));
        return this;
    }

    public CodeBlock prepend(CodeBlock codeBlock)
    {
        if (codeBlock.returns())
            returns = true;

        annotations.addAll(codeBlock.annotations);
        instructionList.insert(codeBlock.instructionList);
        return this;
    }

    public CodeBlock append(CodeBlock codeBlock)
    {
        if (codeBlock.returns())
            returns = true;

        instructionList.add(codeBlock.instructionList);
        tryCatchBlockList.addAll(codeBlock.tryCatchBlockList);
        annotations.addAll(codeBlock.annotations);
        return this;
    }

    public VisibleAnnotation annotation(Class<?> type)
    {
        VisibleAnnotation annotation = new VisibleAnnotation(ci(type));
        addAnnotation(annotation);
        return annotation;
    }

    public CodeBlock addAnnotation(VisibleAnnotation annotation)
    {
        annotations.add(annotation);
        return this;
    }

    public CodeBlock addAll(InsnList insnList)
    {
        instructionList.add(insnList);
        return this;
    }

    public int arity()
    {
        return arity;
    }

    public boolean returns()
    {
        return returns;
    }

    public CodeBlock clear()
    {
        instructionList.clear();
        tryCatchBlockList.clear();
        localVariableList.clear();
        annotations.clear();
        arity = 0;
        returns = false;
        return this;
    }
}
