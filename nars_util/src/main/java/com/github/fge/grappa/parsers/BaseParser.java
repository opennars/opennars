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

package com.github.fge.grappa.parsers;

import com.github.fge.grappa.annotations.Cached;
import com.github.fge.grappa.annotations.DontExtend;
import com.github.fge.grappa.annotations.DontLabel;
import com.github.fge.grappa.annotations.SkipActionsInPredicates;
import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.matchers.*;
import com.github.fge.grappa.matchers.delegate.*;
import com.github.fge.grappa.matchers.join.JoinMatcherBootstrap;
import com.github.fge.grappa.matchers.join.JoinMatcherBuilder;
import com.github.fge.grappa.matchers.predicates.TestMatcher;
import com.github.fge.grappa.matchers.predicates.TestNotMatcher;
import com.github.fge.grappa.matchers.trie.*;
import com.github.fge.grappa.matchers.unicode.CodePointMatcher;
import com.github.fge.grappa.matchers.unicode.CodePointRangeMatcher;
import com.github.fge.grappa.rules.Action;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.support.Characters;
import com.github.fge.grappa.support.Chars;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gs.collections.impl.list.mutable.FastList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Base class of all parboiled parsers. Defines the basic rule creation methods.
 *
 * @param <V> the type of the parser values
 */
