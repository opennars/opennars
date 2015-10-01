package nars.nar;

import nars.process.TaskProcess;

public class SingleStepNAR extends Default {

    public SingleStepNAR() {
        super();
    }

    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                task -> task.isInput() /* allow only input tasks*/,
                task -> TaskProcess.run(SingleStepNAR.this, task)
        );
        return input;
    }
}
