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

package com.github.fge.grappa.matchers;

/**
 * Enumeration of the different matcher types
 */
public enum MatcherType
{
    /**
     * Terminal: a matcher which does not need another matcher to operate
     *
     * <p>This is equivalent to a terminal rule in formal grammars.</p>
     */
    TERMINAL,
    /**
     * Action
     *
     * <p>Only used by {@link ActionMatcher}.</p>
     */
    ACTION,
    /**
     * Composite
     *
     * <p>A matcher which delegates to other matchers to determine the success
     * of its operation. Such a matcher will also contain logic to determine
     * whether the whole match succeeds.</p>
     */
    COMPOSITE,
    /**
     * Predicate
     *
     * <p>Indicates a matcher which delegates to other rules but will never
     * advance into the input text.</p>
     */
    PREDICATE,
    ;
}
