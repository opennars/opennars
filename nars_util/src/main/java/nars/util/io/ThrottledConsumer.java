package nars.util.io;


/** a consumer which returns a value as a signal to the supplier
 *  allowing it to change sending rate or stop */
@FunctionalInterface
public interface ThrottledConsumer<X> {

    /**
     * @param next next sent item to be consumed
     * @return requested sending rate (items/sec). value <= 0 should end the transmission
     */
    float accept(X next);

}
