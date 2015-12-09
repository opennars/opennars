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
package objenome.solver.evolve.selection;

import nars.util.data.random.XORShiftRandom;
import objenome.goal.DoubleFitness;
import objenome.solver.evolve.*;

import java.util.Random;

/**
 * This class represents an {@link IndividualSelector} that selects individuals
 * with a probability proportional to their fitness. In order to calculate a
 * probability, individuals must have a {@link DoubleFitness} value.
 */
public class RouletteSelector extends AbstractSelector {

    static final Random rng = XORShiftRandom.global;

    /**
     * The individuals' selection probabilities.
     */
    private double[] roulette;

    /**
     * Compute the individuals' selection probabilities.
     *
     * @param population the current population.
     */
    @Override
    public void init(Population population) {
        if (population.size() == 0) return;

        Fitness best = population.get(0).getFitness();
        Fitness worst = best;

        if (best == null) {
            //no fitness information available yet
            return;
        }

        if (!(best instanceof DoubleFitness)) {
            throw new IllegalArgumentException("Fitness not supported: " + best.getClass());
        }

        double[] rr = roulette = new double[population.size()];
        double total = 0.0;

        for (int i = 0; i < population.size(); i++) {
            Fitness fitness = population.get(i).getFitness();
            if (fitness.compareTo(best) > 0) {
                best = fitness;
            } else if (fitness.compareTo(worst) < 0) {
                worst = fitness;
            }

            rr[i] = ((DoubleFitness) fitness).getValue();
            total += rr[i];
        }

        double bestValue = ((DoubleFitness) best).getValue();
        double worstValue = ((DoubleFitness) worst).getValue();

        // invert if minimising - using adjusted fitness.
        if (bestValue < worstValue) {
            total = 0.0;
            double delta = (bestValue < 0) ? Math.abs(bestValue) : 0.0;
            for (int i = 0; i < population.size(); i++) {
                rr[i] = 1 / (1 + delta + rr[i]);
                total += rr[i];
            }
        }

        // normalise roulette values and accumulate.
        double cumulative = 0.0;
        for (int i = 0; i < population.size(); i++) {
            rr[i] = cumulative + (rr[i] / total);
            cumulative = rr[i];
        }
        rr[population.size() - 1] = 1.0;

        super.init(population);
    }

    /**
     * Returns an individual using the fitness proportionate selection strategy.
     *
     * @return an individual using the fitness proportionate selection strategy.
     */
    @Override
    public Organism select() {
        //double random = ((RandomSequence)population.getConfig().get(RANDOM_SEQUENCE)).nextDouble();

        double random = rng.nextDouble();

        double[] rr = roulette;
        if (rr == null) return null;
        int nr = rr.length;
        for (int i = 0; i < nr; i++) {
            if (random < rr[i]) {
                return population.get(i);
            }
        }

        throw new IllegalStateException("Illegal roulette probabilities");
    }

}
