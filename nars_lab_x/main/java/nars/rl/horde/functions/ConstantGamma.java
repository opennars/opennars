package nars.rl.horde.functions;

public class ConstantGamma implements GammaFunction {
    private static final long serialVersionUID = -4493833693286307798L;
    private final double gamma;

    public ConstantGamma(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public double gamma() {
        return gamma;
    }
}