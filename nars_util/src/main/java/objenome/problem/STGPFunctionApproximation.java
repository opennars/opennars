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
package objenome.problem;

import objenome.goal.DefaultProblemSTGP;
import objenome.goal.Observation;
import objenome.goal.SumOfError;
import objenome.op.Variable;
import objenome.solver.evolve.FitnessFunction;
import objenome.solver.evolve.TypedOrganism;

import java.util.Collections;
import java.util.Deque;

/**
 * Evolves a function that minimizes the total error of an expression
 * evaluated according to a set of sampled points.
 * @since 2.0
 */
public class STGPFunctionApproximation extends DefaultProblemSTGP {

    



    public final Deque<Observation<Double[], Double>> samples;
    private SumOfError fitness;


    public STGPFunctionApproximation(int populationSize, int expressionDepth, boolean arith, boolean trig, boolean exp, boolean piecewise) {
        super(populationSize, expressionDepth, arith, trig, exp, piecewise);

        samples = fitness.obs;
    }

    @Override
    protected FitnessFunction initFitness() {

        if (fitness == null) {
            fitness = new SumOfError() {
                @Override public void onNextBest(TypedOrganism s, double error) {
                    nextBest = s;
                    nextBestError = error;
                }
            };
        }

        return fitness;
    }


    @Override
    protected Iterable<Variable> initVariables() {
        return Collections.singleton(doubleVariable("X"));
    }

    TypedOrganism nextBest = null;
    double nextBestError = Double.NaN;

    public double getBestError() {
        return nextBestError;
    }

    public TypedOrganism getBest() {
        return nextBest;
    }




}
