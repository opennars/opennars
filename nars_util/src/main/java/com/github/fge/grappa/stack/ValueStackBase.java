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

package com.github.fge.grappa.stack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Base abstract implementation of a {@link ValueStack}
 *
 * @param <V> type parameter of this stack's values
 */
@ParametersAreNonnullByDefault
public abstract class ValueStackBase<V>
    implements ValueStack<V>
{
    @Override
    public final void push(V value)
    {
        push(0, value);
    }

    @Override
    public final void push(int down, V value)
    {
        /*
         * It is legal to append at the end! We must therefore check that the
         * index - 1 is strictly less than size, not the index itself
         */
        if (down < 0)
            throw new IllegalArgumentException("index cannot be negative");
        checkIndex(down - 1);
        //Objects.requireNonNull(value);
        doPush(down, value);
    }

    /**
     * Push a value onto the stack at the given index
     *
     * <p>The value is guaranteed never to be null and the index is guaranteed
     * to be valid.</p>
     *
     * @param down the index
     * @param value the value
     */
    protected abstract void doPush(int down, V value);

    @Nonnull
    @Override
    public final V pop()
    {
        return pop(0);
    }

    @Nonnull
    @Override
    public final V pop(int down)
    {
        if (down < 0)
            throw new IllegalArgumentException("index cannot be negative");
        checkIndex(down);
        return doPop(down);
    }

    @Nonnull
    @Override
    public final <T extends V> T popAs(Class<T> type)
    {
        return type.cast(pop(0));
    }

    @Nonnull
    @Override
    public final <T extends V> T popAs(Class<T> type, int down)
    {
        return type.cast(pop(down));
    }

    /**
     * Removes the value from a given stack index
     *
     * <p>The index is guaranteed to be valid.</p>
     *
     * @param down the index
     * @return the value
     */
    protected abstract V doPop(int down);

    @Nonnull
    @Override
    public final V peek()
    {
        return peek(0);
    }

    @Nonnull
    @Override
    public final V peek(int down)
    {
        if (down < 0)
            throw new IllegalArgumentException("index cannot be negative");
        checkIndex(down);
        return doPeek(down);
    }

    @Nonnull
    @Override
    public final <T extends V> T peekAs(Class<T> type)
    {
        return type.cast(peek(0));
    }

    @Nonnull
    @Override
    public final <T extends V> T peekAs(Class<T> type, int down)
    {
        return type.cast(peek(down));
    }

    /**
     * Retrieves, witout removing, the value at the given stack indx
     *
     * <p>The index is guaranteed to be valid.</p>
     *
     * @param down the index
     * @return the value
     */
    protected abstract V doPeek(int down);

    @Override
    public final void poke(@Nonnull V value)
    {
        poke(0, value);
    }

    @Override
    public final void poke(int down, V value)
    {
        if (down < 0)
            throw new IllegalArgumentException("index cannot be negative");
        checkIndex(down);
        Objects.requireNonNull(value);
        doPoke(down, value);
    }

    /**
     * Replaces a value at a given stack index
     *
     * <p>The index is guaranteed to be valid and the value is guaranteed not to
     * be null.</p>
     *
     * @param down the index
     * @param value the value
     */
    protected abstract void doPoke(int down, V value);

    @Override
    public final void swap(int n)
    {
        if (n < 2)
            throw new IllegalArgumentException("argument must be >= 2");
        /*
         * As for .push(n, value), we need to check for n - 1 here
         */
        checkIndex(n - 1);
        doSwap(n);
    }

    @Override
    public final void swap()
    {
        swap(2);
    }

    /**
     * Duplicates the top value. Equivalent to push(peek()).
     */
    @Override
    public final void dup()
    {
        checkIndex(0);
        doDup();
    }

    protected abstract void doDup();

    /**
     * Reverses the order of the top n stack values
     *
     * <p>The number of values is guaranteed to be valid.</p>
     *
     * @param n the number of values to swap
     */
    protected abstract void doSwap(int n);

    protected abstract void checkIndex(int index);
}
