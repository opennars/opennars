package nars.rl.horde.functions;

public class ConstantOutcomeFunction implements OutcomeFunction {
    private static final long serialVersionUID = -839152208374863448L;
    private final double outcome;

    public ConstantOutcomeFunction(double outcome) {
        this.outcome = outcome;
    }

    @Override
    public double outcome() {
        return outcome;
    }
}
