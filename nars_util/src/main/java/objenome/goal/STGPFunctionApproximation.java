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
package objenome.goal;

import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.op.math.*;
import objenome.op.trig.Sine;
import objenome.solver.evolve.*;
import objenome.solver.evolve.fitness.DoubleFitness;
import objenome.solver.evolve.fitness.SumOfError;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.PointMutation;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.TournamentSelector;
import objenome.util.random.MersenneTwisterFast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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

        if (this.fitness == null) {
            this.fitness = new SumOfError() {
                @Override public void onNextBest(STGPIndividual s, double error) {
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

    STGPIndividual nextBest = null;
    double nextBestError = Double.NaN;

    public double getBestError() {
        return nextBestError;
    }

    public STGPIndividual getBest() {
        return nextBest;
    }




}
