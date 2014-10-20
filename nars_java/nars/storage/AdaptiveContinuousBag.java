package nars.storage;

import nars.core.Memory;
import nars.core.Param.AtomicDurations;
import nars.entity.Item;


/**
 *  1. there is an input curve which maps a linear X value to the exponential curve i sent before
this is to approximate the priority -> probabilty assumption
2. the output value of this curve is used to find an index in the sorted item list
according to a set of control points, in this case i used 10
this is an array of values representing the priority at indexes
so each point is 100 indexes away from another, when the bag is full
it can interpolate these to find an approximate index of the sorted bag where the expected priority value (output from the first step) should be found
then it removes the item at this index
 *
 * @param <I>
 * @param <K>
 */
public class AdaptiveContinuousBag<I extends Item<K>, K> extends ContinuousBag2<I, K> {

    final int resolution = 10;
    double[] index = new double[resolution];
    private double minPriority;
    private double maxPriority;

    public AdaptiveContinuousBag(int capacity) {
        super(capacity, null, true);                
        
        reset();
    }

    public int posToIndex(final int res) {
        return posToIndex(((double) res) / ((double) resolution - 1));
    }

    public int posToIndex(final double p) {
        final int s = size();
        int x = (int) (p * s);
        if (x == s) {
            x = s - 1;
        }
        return x;
    }

    public void reset() {
        for (int i = 0; i < resolution; i++) {
            setIndex(i, i * (((double) getCapacity()) / (resolution - 1)));
        }
    }

    protected void update() {
        for (int i = 0; i < resolution; i++) {
            setIndex(i, items.exact(posToIndex(i)).getPriority());
        }
        minPriority = index[0];
        maxPriority = index[resolution - 1];
        //System.out.println(Arrays.toString(index));
        
    }

    
    protected void setIndex(final int i, final double v) {
        index[i] = v;
    }

    //probability curve that weights priority to proportion
    double p(final double x) {
        //ex:   1-e^(-5*x)
        return 1 - Math.exp(-5 * x);
    }

    int c(double x) {
        double y = p(x);
        //scale to within current priority range
        y = y * (maxPriority - minPriority) + minPriority;
        int i = closestIndex(y);
        return i;
    }

    public int closestIndex(double p) {
        //TODO interpolate between two indexes
        int r;
        int s = size();
        for (r = 0; r < resolution; r++) {
            if (index[r] > p) {
                break;
            }
        }
        if (r > resolution - 1) {
            r = resolution - 1;
        }
        int q = r - 1;
        if (q == -1) {
            q = 0;
        }
        double vq = index[q];
        double vr = index[r];
        double qr;
        if (p < vq) {
            qr = q;
        } else if (p > vr) {
            qr = r;
        } else {
            double dq = p - vq;
            double dr = vr - p;
            double t = dq + dr;
            if (t == 0) {
                qr = q;
            } else {
                dq /= t;
                dr /= t;
                qr = ((1.0 - dq) * q + (1.0 - dr) * r);
            }
        }
        //System.out.println(q + " " + vq + " || " + p + " || " + r + " " + vr + " :: " + qr);
        int i = (int) Math.floor(qr * ((double) s) / (resolution - 1));
        if (i < 0) {
            i = 0;
        }
        if (i >= s) {
            i = s - 1;
        }
        //System.out.println(/*"x=" + n2(x) + */" ..  y=" + p + "   r=" + qr + " @ i=" + i);
        return i;
    }

    @Override
    public int nextRemovalIndex() {
        update();
        final int s = size();
        if (randomRemoval) {
            //uniform random distribution on 0..1.0
            x = Memory.randomNumber.nextFloat();
        } else {
            x += scanningRate * 1.0f / (1 + s);
            if (x >= 1.0f) {
                x = x - 1.0f;
            }
            if (x <= 0.0f) {
                x = x + 1.0f;
            }
        }
        return c(x);
    }

}
