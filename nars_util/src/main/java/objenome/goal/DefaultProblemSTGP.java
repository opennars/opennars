package objenome.goal;

import nars.util.data.random.MersenneTwisterFast;
import objenome.op.DoubleVariable;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.op.math.*;
import objenome.op.trig.Sine;
import objenome.problem.ProblemSTGP;
import objenome.solver.evolve.*;
import objenome.solver.evolve.init.Full;
import objenome.solver.evolve.mutate.OnePointCrossover;
import objenome.solver.evolve.mutate.PointMutation;
import objenome.solver.evolve.mutate.SubtreeCrossover;
import objenome.solver.evolve.mutate.SubtreeMutation;
import objenome.solver.evolve.selection.RouletteSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * ProblemSTGP with some generally useful default settings
 */
public abstract class DefaultProblemSTGP extends ProblemSTGP {

    public DefaultProblemSTGP(int populationSize, int expressionDepth, boolean arith, boolean trig, boolean exp, boolean piecewise) {

        the(Population.SIZE, populationSize);

        //List<TerminationCriteria> criteria = new ArrayList<>();
        //criteria.add(new MaximumGenerations());

        //the(EvolutionaryStrategy.TERMINATION_CRITERIA, criteria);
        //the(MaximumGenerations.MAXIMUM_GENERATIONS, 1);

        the(TypedOrganism.MAXIMUM_DEPTH, expressionDepth);

        the(Breeder.SELECTOR, new RouletteSelector());
        //the(Breeder.SELECTOR, new TournamentSelector(7));

        List<OrganismOperator> operators = new ArrayList<>();
        operators.add(new PointMutation());
        the(PointMutation.PROBABILITY, 0.1);
        the(PointMutation.POINT_PROBABILITY, 0.02);
        operators.add(new OnePointCrossover());
        the(OnePointCrossover.PROBABILITY, 0.1);
        operators.add(new SubtreeCrossover());
        the(SubtreeCrossover.PROBABILITY, 0.1);
        operators.add(new SubtreeMutation());
        the(SubtreeMutation.PROBABILITY, 0.1);
        the(Breeder.OPERATORS, operators);

        double elitismRate = 0.2;
        the(BranchedBreeder.ELITISM, (int)Math.ceil(populationSize * elitismRate));




        the(Initialiser.METHOD, new Full());
        //the(Initialiser.METHOD, new RampedHalfAndHalf());

        RandomSequence randomSequence = new MersenneTwisterFast();
        the(RandomSequence.RANDOM_SEQUENCE, randomSequence);


        ArrayList syntax = new ArrayList();

        //+2.0 allows it to grow
        syntax.add( new DoubleERC(randomSequence, -1.0, 2.0, 2));

        if (arith) {
            syntax.add(new Add());
            syntax.add(new Subtract());
            syntax.add(new Multiply());
            syntax.add(new DivisionProtected());
        }
        if (trig) {
            syntax.add(new Sine());
            //syntax.add(new Tangent());
        }
        if (exp) {
            //syntax.add(new LogNatural());
            //syntax.add(new Exp());
            syntax.add(new Power());
        }
        if (piecewise) {
            syntax.add(new Min2());
            syntax.add(new Max2());
            syntax.add(new Absolute());
        }

        for (Variable v : initVariables())
            syntax.add(new VariableNode(v));


        the(TypedOrganism.SYNTAX, syntax.toArray(new Node[syntax.size()]));
        the(TypedOrganism.RETURN_TYPE, Double.class);

        the(FitnessEvaluator.FUNCTION, initFitness());
    }


    protected abstract FitnessFunction initFitness();
    protected abstract Iterable<Variable> initVariables();

    public static DoubleVariable doubleVariable(String n) {
        return new DoubleVariable(n);
    }

}
