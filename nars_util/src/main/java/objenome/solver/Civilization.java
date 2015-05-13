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
package objenome.solver;

import com.gs.collections.api.map.primitive.MutableObjectDoubleMap;
import com.gs.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import objenome.goal.DoubleFitness;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.solver.evolve.*;
import objenome.solver.evolve.event.EventManager;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.OnePointCrossover;
import objenome.solver.evolve.mutate.PointMutation;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.TournamentSelector;
import objenome.util.random.MersenneTwisterFast;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * A managed population of evolving organisms, each of which represents
 * a solution to a problem characterized by a set of goals defined
 * by the programmer.
 */
abstract public class Civilization<I extends AbstractOrganism> extends GPContainer {

    /**
     * The key for setting and retrieving the list of components.
     */
    public static final GPKey<ArrayList<PopulationProcess>> COMPONENTS = new GPKey<>();

    public final RandomSequence random = new MersenneTwisterFast();


    public final EventManager events = new EventManager();

    public final HashMap<Class<?>, Object> stat = new HashMap<>();

    private final ExecutorService exe;
    private final int threads;
    private final int populationSize;
    protected Pipeline pipeline;
    public final Population<I> population;

    public final List<EGoal<I>> goals = new ArrayList();
    CopyOnWriteArrayList<Life> lives = new CopyOnWriteArrayList<>();

    public Civilization(int threads, int populationSize) {
        super();
        this.threads = threads;
        this.populationSize = populationSize;

        exe = Executors.newFixedThreadPool(threads);

        the(Population.SIZE, populationSize);
        the(RandomSequence.RANDOM_SEQUENCE, random);
        the(TypedOrganism.RETURN_TYPE, Double.class);

        final ArrayList<Node> syntax = new ArrayList();
        syntax.addAll( getOperators(random) );
        the(TypedOrganism.SYNTAX, syntax.toArray(new Node[syntax.size()]));


        the(TypedOrganism.MAXIMUM_DEPTH, 8);
        the(Full.MAXIMUM_INITIAL_DEPTH, 5);
        the(OrganismBuilder.class, new Full());

        //the(Breeder.SELECTOR, new RouletteSelector());
        the(Breeder.SELECTOR, new TournamentSelector(7));

        List<OrganismOperator> operators = new ArrayList<>();
        {
            operators.add(new PointMutation());
            the(PointMutation.PROBABILITY, 0.1);
            the(PointMutation.POINT_PROBABILITY, 0.02);
            operators.add(new OnePointCrossover());
            the(OnePointCrossover.PROBABILITY, 0.1);
            operators.add(new SubtreeCrossover());
            the(SubtreeCrossover.PROBABILITY, 0.1);
            operators.add(new SubtreeMutation());
            the(SubtreeMutation.PROBABILITY, 0.1);
        }
        the(Breeder.OPERATORS, operators);

        population = new Population<I>(this) {

            @Override
            public void add(I individual) {
                super.add(individual);
                addLife(individual);
            }

        };

    }

    abstract public List<Node> getOperators(RandomSequence random);

    public void add(EGoal goal) {
        goals.add(goal);
    }



//    /** apply evolutionary pressure which characterize the creation of
//     *  new individuals desired by the civilization and selects the
//     *  individuals to eugenecize */
//    abstract void update(Object... evolutionaryPressures);
//
//    abstract I onBirth(I newborn);
//
//    abstract I onDeath(I newborn);
//
//    /** measures the raw cost to maintain an individual in the population.
//     *  this value can be interpreted as a rate
//     * */
//    abstract double cost(I individual);
//
//    /** measures the value an individual contributes to the population
//      *  towards a specific goal.  If unknown, the value returned
//      *  is Double.NaN which can be interpreted in some cases as zero.
//      *
//      *  even if dead, its genome can be saved for ressurrection in a
//      *  future era.
//     *
//      *  this value can be interpreted as a rate
//     *  */
//    abstract double value(I individual, Object goal);
//
//
//    /** specifies a component of evolutionary pressure which can
//     * be adjusted while a civilization executes.
//     */
//    public void setDemand(Object goal, double amount) {
//
//    }
//
//    /** performs a discrete evaluatation step of an individual.
//     * this is generally called repeatedly during
//     * an organism's lifetime.  each time it is given a new task to
//     * contribute to its performance evaluation.  before and afterwards
//     * certain actions may be applied to the individual:
//     *      --adjust its value estimates and compare with cost
//     *      --euthanize
//     *      --schedule for re-evaluation (increasing delay time lowers its priority and makes it more likely it will be euthanized in the meantime)
//     *
//     *
//     * */
//    public void live(I individual, double maxRealTime) {
//
//    }



    public void reset() {
        pipeline = null;
        population.clear();
    }

