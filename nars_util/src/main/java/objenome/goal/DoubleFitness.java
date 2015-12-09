/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.goal;

import objenome.solver.evolve.Fitness;

import java.util.Comparator;

/**
 * Represents a <code>Fitness</code> score as a <code>double</code> value. The
 * natural ordering of a fitness represented as a <code>double</code> may be
 * either maximising or minimising. A maximising fitness considers an individual
 * with a larger fitness score as being fitter than one with a smaller score.
 * With a minimising fitness the opposite is true. Fitness scores may range from
 * Double.MIN_VALUE to Double.MAX_VALUE.
 */
public abstract class DoubleFitness implements Fitness {

    /**
     * The comparator used by the {@link Maximise} double fitness
     * implementation.
     */
    private static final Comparator<Double> MAXIMISE = Double::compare;

    /**
     * The comparator used by the {@link Minimize} double fitness
     * implementation.
     */
    private static final Comparator<Double> MINIMISE = (d1, d2) -> Double.compare(d2, d1);

    /**
     * The actual double fitness value.
     */
    private final double fitness;

    /**
     * Constructs a <code>DoubleFitness</code> with the specified value as the
     * fitness score.
     *
     * @param fitness the <code>double</code> value that represents the fitness
     */
    public DoubleFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Returns the <code>double</code> value of this fitness.
     *
     * @return the explicit fitness value this instance represents
     */
    public double getValue() {
        return fitness;
    }

    /**
     * Compares the quality of this fitness to the specified instance for order.
     * Returns a negative integer, zero, or a positive integer as this instance
     * represents the quality of an individual that is less fit, equally fit, or
     * more fit than the specified object. The given <code>Fitness</code> object
     * must be an instance of <code>DoubleFitness</code>.
     *
     * @param o an instance of <code>DoubleFitness</code> to compare against
     * this object for order
     * @return a negative integer, zero, or a positive integer as this object is
     * less fit than, equally fit as, or fitter than the specified object.
     */
    @Override
    public int compareTo(Fitness o) {
        if (getClass().isAssignableFrom(o.getClass())) {
            return comparator().compare(fitness, ((DoubleFitness) o).fitness);
        } else {
            throw new IllegalArgumentException("Expected " + getClass() + ", found " + o.getClass());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DoubleFitness) {
            DoubleFitness fitnessObj = (DoubleFitness) obj;

            return fitnessObj.fitness == fitness;
        }

        return false;
    }

    @Override
    public DoubleFitness clone() {
        try {
            return (DoubleFitness) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Returns a String representation of this fitness' <code>double</code>
     * value.
     *
     * @return a String representation of this fitness object
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + fitness + ')';
    }

    /*
     * Returns a <code>Comparator</code> that provides the ordering of fitness
     * values.
     *
     * @return a <code>Comparator</code> for ordering fitness values
     */
    public abstract Comparator<Double> comparator();

    /**
     * A <code>Fitness</code> score with a <code>double</code> value and a
     * maximising natural ordering.
     */
    public static class Maximise extends DoubleFitness {

        /**
         * Constructs a <code>DoubleFitness</code> with a maximising ordering.
         *
         * @param fitness {@inheritDoc}
         */
        public Maximise(double fitness) {
            super(fitness);
        }

        /**
         * Returns a <code>Comparator</code> that provides a maximising ordering
         * for <code>double</code> values.
         *
         * @return a <code>Comparator</code> for ordering double fitness values
         * with a maximising order.
         */
        @Override
        public Comparator<Double> comparator() {
            return MAXIMISE;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Maximise) && super.equals(obj);
        }
    }

    /**
     * A <code>Fitness</code> score with a <code>double</code> value and a
     * minimising natural ordering.
     */
    public static class Minimize extends DoubleFitness {

        /**
         * Constructs a <code>DoubleFitness</code> with a minimising ordering.
         *
         * @param fitness {@inheritDoc}
         */
        public Minimize(double fitness) {
            super(fitness);
        }

        /**
         * Returns a <code>Comparator</code> that provides a minimising ordering
         * for <code>double</code> values.
         *
         * @return a <code>Comparator</code> for ordering double fitness values
         * with a minimising order.
         */
        @Override
        public Comparator<Double> comparator() {
            return MINIMISE;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Minimize) && super.equals(obj);
        }
    }


}
