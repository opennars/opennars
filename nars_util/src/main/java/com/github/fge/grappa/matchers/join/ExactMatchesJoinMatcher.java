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

package com.github.fge.grappa.matchers.join;

import com.github.fge.grappa.rules.Rule;
import com.google.common.annotations.Beta;

/*
 * A matcher which must match exactly n times. Note that n is >= 2 (otherwise
 * the builder would have returned an empty matcher or the joined rule)
 */
@Beta
public final class ExactMatchesJoinMatcher
    extends JoinMatcher
{
    private final int nrCycles;

    public ExactMatchesJoinMatcher(Rule joined, Rule joining,
                                   int nrCycles)
    {
        super(joined, joining);
        this.nrCycles = nrCycles;
    }

    @Override
    protected boolean runAgain(int cycles)
    {
        return cycles < nrCycles;
    }

    @Override
    protected boolean enoughCycles(int cycles)
    {
        return cycles == nrCycles;
    }
}
