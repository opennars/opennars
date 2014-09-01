/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A wrapper to {@link AtomicLong} that performs the appropriate long/double
 * conversion.
 *
 * @author The Stajistics Project
 */
@ThreadSafe
public class AtomicDouble extends Number {

    private final AtomicLong value = new AtomicLong();

    public AtomicDouble() {
        set(0);
    }

    public AtomicDouble(final double value) {
        set(value);
    }

    public double get() {
        return Double.longBitsToDouble(value.get());
    }

    public void set(final double value) {
        this.value.set(Double.doubleToLongBits(value));
    }

    public void lazySet(final double value) {
        this.value.lazySet(Double.doubleToLongBits(value));
    }

    public double addAndGet(final double delta) {

        double newDoubleValue;

        for (;;) {

            long oldLongValue = this.value.get();
            double oldDoubleValue = Double.longBitsToDouble(oldLongValue);

            newDoubleValue = oldDoubleValue + delta;
            long newLongValue = Double.doubleToLongBits(newDoubleValue);

            if (this.value.compareAndSet(oldLongValue, newLongValue)) {
                break;
            }
        }

        return newDoubleValue;
    }

    public boolean compareAndSet(final double expect,
            final double update) {
        return this.value.compareAndSet(Double.doubleToLongBits(expect),
                Double.doubleToLongBits(update));
    }

    public double incrementAndGet() {
        return addAndGet(1);
    }

    public double decrementAndGet() {
        return addAndGet(-1);
    }

    public double getAndAdd(final double value) {
        double oldDoubleValue;

        for (;;) {
            long oldLongValue = this.value.get();
            oldDoubleValue = Double.longBitsToDouble(oldLongValue);

            double newDoubleValue = oldDoubleValue + value;
            long newLongValue = Double.doubleToLongBits(newDoubleValue);

            if (this.value.compareAndSet(oldLongValue, newLongValue)) {
                break;
            }
        }

        return oldDoubleValue;
    }

    public double getAndIncrement() {
        return getAndAdd(1);
    }

    public double getAndDecrement() {
        return getAndAdd(-1);
    }

    public double getAndSet(final double newValue) {
        return Double.longBitsToDouble(this.value.getAndSet(Double.doubleToLongBits(newValue)));
    }

    @Override
    public double doubleValue() {
        return get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public String toString() {
        return Double.toString(get());
    }
}
