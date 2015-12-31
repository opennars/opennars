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

import com.github.fge.grappa.misc.AsmUtils;
import com.google.common.collect.Range;
import nars.util.data.list.FasterList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.util.Printer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * A node in the instruction dependency graph.
 */
public final class InstructionGraphNode
        extends BasicValue {
    private static final Range<Integer> ILOAD_INSN_RANGE
            = Range.closedOpen(ILOAD, IALOAD);
    private static final Range<Integer> ISTORE_INSN_RANGE
            = Range.closedOpen(ISTORE, IASTORE);

    private final BasicValue resultValue;
    private final FasterList<InstructionGraphNode> predecessors = new FasterList();
    private AbstractInsnNode instruction;
    private InstructionGroup group;
    private boolean isActionRoot;
    private final int id;

    private static volatile int nextID = 0;
    private static int nextID() {
        return nextID++;
    }

    public InstructionGraphNode(AbstractInsnNode instruction,
                                BasicValue resultValue) {
        super(null);
        this.id = nextID();
        this.instruction = instruction;
        this.resultValue = resultValue;
        isActionRoot = AsmUtils.isActionRoot(instruction);
    }

    @Override
    public int getSize() {
        return resultValue.getSize();
    }

    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    public void setInstruction(AbstractInsnNode instruction) {
        this.instruction = instruction;
    }

    public BasicValue getResultValue() {
        return resultValue;
    }

    public List<InstructionGraphNode> getPredecessors() {
        return predecessors;
    }

    public InstructionGroup getGroup() {
        return group;
    }

    public void setGroup(@Nullable InstructionGroup newGroup) {
        if (newGroup == group)
            return;

        if (group != null)
            group.getNodes().remove(this);

        group = newGroup;
        if (group != null)
            group.getNodes().add(this);
    }

    public boolean isActionRoot() {
        return isActionRoot;
    }

    public void setIsActionRoot() {
        isActionRoot = true;
    }

    public boolean isVarInitRoot() {
        return AsmUtils.isVarRoot(instruction);
    }

    public boolean isCallOnContextAware() {
        return AsmUtils.isCallOnContextAware(instruction);
    }

    public boolean isXLoad() {
        return ILOAD_INSN_RANGE.contains(instruction.getOpcode());
    }

    public boolean isXStore() {
        return ISTORE_INSN_RANGE.contains(instruction.getOpcode());
    }

    public void addPredecessors(@Nonnull BasicValue[] preds) {
        Objects.requireNonNull(preds, "preds");
        //preds.stream().filter(pred -> pred instanceof InstructionGraphNode).forEach(pred -> addPredecessor((InstructionGraphNode) pred));
        for (BasicValue pred : preds) {
            if (pred instanceof InstructionGraphNode)
                addPredecessor((InstructionGraphNode) pred);
        }
    }

    public void addPredecessor(InstructionGraphNode node) {
        //if (!predecessors.contains(node)):
        Object[] pl = predecessors.array();
        for (Object in : pl) {
            if (in == node) return;
        }
        predecessors.add(node);
    }

    @Override
    public boolean equals(@Nullable Object value) {
        // TODO: what the...
        return value == this;
    }

    @Override
    public int hashCode() {
        // TODO: what the...
        return id; //System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return instruction.getOpcode() != -1
                ? Printer.OPCODES[instruction.getOpcode()]
                : super.toString();
    }
}
