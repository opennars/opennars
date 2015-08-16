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

package com.github.fge.grappa.helpers;

import com.github.fge.grappa.parsers.ListeningParser;

import javax.annotation.Nonnull;

/**
 * A value builder for use in a parser
 *
 * <p>Note that all setters should return a {@code boolean}, since the primary
 * use of this interface is within rules. If the grammar rules are not enough
 * for validation, you may make the setter return {@code false} to signal a
 * parsing error.</p>
 *
 * <p>This is also the base interface used by {@link ListeningParser} to post
 * events.</p>
 *
 * @param <T> type of the value produced
 *
 * @see ListeningParser#post(ValueBuilder)
 */
public interface ValueBuilder<T>
{
    /**
     * Build the value
     *
     * @return the built value
     */
    @Nonnull
    T build();

    /**
     * Reset this builder
     *
     * <p>This method is called by {@link ListeningParser#post(ValueBuilder)}
     * after the value has been built. Since it always returns {@code true}, you
     * may also use it as an action in a parser.</p>
     *
     * <p>It is the responsibility of the implementations to ensure that the
     * builder is reset to a state so that it is ready to be used again.</p>
     *
     * @return always {@code true}
     */
    boolean reset();
}
