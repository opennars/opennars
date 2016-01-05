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

import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.github.fge.grappa.transform.generate.ActionClassGenerator;
import com.github.fge.grappa.transform.generate.ClassNodeInitializer;
import com.github.fge.grappa.transform.generate.ConstructorGenerator;
import com.github.fge.grappa.transform.generate.VarInitClassGenerator;
import com.github.fge.grappa.transform.process.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.objectweb.asm.ClassWriter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.fge.grappa.misc.AsmUtils.*;

public enum ParserTransformer
{
    ;

    // TODO: remove "synchronized" here
    // TODO: move to Parboiled or the future Grappa class
    public static synchronized <T> Class<? extends T> transformParser(
        Class<T> parserClass)
        throws Exception
    {
        Objects.requireNonNull(parserClass, "parserClass");
        // first check whether we did not already create and load the extension
        // of the given parser class
        String name
            = getExtendedParserClassName(parserClass.getName());
        Class<?> extendedClass
            = findLoadedClass(name,parserClass.getClassLoader());
        Class<?> ret = extendedClass != null
            ? extendedClass
            : extendParserClass(parserClass).getExtendedClass();
        return (Class<? extends T>) ret;
    }

    /**
     * Dump the bytecode of a transformed parser class
     *
     * <p>This method will run all bytecode transformations on the parser class
     * then return a dump of the bytecode as a byte array.</p>
     *
     * @param parserClass the parser class
     * @return a bytecode dump
     *
     * @throws Exception FIXME
     * @see #extendParserClass(Class)
     */
    // TODO: poor exception specification
    public static byte[] getByteCode(Class<?> parserClass)
        throws Exception
    {
        ParserClassNode node = extendParserClass(parserClass);
        return node.getClassCode();
    }

    @VisibleForTesting
    public static ParserClassNode extendParserClass(Class<?> parserClass)
        throws Exception
    {
        ParserClassNode classNode = new ParserClassNode(parserClass);
        new ClassNodeInitializer().process(classNode);
        runMethodTransformers(classNode);
        ConstructorGenerator.process(classNode);
        defineExtendedParserClass(classNode);
        return classNode;
    }

    // TODO: poor exception handling again
    private static void runMethodTransformers(ParserClassNode classNode)
        throws Exception
    {
        List<RuleMethodProcessor> methodProcessors
            = createRuleMethodProcessors();

        // TODO: comment above may be right, but it's still dangerous
        // iterate through all rule methods
        // since the ruleMethods map on the classnode is a treemap we get the
        // methods sorted by name which puts all super methods first (since they
        // are prefixed with one or more '$')
        for (RuleMethod ruleMethod: classNode.getRuleMethods().values()) {
            if (ruleMethod.hasDontExtend())
                continue;

            for (RuleMethodProcessor methodProcessor : methodProcessors)
                if (methodProcessor.appliesTo(classNode, ruleMethod))
                    methodProcessor.process(classNode, ruleMethod);
        }

        classNode.methods.addAll(classNode.getRuleMethods().values().stream().filter(ruleMethod -> !ruleMethod.isGenerationSkipped()).collect(Collectors.toList()));
    }

    private static List<RuleMethodProcessor> createRuleMethodProcessors()
    {
        return ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new ActionClassGenerator(false),
            new VarInitClassGenerator(false),
            new RuleMethodRewriter(),
            new SuperCallRewriter(),
            new BodyWithSuperCallReplacer(),
            new VarFramingGenerator(),
            new LabellingGenerator(),
            new CachingGenerator()
        );
    }

    private static void defineExtendedParserClass(ParserClassNode node)
    {
        ClassWriter classWriter
            = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(classWriter);
        node.setClassCode(classWriter.toByteArray());
        Class<?> extendedClass  = loadClass(node.name.replace('/', '.'),
            node.getClassCode(), node.getParentClass().getClassLoader());
        node.setExtendedClass(extendedClass);
    }
}
