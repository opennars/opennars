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

package com.github.fge.grappa.transform.base;

import com.google.common.collect.Range;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.util.Printer;
import com.github.fge.grappa.misc.AsmUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

/**
 * A node in the instruction dependency graph.
 */
public final class InstructionGraphNode
    extends BasicValue
{
    private static final Range<Integer> ILOAD_INSN_RANGE
        = Range.closedOpen(ILOAD, IALOAD);
    private static final Range<Integer> ISTORE_INSN_RANGE
        = Range.closedOpen(ISTORE, IASTORE);

    private final BasicValue resultValue;
    private final List<InstructionGraphNode> predecessors = new ArrayList<>();
    private AbstractInsnNode instruction;
    private InstructionGroup group;
    private boolean isActionRoot;

    public InstructionGraphNode(final AbstractInsnNode instruction,
        final BasicValue resultValue)
    {
        super(null);
        this.instruction = instruction;
        this.resultValue = resultValue;
        isActionRoot = AsmUtils.isActionRoot(instruction);
    }

    @Override
    public int getSize()
    {
        return resultValue.getSize();
    }

    public AbstractInsnNode getInstruction()
    {
        return instruction;
    }

    public void setInstruction(final AbstractInsnNode instruction)
    {
        this.instruction = instruction;
    }

    public BasicValue getResultValue()
    {
        return resultValue;
    }

    public List<InstructionGraphNode> getPredecessors()
    {
        return predecessors;
    }

    public InstructionGroup getGroup()
    {
        return group;
    }

    public void setGroup(@Nullable final InstructionGroup newGroup)
    {
        if (newGroup == group)
            return;

        if (group != null)
            group.getNodes().remove(this);

        group = newGroup;
        if (group != null)
            group.getNodes().add(this);
    }

    public boolean isActionRoot()
    {
        return isActionRoot;
    }

    public void setIsActionRoot()
    {
        isActionRoot = true;
    }

    public boolean isVarInitRoot()
    {
        return AsmUtils.isVarRoot(instruction);
    }

    public boolean isCallOnContextAware()
    {
        return AsmUtils.isCallOnContextAware(instruction);
    }

    public boolean isXLoad()
    {
        return ILOAD_INSN_RANGE.contains(instruction.getOpcode());
    }

    public boolean isXStore()
    {
        return ISTORE_INSN_RANGE.contains(instruction.getOpcode());
    }

    public void addPredecessors(@Nonnull final Collection<BasicValue> preds)
    {
        Objects.requireNonNull(preds, "preds");
        for (final BasicValue pred: preds)
            if (pred instanceof InstructionGraphNode)
                addPredecessor((InstructionGraphNode) pred);
    }

    public void addPredecessor(final InstructionGraphNode node)
    {
        if (!predecessors.contains(node))
            predecessors.add(node);
    }

    @Override
    public boolean equals(@Nullable final Object value)
    {
        // TODO: what the...
        return value == this;
    }

    @Override
    public int hashCode()
    {
        // TODO: what the...
        return System.identityHashCode(this);
    }

    @Override
    public String toString()
    {
        return instruction.getOpcode() != -1
            ? Printer.OPCODES[instruction.getOpcode()]
            : super.toString();
    }
}
