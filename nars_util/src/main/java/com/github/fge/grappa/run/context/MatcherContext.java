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

package com.github.fge.grappa.run.context;

import com.github.fge.grappa.matchers.base.Matcher;

public interface MatcherContext<V>
    extends Context<V>
{
    MatcherContext<V> getParent();

    /*
     * TODO! Only used in ActionMatcher, VarFramingMatcher
     */
    void setMatcher(Matcher matcher);

    void setStartIndex(int startIndex);

    void setCurrentIndex(int currentIndex);

    void advanceIndex(int delta);

    /*
     * TODO! Only called from ActionMatcher and DefaultMatcherContext
     */
    MatcherContext<V> getBasicSubContext();

    // TODO: only overriden in ActionMatcher and AbstractMatcher
    MatcherContext<V> getSubContext(Matcher matcher);

    boolean runMatcher();
}
