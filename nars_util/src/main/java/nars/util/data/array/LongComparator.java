package nars.util.data.array;

import java.util.Comparator;

/**
 * Created by me on 6/8/15.
 */
public interface LongComparator extends Comparator<Long> {
    /**
	 * Compares the given primitive types.
	 *
	 * @return A positive integer, zero, or a negative integer if the first
	 * argument is greater than, equal to, or smaller than, respectively, the
	 * second one.
	 * @see Comparator
	 */
	int compare(long k1, long k2);
}
