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

import com.github.fge.grappa.transform.RuleMethodInterpreter;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Performs data/control flow analysis and constructs the instructions graph.
 */
public final class InstructionGraphCreator
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return method.containsImplicitActions()
            || method.containsExplicitActions()
            || method.containsVars();
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(method, "method");

        RuleMethodInterpreter interpreter
            = new RuleMethodInterpreter(method);
        RuleMethodAnalyzer analyzer = new RuleMethodAnalyzer(interpreter);

        analyzer.analyze(classNode.name, method);

        interpreter.finish();
    }

    // TODO: probably needs a rewrite; I have a hunch this is a gross hack
    private static final class RuleMethodAnalyzer
        extends Analyzer<BasicValue>
    {
        private final RuleMethodInterpreter interpreter;

        private RuleMethodAnalyzer(RuleMethodInterpreter interpreter)
        {
            super(Objects.requireNonNull(interpreter));
            this.interpreter = interpreter;
        }

        @Override
        protected void newControlFlowEdge(int insn, int successor)
        {
            interpreter.newControlFlowEdge(insn, successor);
        }

        @Override
        protected boolean newControlFlowExceptionEdge(int insn,
                                                      int successor)
        {
            interpreter.newControlFlowEdge(insn, successor);
            return true;
        }
    }
}
