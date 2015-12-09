package nars.util.data.random;

import java.util.Random;

//Extremely fast, medium quality randomness: http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml
public class XORShiftRandom extends Random {
    
    public static final XORShiftRandom global = new XORShiftRandom();


    private long seed;

    public XORShiftRandom() {
        seed = System.nanoTime();
    }

    public XORShiftRandom(long seed) {
        super(seed);
        this.seed = seed;
    }


    protected long nextLong(int nbits) {
        // TODO Not thread-safe but if this function was synchronized it should be, do that in a subclass
        long x = seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        seed = x;
        x &= ((1L << nbits) - 1);
        return x;
    }



    @Override
    public void setSeed(long newSeed) {
        seed = newSeed;
    }

}
