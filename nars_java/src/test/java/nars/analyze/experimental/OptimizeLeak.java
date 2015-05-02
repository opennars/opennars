package nars.analyze.experimental;

import nars.ProtoNAR;
import nars.io.Texts;
import nars.nal.filter.ConstantDerivationLeak;
import nars.prototype.Default;

/**
 * Created by me on 5/1/15.
 */
public class OptimizeLeak {

    public static void main(String[] args) {
        final int cycles = 100;

        for (double p = 0.1; p <= 1.0; p += 0.3) {
            for (double d = 0.1; d <= 1.0; d += 0.3) {

                final double finalD = d;
                final double finalP = p;
                ProtoNAR b = new Default() {
                    @Override
                    protected void initDerivationFilters() {
                        final float DERIVATION_PRIORITY_LEAK = (float)finalP; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                        final float DERIVATION_DURABILITY_LEAK = (float)finalD; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                        getNALParam().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
                    }
                };

                ExampleScores e = new ExampleScores(b, cycles);
                System.out.println("COST: " + Texts.n4(p) + "*pri, " + Texts.n4(d) + "*dur: ==" + e.totalCost);
            }
        }
    }
}
