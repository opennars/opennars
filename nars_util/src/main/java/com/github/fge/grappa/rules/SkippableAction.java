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

package com.github.fge.grappa.rules;

import com.github.fge.grappa.matchers.predicates.TestMatcher;
import com.github.fge.grappa.matchers.predicates.TestNotMatcher;

/**
 * An action that can optionally be skipped when run underneath a predicate
 * matcher.
 */
// TODO: why? Why isn't this in Action?
public interface SkippableAction<V>
    extends Action<V>
{
    /**
     * Determines whether the execution of this action is to be skipped in
     * predicate matchers.
     *
     * @return true if this action is not to be run inside predicates
     *
     * @see TestMatcher
     * @see TestNotMatcher
     */
    boolean skipInPredicates();

}
