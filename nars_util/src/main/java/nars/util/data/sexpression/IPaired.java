package nars.util.data.sexpression;

/** S-expression "Pair" (or "Cell") interface (read-only) */
public interface IPaired {

	/** car, the first element */
	Object _car();

	/** cdr, the remainder (null if non-existent in the case of an atom) */
	Object _cdr();

}
