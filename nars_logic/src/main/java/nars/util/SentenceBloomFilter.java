package nars.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import nars.nal.Sentence;
import nars.nal.Truth;

/**
 * http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/hash/BloomFilter.html
 */
public class SentenceBloomFilter implements Funnel<Sentence> {

    /*
    Your false positive rate will be approximately (1 - e^(-kn/m))^k
    k = # hash functions, m = # bits, n = # items
    so you can just plug the number n of elements you expect to insert, and try various values of k and m to configure your filter for your application.2
    */
    final BloomFilter<Sentence> filter;

    public SentenceBloomFilter(int n) {
        filter = BloomFilter.create(this, n);
    }

    @Override
    public void funnel(Sentence s, PrimitiveSink into) {
        s.hash(into);
    }
}