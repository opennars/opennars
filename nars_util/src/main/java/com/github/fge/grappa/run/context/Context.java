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

package com.github.fge.grappa.run.context;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.stack.ValueStack;
import com.github.fge.grappa.support.IndexRange;
import com.github.fge.grappa.support.Position;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Context object is available to parser actions methods during their runtime
 * and provides various support functionalities.
 */
public interface Context<V>
{
    /**
     * Returns the InputBuffer the parser is currently running against
     *
     * @return the InputBuffer
     */
    @Nonnull
    InputBuffer getInputBuffer();

    /**
     * Returns the Matcher of this context or null, if this context is not valid
     * anymore.
     *
     * @return the matcher
     */
    @Nullable
    Matcher getMatcher();

    /**
     * Returns the index into the underlying input buffer where the matcher of
     * this context started its match.
     *
     * @return the start index
     */
    // TODO: only used from one matcher: ActionMatcher -- again
    int getStartIndex();

    /**
     * Returns the current index in the input buffer.
     *
     * @return the current index
     */
    int getCurrentIndex();

    /**
     * Returns the character at the current index..
     *
     * @return the current character
     */
    char getCurrentChar();

    /**
     * Return the code point at the current index
     *
     * @return the code point, or -1 if end of buffer
     *
     * @see InputBuffer#codePointAt(int)
     */
    int getCurrentCodePoint();

    /**
     * Returns the current matcher level, with 0 being the root level, 1 being
     * one level below the root and so on.
     *
     * @return the current matcher level
     */
    int getLevel();

    /**
     * Determines if the current rule is running somewhere underneath a
     * Test/TestNot rule.
     *
     * @return true if the current context has a parent which corresponds to a
     * Test/TestNot rule
     */
    boolean inPredicate();

    /**
     * Determines if this context or any sub node recorded a parse error.
     *
     * @return true if this context or any sub node recorded a parse error
     */
    boolean hasError();

    /**
     * <p>Returns the input text matched by the rule immediately preceding the
     * action expression that is currently being evaluated. This call can only
     * be used in actions that are part of a Sequence rule and are not at first
     * position in this Sequence.</p>
     *
     * @return the input text matched by the immediately preceding subcontext
     */
    String getMatch();

    /**
     * <p>Returns the first character of the input text matched by the rule
     * immediately preceding the action expression that is currently being
     * evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.</p>
     *
     * <p>If the immediately preceding rule did not match anything this method
     * throws a GrammarException. If you need to able to handle that case use
     * the getMatch() method.</p>
     *
     * @return the input text matched by the immediately preceding subcontext
     */
    char getFirstMatchChar();

    /**
     * <p>Returns the start index of the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used
     * in actions that are part of a Sequence rule and are not at first position
     * in this Sequence.</p>
     *
     * @return the start index of the context immediately preceding current
     * action
     */
    int getMatchStartIndex();

    /**
     * <p>Returns the end index of the rule immediately preceding the action
     * expression that is currently being evaluated. This call can only be used
     * in actions that are part of a Sequence rule and are not at first position
     * in this Sequence.</p>
     *
     * @return the end index of the context immediately preceding current
     * action, i.e. the index of the character immediately following the last
     * matched character
     */
    int getMatchEndIndex();

    /**
     * <p>Returns the number of characters matched by the rule immediately
     * preceding the action expression that is currently being evaluated. This
     * call can only be used in actions that are part of a Sequence rule and are
     * not at first position in this Sequence.</p>
     *
     * @return the number of characters matched
     */
    int getMatchLength();

    /**
     * <p>Returns the current position in the underlying {@link InputBuffer} as
     * a {@link Position} instance.</p>
     *
     * @return the current position in the underlying inputbuffer
     */
    Position getPosition();

    /**
     * Creates a new {@link IndexRange} instance covering the input text matched
     * by the rule immediately preceding the action expression that is currently
     * being evaluated. This call can only be used in actions that are part of a
     * Sequence rule and are not at first position in this Sequence.
     *
     * @return a new IndexRange instance
     */
    IndexRange getMatchRange();

    /**
     * Returns the value stack instance used during this parsing run.
     *
     * @return the value stack
     */
    ValueStack<V> getValueStack();


}

