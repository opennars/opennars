package nars.storage;

/**
 * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
 * @param x input mappig value
 * @return
 */
public interface BagCurve {

    public double y(double x);
    
}
