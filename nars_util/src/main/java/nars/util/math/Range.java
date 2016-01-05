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
//import nars.util.data.ThreadSafe;
//
//import java.io.Serializable;
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
//import java.util.Iterator;
//import java.util.Locale;
//import java.util.NoSuchElementException;
//
///**
// *
// *
// *
// * @author The Stajistics Project
// */
//@ThreadSafe @Deprecated
//public class Range implements Iterable<Double>, Serializable {
//
//    protected static final boolean DEFAULT_EXCLUSIVE_RANGE_END = true;
//
//    private static final DecimalFormat DECIMAL_FORMAT;
//
//    static {
//        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
//        dfs.setDecimalSeparator('.');
//        DECIMAL_FORMAT = new DecimalFormat("0.###", dfs);
//        DECIMAL_FORMAT.setGroupingSize(Byte.MAX_VALUE);
//    }
//
//    private final double begin;
//    private final double end;
//
//    private final String name;
//
//    public Range(final double begin,
//            final double end) {
//        this(begin, end, null);
//    }
//
//    public Range(final double begin,
//            final double end,
//            final String name) {
//
//        if (begin > end) {
//            throw new IllegalArgumentException("begin must be <= end");
//        }
//
//        this.begin = begin;
//        this.end = end;
//
//        if (name == null) {
//            this.name = defaultName(begin, end);
//        } else {
//            this.name = name;
//        }
//    }
//
//    public static String defaultName(final double begin,
//            final double end) {
//        StringBuilder buf = new StringBuilder(32);
//        buf.append(DECIMAL_FORMAT.format(begin));
//        buf.append("..");
//        buf.append(DECIMAL_FORMAT.format(end));
//
//        return buf.toString();
//    }
//
//    public double getBegin() {
//        return begin;
//    }
//
//    public double getEnd() {
//        return end;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public boolean contains(final double value) {
//        return contains(value, DEFAULT_EXCLUSIVE_RANGE_END);
//    }
//
//    public boolean contains(final double value,
//            final boolean exclusiveRangeEnd) {
//        if (value < begin) {
//            return false;
//        }
//
//        if (exclusiveRangeEnd) {
//            if (value >= end) {
//                return false;
//            }
//        } else {
//            if (value > end) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    public boolean contains(final Range range,
//            final boolean exclusiveRangeEnd) {
//        if (begin <= range.begin) {
//            if (exclusiveRangeEnd) {
//                return range.end < end;
//            } else {
//                return range.end <= end;
//            }
//        }
//
//        return false;
//    }
//
//    public boolean overlaps(final Range other) {
//        return overlaps(other, DEFAULT_EXCLUSIVE_RANGE_END);
//    }
//
//    public boolean overlaps(final Range other,
//            final boolean exclusiveRangeEnd) {
//        if (begin <= other.begin) {
//            if (exclusiveRangeEnd) {
//                return other.begin < end;
//            } else {
//                return other.begin <= end;
//            }
//        } else if (exclusiveRangeEnd) {
//            if (begin < other.end) {
//                return other.end < end;
//            }
//        } else {
//            if (begin <= other.end) {
//                return other.end <= end;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public Iterator<Double> iterator() {
//        return iterator(1, DEFAULT_EXCLUSIVE_RANGE_END);
//    }
//
//    public Iterator<Double> iterator(final double increment,
//            final boolean exclusiveRangeEnd) {
//        return new RangeItr(begin, end, increment, exclusiveRangeEnd);
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        return (obj instanceof Range) && equals((Range) obj);
//    }
//
//    public boolean equals(final Range other) {
//        if (other == null) {
//            return false;
//        }
//
//        if (Double.doubleToLongBits(begin) != Double.doubleToLongBits(other.begin)) {
//            return false;
//        }
//
//        if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end)) {
//            return false;
//        }
//
//        return name.equals(other.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return (int) Double.doubleToLongBits(begin)
//                ^ (int) Double.doubleToLongBits(end)
//                ^ name.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return getName();
//    }
//
//    /* NESTED CLASSES */
//    protected static class RangeItr implements Iterator<Double> {
//
//        private final double increment;
//        private final double end;
//        private final boolean exclusiveRangeEnd;
//
//        private double current;
//
//        protected RangeItr(final double begin,
//                final double end,
//                final double increment,
//                final boolean exclusiveRangeEnd) {
//            this.current = begin - increment;
//            this.end = end;
//            this.increment = increment;
//            this.exclusiveRangeEnd = exclusiveRangeEnd;
//        }
//
//        @Override
//        public boolean hasNext() {
//            if (exclusiveRangeEnd) {
//                return (current + increment) < end;
//            } else {
//                return (current + increment) <= end;
//            }
//        }
//
//        @Override
//        public Double next() {
//            double val = current + increment;
//
//            if (exclusiveRangeEnd) {
//                if (val >= end) {
//                    throw new NoSuchElementException();
//                }
//            } else {
//                if (val > end) {
//                    throw new NoSuchElementException();
//                }
//            }
//
//            current += increment;
//
//            return val;
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//    }
// }
