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

package com.github.fge.grappa.support;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A simple immutable container for a range of indices into an underlying
 * InputBuffer.
 */
public final class IndexRange
{
    /**
     * The index of the first character in the range.
     */
    public final int start;

    /**
     * The index of the character following the last character of the range.
     */
    public final int end;

    public IndexRange(int start, int end)
    {
        Preconditions.checkArgument(start >= 0, "start must be >= 0");
        Preconditions.checkArgument(end >= start, "end must be >= start");
        this.start = start;
        this.end = end;
    }

    /**
     * Determines whether this range contains no characters.
     *
     * @return true if the end matches the start of the range.
     */
    public boolean isEmpty()
    {
        return start == end;
    }

    /**
     * @return the number of characters covered by this range
     */
    public int length()
    {
        return end - start;
    }

    /**
     * Determines whether this range overlaps with the given other one.
     *
     * @param other the other range
     * @return true if there is at least one index that is contained in both ranges
     */
    public boolean overlapsWith(IndexRange other)
    {
        Objects.requireNonNull(other, "other");
        return end > other.start && other.end > start;
    }

    /**
     * Determines whether this range immediated follows the given other one.
     *
     * @param other the other range
     * @return true if this range immediated follows the given other one
     */
    public boolean isPrecededBy(IndexRange other)
    {
        Objects.requireNonNull(other, "other");
        return other.end == start;
    }

    /**
     * Determines whether this range is immediated followed by the given other one.
     *
     * @param other the other range
     * @return true if this range is immediated followed by the given other one
     */
    public boolean isFollowedBy(IndexRange other)
    {
        Objects.requireNonNull(other, "other");
        return end == other.start;
    }

    /**
     * Determines whether this range immediated follows or precedes the given other one.
     *
     * @param other the other range
     * @return true if this range immediated follows or precedes the given other one.
     */
    public boolean touches(IndexRange other)
    {
        Objects.requireNonNull(other, "other");
        return other.end == start || end == other.start;
    }

    /**
     * Created a new IndexRange that spans all characters between the smallest and the highest index of the two ranges.
     *
     * @param other the other range
     * @return a new IndexRange instance
     */
    public IndexRange mergedWith(IndexRange other)
    {
        Objects.requireNonNull(other, "other");
        return new IndexRange(Math.min(start, other.start),
            Math.max(end, other.end));
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof IndexRange))
            return false;
        IndexRange that = (IndexRange) obj;
        return end == that.end && start == that.start;
    }

    @Override
    public int hashCode()
    {
        return start ^ end;
    }

    @Nonnull
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
            .add("start", start)
            .add("end", end)
            .toString();
    }
}
