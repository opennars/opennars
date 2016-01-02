/**
 * Copyright 2014 Prasanth Jayachandran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.data.bloom;

import java.util.ArrayList;
import java.util.List;


/**
 * BloomFilter is a probabilistic data structure for set membership check. BloomFilters are
 * highly space efficient when compared to using a HashSet. Because of the probabilistic nature of
 * bloom filter false positive (element not present in bloom filter but test() says true) are
 * possible but false negatives are not possible (if element is present then test() will never
 * say false). The false positive probability is configurable (default: 5%) depending on which
 * storage requirement may increase or decrease. Lower the false positive probability greater
 * is the space requirement.
 * Bloom filters are sensitive to number of elements that will be inserted in the bloom filter.
 * During the creation of bloom filter expected number of entries must be specified. If the number
 * of insertions exceed the specified initial number of entries then false positive probability will
 * increase accordingly.
 * <p/>
 * Internally, this implementation of bloom filter uses Murmur3 fast non-cryptographic hash
 * algorithm. Although Murmur2 is slightly faster than Murmur3 in Java, it suffers from hash
 * collisions for specific sequence of repeating bytes. Check the following link for more info
 * https://code.google.com/p/smhasher/wiki/MurmurHash2Flaw
 */
public class BloomFilter {
    private static final double DEFAULT_FPP = 0.05;
    private AwesomeBitSet bitSet;
    private final int m;
    private final int k;
    private final double fpp;
    private final long n;

    public BloomFilter(long maxNumEntries) {
        this(maxNumEntries, DEFAULT_FPP);
    }

    public BloomFilter(long maxNumEntries, double fpp) {
        assert maxNumEntries > 0 : "maxNumEntries should be > 0";
        assert fpp > 0.0 && fpp < 1.0 : "False positive percentage should be > 0.0 & < 1.0";
        this.fpp = fpp;
        n = maxNumEntries;
        m = optimalNumOfBits(maxNumEntries, fpp);
        k = optimalNumOfHashFunctions(maxNumEntries, m);
        bitSet = new AwesomeBitSet(m);
    }

    // deserialize bloomfilter. see serialize() for the format.
    public BloomFilter(List<Long> serializedBloom) {
        this(serializedBloom.get(0), Double.longBitsToDouble(serializedBloom.get(1)));
        List<Long> bitSet = serializedBloom.subList(2, serializedBloom.size());
        long[] data = new long[bitSet.size()];
        for (int i = 0; i < bitSet.size(); i++) {
            data[i] = bitSet.get(i);
        }
        this.bitSet = new AwesomeBitSet(data);
    }

    public void clear() {
        bitSet.clear();
    }

    static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    static int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    public long sizeInBytes() {
        return bitSet.bitSize() / 8;
    }

    public void add(byte[] val) {
        addBytes(val);
    }
    public boolean testBytes(byte[] val) {
        long hash64 = Murmur3Hash.hash64(val);
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);


        int k = this.k;
        int m = this.m;
        AwesomeBitSet bits = bitSet;

        for (int i = 1; i <= k; i++) {
            int combinedHash = combineHash(hash1, hash2, i);
            if (!bits.get( /*pos*/ combinedHash % m)) {
                return false;
            }
        }
        return true;
    }



    public final void addBytes(byte[] val) {
        // We use the trick mentioned in "Less Hashing, Same Performance: Building a Better Bloom Filter"
        // by Kirsch et.al. From abstract 'only two hash functions are necessary to effectively
        // implement a Bloom filter without any loss in the asymptotic false positive probability'

        // Lets split up 64-bit hashcode into two 32-bit hashcodes and employ the technique mentioned
        // in the above paper
        long hash64 = Murmur3Hash.hash64(val);
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);

        int k = this.k;
        int m = this.m;
        AwesomeBitSet bits = bitSet;

        for (int i = 1; i <= k; i++) {
            int combinedHash = combineHash(hash1, hash2, i);
            bits.set( /*pos*/ combinedHash % m );
        }
    }

    protected static int combineHash(int hash1, int hash2, int i) {
        int combinedHash = hash1 + (i * hash2);
        // hashcode should be positive, flip all the bits if it's negative
        if (combinedHash < 0) {
            combinedHash = ~combinedHash;
        }
        return combinedHash;
    }

    public void addString(String val) {
        addBytes(val.getBytes());
    }

    public void addByte(byte val) {
        addBytes(new byte[]{val});
    }

    public void addInt(int val) {
        // puts int in little endian order
        addBytes(intToByteArrayLE(val));
    }


    public void addLong(long val) {
        // puts long in little endian order
        addBytes(longToByteArrayLE(val));
    }

    public void addFloat(float val) {
        addInt(Float.floatToIntBits(val));
    }

    public void addDouble(double val) {
        addLong(Double.doubleToLongBits(val));
    }



    public boolean testString(String val) {
        return testBytes(val.getBytes());
    }

    public boolean testByte(byte val) {
        return testBytes(new byte[]{val});
    }

    public boolean testInt(int val) {
        return testBytes(intToByteArrayLE(val));
    }

    public boolean testLong(long val) {
        return testBytes(longToByteArrayLE(val));
    }

    public boolean testFloat(float val) {
        return testInt(Float.floatToIntBits(val));
    }

    public boolean testDouble(double val) {
        return testLong(Double.doubleToLongBits(val));
    }

    private static byte[] intToByteArrayLE(int val) {
        return new byte[]{(byte) (val >> 0),
                (byte) (val >> 8),
                (byte) (val >> 16),
                (byte) (val >> 24)};
    }

    private static byte[] longToByteArrayLE(long val) {
        return new byte[]{(byte) (val >> 0),
                (byte) (val >> 8),
                (byte) (val >> 16),
                (byte) (val >> 24),
                (byte) (val >> 32),
                (byte) (val >> 40),
                (byte) (val >> 48),
                (byte) (val >> 56),};
    }

    public int getBitSize() {
        return m;
    }

    public int getNumHashFunctions() {
        return k;
    }

    public double getFalsePositivePercent() {
        return fpp;
    }

    public long getExpectedNumEntries() {
        return n;
    }

    /**
     * First 2 entries are expected entries (n) and false positive percentage (fpp). fpp which is a
     * double is serialized as long. The entries following first 2 entries are the actual bit set.
     *
     * @return bloom filter as list of long
     */
    public List<Long> serialize() {
        List<Long> serialized = new ArrayList<>();
        serialized.add(n);
        serialized.add(Double.doubleToLongBits(fpp));
        for (long l : bitSet.getData()) {
            serialized.add(l);
        }
        return serialized;
    }

    /**
     * Check if the specified bloom filter is compatible with the current bloom filter.
     *
     * @param that - bloom filter to check compatibility
     * @return true if compatible false otherwise
     */
    public boolean isCompatible(BloomFilter that) {
        return this != that &&
                getBitSize() == that.getBitSize() &&
                getNumHashFunctions() == that.getNumHashFunctions();
    }

    /**
     * Merge the specified bloom filter with current bloom filter.
     * NOTE: Merge does not check for incompatibility. Use isCompatible() before calling merge().
     *
     * @param that - bloom filter to merge
     */
    public void merge(BloomFilter that) {
        bitSet.putAll(that.bitSet);
    }

}