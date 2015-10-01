package nars.process;

import java.util.function.Predicate;

/** reports a specified minimum, or maximum NAL level */
public interface Level {


    /** as a minimum value, 0 means that anything will be accepted. values above this will restrict
     * using by reasoners configured for an insufficiently high max level.
     */
    int nal();

    static Predicate<Level> max(final int level) {
        return (r -> (r.nal() <= level));
    }

    //TODO use IntStream.range?
    Predicate<Level>[] max = new Predicate[] {
        max(0), max(1), max(2), max(3), max(4), max(5), max(6), max(7)
    };

    Predicate<Level> AcceptAnyLevel = x -> true;

    public static Predicate<Level> maxFilter(final int maxNALlevel) {
        if (maxNALlevel < 8) {
            return max[maxNALlevel];
        }
        else {
            //if nal >= 8, anything will work, so dont bother testing
            return AcceptAnyLevel;
        }
    }

}
