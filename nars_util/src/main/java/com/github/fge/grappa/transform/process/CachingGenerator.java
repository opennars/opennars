/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.fge.grappa.transform.process;

import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.matchers.wrap.ProxyMatcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.transform.CodeBlock;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.github.fge.grappa.transform.runtime.CacheArguments;
import com.google.common.base.Preconditions;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ARETURN;

/**
 * Wraps the method code with caching and proxying constructs.
 */
public final class CachingGenerator
    implements RuleMethodProcessor
{
    private ParserClassNode classNode;
    private RuleMethod method;
    private InsnList instructions;
    private AbstractInsnNode retInsn;
    private String cacheFieldName;

    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return method.hasCachedAnnotation();
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        // super methods have flag moved to the overriding method
        Preconditions.checkState(!method.isSuperMethod());

        this.classNode = classNode;
        this.method = method;
        instructions = method.instructions;
        retInsn = instructions.getLast();

        while (retInsn.getOpcode() != ARETURN)
            retInsn = retInsn.getPrevious();

        CodeBlock block;

        block = CodeBlock.newCodeBlock();
        generateCacheHitReturn(block);
        generateStoreNewProxyMatcher(block);

        instructions.insert(block.getInstructionList());

        block = CodeBlock.newCodeBlock();
        generateArmProxyMatcher(block);
        generateStoreInCache(block);

        instructions.insertBefore(retInsn, block.getInstructionList());
    }

    // if (<cache> != null) return <cache>;
    private void generateCacheHitReturn(CodeBlock block)
    {
        generateGetFromCache(block);

        LabelNode cacheMiss = new LabelNode();

        block.dup()
            .ifnull(cacheMiss)
            .areturn()
            .label(cacheMiss)
            .pop();
    }

    private void generateGetFromCache(CodeBlock block)
    {
        Type[] paramTypes = Type.getArgumentTypes(method.desc);
        cacheFieldName = findUnusedCacheFieldName();

        // if we have no parameters we use a simple Rule field as cache,
        // otherwise a HashMap
        String cacheFieldDesc = paramTypes.length == 0
            ? CodegenUtils.ci(Rule.class)
            : CodegenUtils.ci(HashMap.class);
        FieldNode field = new FieldNode(ACC_PRIVATE, cacheFieldName,
            cacheFieldDesc, null, null);

        classNode.fields.add(field);

        block.aload(0).getfield(classNode.name, cacheFieldName, cacheFieldDesc);

        if (paramTypes.length == 0)
            return; // if we have no parameters we are done

        // generate: if (<cache> == null) <cache> = new HashMap<Object, Rule>();

        LabelNode alreadyInitialized = new LabelNode();

        block.dup()
            .ifnonnull(alreadyInitialized)
            .pop()
            .aload(0)
            .newobj(CodegenUtils.p(HashMap.class)).dup_x1().dup()
            .invokespecial(CodegenUtils.p(HashMap.class), "<init>",
                CodegenUtils.sig(void.class))
            .putfield(classNode.name, cacheFieldName, cacheFieldDesc)
            .label(alreadyInitialized);

        // if we have more than one parameter or the parameter is an array we
        // have to wrap with our Arguments class since we need to unroll all
        // inner arrays and apply custom hashCode(...) and equals(...)
        // implementations
        if (paramTypes.length > 1 || paramTypes[0].getSort() == Type.ARRAY) {
            // generate: push new Arguments(new Object[] {<params>})

            block.newobj(CodegenUtils.p(CacheArguments.class)).dup();

            generatePushNewParameterObjectArray(block, paramTypes);

            block.invokespecial(CodegenUtils.p(CacheArguments.class), "<init>",
                CodegenUtils.sig(void.class, Object[].class));
        } else {
            generatePushParameterAsObject(block, paramTypes, 0);
        }

        // generate: <hashMap>.get(...)

        block.dup().astore(method.maxLocals)
            .invokevirtual(CodegenUtils.p(HashMap.class), "get",
                CodegenUtils.sig(Object.class, Object.class));
    }

    private String findUnusedCacheFieldName()
    {
        String name = "cache$" + method.name;
        int i = 2;
        while (hasField(name))
            name = "cache$" + method.name + i++;
        return name;
    }

    private boolean hasField(String fieldName)
    {
        for (Object field : classNode.fields)
            if (fieldName.equals(((FieldNode) field).name))
                return true;

        return false;
    }

    private static void generatePushNewParameterObjectArray(CodeBlock block,
                                                            Type[] paramTypes)
    {
        block.bipush(paramTypes.length).anewarray(CodegenUtils.p(Object.class));

        for (int i = 0; i < paramTypes.length; i++) {
            block.dup().bipush(i);
            generatePushParameterAsObject(block, paramTypes, i);
            block.aastore();
        }
    }

    private static void generatePushParameterAsObject(CodeBlock block,
                                                      Type[] paramTypes, int parameterNr)
    {
        switch (paramTypes[parameterNr++].getSort()) {
            case Type.BOOLEAN:
                block.iload(parameterNr)
                    .invokestatic(CodegenUtils.p(Boolean.class), "valueOf",
                        CodegenUtils.sig(Boolean.class, boolean.class));
                return;
            case Type.CHAR:
                block.iload(parameterNr)
                    .invokestatic(CodegenUtils.p(Character.class), "valueOf",
                    CodegenUtils.sig(Character.class, char.class));
                return;
            case Type.BYTE:
                block.iload(parameterNr)
                    .invokestatic(CodegenUtils.p(Byte.class), "valueOf",
                    CodegenUtils.sig(Byte.class, byte.class));
                return;
            case Type.SHORT:
                block.iload(parameterNr)
                    .invokestatic(CodegenUtils.p(Short.class), "valueOf",
                    CodegenUtils.sig(Short.class, short.class));
                return;
            case Type.INT:
                block.iload(parameterNr)
                    .invokestatic(CodegenUtils.p(Integer.class), "valueOf",
                    CodegenUtils.sig(Integer.class, int.class));
                return;
            case Type.FLOAT:
                block.fload(parameterNr)
                    .invokestatic(CodegenUtils.p(Float.class), "valueOf",
                    CodegenUtils.sig(Float.class, float.class));
                return;
            case Type.LONG:
                block.lload(parameterNr)
                    .invokestatic(CodegenUtils.p(Long.class), "valueOf",
                    CodegenUtils.sig(Long.class, long.class));
                return;
            case Type.DOUBLE:
                block.dload(parameterNr)
                    .invokestatic(CodegenUtils.p(Double.class), "valueOf",
                    CodegenUtils.sig(Double.class, double.class));
                return;
            case Type.ARRAY:
            case Type.OBJECT:
                block.aload(parameterNr);
                return;
            case Type.VOID:
            default:
                throw new IllegalStateException();
        }
    }

    // <cache> = new ProxyMatcher();
    private void generateStoreNewProxyMatcher(CodeBlock block)
    {
        block.newobj(CodegenUtils.p(ProxyMatcher.class))
            .dup()
            .invokespecial(CodegenUtils.p(ProxyMatcher.class), "<init>",
                CodegenUtils.sig(void.class));

        generateStoreInCache(block);
    }

    // <proxyMatcher>.arm(<rule>)
    private static void generateArmProxyMatcher(CodeBlock block)
    {
        block.dup_x1()
            .checkcast(CodegenUtils.p(Matcher.class))
            .invokevirtual(CodegenUtils.p(ProxyMatcher.class), "arm",
                CodegenUtils.sig(void.class, Matcher.class));
    }

    private void generateStoreInCache(CodeBlock block)
    {
        Type[] paramTypes = Type.getArgumentTypes(method.desc);

        block.dup();

        if (paramTypes.length == 0) {
            block.aload(0)
                .swap()
                .putfield(classNode.name, cacheFieldName,
                    CodegenUtils.ci(Rule.class));
            return;
        }

        block.aload(method.maxLocals)
            .swap()
            .aload(0)
            .getfield(classNode.name, cacheFieldName,
                CodegenUtils.ci(HashMap.class))
            .dup_x2()
            .pop()
            .invokevirtual(CodegenUtils.p(HashMap.class), "put",
                CodegenUtils.sig(Object.class, Object.class, Object.class))
            .pop();
    }
}
