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
 * The latest version is available from: http:/www.epochx.org
 */
package objenome.util;

import java.util.function.Function;
import objenome.goal.STGPBoolean.BooleanCases;

/**
 * This class provides correct solutions to various benchmark problems that can
 * be used within fitness functions
 *
 * @since 2.0
 */
public final class BenchmarkSolutions {

    private BenchmarkSolutions() {
    }

    /**
     * Calculates and returns the correct result of the target function used in
     * the cubic regression benchmark problem, for the given value of
     * <code>x</code>.
     *
     * The target function of the cubic regression function is: x + x^2 + x^3
     *
     * @param x the input value
     * @return the result of applying the function to the given value of
     * <code>x</code>
     */
    public static Double cubicRegression(Double x) {
        return x + x * x + x * x * x;
    }

    public final static Function<Double,Double> XpXXpXXX = new Function<Double,Double>() {
        @Override public Double apply(Double x) {
            return cubicRegression(x);
        }        
    };
    
    
    /**
     * Calculates and returns the correct result of the target function used in
     * the quartic regression benchmark problem, for the given value of
     * <code>x</code>.
     *
     * The target function of the quartic regression function is: x + x^2 + x^3
     * + x^4
     *
     * @param x the input value
     * @return the result of applying the function to the given value of
     * <code>x</code>
     */
    public static Double quarticRegression(Double x) {
        return cubicRegression(x) + x * x * x * x;
    }
    

    /**
     * Calculates and returns the correct result of the target function used in
     * the sextic regression benchmark problem, for the given value of
     * <code>x</code>.
     *
     * The target function of the sextic regression function is: x^6 - (2 * x^4)
     * + x^2
     *
     * @param x the input value
     * @return the result of applying the function to the given value of
     * <code>x</code>
     */
    public static Double sexticRegression(Double x) {
        return Math.pow(x, 6) - (2 * Math.pow(x, 4)) + Math.pow(x, 2);
    }

    /**
     * Calculates and returns the correct result of the multiplexer benchmark
     * problem for the given input values.
     *
     * The first <code>n</code> elements of the input array are the address bits
     * and the remaining <code>m</code> bits are the data bits. The following
     * must be true:
     *
     * <code>m = 2^m</code>
     *
     * If this will be called multiple times, it is more efficient to obtain the
     * number of address bits using {@link #multiplexerAddressBits(int)} and
     * pass them to the {@link #multiplexer(boolean[], int)} method.
     *
     * @param inputs an array of boolean inputs
     * @return the result of applying a correct multiplexer on the given inputs
     */
    public static Boolean multiplexer(Boolean[] inputs) {
        int noAddressBits = multiplexerAddressBits(inputs.length);

        return multiplexer(inputs, noAddressBits);
    }

    /**
     * Calculates and returns the correct result of the multiplexer benchmark
     * problem for the given input values and the number of address bits.
     *
     * The first <code>n</code> elements of the input array are the address bits
     * and the remaining <code>m</code> bits are the data bits. The following
     * must be true:
     *
     * <code>m = 2^m</code>
     *
     * @param inputs an array of boolean inputs
     * @param noAddressBits the number of address bits to expect in the inputs
     * array
     * @return the result of applying a correct multiplexer on the given inputs
     */
    public static Boolean multiplexer(Boolean[] inputs, int noAddressBits) {
        int dataPosition = 0;
        for (int i = 0; i < noAddressBits; i++) {
            if (inputs[i]) {
                dataPosition += Math.pow(2, i);
            }
        }

        return inputs[noAddressBits + dataPosition];
    }

    /**
     * Calculates and returns the number of address bits are required for a
     * given number of input bits when solving the multiplexer problem.
     *
     * @param noBits the total number of input bits, including both address and
     * data bits
     * @return the number of address bits
     * @throws IllegalArgumentException if the number of data bits is invalid,
     * given the number of address bits
     */
    public static int multiplexerAddressBits(int noBits) {
        int noAddressBits = 0;
        int noDataBits = 0;
        do {
            noAddressBits++;
            noDataBits = (int) Math.pow(2, noAddressBits);
        } while (noAddressBits + noDataBits < noBits);

        if ((noAddressBits + noDataBits) != noBits) {
            throw new IllegalArgumentException("The number of data bits must be 2 to the power of the number of address bits");
        }

        return noAddressBits;
    }
    
    public static BooleanCases multiplexerProblem(int bits) {
        int noAddressBits = BenchmarkSolutions.multiplexerAddressBits(bits);
        Boolean[][] inputValues = BooleanUtils.generateBoolSequences(bits);
        Boolean[] expectedOutputs = new Boolean[inputValues.length];
        for (int i = 0; i < inputValues.length; i++) {
            expectedOutputs[i] = BenchmarkSolutions.multiplexer(inputValues[i], noAddressBits);
        }
        return new BooleanCases(inputValues, expectedOutputs);
    }
        


    /**
     * Calculates and returns the correct result of the majority benchmark
     * problem for the given input values.
     *
     * @param inputs an array of boolean inputs
     * @return the result of applying majority on the given input values
     */
    public static Boolean majority(Boolean[] inputs) {
        int trueCount = 0;

        for (Boolean b : inputs) {
            if (b) {
                trueCount++;
            }
        }

        return (trueCount >= (inputs.length / 2));
    }

    /**
     * Calculates and returns the correct result of checking for even parity in
     * the given input values.
     *
     * @param inputs an array of boolean inputs
     * @return true if there is an even number of true values in the input array
     */
    public static Boolean evenParity(Boolean[] inputs) {
        int noTrues = 0;

        for (boolean b : inputs) {
            if (b) {
                noTrues++;
            }
        }

        return ((noTrues % 2) == 0);
    }

    /**
     * Calculates and returns the correct result of checking for odd parity in
     * the given input values.
     *
     * @param inputs an array of boolean inputs
     * @return true if there is an odd number of true values in the input array
     */
    public static Boolean oddParity(Boolean[] inputs) {
        return !evenParity(inputs);
    }
}
