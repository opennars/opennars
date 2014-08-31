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
package nars.util.meter.recorder;

import java.util.Collections;
import java.util.Set;
import nars.util.meter.Tracker;
import nars.util.meter.data.DataSet;
import nars.util.meter.session.StatsSession;
import nars.util.meter.util.AtomicDouble;
import nars.util.meter.util.Misc;
import nars.util.meter.util.ThreadSafe;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
@ThreadSafe
public class DistributionDataRecorder implements DataRecorder {

    private static final Set<String> SUPPORTED_FIELD_NAMES =
        Collections.unmodifiableSet(Misc.getStaticFieldValues(Field.class, String.class));

    protected final AtomicDouble product = new AtomicDouble(1); // For geometric mean
    protected final AtomicDouble sumOfInverses = new AtomicDouble(0); // For harmonic mean
    protected final AtomicDouble sumOfSquares = new AtomicDouble(0); // For standard deviation and quadratic mean

    @Override
    public Set<String> getSupportedFieldNames() {
        return SUPPORTED_FIELD_NAMES;
    }

    @Override
    public void update(final StatsSession session,
                       final Tracker tracker,
                       final long now) {

        final double currentValue = tracker.getValue();
        double tmp;

        // Sum of squares (for standard deviation and quadratic mean calculation)
        sumOfSquares.addAndGet(currentValue * currentValue);

        // Product (for geometric mean calculation)
        for (;;) {
            tmp = product.get();
            double newProduct = tmp * currentValue;
            if (product.compareAndSet(tmp, newProduct)) {
                break;
            }
        }

        // Sum of inverses (for harmonic mean calculation)
        sumOfInverses.addAndGet(1D / currentValue);
    }

    @Override
    public void restore(final DataSet dataSet) {
        product.set(dataSet.getField(Field.PRODUCT, Double.class));
        sumOfSquares.set(dataSet.getField(Field.SUM_OF_SQUARES, Double.class));
        sumOfInverses.set(dataSet.getField(Field.SUM_OF_INVERSES, Double.class));
    }

    @Override
    public Object getField(final StatsSession session,
                           String name) {
        // Intern the name to allow fast reference equality checks
        name = name.intern();

        if (name == Field.PRODUCT) {
            return product.get();
        }
        if (name == Field.SUM_OF_SQUARES) {
            return sumOfSquares.get();
        }
        if (name == Field.SUM_OF_INVERSES) {
            return sumOfInverses.get();
        }
        if (name == Field.ARITHMETIC_MEAN) {
            return getArithmeticMean(session);
        }
        if (name == Field.GEOMETRIC_MEAN) {
            return getGeometricMean(session);
        }
        if (name == Field.HARMONIC_MEAN) {
            return getHarmonicMean(session);
        }
        if (name == Field.QUADRATIC_MEAN) {
            return getQuadraticMean(session);
        }
        if (name == Field.STANDARD_DEVIATION) {
            return getStandardDeviation(session);
        }

        return null;
    }

    @Override
    public void collectData(final StatsSession session, final DataSet dataSet) {
        dataSet.setField(Field.PRODUCT, product.get());
        dataSet.setField(Field.SUM_OF_SQUARES, sumOfSquares.get());
        dataSet.setField(Field.SUM_OF_INVERSES, sumOfInverses.get());
        dataSet.setField(Field.ARITHMETIC_MEAN,
                         getArithmeticMean(session));
        dataSet.setField(Field.GEOMETRIC_MEAN,
                         getGeometricMean(session));
        dataSet.setField(Field.HARMONIC_MEAN,
                         getHarmonicMean(session));
        dataSet.setField(Field.QUADRATIC_MEAN,
                         getQuadraticMean(session));
        dataSet.setField(Field.STANDARD_DEVIATION,
                         getStandardDeviation(session));
    }

    @Override
    public void clear() {
        product.set(1);
        sumOfInverses.set(0);
        sumOfSquares.set(0);
    }

    protected double getArithmeticMean(final StatsSession session) {
        final long n = session.getCommits();
        if (n <= 0) {
            return 0.0;
        }

        return session.getSum() / n;
    }

    protected double getGeometricMean(final StatsSession session) {
        final long n = session.getCommits();
        if (n <= 0) {
            return 0.0;
        }

        return Math.pow(product.get(), 1.0 / n);
    }

    protected double getHarmonicMean(final StatsSession session) {
        final long n = session.getCommits();
        final double soi = sumOfInverses.get();
        if (n <= 0 || soi <= 0) {
            return 0.0;
        }

        return n / soi;
    }

    protected double getQuadraticMean(final StatsSession session) {
        final long n = session.getCommits();
        if (n <= 0) {
            return 0.0;
        }

        return Math.sqrt(sumOfSquares.get() / n);
    }

    protected double getStandardDeviation(final StatsSession session) {
        final long n = session.getCommits();
        if (n <= 0) {
            return 0.0;
        }

        double valueSum = session.getSum();
        double nMinus1 = (n <= 1) ? 1 : n - 1;
        double numerator = sumOfSquares.get() - ((valueSum * valueSum) / n);

        return Math.sqrt(numerator / nMinus1);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /* NESTED CLASSES */

    public static interface Field {
        public static final String PRODUCT = "product";
        public static final String SUM_OF_INVERSES = "sumOfInverses";
        public static final String SUM_OF_SQUARES = "sumOfSquares";

        public static final String ARITHMETIC_MEAN = "aMean";
        public static final String GEOMETRIC_MEAN = "gMean";
        public static final String HARMONIC_MEAN = "hMean";
        public static final String QUADRATIC_MEAN = "qMean";
        public static final String STANDARD_DEVIATION = "stdDev";
    }
}
