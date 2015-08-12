package nars.meter.experiment;

import nars.io.in.LibraryInput;
import nars.meter.TestNAR;
import nars.meter.condition.OutputCondition;
import nars.task.Task;
import objenome.op.Variable;
import objenome.solver.Controls;
import objenome.solver.EGoal;
import objenome.solver.evolve.TypedOrganism;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Measures the "speed" a particular NAR completes a set of unit test scripts.
 * In terms of how many cycles to reach the required conditions.
 * If the conditions are not satisfied within the provided maximum cycle
 * limit, the score is equal this worst case.  Otherwise if it completes sooner,
 * then the cost is proportional to the number of cycles that it required
 * (cost being inversely proportional to reward or score).
 */
public abstract class TestCompletionSpeed extends EGoal<TypedOrganism> {

    final static Map<String, Task> conditionsCache = new ConcurrentHashMap<>();

    private final String path;
    private final int maxCycles;

    public List<Variable> var;

    private List script;
    String scriptSrc;

    public TestCompletionSpeed(String path, int maxCycles) {
        super(path);
        this.path = path;
        this.maxCycles = maxCycles;
        this.scriptSrc = LibraryInput.getExample(path);
        this.script = LibraryInput.rawTasks(scriptSrc);


        var = Controls.reflect(TestCompletionSpeed.class, this);

    }

    @Override
    public double cost(TypedOrganism leakProgram) {


        TestNAR nar = newNAR(leakProgram);

        nar.requires.addAll(OutputCondition.getConditions(nar, scriptSrc, 0, conditionsCache));

        //nar.input(new TextInput(nar.textPerception, script));
        nar.input(LibraryInput.getExampleInput(script, nar.memory));

        nar.run(maxCycles);

        double cost = nar.getCost();
        if (Double.isInfinite(cost))
            cost = 1.0;

        nar.delete();

        return cost / maxCycles;
    }

    public abstract TestNAR newNAR(TypedOrganism leakProgram);
}
