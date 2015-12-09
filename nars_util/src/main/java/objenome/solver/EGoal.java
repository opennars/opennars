package objenome.solver;

/**
 * Quantifiable Experiment - characterizes a problem and its possible solutions
 *
 * A solution is modeled as a Statically-typed Genetic-Programming "Organism":
 *   VARIABLES
 *     All unknown parameters values are assigned by a Variable node.
 *
 *     The set of these define the parameter space in which solutions
 *     exist.  The following Variable meta-parameters define a Variable:
 *
 *       ID:
 *          --Name, allowing it to be identified when it assigns it value in
 *              evaluation a solution
 *          --Data Type (ie. primitive data types like numbers)
 *
 *       Optional:
 *          --(optional) predicate function determining if the value is invalid,
 *              in which case the organism dies
 *          --Data range: Minimum and Maximum possible values
 *
 *   META-VARIABLES
 *      Meta-variables operate on certain other variables to iteratively
 *      adjust their values during the iterative evaluation of an organism.
 *
 *      They depend on one or more other variables and their current values
 *      to determine a changing output value of a certain type.  A meta-variable
 *      can hold state between iterations for learning purposes.
 *
 *          --Scalar Numeric Optimization of an additionally supplied Evaluation Function
 *              -Set of existing variables and their current values constant binding in the Evaluation Function
 *              -One or more unknown variables are the parameters to this function,
 *                 and their ideal values become the current values of the Organism's
 *                 variables used in the main evaluation
 *              -Maximize or Minimize
 *              -Optimizer model parameters: precision limits, heuristics, etc
 *
 *          --Backpropagation constraint
 *              -Set of existing variables and their binding to a Feedforward Neural Network
 *                --Input, Hidden, or Output?
 *              -Training data
 *
 *          --Recurrent NN?
 *
 *          --Reinforcement Learning??
 *
 *
 * A solution which is evaluated in the following optional ways:
 *
 *   --prior to any testing, to immediately disqualify invalid candidates
 *   --after each non-final iteration
 *   --after the final iteration, if more than one cycles are iterated
 *
 * TODO use a set of Java Annotations to reflectively analyze
 * implementations of this class/interface to construct a problem/solution
 * model that can be evaluated in the Civilization system.
 *
 */
public abstract class EGoal<I> {

    public final String id;

    public EGoal(String id) {
        this.id = id;
    }

    /** inversely proportional to score or reward; generally minimized */
    public abstract double cost(I o);

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((EGoal)obj).id);
    }
}
