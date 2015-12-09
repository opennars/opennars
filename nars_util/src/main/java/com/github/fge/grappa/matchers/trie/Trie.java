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

import com.google.common.annotations.Beta;

import javax.annotation.concurrent.Immutable;

/**
 * "User-facing" class of a {@link TrieMatcher}
 *
 * <p>This is an implementation of <a
 * href="http://en.wikipedia.org/wiki/Trie" target="_blank">trie</a> designed
 * to search for string constants.</p>
 *
 * <p>It is a rather "naïve" implementation in that it is only a trie and not
 * a much more sophisticated <a href="http://en.wikipedia.org/wiki/Radix_tree"
 * target="_blank">radix tree</a>, but it is efficient enough that searching
 * for a string among a series of strings is very fast.</p>
 *
 * <p>The core of the trie search algorithm is implemented by {@link
 * TrieNode}.</p>
 *
 * @since 1.0.0-beta.6
 */
@Immutable
@Beta
public final class Trie
{
    private final int nrWords;
    private final int maxLength;
    private final TrieNode node;

    /**
     * Create a new builder for this class
     *
     * @return a builder
     */
    public static TrieBuilder newBuilder()
    {
        return new TrieBuilder();
    }

    /**
     * Get the number of words injected into this trie
     *
     * @return the number of words
     */
    public int getNrWords()
    {
        return nrWords;
    }

    /**
     * Get the maximum length of a match
     *
     * @return the length of the longest word(s) added to this trie
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * Search for a string into this trie
     *
     * @param needle the string to search
     * @return the length of the match (ie, the string) or -1 if not found
     */
    public int search(String needle, boolean ignoreCase)
    {
        return node.search(needle, ignoreCase);
    }

    public int search(char[] needle, boolean ignoreCase)
    {
        return node.search(needle, ignoreCase);
    }

    Trie(TrieBuilder builder)
    {
        nrWords = builder.nrWords;
        maxLength = builder.maxLength;
        node = builder.nodeBuilder.build();
    }
}
