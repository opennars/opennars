package nars.util.data.buffer;


import java.util.function.Supplier;

/**
 * An attached Input supplier and Attention Allocation State
 */
public interface Source<Y> extends Supplier<Y> {

    default void stop() {

    }

    //    /** takes input object. any tasks that it generates are input into the InPort
//     *  via queue() methods.
//     */
//    abstract public void perceive(X x);

    /*public Iterator<Y> postprocess(final Iterator<Y> yy) {
        return yy;
    }*/


    //    /** add a task to the end of the buffer */
//    protected boolean queue(Y task) {
//        if (task instanceof Reboot) {
//            buffer.clear();
//            return false;
//        }
//
//        if (task!=null)
//            return buffer.add(task);
//        return false;
//    }
//
//    @Override
//    public boolean hasNext() {
//        if (buffer == null) {
//            return !input.finished(false);
//        }
//
//        return buffer.size() > 0;
//    }
    
//    protected X nextXDirect() {
//        try {
//            if (input.finished(false))
//                return null;
//            
//            X x = input.next();
//            if (x == null)
//                return null;
//            return x;
//        }
//        catch (IOException e) {
//            return null;
//        }
//            
//    }
    

//    public void update() throws IOException {
//        if (buffer == null) return;
//
//        while (!input.finished(false) && (buffer.available() > 0) ) {
//            X x = input.next();
//            if (x == null)
//                continue;
//
//            perceive(x);
//        }
//    }


    
//    public boolean finish() {
//        return input.finished(true);
//    }
//
//    public boolean finished() {
//        if (buffer!=null)
//            if (buffer.size() > 0)
//                return false;
//
//        return input.finished(false);
//    }
    
//    public Y next() {
//        /*if (buffer == null)
//            return nextXDirect();*/
//
//        Y n = buffer.poll();
//
//        //TODO update statistics
//
//        return n;
//    }
    
//    /** empties the buffer */
//    public void reset() {
//        buffer.clear();
//    }
//
//    public int getItemsBuffered() {
//        return buffer.size();
//    }
    
    //public float getMass(X input) // allows variable weighting of input items; default=1.0
    
    //public double getInputMassRate(double windowSeconds); // calculates throughput rate in mass/sec within a given past window size, using an internal histogram of finite resolution

    
    
    
}
