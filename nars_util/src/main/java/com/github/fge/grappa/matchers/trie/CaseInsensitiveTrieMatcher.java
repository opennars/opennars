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

package com.github.fge.grappa.matchers.trie;

import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.run.context.MatcherContext;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * The trie matcher
 */
@Immutable
public final class CaseInsensitiveTrieMatcher
    extends AbstractMatcher
{
    private final Trie trie;

    public CaseInsensitiveTrieMatcher(Trie trie)
    {
        super("trieIgnoreCase(" + Objects.requireNonNull(trie).getNrWords()
            + " strings)");
        this.trie = trie;
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    /**
     * Tries a match on the given MatcherContext.
     *
     * @param context the MatcherContext
     * @return true if the match was successful
     */
    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        /*
         * Since the trie knows about the length of its possible longest match,
         * extract that many characters from the buffer. Remind that .extract()
         * will adjust illegal indices automatically.
         *
         * Convert the word to lowercase, since all characters are stored as
         * lowercase in the trie.
         */
        int maxLength = trie.getMaxLength();
        int index = context.getCurrentIndex();
        char[] input = context.getInputBuffer()
            .extractChars(index, index + maxLength);

        /*
         * We now just have to trie and search... (pun intended)
         */
        int ret = trie.search(input, true);
        if (ret == -1)
            return false;

        /*
         * and since the result, when positive, is the length of the match,
         * advance the index in the buffer by that many positions.
         */
        context.advanceIndex(ret);
        return true;
    }
}
