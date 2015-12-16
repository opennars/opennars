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

package com.github.fge.grappa.misc;

import nars.util.data.list.FasterList;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A simple, immutable {@link GraphNode} implementation.
 *
 * @param <T> the actual implementation type of this ImmutableGraphNode
 */
// TODO: rename; this class IS NOT immutable.
public class ImmutableGraphNode<T extends GraphNode<T>>
    implements GraphNode<T>
{
    private static final FasterList emptyList = new FasterList();

    /*
         * TODO: cleanup that royal mess
         *
         * ImmutableLinkedList seems only to be used in a few special cases; this
         * class unfortunately allows for both the aforementioned class (which is,
         * frankly, also a mess) and regular lists.
         *
         * Find a way to separate.
         */
    private final FasterList<T> children;

    public ImmutableGraphNode()
    {
        this(emptyList/*ImmutableList.<T>of()*/);
    }

    // TODO! Null! Again! I need a gun!
    public ImmutableGraphNode(@Nonnull FasterList<T> children)
    {
        //Objects.requireNonNull(children);
        /*
         * ImmutableLinkedList has no such thing as a "safe copy constructor";
         * ImmutableList (Guava's, that is) does; what is more, if the argument
         * to .copyOf() is _also_ a (Guava...) ImmutableList, it won't even make
         * a copy.
         */
        //this.children = /*ImmutableList.copyOf*/new FasterList(children);
        this.children = children;
    }

    @Override
    public final FasterList<T> getChildren()    {
        return children;
    }

    public final void forEach(Consumer<T> c) {
        children.forEach(c);
    }
}
