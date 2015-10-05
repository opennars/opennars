package nars.nar;

public class SingleStepNAR extends Default {

    public SingleStepNAR() {
        super(1024, 1, 1, 3);
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
