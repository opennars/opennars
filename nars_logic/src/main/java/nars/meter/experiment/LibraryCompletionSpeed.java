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
 * Created by me on 7/16/15.
 */
public abstract class LibraryCompletionSpeed extends EGoal<TypedOrganism> {

    final static Map<String, Task> conditionsCache = new ConcurrentHashMap<>();
    private final String path;
    private final int maxCycles;

    public List<Variable> var;

    private List script;
    String scriptSrc;

    public LibraryCompletionSpeed(String path, int maxCycles) {
        super(path);
        this.path = path;
        this.maxCycles = maxCycles;
        this.scriptSrc = LibraryInput.getExample(path);
        this.script = LibraryInput.rawTasks(scriptSrc);


        var = Controls.reflect(LibraryCompletionSpeed.class, this);

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

    abstract TestNAR newNAR(TypedOrganism leakProgram);
}
