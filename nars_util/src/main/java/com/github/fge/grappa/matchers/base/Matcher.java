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

package com.github.fge.grappa.matchers.base;

import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.misc.GraphNode;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;

/**
 * A Matcher instance is responsible for "executing" a specific Rule instance, i.e. it implements the actual
 * rule type specific matching logic.
 * Since it extends the {@link GraphNode} interface it can have submatchers.
 */
public interface Matcher
    extends Rule, GraphNode<Matcher>
{
    /**
     * Returns the type of this matcher
     *
     * @return the type
     *
     * @see MatcherType
     */
    MatcherType getType();

    /**
     * @return the label of the matcher (which is identical to the label of the
     * Rule this matcher matches)
     */
    String getLabel();

    /**
     * @return true if this matcher has been assigned a custom label
     */
    boolean hasCustomLabel();

    /**
     * Creates a context for the matching of this matcher using the given parent
     * context.
     *
     * @param context the parent context
     * @return the context this matcher is to be run in
     */
    <V> MatcherContext<V> getSubContext(MatcherContext<V> context);

    /**
     * Tries a match on the given MatcherContext.
     *
     * @param context the MatcherContext
     * @return true if the match was successful
     */
    <V> boolean match(MatcherContext<V> context);
}
