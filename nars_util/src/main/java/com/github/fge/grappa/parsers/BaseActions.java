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

package com.github.fge.grappa.parsers;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.run.context.Context;
import com.github.fge.grappa.run.context.ContextAware;
import com.github.fge.grappa.stack.ValueStack;
import com.github.fge.grappa.support.IndexRange;
import com.github.fge.grappa.support.Position;

import java.util.Objects;

/**
 * Convenience context aware base class defining a number of useful helper methods.
 *
 * @param <V> the type of the parser values
 */
public abstract class BaseActions<V>
    implements ContextAware<V>
{
    private Context<V> context;

    /**
     * The current context for use with action methods. Updated immediately
     * before action calls.
     *
     * @return the current context
     */
    public final Context<V> getContext()
    {
        return context;
    }

    /**
     * ContextAware interface implementation.
     *
     * @param context the context
     */
    @Override
    public final void setContext(Context<V> context)
    {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns the current index in the input buffer.
     *
     * @return the current index
     */
    public final int currentIndex()
    {
        //check();
        return context.getCurrentIndex();
    }

    /**
     * <p>Returns the input text matched by the rule immediately preceding the
     * action expression that is currently being evaluated. This call can only
     * be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the input text matched by the immediately preceding subrule
     */
    public String match()
    {
        //check();
        return context.getMatch();
    }

    /**
     * Creates a new {@link IndexRange} instance covering the input text matched
     * by the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.
     *
     * @return a new IndexRange instance
     */
    public IndexRange matchRange()
    {
        //check();
        return context.getMatchRange();
    }

    /**
     * <p>Returns the input text matched by the rule immediately preceding the
     * action expression that is currently being evaluated. If the matched input
     * text is empty the given default string is returned. This call can only be
     * used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @param defaultString the default string to return if the matched input
     * text is empty
     * @return the input text matched by the immediately preceding subrule or
     * the default string
     */
    public String matchOrDefault(String defaultString)
    {
        //check();
        String match = context.getMatch();
        return match.isEmpty() ? defaultString : match;
    }

    /**
     * <p>Returns the first character of the input text matched by the rule
     * immediately preceding the action expression that is currently being
     * evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.</p>
     * <p>If the immediately preceding rule did not match anything this method
     * throws a GrammarException. If you need to be able to handle that case use
     * the getMatch() method.</p>
     *
     * @return the first input char of the input text matched by the immediately
     * preceding subrule or null, if the previous rule matched nothing
     */
    // TODO: can't return null; check what _really_ happens.
    public char matchedChar()
    {
        //check();
        return context.getFirstMatchChar();
    }

    /**
     * <p>Returns the start index of the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used
     * in actions that are part of a Sequence rule and are not at first position
     * in this Sequence.</p>
     *
     * @return the start index of the context immediately preceding current
     * action
     */
    public int matchStart()
    {
        //check();
        return context.getMatchStartIndex();
    }

    /**
     * <p>Returns the end location of the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used
     * in actions that are part of a Sequence rule and are not at first position
     * in this Sequence.</p>
     *
     * @return the end index of the context immediately preceding current
     * action, i.e. the index of the character immediately following the last
     * matched character
     */
    public int matchEnd()
    {
        //check();
        return context.getMatchEndIndex();
    }

    /**
     * <p>Returns the number of characters matched by the rule immediately
     * preceding the action expression that is currently being evaluated. This
     * call can only be used in actions that are part of a Sequence rule and are
     * not at first position in this Sequence.</p>
     *
     * @return the number of characters matched
     */
    public int matchLength()
    {
        //check();
        return context.getMatchLength();
    }

    /**
     * <p>Returns the current position in the underlying {@link InputBuffer} as
     * a {@link Position} instance.</p>
     *
     * @return the current position in the underlying inputbuffer
     */
    public Position position()
    {
        //check();
        return context.getPosition();
    }

    /**
     * Pushes the given value onto the value stack. Equivalent to push(0,
     * value).
     *
     * @param value the value to push
     * @return true
     */
    public boolean push(V value)
    {
        //check();
        context.getValueStack().push(value);
        return true;
    }

    /**
     * Inserts the given value a given number of elements below the current top
     * of the value stack.
     *
     * @param down the number of elements to skip before inserting the value (0
     * being equivalent to push(value))
     * @param value the value
     * @return true
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    public boolean push(int down, V value)
    {
        //check();
        context.getValueStack().push(down, value);
        return true;
    }

    /**
     * Removes the value at the top of the value stack and returns it.
     *
     * @return the current top value
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public V pop()
    {
        //check();
        return context.getValueStack().pop();
    }

    /**
     * Removes the value the given number of elements below the top of the value
     * stack.
     *
     * @param down the number of elements to skip before removing the value (0
     * being equivalent to pop())
     * @return the value
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    public V pop(int down)
    {
        //check();
        return context.getValueStack().pop(down);
    }

    /**
     * Removes the value at the top of the value stack.
     *
     * @return true
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean drop()
    {
        //check();
        context.getValueStack().pop();
        return true;
    }

    /**
     * Removes the value the given number of elements below the top of the value
     * stack.
     *
     * @param down the number of elements to skip before removing the value (0
     * being equivalent to drop())
     * @return true
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    public boolean drop(int down)
    {
        //check();
        context.getValueStack().pop(down);
        return true;
    }

    /**
     * Returns the value at the top of the value stack without removing it.
     *
     * @return the current top value
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public V peek()
    {
        //check();
        return context.getValueStack().peek();
    }

    /**
     * Returns the value the given number of elements below the top of the value
     * stack without removing it.
     *
     * @param down the number of elements to skip (0 being equivalent to peek())
     * @return the value
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    public V peek(int down)
    {
        //check();
        return context.getValueStack().peek(down);
    }

    /**
     * Replaces the current top value of the value stack with the given value.
     * Equivalent to poke(0, value).
     *
     * @param value the value
     * @return true
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean poke(V value)
    {
        //check();
        context.getValueStack().poke(value);
        return true;
    }

    /**
     * Replaces the element the given number of elements below the current top
     * of the value stack.
     *
     * @param down the number of elements to skip before replacing the value (0
     * being equivalent to poke(value))
     * @param value the value to replace with
     * @return true
     *
     * @throws IllegalArgumentException if the stack does not contain enough
     * elements to perform this operation
     */
    public boolean poke(int down, V value)
    {
        //check();
        context.getValueStack().poke(down, value);
        return true;
    }

    /**
     * Duplicates the top value of the value stack. Equivalent to push(peek()).
     *
     * @return true
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public boolean dup()
    {
        //check();
        context.getValueStack().dup();
        return true;
    }

    /**
     * Swaps the top two elements of the value stack.
     *
     * @return true
     *
     * @throws IllegalArgumentException if the stack does not contain at least
     * two elements
     */
    public boolean swap()
    {
        //check();
        context.getValueStack().swap();
        return true;
    }

    /**
     * Reverse the order of the top n elements of this context's value stack
     *
     * @param n the number of elements to swap
     * @return always true
     * @throws IllegalArgumentException stack does not contain enough elements
     *
     * @see ValueStack#swap(int)
     */
    public boolean swap(int n)
    {
        //check();
        context.getValueStack().swap(n);
        return true;
    }

    /**
     * Returns the next input character about to be matched.
     *
     * @return the next input character about to be matched
     */
    public char currentChar()
    {
        //check();
        return context.getCurrentChar();
    }

    /**
     * Returns true if the current rule is running somewhere underneath a
     * Test/TestNot rule.
     * Useful for example for making sure actions are not run inside of a
     * predicate evaluation:
     * {@code
     * return Sequence(
     * ...,
     * inPredicate() || actions.doSomething()
     * );
     * }
     *
     * @return true if in a predicate
     */
    public boolean inPredicate()
    {
        //check();
        return context.inPredicate();
    }

    /**
     * Determines whether the current rule or a sub rule has recorded a parse
     * error.
     * Useful for example for making sure actions are not run on erroneous
     * input:
     * {@code
     * return Sequence(
     * ...,
     * !hasError() &amp;&amp; actions.doSomething()
     * );
     * }
     *
     * @return true if either the current rule or a sub rule has recorded a
     * parse error
     */
    public boolean hasError()
    {
        //check();
        return context.hasError();
    }

    // TODO: pain point here
    private void check()
    {
        if (context == null /*|| context.getMatcher() == null*/)
            throw new InvalidGrammarException("rule has an unwrapped action"
                + " expression");
    }
}
