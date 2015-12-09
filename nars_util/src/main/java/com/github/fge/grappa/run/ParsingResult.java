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

package com.github.fge.grappa.run;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.internal.NonFinalForTesting;
import com.github.fge.grappa.stack.ValueStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A simple container encapsulating the result of a parsing run.
 */
@NonFinalForTesting
public class ParsingResult<V>
{
    private final boolean matched;
    private final ValueStack<V> valueStack;
    private final InputBuffer inputBuffer;

    /**
     * Creates a new ParsingResult.
     *
     * @param matched true if the rule matched the input
     * @param valueStack the value stack of the parsing run
     * @param inputBuffer the input buffer
     */
    public ParsingResult(boolean matched,
                         @Nonnull ValueStack<V> valueStack,
                         @Nonnull InputBuffer inputBuffer)
    {
        this.matched = matched;
        this.valueStack = Objects.requireNonNull(valueStack);
        this.inputBuffer = Objects.requireNonNull(inputBuffer);
    }


    /**
     * Return true if this parse result is a match
     *
     * @return see description
     */
    public boolean isSuccess()
    {
        return matched;
    }

    /**
     * Gets the value at the top of the stack, if any
     *
     * @return the value at the top of the stack
     * @throws IllegalArgumentException stack is empty
     *
     * @see ValueStack#peek()
     */
    @Nonnull
    public V getTopStackValue()
    {
        return valueStack.peek();
    }

    /**
     * Get the value stack
     *
     * @return the value stack used during the parsing run
     */
    @Nonnull
    public ValueStack<V> getValueStack()
    {
        return valueStack;
    }

    @Override
    public String toString() {
        return "ParsingResult{" +
                "matched=" + matched +
                ", valueStack=" + valueStack +
                ", inputBuffer=" + inputBuffer +
                '}';
    }

    /**
     * Get the input buffer used by the parsing run
     *
     * @return see description
     */
    @Nonnull
    public InputBuffer getInputBuffer()
    {
        return inputBuffer;
    }
}
