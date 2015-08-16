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

package com.github.fge.grappa.stack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Stack implementation for use in parsers
 *
 * <p>A stack state can be saved and restored using {@link #takeSnapshot()} and
 * {@link #restoreSnapshot(Object)}.</p>
 *
 * <p>Stacks do not accept null values; if a null value is inserted, a {@link
 * NullPointerException} will be thrown.</p>
 *
 * <p>Any attempt to pop/swap values on a stack with not enough elements will
 * throw an {@link IllegalStateException}.</p>
 *
 * <p>You probably want to extend {@link ValueStackBase} instead of directly
 * implementing this interface.</p>
 *
 * @param <V> the type of the value objects
 */
@ParametersAreNonnullByDefault
public interface ValueStack<V>
    extends Iterable<V>
{
    /**
     * Determines whether the stack is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of elements currently on the stack.
     *
     * @return the number of elements
     */
    int size();

    /**
     * Clears all values.
     */
    void clear();

    /**
     * Returns an object representing the current state of the stack.
     *
     * @return an object representing the current state of the stack
     */
    @Nonnull
    Object takeSnapshot();

    /**
     * Restores the stack state as previously returned by {@link
     * #takeSnapshot()}.
     *
     * @param snapshot a snapshot object previously returned by {@link
     * #takeSnapshot()}
     */
    void restoreSnapshot(Object snapshot);

    /**
     * Pushes the given value onto the stack. Equivalent to push(0, value).
     *
     * @param value the value
     */
    void push(V value);

    /**
     * Inserts the given value a given number of elements below the current top
     * of the stack.
     *
     * @param down the number of elements to skip before inserting the value (0
     * being equivalent to push(value))
     * @param value the value
     */
    void push(int down, V value);

    /**
     * Removes the value at the top of the stack and returns it.
     *
     * @return the current top value
     */
    @Nonnull
    V pop();

    /**
     * Removes the value the given number of elements below the top of the stack.
     *
     * @param down the number of elements to skip before removing the value (0
     * being equivalent to pop())
     * @return the value
     */
    @Nonnull
    V pop(int down);

    /**
     * Removes and casts the value at the top of the stack, and returns it
     *
     * @param type the type to cast to
     * @param <T> type parameter
     * @return the value
     */
    @Nonnull
    <T extends V> T popAs(Class<T> type);

    /**
     * Removes and casts the value at a given index in the stack, and returns it
     *
     * @param type the type to cast to
     * @param down the index
     * @param <T> type parameter
     * @return the value
     */
    @Nonnull
    <T extends V> T popAs(Class<T> type, int down);

    /**
     * Returns the value at the top of the stack without removing it.
     *
     * @return the current top value
     */
    @Nonnull
    V peek();

    /**
     * Returns the value the given number of elements below the top of the stack
     * without removing it.
     *
     * @param down the number of elements to skip (0 being equivalent to peek())
     * @return the value
     */
    @Nonnull
    V peek(int down);

    /**
     * Casts and returns the value at the top of the stack without removing it
     *
     * @param type the type to cast to
     * @param <T> type parameter
     * @return the value
     */
    @Nonnull
    <T extends V> T peekAs(Class<T> type);

    /**
     * Casts and returns the value at a given index in the stack without
     * removing it
     *
     * @param type the type to cast to
     * @param down the index in the stack
     * @param <T> type parameter
     * @return the value
     */
    @Nonnull
    <T extends V> T peekAs(Class<T> type, int down);

    /**
     * Replaces the current top value with the given value. Equivalent to
     * poke(0, value).
     *
     * @param value the value
     */
    void poke(@Nonnull V value);

    /**
     * Replaces the element the given number of elements below the current top
     * of the stack.
     *
     * @param down the number of elements to skip before replacing the value (0
     * being equivalent to poke(value))
     * @param value the value to replace with
     */
    void poke(int down, V value);

    /**
     * Duplicates the top value. Equivalent to push(peek()).
     */
    void dup();

    /**
     * Reverses the order of the top n stack values
     *
     * @param n the number of elements to reverse
     */
    void swap(int n);

    /**
     * Swaps the top two stack values.
     */
    void swap();
}
