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

import com.github.fge.grappa.annotations.Cached;
import com.github.fge.grappa.matchers.EmptyMatcher;
import com.github.fge.grappa.matchers.delegate.OptionalMatcher;
import com.github.fge.grappa.rules.Rule;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * The final step to building a {@link JoinMatcher}
 *
 * <p>At this step of the build, we have both rules (the "joined" rule and the
 * "joining" rule). The final information to feed to the matcher is the number
 * of cycles.</p>
 *
 * <p>The number of cycles can be bounded on the lower end and on the upper end.
 * The "true" building method is {@link #range(Range)}; all other methods
 * ultimately call this one to generate the result.</p>
 *
 * <p>The real matcher generated depends on the number of cycles required (for
 * the notation used here, see the javadoc for {@link Range}):</p>
 *
 * <ul>
 *     <li>[0..0]: an {@link EmptyMatcher};</li>
 *     <li>[0..1]: an {@link OptionalMatcher} with the joined rule as a
 *     submatcher;</li>
 *     <li>[1..1]: the "joined" rule itself;</li>
 *     <li>[n..+∞) for whatever n: a {@link BoundedDownJoinMatcher};</li>
 *     <li>[0..n] for n &gt;= 2: a {@link BoundedUpJoinMatcher};</li>
 *     <li>[n..n] for n &gt;= 2: an {@link ExactMatchesJoinMatcher};</li>
 *     <li>[n..m]: a {@link BoundedBothJoinMatcher}.</li>
 * </ul>
 *
 * @see JoinMatcher
 * @see Range
 */
@ParametersAreNonnullByDefault
@Beta
public final class JoinMatcherBuilder
{
    private static final Range<Integer> AT_LEAST_ZERO = Range.atLeast(0);

    private final Rule joined;
    private final Rule joining;

    JoinMatcherBuilder(Rule joined, Rule joining)
    {
        this.joined = joined;
        this.joining = joining;
    }

    /**
     * Return a rule with a minimum number of cycles to run
     *
     * @param nrCycles the number of cycles
     * @return a rule
     * @throws IllegalArgumentException {@code nrCycles} is less than 0
     *
     * @see Range#atLeast(Comparable)
     */
    public Rule min(int nrCycles)
    {
        Preconditions.checkArgument(nrCycles >= 0,
            "illegal repetition number specified (" + nrCycles
            + "), must be 0 or greater");
        return range(Range.atLeast(nrCycles));
    }

    /**
     * Return a rule with a maximum number of cycles to run
     *
     * @param nrCycles the number of cycles
     * @return a rule
     * @throws IllegalArgumentException {@code nrCycles} is less than 0
     *
     * @see Range#atMost(Comparable)
     */
    public Rule max(int nrCycles)
    {
        Preconditions.checkArgument(nrCycles >= 0,
            "illegal repetition number specified (" + nrCycles
            + "), must be 0 or greater");
        return range(Range.atMost(nrCycles));
    }

    /**
     * Return a rule with an exact number of cycles to run
     *
     * @param nrCycles the number of cycles
     * @return a rule
     * @throws IllegalArgumentException {@code nrCycles} is less than 0
     *
     * @see Range#singleton(Comparable)
     */
    public Rule times(int nrCycles)
    {
        Preconditions.checkArgument(nrCycles >= 0,
            "illegal repetition number specified (" + nrCycles
                + "), must be 0 or greater");
        return range(Range.singleton(nrCycles));
    }

    /**
     * Return a rule with both lower and upper bounds on the number of cycles
     *
     * <p>Note that the range of cycles to run is closed on both ends (that is,
     * the minimum and maximum number of cycles are inclusive).</p>
     *
     * <p>Note also that the rule <strong>will not</strong> fail if there are
     * more than the maximum number of cycles; it will simply stop matching if
     * this number of cycles is reached.</p>
     *
     * @param minCycles the minimum number of cycles
     * @param maxCycles the maximum number of cycles
     * @return a rule
     * @throws IllegalArgumentException minimum number of cycles is negative; or
     * maximum number of cycles is less than the minimum
     *
     * @see Range#closed(Comparable, Comparable)
     */
    public Rule times(int minCycles, int maxCycles)
    {
        Preconditions.checkArgument(minCycles >= 0,
            "illegal repetition number specified (" + minCycles
                + "), must be 0 or greater");
        Preconditions.checkArgument(maxCycles >= minCycles,
            "illegal range specified (" + minCycles + ", " + maxCycles
            + "): maximum must be greater than minimum");
        return range(Range.closed(minCycles, maxCycles));
    }

    /**
     * Generic method to build a {@link JoinMatcher}
     *
     * <p>You can use this method directly; note however that the range you will
     * pass as an argument will be {@link Range#intersection(Range) intersected}
     * with {@code Range.atLeast(0)}; if the result of the intersection is an
     * {@link Range#isEmpty() empty range}, this is an error condition.</p>
     *
     * <p>Ranges which are {@link BoundType#OPEN open} on any end will be turned
     * to closed range using {@link Range#canonical(DiscreteDomain)}.</p>
     *
     * @param range the range (must not be null)
     * @return a rule
     * @throws IllegalArgumentException see description
     *
     * @see Range#canonical(DiscreteDomain)
     */
    // TODO: check that it actually has an effect
    @Cached
    public Rule range(@Nonnull Range<Integer> range)
    {
        Objects.requireNonNull(range, "range must not be null");
        /*
         * We always intersect with that range...
         */
        Range<Integer> realRange = AT_LEAST_ZERO.intersection(range);

        /*
         * Empty ranges not allowed (what are we supposed to do with that
         * anyway?)
         */
        Preconditions.checkArgument(!realRange.isEmpty(), "illegal range "
            + range + ": should not be empty after intersection with "
            + AT_LEAST_ZERO);

        /*
         * Given that we intersect with AT_LEAST_ZERO, which has a lower bound,
         * the range will always have a lower bound. We want a closed range
         * internally, therefore change it if it is open.
         */
        Range<Integer> closedRange = toClosedRange(realRange);

        /*
         * We always have a lower bound
         */
        int lowerBound = closedRange.lowerEndpoint();

        /*
         * Handle the case where there is no upper bound
         */
        if (!closedRange.hasUpperBound())
            return new BoundedDownJoinMatcher(joined, joining, lowerBound);

        /*
         * There is an upper bound. Handle the case where it is 0 or 1. Since
         * the range is legal, we know that if it is 0, so is the lowerbound;
         * and if it is one, the lower bound is either 0 or 1.
         */
        int upperBound = closedRange.upperEndpoint();
        if (upperBound == 0)
            return new EmptyMatcher();
        if (upperBound == 1)
            return lowerBound == 0 ? new OptionalMatcher(joined) : joined;

        /*
         * So, upper bound is 2 or greater; return the appropriate matcher
         * according to what the lower bound is.
         *
         * Also, if the lower and upper bounds are equal, return a matcher doing
         * a fixed number of matches.
         */
        if (lowerBound == 0)
            return new BoundedUpJoinMatcher(joined, joining, upperBound);

        return lowerBound == upperBound
            ? new ExactMatchesJoinMatcher(joined, joining, lowerBound)
            : new BoundedBothJoinMatcher(joined, joining, lowerBound,
                upperBound);
    }

    private static Range<Integer> toClosedRange(Range<Integer> range)
    {
        /*
         * The canonical form will always be the same: closed on the lower bound
         * (if any; but here we are guaranteed that), open on the upper bound
         * (if any).
         *
         * All we have to do is therefore to pick the canonical representation,
         * pick the lower bound, and if it has an upper bound, pick it and
         * substract 1.
         */
        Range<Integer> canonical
            = range.canonical(DiscreteDomain.integers());
        int lowerBound = canonical.lowerEndpoint();
        return canonical.hasUpperBound()
            ? Range.closed(lowerBound, canonical.upperEndpoint() - 1)
            : Range.atLeast(lowerBound);
    }
}
