package nars.operate.meta;

/**
 * executes/evaluates each of the supplied arguments in one of various modes:
 *      sequential immediate (runs each item in sequence, blocking, then returns the result immediately)
 *      parallel immediate (can parallelize but blocks until they all finish)
 *      asynch sequential (#cycles delay, # cycles between arg, max # cycles)
 *      asynch parallel (starts each in a new thread, max time)
 *      ...
 */
public class Exe {
}
