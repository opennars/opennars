///* Copyright 2009 - 2010 The Stajistics Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package nars.util.math;
//
//import com.google.common.collect.Iterators;
//import nars.util.data.ThreadSafe;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//
///**
// *
// *
// *
// * @author The Stajistics Project
// */
//@ThreadSafe
//public class RangeList implements Iterable<Range>, Serializable {
//
//    private final Range[] ranges;
//
//    private final boolean exclusiveRangeEnd;
//    private final boolean hasOverlap;
//
//    private final double minBegin;
//    private final double maxEnd;
//
//    public RangeList(final Range... ranges) {
//        this(Arrays.asList(ranges));
//    }
//
//    public RangeList(final List<Range> ranges) {
//        this(ranges, Range.DEFAULT_EXCLUSIVE_RANGE_END);
//    }
//
//    public RangeList(final List<Range> ranges,
//            final boolean exclusiveRangeEnd) {
//
//        this.ranges = ranges.toArray(new Range[ranges.size()]);
//        this.exclusiveRangeEnd = exclusiveRangeEnd;
//
//        this.hasOverlap = calcHasOverlap();
//        this.minBegin = calcMinBegin();
//        this.maxEnd = calcMaxEnd();
//    }
//
//    public static Builder build() {
//        return new Builder();
//    }
//
//    protected boolean calcHasOverlap() {
//        for (int i = 0; i < ranges.length; i++) {
//            for (int j = i; j < ranges.length; j++) {
//                if (i == j) {
//                    continue;
//                }
//
//                if (ranges[i].overlaps(ranges[j], exclusiveRangeEnd)) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    protected double calcMinBegin() {
//        if (ranges.length == 0) return Double.POSITIVE_INFINITY;
//        double min = ranges[0].getBegin();
//
//        for (int i = 1; i < ranges.length; i++) {
//            double tmp = ranges[i].getBegin();
//            if (tmp < min) {
//                min = tmp;
//            }
//        }
//
//        return min;
//    }
//
//    protected double calcMaxEnd() {
//        if (ranges.length == 0) return Double.NEGATIVE_INFINITY;
//        double max = ranges[0].getEnd();
//
//        double tmp;
//        for (int i = 1; i < ranges.length; i++) {
//            tmp = ranges[i].getEnd();
//            if (tmp > max) {
//                max = tmp;
//            }
//        }
//
//        return max;
//    }
//
//    public boolean isExclusiveRangeEnd() {
//        return exclusiveRangeEnd;
//    }
//
//    public boolean hasOverlap() {
//        return hasOverlap;
//    }
//
//    public double getMinBegin() {
//        return minBegin;
//    }
//
//    public double getMaxEnd() {
//        return maxEnd;
//    }
//
//    public int size() {
//        return ranges.length;
//    }
//
//    public List<Range> getRanges() {
//        return Arrays.asList(ranges);
//    }
//
//    @Override
//    public Iterator<Range> iterator() {
//        return Iterators.forArray(ranges);
//    }
//
//    public int indexOfRangeContaining(final double value) {
//        return indexOfRangeContaining(value, 0);
//    }
//
//    public int indexOfRangeContaining(final double value,
//            int fromIndex) {
//        if (value < minBegin || value > maxEnd || (exclusiveRangeEnd && value == maxEnd)) {
//            return -1;
//        }
//
//        final int rangeCount = ranges.length;
//
//        if (fromIndex < 0) {
//            fromIndex = 0;
//
//        } else if (fromIndex >= rangeCount) {
//            return -1;
//        }
//
//        for (int i = fromIndex; i < rangeCount; i++) {
//            if (ranges[i].contains(value, exclusiveRangeEnd)) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    public Range rangeContaining(final double value) {
//        return rangeContaining(value, 0);
//    }
//
//    public Range rangeContaining(final double value,
//            final int fromIndex) {
//        Range range = null;
//
//        int index = indexOfRangeContaining(value, fromIndex);
//        if (index > -1) {
//            range = ranges[index];
//        }
//
//        return range;
//    }
//
//    public boolean contains(final double value) {
//        if (value < minBegin || value > maxEnd || (exclusiveRangeEnd && value == maxEnd)) {
//            return false;
//        }
//
//        final int rangeCount = ranges.length;
//        for (int i = 0; i < rangeCount; i++) {
//            if (ranges[i].contains(value, exclusiveRangeEnd)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        return (obj instanceof RangeList) && equals((RangeList) obj);
//    }
//
//    public boolean equals(final RangeList other) {
//        return getRanges().equals(other.getRanges());
//    }
//
//    @Override
//    public int hashCode() {
//        return getRanges().hashCode();
//    }
//
//    /* NESTED CLASSES */
//    public static class Builder {
//
//        private final List<Range> ranges = new ArrayList<>();
//        private boolean exclusiveRangeEnd = Range.DEFAULT_EXCLUSIVE_RANGE_END;
//
//        private boolean beginningRangeAdded = false;
//        private boolean endingRangeAdded = false;
//
//        private Builder() {
//        }
//
//        public Builder addRange(final double begin,
//                final double end) {
//            return addRange(begin, end, null);
//        }
//
//        public Builder addRange(final double begin,
//                final double end,
//                final String name) {
//            ranges.add(new Range(begin, end, name));
//            return this;
//        }
//
//        public Builder addBeginningRange(final double end) {
//            return addBeginningRange(end, null);
//        }
//
//        public Builder addBeginningRange(final double end,
//                final String name) {
//            if (beginningRangeAdded) {
//                throw new IllegalStateException("beginning range already added");
//            }
//
//            beginningRangeAdded = true;
//
//            return addRange(Double.NEGATIVE_INFINITY, end, name);
//        }
//
//        public Builder addEndingRange(final double begin) {
//            return addEndingRange(begin, null);
//        }
//
//        public Builder addEndingRange(final double begin,
//                final String name) {
//            if (endingRangeAdded) {
//                throw new IllegalStateException("ending range already added");
//            }
//
//            endingRangeAdded = true;
//
//            return addRange(begin, Double.POSITIVE_INFINITY, name);
//        }
//
//        public Builder withExclusiveRangeEnd(final boolean exclusiveRangeEnd) {
//            this.exclusiveRangeEnd = exclusiveRangeEnd;
//            return this;
//        }
//
//        public RangeList rangeList() {
//            return new RangeList(ranges, exclusiveRangeEnd);
//        }
//    }
// }
