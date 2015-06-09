package nars.util.data.id;

/**
 * Created by me on 6/8/15.
 */
public interface Identified extends Named<Identifier> {
    /**
     * allows a host of an identifier to replace its identifier
     * with an instance known to be equal, effectively
     * removing duplicates from the system.
     */
    default void identifierEquals(Identifier other) {

    }
}