@ParametersAreNonnullByDefault
public abstract class BaseParser<V>
    extends BaseActions<V>
{
    /**
     * End of input rule
     *
     * <p>Please don't use this directly; use {@link #eof()} instead.</p>
     */
    protected static final Rule EOI = new EndOfInputMatcher();

    /**
     * Matches any character except {@link Chars#EOI}.
     */
    protected static final Rule ANY = new AnyMatcher();

    /**
     * Matches nothing and always succeeds.
     */
    protected static final Rule EMPTY = new EmptyMatcher();

    /**
     * Matches nothing and always fails.
     */
    protected static final Rule NOTHING = new NothingMatcher();

    /*
     * CORE RULES
     */

    /**
     * Match only at the end of input
     *
     * @return a rule
     */
    public Rule eof()
    {
        return EOI;
    }

    /**
     * Match one given character
     *
     * @param c the character to match
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule ch(final char c)
    {
        return new CharMatcher(c);
    }

    /**
     * Match a given character in a case-insensitive manner
     *
     * @param c the character to match
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule ignoreCase(final char c)
    {
        return Character.isLowerCase(c) == Character.isUpperCase(c)
            ? ch(c) : new CharIgnoreCaseMatcher(c);
    }

    /**
     * Match one Unicode character
     *
     * @param codePoint the code point
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule unicodeChar(final int codePoint)
    {
        if (!Character.isValidCodePoint(codePoint))
            throw new InvalidGrammarException("invalid code point "
                + codePoint);
        return new CodePointMatcher(codePoint);
    }

    /**
     * Match a Unicode character range
     *
     * <p>Note that this method will delegate to "regular" character matchers if
     * part of, or all of, the specified range is into the basic multilingual
     * plane.</p>
     *
     * @param low the lower code point (inclusive)
     * @param high the upper code point (inclusive)
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule unicodeRange(final int low, final int high)
    {
        if (!Character.isValidCodePoint(low))
            throw new InvalidGrammarException("invalid code point " + low);
        if (!Character.isValidCodePoint(high))
            throw new InvalidGrammarException("invalid code point " + high);
        if (low > high)
            throw new InvalidGrammarException("invalid code point range: "
                + low + " > " + high);
        return low == high ? new CodePointMatcher(low)
            : new CodePointRangeMatcher(low, high);
    }

    /**
     * Match an inclusive range of {@code char}s
     *
     * @param cLow the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule charRange(final char cLow, final char cHigh)
    {
        return cLow == cHigh ? ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Match any of the characters in the given string
     *
     * <p>This method delegates to {@link #anyOf(Characters)}.</p>
     *
     * @param characters the characters
     * @return a rule
     *
     * @see #anyOf(Characters)
     */
    @DontLabel
    public Rule anyOf(final String characters)
    {
        Objects.requireNonNull(characters);
        // TODO: see in this Characters class whether it is possible to wrap
        return anyOf(characters.toCharArray());
    }

    /**
     * Match any character in the given {@code char} array
     *
     * <p>This method delegates to {@link #anyOf(Characters)}.</p>
     *
     * @param characters the characters
     * @return a rule
     *
     * @see #anyOf(Characters)
     */
    @DontLabel
    public Rule anyOf(final char[] characters)
    {
        Objects.requireNonNull(characters);
        Preconditions.checkArgument(characters.length > 0);
        return characters.length == 1 ? ch(characters[0])
            : anyOf(Characters.of(characters));
    }

    /**
     * Match any given character among a set of characters
     *
     * <p>Both {@link #anyOf(char[])} and {@link #anyOf(String)} ultimately
     * delegate to this method, which caches its resuls.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule anyOf(final Characters characters)
    {
        Objects.requireNonNull(characters);
        if (!characters.isSubtractive() && characters.getChars().length == 1)
            return ch(characters.getChars()[0]);
        if (characters.equals(Characters.NONE))
            return NOTHING;
        return new AnyOfMatcher(characters);
    }

    /**
     * Match any characters <em>except</em> the ones contained in the strings
     *
     * @param characters the characters
     * @return a rule
     */
    @DontLabel
    public Rule noneOf(final String characters)
    {
        Objects.requireNonNull(characters);
        return noneOf(characters.toCharArray());
    }

    /**
     * Match all characters <em>except</em> the ones in the {@code char} array
     * given as an argument
     *
     * @param characters the characters
     * @return a new rule
     */
    @DontLabel
    public Rule noneOf(char[] characters)
    {
        Objects.requireNonNull(characters);
        Preconditions.checkArgument(characters.length > 0);

        // make sure to always exclude EOI as well
        boolean containsEOI = false;
        for (final char c: characters)
            if (c == Chars.EOI) {
                containsEOI = true;
                break;
            }
        if (!containsEOI) {
            final char[] withEOI = new char[characters.length + 1];
            System.arraycopy(characters, 0, withEOI, 0, characters.length);
            withEOI[characters.length] = Chars.EOI;
            characters = withEOI;
        }

        return anyOf(Characters.allBut(characters));
    }

    /**
     * Match a string literal
     *
     * @param string the string to match
     * @return a rule
     */
    @DontLabel
    public Rule string(final String string)
    {
        Objects.requireNonNull(string);
        return string(string.toCharArray());
    }

    /**
     * Match a given set of characters as a string literal
     *
     * @param characters the characters of the string to match
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule string(final char... characters)
    {
        if (characters.length == 1)
            return ch(characters[0]); // optimize one-char strings
        return new StringMatcher(new String(characters));
    }

    /**
     * Match a string literal in a case insensitive manner
     *
     * @param string the string to match
     * @return a rule
     */
    @DontLabel
    public Rule ignoreCase(final String string)
    {
        Objects.requireNonNull(string);
        return ignoreCase(string.toCharArray());
    }

    /**
     * Match a sequence of characters as a string literal (case insensitive)
     *
     * @param characters the characters of the string to match
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule ignoreCase(final char... characters)
    {
        if (characters.length == 1)
            return ignoreCase(characters[0]); // optimize one-char strings
        return new StringIgnoreCaseMatcher(new String(characters));
    }

    /**
     * Match one string among many using a <a
     * href="http://en.wikipedia.org/wiki/Trie" target="_blank">trie</a>
     *
     * <p>Duplicate elements will be silently eliminated.</p>
     *
     * <p>Note that order of elements does not matter, and that this rule will
     * always trie (err, try) and match the <em>longest possible sequence</em>.
     * That is, if you build a rule with inputs "do" and "double" in this order
     * and the input text is "doubles", then "double" will be matched. However,
     * if the input text is "doubling" then "do" is matched instead.</p>
     *
     * <p>Note also that the minimum length of strings in a trie is 2.</p>
     *
     * @param strings the list of strings for this trie
     * @return a rule
     *
     * @see TrieMatcher
     * @see TrieNode
     */
    // TODO: potentially a slew of strings in a trie; so maybe it's not a good
    // idea to cache here
    @Cached
    public Rule trie(final Collection<String> strings)
    {
        final List<String> list = /*ImmutableList.copyOf*/new FastList(strings);

        final TrieBuilder builder = Trie.newBuilder();

        for (final String word: list)
            builder.addWord(word);

        return new TrieMatcher(builder.build());
    }

    /**
     * Match one string among many using a <a
     * href="http://en.wikipedia.org/wiki/Trie" target="_blank">trie</a>
     *
     * <p>This method delegates to {@link #trie(Collection)}.</p>
     *
     * @param first the first string
     * @param second the second string
     * @param others other strings
     * @return a rule
     *
     * @see TrieMatcher
     * @see TrieNode
     */
    public Rule trie(final String first, final String second,
        final String... others)
    {
        final List<String> words = ImmutableList.<String>builder().add(first)
            .add(second).add(others).build();

        return trie(words);
    }

    /**
     * Match one string among many using a <a
     * href="http://en.wikipedia.org/wiki/Trie" target="_blank">trie</a>, case
     * insensitive version
     *
     * <p>Duplicate elements will be silently eliminated.</p>
     *
     * <p>Note that order of elements does not matter, and that this rule will
     * always trie (err, try) and match the <em>longest possible sequence</em>.
     * That is, if you build a rule with inputs "do" and "double" in this order
     * and the input text is "doubles", then "double" will be matched. However,
     * if the input text is "doubling" then "do" is matched instead.</p>
     *
     * <p>Note also that the minimum length of strings in a trie is 2.</p>
     *
     * @param strings the list of strings for this trie
     * @return a rule
     *
     * @see CaseInsensitiveTrieMatcher
     */
    // TODO: potentially a slew of strings in a trie; so maybe it's not a good
    // idea to cache here
    @Cached
    public Rule trieIgnoreCase(final Collection<String> strings)
    {
        final List<String> list = ImmutableList.copyOf(strings);

        final TrieBuilder builder = Trie.newBuilder();

        for (final String word: list)
            builder.addWord(word);

        return new CaseInsensitiveTrieMatcher(builder.build());
    }

    /**
     * Match one string among many using a <a
     * href="http://en.wikipedia.org/wiki/Trie" target="_blank">trie</a>, case
     * insensitive
     *
     * <p>This method delegates to {@link #trieIgnoreCase(Collection)}.</p>
     *
     * @param first the first string
     * @param second the second string
     * @param others other strings
     * @return a rule
     *
     * @see CaseInsensitiveTrieMatcher
     */
    public Rule trieIgnoreCase(final String first, final String second,
        final String... others)
    {
        final List<String> words = ImmutableList.<String>builder().add(first)
            .add(second).add(others).build();

        return trieIgnoreCase(words);
    }

    /*
     * "DELEGATING" RULES
     *
     * All rules below delegate to one or more other rules
     */


    /**
     * Match the first rule of a series of rules
     *
     * <p>When one rule matches, all others are ignored.</p>
     *
     * <p>Note: if you are considering matching one string among many, consider
     * using {@link #trie(Collection)}/{@link #trie(String, String, String...)}
     * instead.</p>
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a rule
     */
    @DontLabel
    public Rule firstOf(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        final Object[] rules = ImmutableList.builder().add(rule).add(rule2)
            .add(moreRules).build().toArray();
        return firstOf(rules);
    }

    /**
     * Match the first rule of a series of rules
     *
     * <p>When one rule matches, all others are ignored.</p>
     *
     * @param rules the subrules
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule firstOf(final Object[] rules)
    {
        Objects.requireNonNull(rules, "rules");
        if (rules.length == 1)
            return toRule(rules[0]);

        final Collection<String> strings = new ArrayList<>();

        for (final Object object: rules) {
            if (!(object instanceof String))
                return new FirstOfMatcher(toRules(rules));
            strings.add((String) object);
        }

        return trie(strings);
    }

    /**
     * Try and match a rule repeatedly, at least once
     *
     * @param rule the subrule
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule oneOrMore(final Object rule)
    {
        return new OneOrMoreMatcher(toRule(rule));
    }

    /**
     * Try and repeatedly match a set of rules, at least once
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a rule
     */
    @DontLabel
    public Rule oneOrMore(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        return oneOrMore(sequence(rule, rule2, moreRules));
    }

    /**
     * Try and match a rule zero or one time
     *
     * <p>This rule therefore always succeeds.</p>
     *
     * @param rule the subrule
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule optional(final Object rule)
    {
        Objects.requireNonNull(rule);
        return new OptionalMatcher(toRule(rule));
    }

    /**
     * Try and match a given set of rules once
     *
     * <p>This rule will therefore never fail.</p>
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a rule
     */
    @DontLabel
    public Rule optional(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        return optional(sequence(rule, rule2, moreRules));
    }

    /**
     * Match a given set of rules, exactly once
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a rule
     */
    @DontLabel
    public Rule sequence(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        final Object[] rules = ImmutableList.builder().add(rule).add(rule2)
            .add(moreRules).build().toArray();
        return sequence(rules);
    }

    /**
     * Match a given set of rules, exactly once
     *
     * @param rules the rules
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule sequence(final Object[] rules)
    {
        Objects.requireNonNull(rules);
        return rules.length == 1 ? toRule(rules[0])
            : new SequenceMatcher(toRules(rules));
    }

    /**
     * Kickstart a {@code join} rule
     *
     * <p>Usages:</p>
     *
     * <pre>
     *     return join(rule()).using(otherRule()).times(n);
     *     return join(rule()).using(otherRule()).min(n);
     * </pre>
     *
     * <p>etc. See {@link JoinMatcherBuilder} for more possible constructs.</p>
     *
     * @param rule the joined rule (must not match an empty sequence!)
     * @return a {@link JoinMatcherBootstrap}
     *
     * @see JoinMatcherBootstrap#using(Object)
     */
    public final JoinMatcherBootstrap<V, BaseParser<V>> join(final Object rule)
    {
        return new JoinMatcherBootstrap<>(this, rule);
    }

    /*
     * PREDICATES
     */

    /**
     * Test a rule, but do not consume any input (predicate)
     *
     * <p>Its success conditions are the same as the rule. Note that this rule
     * will never consume any input, nor will it create a parse tree node.</p>
     *
     * <p>Note that the embedded rule can be arbitrarily complex, and this
     * includes potential {@link Action}s which can act on the stack for
     * instance; these <em>will</em> be executed here, unless you have chosen to
     * annotate your rule, or parser class, with {@link
     * SkipActionsInPredicates}.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule test(final Object rule)
    {
        final Rule subMatcher = toRule(rule);
        return new TestMatcher(subMatcher);
    }

    /**
     * Test a set of rules, but do not consume any input
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     *
     * @see #test(Object)
     */
    @DontLabel
    public Rule test(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        return test(sequence(rule, rule2, moreRules));
    }

    /**
     * Test, without consuming an input, that a rule does not match
     *
     * <p>The same warnings given in the description of {@link #test(Object)}
     * apply here.</p>
     *
     * @param rule the subrule
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule testNot(final Object rule)
    {
        return new TestNotMatcher(toRule(rule));
    }

    /**
     * Test that a set of rules do not apply at this position
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     *
     * @see #test(Object)
     * @see #testNot(Object)
     */
    @DontLabel
    public Rule testNot(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        return testNot(sequence(rule, rule2, moreRules));
    }

    /**
     * Try and match a rule zero or more times
     *
     * <p>The rule will therefore always succeed.</p>
     *
     * @param rule the subrule
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule zeroOrMore(final Object rule)
    {
        return new ZeroOrMoreMatcher(toRule(rule));
    }

    /**
     * Try and match a set of rules zero or more times
     *
     * @param rule the first subrule
     * @param rule2 the second subrule
     * @param moreRules the other subrules
     * @return a rule
     */
    @DontLabel
    public Rule zeroOrMore(final Object rule, final Object rule2,
        final Object... moreRules)
    {
        Objects.requireNonNull(moreRules);
        return zeroOrMore(sequence(rule, rule2, moreRules));
    }

    /**
     * Match a rule a fixed number of times
     *
     * @param repetitions The number of repetitions to match. Must be &gt;= 0.
     * @param rule the sub rule to match repeatedly.
     * @return a rule
     */
    @Cached
    @DontLabel
    public Rule nTimes(final int repetitions, final Object rule)
    {
        Objects.requireNonNull(rule);
        if (repetitions < 0)
            throw new InvalidGrammarException("illegal repetition count "
                + repetitions + ": must not be negative");

        final Rule theRule = toRule(rule);
        if (repetitions == 0)
            return EMPTY;
        if (repetitions == 1)
            return theRule;

        final Rule[] array = new Rule[repetitions];
        Arrays.fill(array, theRule);
        return sequence(array);
    }

    /*
     * UTILITY RULES
     *
     *  All rules defined by RFC 5234, appendix B, section 1
     */

    /**
     * ALPHA as defined by RFC 5234, appendix B, section 1: ASCII letters
     *
     * <p>Therefore a-z, A-Z.</p>
     *
     * @return a rule
     */
    public Rule alpha()
    {
        return firstOf(charRange('a', 'z'), charRange('A', 'Z'));
    }

    /**
     * BIT as defined by RFC 5234, appendix B, section 1: {@code 0} or {@code 1}
     *
     * @return a rule
     */
    public Rule bit()
    {
        return anyOf(Characters.of('0', '1'));
    }

    /**
     * CHAR as defined by RFC 5234, appendix B, section 1: ASCII, except NUL
     *
     * <p>That is, 0x01 to 0x7f.</p>
     *
     * @return a rule
     */
    public Rule asciiChars()
    {
        return charRange((char) 0x01, (char) 0x7f);
    }

    /**
     * CR as defined by RFC 5234, appendix B, section 1 ({@code \r})
     *
     * @return a rule
     */
    public Rule cr()
    {
        return ch('\r');
    }

    /**
     * CRLF as defined by RFC 5234, appendix B, section 1 ({@code \r\n}
     *
     * @return a rule
     */
    public Rule crlf()
    {
        return string("\r\n");
    }

    /**
     * CTL as defined by RFC 5234, appendix B, section 1: control characters
     *
     * <p>0x00-0x1f, plus 0x7f.</p>
     *
     * @return a rule
     */
    public Rule ctl()
    {
        return firstOf(charRange((char) 0x00, (char) 0x1f), ch((char) 0x7f));
    }

    /**
     * DIGIT as defined by RFC 5234, appendix B, section 1 (0 to 9)
     *
     * @return a rule
     */
    public Rule digit()
    {
        return charRange('0', '9');
    }

    /**
     * DQUOTE as defined by RFC 5234, appendix B, section 1 {@code "}
     *
     * @return a rule
     */
    public Rule dquote()
    {
        return ch('"');
    }

    /**
     * Hexadecimal digits, case insensitive
     *
     * <p><b>Note:</b> RFC 5234 only defines {@code HEXDIG} for uppercase
     * letters ({@code A} to {@code F}). Use {@link #hexDigitUpperCase()} for
     * this definition. Use {@link #hexDigitLowerCase()} for lowercase letters
     * only.</p>
     *
     * @return a rule
     */
    public Rule hexDigit()
    {
        return anyOf("ABCDEFabcdef0123456789");
    }

    /**
     * Hexadecimal digits, uppercase
     *
     * @return a rule
     * @see #hexDigit()
     */
    public Rule hexDigitUpperCase()
    {
        return anyOf("ABCDEF0123456789");
    }

    /**
     * Hexadecimal digits, lowercase
     *
     * @return a rule
     * @see #hexDigit()
     */
    public Rule hexDigitLowerCase()
    {
        return anyOf("abcdef0123456789");
    }

    /**
     * HTAB as defined by RFC 5234, appendix B, section 1 ({@code \t})
     *
     * @return a rule
     */
    public Rule hTab()
    {
        return ch('\t');
    }

    /**
     * LF as defined by RFC 5234, appendix B, section 1 ({@code \n})
     *
     * @return a rule
     */
    public Rule lf()
    {
        return ch('\n');
    }

    /**
     * OCTET as defined by RFC 5234, appendix B, section 1 (0x00 to 0xff)
     *
     * @return a rule
     */
    public Rule octet()
    {
        return charRange((char) 0x00, (char) 0xff);
    }

    /**
     * SP as defined by RFC 5234, appendix B, section 1 (one space, 0x20)
     *
     * @return a rule
     */
    public Rule sp()
    {
        return ch(' ');
    }

    /**
     * VCHAR as defined by RFC 5234, appendix B, section 1: ASCII "visible"
     *
     * <p>Letters, {@code @}, etc etc. Note that this <strong>excludes</strong>
     * whitespace characters!</p>
     *
     * @return a rule
     */
    public Rule vchar()
    {
        return charRange((char) 0x21, (char) 0x7e);
    }

    /**
     * WSP as defined by RFC 5234, appendix B, section 1: space or tab
     *
     * @return a rule
     */
    public Rule wsp()
    {
        return anyOf(" \t");
    }

    ///************************* "MAGIC" METHODS ***************************///

    /**
     * Explicitly marks the wrapped expression as an action expression.
     * parboiled transforms the wrapped expression into an {@link Action} instance during parser construction.
     *
     * @param expression the expression to turn into an Action
     * @return the Action wrapping the given expression
     */
    public static <T> Action<T> ACTION(final boolean expression)
    {
        throw new UnsupportedOperationException("ACTION(...) calls can only be"
            + " used in Rule creating parser methods");
    }

    ///************************* HELPER METHODS ***************************///

    /**
     * Used internally to convert the given character literal to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the character.
     *
     * @param c the character
     * @return the rule
     */
    @DontExtend
    protected Rule fromCharLiteral(final char c)
    {
        return ch(c);
    }

    /**
     * Used internally to convert the given string literal to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the string.
     *
     * @param string the string
     * @return the rule
     */
    @DontExtend
    protected Rule fromStringLiteral(final String string)
    {
        Objects.requireNonNull(string);
        return fromCharArray(string.toCharArray());
    }

    /**
     * Used internally to convert the given char array to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the characters.
     *
     * @param array the char array
     * @return the rule
     */
    @DontExtend
    protected Rule fromCharArray(final char[] array)
    {
        Objects.requireNonNull(array);
        return string(array);
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    @DontExtend
    public Rule[] toRules(final Object... objects)
    {
        final Rule[] rules = new Rule[objects.length];

        for (int i = 0; i < objects.length; i++)
            rules[i] = toRule(objects[i]);
        return rules;
    }

    /**
     * Converts the given object to a rule.
     * This method can be overriden to enable the use of custom objects directly
     * in rule specifications.
     *
     * @param obj the object to convert
     * @return the rule corresponding to the given object
     */
    @DontExtend
    public Rule toRule(final Object obj)
    {
        Objects.requireNonNull(obj);
        if (obj instanceof Rule)
            return (Rule) obj;
        if (obj instanceof Character)
            return fromCharLiteral((Character) obj);
        if (obj instanceof String)
            return fromStringLiteral((String) obj);
        if (obj instanceof char[])
            return fromCharArray((char[]) obj);
        if (obj instanceof Action) {
            final Action<?> action = (Action<?>) obj;
            return new ActionMatcher(action);
        }

        final String errmsg = obj instanceof  Boolean
            ? "unwrapped Boolean value in rule (wrap it with ACTION())"
            : "'" + obj + "' cannot be automatically converted to a rule";
        throw new InvalidGrammarException(errmsg);
    }
}
