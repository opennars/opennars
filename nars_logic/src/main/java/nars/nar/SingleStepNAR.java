package nars.nar;

public class SingleStepNAR extends Default {

    public SingleStepNAR() {
        super();
    }

    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                task -> task.isInput() /* allow only input tasks*/,
                task -> exec(task)
        );
        return input;
    }
}