    /**
     * Performs an evolutionary run. Each component in the pipeline returned by
     * the <code>setupPipeline</code> method is processed in sequence. An empty
     * {@link Population} is provided to the first component, and each
     * succeeding component is supplied with the <code>Population</code>
     * returned by the previous component.
     *
     * @return a <code>Population</code> that is the result of processing the
     * pipeline of components, as returned by the final component in that
     * pipeline
     */
//    public Population<I> cycle() {
//        if (pipeline == null) {
//            pipeline = new Pipeline();
//            /* Initialises the supplied <code>Pipeline</code> with the components that
//             * an evolutionary run is composed of. The specific list of components used
//             * is obtained from the {@link Config}, using the appropriate <code>Class</code> */
//            for (PopulationProcess component : (Iterable<PopulationProcess>) the(COMPONENTS)) {
//                Civilization.setContainerAware(this, component);
//                pipeline.add(component);
//            }
//
//            //population = new Population<I>(this);
//        }
//
//        //config.fire(new StartRun(0));
//
//        population = pipeline.process(population);
//
//        //config.fire(new EndRun(0, population));
//
//        return population;
//    }


    public void run(double maxTimeSeconds) {


        updatePopulation();


        try {
            exe.awaitTermination((long)(maxTimeSeconds * 1000.0), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void addLife(I i) {
        Life l = new Life(i);
        lives.add(l);
        new Thread(l).start();
    }

    protected synchronized DescriptiveStatistics getCostStatistics() {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (Life l : lives) {
            ds.addValue(l.costRate());
        }
        return ds;
    }

    double minLifeSpan = 0.25;
    double maxSurvivalCostPercentile = 80;

    protected void reproduce(int n) {
        //System.out.println(x + " reproduce");
        BranchedBreeder b = new BranchedBreeder();
        n = Math.max(populationSize - (getPopulation().size() + n), 0);
        if (n == 0) return;
        try {
            b.update(getPopulation(), n);
            System.err.println("\nborn " + n);
        }
        catch (Exception e) {
            //System.err.println("reproduction accident: " + e);
            //e.printStackTrace();

            //give up reproducing for now
            //TODO fix this
        }
    }

    protected void die(Life x) {
        lives.remove(this);
        getPopulation().remove(x.i);
        System.out.println(x + " die @ " + x.getAge() );

        reproduce(1);
    }

    int cycle = 0;
    protected synchronized void updatePopulation() {
        //maintain population level
        if (population.size() == 0) {
            getOrganismBuilder().populate(getPopulation(), populationSize);
        }
        else {
            reproduce(1);
        }

        if (cycle % 64 == 0)
            System.err.println( "\n" + population.size() + " organisms between " + getCostStatistics().getMin() + ":min .. max:" + getCostStatistics().getMax() + "\n" );

        cycle++;
    }

    protected boolean shouldLive(Life x) {
        DescriptiveStatistics s = getCostStatistics();
        double pp = (x.costRate()),  maxSurvivalCostPercentileEstimate = s.getPercentile(maxSurvivalCostPercentile * x.getAge());

        boolean result;

        if (x.getAge() < minLifeSpan) {
            result = true;
        }
        else if (pp > maxSurvivalCostPercentileEstimate) {
            result = false;
        }
        else {
            //double minReproductionCostPercentileEstimate = s.getPercentile(minReproductionCostPercentile);
//            if (population.size() < populationSize && pp <= minReproductionCostPercentileEstimate) {
//                reproduce(x);
//            }
            result = true;
        }

        if (!result)
            die(x);

        updatePopulation();

        return result;
    }

    class Life implements Runnable {

        final MutableObjectDoubleMap<EGoal<I>> cost = new ObjectDoubleHashMap<EGoal<I>>().asSynchronized();

        double age = 0;

        public final I i;
        private double totalCost = 0;

        public Life(I i) {
            this.i = i;
        }


        @Override
        public String toString() {
            return i.toString() + " [" + age + ", " + (totalCost) + "]";
        }

        /** rate: cost/time */
        public double costRate() {
            if (age == 0) return 0.5;
            return totalCost / age;
        }

        protected void evaluate(EGoal<I> goal, double cost) {
            this.cost.put(goal, cost);
            totalCost = this.cost.sum();
            i.setFitness(new DoubleFitness.Minimize(costRate()));
        }

        /** returns true if still alive, false if dead */
        protected boolean age(double time) {
            //Thread.currentThread().setPriority(..);

            age+=time;

            //shouldReproduce(this);
            return shouldLive(this);
        }


        @Override public void run() {
            List<EGoal> goalSequence = new ArrayList(goals);
            Collections.shuffle(goalSequence);

            for (int j = 0; j < goalSequence.size(); j++) {
                final EGoal g = goalSequence.get(j);

                goalSequence.set(j, null); //GC assistance

                try {
                    double s = exe.submit(new Callable<Double>() {
                        @Override
                        public Double call() throws Exception {
                            //System.out.println(i + " evaluating " + g);
                            double s = g.cost(i);
                            //System.out.println("SCORE=" + s);
                            return s;
                        }
                    }).get();

                    evaluate(g, s);

                    if (!age(1.0 / goalSequence.size())) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //evaluate(g, maxCycles)
                    break;
                }
            }

            die(this);
            updatePopulation();
        }

        /** age of the organism; 0=birth, 1=retired (finished all goals) */
        public double getAge() {
            return age;
        }
    }



    public OrganismBuilder<I> getOrganismBuilder() {
        return the(OrganismBuilder.class);
    }


    public Population<I> getPopulation() {
        return population;
    }

    public static interface GPContainerAware {

        public void setConfig(Civilization c);
    }

//    /**
//     * the involved syntax elements
//     */
//    public Node[] getSyntax() {
//        return get(TypedOrganism.SYNTAX);
//    }

    /**
     * the involved variables, which are obtained by iterating syntax elements.
     */
    public Variable[] getVariables() {

        List<Variable> variables = new ArrayList();

        for (Node n : getSyntax()) {
            if (n instanceof VariableNode) {
                variables.add(((VariableNode) n).getVariable());
            }
        }

        return variables.toArray(new Variable[variables.size()]);
    }

//    /**
//     * Removes the specified <code>AbstractStat</code> from the repository.
//     *
//     * @param type the class of <code>AbstractStat</code> to be removed.
//     */
//    public <E extends Event> void remove(Class<? extends AbstractStat<E>> type) {
//        super.remove(type);
//        if (stat.containsKey(type)) {
//            AbstractStat<E> stat = type.cast(this.stat.remove(type));
//            off(stat.getEvent(), stat.getListener());
//            off(stat.getClearEvent(), stat.getListener());
//        }
//    }
//    /** define a statistic to collect, creating a singleton component keyed by its class */
//    public <X extends Object & Event> AbstractStat<X> stat(Class<? extends AbstractStat<X>> type) {
//
//        
//        // if the repository already contains an instance of the specified stat,
//        // we do not create a new one; otherwise, we create a new instance and
//        // register its listener in the EventManager
//        if (!stat.containsKey(type)) {
//            try {
//                AbstractStat<X> s = the(type);
//                this.stat.put(type, s);
//                on(s.getEvent(), s.listener);
//                return s;
//            } catch (Exception e) {
//                throw new RuntimeException("Could not create an instance of " + type, e);
//            }
//        }
//        return the(type);
//
//    }

    /**
     * Sets the value of the specified configuration key. If the given key
     * already has a value associated with it, then it will be overwritten. The
     * value is constrained to be of the correct object type as defined by the
     * generic type of the key. Calling this method will trigger the firing of a
     * new configuration event after the configuration option has been set.
     *
     * @param key the <code>ConfigKey</code> for the configuration parameter
     * that a new value is to be set for
     * @param value the new value to set for the specified configuration key
     */
    public <T> Civilization set(GPKey<T> key, T value) {
        setContainerAware(this, value);
        remove(key);
        the(key, value);
        //fire(new ConfigEvent(this, key));
        return this;
    }

    public static void setContainerAware(Civilization config, Object value) {
        if (value instanceof GPContainerAware) {
            ((GPContainerAware) value).setConfig(config);
        }
        if (value instanceof Iterable) {
            Iterable ii = (Iterable) value;
            for (Object i : ii) {
                if (i instanceof GPContainerAware) {
                    ((GPContainerAware) i).setConfig(config);
                }
            }
        }
    }

    /**
     * convenience method
     */
    public <T> Civilization with(GPKey<T> key, T value) {
        return set(key, value);
    }

//    /**
//     * Retrieves the value of the configuration parameter associated with the
//     * specified key. If no value has been set for the given key then
//     * <code>null</code> is returned.
//     *
//     * @param key the <code>ConfigKey</code> for the configuration parameter to
//     * retrieve
//     * @return the value of the specified configuration parameter, or
//     * <code>null</code> if it has not been set. The object type is defined by
//     * the generic type of the key.
//     */
//    public <T> T get(GPKey<T> key) {
//        return get(key, null);
//    }
//    /**
//     * Retrieves the value of the configuration parameter associated with the
//     * specified key. If the parameter has not been set, the value will be
//     * retrived from the <code>Template</code> object.
//     *
//     * @param key the <code>ConfigKey</code> for the configuration parameter to
//     * retrieve
//     * @param defaultValue the default value to be returned if the parameter has
//     * not been set
//     * @return the value of the specified configuration parameter, or
//     * <code>null</code> if it has not been set. The object type is defined by
//     * the generic type of the key.
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T get(GPKey<T> key, T defaultValue) {
//        T value = (T) prop.get(key);
//
//        if (value == null) {
//            STProblem template = (STProblem) prop.get(STProblem.PROBLEM);
//            return (template == null) ? defaultValue : template.get(key, defaultValue);
//        }
//
//        return value;
//    }
    /**
     * Removes all configuration parameter mapping. The configuration will be
     * empty this call returns.
     */
    @Override
    public void clearCache() {
        resetStats();
        //prop.clear();
    }


    /**
     * Instances of <code>ConfigKey</code> are used to uniquely identify
     * configuration parameters. The generic type <code>T</code> defines a
     * constraint upon the object type of the parameter's value.
     *
     * @param <T> the required object type of values for this parameter
     */
    @Deprecated public static class GPKey<T> {
    }

}
