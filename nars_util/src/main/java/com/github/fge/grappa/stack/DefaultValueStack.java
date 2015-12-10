/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import nars.util.data.list.FasterList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Iterator;

@ParametersAreNonnullByDefault
public final class DefaultValueStack<V>
        extends ValueStackBase<V> {

    public final FasterList<V> stack = new FasterList<>();

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public void clear() {
        stack.clear();
    }

    @Nonnull
    @Override
    public Object takeSnapshot() {
        if (stack.isEmpty()) return null; //avoid creating an empty collection
        return new FasterList<>(stack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreSnapshot(Object snapshot) {
        if (snapshot==null) {
            stack.clear();
        }
        else {
            //Objects.requireNonNull(snapshot);
            //Preconditions.checkState(snapshot.getClass() == ArrayList.class);

            //stack = (FasterList<V>) snapshot;
            if (stack!=snapshot) {
                stack.clear();
                stack.addAll((FasterList)snapshot);
            }
        }
    }

    @Override
    protected void doPush(int down, V value) {
        stack.add(down, value);
    }

    @Nonnull
    @Override
    protected V doPop(int down) {
        return stack.remove(down);
    }

    @Nonnull
    @Override
    protected V doPeek(int down) {
        return stack.get(down);
    }

    @Override
    protected void doPoke(int down, V value) {
        stack.set(down, value);
    }

    @Override
    protected void doDup() {
        V element = stack.get(0);
        stack.add(0, element);
    }

    @Override
    protected void doSwap(int n) {
        Collections.reverse(stack.subList(0, n));
    }

    @Override
    public Iterator<V> iterator() {
        return Iterators.unmodifiableIterator(stack.iterator());
    }

    @Nonnull
    @Override
    public String toString() {
        return stack.toString();
    }

    @Override
    protected void checkIndex(int index) {
        Preconditions.checkState(index < stack.size(),
                "not enough elements in stack");
    }
}
