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
import java.nio.CharBuffer;
import java.util.Arrays;

/**
 * The core of the trie
 *
 * <p>This class implements a trie node. It has two elements in it:</p>
 *
 * <ul>
 *     <li>a boolean telling whether this node matches a full word;</li>
 *     <li>a list of children trie nodes, if any.</li>
 * </ul>
 *
 * <p>The children are indexed by the character they match (which means, in
 * effect, that a trie node has no characters "belonging" to him, and the root
 * node knows of all first characters there are to match).</p>
 *
 * @since 1.0.0-beta.6
 */
@Immutable
@Beta
public final class TrieNode
{
    private final boolean fullWord;

    private final char[] nextChars;
    private final TrieNode[] nextNodes;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    TrieNode(boolean fullWord, char[] nextChars,
             TrieNode[] nextNodes)
    {
        this.fullWord = fullWord;
        this.nextChars = nextChars;
        this.nextNodes = nextNodes;
    }

    public int search(String needle, boolean ignoreCase)
    {
        return doSearch(CharBuffer.wrap(needle), fullWord ? 0 : -1, 0,
            ignoreCase);
    }
    public int search(char[] needle, boolean ignoreCase)
    {
        return doSearch(CharBuffer.wrap(needle), fullWord ? 0 : -1, 0,
                ignoreCase);
    }

    /**
     * Core search method
     * <p>
     * <p>This method uses a {@link CharBuffer} to perform searches, and changes
     * this buffer's position as the match progresses. The two other arguments
     * are the depth of the current search (ie the number of nodes visited
     * since root) and the index of the last node where a match was found (ie
     * the last node where {@link #fullWord} was true.</p>
     *
     * @param buffer        the charbuffer
     * @param matchedLength the last matched length (-1 if no match yet)
     * @param currentLength the current length walked by the trie
     * @return the length of the match found, -1 otherwise
     */
    private int doSearch(CharBuffer buffer, int matchedLength,
                         int currentLength, boolean ignoreCase) {
        TrieNode other = this;
        while (true) {
        /*
         * Try and see if there is a possible match here; there is if "fullword"
         * is true, in this case the next "matchedLength" argument to a possible
         * child call will be the current length.
         */
            int nextLength = other.fullWord ? currentLength : matchedLength;


        /*
         * If there is nothing left in the buffer, we have a match.
         */
            if (!buffer.hasRemaining())
                return nextLength;

        /*
         * OK, there is at least one character remaining, so pick it up and see
         * whether it is in the list of our children...
         */
            char c = buffer.get();
            int index = Arrays.binarySearch(other.nextChars, c);
            if (index < 0 && ignoreCase) {
                boolean isUpper = Character.isUpperCase(c);
                boolean isLower = Character.isLowerCase(c);
                if (isUpper != isLower) {
                    c = isUpper ? Character.toLowerCase(c)
                            : Character.toUpperCase(c);
                    index = Arrays.binarySearch(other.nextChars, c);
                }
            }

        /*
         * If not, we return the last good match; if yes, we call this same
         * method on the matching child node with the (possibly new) matched
         * length as an argument and a depth increased by 1.
         */
            if (index < 0)
                return nextLength;
            matchedLength = nextLength;
            currentLength = currentLength + 1;
            other = other.nextNodes[index];
        }
    }
}
