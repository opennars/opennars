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

import java.nio.CharBuffer;
import java.util.Map;
import java.util.TreeMap;

/**
 * A builder for a {@link TrieNode} tree
 *
 * <p>The tree is built "in depth"; each character of a string will create a new
 * builder unless there is already a builder for that character.</p>
 *
 * <p>When {@link #build()} is called, the whole tree is built from the leaves
 * up to the root.</p>
 *
 * @since 1.0.0-beta.6
 */
@Beta
public final class TrieNodeBuilder
{
    private boolean fullWord = false;

    private final Map<Character, TrieNodeBuilder> subnodes = new TreeMap<>();

    public TrieNodeBuilder addWord(String word)
    {
        doAddWord(CharBuffer.wrap(word));
        return this;
    }

    /**
     * Add a word
     *
     * <p>Here also, a {@link CharBuffer} is used, which changes position as we
     * progress into building the tree, character by character, node by node.
     * </p>
     *
     * <p>If the buffer is "empty" when entering this method, it means a match
     * must be recorded (see {@link #fullWord}).</p>
     *
     * @param buffer the buffer (never null)
     */
    private void doAddWord(CharBuffer buffer)
    {
        if (!buffer.hasRemaining()) {
            fullWord = true;
            return;
        }

        char c = buffer.get();
        TrieNodeBuilder builder = subnodes.get(c);
        if (builder == null) {
            builder = new TrieNodeBuilder();
            subnodes.put(c, builder);
        }
        builder.doAddWord(buffer);
    }

    public TrieNode build()
    {
        char[] nextChars = new char[subnodes.size()];
        TrieNode[] nextNodes = new TrieNode[subnodes.size()];

        int index = 0;
        for (Map.Entry<Character, TrieNodeBuilder> entry:
            subnodes.entrySet()) {
            nextChars[index] = entry.getKey();
            nextNodes[index] = entry.getValue().build();
            index++;
        }
        return new TrieNode(fullWord, nextChars, nextNodes);
    }
}
